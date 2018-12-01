import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.json.JSONArray;
import org.json.JSONObject;
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
        BufferedReader reader = new BufferedReader(new FileReader(new File("code.txt")));
        ArrayList<String> code = new ArrayList<>();
        ArrayList<String> comment = new ArrayList<>();

        String line = "";
        while((line = reader.readLine()) != null)
            code.add(line + '\n');

        reader = new BufferedReader(new FileReader(new File("corpus.txt")));
        while((line = reader.readLine()) != null)
            comment.add(line + '\n');

        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        for(String lne: code){
            array.put(lne);
        }
        object.put("code", array);

        JSONArray a = new JSONArray();
        for(String l: comment) {
            a.put(l);
        }
        object.put("comment", a);
        System.out.println(object.toString());

        /*System.out.println(lcs("Re-analyze", "analysis"));*/

        /*SqlConnector conn = new SqlConnector(
                "jdbc:mysql://127.0.0.1/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver"
        );
        conn.start();
        String sql = "select name, returnType, argumentTypes, argumentNames, javadoc from api where type = \"METHOD\"";
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();
        BufferedWriter mnWriter = new BufferedWriter(new FileWriter(new File("D:\\lstm data\\train.methname.txt")));
        BufferedWriter asWriter = new BufferedWriter(new FileWriter(new File("D:\\lstm data\\train.apiseq.txt")));
        BufferedWriter tWriter = new BufferedWriter(new FileWriter(new File("D:\\lstm data\\train.tokens.txt")));
        BufferedWriter dWriter = new BufferedWriter(new FileWriter(new File("D:\\lstm data\\train.desc.txt")));

        int count = 0;
        while(rs.next()){
            String javadoc = rs.getString("javadoc").trim();
            if(javadoc.length() == 0)
                continue;
            int start = javadoc.indexOf(". ");
            if(start > -1)
                javadoc = javadoc.substring(0, start + 1);
            dWriter.write(javadoc.replaceAll("[ ]+" , " ") + "\n");

            String methodName = rs.getString("name");
            List<String> names = Stemmer.camelCase(methodName);
            mnWriter.write(String.join(" ", names) + "\n");

            String[] arguments = rs.getString("argumentTypes").split(" \\| ");
            asWriter.write(rs.getString("returnType") + " " + String.join(" " , arguments) + "\n");

            if(arguments.length == 0){
                tWriter.write("NULL" + "\n");
            }else{
                String[] argumentNames = rs.getString("argumentNames").split(" \\| ");
                tWriter.write(String.join(" ", argumentNames )+ "\n");
            }

            count ++;
        }

        System.out.println(count);

        conn.close();
        mnWriter.close();
        asWriter.close();
        tWriter.close();
        dWriter.close();*/
    }

    // longest comment substring
    static int lcs(String str1, String str2){
        str1 = str1.trim().toLowerCase();
        str2 = str2.trim().toLowerCase();
        int ans = 0;
        int length = Math.min(str1.length(), str2.length());
        int l = length;
        while(l > 0){
            for(int i = 0 ; i + l <= length ; i ++){
                for(int j = 0  ; j + l <= length ; j++){
                    if(str1.substring(i , i + l ).compareTo(str2.substring(j, j + l)) == 0)
                        return l;
                }
            }

            l --;
        }
        return 0;
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


    public String toGoatLatin(String S) {
        List<Character> vowel  = new ArrayList<Character>();
        vowel.add('a');
        vowel.add('e');
        vowel.add('i');
        vowel.add('o');
        vowel.add('u');



        String[] words = S.split(" ");
        String result = "";
        for(int i = 0 ; i < words.length ; i ++){
            if(vowel.contains(words[i].toLowerCase().charAt(0))){
                words[i] +=  "ma";
            }else {
                words[i] += (words[i].substring(1) + "ma");
            }

            for(int j = 0 ; j < i + 1 ; j ++){
                words[i] += "a";
            }

            if(i == 0)
                result = words[i];
            else
                result += (" " +words[i]);
        }

        return result;

    }

    @Override
    public String toString() {
        return "i like testing!";
    }
}
