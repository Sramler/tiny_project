package com.tiny.common.exception.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异常工具类
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class ExceptionUtils {
    
    private static final Logger log = LoggerFactory.getLogger(ExceptionUtils.class);
    
    /**
     * 获取异常详情信息
     * 
     * @param ex 异常
     * @return 异常详情
     */
    public static String getExceptionDetail(Throwable ex) {
        if (ex == null) {
            return "未知异常";
        }
        
        // 优先返回异常消息
        if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            return ex.getMessage();
        }
        
        // 如果没有消息，返回类名
        return ex.getClass().getSimpleName();
    }
    
    /**
     * 获取异常的根原因
     * 
     * @param ex 异常
     * @return 根原因
     */
    public static Throwable getRootCause(Throwable ex) {
        if (ex == null) {
            return null;
        }
        
        Throwable cause = ex.getCause();
        if (cause == null || cause == ex) {
            return ex;
        }
        
        return getRootCause(cause);
    }
    
    /**
     * 获取异常堆栈信息（用于日志）
     * 
     * @param ex 异常
     * @return 堆栈信息字符串
     */
    public static String getStackTrace(Throwable ex) {
        if (ex == null) {
            return "";
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * 判断是否为业务异常（可向客户端暴露详细信息的异常）
     * 
     * @param ex 异常
     * @return true 表示是业务异常
     */
    public static boolean isBusinessException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        
        // 常见的业务异常类型
        String className = ex.getClass().getName();
        return className.contains("BusinessException") ||
               className.contains("ValidationException") ||
               className.contains("IllegalArgumentException");
    }
}

