package com.young.config;

import com.young.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域配置 (CORS)、拦截器注册及静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册登录拦截器，拦截所有 API，排除 /api/auth/** 和 /api/uploads/** (资源访问)
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login", 
                    "/api/auth/client-login", 
                    "/api/auth/register", 
                    "/api/uploads/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射静态资源路径到后端工程根目录下的 uploads 文件夹
        String uploadDir = "file:" + System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadDir);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许任何来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // 允许携带 Cookie
                .maxAge(3600); // 预检请求的缓存时间 (秒)
    }
}
