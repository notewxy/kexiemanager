package com.wan.servlet;

import com.google.gson.Gson;
import com.wan.mapper.AllGroupsMapper;
import com.wan.pojo.AllGroups;
import com.wan.r.R;
import com.wan.service.AllGroupsService;
import com.wan.service.impl.AllGroupsServiceImpl;
import com.wan.util.Help;
import com.wan.util.SSFactory;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static com.wan.util.Help.getJson;

@WebServlet({"/AllGroups/*"})
public class AllGroupsServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath()+request.getPathInfo();

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("username")!=null){
            if ("/AllGroups/list".equals(servletPath)){
                System.out.println("???????????");
                System.out.flush();
                doList(request,response);
            }else if ("/AllGroups/del".equals(servletPath)){
                doDel(request,response);
            }else if ("/AllGroups/add".equals(servletPath)){
                doAdd(request,response);
            }else if ("/AllGroups/update".equals(servletPath)){
                doUpdate(request,response);
            }else if ("/AllGroups/show".equals(servletPath)){
                doShow(request,response);
            }
        }else{
            out.print(getJson("请先登录"));
        }

        out.close();
    }

    private void doShow(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        AllGroupsService allGroupsService = getAllGroupsService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        AllGroups allGroups = allGroupsService.showSingleGroupById(1);

        out.write(getJson(allGroups));

        out.close();

        session.close();
    }

    private void doUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        AllGroupsService allGroupsService = getAllGroupsService(session);

        //获取修改的当前id,获取修改的name和introduce
        AllGroups allGroups = new AllGroups();
        allGroups.setId(4);
        allGroups.setName("2");
        allGroups.setIntroduce("222");

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        int i = allGroupsService.UpdateGroupsById(allGroups);
        System.out.println(i);
        if (i>0){
            out.print("修改成功");
        }else {
            out.print("修改失败");
        }

        out.close();

        session.commit();
        session.close();
    }

    private void doAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        SqlSession session = getSession();
        AllGroupsService allGroupsService = getAllGroupsService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();


        //获取前端表单,只需要name和introduce就行
        AllGroups allGroups = new AllGroups();
        allGroups.setName("www");
        allGroups.setIntroduce("111");

        int i = allGroupsService.AddGroups(allGroups);
        System.out.println(i);
        if (i>0){
            out.print("添加成功");
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
        AllGroupsService allGroupsService = getAllGroupsService(session);

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();

        out.print("你好");


        //这个数字到时候需要在前端获取
        String idStr = request.getParameter("id");
        int id = Integer.parseInt(idStr);

        int i = allGroupsService.DelGroupsById(id);
        System.out.println(i);
        if (i>0){
            out.print("删除成功");
        }else {
            out.print("删除失败");
        }

        out.close();
        session.commit();
        session.close();
    }

    private void doList(HttpServletRequest request, HttpServletResponse response) throws IOException {

        SqlSession session = getSession();
        AllGroupsService allGroupsService = getAllGroupsService(session);

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

        out.write(getJson(allGroups));

        out.close();

        session.close();
    }

    //封装获取service的代码
    public AllGroupsService getAllGroupsService(SqlSession session){

        AllGroupsMapper allGroupsMapper = session.getMapper(AllGroupsMapper.class);
        AllGroupsService allGroupsService = new AllGroupsServiceImpl(allGroupsMapper);

        return allGroupsService;
    }

    //封装获取session的代码
    public SqlSession getSession(){

        SqlSessionFactory factory = SSFactory.getSSF();
        SqlSession session = factory.openSession();

        return session;
    }
}
