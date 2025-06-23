//package com.tiny.web.exception;
//
//import com.tiny.web.core.ResponseCode;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.InvalidDataAccessApiUsageException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.zalando.problem.Problem;
//import org.zalando.problem.Status;
//import org.zalando.problem.spring.web.advice.ProblemHandling;
//import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.context.request.NativeWebRequest;
//
//@RestControllerAdvice
//@Slf4j
//public class GlobalExceptionHandling implements ProblemHandling, SecurityAdviceTrait {
//
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<Problem> handleBusinessException(BusinessException ex, NativeWebRequest request) {
//        ResponseCode rc = ex.getResponseCode();
//
//        Problem problem = Problem.builder()
//                .withType(Problem.DEFAULT_TYPE)
//                .withTitle(rc.getMessage())
//                .withStatus(Status.valueOf(rc.getStatus().value()))
//                .with("code", rc.getCode())
//                .with("message", rc.getMessage())
//                .build();
//
//        return create(ex, problem, request);
//    }
//
////    @ExceptionHandler({
////            InvalidDataAccessApiUsageException.class,
////            IllegalArgumentException.class
////    })
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Problem> handleJpaErrors(RuntimeException ex, NativeWebRequest request) {
//
//        //Throwable root = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(ex);
//        log.warn("处理异常：{}", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
//
//        Problem problem = Problem.builder()
//                .withTitle("服务器异常")
//                .withStatus(Status.INTERNAL_SERVER_ERROR)
//                .with("message", ex.getMessage())
//                .build();
//        return create(ex, problem, request);
//    }
//
//    // 可根据需要添加对其他异常类型的处理方法
//}