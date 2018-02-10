import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.feature.Feature;
import cn.edu.pku.sei.structureAlignment.parser.code.CodeVisitor;
import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.*;
import cn.edu.pku.sei.structureAlignment.util.DoubleValue;
import cn.edu.pku.sei.structureAlignment.util.Matrix;
import cn.edu.pku.sei.structureAlignment.util.SimilarPair;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import edu.stanford.nlp.simple.Sentence;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

import javafx.util.Pair;

import javax.xml.soap.Text;

/**
 * Created by oliver on 2017/12/25.
 */
public class Main {
    static int globalTotal = 0;
    static int globalRight = 0;
    static int globalWrong = 0;

    public static void main(String[] args) throws IOException {

        //match(new File("C:\\Users\\oliver\\Desktop\\数据\\no control sentence\\10.txt"));

        File d = new File("C:\\Users\\oliver\\Desktop\\数据\\no control sentence");
        File[] files = d.listFiles();
        for(File file : files) {
            match(file);
        }
        double precision = (globalWrong + globalRight) == 0 ? 0 : (double) globalRight / (globalRight + globalWrong) ;
        double recall = globalTotal == 0 ? 1 : (double)globalRight / globalTotal;
        System.out.printf("total:%d right:%d wrong:%d precision:%.2f recall:%.2f\n\n\n", globalTotal , globalRight , globalWrong , precision , recall);
        System.out.printf("");



        /*File[] files = new File("codeSnippets").listFiles();

        List<CodeLineRelationGraph> graphs = new ArrayList<>();
        List<List<Feature>> features = new ArrayList<>();

        for(File file : files){
            String fileName = file.getName();
            fileName = fileName.substring(0 , fileName.length() - 4);
            //fileName = "Demonstrates the simplicity of searching using a TermQuery";
            CodeLineRelationGraph graph = new CodeLineRelationGraph();

            graph.build(file.getPath());
            //graph.build("E:\\Intellij workspace\\StructureAlignment\\codeSnippets\\Demonstrates the simplicity of searching using a TermQuery.txt");

            NLParser parser = new NLParser(fileName);

            graphs.add(graph);

            features.add(parser.getFeatures());
        }

        for(int i = 0 ; i < graphs.size() ; i ++){
            for(int j = 0 ; j < graphs.size() ; j ++){
                CodeLineRelationGraph graph = graphs.get(i);
                List<Feature> feature = features.get(j);


                System.out.print(i + "  " + j  + " : ");
                System.out.print(graph.match(feature) + "  |   ");

            }

            System.out.println();
        }


        /*
        CodeLineRelationGraph graph = new CodeLineRelationGraph();
        graph.build("codeSnippets\\PrefixQuery finds all documents containing a specified prefix.txt");

        NLParser parser = new NLParser("PrefixQuery finds all documents containing a specified prefix");

        List<Feature> features = parser.getFeature();

        int count = 0 ;
        for(Feature feature : features){
            if(feature.match(graph)){
                count ++;
            }
        }

        System.out.println(count);*

        //compare("code.txt" , "text.txt");
        /*List<CodeLineRelationGraph> graphs = new ArrayList<>();
        List<String> description = new ArrayList<>();
        File[] files = new File("codeSnippets").listFiles();
        for(File file : files){
            String path = file.getAbsolutePath();
            CodeLineRelationGraph graph = new CodeLineRelationGraph();
            graph.build(path);
            graphs.add(graph);

            String fileName = file.getName();
            fileName = fileName.substring(0 , fileName.length() - 4);
            description.add(fileName);
        }

        for(int i = 0 ; i < graphs.size() ; i ++){
            System.out.println(i + " :" + description.get(i));
            CodeLineRelationGraph graph = graphs.get(i);
            graph.paint();
            for(int j = 0 ; j < description.size() ; j ++){
                if(graph.compare(description.get(j)) == 1){
                    System.out.print(j + " ");
                }
            }
            System.out.println();
        }*/

    }

