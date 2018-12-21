package cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2018/7/24.
 */
public class SOPostParser {

    public static void main(String[] args){
        SqlConnector conn = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");
        String sql = "select Body from lucene_answer";
        conn.start();
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();
        try {
            while (rs.next()) {
                System.out.println("-----------------------------------------------");
                String body = rs.getString("Body");
                System.out.println(body.trim());
                System.out.println();
                body = filterHtmlTag(body);
                System.out.println(body.trim());
                System.out.println();

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static List<String> extractCodeSnippets(String postBody, boolean compilable){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("<code>((.|\\s)+?)</code>", Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
        Matcher matcher = pattern.matcher(postBody);

        while(matcher.find()){
            String code = matcher.group(1);
            if(!compilable || isCompilable(code))
                result.add(code);
        }
        return result;
    }

    public static List<String> extractComment(String postBody){
        postBody = filterHtmlTag(postBody);
        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("<code>((.|\\s)+?)</code>", Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
        Matcher matcher = pattern.matcher(postBody);
        int former = 0;
        while(matcher.find()){
            result.append(" ").append(postBody.substring(former, matcher.start()).trim());
            String code = matcher.group(1).trim();
            if(code.length() < 30 && !isCompilable(code)) {
                result.append(" ").append(code);
            }else{
                if(result.charAt(result.length() - 1) != '.')
                    result.append(". ");
            }
            former = matcher.end();
        }

        return commentSplit(result.toString());
    }

    public static List<String> extractComments(String postBody){
        postBody = filterHtmlTag(postBody);
        List<String> result = new ArrayList<>();

        Pattern pattern = Pattern.compile("<code>((.|\\s)+?)</code>",Pattern.CASE_INSENSITIVE+Pattern.DOTALL);
        Matcher matcher = pattern.matcher(postBody);

        int former = 0;
        while(matcher.find()){
            String comment = postBody.substring(former, matcher.start()).trim();
            if(comment.split(" ").length > 3) {
                result.addAll(
                        commentSplit(comment)
                );
            }
            former = matcher.end();
        }
        return result;
    }

    public static List<String> commentSplit(String comment){
        List<String> result = new ArrayList<>();
        comment = comment.trim();
        Reader reader = new StringReader(comment);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        for(List<HasWord> sentence: dp){
            String sentenceString = SentenceUtils.listToString(sentence);
            result.add(sentenceString);
        }
        return result;
    }

    public static boolean isCompilable(String code){
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);

        Block block = (Block) parser.createAST(null);
        return block.statements().size() > 0;
    }

    public static String filterHtmlTag(String s){

        String base = "<BASE>((.|\\s)+?)</BASE>";
        s = filterTag(s, base.replaceAll("BASE", "pre"));

        s = filterTag(s, base.replaceAll("BASE", "p"));

        s = filterTag(s, base.replaceAll("BASE", "ul"));
        s = filterTag(s, base.replaceAll("BASE", "li"));
        s = filterTag(s, base.replaceAll("BASE", "em"));
        s = filterTag(s, "<a [^>]+?>((.|\\s)+?)</a>");

        s = filterHyperLink(s);
        return s;
    }

    public static String filterTag(String s, String rex){
        String head="", tail="";
        boolean isP = false;
        if(rex.startsWith("<p>")){
            isP = true;
        }

        s = s.trim();
        String result ="";
        Pattern pattern = Pattern.compile(rex,Pattern.CASE_INSENSITIVE+Pattern.DOTALL+Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(s);
        int former = 0;
        while(matcher.find()){
            result += s.substring(former, matcher.start()).trim();
            result += matcher.group(1);
            if(isP && result.charAt(result.length() - 1) == '.')
                result += ". ";
            former = matcher.end();
        }
        if(former < s.length())
            result += s.substring(former);
        return result;
    }

    public static String filterHyperLink(String s){
        return s.replaceAll("[a-zA-z]+://[^\\s]*", "");
    }


}
