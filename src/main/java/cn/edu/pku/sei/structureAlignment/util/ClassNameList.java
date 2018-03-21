package cn.edu.pku.sei.structureAlignment.util;

import mySql.SqlConnector;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by oliver on 2018/2/7.
 */
public class ClassNameList {
    public static List<String> classList;
    static{
        classList = new ArrayList<>();
        ResourceBundle bundle = ResourceBundle.getBundle("database");
        String url = bundle.getString("luceneAPI_url");
        String user = bundle.getString("luceneAPI_user");
        String pwd = bundle.getString("luceneAPI_pwd");
        String driver = bundle.getString("luceneAPI_driver");
        String tableName = bundle.getString("luceneAPI_table");
        String sql = "select distinct name from " + tableName + " where type = 'CLASS'";

        SqlConnector conn = new SqlConnector(url , user , pwd , driver);
        conn.start();
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();

        if(rs != null){
            try {
                while (rs.next()) {
                    String name = rs.getString(1);
                    classList.add(name);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        conn.close();
    }

    public static boolean contains(String word){
        return classList.contains(word);
    }
}
