package com.imocha.fpx;

import com.imocha.fpx.endpoint.AgrobankFpxEndpoint;
import com.imocha.fpx.model.AgrobankFpxMessageResponse;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.security.Security;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class TestSoapClient {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public static void main(String[] args) throws Exception {
        // Create JAX-WS client
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(AgrobankFpxEndpoint.class);
        factory.setAddress("http://localhost:8080/soap/AgrobankFpxService");
        
        // Configure WS-Security
        Map<String, Object> outProps = new HashMap<>();
        outProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        outProps.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(ConfigurationConstants.USER, "testuser");
        outProps.put(ConfigurationConstants.PW_CALLBACK_REF, new ClientPasswordCallback());
        
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        factory.getOutInterceptors().add(wssOut);
        
        // Create client
        AgrobankFpxEndpoint client =
            (AgrobankFpxEndpoint) factory.create();
        
        // Add custom headers
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, java.util.List<String>> headers = new HashMap<>();
        headers.put("CLIENT_IP", java.util.Arrays.asList("192.168.1.100"));
        headers.put("USERNAME", java.util.Arrays.asList("testuser"));
        headers.put("CLIENT_NDC", java.util.Arrays.asList("TEST-NDC-123"));
        cxfClient.getRequestContext().put(org.apache.cxf.message.Message.PROTOCOL_HEADERS, headers);
        
        // Prepare test message
        String testMessage = "<fpxMessage>Test FPX Message</fpxMessage>";
        byte[] encodedMessage = Base64.getEncoder().encode(testMessage.getBytes());
        
        // Call the service
        System.out.println("Calling SOAP service...");
        AgrobankFpxMessageResponse response =
            client.validateInitFpxMessageB2C(encodedMessage, "TestSender", "TXN-" + System.currentTimeMillis());
        
        // Print response
        System.out.println("Response Code: " + response.getResponseCode());
        System.out.println("Ref ID: " + response.getRefId());
        System.out.println("Transaction Ref: " + response.getTxnRefNum());
        
        if (response.getErrorCode() != null) {
            System.out.println("Error Code: " + response.getErrorCode());
            System.out.println("Error Description: " + response.getErrorDescription());
        }
    }
    
    static class ClientPasswordCallback implements CallbackHandler {
        @Override
        public void handle(Callback[] callbacks) {
            for (Callback callback : callbacks) {
                if (callback instanceof WSPasswordCallback) {
                    WSPasswordCallback pc = (WSPasswordCallback) callback;
                    if ("testuser".equals(pc.getIdentifier())) {
                        try {
                            // Encrypt the password
                            String encryptedPwd = encryptPassword("password", "mySecretKey123");
                            pc.setPassword(encryptedPwd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        private String encryptPassword(String password, String key) throws Exception {
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setProviderName("BC");
            encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
            encryptor.setPassword(key);
            return encryptor.encrypt(password);
        }
    }
}
