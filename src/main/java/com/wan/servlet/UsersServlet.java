package com.wan.servlet;

import com.wan.mapper.AllGroupsMapper;
import com.wan.mapper.UsersMapper;
import com.wan.pojo.Users;
import com.wan.service.AllGroupsService;
import com.wan.service.UsersService;
import com.wan.service.impl.AllGroupsServiceImpl;
import com.wan.service.impl.UsersServiceImpl;
import com.wan.util.SSFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.wan.util.Help.getJson;

@WebServlet({"/Users/*"})
public class UsersServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath()+request.getPathInfo();

        if ("/Users/login".equals(servletPath)){
            doCheck(request,response);
        }else if ("/Users/register".equals(servletPath)){
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
        String username = "wanwan";
        String password = "789";
        Users users = new Users(username, password);

        String truePW = usersService.getPasswordByName(username);
        out.print("truePW:"+truePW);

        //将结果反馈给前端
        if (truePW != null){
            out.print(getJson("用户名已存在"));
        }else {
            int i = usersService.AddUsers(users);
            if (i<=0){
                out.print(getJson("注册失败"));
            }else{
                out.print(getJson("注册成功"));
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

        //获取前端的用户名和密码，通过用户名查询对应的密码，并进行比较
        String username = "wan";
        String password = "1234";

        String truePW = usersService.getPasswordByName(username);
        //out.print("truePW:"+truePW);

        //将结果反馈给前端
        if (truePW == null){
            out.write(getJson("用户名不存在"));
        }else if (truePW.equals(password)){
            out.write(getJson("登录成功"));
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
