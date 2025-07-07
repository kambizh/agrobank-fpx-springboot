package com.imocha.fpx.config;

import com.imocha.fpx.endpoint.AgrobankFpxEndpoint;
import com.imocha.fpx.interceptor.CustomHttpHeaderInterceptor;
import com.imocha.fpx.interceptor.LoggingInInterceptor;
import com.imocha.fpx.interceptor.LoggingOutInterceptor;
import com.imocha.fpx.security.AgrobankFpxEndpointAuthHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.xml.ws.Endpoint;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CxfConfig {
    
    private final Bus bus;
    private final AgrobankFpxEndpoint agrobankFpxEndpoint;
    private final ApplicationContext applicationContext;
    
    @Value("${agrobankFpxEndpoint.wsse.byPass:false}")
    private boolean wsseByPass;
    
    @Bean
    public Endpoint agrobankFpxEndpointBean() {
        EndpointImpl endpoint = new EndpointImpl(bus, agrobankFpxEndpoint);
        endpoint.publish("/AgrobankFpxService");
        
        // Add interceptors
        endpoint.getInInterceptors().add(loggingInInterceptor());
        endpoint.getInInterceptors().add(customHttpHeaderInterceptor());
        endpoint.getOutInterceptors().add(loggingOutInterceptor());
        
        // Configure WS-Security if not bypassed
        if (!wsseByPass) {
            Map<String, Object> inProps = new HashMap<>();
            inProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
            inProps.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
            inProps.put(ConfigurationConstants.PW_CALLBACK_REF, agrobankFpxEndpointAuthHandler());
            
            WSS4JInInterceptor wssInInterceptor = new WSS4JInInterceptor(inProps);
            endpoint.getInInterceptors().add(wssInInterceptor);
            
            // Disable nonce and timestamp cache
            endpoint.getProperties().put("ws-security.enable.nonce.cache", "false");
            endpoint.getProperties().put("ws-security.enable.timestamp.cache", "false");
        }
        
        log.info("AgrobankFpxEndpoint published at /soap/AgrobankFpxService");
        log.info("WS-Security enabled: {}", !wsseByPass);
        
        return endpoint;
    }
    
    @Bean
    public LoggingInInterceptor loggingInInterceptor() {
        return new LoggingInInterceptor();
    }
    
    @Bean
    public LoggingOutInterceptor loggingOutInterceptor() {
        return new LoggingOutInterceptor();
    }
    
    @Bean
    public CustomHttpHeaderInterceptor customHttpHeaderInterceptor() {
        return new CustomHttpHeaderInterceptor();
    }
    
    @Bean
    public AgrobankFpxEndpointAuthHandler agrobankFpxEndpointAuthHandler() {
        return new AgrobankFpxEndpointAuthHandler(applicationContext);
    }
}
