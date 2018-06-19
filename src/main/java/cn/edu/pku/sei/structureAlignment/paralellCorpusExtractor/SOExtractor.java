package cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.Main;
import cn.edu.pku.sei.structureAlignment.feature.CreateClassFeature;
import cn.edu.pku.sei.structureAlignment.feature.KeyWordFeature;
import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.*;
import cn.edu.pku.sei.structureAlignment.util.*;
import edu.stanford.nlp.simple.Sentence;
import javafx.util.Pair;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import javax.xml.soap.Text;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by oliver on 2018/3/14.
 */
public class SOExtractor {

    private SqlConnector connSelector;
    private SqlConnector connInserter;
    private ASTParser parser;
    private int pairs = 0;
    private static int testID = 12;
    private static int parallelCount = 0;
    private static int postsCount = 0;

    public static void main(String[] args) {
        SOExtractor extractor = new SOExtractor();
        //extractor.selectPosts();
        extractor.parseSOPosts();
        System.out.println("parallel:" + parallelCount);
        System.out.println("posts:" + postsCount);
        //extractor.extractParallelCorpus();

        /*List<String> sentences = new ArrayList<>();
        sentences.add(" I think you need to add each query to the BooleanQuery like below.");
        extractor.match(sentences , null);*/

        /*try {
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\oliver\\Desktop\\数据\\stackoverflow\\379345.txt"));
            String line = "";
            String code = "";
            while((line = reader.readLine()).trim().length() != 0){
                code += (line + "\n");
            }

            CodeLineRelationGraph graph = new CodeLineRelationGraph();
            graph.build(code);

            CamelCaseDictionary dictionary = new CamelCaseDictionary(graph);
            List<String> comments = new ArrayList<>();

            while((line = reader.readLine()) != null){
                comments.add(dictionary.mergeTokenByCamelCase(line));
            }

            List<TextStructureTree> textTrees = new ArrayList<>();
            for(String comment : comments){
                TextStructureTree textTree = new TextStructureTree(0);
                textTree.construct(new Sentence(comment));
                textTrees.add(textTree);

            }
            extractor.match(graph.getCodeLineTrees() , textTrees);
            System.out.println("end.");
        }catch (Exception e){
             e.printStackTrace();
        }*/



        //extractor.outputResult(graph.getCodeLineTrees() , textTrees);

    }

