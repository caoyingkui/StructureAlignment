package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2017/12/23.
 */
public class TextStructureTree extends cn.edu.pku.sei.structureAlignment.tree.Tree<TextStructureTree>{
    private static int id;
    private List<SemanticGraphEdge> dependencies;

    public static void main(String[] args){



        //NLParser parser = new NLParser("I have set rowCacheSize to 1000 (with your higher value, it took way too long)");
        //NLParser parser = new NLParser("I have set rowCacheSize to 1000");

        NLParser parser = new NLParser("Finally assign the font instance to CellStyle instance");
        edu.stanford.nlp.trees.Tree tree = parser.getNLTree();
        tree.pennPrint();
        TextStructureTree structTree = new TextStructureTree(0);
        structTree.construct(new Sentence("Here, the filter constrains document searches to only documents owned by “jake”."));

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
        children = new ArrayList<Tree>();
    }

    private TextStructureTree(){
        children = new ArrayList<Tree>();
    }

    public synchronized void construct(Sentence sentence){

        construct(sentence.parse() , null);

        List<TextStructureTree> leafs = new ArrayList<>();
        Map<Integer , Node> leafNodes = getAllLeafNodes();
        // the tree's order will be the same as the sentence's tokens' order

        for(int i = 0 ; i <= endIndex ; i ++){
            if(leafNodes.containsKey(i))
                leafs.add((TextStructureTree) getTree(i));
        }

        List<SemanticGraphEdge> edges = sentence.dependencyGraph().edgeListSorted();
        for(SemanticGraphEdge edge : edges){
            int source = edge.getSource().index();
            int target = edge.getTarget().index();
            leafs.get(source - 1).addDependency(edge);
            leafs.get(target - 1).addDependency(edge);
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

    public void addDependency(SemanticGraphEdge dependency){
        if(dependencies == null){
            dependencies = new ArrayList<>();
        }

        dependencies.add(dependency);
    }

    public List<String> getDependency(String relationName){
        String rootString = root.getContent();
        List<String> result = new ArrayList<>();
        if(children.size() > 0) return null;
        else{
            for(SemanticGraphEdge edge : dependencies){
                if(edge.getRelation().getLongName().compareTo(relationName) == 0){
                    String sourceString = edge.getSource().word();
                    String targetString = edge.getTarget().word();

                    //两个中，总有一个是root结点的内容。
                    if(sourceString.compareTo(rootString) != 0){
                        result.add(sourceString);
                    }else{
                        result.add(targetString);
                    }
                }
            }
        }

        return result;
    }


}
