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
            doDel(request,response);
        }else if ("/GroupMun/add".equals(servletPath)){
            doAdd(request,response);
        }else if ("/GroupMun/update".equals(servletPath)){   //可以选择改变或不改变
            doUpdate(request,response);
        }else if ("/GroupMun/show".equals(servletPath)){  //在修改页面查询具体单个成员信息
            doShow(request,response);
        }


    }

    private void doShow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        GroupMun groupMun = groupMunService.showSingleGroupsMunById(1);
        out.print(groupMun.toString());

        out.close();
        session.close();
    }

    private void doUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        //获取当前成员编号 和 提交的表单，修改当前小组的成员
        //当前的group_id不用修改
        int id = 7;

        GroupMun groupMun = new GroupMun();
        groupMun.setId(id);
        groupMun.setName("小万");
        groupMun.setGender("女");
        groupMun.setNumber(20249);
        groupMun.setWork("组员");
        groupMun.setGroupId(1);

        int i = groupMunService.UpdateGroupMunById(groupMun);

        if (i>0){
            out.print("修改成功");
            out.print("<a href='/SA/GroupMun/list'>返回</a>");
        }else {
            out.print("修改失败");
        }

        session.commit();
        session.close();
    }

    private void doAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        //从前端获取小组编号 和 提交的表单，添加当前小组的成员
        int groupId = 1; //当前小组编号

        GroupMun groupMun = new GroupMun();
        groupMun.setName("小王");
        groupMun.setGender("男");
        groupMun.setNumber(20247);
        groupMun.setWork("组员");
        groupMun.setGroupId(groupId);

        int i = groupMunService.AddGroupMun(groupMun);
        if (i>0){
            out.print("添加成功");
            out.print("<a href='/SA/GroupMun/list'>返回</a>");
        }else {
            out.print("添加失败");
        }


        out.close();

        session.commit();
        session.close();
    }

    private void doDel(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        //从前端获取当前的成员id，删除成员
        int id = 6;
        int i = groupMunService.delGroupMunById(id);
        if (i>0){
            out.print("删除成功");
            out.print("<a href='/SA/GroupMun/list'>返回</a>");
        }else {
            out.print("删除失败");
        }

        out.close();

        session.commit();
        session.close();
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
        for (GroupMun groupMun : groupMuns) {
            out.print(groupMun.toString());
        }

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
