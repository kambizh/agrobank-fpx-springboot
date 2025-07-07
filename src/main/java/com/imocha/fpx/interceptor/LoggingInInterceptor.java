package com.imocha.fpx.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class LoggingInInterceptor extends AbstractPhaseInterceptor<Message> {
    
    public LoggingInInterceptor() {
        super(Phase.RECEIVE);
    }
    
    @Override
    public void handleMessage(Message message) throws Fault {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        
        String ndc = null;
        if (headers != null && headers.containsKey("CLIENT_NDC")) {
            List<String> ndcList = headers.get("CLIENT_NDC");
            if (ndcList != null && !ndcList.isEmpty()) {
                ndc = ndcList.get(0);
            }
        }
        
        // Use SLF4J MDC instead of Log4j NDC
        if (MDC.get("correlationId") == null) {
            if (ndc != null) {
                MDC.put("correlationId", "C:" + ndc);
            } else {
                MDC.put("correlationId", UUID.randomUUID().toString().substring(0, 6));
            }
        }
        
        log.debug("LoggingInInterceptor: Message received with correlationId: {}", MDC.get("correlationId"));
    }
}
