package com.young.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(NoResourceFoundException e) {
        Result<String> result = Result.error("请求的接口或资源不存在");
        result.setCode(404);
        return result;
    }

    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        Result<String> result = Result.error(e.getMessage());
        result.setCode(e.getCode());
        return result;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        Result<String> result = Result.error("缺少必填参数: " + e.getParameterName());
        result.setCode(400);
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数验证失败: {}", message);
        Result<String> result = Result.error(message);
        result.setCode(400);
        return result;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getName());
        Result<String> result = Result.error("参数类型错误: " + e.getName());
        result.setCode(400);
        return result;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<String> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        Result<String> result = Result.error("不支持的请求方法: " + e.getMethod());
        result.setCode(405);
        return result;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传大小超限");
        Result<String> result = Result.error("上传文件大小超出限制");
        result.setCode(413);
        return result;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleDataIntegrity(DataIntegrityViolationException e) {
        log.error("数据完整性异常", e);
        String message = "数据操作失败";
        if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
            message = "数据已存在，请勿重复提交";
        }
        return Result.error(message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("业务运行时异常", e);
        // 只对 BusinessException 的子类返回具体消息，其他一律返回通用提示
        if (e instanceof BusinessException) {
            return handleBusinessException((BusinessException) e);
        }
        return Result.error("操作失败，请稍后重试或联系管理员");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统内部异常", e);
        return Result.error("系统内部错误，请联系管理员");
    }
}
