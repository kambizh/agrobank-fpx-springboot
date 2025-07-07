package com.imocha.fpx.endpoint;

import com.imocha.fpx.model.AgrobankFpxMessageResponse;
import com.imocha.fpx.model.ResponseCode;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
@WebService(
    serviceName = "AgrobankFpxService",
    targetNamespace = "http://ws.fpx.imocha/",
    portName = "AgrobankFpxServicePort"
)
@RequiredArgsConstructor
public class AgrobankFpxEndpoint {
    
    @WebMethod
    @WebResult(name = "response")
    public AgrobankFpxMessageResponse validateInitFpxMessageB2C(
            @WebParam(name = "message") @XmlElement(required = true, nillable = false) byte[] message,
            @WebParam(name = "sender") String sender,
            @WebParam(name = "txnRefNum") String txnRefNum) {
        
        log.info("#validateInitFpxMessageB2C(txnRefNum={})# received a request", txnRefNum);
        
        AgrobankFpxMessageResponse response = new AgrobankFpxMessageResponse();
        
        try {
            // Decode the message
            String decodedMessage = new String(Base64.getDecoder().decode(message), "UTF-8");
            log.debug("Decoded message: {}", decodedMessage);
            
            // At this point, authentication has already been performed by interceptors
            // Your business logic here
            
            response.setResponseCode(ResponseCode.OK.name());
            response.setRefId("REF-" + System.currentTimeMillis());
            response.setTxnRefNum(txnRefNum);
            
            log.info("#validateInitFpxMessageB2C(txnRefNum={})# processed successfully", txnRefNum);
            
        } catch (Exception e) {
            log.error("#validateInitFpxMessageB2C(txnRefNum={})# Error processing request", txnRefNum, e);
            response.setResponseCode(ResponseCode.FAIL.name());
            response.setErrorCode("99");
            response.setErrorDescription("Internal error: " + e.getMessage());
        }
        
        return response;
    }
}
