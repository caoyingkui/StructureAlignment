package cn.edu.pku.sei.structureAlignment.paralellCorpusExtractor;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.Main;
import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.*;
import cn.edu.pku.sei.structureAlignment.util.CamelCaseDictionary;
import cn.edu.pku.sei.structureAlignment.util.StopWordList;
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

    SqlConnector connSelector;
    SqlConnector connInserter;
    ASTParser parser;
    int pairs = 0;
    static int testID = 50;

    public static void main(String[] args) {
        SOExtractor extractor = new SOExtractor();
        //extractor.selectPosts();
        //extractor.extractParallelCorpus();

        /*List<String> sentences = new ArrayList<>();
        sentences.add(" I think you need to add each query to the BooleanQuery like below.");
        extractor.match(sentences , null);*/

        try {
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
            extractor.outputResult(graph.getCodeLineTrees() , textTrees);
            System.out.println("end.");
        }catch (Exception e){
            e.printStackTrace();
        }



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

    private List<Pair<String , String>> extractCodeAndText(String postBody){
        Pattern pattern = Pattern.compile("<code>([\\s\\S]*?)</code>");
        Matcher matcher  = pattern.matcher(postBody);

        List<String> codeSnippets = new ArrayList<>();

        while(matcher.find()){
            String code = matcher.group(1);
            codeSnippets.add(code);
        }

        List<Pair<String , String> > result = new ArrayList<>();
        if(codeSnippets.size() > 0){
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
                if(snippet.length() > 50) {
                    parser.setSource(snippet.toCharArray());
                    parser.setKind(ASTParser.K_STATEMENTS);
                    Block block = (Block) parser.createAST(null);
                    if (block.statements().size() > 0)
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

    void match(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){

        tryToMatchLeafNode(codeTrees , textTrees);

        outputResult(codeTrees , textTrees);

        tryToMatchNonleafNode(codeTrees, textTrees);
    }

    void tryToMatchLeafNode(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){
        List<List<Node>> codeTreeLeafNodes = new ArrayList<>();
        for(CodeStructureTree codeTree : codeTrees){
            codeTreeLeafNodes.add(codeTree.getAllLeafNodes());
        }

        List<List<TextStructureTree>> textTreeLeafTrees = new ArrayList<>();
        for(TextStructureTree textTree : textTrees){
            textTreeLeafTrees.add(textTree.getAllLeafTrees());
        }

        int codeLine = 0 , codeLineCount = codeTrees.size();

        for(List<TextStructureTree> textTreeLeafTreeList : textTreeLeafTrees){
            for(codeLine = 0 ; codeLine < codeLineCount ; codeLine ++){
                List<Node> codeTreeLeafNode = codeTreeLeafNodes.get(codeLine);

                for(TextStructureTree textLeafTree : textTreeLeafTreeList){
                    Node textNode = textLeafTree.root;
                    if(StopWordList.contains(textNode.getContent()))
                        continue;

                    for(Node codeNode : codeTreeLeafNode){
                        double comparedResult = codeNode.compare(textNode);

                        if(codeNode.maxSimilarity < comparedResult)
                            codeNode.maxSimilarity = comparedResult;

                        // 只有相似度比以前高，才能更新结点匹配的信息
                        if(comparedResult > 0 && comparedResult >= textLeafTree.root.maxSimilarity){
                            if(comparedResult == 1 && textLeafTree.root.maxSimilarity < 1 )
                                textLeafTree.matchedCodeNodeList.clear();

                            List<Integer> matchedList = textLeafTree.matchedCodeNodeList.getOrDefault((Object)codeLine , new ArrayList<Integer>());
                            matchedList.add(codeNode.getId());

                            if(!textLeafTree.matchedCodeNodeList.containsKey(codeLine)){
                                textLeafTree.matchedCodeNodeList.put(codeLine , matchedList);
                            }
                            textLeafTree.root.maxSimilarity = comparedResult;
                        }
                    }
                }
            }
        }
    }

    void tryToMatchNonleafNode(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){
        //tryToMatchNonleafNode(codeTrees , textTrees.get(2));
        for(TextStructureTree textTree : textTrees){
            tryToMatchNonleafNode(codeTrees , textTree);
            //backPropagation(textTree , new HashMap<>());
        }
        //System.out.println("asdfasdfasdfasdfasdfa");
        //outputResult(codeTrees, textTrees);

        for(TextStructureTree textTree : textTrees){
            firstBackPropagationForPruning(textTree , textTree.matchedCodeNodeList.keySet());
        }
        //System.out.println("first");
        //outputResult(codeTrees, textTrees);

        for(TextStructureTree textTree : textTrees){
            pushUp(textTree);
        }
        //System.out.println("pushUp");
        //outputResult(codeTrees, textTrees);

        for(TextStructureTree textTree : textTrees){
            secondBackPropagationFor(textTree, new HashMap<>());
        }
       // System.out.println("second");
        outputResult(codeTrees, textTrees);



    }

    Set<Pair<Integer , Integer>> tryToMatchNonleafNode(List<CodeStructureTree> codeTrees , TextStructureTree textTree){


        if(textTree.root.getId() == testID){
            int breakPoint = 0;
        }


        Set<Pair<Integer ,Integer>> result = new HashSet<>();
        if(textTree.getChildren().size() == 0){
            double maxSimilarity = textTree.root.maxSimilarity;
            Map<Integer ,List<Integer>> matchedCodeNodeList = textTree.matchedCodeNodeList;

            Iterator<Map.Entry<Integer , List<Integer>>> codeLineIterator = matchedCodeNodeList.entrySet().iterator();
            while(codeLineIterator.hasNext()){
                Map.Entry<Integer ,List<Integer>> entry = codeLineIterator.next();
                int codeLine = entry.getKey();
                List<Integer> nodeList = entry.getValue();
                Iterator<Integer> it = nodeList.iterator();
                while(it.hasNext()){
                    int nodeNum = it.next();
                    if(codeTrees.get(codeLine).getTree(nodeNum).root.maxSimilarity > maxSimilarity){
                        it.remove();
                    }else {
                        result.add(new Pair<>(codeLine, nodeNum));
                    }
                }
                if(nodeList.size() == 0)
                    codeLineIterator.remove();
            }
            return result;
        }else{

            List<TextStructureTree> children = textTree.getChildren();

            Set<Pair<Integer , Integer>> tempResult = new HashSet<>();
            Map<Integer , Set<Integer>> subNodesInSameTree = new HashMap<>();

            // subTreeCount 中的key-value,代表的是
            //从textTree的子节点匹配成功了第key棵代码树的部分节点，value记录的是这些子树的棵树
            //主要是为了当一个子树传递了多个同一个棵代码树的多个子节点，但这种情况下不能够进行merge
            //因为，非叶子节点在向上传递值时，不同传递同一个树的多个节点，因为这个时候会进行merge,并且把merge之后的节点网上传
            //但是如果从一棵子树获取了同一棵代码树的多个节点，就说明，是从叶子节点匹配上来的，也就是说，这个文本树的叶子节点，匹配成功了多个代码节点，例如单词IndexWriter
            //所以这个时候，不要进行merge。所以用subTreeCount去记录
            Map<Integer , Integer> subTreeCount = new HashMap<>();


            for(TextStructureTree child : children){
                Set<Integer> hasMetTheTree = new HashSet<>();
                Set<Pair<Integer , Integer>> temp = tryToMatchNonleafNode(codeTrees , child);
                tempResult.addAll(temp);
                for(Pair<Integer , Integer> pair :temp ) {
                    int codeLine = pair.getKey();
                    if(!hasMetTheTree.contains(codeLine)){
                        hasMetTheTree.add(codeLine);
                        subTreeCount.put(codeLine , subTreeCount.getOrDefault(codeLine , 0) + 1);
                    }
                }
            }


            int codeLine , nodeNum;
            for(Pair<Integer , Integer> codeNode : tempResult){
                codeLine = codeNode.getKey();
                nodeNum = codeNode.getValue();

                Set<Integer> tempSet = subNodesInSameTree.getOrDefault(codeLine , new HashSet<Integer>());
                tempSet.add(nodeNum);
                if(!subNodesInSameTree.containsKey(codeLine)){
                    subNodesInSameTree.put(codeLine , tempSet);
                }
            }

            Set<Integer> hasMergedTree = new HashSet<>();
            for(Integer codeLineNum : subNodesInSameTree.keySet()){
                Set<Integer> tempSet = subNodesInSameTree.get(codeLineNum);
                if(tempSet.size() > 1 && subTreeCount.get(codeLineNum) > 1){
                    hasMergedTree.add(codeLineNum);
                    result.add(new Pair<>(
                       codeLineNum , codeTrees.get(codeLineNum).findCommonParents(tempSet)
                    ));
                }
            }

            // 这说明 子节点中，有可以匹配成功的点 ， 这个结点的matchedCodeList将会被更新为这些结点
            if(result.size() > 0){
                textTree.matchedCodeNodeList.clear();
                for(Pair<Integer ,Integer> pair : result){
                    List<Integer> matchedCodeNodeList = new ArrayList<>() ;
                    matchedCodeNodeList.add(pair.getValue());
                    textTree.matchedCodeNodeList.put(pair.getKey() , matchedCodeNodeList);
                }
            }else{
                ;
                /*textTree.matchedCodeNodeList.clear();
                for(Pair<Integer , Integer> pair : tempResult){
                    codeLine = pair.getKey();
                    nodeNum = pair.getValue();

                    List<Integer> matchedCodeNodeList = textTree.matchedCodeNodeList.getOrDefault(codeLine , new ArrayList<Integer>());
                    matchedCodeNodeList.add(nodeNum);
                    if(!textTree.matchedCodeNodeList.containsKey(codeLine)){
                        textTree.matchedCodeNodeList.put(codeLine , matchedCodeNodeList);
                    }
                }*/
            }

            //在返回的结果中，一部分是匹配的结点，就是之前这行代码之前result中的内容，
            //而另一部分应该还包含那些尚未匹配成功的结点，也就是tempResult的中，尚未成功匹配的结点
            //例如，在tempResult中有{<1 , 5>,<1 ,9> , <3 , 2>}
            //<1 , 5> 和<1,9>肯定会被merge为一个新结点例如：<1 , 3> ，也就是说当前result的中内容是{<1 , 3>}
            //而<3,2>也应该包含到这个结点中去，这样的话就可以说明，这棵树的某个结点应该存在匹配1树，但同时还存在疑似和3树匹配的结点。
            for(Pair<Integer ,Integer> codeNode : tempResult){
                if(!hasMergedTree.contains((Object)(codeNode.getKey()))){
                    result.add(codeNode);
                }
            }

            if(textTree.root.getId() == testID){
                int breakPoint = 0;
            }
            return result;

        }
    }

    Set<Pair<Integer , Integer>> pushUp(TextStructureTree textTree){
        if(testID == textTree.getId()){
            int breakpoint = 2;
        }

        Set<Pair<Integer , Integer>> result = new HashSet<>();
        List<TextStructureTree> children = textTree.getChildren();
        Map<Integer , List<Integer>> matchedCodeNodeList = textTree.matchedCodeNodeList;
        if(children.size() > 0){
            for(Tree child : children){
                result.addAll(pushUp((TextStructureTree) child));
            }

            if(matchedCodeNodeList.size() > 0){//当前节点是被merge成功的节点
                result.clear();
                for(int codeLine : matchedCodeNodeList.keySet()){
                    List<Integer> nodes = matchedCodeNodeList.get(codeLine);
                    if(nodes.size() == 1){
                        result.add(new Pair<>(codeLine , nodes.get(0)));
                    }else{
                        System.out.println("error from pushUp");
                    }
                }
            }else{
                if(result.size() > 1)
                    result.clear();
                else if (result.size() == 1){
                    for(Pair<Integer , Integer> pair : result){
                        List<Integer> list = new ArrayList<>();
                        list.add(pair.getValue());
                        textTree.matchedCodeNodeList.put(pair.getKey() , list);
                    }
                }
            }
        }else{
            ;
            /*for(int codeLine : matchedCodeNodeList.keySet()){
                List<Integer> nodes = matchedCodeNodeList.get(codeLine);
                for(int node : nodes){
                    result.add(new Pair<>(codeLine , node));
                }
            }*/
        }

        if(testID == textTree.getId()){
            int breakpoint = 2;
        }
        return result;
    }



    void firstBackPropagationForPruning(TextStructureTree textTree ,Set<Integer> parentHasBeenMatchedToTheseTrees){

        if(testID == textTree.getId()){
            int breakpoint = 2;
        }

        List<TextStructureTree> children = textTree.getChildren();
        if(children.size() > 0){
            for(TextStructureTree child : children){
                if(textTree.matchedCodeNodeList.size() > 0)
                    firstBackPropagationForPruning(child , textTree.matchedCodeNodeList.keySet());
                else
                    firstBackPropagationForPruning(child , parentHasBeenMatchedToTheseTrees);
            }
        }else{
            if(parentHasBeenMatchedToTheseTrees.size() > 0){
                 Iterator<Map.Entry<Integer , List<Integer>>> iterator = textTree.matchedCodeNodeList.entrySet().iterator();
                 while(iterator.hasNext()){
                     Map.Entry<Integer , List<Integer>> entry = iterator.next();
                     if(! parentHasBeenMatchedToTheseTrees.contains(entry.getKey())){
                         iterator.remove();
                     }
                 }
            }else{
                textTree.matchedCodeNodeList.clear();
            }
        }

        if(testID == textTree.getId()){
            int breakpoint = 2;
        }
    }

    void secondBackPropagationFor(TextStructureTree textTree , Map<Integer , List<Integer>> parentHasBeenMatchedToTheseNodes){

        if(isTreeNodeAreMatchedToSameNodeLists(parentHasBeenMatchedToTheseNodes , textTree.matchedCodeNodeList))
            textTree.matchedCodeNodeList.clear();

        List<TextStructureTree> children = textTree.getChildren();
        if(children.size() > 0){
            for(TextStructureTree child : children){
                secondBackPropagationFor(child ,
                        textTree.matchedCodeNodeList.size() == 0 ? parentHasBeenMatchedToTheseNodes : textTree.matchedCodeNodeList);
            }
        }

    }

    boolean isTreeNodeAreMatchedToSameNodeLists(Map<Integer , List<Integer>> list1 , Map<Integer , List<Integer>> list2){
        Set<Integer> codeLines1  = list1.keySet();
        Set<Integer> codeLines2  = list2.keySet();
        if(codeLines1.size() != codeLines2.size())
            return false;
        else{
            for(int codeLine1 : codeLines1){
                List<Integer> nodes2 = list2.getOrDefault(codeLine1 , null);
                if(nodes2 == null)
                    return false;
                else{
                    List<Integer> nodes1 = list1.get(codeLine1);
                    int sameCount = 0;
                    for(int node1 : nodes1){
                        if(nodes2.contains((Object)node1))
                            sameCount ++;
                    }
                    if(sameCount != nodes1.size())
                        return false;
                }
            }
        }


        return true;
    }

    void backPropagation(TextStructureTree textTree , Map<Integer , Integer> parentHasBeenMatchedToTheseTrees){
        Iterator<Map.Entry<Integer , List<Integer>>> iterator = textTree.matchedCodeNodeList.entrySet().iterator();

        List<TextStructureTree> children = textTree.getChildren();
        // 叶子节点
        if(children.size() == 0){

            while(iterator.hasNext()){
                if(!parentHasBeenMatchedToTheseTrees.containsKey( iterator.next().getKey() )){
                    iterator.remove();
                }
            }
        }else{ // 非叶子节点
            Map temp = new HashMap<Integer , Integer>();
            temp.putAll(parentHasBeenMatchedToTheseTrees);

            while(iterator.hasNext()){
                Map.Entry<Integer , List<Integer>> entry = iterator.next();
                if(parentHasBeenMatchedToTheseTrees.containsKey(entry.getKey()) &&
                        entry.getValue().get(0) == parentHasBeenMatchedToTheseTrees.get(entry.getValue())){
                    iterator.remove();
                }else{
                    temp.put(entry.getKey() , entry.getValue());
                }
            }

            for(Tree child : children){
                backPropagation((TextStructureTree)child , temp);
            }
        }
    }

    void outputResult(List<CodeStructureTree> codeTrees , List<TextStructureTree> textTrees){
        for(TextStructureTree textTree : textTrees){
            List<TextStructureTree> nonLeafTrees = textTree.getAllNonleafTrees();
            for(TextStructureTree nonLeafTree : nonLeafTrees){
                Map<Integer , List<Integer>> matchedCodeNodeList = nonLeafTree.matchedCodeNodeList;
                if(matchedCodeNodeList.size() > 0){
                    System.out.println(nonLeafTree.getContent());
                    System.out.println();
                    for(Integer codeLine : matchedCodeNodeList.keySet()){
                        List<Integer> matchedCodeNode = matchedCodeNodeList.get((Object) codeLine);
                        for(int nodeNum : matchedCodeNode){
                            System.out.println(((CodeStructureTree)codeTrees.get(codeLine).getTree(nodeNum)).getCode());
                        }
                        System.out.println();
                    }

                    System.out.println("***************************************");
                }


            }
        }


        for(int i = 0 ; i < textTrees.size() ; i++){
            List<TextStructureTree> allTrees = textTrees.get(i).getAllNonleafTrees();
            for(TextStructureTree tree : allTrees){
                Map<Integer ,List<Integer>> matchedCodeList = tree.matchedCodeNodeList;
                if(matchedCodeList.size() > 0){
                    System.out.print(i + " " + tree.root.getId() + " : " );
                    for(int codeLine : matchedCodeList.keySet()){
                        System.out.print(" | " + codeLine + " | ");
                        for(int node : matchedCodeList.get(codeLine)){
                            System.out.print(node + " ");
                        }
                    }
                    System.out.println();
                }

            }
        }


    }
}
