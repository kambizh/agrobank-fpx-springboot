package com.imocha.fpx.security;

import com.imocha.fpx.service.AgrobankFpxEndpointAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.context.ApplicationContext;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class AgrobankFpxEndpointAuthHandler implements CallbackHandler {
    
    private final ApplicationContext applicationContext;
    
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callback;
                
                if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
                    String username = pc.getIdentifier();
                    log.info("Authenticating <wsse:Username>{}</wsse:Username>", username);
                    
                    try {
                        AgrobankFpxEndpointAuthService authService = 
                            applicationContext.getBean(AgrobankFpxEndpointAuthService.class);
                        
                        // Get current SOAP message
                        SoapMessage soapMessage = (SoapMessage) PhaseInterceptorChain.getCurrentMessage();
                        
                        // Retrieve password from SOAP header
                        String wssePwd = authService.getSoapHeaderInfoByLocalName(soapMessage, "Password");
                        
                        // Get encryption key
                        String encryptionKey = authService.getEncryptionKey();
                        
                        // Decrypt password
                        String clearTxtPwd;
                        try {
                            clearTxtPwd = authService.decryptPass(wssePwd.getBytes("UTF-8"), encryptionKey);
                        } catch (Exception ex) {
                            log.error("<wsse:Username>{}</wsse:Username> : Unable to decrypt password", username, ex);
                            throw new UnsupportedCallbackException(callback, "Unable to decrypt password");
                        }
                        
                        // Validate password
                        String userPwd = authService.findAuthUserPwd(username);
                        boolean match = false;
                        
                        if (userPwd != null && userPwd.equals(authService.sha256Hex(clearTxtPwd))) {
                            match = true;
                        }
                        
                        if (match) {
                            // Set password to mark authentication successful
                            pc.setPassword(wssePwd);
                            log.info("<wsse:Username>{}</wsse:Username> : Password authentication was successful", username);
                            return;
                        } else {
                            log.info("<wsse:Username>{}</wsse:Username> : Password authentication was unsuccessful", username);
                            throw new UnsupportedCallbackException(callback, "Password authentication was unsuccessful");
                        }
                        
                    } catch (Exception e) {
                        if (e instanceof UnsupportedCallbackException) {
                            throw (UnsupportedCallbackException) e;
                        }
                        log.error("Error during authentication for user: {}", username, e);
                        throw new UnsupportedCallbackException(callback, "Authentication error: " + e.getMessage());
                    }
                }
            }
        }
    }
}
