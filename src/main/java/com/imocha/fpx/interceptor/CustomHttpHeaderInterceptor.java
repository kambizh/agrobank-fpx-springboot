package com.imocha.fpx.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

@Slf4j
public class CustomHttpHeaderInterceptor extends AbstractPhaseInterceptor<Message> {
    
    public CustomHttpHeaderInterceptor() {
        super(Phase.READ);
    }
    
    @Override
    public void handleMessage(Message message) throws Fault {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        
        String clientIp = "";
        String fpxUsername = "";
        
        if (headers != null) {
            if (headers.containsKey("CLIENT_IP") && !headers.get("CLIENT_IP").isEmpty()) {
                clientIp = headers.get("CLIENT_IP").get(0);
            }
            
            if (headers.containsKey("USERNAME") && !headers.get("USERNAME").isEmpty()) {
                fpxUsername = headers.get("USERNAME").get(0);
            }
        }
        
        log.debug("CustomHttpHeaderInterceptor: CLIENT_IP={}, USERNAME={}", clientIp, fpxUsername);
        
        // Get HttpServletRequest from CXF message
        HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        
        if (request != null) {
            request.setAttribute("CLIENT_IP", clientIp);
            request.setAttribute("USERNAME", fpxUsername);
        } else {
            // Fallback to Spring's RequestContextHolder
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest springRequest = attributes.getRequest();
                springRequest.setAttribute("CLIENT_IP", clientIp);
                springRequest.setAttribute("USERNAME", fpxUsername);
            } catch (Exception e) {
                log.warn("Unable to retrieve request object: {}", e.getMessage());
            }
        }
    }
}
