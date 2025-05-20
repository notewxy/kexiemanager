package com.wan.servlet;

import com.wan.mapper.UsersMapper;
import com.wan.service.impl.UsersServiceImpl;
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

import static com.wan.util.Help.getJson;

@WebServlet("/WelcomeServlet")
@MultipartConfig
public class WelcomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //获取cookie
        Cookie[] cookies = request.getCookies();
        String username=null;
        String password=null;
        if (cookies != null){
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if ("username".equals(name)){
                    username = cookie.getValue();
                }else if ("password".equals(name)){
                    password = cookie.getValue();
                }
            }
        }

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        if (username != null && password != null){

            //验证用户名和密码是否正确
            SqlSessionFactory ssf = SSFactory.getSSF();
            SqlSession sqlSession = ssf.openSession();
            UsersMapper mapper = sqlSession.getMapper(UsersMapper.class);
            UsersServiceImpl usersService = new UsersServiceImpl(mapper);

            String PW = usersService.getPasswordByName(username);

            //刚开始写了PW==password一直出错...... 字符串判断要用equals!!!
            if (PW.equals(password)){
                request.getSession().setAttribute("username",username);
                out.write(getJson("登录成功"));
            }else{
                out.write(getJson("登录失败"));
            }

            sqlSession.close();

        }else{
            out.write(getJson("登录失败"));
        }

        out.close();
    }
}