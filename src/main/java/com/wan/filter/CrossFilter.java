package com.wan.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")     /* 针对哪些有效 */
public class CrossFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)servletRequest;
        HttpServletResponse resp = (HttpServletResponse)servletResponse;

        /* 解决跨域问题 */
        resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:8081");
        // 允许的方法（如POST, GET等）
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        // 预检请求的缓存时间
        resp.setHeader("Access-Control-Max-Age", "3600");
        // 允许的头信息
        resp.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type, accept, origin, authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        filterChain.doFilter(req,resp);  //继续执行，放行
    }

    @Override
    public void destroy() {

    }
}
