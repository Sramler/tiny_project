//package com.tiny.web.exception;
//
//import com.tiny.web.core.ResponseCode;
//
//public class BusinessException extends RuntimeException {
//
//    private final ResponseCode responseCode;
//
//    public BusinessException(ResponseCode responseCode) {
//        super(responseCode.getMessage());
//        this.responseCode = responseCode;
//    }
//
//    public ResponseCode getResponseCode() {
//        return responseCode;
//    }
//}