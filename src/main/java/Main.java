import cn.edu.pku.sei.structureAlignment.Printer;
import cn.edu.pku.sei.structureAlignment.parser.CodeVisitor;
import cn.edu.pku.sei.structureAlignment.parser.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.*;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.*;
import javafx.util.Pair;
import org.eclipse.jdt.core.dom.PrimitiveType;

/**
 * Created by oliver on 2017/12/25.
 */
public class Main extends JPanel{
    @Override
    protected void printComponent(Graphics g) {
        g.drawOval(5, 5, 25, 25);
    }

    @Override
    public void paint(Graphics g) {

        super.paint(g);
        g.setColor(Color.orange);
        //g.fillRect(10, 10, 10, 10 );

        //Font font = Font.decode("Times New Roman");

        Font font = new Font("Verdana", Font.BOLD, 24);
        g.setFont(font);
        String text = "Foo123456789abcdefg";

        //Rectangle2D r2d = g.getFontMetrics(font).getStringBounds(text, g);
        g.drawString(text , 0 , 30);
        g.fillRect(0 , 30 , 308 , 30);


        int width = this.getWidth();
        int height = this.getHeight();

        g.fillRect(width / 2 - 10 , 0 , 20 , 10);

        //g.fillRect(123, 0 , (int)r2d.getWidth() + 1 , 11);
        //g.fillRect(0 , 11, (int)r2d.getWidth() , (int)r2d.getHeight());

    }

    public static void main(String[] args) throws IOException {
        compare("code.txt" , "text.txt");
       /* ASTParser parser = ASTParser.newParser(AST.JLS8);
        //parser.setSource("StreamingReader reader = StreamingReader.builder().rowCacheSize(1000).bufferSize(4096).sheetIndex(0).read(is);".toCharArray());
        parser.setSource("row.getCell(3).setCellStyle(style);".toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);

        Block block = (Block)parser.createAST(null);

        CodeVisitor visitor = new CodeVisitor(0);
        block.accept(visitor);

        CodeStructureTree tree = visitor.getTree();


        //NLParser textParser = new NLParser("I have set rowCacheSize to 1000 (with your higher value, it took way too long)");
        NLParser textParser = new NLParser("I have created the cellStyle myself and used setCellStyle on it");
        edu.stanford.nlp.trees.Tree textTree = textParser.parse();
        TextStructureTree structTree = new TextStructureTree(0);
        structTree.construct(textTree , null);




        Map<Integer , Node> textLeafNodes = structTree.getAllLeafNodes();
        Map<Integer , Node> codeLeafNodes = tree.getAllLeafNodes();
        ArrayList<Pair<Integer , Integer>>similar = new ArrayList<Pair<Integer , Integer>>();


        for(int codeKey : codeLeafNodes.keySet()){
            Node codeNode = codeLeafNodes.get(codeKey);
            for(int textKey : textLeafNodes.keySet()){
                Node textNode = textLeafNodes.get(textKey);
                if(codeNode.compare(textNode) > 0.01){
                    System.out.println(  ((CodeStructureTree)tree.getTree(codeKey)).getCode() );
                    System.out.println(textNode.getContent());
                    similar.add(new Pair<Integer , Integer>(codeKey , textKey));
                }
            }
        }

        for(int i = 0 ; i < similar.size(); i++){
            for(int j = i + 1 ; j < similar.size() ; j++){
                Pair<Integer , Integer> pair1 = similar.get(i);
                Pair<Integer , Integer> pair2 = similar.get(j);

                int id1 = pair1.getKey();
                int id2 = pair2.getKey();
                if(id1 != id2){
                    int parent1 = tree.findCommonParents(id1 , id2 , 1) ;
                    int parent2 = structTree.findCommonParents(pair1.getValue() , pair2.getValue() , 0);
                    if(parent1 != -1 && parent2 != -1){
                        System.out.println(((CodeStructureTree)tree.getTree(parent1)).getCode());
                        System.out.println(structTree.getTree(parent2).getContent());
                        System.out.println();
                    }
                }
            }
        }

*/


    }

