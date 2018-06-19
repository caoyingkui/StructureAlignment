import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import scala.Int;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2018/3/7.
 */



public class Test {

    static  int count  = 1;

    public static void main(String[] args) throws  Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File("H:\\stackoverflow\\temp\\cassandra\\posts.xml")));
        BufferedWriter writer1 = new BufferedWriter(new FileWriter( new File("H:\\stackoverflow\\temp\\cassandra\\questions.xml")));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File("H:\\stackoverflow\\temp\\cassandra\\answers.xml")));
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                if(line.contains("PostTypeId=\"1\"")){
                    writer1.write(line + "\n" );
                }else if(line.contains("PostTypeId=\"2\"")){
                    writer2.write(line + "\n");
                }else{
                    System.out.println("error!");
                }
            }

            writer1.flush();
            writer2.flush();
            writer1.close();
            writer2.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void main1(String[] args) throws Exception {
        List<Integer> list2 = new ArrayList<>();
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list0 = new ArrayList<>();


        SqlConnector conn = new SqlConnector(
                "jdbc:mysql://127.0.0.1/labelresulttables",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver"
        );


        conn.start();
        conn.setPreparedStatement("select itemID from results_9 where result = 2");
        ResultSet rs = conn.executeQuery();
        while(rs.next()){
            list2.add(rs.getInt(1));
        }

        conn.setPreparedStatement("select itemID from results_9 where result = 1");
        rs = conn.executeQuery();
        while(rs.next()){
            list1.add(rs.getInt(1));
        }

        conn.setPreparedStatement("select itemID from results_9 where result = 0");
        rs = conn.executeQuery();
        while(rs.next()){
            list0.add(rs.getInt(1));
        }
        conn.close();

        conn = new SqlConnector(
                "jdbc:mysql://127.0.0.1/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        conn.start();
        conn.setPreparedStatement("select postId , pair from SOParallel where id = ?");


        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("corpus.txt")));
        for(int id: list2){
            conn.setInt(1 , id);
            rs = conn.executeQuery();
            if(rs.next()) {
                String pair = rs.getString(2);
                parse(writer , pair , rs.getInt(1));
            }
        }

        for(int id: list1){
            conn.setInt(1 , id);
            rs = conn.executeQuery();
            if(rs.next()) {
                String pair = rs.getString(2);
                parse(writer , pair , rs.getInt(1));
            }
        }

        for(int id: list0){
            conn.setInt(1 , id);
            rs = conn.executeQuery();
            if(rs.next()) {
                String pair = rs.getString(2);
                parse(writer , pair , rs.getInt(1));
            }
        }

        writer.close();
        conn.close();




     }

     public static void parse(BufferedWriter writer , String pair , int post) throws Exception {
        String code = "";
        String comment = "";

        int start = pair.indexOf("<p>");
        int end = pair.indexOf("</p>");
        comment = pair.substring(start + 3 , end);

        start = pair.indexOf("<code>");
        end = pair.indexOf("</code>");
        code = pair.substring(start + 6 , end);

        /*Pattern pattern = Pattern.compile("<p>((.|\\s)+)</p>");
        Matcher matcher = pattern.matcher(pair);
        while(matcher.find()){
            comment += matcher.group(1) + " ";
        }*/

        comment = comment.trim();
        if(comment.charAt(comment.length() - 1) == '\n'){
            comment = comment.substring(0 , comment.length() - 1);
        }


        /*pattern = Pattern.compile("<pre><code>((.|\\s)+)</code></pre>");
        matcher = pattern.matcher(pair);
        while(matcher.find()){
            code += matcher.group(1) + " ";
        }*/

        if(code.charAt(code.length() -1) ==  '\n'){
            code = code.substring(0 , code.length() - 1);
        }

        writer.write("****************************** #"+ count +" ******************************\n");
        writer.write("StackOverflow URL: https://stackoverflow.com/questions/" + post + "\n\n");

        writer.write("comment sentences(s):\n");
        writer.write(comment + "\n\n");

        writer.write("code statement(s):\n");
        writer.write(code + "\n\n");

        count ++;







     }


    @Override
    public String toString() {
        return "i like testing!";
    }
}
