package cn.edu.pku.sei.structureAlignment.tree;

/**
 * Created by oliver on 2018/3/22.
 * MatchedPair is used to recode the matched node with the similarity
 */
public class MatchedNode {
    // MatchedPair is stored in the nodes of codeTree or textTree
    // So , it is only needed to store the matched nodes' info
    // for a text node , the matchedTreeID means the matched code tree id , vice versa
    public int matchedTreeID;
    public int matchedNodeID;
    public Node matchedNode;
    public double similarity;

    public MatchedNode(int treeId , int NodeId , Node node , double sim){
        matchedTreeID = treeId;
        matchedNodeID = NodeId;
        matchedNode = node;
        similarity = sim;
    }
}
