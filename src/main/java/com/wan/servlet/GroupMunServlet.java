package com.wan.servlet;

import com.wan.mapper.AllGroupsMapper;
import com.wan.mapper.GroupMunMapper;
import com.wan.pojo.GroupMun;
import com.wan.service.AllGroupsService;
import com.wan.service.GroupMunService;
import com.wan.service.impl.AllGroupsServiceImpl;
import com.wan.service.impl.GroupMunServiceImpl;
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
import java.util.List;

@WebServlet({"/GroupMun/*"})
public class GroupMunServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath()+request.getPathInfo();

        if ("/GroupMun/list".equals(servletPath)){
            doList(request,response);
        }else if ("/GroupMun/del".equals(servletPath)){
            //doDel(request,response);
        }else if ("/GroupMun/add".equals(servletPath)){
            //doAdd(request,response);
        }else if ("/GroupMun/update".equals(servletPath)){
            //doUpdate(request,response);
        }


    }

    private void doList(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        //获取当前的小组编号，在成员表中查找对应小组的成员
        int groupId = 1;

        List<GroupMun> groupMuns = groupMunService.showGroupsMunById(groupId);
        /*for (GroupMun groupMun : groupMuns) {
            out.print(groupMun.toString());
        }*/

        out.close();

        session.close();
    }


    //封装获取service的代码
    public GroupMunService getGroupMunService(SqlSession session){

        GroupMunMapper mapper = session.getMapper(GroupMunMapper.class);
        GroupMunServiceImpl groupMunService = new GroupMunServiceImpl(mapper);

        return groupMunService;
    }

    //封装获取session的代码
    public SqlSession getSession(){

        SqlSessionFactory factory = SSFactory.getSSF();
        SqlSession session = factory.openSession();

        return session;
    }
}
