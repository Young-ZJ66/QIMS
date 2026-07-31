package com.young.annotation;

import com.young.pojo.enums.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限校验注解
 * <p>
 * 标注在 Controller 方法上，由 {@link com.young.aspect.RequireRoleAspect} AOP 切面统一拦截。
 * 如果当前登录用户的角色不在允许列表中，直接返回 403 错误。
 * </p>
 *
 * 用法示例：
 * <pre>
 * {@literal @}RequireRole(UserRole.ADMIN)
 * public Result&lt;Void&gt; delete(@PathVariable Long id) { ... }
 *
 * {@literal @}RequireRole({UserRole.ADMIN, UserRole.CLIENT})
 * public Result&lt;List&lt;BizDelegation&gt;&gt; getAll() { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 允许访问的角色列表
     */
    UserRole[] value();
}
