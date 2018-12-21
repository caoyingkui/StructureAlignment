package cn.edu.pku.sei.structureAlignment.alignment;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.feature.CreateClassFeature;
import cn.edu.pku.sei.structureAlignment.feature.KeyWordFeature;
import cn.edu.pku.sei.structureAlignment.feature.MethodInvocationFeature;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.MatchedNode;
import cn.edu.pku.sei.structureAlignment.tree.Node;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import cn.edu.pku.sei.structureAlignment.util.DoubleValue;
import cn.edu.pku.sei.structureAlignment.util.LSTM;
import cn.edu.pku.sei.structureAlignment.util.Matrix;
import cn.edu.pku.sei.structureAlignment.util.WN;
import edu.stanford.nlp.simple.Sentence;
import javafx.util.Pair;

import java.util.*;

/**
 * Created by oliver on 2018/6/28.
 */
public class SimilarityMatrix {
    public String codeString;
    public List<CodeStructureTree> codeTrees;
    public List<String> comments;
    public List<TextStructureTree> textTrees;
    public Matrix<DoubleValue> matrix;
    public Matrix<DoubleValue> sliceMatrix;

    public Map<String , Integer> codeTf ;
    public Map<String , Integer> textTf;

    public SimilarityMatrix(String code, List<String> comments){
        parseCodeString(code);
        parseComments(comments);
        if(LSTM.currentFile != null){
            this.matrix = new Matrix<>(LSTM.m, LSTM.n , new DoubleValue(0));
            List<LSTM.SIM> pairs = LSTM.cases.get(LSTM.currentFile);
            for(LSTM.SIM pair : pairs){
                int row = pair.code;
                int col = pair.comment;
                double sim = pair.sim;
                this.matrix.setValue(row, col, sim);
            }
        }
    }

    private void parseCodeString(String code){
        this.codeString = code;
        CodeLineRelationGraph graph = new CodeLineRelationGraph();
        graph.build(code);
        codeTrees = graph.getCodeLineTrees();
        sliceMatrix = graph.slicesMatrix;
        codeTf = graph.tokenOccurFrequency;

        for(CodeStructureTree codeTree: codeTrees){
            codeTree.generate();
        }

        LSTM.m = codeTrees.size();
    }

    private void parseComments(List<String> comments){
        LSTM.n = comments.size();

        this.comments = comments;
        textTf = new HashMap<>();
        textTrees = new ArrayList<>();
        for(String comment : comments){
            TextStructureTree textTree = new TextStructureTree(0);
            textTree.construct(new Sentence(comment));
            textTrees.add(textTree);

            List<Node> nodes = textTree.getAllNodes();
            for(Node node : nodes){
                String content = node.getContent();
                textTf.put(content , textTf.getOrDefault(content , 0) + 1 );
            }
        }
    }




    public Matrix<DoubleValue> getMatrix(){
        String[] tokens = "".split(" ");
        String s = "";
        s.length();
        if(matrix != null)
            return matrix;
        else{
            calculateSimilarityMatrix();
            return matrix;
        }

    }


