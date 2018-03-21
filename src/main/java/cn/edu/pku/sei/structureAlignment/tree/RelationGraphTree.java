package cn.edu.pku.sei.structureAlignment.tree;

import java.util.ArrayList;

/**
 * Created by oliver on 2018/1/14.
 */
public class RelationGraphTree extends Tree {

    public RelationGraphTree(String content){
        this.root = new Node(NodeType.NULL , content , 0);
        children = new ArrayList();
    }

    public void addChild(RelationGraphTree child){
        children.add(child);
    }

    @Override
    public String getDisplayContent() {
        return root.getContent();
    }
}
