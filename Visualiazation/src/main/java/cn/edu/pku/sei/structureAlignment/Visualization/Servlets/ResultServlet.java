package cn.edu.pku.sei.structureAlignment.Visualization.Servlets;

import mySql.SqlConnector;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by oliver on 2018/4/15.
 */
public class ResultServlet extends HttpServlet {
    static SqlConnector conn = new SqlConnector(
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
        String userName = request.getParameter("userName");
        int goalId = Integer.parseInt(request.getParameter("goalId"));
        double time = Double.parseDouble(request.getParameter("time"));
        int rate = Integer.parseInt(request.getParameter("rate"));

        String sql = "insert into annotation (userId , goalId , time , rate) values (\""+ userName +"\" ," + goalId + " , " + time + " , " + rate + ")";
        conn.start();
        conn.setPreparedStatement(sql);
        conn.execute();
        conn.close();

        JSONObject result = new JSONObject();
        result.put("condition" , "succeed");

        response.setContentType("application/json");
        response.getWriter().print(result.toString());
    }
}