    private void calculateSimilarityMatrix(){


        tryToMatchLeafNode();
        tryToMatchNonleafNode();

        this.matrix = new Matrix<>(codeTrees.size() , textTrees.size() , new DoubleValue(0));

        for(int i = 0 ; i < textTrees.size() ; i ++){
            TextStructureTree textTree = textTrees.get(i);
            Map<Integer , List<MatchedNode>> alignments = getMatchedNode(textTree);

            for(Integer codeTreeNum : alignments.keySet()){
                Matrix<DoubleValue> matrixTemp = new Matrix<>(codeTrees.get(codeTreeNum).getEndIndex() + 1 , textTree.getEndIndex() + 1, new DoubleValue(0));

                List<MatchedNode> alignedNodes = alignments.get(codeTreeNum);
                for(MatchedNode matchedNode : alignedNodes){

                    double m1=0, n1=0;
                    for(MatchedNode node: matchedNode.codeNode.matchedCodeNodeList){
                        n1 ++;
                        if(node.textTreeID == i)
                            m1 ++;
                    }

                    double m2= 0, n2=0;
                    for(MatchedNode node: matchedNode.textNode.matchedCodeNodeList){
                        n2 ++;
                        if(node.codeTreeID == codeTreeNum)
                            m2 ++;
                    }
                    double sim = 0.5 * matchedNode.similarity * (m1 / n1 + m2 / n2);

                    /*double sim = matchedNode.similarity /
                            (matchedNode.codeNode.matchedCodeNodeList.size() + matchedNode.textNode.matchedCodeNodeList.size());*/


                    //String log = "  " + matchedNode.codeNode.getContent() + " " + matchedNode.textNode.getContent() + " : " + matchedNode.similarity + " " + sim + "\n";
                    //String log = matchedNode.logInfo + "  ---" + (matchedNode.codeNode.matchedCodeNodeList.size() + matchedNode.textNode.matchedCodeNodeList.size());
                    String log = matchedNode.logInfo + "  --- 0.5 * " + matchedNode.similarity + " * ( " + m1 + "/" + n1 + "+" + m2 + "/" + n2 + ")";
                    matrixTemp.setValue(matchedNode.codeNode.getId() , matchedNode.textNode.getId(), sim);
                    matrixTemp.setLogInfo(matchedNode.codeNode.getId() , matchedNode.textNode.getId(), log );

                }


                Map<Pair<Integer , Integer> , Double> matchedNodes = new HashMap<>();
                double sim = matrixTemp.similarity(matchedNodes);
                String log = "";
                if(sim > 0){
                    for(Pair<Integer , Integer> pair : matchedNodes.keySet()){
                        log += "\t" + matrixTemp.getLogInfo(pair.getKey(), pair.getValue()) + "\n";
                    }
                }

                double  featureSim = 0;
                CreateClassFeature feature = new CreateClassFeature();
                if(feature.getFeature(textTree)) {
                    featureSim = feature.match(codeTrees.get(codeTreeNum));
                    if(featureSim > 0){
                        sim += featureSim;
                        log += "  CreateClassFeature:" + featureSim + "\n";
                    }
                }

                KeyWordFeature keyWordFeature = new KeyWordFeature();
                if(keyWordFeature.getFeature(textTree)) {
                    featureSim = keyWordFeature.match(codeTrees.get(codeTreeNum));
                    if(featureSim > 0){
                        sim += featureSim;
                        log += "  KeyWordFeature:" + featureSim + "\n";
                    }

                }

                MethodInvocationFeature methodInvocationFeature = new MethodInvocationFeature();
                if(methodInvocationFeature.getFeature(textTree)) {
                    featureSim = methodInvocationFeature.match(codeTrees.get(codeTreeNum));
                    if(featureSim > 0){
                        sim += featureSim;
                        log += "  MethodInvocationFeature:" + featureSim + "\n";
                    }

                }

                log = " " + ( codeTreeNum + 1 ) + " " + ( i + 1 ) + " : "+ sim + "\n" + log;
                this.matrix.setValue(codeTreeNum , i , sim);
                this.matrix.setLogInfo(codeTreeNum , i , log);
            }
        }

        /*if(LSTM.currentFile != null){
            //this.matrix = new Matrix<>(LSTM.m, LSTM.n , new DoubleValue(0));
            List<LSTM.SIM> pairs = LSTM.cases.get(LSTM.currentFile);
            for(LSTM.SIM pair : pairs){
                int row = pair.code;
                int col = pair.comment;
                double sim = pair.sim + this.matrix.getValue(row, col);
                this.matrix.setValue(row, col, sim);
            }
        }*/
    }


