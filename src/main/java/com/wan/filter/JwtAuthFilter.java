package com.wan.filter;

import com.wan.util.Help;
import com.wan.util.JwtUtil;
import io.jsonwebtoken.io.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/api/*")
public class JwtAuthFilter implements Filter {

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/Users/login",
            "/api/Users/register",
            "/api/Users/refresh"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("JwtAuthFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求的 URI，并移除上下文路径
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String pathWithoutContext = requestURI.substring(contextPath.length());

        System.out.println("JwtAuthFilter processing request for: " + pathWithoutContext); // 添加日志

        // JWT 过滤器不需要再单独处理 OPTIONS 请求，因为 CrossFilter 已经处理并放行了它。
        // CrossFilter 应该确保 OPTIONS 请求在 JWT 过滤器之前被处理，并返回 200 OK。

        // 1. 检查请求路径是否在白名单中 (不需要认证的路径)
        if (EXCLUDE_PATHS.stream().anyMatch(pathWithoutContext::startsWith)) {
            System.out.println("Excluded path: " + pathWithoutContext + ". Bypassing JWT authentication.");
            chain.doFilter(request, response);
            return;
        }

        // 2. 从 HTTP 请求头中获取 Access Token
        String authorizationHeader = httpRequest.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            System.out.println("Missing or invalid Authorization header for: " + pathWithoutContext);
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "未提供认证令牌或令牌格式不正确");
            return;
        }

        String accessToken = authorizationHeader.substring(7);

        // 3. 验证 Access Token 的有效性
        try {
            if (JwtUtil.validateAccessToken(accessToken)) {
                Claims claims = JwtUtil.parseAccessToken(accessToken);
                String username = claims.getSubject();
                httpRequest.setAttribute("authenticatedUsername", username);

                System.out.println("Access Token valid for user: " + username + ". Proceeding with request to: " + pathWithoutContext);
                chain.doFilter(request, response);
            } else {
                System.out.println("Access Token invalid or expired for: " + pathWithoutContext);
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌无效或已过期，请重新登录");
            }
        } catch (JwtException e) {
            System.err.println("JWT parsing failed for " + pathWithoutContext + ": " + e.getMessage());
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌解析失败: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in JwtAuthFilter for " + pathWithoutContext + ": " + e.getMessage());
            sendErrorResponse(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误");
        }
    }

    @Override
    public void destroy() {
        System.out.println("JwtAuthFilter destroyed.");
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(Help.getJsonError(message));
    }
}
