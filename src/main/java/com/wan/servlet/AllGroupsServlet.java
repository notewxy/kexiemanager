package com.wan.servlet;

import com.google.gson.Gson;
import com.wan.mapper.AllGroupsMapper;
import com.wan.pojo.AllGroups;
import com.wan.r.R;
import com.wan.service.AllGroupsService;
import com.wan.service.impl.AllGroupsServiceImpl;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

@WebServlet({"/AllGroups/list"})
public class AllGroupsServlet extends HttpServlet {



    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/AllGroups/list".equals(servletPath)){
            doList(request,response);
        }
    }

    private void doList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        InputStream is = Resources.getResourceAsStream("mybatis.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(is);
        SqlSession session = factory.openSession();
        AllGroupsMapper allGroupsMapper = session.getMapper(AllGroupsMapper.class);

        AllGroupsService allGroupsService = new AllGroupsServiceImpl(allGroupsMapper);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        List<AllGroups> allGroups = allGroupsService.showGroups();
        /*for (AllGroups allGroup : allGroups) {
            out.println(allGroup.getId()+"<br>");
            out.println(allGroup.getName()+"<br>");
            out.println(allGroup.getIntroduce()+"<br>");
            out.println(allGroup.getImage()+"<br>");
        }*/

        R r = new R();
        r.setCode("200");
        r.setState("ok");
        r.setData(allGroups);

        Gson gson = new Gson();
        String json = gson.toJson(r);
        out.write(json);
        out.close();

        session.commit();
        session.close();
    }
}
