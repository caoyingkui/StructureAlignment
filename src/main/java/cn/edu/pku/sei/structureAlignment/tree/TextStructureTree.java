package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2017/12/23.
 */
public class TextStructureTree extends cn.edu.pku.sei.structureAlignment.tree.Tree<TextStructureTree>{
    private static int id;
    public List<Dependency> dependencies;

    // maxSimilarity用于在匹配节点时，如果maxSimilarity的值为1，就说明以前存在有完全匹配成功节点，那么部分相似的节点将不会加入matchedCodeNodeList
    // 如果maxSimilarity的值小于1，说明前面匹配的节点都是部分相似的节点，如果匹配到完全相似的节点，那就把以前的匹配成功的节点全部清零。
    // node中有maxSimilarity
    // public double maxSimilarity = 0.0;

    // matchedCodeNodeList 是用与记录当前节点的匹配历史
    //map中的key，表示当前节点和k值指向的树存在相似或相同的节点，value存储的是list，表示树中匹配成功的节点编号
    public Map<Integer , List<Integer>> matchedCodeNodeList ;
    public static void main(String[] args){



        //NLParser parser = new NLParser("I have set rowCacheSize to 1000 (with your higher value, it took way too long)");
        //NLParser parser = new NLParser("I have set rowCacheSize to 1000");

        NLParser parser = new NLParser("The first argument to the QueryParser constructor is the default search field");
        edu.stanford.nlp.trees.Tree tree = parser.getNLTree();
        tree.pennPrint();
        TextStructureTree structTree = new TextStructureTree(0);
        structTree.construct(new Sentence("you won't get the destination"));

        structTree.print();
        /*JFrame frame = new JFrame();
        Printer printer = new Printer(structTree.getTree(8));
        printer.setBackground(Color.white);
        frame.add(printer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1200);
        frame.setVisible(true);*/


    }

    public TextStructureTree(int id){
        this.id = id;
        children = new ArrayList<TextStructureTree>();
        matchedCodeNodeList = new HashMap<>();
    }

    private TextStructureTree(){
        children = new ArrayList<TextStructureTree>();
        matchedCodeNodeList = new HashMap<>();
    }

    public synchronized void construct(Sentence sentence){

        construct(sentence.parse() , null);

        List<Integer> leafNodeIndexInTextTree = new ArrayList<>();
        List<Node> leafNodes = getAllLeafNodes();

        // the tree's order will be the same as the sentence's tokens' order
        for(Node node : leafNodes){
            leafNodeIndexInTextTree.add(node.id);
        }

        List<Dependency> dependencyList = new ArrayList<>();
        List<SemanticGraphEdge> edges = sentence.dependencyGraph().edgeListSorted();
        for(SemanticGraphEdge edge : edges){

            Dependency dependency = new Dependency(edge);


            int source = edge.getSource().index();
            int target = edge.getTarget().index();
            dependency.target.id = leafNodeIndexInTextTree.get(target - 1);
            dependency.source.id = leafNodeIndexInTextTree.get(source - 1);
            dependencyList.add(dependency);
        }

        updateDependencyInfo(this , dependencyList);
    }

    private void updateDependencyInfo(TextStructureTree textTree , List<Dependency> dependencyList){
        int startIndex = textTree.getStartIndex();
        int endIndex = textTree.getEndIndex();

        if(textTree.children.size() == 0) return;

        List<Dependency> subDependencyList = new ArrayList<>();
        for(Dependency dependency : dependencyList){
            int targetIndex = dependency.source.id;
            int sourceIndex = dependency.target.id;

            if(targetIndex >= startIndex && targetIndex <= endIndex &&
                    sourceIndex >= startIndex && sourceIndex <= endIndex){
                subDependencyList.add(dependency);
            }
        }
        textTree.dependencies = subDependencyList;

        for(Tree child : textTree.children){
            updateDependencyInfo((TextStructureTree) child , dependencyList);
        }
    }

    private void construct(edu.stanford.nlp.trees.Tree tree , TextStructureTree parent) {
        this.parent = parent;

        String rootLabel = tree.label().toString();
        NodeType type = getNodeType(rootLabel);

        if(tree.children().length == 0){
            String content = tree.nodeString().trim();
            root = new Node(type , content , id ++);
            root.setDisplayContent(rootLabel);
            startIndex = endIndex = root.getId();

        }else{
            int r_id = id ++;
            int e_id = 0; // endIndex
            for(edu.stanford.nlp.trees.Tree child : tree.children()){
                TextStructureTree temp = new TextStructureTree();
                temp.construct(child , this);
                children.add(temp);
                e_id = temp.getEndIndex();
            }
            String content = combineContentOfChildNode();
            root = new Node(type , content , r_id);
            root.setDisplayContent(rootLabel);
            startIndex = r_id;
            endIndex = e_id;
        }
    }

    public String getContent(){
        return this.root.getContent();
    }

    public String combineContentOfChildNode(){
        String result = "";
        for(cn.edu.pku.sei.structureAlignment.tree.Tree child : this.children){
            result += child.getContent() + "  ";
        }
        return result.trim();
    }


    public static NodeType getNodeType(String text){
        return NodeType.NULL;
    }

    public String getDisplayContent(){
        //return root.getDisplayContent();
        return root.getId() + ": " + root.getDisplayContent();
    }

    public ArrayList<TextStructureTree> findAllVP(){
        ArrayList<TextStructureTree> result = new ArrayList<TextStructureTree>();

        for(Tree child : children){
            ArrayList<TextStructureTree> temp = ((TextStructureTree)child).findAllVP();
            if(temp.size() >0 ){
                result.addAll(temp);
            }
        }
        if(root.getDisplayContent().compareTo("VP") == 0){
            result.add(this);
        }
        return result;
    }

    public ArrayList<TextStructureTree> findAllVerb(){
        ArrayList<TextStructureTree> result = new ArrayList<>();
        String type = root.getDisplayContent();
        if(type.compareTo("VB") == 0 ||
                type.compareTo("VBD") == 0 ||
                type.compareTo("VBG") == 0 ||
                type.compareTo("VBN") == 0 ||
                type.compareTo("VBP") == 0 ||
                type.compareTo("VBZ") == 0 ){
            result.add((TextStructureTree) children.get(0));
        }else if(children.size() > 0){
            int size = children.size();
            for(int i = 0 ; i < size ; i ++){
                TextStructureTree c = (TextStructureTree) children.get(i);
                result.addAll(c.findAllVerb());
            }
        }
        return result;
    }

    public ArrayList<String> findAllNoun(){
        ArrayList<String> result = new ArrayList<>();
        String type = root.getDisplayContent().trim();
        if(type.compareTo("NN") == 0 ||
                type.compareTo("NNS") == 0 ||
                type.compareTo("NNP") == 0 ||
                type.compareTo("NNPS") == 0){
            result.add(children.get(0).root.getContent().trim());
        }else if(children.size() > 0){
            int size = children.size();
            for(int i = 0 ; i < size ; i++){
                TextStructureTree c = (TextStructureTree)children.get(i);
                result.addAll(c.findAllNoun());
            }
        }
        return result;
    }


    public List<Dependency> getDependency(String longName){
        String rootString = root.getContent();
        List<Dependency> result = new ArrayList<>();
        if(children.size() > 0) return null;
        else{
            for(Dependency dependency : dependencies){
                if(dependency.getRelation().compareTo(longName) == 0){
                    result.add(dependency);
                }
            }
        }
        return result;
    }

}