    public static void compare(String codePath , String textPath){
        ArrayList<CodeStructureTree> codeTrees = new ArrayList<CodeStructureTree>();
        ArrayList<TextStructureTree> textTrees = new ArrayList<TextStructureTree>();


        try{
            String line = "";


            BufferedReader reader = new BufferedReader(new FileReader(new File(codePath)));
            while((line = reader.readLine()) != null){
                ASTParser codeParser = ASTParser.newParser(AST.JLS8);
                codeParser.setKind(ASTParser.K_STATEMENTS);
                codeParser.setSource(line.toCharArray());
                Block block = (Block) codeParser.createAST(null);
                CodeVisitor visitor = new CodeVisitor(0);
                block.accept(visitor);
                CodeStructureTree tree = visitor.getTree();
                codeTrees.add(tree);
            }
            reader.close();


            line = "";
            reader = new BufferedReader(new FileReader(new File(textPath)));
            while((line = reader.readLine()) != null ) {
                NLParser textParser = new NLParser(line);
                TextStructureTree tree = textParser.getTree();
                textTrees.add(tree);
            }


            for(CodeStructureTree codeTree : codeTrees){
                for(TextStructureTree textTree : textTrees){
                    compare(codeTree , textTree);
                }
            }






        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void compare(CodeStructureTree codeTree , TextStructureTree textTree){
        Map<Integer , Node> codeLeafNodes = codeTree.getAllLeafNodes();
        Map<Integer , Node> textLeafNodes = textTree.getAllLeafNodes();

        //找到完全相同的点
        Map<Integer , ArrayList<Integer>> nodes = new HashMap<Integer, ArrayList<Integer>>();
        for(int codeId : codeLeafNodes.keySet()){
            Node codeNode = codeLeafNodes.get(codeId);
            ArrayList<Integer> similar = new ArrayList<Integer>();
            for(int textId : textLeafNodes.keySet()){
                Node textNode = textLeafNodes.get(textId);
                if(codeNode.compare(textNode) == 1){
                    similar.add(textId);
                }
            }
            if(similar.size() >0){
                nodes.put(codeId , similar);
            }
        }

        ArrayList<Integer> group = new ArrayList<Integer>();
        for(int key : nodes.keySet()){
            group.add(key);
        }

        Map<Integer , List<Integer>> parent_children = codeTree.findCommonParents( group , 20);

        for(int parent : parent_children.keySet()){
            List<Integer> children = parent_children.get(parent);
            List<Integer> textChildren = new ArrayList<Integer>();

            for(int child : children){
                textChildren.add(nodes.get(child).get(0));
            }

            int textParent = textTree.findCommonParents(textChildren);

            System.out.println(((CodeStructureTree)codeTree.getTree(parent)).getCode());
            System.out.println(textTree.getTree(textParent).getContent());
            System.out.println("   ");
        }


        ArrayList<TextStructureTree> VPs = textTree.findAllVP();

        List<CodeStructureTree> nonleafNodes = codeTree.getAllNonleafNodes();

        for(TextStructureTree vpTree : VPs){
            double max = -1;
            CodeStructureTree candidate = null;

            for(CodeStructureTree nonleafNode : nonleafNodes){
                double sim = Stemmer.compare(vpTree.getContent() , nonleafNode.getContent());

                if(vpTree.getId() == 2 && nonleafNode.getId() == 9){
                    System.out.println("similarity:" + sim);
                }

                /*System.out.println("similarity:" + sim);
                System.out.println(nonleafNode.getCode());
                System.out.println(vpTree.getContent());
                System.out.println();*/


                if(sim > max){
                    max = sim;
                    candidate = nonleafNode;
                }else if(sim == max){
                    if(candidate.getTree(nonleafNode.getId()) != null){
                        candidate = nonleafNode;
                    }
                }
            }
            if(max > 0.2) {
                System.out.println(candidate.getCode());
                System.out.println(vpTree.getContent());
                System.out.println("   ");
            }
        }


    }
}
