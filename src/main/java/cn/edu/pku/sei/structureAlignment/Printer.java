package cn.edu.pku.sei.structureAlignment;

import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.Tree;

import javax.swing.*;
import java.awt.*;

/**
 * Created by oliver on 2017/12/25.
 */
public class Printer extends JPanel {
    Tree tree;

    public Printer(Tree tree){
        this.tree = tree;
    }

    public void setTree(Tree tree){
        this.tree = tree;
    }



    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Font font = new Font("Verdana", Font.BOLD, 12);

        g.setFont(font);

        int width = this.getWidth();
        int height = this.getHeight();
        tree.calculateWidth(6 , g);
        tree.print(width / 2 , 0 , 13 , 30   , 10 , g);
    }
}