    private void matchNodes(int codeTreeID, int textTreeID, List<Node> codeLeafNodes, List<Node> textLeafNodes){
        if(codeTreeID == 1 && textTreeID == 1){
            int i = 0;
        }
        for(Node textNode : textLeafNodes){
            for(Node codeNode : codeLeafNodes){
                if(codeNode.isPunctuation())
                    continue;
                double compareResult = NodeComparator.compare(codeNode, textNode);

                if(compareResult > 0 ){ //&& compareResult >= textNode.maxSimilarity){
                    MatchedNode newNode = new MatchedNode(codeTreeID , codeNode , textTreeID , textNode, compareResult);
                    newNode.logInfo = codeNode.getContent() + " " + textNode.getContent() + " : " + compareResult + " " + codeNode.getId() + " " + textNode.getId() ;



                    textNode.addMatchedNode(newNode);
                    codeNode.addMatchedNode(newNode);
                }
            }
        }
    }

    private void tryToMatchLeafNode( ){

        //WN.extend(codeTrees , textTrees);

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
                matchNodes(codeLine , textLine , codeLeafNodes , textLeafNodes);
            }
        }

    }

    private void tryToMatchNonleafNode(){

        int textTreeID , textTreeCount = textTrees.size();
        for(textTreeID = 0 ; textTreeID < textTreeCount ; textTreeID ++){
            TextStructureTree tree = textTrees.get(textTreeID);
            tryToMatchNonleafNode(textTreeID, tree);
        }


        for(TextStructureTree textTree : textTrees){
            firstBackPropagationForPruning(textTree , new HashSet<>());
        }

    }

    List<MatchedNode> tryToMatchNonleafNode(int textTreeID, TextStructureTree textTree){
        List<MatchedNode> result = new ArrayList<>();

        List<TextStructureTree> children = textTree.getChildren();

        if(children.size() == 0){
            for(MatchedNode node : textTree.root.matchedCodeNodeList){
                if(node.similarity > 1){
                    result.add(node);
                }
            }
            return result;
        }else{

            int treeOccurTimes[] = new int[codeTrees.size()];
            for(int i = 0; i < codeTrees.size() ; i ++)
                treeOccurTimes[i] = 0;

            Map<Integer, Set<MatchedNode>> nodesFromSameTree = new HashMap<>();

            for(TextStructureTree child : children){
                List<MatchedNode> matchedNodes = tryToMatchNonleafNode(textTreeID, child);

                //在当前child子树中出现了那些树的节点。
                Set<Integer> codeTreeNums = new HashSet<>();


                for(MatchedNode matchedNode : matchedNodes){
                    int codeTreeNum;
                    codeTreeNum = matchedNode.codeTreeID;
                    codeTreeNums.add(codeTreeNum);
                    if(! nodesFromSameTree.containsKey(codeTreeNum)){
                        Set<MatchedNode> nodeSet = new HashSet<>();
                        nodeSet.add(matchedNode);
                        nodesFromSameTree.put(codeTreeNum, nodeSet);
                    }else{
                        nodesFromSameTree.get(codeTreeNum).add(matchedNode);
                    }
                }

                for(int codeTreeNum : codeTreeNums)
                    treeOccurTimes[codeTreeNum] ++;
            }

            Pair<Map<Integer, Integer> , Double> mergeNodes = getTextMergeNode(nodesFromSameTree, treeOccurTimes);

            if(mergeNodes != null){
                Map<Integer, Integer> nodes = mergeNodes.getKey();
                double sim = mergeNodes.getValue();
                Node textNode = textTree.root;

                for(int codeTreeID : nodes.keySet()){
                    int codeNodeID = nodes.get(codeTreeID);
                    Node codeNode = codeTrees.get(codeTreeID).getNode(codeNodeID);
                    MatchedNode newNode = new MatchedNode(codeTreeID , codeNode , textTreeID, textNode , sim);
                    String logInfo = "";
                    for(MatchedNode subNode : nodesFromSameTree.get(codeTreeID)){
                        logInfo += " (" + subNode.logInfo + ")";
                    }
                    logInfo += (" : " + sim );

                    newNode.logInfo = logInfo;
                    textNode.addMatchedNode(newNode);
                    codeNode.addMatchedNode(newNode);

                    result.add(newNode);
                }

                for(Integer codeTreeNum : nodesFromSameTree.keySet()){
                    //codeTreeNum这棵树的节点以及被merge,所以来自这个树的节点就不要了。
                    if(nodes.containsKey(codeTreeNum))
                        continue;
                    for(MatchedNode matchedNode: nodesFromSameTree.get(codeTreeNum))
                        result.add(matchedNode);
                }

                return result;
            }else{
                for(int codeTreeNum : nodesFromSameTree.keySet()){
                    result.addAll(nodesFromSameTree.get(codeTreeNum));
                }
                return result;
            }


            /*List<List<MatchedNode>> matchedNodesFromChildren = new ArrayList<>();

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
            }*/
        }


    }

    Pair<Map<Integer, Integer>, Double> getTextMergeNode(Map<Integer, Set<MatchedNode>> nodes, int[] treeOccurTimes){
        Map<Integer, Integer> mergeNodes = new HashMap<>();
        double max = 0;
        double sim = 0;
        for(int codeTreeID : nodes.keySet()){
            if(treeOccurTimes[codeTreeID] < 2)
                continue;

            sim = 0;
            Set<MatchedNode> matchedNodes = nodes.get(codeTreeID);
            if(matchedNodes.size() < 2)
                continue;

            Set<Integer> matchedNodeIDs = new HashSet<>();
            for(MatchedNode matchedNode : matchedNodes){
                matchedNodeIDs.add(matchedNode.codeNode.getId());
                if(matchedNode.similarity > sim)
                    sim = matchedNode.similarity;
            }
            sim += (0.2 * matchedNodes.size());
            int mergeNodeID = codeTrees.get(codeTreeID).findCommonParents(matchedNodeIDs);

            if(max <= sim){
                if(max < sim) {
                    max = sim;
                    mergeNodes = new HashMap<>();
                }
                mergeNodes.put(codeTreeID, mergeNodeID);
            }
        }

        return mergeNodes.size() > 0 ? new Pair<>(mergeNodes , max) : null;
    }

    void firstBackPropagationForPruning(TextStructureTree textTree ,Set<Integer> parentHasBeenMatchedToTheseTrees){
        if(parentHasBeenMatchedToTheseTrees.size() > 0){
            Iterator<MatchedNode> iterator = textTree.root.matchedCodeNodeList.iterator();
            while(iterator.hasNext()){
                //去除了一个！
                if(parentHasBeenMatchedToTheseTrees.contains(iterator.next().codeTreeID)){
                    iterator.remove();
                }
            }
        }


        List<TextStructureTree> children = textTree.getChildren();
        if(children.size() > 0){
            for(TextStructureTree child : children) {
                Set<Integer> treeSet = new HashSet<>();
                treeSet.addAll(parentHasBeenMatchedToTheseTrees);
                for (MatchedNode matchedNode :textTree.root.matchedCodeNodeList ){
                    treeSet.add(matchedNode.codeTreeID);
                }
                firstBackPropagationForPruning(child, treeSet);
            }
        }
    }



    public Map<Integer , List<MatchedNode>> getMatchedNode(TextStructureTree textTree){
        Map<Integer , List<MatchedNode>> result = new HashMap<>();
        List<MatchedNode> matchedNodeList = textTree.root.matchedCodeNodeList;

        if(matchedNodeList.size() > 0){
            for(MatchedNode node : matchedNodeList){
                int codeTreeID = node.codeTreeID;
                if(result.containsKey(codeTreeID))
                    result.get(codeTreeID).add(node);
                else{
                    List<MatchedNode> matchedNodes = new ArrayList< >();
                    matchedNodes.add(node);
                    result.put(codeTreeID, matchedNodes);
                }

            }
        }

        List<TextStructureTree> children = textTree.getChildren();
        for(TextStructureTree child : children){
            Map<Integer , List< MatchedNode>> temp = getMatchedNode(child);
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
}
