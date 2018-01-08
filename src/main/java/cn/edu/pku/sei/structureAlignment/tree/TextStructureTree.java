package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.Printer;
import cn.edu.pku.sei.structureAlignment.parser.NLParser;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.SentenceUtils.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by oliver on 2017/12/23.
 */
public class TextStructureTree extends cn.edu.pku.sei.structureAlignment.tree.Tree<TextStructureTree>{
    private static int id;

    public static void main(String[] args){
        //NLParser parser = new NLParser("I have set rowCacheSize to 1000 (with your higher value, it took way too long)");
        //NLParser parser = new NLParser("I have set rowCacheSize to 1000");
        NLParser parser = new NLParser();
        parser.setNlText("I have created the cellStyle myself and used setCellStyle on it");
        edu.stanford.nlp.trees.Tree tree = parser.parse();
        tree.pennPrint();
        TextStructureTree structTree = new TextStructureTree(0);
        structTree.construct(tree , null);

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

    public void construct(edu.stanford.nlp.trees.Tree tree , Tree parent) {
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
        //return getContent();
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
        if(result.size() == 0 && root.getDisplayContent().compareTo("VP") == 0){
            result.add(this);
        }
        return result;
    }
}
