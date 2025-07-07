package com.imocha.fpx.model;

import lombok.Data;

@Data
public class AgrobankFpxMessageResponse {
    private String responseCode;
    private String errorCode;
    private String errorDescription;
    private String refId;
    private String txnRefNum;
    private String xmlMessage;
    private String responseUrl;
}
