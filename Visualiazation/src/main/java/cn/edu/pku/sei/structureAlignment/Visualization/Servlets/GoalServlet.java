package cn.edu.pku.sei.structureAlignment.Visualization.Servlets;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor.SOExtractor;
import cn.edu.pku.sei.structureAlignment.util.DoubleValue;
import cn.edu.pku.sei.structureAlignment.util.Matrix;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by oliver on 2018/4/15.
 */
public class GoalServlet extends HttpServlet{
    Map<Integer , JSONObject> alignmentsWith = new HashMap<>();
    Map<Integer , JSONObject> alignmentsWithout = new HashMap<>();

    public static void main(String[] args){
        int a;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        doPost(request , response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userName = request.getParameter("userName");
        int goalId  = Integer.parseInt(request.getParameter("goalId"));

        JSONObject result;
        JSONObject resultWith;
        JSONObject resultWithout;

        boolean isWith = false;
        if(Integer.parseInt(userName) % 2 == goalId % 2){
            isWith = true;
        }

        if(alignmentsWith.containsKey(goalId)){
            if(isWith)
                result = alignmentsWith.get(goalId);
            else
                result = alignmentsWithout.get(goalId);
        }else{
            List<Object> metaInfo = getFileMetaInfo(new File("E:\\Intellij workspace\\StructureAlignment\\Visualiazation\\cook book example\\" + goalId + ".txt" ));

            String code = (String) metaInfo.get(2);
            List<String> comments  = (List<String>) metaInfo.get(3);

            CodeLineRelationGraph graph = new CodeLineRelationGraph();
            graph.build(code);

            List<Pair<Integer , Integer>> scheme = new SOExtractor().match(code , comments);

            resultWith = generateJSONObject(goalId ,metaInfo, graph , scheme);
            resultWithout = generateJSONObject(goalId , metaInfo , graph , scheme);
            resultWithout.remove("alignments");

            alignmentsWith.put(goalId , resultWith);
            alignmentsWithout.put(goalId , resultWithout);

            if(isWith)
                result = alignmentsWith.get(goalId);
            else
                result = alignmentsWithout.get(goalId);
        }
        response.setContentType("application/json");
        response.getWriter().print(result.toString());
    }

    List<Object> getFileMetaInfo(File file){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

            String line = "" ;
            while((line = reader.readLine()) != null && line.compareTo("GOAL:") != 0){
                ;
            }

            String Goal = "";
            while((line = reader.readLine()) != null ){
                if(line.compareTo("DESCRIPTION:") == 0)
                    break;
                if(line.trim().length() == 0)
                    continue;
                Goal += (line + "\n");
            }

            String descriptions = "";
            List<String> description = new ArrayList<>();
            while((line = reader.readLine()) != null ){
                if(line.compareTo("CODE:") == 0)
                    break;
                descriptions += (line + " ");
            }
            for(String d : descriptions.split("\\*\\*\\*\\*\\*\\*")){
                description.add(d);
            }

            //description = description.replace("******" , "\n");

            String code = "";
            while((line = reader.readLine()) != null ){
                if(line.compareTo("COMMENT:") == 0)
                    break;
                if(line.trim().length() == 0)
                    continue;
                code += (line + "\n");
            }

            List<String> comments = new ArrayList<>();
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(line.length() == 0)
                    continue;
                comments.add(line);
            }

            List<Object> result = new ArrayList<>();
            result.add(Goal);
            result.add(description);
            result.add(code);
            result.add(comments);
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    JSONObject generateJSONObject(int goalId , List<Object> metaInfo, CodeLineRelationGraph graph , List<Pair<Integer , Integer>> scheme){
        String goal = (String)(metaInfo.get(0));
        List<String> description = (List<String>)(metaInfo.get(1));
        String code = (String)(metaInfo.get(2));
        List<String> comments = (List<String>)(metaInfo.get(3));

        Map<Integer ,List> comment2Lines = new HashMap<>();
        for(Pair<Integer , Integer> pair : scheme){
            int codeLine = pair.getKey();
            int commentLine = pair.getValue();

            if(!comment2Lines.containsKey(commentLine)){
                List<Integer> codeList = new ArrayList<>();
                codeList.add(codeLine);
                comment2Lines.put(commentLine , codeList);

            }else{
                comment2Lines.get(commentLine).add(codeLine);
            }
        }

        Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        };

        JSONObject result = new JSONObject();
        result.put("goalId" , goalId);
        result.put("goal" , goal);


        JSONArray descriptions = new JSONArray();
        for(String d : description){
            descriptions.put(d);
        }
        result.put("description" , descriptions);

        JSONArray codeLines = new JSONArray();
        String[] codes = code.split("\\n");
        for(String c : codes){
            codeLines.put(c);
        }
        result.put("code" , codeLines);

        JSONArray commentLines = new JSONArray();
        for(String comment : comments){
            commentLines.put(comment);
        }
        result.put("comment" , commentLines);

        JSONArray alignments = new JSONArray();
        for(Integer commentLine : comment2Lines.keySet()){
            List<Integer> codeList = comment2Lines.get(commentLine);
            Collections.sort(codeList);

            JSONArray codeArray = new JSONArray();
            for(int codeLine : codeList){
                codeArray.put(graph.statementLineIndexes.get(codeLine));
            }

            JSONArray commentArray = new JSONArray();
            commentArray.put(commentLine);

            JSONObject object = new JSONObject();
            object.put("code" , codeArray);
            object.put("comment" , commentArray);
            alignments.put(object);
        }

        result.put("alignments" , alignments);

        return result;
    }
}
