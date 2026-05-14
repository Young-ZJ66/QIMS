package com.young.interceptor;

import com.young.common.Result;
import com.young.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;

/**
 * JWT Token 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        
        // 尝试从请求头中获取 Token
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                // 验证 Token 是否合法
                Claims claims = JwtUtils.parseToken(token);
                // 可以将用户信息放入 request 域，供后续 Controller 使用
                request.setAttribute("userId", claims.get("userId"));
                request.setAttribute("username", claims.get("username"));
                request.setAttribute("roleId", claims.get("roleId"));
                request.setAttribute("clientId", claims.get("clientId"));
                return true;
            } catch (Exception e) {
                // Token 过期或非法
            }
        }

        // 拦截并返回 401 未登录
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        Result<String> error = Result.error("未登录或 Token 已过期");
        error.setCode(401);
        out.write(new ObjectMapper().writeValueAsString(error));
        out.flush();
        out.close();
        return false;
    }
}
