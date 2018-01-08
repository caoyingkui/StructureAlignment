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
            NLParser textParser = new NLParser();
            reader = new BufferedReader(new FileReader(new File(textPath)));
            while((line = reader.readLine()) != null ) {
                textParser.setNlText(line);
                TextStructureTree tree = textParser.getTree();
                textTrees.add(tree);
            }

            Scanner sc = new Scanner(System.in);
            int a = 1, b;
            while(a != -1){
                a = sc.nextInt();
                b = sc.nextInt();

                CodeStructureTree ct = codeTrees.get(a);
                TextStructureTree tt = textTrees.get(b);

                ct.print();
                tt.print();

                compare(ct, tt);
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

        int codeEndIndex = codeTree.getEndIndex();
        int textEndIndex = textTree.getEndIndex();
        double[][] similarMatrix = new double[codeEndIndex][textEndIndex];
        for(int i = 0 ; i < codeEndIndex ; i ++)
            for(int j = 0 ; j < textEndIndex ; j ++)
                similarMatrix[i][j] = -1;

        findIdenticalPair(codeTree , textTree , similarMatrix);

        ArrayList<TextStructureTree> VPs = textTree.findAllVP();

        List<CodeStructureTree> nonleafNodes = codeTree.getAllNonleafNodes();

        for(TextStructureTree vpTree : VPs){

            for(CodeStructureTree nonleafNode : nonleafNodes){
                int textId = vpTree.getId();
                int codeId = nonleafNode.getId();
                similarMatrix[codeId][textId] = Stemmer.compare(vpTree.getContent() , nonleafNode.getContent());

            }
        }

        boolean signal = true;
        while(signal){
            signal = false;
            double max = 0.5;
            int max_codeId = -1;
            int max_textId = -1;
            for(int codeId = 0 ; codeId < codeEndIndex ; codeId ++){
                for(int textId = 0 ; textId < textEndIndex ; textId ++){
                    if(similarMatrix[codeId][textId] > max){
                        signal = true;
                        max_codeId = codeId;
                        max_textId = textId;
                    }
                }
            }

            if(signal){
                System.out.println(((CodeStructureTree)codeTree.getTree(max_codeId)).getCode().trim());
                System.out.println(textTree.getTree(max_textId).getContent().trim());
                System.out.println(" ");

                for(int codeId = 0 ; codeId < codeEndIndex ; codeId ++)
                    similarMatrix[codeId][max_textId] = -1;
                for(int textId = 0 ; textId < textEndIndex ; textId ++)
                    similarMatrix[max_codeId][textId] = -1;
            }
        }

    }

    public static void findIdenticalPair(CodeStructureTree codeTree , TextStructureTree textTree , double[][] matrix){
        Map<Integer , Node> codeLeafNodes = codeTree.getAllLeafNodes();
        Map<Integer , Node> textLeafNodes = textTree.getAllLeafNodes();

        Map<Integer , Integer> similarPairs = new HashMap<Integer, Integer>();

        // this array is used to store the text node which have been recognized as identical with some code node
        ArrayList<Integer> textNodes = new ArrayList<>();
        for(int codeId : codeLeafNodes.keySet()){
            Node codeNode = codeLeafNodes.get(codeId);
            for(int textId : textLeafNodes.keySet()){
                Node textNode = textLeafNodes.get(textId);
                if(codeNode.compare(textNode) == 1 && !textNodes.contains(textId)){
                    textNodes.add(textId);
                    similarPairs.put(codeId , textId);
                    break;
                }
            }
        }


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
                matrix[codeId][textId] = 1;

            }

            int textParent = textTree.findCommonParents(textChildren);
            matrix[parent][textParent] = 1;

            System.out.println(((CodeStructureTree)codeTree.getTree(parent)).getCode().trim());
            System.out.println(textTree.getTree(textParent).getContent().trim());
            System.out.println("   ");
        }

    }
}
