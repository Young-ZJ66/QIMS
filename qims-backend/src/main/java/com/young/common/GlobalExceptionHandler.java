package com.young.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
        // 拦截 Spring Boot 3 找不到静态资源的异常，静默处理（如浏览器插件的探测请求），不再打印一长串堆栈
        Result<String> result = Result.error("请求的接口或资源不存在");
        result.setCode(404);
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统内部错误，请联系管理员");
    }
}
