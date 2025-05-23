package com.wan.servlet;

import com.wan.util.Help;
import com.wan.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/refresh")
@MultipartConfig
public class TokenRefreshServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("TokenRefreshServlet initialized.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, IOException {

        response.setContentType("application/json"); // 设置响应内容类型为 JSON
        response.setCharacterEncoding("UTF-8");     // 设置字符编码为 UTF-8
        PrintWriter out = response.getWriter();

        // 1. 从 HTTP 请求头中获取 Refresh Token
        // 约定：Refresh Token 也通过 Authorization 头，格式为 "Bearer <token>"
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // 如果没有 Authorization 头或者格式不正确，返回 401 Unauthorized
            System.out.println("Missing or invalid Authorization header for refresh token request.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(Help.getJsonError("未提供刷新令牌或令牌格式不正确"));
            return;
        }

        String refreshToken = authorizationHeader.substring(7); // 提取 "Bearer " 后面的令牌字符串

        // 2. 验证 Refresh Token 的有效性
        try {
            if (JwtUtil.validateRefreshToken(refreshToken)) {
                // Refresh Token 有效，解析令牌获取用户信息
                Claims claims = JwtUtil.parseRefreshToken(refreshToken);
                String username = claims.getSubject(); // 获取 Refresh Token 中的用户名

                // 3. 生成新的 Access Token
                Map<String, Object> newAccessTokenClaims = new HashMap<>();
                // 可以从旧的 Refresh Token 的 Claims 中复制一些信息到新的 Access Token 中，
                // 例如用户ID、角色等，而无需再次查询数据库。
                // newAccessTokenClaims.put("userId", claims.get("userId", String.class));
                // newAccessTokenClaims.put("roles", claims.get("roles", List.class));
                String newAccessToken = JwtUtil.generateAccessToken(username, newAccessTokenClaims);

                Map<String, String> successResponseData = new HashMap<>();
                successResponseData.put("message", "令牌刷新成功");
                successResponseData.put("accessToken", newAccessToken); // 返回新的 Access Token

                // TODO (可选): 刷新令牌轮换 (Refresh Token Rotation)
                // 为了更高的安全性，你可以在这里生成一个新的 Refresh Token，并使旧的 Refresh Token 失效。
                // 这需要服务器端存储 Refresh Token（例如在数据库中），并维护其状态。
                // 如果实施轮换，你还需要将新的 Refresh Token 也返回给前端：
                // String newRefreshToken = JwtUtil.generateRefreshToken(username);
                // successResponseData.put("refreshToken", newRefreshToken);
                // 并且后端需要将旧的 refreshToken 标记为已使用/失效。

                // 使用 Help.getJson() 封装成功响应数据并发送
                out.print(Help.getJson(successResponseData));
                System.out.println("Access Token refreshed for user: " + username + ". New Access Token issued.");

            } else {
                // Refresh Token 无效或已过期
                System.out.println("Refresh Token invalid or expired.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                out.print(Help.getJsonError("刷新令牌无效或已过期，请重新登录"));
            }
        } catch (JwtException e) {
            // Refresh Token 解析失败，可能是令牌格式错误、篡改等问题
            System.err.println("Refresh Token parsing failed: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            out.print(Help.getJsonError("刷新令牌解析失败: " + e.getMessage()));
        } catch (Exception e) {
            // 捕获其他任何未预料的异常
            System.err.println("Unexpected error in TokenRefreshServlet: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            out.print(Help.getJsonError("服务器内部错误"));
        }
    }
}
