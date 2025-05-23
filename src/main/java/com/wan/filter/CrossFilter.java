package com.wan.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")     /* 针对哪些有效 */
public class CrossFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;

        String origin = req.getHeader("Origin"); // 获取请求的 Origin 头

        // 校验 origin 是否在你的允许列表中 (推荐做法)
        List<String> allowedOrigins = Arrays.asList("http://127.0.0.1:8081", "http://127.0.0.1:8081/"); // 你的允许列表

        if (origin != null && allowedOrigins.contains(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin); // 将请求的 Origin 原样返回
        } else {
            // 如果 origin 不在允许列表中，你可以选择：
            // 1. 不设置 Access-Control-Allow-Origin 头，让浏览器阻止请求。
            // 2. 设置一个默认的允许源，如 resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:8081");
        }

        /* 解决跨域问题 */
        /*resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:8081/");*/
        // 允许的方法（如POST, GET等）
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE ,PUT");
        // 预检请求的缓存时间
        resp.setHeader("Access-Control-Max-Age", "3600");
        // 允许的头信息
        resp.setHeader("Access-Control-Allow-Headers", "x-requested-with, Access-Control-Allow-Headers, Content-Type, accept, origin, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            System.out.println("CrossFilter: Handling OPTIONS preflight request for: " + req.getRequestURI());
            resp.setStatus(HttpServletResponse.SC_OK); // 返回 200 OK 状态码
            return; // <<=== ！！！非常关键！！！终止过滤器链，不向下传递，不处理实际业务逻辑
        }

        filterChain.doFilter(req,resp);  //继续执行，放行
    }

    @Override
    public void destroy() {

    }
}
