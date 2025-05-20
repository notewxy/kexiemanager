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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.wan.util.Help.getJson;

@WebServlet({"/GroupMun/*"})
@MultipartConfig
public class GroupMunServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath()+request.getPathInfo();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("username")!=null){
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
            }else if ("/GroupMun/all".equals(servletPath)){
                doAll(request,response);
            }else if ("/GroupMun/sort1".equals(servletPath)){  //升序
                doSort1(request,response);
            }else if ("/GroupMun/sort2".equals(servletPath)){  //降序
                doSort2(request,response);
            }
        }else{
            out.write(getJson("请先登录"));
        }

        out.close();
    }

    private void doSort2(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        List<GroupMun> groupMuns = groupMunService.sortByNumberDown();

        out.write(getJson(groupMuns));

        out.close();

        session.close();
    }

    private void doSort1(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        List<GroupMun> groupMuns = groupMunService.sortByNumberUp();

        out.write(getJson(groupMuns));

        out.close();

        session.close();
    }

    private void doAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        List<GroupMun> groupMuns = groupMunService.showAll();

        out.write(getJson(groupMuns));

        out.close();

        session.close();
    }

    private void doShow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        GroupMunService groupMunService = getGroupMunService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        int id = Integer.parseInt(request.getParameter("id"));

        GroupMun groupMun = groupMunService.showSingleGroupsMunById(id);

        out.write(getJson(groupMun));   //向前端传递数据

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
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String gender = request.getParameter("gender");
        int number = Integer.parseInt(request.getParameter("number"));
        String work = request.getParameter("work");
        int groupId = Integer.parseInt(request.getParameter("groupid"));

        GroupMun groupMun = new GroupMun();
        groupMun.setId(id);
        groupMun.setName(name);
        groupMun.setGender(gender);
        groupMun.setNumber(number);
        groupMun.setWork(work);
        groupMun.setGroupId(groupId);

        int i = groupMunService.UpdateGroupMunById(groupMun);

        if (i>0){
            out.print(getJson("修改成功"));
            out.print("<a href='/SA/GroupMun/list'>返回</a>");
        }else {
            out.print(getJson("修改失败"));
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

        //从前端获取提交的表单，添加当前小组的成员
        String name = request.getParameter("name");
        String gender = request.getParameter("gender");
        int number = Integer.parseInt(request.getParameter("number"));
        String work = request.getParameter("work");
        int groupId = Integer.parseInt(request.getParameter("groupid"));

        GroupMun groupMun = new GroupMun();
        groupMun.setName(name);
        groupMun.setGender(gender);
        groupMun.setNumber(number);
        groupMun.setWork(work);
        groupMun.setGroupId(groupId);

        int i = groupMunService.AddGroupMun(groupMun);
        if (i>0){
            out.print(getJson("添加成功"));
            out.print("<a href='/SA/GroupMun/list'>返回</a>");
        }else {
            out.print(getJson("添加失败"));
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
        int id = Integer.parseInt(request.getParameter("id"));
        int i = groupMunService.delGroupMunById(id);
        if (i>0){
            out.print(getJson("删除成功"));
        }else {
            out.print(getJson("删除失败"));
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
        int groupId = Integer.parseInt(request.getParameter("groupid"));

        List<GroupMun> groupMuns = groupMunService.showGroupsMunById(groupId);
        /*for (GroupMun groupMun : groupMuns) {
            out.print(groupMun.toString());
        }*/
        out.write(getJson(groupMuns));   //向前端传递数据

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
