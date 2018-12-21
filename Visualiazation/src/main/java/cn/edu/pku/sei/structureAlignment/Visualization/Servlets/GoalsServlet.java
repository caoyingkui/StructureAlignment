package cn.edu.pku.sei.structureAlignment.Visualization.Servlets;

import mySql.SqlConnector;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by oliver on 2018/4/15.
 */
public class GoalsServlet extends HttpServlet {
    static int exampleCount ;
    static Map<Integer , String> index2file;
    static {
        exampleCount = 15;
        //exampleCount = new File("Visualiazation\\cook book example").listFiles().length;

    }

    SqlConnector conn = new SqlConnector(
            "jdbc:mysql://127.0.0.1/ASEAnnotation",
            "root",
            "woxnsk",
            "com.mysql.jdbc.Driver");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request , response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userId = request.getParameter("userName");
        String sql = "select goalId from annotation where userId = ?";
        conn.start();
        conn.setPreparedStatement(sql);
        conn.setString(1 , userId);

        try{
            ResultSet rs= conn.executeQuery();
            List<Integer> hasAnnotatedList = new ArrayList<>();
            while(rs.next()){
                hasAnnotatedList.add(rs.getInt(1));
            }

            JSONArray array = new JSONArray();
            for(int i = 1 ; i <= exampleCount ; i ++){
                if(hasAnnotatedList.contains(i)){
                    array.put(true);
                }else{
                    array.put(false);
                }
            }

            JSONObject result = new JSONObject();
            result.put("annotations", array);

            response.setContentType("application/json");
            response.getWriter().print(result.toString());
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            conn.close();
        }


    }
}
