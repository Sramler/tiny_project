package com.tiny.scheduling.exception;

/**
 * 创建 {@link SchedulingException} 的便捷工具。
 */
public final class SchedulingExceptions {

    private SchedulingExceptions() {
    }

    public static SchedulingException notFound(String message, Object... args) {
        return new SchedulingException(SchedulingErrorCode.NOT_FOUND, format(message, args));
    }

    public static SchedulingException validation(String message, Object... args) {
        return new SchedulingException(SchedulingErrorCode.VALIDATION_ERROR, format(message, args));
    }

    public static SchedulingException conflict(String message, Object... args) {
        return new SchedulingException(SchedulingErrorCode.CONFLICT, format(message, args));
    }

    public static SchedulingException operationNotAllowed(String message, Object... args) {
        return new SchedulingException(SchedulingErrorCode.OPERATION_NOT_ALLOWED, format(message, args));
    }

    public static SchedulingException systemError(String message, Throwable cause, Object... args) {
        return new SchedulingException(SchedulingErrorCode.SYSTEM_ERROR, format(message, args), cause);
    }

    public static SchedulingException systemError(String message, Object... args) {
        return new SchedulingException(SchedulingErrorCode.SYSTEM_ERROR, format(message, args));
    }

    private static String format(String message, Object... args) {
        return args == null || args.length == 0 ? message : String.format(message, args);
    }
}

