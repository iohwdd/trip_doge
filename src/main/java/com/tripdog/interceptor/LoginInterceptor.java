package com.tripdog.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripdog.common.ErrorCode;
import com.tripdog.common.Result;
import com.tripdog.model.vo.UserInfoVO;
import com.tripdog.service.impl.UserSessionService;
import com.tripdog.common.utils.TokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 * 基于Redis token验证用户登录状态
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final UserSessionService userSessionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求URI和方法
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.debug("登录拦截器处理请求: {} {}", method, requestURI);

        // 对于OPTIONS请求（预检请求），直接放行
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 检查是否是需要登录的接口
        if (isExcludedPath(requestURI)) {
            return true;
        }

        // 从请求中提取token
        String token = TokenUtils.extractToken(request);
        if (token == null) {
            log.debug("请求中未找到token: {}", requestURI);
            writeErrorResponse(response, ErrorCode.USER_NOT_LOGIN);
            return false;
        }

        // 验证token格式
        if (!TokenUtils.isValidTokenFormat(token)) {
            log.debug("token格式无效: {}", token);
            writeErrorResponse(response, ErrorCode.USER_NOT_LOGIN);
            return false;
        }

        // 获取用户Session信息
        UserInfoVO loginUser = userSessionService.getSession(token);
        if (loginUser == null) {
            log.debug("token对应的用户session不存在或已过期: {}", token);
            writeErrorResponse(response, ErrorCode.USER_NOT_LOGIN);
            return false;
        }

        // 将用户信息放入请求属性中，便于Controller使用
        request.setAttribute("loginUser", loginUser);
        request.setAttribute("userToken", token);

        log.debug("用户身份验证成功: {} ({})", loginUser.getNickname(), loginUser.getId());
        return true;
    }

    /**
     * 判断是否是不需要登录的路径
     */
    private boolean isExcludedPath(String requestURI) {
        // 不需要登录的路径
        String[] excludePaths = {
            "/api/user/register",
            "/api/user/login",
            "/api/user/sendEmail",
            "/api/roles/list"
        };

        for (String path : excludePaths) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }

        return false;
    }



    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Result<Void> result = Result.error(errorCode);
        String jsonResult = objectMapper.writeValueAsString(result);
        response.getWriter().write(jsonResult);
    }

}
