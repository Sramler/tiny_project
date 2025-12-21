package com.tiny.web.core;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private T data;
        private final HttpHeaders headers = new HttpHeaders();
        private HttpStatus status = ResponseCode.SUCCESS.getStatus();
        private int code = ResponseCode.SUCCESS.getCode();
        private String message = ResponseCode.SUCCESS.getMessage();

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> header(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder<T> status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> responseCode(ResponseCode rc) {
            this.code = rc.getCode();
            this.message = rc.getMessage();
            this.status = rc.getStatus();
            return this;
        }

        public ResponseEntity<GlobalResponse<T>> build() {
            GlobalResponse<T> body = new GlobalResponse<>();
            body.setCode(code);
            body.setMessage(message);
            if (code == ResponseCode.SUCCESS.getCode()) {
                body.setData(data);
            } else {
                body.setStatus(status.value());
            }
            return new ResponseEntity<>(body, headers, status);
        }
    }

    public static <T> ResponseEntity<GlobalResponse<T>> ok(T data) {
        return new ResponseEntity<>(GlobalResponse.ok(data), ResponseCode.SUCCESS.getStatus());
    }

    public static <T> ResponseEntity<GlobalResponse<T>> ok(T data, HttpHeaders headers) {
        return new ResponseEntity<>(GlobalResponse.ok(data), headers, ResponseCode.SUCCESS.getStatus());
    }

    public static ResponseEntity<GlobalResponse<Void>> error(int code, String message, HttpStatus status) {
        return new ResponseEntity<>(GlobalResponse.error(code, message, status.value()), status);
    }

    public static ResponseEntity<GlobalResponse<Void>> error(ResponseCode rc) {
        return new ResponseEntity<>(GlobalResponse.error(rc), rc.getStatus());
    }
}