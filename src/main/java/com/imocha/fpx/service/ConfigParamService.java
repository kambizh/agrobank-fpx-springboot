package com.imocha.fpx.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigParamService {
    
    private final Map<String, Map<String, String>> configParams = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Initialize with test data
        saveConfigParam("AGROFPXWSSE", "encryptionKey", "testEncryptionKey");
        saveConfigParam("AGROFPXWSSE", "testuser", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"); // password
    }
    
    public String getValueByModuleAndCode(String module, String code) {
        Map<String, String> moduleConfig = configParams.get(module);
        return moduleConfig != null ? moduleConfig.get(code) : null;
    }
    
    public void saveConfigParam(String module, String code, String value) {
        configParams.computeIfAbsent(module, k -> new ConcurrentHashMap<>()).put(code, value);
    }
}