    public SOExtractor(){
        parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_STATEMENTS);
    }

    private void selectPosts(){
        connInserter = new SqlConnector("jdbc:mysql://127.0.0.1:3306/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        connSelector = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        SqlConnector connForLookup = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver"
                );
        connForLookup.start();
        connForLookup.setPreparedStatement("select Title from lucene_question where Id = ?");

        connInserter.start();
        connSelector.start();

        connInserter.setPreparedStatement("delete from textCode");
        connInserter.execute();

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_STATEMENTS);



        int limit = 1000;
        int step = 1000;
        int id = -1;
        while(true){
            String sql_select = "select * from ( select * from lucene_answer order by Id asc limit " + limit + " ) a order by Id desc limit " + step;
            //sql_select = "select * from lucene_answer where Id = 1123981";
            String sql_insert = "insert into textCode (id , post , question , text , code) values (1, ? , ? , ?  , ?)";
            try {
                connSelector.setPreparedStatement(sql_select);
                connInserter.setPreparedStatement(sql_insert);
                ResultSet rs = connSelector.executeQuery();
                if (rs.next()) {
                    int temp = rs.getInt("Id");
                    if(id == temp)
                        break;
                    else
                        id = temp;

                    do{
                        String body = rs.getString("body");
                        int parentId = rs.getInt("ParentId");

                        int post = rs.getInt("Id");
                        List<Pair<String , String>> text_codes = extractCodeAndText(body);
                        if(text_codes != null) {

                            connForLookup.setInt(1 , parentId);
                            ResultSet parent = connForLookup.executeQuery();
                            String questionTitle = "";
                            if(parent.next()){
                                questionTitle = parent.getString("Title");
                            }

                            for (Pair<String, String> text_code : text_codes) {
                                String text = text_code.getKey();
                                String code = text_code.getValue();

                                connInserter.setInt(1 , post);
                                connInserter.setString(2 , questionTitle);
                                connInserter.setString(3 , text);
                                connInserter.setString(4 , code);
                                //connInserter.setString(3 , "<pre><code>" + code + "</code></pre>");
                                connInserter.execute();
                            }
                        }
                    }while(rs.next());
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            limit += 1000;
        }

        connInserter.close();
        connSelector.close();
    }

    private void parseSOPosts(){
        connSelector = new SqlConnector("jdbc:mysql://127.0.0.1:3306/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        connSelector.start();

        String sql = "select post , text , code from textCode";
        connSelector.setPreparedStatement(sql);
        ResultSet rs = connSelector.executeQuery();
        try {
            while (rs.next()) {
                postsCount ++;
                int post = rs.getInt(1);
                if(post == 1827116) {
                    int a = 1;
                }
                String text = Stemmer.filterHtmlTags(rs.getString(2));
                //String text = rs.getString(2);
                String code = rs.getString(3);

                System.out.println("***********************************");
                System.out.println(post);
                parseSOPosts(post , text , code);
                System.out.println();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            connSelector.close();
        }
    }

    private void parseSOPosts(int post , String text , String code){
        List<String> sentences = Stemmer.string2sentence(text);

        //String[] sentences = text.split("\\.");
        List<TextStructureTree> textTrees = new ArrayList<>();
        for(String sentence : sentences){
            if(sentence.length() < 30)
                continue;

            TextStructureTree textTree = new TextStructureTree(0);
            textTree.construct(new Sentence(sentence));
            textTrees.add(textTree);
        }

        CodeLineRelationGraph graph = new CodeLineRelationGraph();
        graph.build(code);


        List<CodeStructureTree> codeTree = graph.getCodeLineTrees();

        //匹配
        //match( codeTree , textTrees);

        //将结果存入数据库
        storeResult(post , code , sentences , codeTree , textTrees );

    }

    private void storeResult(int post , String code , List<String> sentences , List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){
        SqlConnector conn = new SqlConnector("jdbc:mysql://127.0.0.1:3306/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");

        conn.start();
        conn.setPreparedStatement("select Body from lucene_answer where id = " + post);
        ResultSet rs = conn.executeQuery();
        String postBody = "";
        try {
            if (rs.next())
                postBody = rs.getString(1);
        }catch (Exception e){
            e.printStackTrace();
        }

        String text = "";
        for(String sentence : sentences){
            text += ("<p>" + sentence + "<p>");
        }
        conn.setPreparedStatement("insert into SOParallel (id , postId , post , code , sentences , pair) values (0 , ? , ? , ? , ? , ?)");

        for(int i = 0 ; i < textTrees.size(); i ++ ){
            TextStructureTree textTree = textTrees.get(i);
            List<TextStructureTree> nonLeafTrees = textTree.getAllNonleafTrees();
            for(TextStructureTree nonLeafTree : nonLeafTrees){
                List<MatchedNode> matchedCodeNodeList = nonLeafTree.root.matchedCodeNodeList;
                if(matchedCodeNodeList != null && matchedCodeNodeList.size() > 0){
                    //System.out.println(i + " " + nonLeafTree.getId() + " " + nonLeafTree.getContent());
                    String pair = "<p>" + nonLeafTree.getContent() + "</p>";
                    pair += "<pre><code>";
                    for(MatchedNode matchedNode : matchedCodeNodeList){
                        pair +=  codeTrees.get(matchedNode.matchedTreeID).getTree(matchedNode.matchedNodeID).getCode() + "\n";
                    }
                    pair += "</code></pre>";

                    conn.setInt(1 , post);
                    conn.setString(2 , postBody);
                    conn.setString(3 , code );
                    conn.setString(4 , text);
                    conn.setString(5 , pair);
                    conn.execute();
                }
            }
        }

        conn.close();
    }

    private List<Pair<String , String>> extractCodeAndText(String postBody){
        Pattern pattern = Pattern.compile("<code>([\\s\\S]*?)</code>");
        Matcher matcher  = pattern.matcher(postBody);

        List<String> codeSnippets = new ArrayList<>();

        while(matcher.find()){
            String code = matcher.group(1);
            codeSnippets.add(code);
        }

        List<Pair<String , String> > result = new ArrayList<>();
        if(codeSnippets.size() == 1){
            for (String snippet : codeSnippets) {
                if(snippet.length() > 50)
                    postBody = postBody.replace("<code>" + snippet + "</code>", " ");
            }


            postBody = postBody.replaceAll("<pre>" , " ");
            postBody = postBody.replaceAll("</pre>" , " ");
            postBody = postBody.replaceAll("<p>" , " ");
            postBody = postBody.replaceAll("</p>" , " ");

            postBody = postBody.replace("<code>" , " ");
            postBody = postBody.replace("</code>" , "");

            for(String snippet : codeSnippets){
                if(snippet.length() > 50 ) {
                    parser.setSource(snippet.toCharArray());
                    parser.setKind(ASTParser.K_STATEMENTS);
                    Block block = (Block) parser.createAST(null);
                    if (block.statements().size() > 0)// && block.statements().size() < 10 &&
                            //postBody.length() * 1.0 / snippet.length() <= 10)
                        result.add(new Pair<String, String>(postBody, snippet));
                }
            }
        }
        if(result.size() > 0) return result;
        else return null;
    }

    private void extractParallelCorpus(){
        connSelector = new SqlConnector("jdbc:mysql://127.0.0.1:3306/lucene",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");
        connSelector.start();
        connSelector.setPreparedStatement("select * from textCode");
        //connSelector.setPreparedStatement("select * from textCode where post = 8664210");
        ResultSet rs = connSelector.executeQuery();
        try {
            while (rs.next()) {
                System.out.flush();
                System.out.println(rs.getString("post") + ":");

                String code = rs.getString("code");
                CodeLineRelationGraph graph = new CodeLineRelationGraph();
                try {
                    graph.build(code);
                }catch (Exception e){
                    continue;
                }

                List<CodeStructureTree > codeTrees = graph.getCodeLineTrees();
                if(codeTrees.size() == 0)
                    continue;


                String commentString = rs.getString("text");
                commentString = commentString.replaceAll("\\n" , " ").trim();

                List<String> comments = new ArrayList<>(Arrays.asList(commentString.split("(\\.|\\?|!) " )));

                Iterator<String> commentIterator = comments.iterator();
                while(commentIterator.hasNext()){
                    String next = commentIterator.next();
                    if(next.trim().length() == 0)
                        commentIterator.remove();
                }

                if(comments.size() == 0)
                    continue;

                /*BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
                iterator.setText(commentString);
                int start = iterator.first();
                for (int end = iterator.next();
                     end != BreakIterator.DONE;
                     start = end, end = iterator.next()) {
                    comments.add(commentString.substring(start,end));
                }*/

                List<Pair<Integer ,Integer>> matchScheme = Main.match(graph , comments);



                int formerText = -1;
                String codeString;

                Map<Integer , String> textLine_code = new HashMap<>();
                for(Pair<Integer ,Integer> pair : matchScheme){
                    int codeLine = pair.getKey();
                    int textLine = pair.getValue();

                    if(!textLine_code.containsKey(textLine)){
                        textLine_code.put(textLine , codeTrees.get(codeLine).getCode());
                    }else{
                        textLine_code.put(textLine , textLine_code.get(textLine) + codeTrees.get(codeLine).getCode());
                    }
                }

                for(int textLine : textLine_code.keySet()){
                    pairs ++;
                    System.out.println(comments.get(textLine).trim());
                    System.out.println(textLine_code.get((Object)textLine));
                }
                System.out.flush();
                System.out.println("\n**********************************\n");

            }

            System.out.println("total:" + pairs);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void match(List<String> sentences , CodeLineRelationGraph codeGraph){

        CamelCaseDictionary dictionary = new CamelCaseDictionary(codeGraph);
        List<TextStructureTree> textTrees = new ArrayList<>();
        for(String sentence : sentences){
            TextStructureTree textTree = new TextStructureTree(0);
            textTree.construct(new Sentence(
                    dictionary.mergeTokenByCamelCase(sentence)
            ));
        }

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };

        List<String> codeTokens = new ArrayList<String>();
        List<String> textTokens = new ArrayList<String>();

        Map<String , List<Pair<Integer , Integer>>> codeTokenMap = new TreeMap<>(comparator);
        Map<String , List<Pair<Integer , Integer>>> textTokenMap = new TreeMap<>(comparator);

        List<CodeStructureTree> codeTrees = codeGraph.getCodeLineTrees();
        int codeTreeCount = codeTrees.size();
        for(int i = 0 ; i < codeTreeCount ; i ++){
            CodeStructureTree codeTree = codeTrees.get(i);
            List<Node> leafNodes = codeTree.getAllLeafNodes();
            for(Node leafNode : leafNodes){
                int type = leafNode.getType().ordinal();
                if(type >= NodeType.CODE_ASSIGNMENT_OPERATOR.ordinal()&& type <= NodeType.NULL.ordinal()){
                    continue;
                }
                String content = leafNode.getContent();

                if(codeTokens.contains(content)){
                    codeTokenMap.get(content).add(new Pair<Integer , Integer>(i , leafNode.getId()));
                }else{
                    codeTokens.add(content);

                    List<Pair<Integer , Integer>> pairs = new ArrayList<>();
                    pairs.add(new Pair<Integer , Integer>(i , leafNode.getId()));
                    codeTokenMap.put(content , pairs);
                }
            }
        }

        int textTreeCount = textTrees.size();
        for(int i = 0 ; i < textTreeCount ; i ++){
            TextStructureTree textTree = textTrees.get(i);
            List<Node> leafNodes = textTree.getAllLeafNodes();
            for(Node leafNode : leafNodes){
                String content = leafNode.getContent();
                if(textTokens.contains(content)){
                    textTokenMap.get(content).add(new Pair<Integer ,Integer>(i , leafNode.getId()));
                }else{
                    textTokens.add(content);

                    List<Pair<Integer , Integer>> pairs = new ArrayList<>();
                    pairs.add(new Pair<Integer ,Integer>(i , leafNode.getId()));
                    textTokenMap.put(content , pairs);
                }
            }
        }

        Collections.sort(codeTokens);
        Collections.sort(textTokens);

        List<Pair<Integer , Integer>> matched_Code_Text = new ArrayList<>();
        boolean signal = false;
        int codeIndex , startIndex , textIndex , codeTokenCount = codeTokens.size() , textTokenCount = textTokens.size();
        for(codeIndex = 0 , startIndex = 0 ; codeIndex < codeTokenCount ; codeIndex ++){

            for(textIndex = startIndex ; textIndex < textTokenCount ; textIndex ++){
                int comparedResult = codeTokens.get(codeIndex).compareTo(textTokens.get(textIndex));
                if(comparedResult == 0){
                    String token = codeTokens.get(codeIndex);

                    List<Pair<Integer , Integer>> codeTokenNodes = codeTokenMap.get(token);
                    List<Pair<Integer , Integer>> textTokenNodes = textTokenMap.get(token);
                    //说明匹配上的token在代码和文本中均只出现一次，所以对应的代码和文本一定是匹配成功的。
                    if(codeTokenNodes.size() + textTokenNodes.size() == 2){
                        matched_Code_Text.add(new Pair<>(
                           codeTokenNodes.get(0).getKey(),  // 匹配上的代码行编号
                           textTokenNodes.get(0).getKey()   // 匹配上的文本行编号
                        ));
                    }

                    startIndex = textIndex + 1;
                    break;
                }else if(comparedResult < 0){
                    startIndex = textIndex;
                    break;
                }else if(comparedResult > 0){
                    continue;
                }
            }
        }


    }

    public Matrix<DoubleValue>  match(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees , Map<String , Integer> tokenOccurFrequency){

        tryToMatchLeafNode(codeTrees , textTrees);

        //outputResult( textTrees , codeTrees);

        tryToMatchNonleafNode(codeTrees, textTrees);


        Matrix<DoubleValue> result = new Matrix<>(codeTrees.size() , textTrees.size() , new DoubleValue(0));

        for(int i = 0 ; i < textTrees.size() ; i ++){
            TextStructureTree textTree = textTrees.get(i);
            Map<Integer , List<Pair<Integer , MatchedNode>>> alignments = getAlignmentResult(textTree);

            for(Integer codeTreeNum : alignments.keySet()){
                Matrix<DoubleValue> matrix = new Matrix<>(codeTrees.get(codeTreeNum).getEndIndex() + 1 , textTree.getEndIndex() + 1, new DoubleValue(0));

                List<Pair<Integer , MatchedNode>> alignedNodes = alignments.get(codeTreeNum);
                for(Pair<Integer , MatchedNode> pair : alignedNodes){
                    int textNodeNum = pair.getKey();
                    MatchedNode node = pair.getValue();
                    /*if(codeTrees.get(node.matchedTreeID).getTree(node.matchedNodeID).getChildren().size() != 0)
                        continue;*/


                    double factor = tokenOccurFrequency.containsKey(node.matchedNode.getContent()) ?
                            2.0 / tokenOccurFrequency.get(node.matchedNode.getContent() ): 2.0;

                    double sim = node.similarity;
                    if(sim >= 1)
                        sim *= factor;



                    matrix.setValue(node.matchedNodeID , textNodeNum , sim);
                }

                Map<Pair<Integer , Integer> , Double> matchedNodes = new HashMap<>();
                double sim = matrix.similarity(matchedNodes);
                /*if(sim  > 0){
                    System.out.println(codeTreeNum + "  " + i + ":");
                    CodeStructureTree codeTree = codeTrees.get(codeTreeNum);
                    for(Pair<Integer , Integer> pair : matchedNodes.keySet()){
                        System.out.println(codeTree.getNode(pair.getKey()).getContent() + "  " + textTree.getNode(pair.getValue()).getContent() + " : " + matchedNodes.get(pair));
                    }
                }*/

                CreateClassFeature feature = new CreateClassFeature();
                if(feature.getFeature(textTree))
                    sim += feature.match(codeTrees.get(codeTreeNum));

                KeyWordFeature keyWordFeature = new KeyWordFeature();
                if(keyWordFeature.getFeature(textTree))
                    sim += keyWordFeature.match(codeTrees.get(codeTreeNum));

                result.setValue(codeTreeNum , i , sim);
            }
        }

        return result;
    }

    public Matrix<DoubleValue> match(CodeLineRelationGraph codeGraph , List<String> sentences ){

        Map<String , Integer> tokenOccurFrequency = codeGraph.tokenOccurFrequency;
        List<TextStructureTree> textTrees = new ArrayList<>();
        for(String sentence : sentences){
            TextStructureTree textTree = new TextStructureTree(0);
            textTree.construct(new Sentence(sentence));
            textTrees.add(textTree);
        }

        return match(codeGraph.getCodeLineTrees() , textTrees , tokenOccurFrequency);
    }

    public Map<Integer , List<Pair<Integer , MatchedNode>>> getAlignmentResult(TextStructureTree textTree){
        Map<Integer , List<Pair<Integer , MatchedNode>>> result = new HashMap<>();
        List<MatchedNode> matchedNodeList = textTree.root.matchedCodeNodeList;

        if(matchedNodeList.size() > 0){
            for(MatchedNode node : matchedNodeList){
                int codeTreeNum = node.matchedTreeID;
                if(result.containsKey(codeTreeNum)){
                    result.get(codeTreeNum).add(new Pair(textTree.root.getId() , node));
                }else{
                    List<Pair<Integer , MatchedNode>> nodes = new ArrayList<>();
                    nodes.add(new Pair(textTree.root.getId() , node));
                    result.put(codeTreeNum , nodes);
                }
            }
        }

        List<TextStructureTree> children = textTree.getChildren();
        for(TextStructureTree child : children){
            Map<Integer , List<Pair<Integer , MatchedNode>>> temp = getAlignmentResult(child);
            for(Integer codeTreeNum : temp.keySet()){
                if(result.containsKey(codeTreeNum)){
                    result.get(codeTreeNum).addAll(temp.get(codeTreeNum));
                }else{
                    result.put(codeTreeNum , temp.get(codeTreeNum));
                }
            }
        }
        return result;
    }

    void tryToMatchLeafNode(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){
        WN.extend(codeTrees , textTrees);
        List<List<Node>> codeTreeLeafNodes = new ArrayList<>();
        for(CodeStructureTree codeTree : codeTrees){
            codeTreeLeafNodes.add(codeTree.getAllLeafNodes());
        }

        List<List<Node>> textTreeLeafNodes = new ArrayList<>();
        for(TextStructureTree textTree : textTrees){
            textTreeLeafNodes.add(textTree.getAllLeafNodes());
        }

        int codeLine = 0 , codeLineCount = codeTrees.size();
        int textLine = 0 , textLineCount = textTrees.size();

        for(textLine = 0 ; textLine < textLineCount ; textLine ++){
            List<Node> textLeafNodes = textTreeLeafNodes.get(textLine);
            for(codeLine = 0 ; codeLine < codeLineCount ; codeLine ++){
                List<Node> codeLeafNodes = codeTreeLeafNodes.get(codeLine);

                for(Node textNode : textLeafNodes){
                    if(textNode.isStopWord() )
                        continue;
                    for(Node codeNode : codeLeafNodes){
                        if(codeNode.isPunctuation())
                            continue;
                        double compareResult = codeNode.compare(textNode);

                        if(compareResult > 0 ){ //&& compareResult >= textNode.maxSimilarity){
                            MatchedNode newNode = new MatchedNode(codeLine , codeNode.getId() , codeNode , compareResult);
                            //addMatchedNode函数会同时更新 code的叶子节点和text的叶子节点，所以只需要textNode调用该函数就可以了
                            textNode.addMatchedNode(newNode , textLine);
                        }
                    }
                }
            }
        }

    }

    void tryToMatchNonleafNode(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){

        int textTreeNum , textTreeCount = textTrees.size();
        for(textTreeNum = 0 ; textTreeNum < textTreeCount ; textTreeNum ++){
            TextStructureTree tree = textTrees.get(textTreeNum);
            tryToMatchNonleafNode(codeTrees , tree , textTreeNum);
        }

        //System.out.println("\n\n\n\nbefore propagation!");
        //outputResult(textTrees , codeTrees);

        for(TextStructureTree textTree : textTrees){
            firstBackPropagationForPruning(textTree , new HashSet<>());
        }

    }

    List<MatchedNode> tryToMatchNonleafNode(List<CodeStructureTree> codeTrees , TextStructureTree textTree , int thisTreeNum){

        List<MatchedNode> result = new ArrayList<>();


        if(testID == textTree.getId()){
            int breakPoint = 0;
        }

        List<TextStructureTree> children = textTree.getChildren();

        if(children.size() == 0){
            for(MatchedNode node : textTree.root.matchedCodeNodeList){
                if(node.similarity >= 1){
                    result.add(node);
                }
            }

            return result;
        }else{
            List<List<MatchedNode>> matchedNodesFromChildren = new ArrayList<>();

            for(TextStructureTree child : children){
                List<MatchedNode> matchedNodes = tryToMatchNonleafNode(codeTrees , child , thisTreeNum);
                if(matchedNodes != null && matchedNodes.size() > 0){
                    matchedNodesFromChildren.add(matchedNodes);
                }
            }

            if(matchedNodesFromChildren.size() > 1)
                result = textTree.root.merge(matchedNodesFromChildren , thisTreeNum , codeTrees);

            if(result != null && result.size() > 0)
                return result;
            else{
                result = new ArrayList<>();
                for(List<MatchedNode> matchedNodesFromOneChild : matchedNodesFromChildren){
                    result.addAll(matchedNodesFromOneChild);
                }

                return result.size() > 0 ? result : null;
            }
        }


    }

    List<MatchedNode> pushUp(TextStructureTree textTree){
        if(testID == textTree.getId()){
            int breakpoint = 2;
        }

        List<MatchedNode> result = new ArrayList<>();
        List<TextStructureTree> children = textTree.getChildren();
        List<MatchedNode> matchedCodeNodeList = textTree.root.matchedCodeNodeList;
        if(children.size() > 0){
            if(matchedCodeNodeList.size() > 0) {//当前节点是被merge成功的节点，不能被子树信息更新
                return matchedCodeNodeList;
            }else{
                int sourceCount = 0;
                for(TextStructureTree child : children){
                    List<MatchedNode> temp = pushUp(child);
                    if(temp.size() > 0) {
                        sourceCount++;
                        result.addAll(pushUp(child));
                    }
                }

                if(sourceCount == 1){
                    for(MatchedNode node : result) {
                        textTree.root.addMatchedNode(new MatchedNode(node.matchedTreeID, node.matchedNodeID, node.matchedNode, node.similarity));
                    }
                    return result;
                }else{
                    if(result.size() > 0)
                        result.clear();
                    return result;
                }

            }
        }else{
            return textTree.root.matchedCodeNodeList;
        }
    }

    void firstBackPropagationForPruning(TextStructureTree textTree ,Set<Integer> parentHasBeenMatchedToTheseTrees){

        if(testID == textTree.getId()){
            int breakpoint = 2;
        }

        List<TextStructureTree> children = textTree.getChildren();
        if(children.size() > 0){
            for(TextStructureTree child : children){
                if(textTree.root.matchedCodeNodeList.size() > 0) {
                    Set<Integer> treeSet = new HashSet<>();
                    for(MatchedNode matchedNode : textTree.root.matchedCodeNodeList){
                        treeSet.add(matchedNode.matchedTreeID);
                    }
                    firstBackPropagationForPruning(child, treeSet);
                }
                else
                    firstBackPropagationForPruning(child , parentHasBeenMatchedToTheseTrees);
            }
        }else{
            if(parentHasBeenMatchedToTheseTrees.size() > 0){
                 Iterator<MatchedNode> iterator = textTree.root.matchedCodeNodeList.iterator();
                 while(iterator.hasNext()){
                     //去除了一个！
                     if(parentHasBeenMatchedToTheseTrees.contains(iterator.next().matchedTreeID)){
                         iterator.remove();
                     }
                 }
            }else{
                //textTree.root.matchedCodeNodeList.clear();
            }
        }
    }

    void secondBackPropagationFor(TextStructureTree textTree , List<MatchedNode> parentHasBeenMatchedToTheseNodes){
        if(testID == textTree.getId()){
            int breakpoint = 2;
        }

        if(isTreeNodeAreMatchedToSameNodeLists(parentHasBeenMatchedToTheseNodes , textTree.root.matchedCodeNodeList))
            textTree.root.matchedCodeNodeList.clear();

        List<TextStructureTree> children = textTree.getChildren();
        if(children.size() > 0){
            for(TextStructureTree child : children){
                secondBackPropagationFor(child ,
                        textTree.root.matchedCodeNodeList.size() == 0 ? parentHasBeenMatchedToTheseNodes : textTree.root.matchedCodeNodeList);
            }
        }

    }

    boolean isTreeNodeAreMatchedToSameNodeLists(List<MatchedNode> list1 , List<MatchedNode> list2){
        Comparator<MatchedNode> comparator = new Comparator<MatchedNode>() {
            @Override
            public int compare(MatchedNode o1, MatchedNode o2) {
                int result = o1.matchedTreeID - o2.matchedTreeID;
                if(result == 0)
                    return o1.matchedNodeID - o2.matchedNodeID;
                else
                    return result;
            }
        };

        list1.sort(comparator);
        list2.sort(comparator);

        int size = list1.size();
        if(size == list2.size()){
            for(int i = 0 ; i < size ; i++){
                if(comparator.compare(list1.get(i) , list2.get(i)) != 0)
                    return false;
            }
            return true;
        }else
            return false;

    }

    void outputResult(List<TextStructureTree> textTrees , List<CodeStructureTree> codeTrees){

        for(int i = 0 ; i < textTrees.size(); i ++ ){
            TextStructureTree textTree = textTrees.get(i);
            List<TextStructureTree> nonLeafTrees = textTree.getAllTrees();
            for(TextStructureTree nonLeafTree : nonLeafTrees){
                List<MatchedNode> matchedCodeNodeList = nonLeafTree.root.matchedCodeNodeList;
                if(matchedCodeNodeList != null && matchedCodeNodeList.size() > 0){
                    System.out.println(i + " " + nonLeafTree.getId() + " " + nonLeafTree.getContent());
                    for(MatchedNode matchedNode : matchedCodeNodeList){
                        parallelCount ++;
                        System.out.println("        " + matchedNode.matchedTreeID + " " + matchedNode.matchedNodeID + " " +  codeTrees.get(matchedNode.matchedTreeID).getTree(matchedNode.matchedNodeID).getCode() +  " : " + matchedNode.similarity);
                    }
                    System.out.println("***************************************");
                }


            }
        }
    }
}
