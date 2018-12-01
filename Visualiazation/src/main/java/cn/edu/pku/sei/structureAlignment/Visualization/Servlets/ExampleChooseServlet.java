package cn.edu.pku.sei.structureAlignment.Visualization.Servlets;



import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor.SOExtractor;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.MatchedNode;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import cn.edu.pku.sei.structureAlignment.util.CamelCaseDictionary;
import edu.stanford.nlp.simple.Sentence;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2018/3/21.
 */
public class ExampleChooseServlet extends HttpServlet{

    Map<Integer , Pair<List<CodeStructureTree>, List<TextStructureTree>>> examples ;
    List<String> codes;
    List<String> commentList;

    void initialize() {
        examples = new HashMap<Integer, Pair<List<CodeStructureTree>, List<TextStructureTree>>>();
        String path = "C:\\Users\\oliver\\Desktop\\数据\\stackoverflow";
        codes = new ArrayList<>();
        commentList = new ArrayList<>();

        SOExtractor extractor = new SOExtractor();
        BufferedReader reader;
        String line;
        int count = 0;
        for (File file : new File(path).listFiles()) {
            try {
                System.out.println(file.getName());
                String code = "";

                reader = new BufferedReader(new FileReader(file));
                while ((line = reader.readLine()).trim().length() != 0) {
                    code += (line + "\n");
                }

                codes.add(code);
                CodeLineRelationGraph graph = new CodeLineRelationGraph();
                graph.build(code);

                CamelCaseDictionary dictionary = new CamelCaseDictionary(graph);
                List<String> comments = new ArrayList<>();


                String c = "";
                boolean meetAnnotations = false;
                while ((line = reader.readLine()) != null && line.compareTo("END") != 0) {
                    if(line.trim().length() == 0)
                        meetAnnotations = true;
                    c += (line + "\n") ;

                    if(!meetAnnotations)
                        comments.add(dictionary.mergeTokenByCamelCase(line));
                }
                commentList.add(c);

                List<TextStructureTree> textTrees = new ArrayList<>();
                for (String comment : comments) {
                    TextStructureTree textTree = new TextStructureTree(0);
                    textTree.construct(new Sentence(comment));
                    textTrees.add(textTree);

                }
                extractor.match(graph.getCodeLineTrees(), textTrees , null);

                examples.put(count++, new Pair<>(graph.getCodeLineTrees(), textTrees));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public ExampleChooseServlet(){
        initialize();
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req , resp);
    }



    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        int example = Integer.parseInt(req.getParameter("codeLine"));
        int comment = Integer.parseInt(req.getParameter("comment"));

        if(example < 0 || example >= examples.size() ||
                comment < 0 || comment >= examples.get(example).getValue().size()){
            resp.setContentType("application/json");
            resp.getWriter().print("");
        }else {
            Pair<List<CodeStructureTree>, List<TextStructureTree>> pair = examples.get(example);
            List<CodeStructureTree> codeTrees = pair.getKey();
            List<TextStructureTree> textTrees = pair.getValue();

            TextStructureTree textTree = textTrees.get(comment);
            JSONObject result = new JSONObject();

            JSONObject chart = new JSONObject();
            chart.put("container", "#displayResult");

            JSONObject node = new JSONObject();
            node.put("collapsable", "true");
            chart.put("node", node);

            JSONObject animation = new JSONObject();
            animation.put("nodeAnimation", "easeOutBounce");
            animation.put("nodeSpeed", 700);
            animation.put("connectorsAnimation", "bounce");
            animation.put("connectorsSpeed", 700);
            chart.put("animation", animation);

            result.put("chart", chart);

            JSONObject child = getCollapsableChildren(textTree, codeTrees);
            result.put("nodeStructure", child);

            JSONObject finalResult = new JSONObject();
            finalResult.put("exampleNum", example);
            finalResult.put("commentNum", comment);
            finalResult.put("comment", commentList.get(example));
            finalResult.put("code", codes.get(example));
            finalResult.put("chart", result);

            Cookie cookie = new Cookie("chart" , result.toString());
            resp.setContentType("application/json");
            resp.getWriter().print(finalResult.toString());
            //RequestDispatcher dispatcher = req.getRequestDispatcher("index.jsp");
            //dispatcher.forward(req , resp);
        }

    }

    static JSONObject getCollapsableChildren(TextStructureTree textTree , List<CodeStructureTree> codeTrees){
        JSONObject result = null;
        List<TextStructureTree> childTrees = textTree.getChildren();
        if(childTrees.size() == 0 ){

            if(textTree.root.matchedCodeNodeList.size() > 0){
                result = new JSONObject();
                JSONObject matchedNodes = new JSONObject();


                matchedNodes.put("1" , textTree.getContent());
                matchedNodes.put("2" , "---");
                int count = 3;
                for(MatchedNode matchedNode : textTree.root.matchedCodeNodeList){
                    int codeLine = matchedNode.matchedTreeID;
                    CodeStructureTree codeTree = codeTrees.get(codeLine);
                    matchedNodes.put(count ++ + "" , codeLine + " " + matchedNode.matchedNodeID + ": " + codeTree.getTree(matchedNode.matchedNodeID).getCode());
                }
                result.put("text" , matchedNodes);
                result.put("collapsed" , "false");
            }
        }else{
            if(textTree.root.matchedCodeNodeList.size() > 0){
                JSONObject matchedNodes = new JSONObject();
                result = new JSONObject();



                int count = 3;
                matchedNodes.put("1" , textTree.getContent());
                matchedNodes.put("2" , "---");
                for(MatchedNode matchedNode : textTree.root.matchedCodeNodeList){
                    int codeLine = matchedNode.matchedTreeID;
                    int node = matchedNode.matchedNodeID;

                    CodeStructureTree codeTree = codeTrees.get(codeLine);
                    matchedNodes.put(count ++ + "" , codeLine  + " " + node + ": " + codeTree.getTree(node).getCode());
                }


                result.put("text" , matchedNodes);
                result.put("collapsed" , "true");

                JSONArray children = new JSONArray();
                for(TextStructureTree childTree : childTrees){
                    JSONObject child = getCollapsableChildren(childTree , codeTrees);
                    if(child != null)
                        children.put(child);
                }
                result.put("children" , children);
            }else{
                int count = 0;
                for(TextStructureTree childTree : childTrees){
                    JSONObject child = getCollapsableChildren(childTree , codeTrees);
                    if(child != null){
                        result = child;
                        count ++;
                    }
                }
                if(count > 1){
                    System.out.println("error from getCollapsableChildren");
                }

            }
        }
        return result;
    }
}
