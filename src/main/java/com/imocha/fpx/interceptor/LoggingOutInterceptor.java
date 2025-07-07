package com.imocha.fpx.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.MDC;

@Slf4j
public class LoggingOutInterceptor extends AbstractPhaseInterceptor<Message> {
    
    public LoggingOutInterceptor() {
        super(Phase.SEND);
    }
    
    @Override
    public void handleMessage(Message message) throws Fault {
        log.debug("LoggingOutInterceptor: Clearing MDC for correlationId: {}", MDC.get("correlationId"));
        MDC.clear();
    }
}
