package cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor;

import javafx.util.Pair;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2018/7/24.
 */
public class SOPostsFilter {
    static SqlConnector answerFinder;
    static SqlConnector answerInsertor;
    static{
        answerFinder = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");
        answerFinder.start();

        answerInsertor = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");
        answerInsertor.start();
        String sql = "insert into lucene_answer_copy values(? , ? , ?)";
        answerInsertor.setPreparedStatement(sql);
    }

    public static void main(String[] args){

        List<String> questionIds = extractQuestionId();
        String sql = "insert into lucene_answer_copy values ";
        int temp = sql.length();
        for(String questionId: questionIds){

            Map<String, String> answers = extractAnswers(questionId);
            for(String answerId: answers.keySet()){
                System.out.println(answerId);
                String answerBody= answers.get(answerId);
                List<String> comments = SOPostParser.extractComment(answerBody);
                List<String> codeSnippets = SOPostParser.extractCodeSnippets(answerBody, true);

                if(comments.size()== 0 || codeSnippets.size() != 1 )
                    continue;

                //sql += ( sql.length() > temp ? "," : "" ) + "("+answerId +" , '" + questionId + "',\"" + answerBody +"\") ";

                answerInsertor.setInt(1, Integer.parseInt(answerId));
                answerInsertor.setString(2, questionId);
                answerInsertor.setString(3, answerBody);
                answerInsertor.execute();
                /*System.out.println("-----------------" + answerId + "--------------------");
                System.out.println("comments:");
                for(String comment: comments){
                    System.out.println( comment.trim());
                }
                System.out.println("");

                for(String snippet: codeSnippets){
                    System.out.println("code:");
                    SOExtractor ex = new SOExtractor();
                    List<Pair<Integer, Integer>> alignments = ex.match(snippet, comments);
                    for(Pair<Integer, Integer> pair : alignments){
                        System.out.println(pair.getKey() + " "+ pair.getValue());
                        System.out.println(ex.sm.codeTrees.get(pair.getKey()).getCode());
                        System.out.println(ex.sm.textTrees.get(pair.getValue()).getContent().trim());
                        System.out.println();
                    }

                }*/
            }
        }


    }

    public static List<String> extractQuestionId(){
        List<String> result = new ArrayList<>();
        SqlConnector conn = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        String sql = "select Id, Title from lucene_question";
        conn.start();
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();

        try {
            int total = 0;
            int count = 0;
            while (rs.next()) {
                total ++;
                String title = rs.getString("title");
                if(true || title.trim().startsWith("how to")) {
                    result.add(rs.getString("Id"));
                    //System.out.println(rs.getString("title"));
                    count ++;
                }

            }
            System.out.println(total + " " + count);
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String, String> extractAnswers(String questionId){
        Map<String, String> result = new HashMap<>();
        String sql = "select * from lucene_answer where ParentId = ? and Score > 2";
        answerFinder.setPreparedStatement(sql);
        answerFinder.setString(1, questionId);
        ResultSet rs = answerFinder.executeQuery();
        try{
            while(rs.next()){
                result.put(rs.getString("Id"), rs.getString("Body"));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }




    public void finalize() throws Throwable{
        answerFinder.close();
    }
}