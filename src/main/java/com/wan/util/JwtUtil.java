package com.wan.util;

import io.jsonwebtoken.io.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // --- JWT 密钥配置 ---
    // !!! 生产环境中，这些密钥必须是强随机的，并且从安全配置中加载，绝不能硬编码在此处 !!!
    // Access Token 的密钥：用于签名 Access Token，应足够长且保密。
    // HS256 (HMAC SHA-256) 算法至少需要 32 字节 (256 位) 的密钥。
    private static final String ACCESS_SECRET_STRING = "your_access_token_super_secret_key_please_replace_me_with_a_long_random_string_of_at_least_32_bytes";
    private static final Key ACCESS_SECRET_KEY = Keys.hmacShaKeyFor(ACCESS_SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Refresh Token 的密钥：建议与 Access Token 的密钥不同，且更长/更复杂，因为它有效期更长。
    // HS512 (HMAC SHA-512) 算法至少需要 64 字节 (512 位) 的密钥。
    private static final String REFRESH_SECRET_STRING = "your_refresh_token_extremely_secure_key_please_replace_me_with_a_very_long_and_complex_random_string_of_at_least_64_bytes";
    private static final Key REFRESH_SECRET_KEY = Keys.hmacShaKeyFor(REFRESH_SECRET_STRING.getBytes(StandardCharsets.UTF_8));


    // --- JWT 过期时间配置 ---
    // Access Token 过期时间（毫秒），用于日常 API 访问，应设置较短。
    // 例如：1 小时 (1000 * 60 * 60)
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60;

    // Refresh Token 过期时间（毫秒），用于“免登录”功能，可设置较长。
    // 例如：10 天 (1000L * 60 * 60 * 24 * 10)
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 10;

    /**
     * 生成访问令牌 (Access Token)。
     * Access Token 包含用户身份信息和授权信息，用于短期的 API 访问。
     *
     * @param subject 通常是用户唯一标识，如用户名或用户ID。
     * @param claims 额外要放入令牌的自定义声明，如用户角色、权限列表等。可为 null。
     * @return 生成的 JWT 字符串 (Access Token)。
     */
    public static String generateAccessToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);

        if (claims == null) {
            claims = new HashMap<>();
        }

        return Jwts.builder()
                .setClaims(claims != null ? claims : new HashMap<>()) // 设置自定义声明，避免 null
                .setSubject(subject) // 主题，代表用户
                .setIssuedAt(now) // 签发时间
                .setExpiration(expiration) // 过期时间
                .signWith(ACCESS_SECRET_KEY, SignatureAlgorithm.HS256) // 使用 Access Token 密钥签名
                .compact(); // 生成紧凑的 JWT 字符串
    }

    /**
     * 生成刷新令牌 (Refresh Token)。
     * Refresh Token 用于在 Access Token 过期后获取新的 Access Token，其有效期较长。
     *
     * @param subject 通常是用户唯一标识，如用户名或用户ID。
     * @return 生成的 JWT 字符串 (Refresh Token)。
     */
    public static String generateRefreshToken(String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(subject) // 主题，代表用户
                .setIssuedAt(now) // 签发时间
                .setExpiration(expiration) // 过期时间
                .signWith(REFRESH_SECRET_KEY, SignatureAlgorithm.HS512) // 使用 Refresh Token 密钥签名 (可选择更强的算法)
                .compact(); // 生成紧凑的 JWT 字符串
    }

    /**
     * 解析 Access Token 并获取其有效载荷 (Claims)。
     *
     * @param token Access Token 字符串。
     * @return 包含所有声明的 Claims 对象。
     * @throws JwtException 如果令牌无效，例如签名不匹配、过期、格式错误等。
     */
    public static Claims parseAccessToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(ACCESS_SECRET_KEY) // 使用 Access Token 密钥解析
                .build()
                .parseClaimsJws(token) // 解析 JWS (JSON Web Signature)
                .getBody(); // 获取 Claims
    }

    /**
     * 解析 Refresh Token 并获取其有效载荷 (Claims)。
     *
     * @param token Refresh Token 字符串。
     * @return 包含所有声明的 Claims 对象。
     * @throws JwtException 如果令牌无效，例如签名不匹配、过期、格式错误等。
     */
    public static Claims parseRefreshToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(REFRESH_SECRET_KEY) // 使用 Refresh Token 密钥解析
                .build()
                .parseClaimsJws(token) // 解析 JWS (JSON Web Signature)
                .getBody(); // 获取 Claims
    }

    /**
     * 验证 Access Token 的有效性。
     * 会检查签名是否正确以及令牌是否过期。
     *
     * @param token Access Token 字符串。
     * @return true 如果 Access Token 有效；false 如果无效（如签名不匹配、已过期）。
     */
    public static boolean validateAccessToken(String token) {
        try {
            parseAccessToken(token); // 尝试解析，如果成功则有效
            return true;
        } catch (JwtException e) {
            // 令牌无效时的日志输出，便于调试
            System.err.println("Access Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证 Refresh Token 的有效性。
     * 会检查签名是否正确以及令牌是否过期。
     *
     * @param token Refresh Token 字符串。
     * @return true 如果 Refresh Token 有效；false 如果无效（如签名不匹配、已过期）。
     */
    public static boolean validateRefreshToken(String token) {
        try {
            parseRefreshToken(token); // 尝试解析，如果成功则有效
            return true;
        } catch (JwtException e) {
            // 令牌无效时的日志输出，便于调试
            System.err.println("Refresh Token validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从 Access Token 中提取主题 (Subject)。
     * 通常主题就是用户的唯一标识（用户名或用户ID）。
     *
     * @param token Access Token 字符串。
     * @return JWT 的主题字符串。
     * @throws JwtException 如果令牌无效。
     */
    public static String getSubjectFromAccessToken(String token) throws JwtException {
        Claims claims = parseAccessToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Refresh Token 中提取主题 (Subject)。
     * 通常主题就是用户的唯一标识（用户名或用户ID）。
     *
     * @param token Refresh Token 字符串。
     * @return JWT 的主题字符串。
     * @throws JwtException 如果令牌无效。
     */
    public static String getSubjectFromRefreshToken(String token) throws JwtException {
        Claims claims = parseRefreshToken(token);
        return claims.getSubject();
    }


    // --- 可选：main 方法用于本地简单测试 JwtUtil 的功能 ---
    /*public static void main(String[] args) {
        String testUsername = "exampleUser123";            //这个是唯一确定用户身份的标识符
        Map<String, Object> testClaims = new HashMap<>();  //非敏感、非核心的附加信息
        testClaims.put("role", "admin");
        testClaims.put("userId", 456);

        // 1. 生成 Access Token
        String accessToken = generateAccessToken(testUsername, testClaims);
        System.out.println("Generated Access Token: " + accessToken);

        // 2. 验证 Access Token
        boolean isAccessValid = validateAccessToken(accessToken);
        System.out.println("Access Token valid: " + isAccessValid);
        if (isAccessValid) {
            String subjectFromAccess = getSubjectFromAccessToken(accessToken);
            System.out.println("Subject from Access Token: " + subjectFromAccess);
            Claims accessClaims = parseAccessToken(accessToken);
            System.out.println("Role from Access Token claims: " + accessClaims.get("role"));
        }

        System.out.println("------------------------------------");

        // 3. 生成 Refresh Token
        String refreshToken = generateRefreshToken(testUsername);
        System.out.println("Generated Refresh Token: " + refreshToken);

        // 4. 验证 Refresh Token
        boolean isRefreshValid = validateRefreshToken(refreshToken);
        System.out.println("Refresh Token valid: " + isRefreshValid);
        if (isRefreshValid) {
            String subjectFromRefresh = getSubjectFromRefreshToken(refreshToken);
            System.out.println("Subject from Refresh Token: " + subjectFromRefresh);
        }

        // 5. 模拟 Access Token 过期后的验证
        System.out.println("\n--- Simulating Access Token Expiration ---");
        try {
            // 强制将 Access Token 的过期时间设置为过去，仅用于测试
            // 在实际代码中不要这样做！
            long oldExpTime = ACCESS_TOKEN_EXPIRATION_TIME;
            java.lang.reflect.Field field = JwtUtil.class.getDeclaredField("ACCESS_TOKEN_EXPIRATION_TIME");
            field.setAccessible(true);
            field.set(null, -1000L); // 设置为-1秒，立即过期

            String expiredAccessToken = generateAccessToken(testUsername, testClaims);
            System.out.println("Generated Expired Access Token: " + expiredAccessToken);
            boolean isExpiredAccessValid = validateAccessToken(expiredAccessToken);
            System.out.println("Expired Access Token valid: " + isExpiredAccessValid);

            // 恢复过期时间，避免影响后续测试或实际应用
            field.set(null, oldExpTime);

        } catch (Exception e) {
            System.err.println("Error simulating expiration: " + e.getMessage());
        }
    }*/
}
