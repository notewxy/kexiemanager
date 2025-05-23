package com.wan.servlet;

import com.wan.mapper.AllGroupsMapper;
import com.wan.mapper.UsersMapper;
import com.wan.pojo.Users;
import com.wan.service.AllGroupsService;
import com.wan.service.UsersService;
import com.wan.service.impl.AllGroupsServiceImpl;
import com.wan.service.impl.UsersServiceImpl;
import com.wan.util.JwtUtil;
import com.wan.util.SSFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static com.wan.util.Help.getJson;

@WebServlet({"/api/Users/*"})
@MultipartConfig
public class UsersServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath()+request.getPathInfo();

        if ("/api/Users/login".equals(servletPath)){
            doCheck(request,response);
        }else if ("/api/Users/register".equals(servletPath)){
            doRegister(request,response);
        }

    }

    private void doRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        UsersService usersService = getUsersService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        //获取前端的表单，先判断用户名是否存在，如果存在，则提示用户名已存在，如果不存在，则添加用户
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Users users = new Users(username, password);

        String truePW = usersService.getPasswordByName(username);
        //out.print("truePW:"+truePW);

        //将结果反馈给前端
        if (truePW != null){
            out.print(getJson("用户名已存在"));
        }else {
            int i = usersService.AddUsers(users);
            if (i<=0){
                out.print(getJson("注册失败"));
            }else{
                out.print(getJson("注册成功"));
                //到时候前端再转到登录页面
            }
        }

        out.close();
        session.commit();
        session.close();
    }

    private void doCheck(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        UsersService usersService = getUsersService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        /*java.util.Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap != null && !parameterMap.isEmpty()) {
            System.out.println("---- Received Parameters ----");
            for (java.util.Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + java.util.Arrays.toString(entry.getValue()));
            }
            System.out.println("-----------------------------");
        } else {
            System.out.println("No parameters found in request.getParameterMap().");
        }*/

        //获取前端的用户名和密码，通过用户名查询对应的密码，并进行比较
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String f = request.getParameter("rememberMe");

        String truePW = usersService.getPasswordByName(username);
        //out.print("truePW:"+truePW);

        //将结果反馈给前端
        if (truePW == null){
            out.write(getJson("用户名不存在"));
        }else if (truePW.equals(password)){

            // 用户认证成功
            Map<String, Object> claimsForAccessToken = new HashMap<>();
            // 将从数据库获取的用户ID、角色等信息放入 claims
            //claimsForAccessToken.put("userId", request.getAttribute("userId")); // 示例
            //claimsForAccessToken.put("roles", request.getAttribute("roles")); // 示例

            // 1. 生成 Access Token
            String accessToken = JwtUtil.generateAccessToken(username, claimsForAccessToken);
            Map<String, String> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken); // 将 Access Token 返回给前端

            // 2. 根据前端的“记住我”标志决定是否生成 Refresh Token
            if ("1".equals(f)) { // 如果 f 为 "1"，表示用户选择了“十天内免登录”
                String refreshToken = JwtUtil.generateRefreshToken(username);
                responseData.put("refreshToken", refreshToken); // 将 Refresh Token 返回给前端
                System.out.println("User " + username + " logged in with 'remember me' option. Refresh Token generated.");
            } else {
                System.out.println("User " + username + " logged in (no 'remember me' option selected).");
            }

            // 3. 使用 Help.getJson() 封装成功响应数据并发送
            out.print(getJson(responseData));
            System.out.println("Login successful for user: " + username + ". Response sent.");



            /*out.write(getJson("登录成功"));
            //创建成功，存入用户名
            request.getSession().setAttribute("username",username);

            //前端获取"是否选择十天内免登录的选项",判断是否为1
            if ("1".equals(f)){
                Cookie cookie1 = new Cookie("username", username);
                Cookie cookie2 = new Cookie("password", password);

                cookie1.setMaxAge(60*60*24*10);   //10天
                cookie2.setMaxAge(60*60*24*10);

                cookie1.setPath(request.getContextPath());
                cookie2.setPath(request.getContextPath());

                //响应给浏览器
                response.addCookie(cookie1);
                response.addCookie(cookie2);
            }else{
                //前端应该不需要再判断了，每当发送请求的时候，访问后端，将后端的检验结果返回即可。
            }*/

        }else {
            out.write(getJson("密码错误"));
        }

        out.close();
        session.close();
    }

    //封装获取service的代码
    public UsersService getUsersService(SqlSession session){

        UsersMapper mapper = session.getMapper(UsersMapper.class);
        UsersServiceImpl usersService = new UsersServiceImpl(mapper);

        return usersService;
    }

    //封装获取session的代码
    public SqlSession getSession(){

        SqlSessionFactory factory = SSFactory.getSSF();
        SqlSession session = factory.openSession();

        return session;
    }
}
