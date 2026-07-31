package com.young.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
        // 拦截 Spring Boot 3 找不到静态资源的异常，静默处理
        Result<String> result = Result.error("请求的接口或资源不存在");
        result.setCode(404);
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // 业务参数校验异常，可以安全地返回给前端
        log.warn("参数校验异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        // 业务运行时异常，记录完整堆栈但不暴露给前端
        log.error("业务运行时异常", e);
        // 如果是业务自定义的异常消息，可以返回
        // 但如果是系统级异常，返回通用提示
        String message = e.getMessage();
        if (message != null && !message.isEmpty() && !isSystemException(e)) {
            return Result.error(message);
        }
        return Result.error("操作失败，请稍后重试或联系管理员");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 兜底异常处理，记录完整堆栈，不暴露内部信息
        log.error("系统内部异常", e);
        return Result.error("系统内部错误，请联系管理员");
    }

    /**
     * 判断是否为系统级异常
     */
    private boolean isSystemException(Exception e) {
        return e instanceof NullPointerException
                || e instanceof ClassCastException
                || e instanceof ArrayIndexOutOfBoundsException
                || e instanceof NumberFormatException
                || e instanceof IllegalStateException;
    }
}
