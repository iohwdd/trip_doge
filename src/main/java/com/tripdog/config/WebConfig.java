package com.tripdog.config;

import com.tripdog.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080,http://localhost:5173,http://localhost:4200,http://127.0.0.1:3000,http://127.0.0.1:8080,http://127.0.0.1:5173,http://127.0.0.1:4200,https://trip-doge-frontend.zeabur.app}")
    private String allowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有/api/**的请求
                .excludePathPatterns(
                        "/api/user/register",      // 注册
                        "/api/user/login",         // 登录
                        "/api/user/sendEmail",     // 发送验证码
                        "/api/roles/list",         // 角色列表（可能需要在未登录时访问）
                        "/api/api-docs/**",        // Swagger API 文档
                        "/api/swagger-ui/**",      // Swagger UI 资源
                        "/api/swagger-ui.html"     // Swagger UI 首页
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
