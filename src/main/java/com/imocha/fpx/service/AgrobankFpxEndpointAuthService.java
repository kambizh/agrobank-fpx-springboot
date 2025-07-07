package com.imocha.fpx.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgrobankFpxEndpointAuthService {
    
    public static final String CONFIG_PARAM_MODULE_AGROFPXWSSE = "AGROFPXWSSE";
    public static final String CONFIG_PARAM_CODE_ENCRYPTION_KEY = "encryptionKey";
    
    private final ConfigParamService configParamService;
    
    @PostConstruct
    public void init() {
        // Add BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());
    }
    
    public String encryptPass(byte[] password, String key) throws Exception {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setProviderName("BC");
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setPassword(key);
        
        String encryptedPass = encryptor.encrypt(new String(password, StandardCharsets.UTF_8));
        return encryptedPass;
    }
    
    public String decryptPass(byte[] password, String key) throws Exception {
        StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
        decryptor.setProviderName("BC");
        decryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        decryptor.setPassword(key);
        
        String decryptedPass = decryptor.decrypt(new String(password, StandardCharsets.UTF_8));
        return decryptedPass;
    }
    
    public String getSoapHeaderInfoByLocalName(SoapMessage message, String localName) {
        List<Header> headers = message.getHeaders();
        String info = "";
        
        for (Header header : headers) {
            if (header == null) {
                continue;
            }
            
            Object headerObject = header.getObject();
            if (!(headerObject instanceof Element)) {
                continue;
            }
            
            Element element = (Element) headerObject;
            NodeList nodeList = element.getElementsByTagName("*");
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                
                if (node.getNodeType() == Node.ELEMENT_NODE 
                    && localName.equalsIgnoreCase(node.getLocalName())) {
                    String textContent = node.getTextContent();
                    if (textContent != null && !textContent.isEmpty()) {
                        info = textContent;
                        break;
                    }
                }
            }
        }
        
        return info;
    }
    
    public String sha256Hex(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    public String saveAuthUserPwd(String username, String pwd) throws NoSuchAlgorithmException {
        String hex = sha256Hex(pwd);
        configParamService.saveConfigParam(CONFIG_PARAM_MODULE_AGROFPXWSSE, username, hex);
        return hex;
    }
    
    public String findAuthUserPwd(String username) {
        return configParamService.getValueByModuleAndCode(CONFIG_PARAM_MODULE_AGROFPXWSSE, username);
    }
    
    public String getEncryptionKey() {
        return configParamService.getValueByModuleAndCode(
            CONFIG_PARAM_MODULE_AGROFPXWSSE, 
            CONFIG_PARAM_CODE_ENCRYPTION_KEY
        );
    }
    
    public String saveEncryptionKey(String key) throws NoSuchAlgorithmException {
        String hex = sha256Hex(key);
        configParamService.saveConfigParam(
            CONFIG_PARAM_MODULE_AGROFPXWSSE, 
            CONFIG_PARAM_CODE_ENCRYPTION_KEY, 
            hex
        );
        return hex;
    }
}
