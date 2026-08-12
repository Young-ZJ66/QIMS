package com.young.aspect;

import com.young.annotation.RequireRole;
import com.young.common.BusinessException;
import com.young.pojo.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link RequireRole} 注解的 AOP 切面实现
 * <p>
 * 在方法执行前校验当前登录用户的角色是否在允许列表中，不匹配则直接抛出 {@link BusinessException}，
 * 由 {@link com.young.common.GlobalExceptionHandler} 统一返回 403。
 * </p>
 */
@Aspect
@Component
public class RequireRoleAspect {

    private static final Logger log = LoggerFactory.getLogger(RequireRoleAspect.class);

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        Object roleIdObj = request.getAttribute("roleId");

        // 未获取到角色信息
        if (roleIdObj == null) {
            throw new BusinessException(401, "未登录或 Token 已过期");
        }

        int userRoleCode;
        try {
            userRoleCode = Integer.parseInt(String.valueOf(roleIdObj));
        } catch (NumberFormatException e) {
            throw new BusinessException(403, "角色信息异常");
        }

        // 检查用户角色是否在允许列表中
        Set<Integer> allowedCodes = Arrays.stream(requireRole.value())
                .map(UserRole::getCode)
                .collect(Collectors.toSet());

        if (!allowedCodes.contains(userRoleCode)) {
            log.warn("用户角色 {} 尝试访问受限资源: {}", userRoleCode, joinPoint.getSignature().toShortString());
            throw new BusinessException(403, "无权限访问该资源");
        }

        // 权限校验通过，继续执行原方法
        return joinPoint.proceed();
    }
}