    public static void compare(String codePath , String textPath){
        ArrayList<CodeStructureTree> codeTrees = new ArrayList<CodeStructureTree>();
        ArrayList<TextStructureTree> textTrees = new ArrayList<TextStructureTree>();


        try{
            String line = "";

            CodeVisitor.initialize();
            BufferedReader reader = new BufferedReader(new FileReader(new File(codePath)));
            while((line = reader.readLine()) != null){
                ASTParser codeParser = ASTParser.newParser(AST.JLS8);
                codeParser.setKind(ASTParser.K_STATEMENTS);
                codeParser.setSource(line.toCharArray());
                Block block = (Block) codeParser.createAST(null);

                if(block != null || block.statements().size() == 0)
                    continue;
                CodeVisitor visitor = new CodeVisitor(0);
                ((ASTNode)(block.statements().get(0)) ).accept(visitor);
                CodeStructureTree tree = visitor.getTree();
                codeTrees.add(tree);
                //tree.print();
            }
            reader.close();


            line = "";
            NLParser textParser = null;
            reader = new BufferedReader(new FileReader(new File(textPath)));
            while((line = reader.readLine()) != null ) {
                textParser = new NLParser(line);
                TextStructureTree tree = textParser.getTextStructureTree();
                textTrees.add(tree);
            }

            Matrix<SimilarPair> matrix = new Matrix<>(codeTrees.size() , textTrees.size() , new SimilarPair(0 , 0));

            for(int i = 0 ; i < codeTrees.size() ; i ++){
                for(int j = 0 ; j < textTrees.size() ; j ++){
                    CodeStructureTree codeTree = codeTrees.get(i);
                    TextStructureTree textTree = textTrees.get(j);
                    SimilarPair pair = compare(codeTree , textTree);

                    matrix.setCell(i , j , pair);
                }
            }

            Pair<Integer , Integer> pair = null;
            do{
                //matrix.print(0);
                pair = matrix.getMax(0.5);
                if(pair != null){
                    int codeId = pair.getKey();
                    int textId = pair.getValue();
                    SimilarPair simiPair = matrix.getCell(codeId , textId);

                    matrix.cleanRow(codeId);
                    matrix.cleanColumn(textId);

                    CodeStructureTree scTree = (CodeStructureTree)codeTrees.get(codeId).getTree(simiPair.left);
                    TextStructureTree ssTree = (TextStructureTree)textTrees.get(textId).getTree(simiPair.right);

                    System.out.println(scTree.getCode().trim());
                    System.out.println(ssTree.getContent().trim() + "\n");


                }
            }while(pair != null);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static SimilarPair compare(CodeStructureTree codeTree , TextStructureTree textTree){

        double threshold = 0.5;

        int codeEndIndex = codeTree.getEndIndex();
        int textEndIndex = textTree.getEndIndex();
        Matrix<DoubleValue> similarMatrix = new Matrix(codeEndIndex + 1 , textEndIndex + 1 , new DoubleValue(-1));
        for(int i = 0 ; i <= codeEndIndex ; i ++)
            for(int j = 0 ; j <= textEndIndex ; j ++) {
                DoubleValue doubleValue = new DoubleValue(-1.0);
                similarMatrix.setCell(i, j, doubleValue);
            }

        findIdenticalPair(codeTree , textTree , similarMatrix);

        //similarMatrix.print(0);

        ArrayList<TextStructureTree> VPs = textTree.findAllVP();
        List<CodeStructureTree> codeNodes = null;

        for(TextStructureTree vpTree : VPs){
            Set<String> verbs = new HashSet<>();
            Set<String> directNouns = new HashSet<>();// 这部分名词，是在句法树中，明显能够分析得到的dobj关系的名词
            Set<String> normalNouns = new HashSet<>();// 这部分名词，就是出现在动词周围的名词，但是没有找到dobj关系

            //获取所有的动词，个人感觉一个VP中只会出现一个动词吧！？？
            List<TextStructureTree> verbNodes = vpTree.findAllVerb();
            for(TextStructureTree verbNode : verbNodes){
                verbs.addAll(Stemmer.stem(verbNode.getContent()));
                directNouns.addAll(Stemmer.stem(verbNode.getDependency("direct object")));
            }

            normalNouns.addAll(Stemmer.stem(vpTree.findAllNoun()));

            for(String verb : directNouns){
                if(normalNouns.contains(verb))
                    normalNouns.remove(verb);
            }

            codeNodes = codeTree.getAllNonleafTree();
            //codeNodes = codeTree.getSpecificTypeNode(NodeType.CODE_MethodInvocation);
            if(verbs.contains("creat")) {
                codeNodes.addAll( codeTree.getSpecificTypeNode(NodeType.CODE_ClassInstanceCreation) );
                verbs.add("new");
            }

            double base = 1;

            for(CodeStructureTree codeNode : codeNodes){
                if(codeNode.getChildrenSize() < 2)//和一个动宾短语进行匹配，至少需要两个子节点嘛
                    continue;
                List<String> tokens = Stemmer.stem(codeNode.getContent());

                int signal = 0;
                for(String verb : verbs){
                    for(String token : tokens){
                        if( twoWordsAreSame(verb , token) ){
                            base = 0.5;
                            signal ++;
                            break;
                        }
                    }
                    if(signal == 1){
                        break;
                    }
                }

                if(directNouns.size() != 0){
                    for(String noun : directNouns){
                        if(tokens.contains(noun)){
                            base += 0.3;
                            break;
                        }
                    }
                }

                if(normalNouns.size() != 0){
                    for(String noun : normalNouns){
                        if(tokens.contains(noun)){
                            base += 0.1;
                            break;
                        }
                    }
                }

                if(base >= 0.5) {
                    int textId = vpTree.getId();
                    int codeId = codeNode.getId();
                    double sim = Stemmer.compare(vpTree.getContent() , codeNode.getContent());

                    if(base == 0.5) // 只出现了一个动词
                        sim = base + 0.1 * sim;
                    else if(base == 0.6) // 出现了普通名词
                        sim = base + 0.2 * sim;
                    else if(base == 0.8) // 出现了关键名词
                        sim = base + 0.1 * sim;
                    else if(base == 0.9) // 出现了普通名词和关键名词
                        sim = base + 0.1 * sim;

                    similarMatrix.setValue(codeId , textId , sim);
                }

            }
        }

        //similarMatrix.print(0);

        boolean signal = false;

        double max = 0.5;
        int max_codeId = -1;
        int max_textId = -1;
        for(int codeId = codeEndIndex  ; codeId > -1 ; codeId --){
            for(int textId = textEndIndex ; textId > -1  ; textId --){
                double simTemp = similarMatrix.getValue(codeId , textId);
                if( simTemp > max){
                    signal = true;
                    max_codeId = codeId;
                    max_textId = textId;
                    max = simTemp;
                }
            }
        }

        if(signal) {
            SimilarPair similarPair = new SimilarPair(max_codeId , max_textId );
            similarPair.setValue(max);
            return similarPair;
        }else
            return null;

    }

    public static void findIdenticalPair(CodeStructureTree codeTree , TextStructureTree textTree , Matrix matrix){


        Map<Integer , Integer> similarPairs = findIdenticalNodes(codeTree , textTree);

        // group stores the code nodes which have been found some identical text node in the text tree.
        ArrayList<Integer> group = new ArrayList<Integer>();
        for(int key : similarPairs.keySet()){
            group.add(key);
        }

        // find the nodes' common parent node
        Map<Integer , List<Integer>> parent_children = codeTree.findCommonParents( group , 20);

        for(int parent : parent_children.keySet()){
            List<Integer> children = parent_children.get(parent);
            List<Integer> textChildren = new ArrayList<Integer>();

            for(int codeId : children){
                int textId = similarPairs.get(codeId);
                textChildren.add(textId);
                matrix.setValue(codeId , textId ,1);

            }

            int textParent = textTree.findCommonParents(textChildren);
            matrix.setValue(parent , textParent , 1);

            System.out.println(((CodeStructureTree)codeTree.getTree(parent)).getCode().trim());
            System.out.println(textTree.getTree(textParent).getContent().trim());
            System.out.println("   ");
        }

    }

    static Map<Integer , Integer> findIdenticalNodes(CodeStructureTree codeTree , TextStructureTree textTree){
        Map<Integer , Node> codeLeafNodes = codeTree.getAllLeafNodes();
        Map<Integer , Node> textLeafNodes = textTree.getAllLeafNodes();

        Map<Integer , Integer> similarPairs = new HashMap<Integer, Integer>();

        // this array is used to store the text node which have been recognized as identical with some code node
        ArrayList<Integer> textNodes = new ArrayList<>();
        for(int codeId : codeLeafNodes.keySet()){
            Node codeNode = codeLeafNodes.get(codeId);
            for(int textId : textLeafNodes.keySet()){
                Node textNode = textLeafNodes.get(textId);
                if(codeNode.compare(textNode) >= 0.5 && !textNodes.contains(textId)){
                    textNodes.add(textId);
                    similarPairs.put(codeId , textId);
                    break;
                }
            }
        }
        return similarPairs;
    }

    static double howWellAreTwoTreesAreSimilar(CodeStructureTree codeTree , TextStructureTree textTree){
        Map<Integer , Node> codeLeafNodes = codeTree.getAllLeafNodes();
        Map<Integer , Node> textLeafNodes = textTree.getAllLeafNodes();



        List<Node> codeNodes = new ArrayList<>();
        List<Node> textNodes = new ArrayList<>();

        for(Integer codeId : codeLeafNodes.keySet()){
            codeNodes.add(codeLeafNodes.get(codeId));
        }
        for(Integer textId : textLeafNodes.keySet()){
            textNodes.add(textLeafNodes.get(textId));
        }

        int codeLeafCount = codeLeafNodes.size();
        int textLeafCount = textLeafNodes.size();
        Matrix<DoubleValue> matrix = new Matrix<>(codeLeafCount , textLeafCount , new DoubleValue(0));
        for(int i = 0 ; i < codeLeafCount ; i ++){
            Node codeNode = codeNodes.get(i);
            for(int j = 0 ; j < textLeafCount ; j ++){
                Node textNode = textNodes.get(j);
                matrix.setValue(i , j , codeNode.compare(textNode));
            }
        }
        double result = 0;
        Pair<Integer , Integer> max;
        while((max = matrix.getMax(2)) != null){
            int codeId = max.getKey();
            int textId = max.getValue();
            result += 4;
            matrix.cleanRow(codeId);
            matrix.cleanColumn(textId);
        }

        while((max = matrix.getMax(1)) != null){
            int codeId = max.getKey();
            int textId = max.getValue();
            result += 2;
            matrix.cleanRow(codeId);
            matrix.cleanColumn(textId);
        }

        while((max = matrix.getMax(0.5)) != null){
            int codeId = max.getKey();
            int textId = max.getValue();
            result += 1;
            matrix.cleanRow(codeId);
            matrix.cleanColumn(textId);
        }

        return result;
    }

    static boolean twoWordsAreSame(String word1 , String word2){
        return word1.contains(word2)||word2.contains(word1);
    }




    /**
     * the file should be arranged as the following format:
     * codeLines
     *
     * comments
     *
     * results
     * @param file
     * @return
     */
    static double match(File file){

        System.out.println(file.getAbsolutePath() + ":");
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> codeLines = new ArrayList<>();
            String line;
            while((line = reader.readLine()).length() != 0){
                codeLines.add(line);
            }
            CodeLineRelationGraph graph = new CodeLineRelationGraph();
            graph.build(codeLines);
            List<CodeStructureTree> codeTrees = graph.getCodeLineTrees();


            List<String> comments = new ArrayList<>();
            List<TextStructureTree> textTrees = new ArrayList<>();
            while((line = reader.readLine()).length() != 0){
                comments.add(line);
            }
            for(String c : comments){
                TextStructureTree textTree = new TextStructureTree(0);
                textTree.construct( new Sentence( c));
                textTrees.add(textTree);
            }


            Map<Integer, Integer> annotations = new HashMap<>();
            int codeLineNum;
            int commentNum;
            while((line = reader.readLine()).compareTo("END") != 0 ){
                String[] nums = line.split(" ");
                commentNum = Integer.parseInt(nums[1]);
                for(String s : nums[0].split("\\|")){
                    codeLineNum = Integer.parseInt(s);
                    annotations.put(codeLineNum , commentNum);
                }


            }

            reader.close();

            int codeTreeCount = codeTrees.size();
            int textTreeCount = textTrees.size();

            Matrix<DoubleValue> matrix = new Matrix<>(codeTreeCount , textTreeCount , new DoubleValue(0));

            int right = 0;
            int wrong = 0;
            for(int i = 0 ; i < codeTreeCount ; i ++){
                CodeStructureTree codeTree = codeTrees.get(i);
                for(int j = 0 ; j < textTreeCount ; j ++){
                    TextStructureTree textTree = textTrees.get(j);
                    matrix.setValue(i , j , howWellAreTwoTreesAreSimilar(codeTree , textTree));
                }
            }
            matrix.print();

            int maxCode = 0;
            for(commentNum = 0 ; commentNum < textTreeCount ; commentNum ++){
                codeLineNum = matrix.getColumnMax(commentNum , maxCode , 2);

                //数据中代码和注释的编号是从1开始的。
                if(codeLineNum != -1){
                    if(annotations.containsKey(codeLineNum + 1) && annotations.get(codeLineNum + 1) == commentNum + 1){
                        right ++ ;
                        System.out.print("  right:");
                    }else{
                        wrong ++ ;
                        System.out.print("  wrong:");
                    }
                    System.out.println((codeLineNum + 1 )+ " " + ( commentNum + 1));
                    maxCode = codeLineNum + 1;
                }
            }

            //region <old>
            Pair<Integer , Integer> max;
            while((max = matrix.getMax(2)) != null){
                codeLineNum = max.getKey();
                commentNum = max.getValue();
                matrix.cleanRow(codeLineNum);
                matrix.cleanColumn(commentNum);

                //数据中代码和注释的编号是从1开始的。
                codeLineNum ++;
                commentNum ++ ;
                if(annotations.containsKey(codeLineNum) && annotations.get(codeLineNum) == commentNum){
                    right ++ ;
                    System.out.print("  right:");
                }else{
                    wrong ++ ;
                    System.out.print("  wrong:");
                }
                System.out.println(codeLineNum + " " + commentNum);

            }
            //endregion <old>

            int total = annotations.size();
            globalTotal += total;
            globalRight += right;
            globalWrong += wrong;
            double precision = (wrong + right) == 0 ? 0 : (double) right / (right + wrong) ;
            double recall = total == 0 ? 1 : (double)right / total;
            System.out.printf("total:%d right:%d wrong:%d precision:%.2f recall:%.2f\n\n\n", total , right , wrong , precision , recall);

        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }

}
