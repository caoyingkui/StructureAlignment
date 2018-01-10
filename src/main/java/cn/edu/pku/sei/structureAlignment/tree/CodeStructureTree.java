package cn.edu.pku.sei.structureAlignment.tree;


import cn.edu.pku.sei.structureAlignment.Printer;
import cn.edu.pku.sei.structureAlignment.parser.CodeVisitor;
import org.eclipse.jdt.core.dom.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oliver on 2017/12/23.
 */
public class CodeStructureTree extends Tree<CodeStructureTree>{

    protected String code;

    public static void main(String[] args ){
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        ///parser.setSource("XSSFCellStyle style = new XSSFCellStyle(new StylesTable());".toCharArray());
        ///parser.setSource("XSSFCellStyle style = new XSSFCellStyle(new StylesTable());".toCharArray());
        parser.setSource("d = null;".toCharArray());

        //parser.setSource("public class test{}".toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);

        int[] test = new int[]{1 , 2};
        Block block = (Block)parser.createAST(null);

        CodeVisitor visitor = new CodeVisitor(0);
        block.accept(visitor);

        CodeStructureTree tree = visitor.getTree();
        //tree.findCommonParents(16 , 19 , 4);
        List<Integer> nodes = new ArrayList<Integer>();
        nodes.add(16 );
        nodes.add(20);
        nodes.add(19);
        nodes.add(5);
        nodes.add(8);
        tree.findCommonParents(nodes , 4);

        /*JFrame frame = new JFrame();
        Printer printer = new Printer(tree.getTree(2));
        printer.setBackground(Color.white);
        frame.add(printer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 1200);
        frame.setVisible(true);*/

        tree.print();
    }

    public String getCode() {
        return this.code;
    }

    public CodeStructureTree(Node root , String code , Tree parent){
        this.root = root;
        this.code = code;
        this.children = new ArrayList<Tree>();
        this.parent = parent;
        startIndex = endIndex = root.getId();
    }

    public void setChildren(List<Tree> children){
        int e_id = 0; // endIndex
        for(Tree child : children){
            this.children.add(child);
            e_id = child.getEndIndex();
        }

        String content = combineContentOfChildNode();
        this.root.setContent(content);
        endIndex = e_id;
    }

    public String combineContentOfChildNode(){
        String result = "";
        for(Tree child : this.children){
            result += (" " + child.getContent().trim() );
        }
        return result.trim();
    }

    public List<CodeStructureTree> getSpecificTypeNode(NodeType type){
        List<CodeStructureTree> result = new ArrayList<>();

        if(root.type == type){
            result.add(this);
        }

        if(children.size() > 0){
            for(Tree child : children){
                result.addAll(((CodeStructureTree)child).getSpecificTypeNode(type));
            }
        }
        return result;
    }

    //this function only can be used when tree type is MethodInvocation
    public String getMethodInvocationName(){
        if(root.type != NodeType.CODE_MethodInvocation)
            return null;
        else{
            ASTParser parser = ASTParser.newParser(AST.JLS8);
            parser.setSource(code.toCharArray());
            parser.setKind(ASTParser.K_STATEMENTS);
            MethodInvocation methodInvocation = (MethodInvocation)parser.createAST(null);
            return methodInvocation.getName().toString();
        }
    }

    @Override
    public String getContent() {
        return root.getContent();
    }


    public String getDisplayContent(){
        String result = "";
        result += root.getId() + ": ";
        if(children.size() == 0) result += root.getContent();
        else result += root.getType().toString().substring(5);

        return result;
    }


    public List<CodeStructureTree> getAllNonleafTree(){
        List<CodeStructureTree> result = new ArrayList<>();
        if(children.size() != 0) {
            result.add(this);
            for(Tree child : children){
                result.addAll(((CodeStructureTree)child).getAllNonleafTree());
            }return result;
        }
        return result;
    }
}
