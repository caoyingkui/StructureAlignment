package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.parser.code.CodeVisitor;
import cn.edu.pku.sei.structureAlignment.util.ClassNameList;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oliver on 2017/12/23.
 */
public class Node {
    protected int id;
    protected NodeType type;
    protected String content;  // the original text of a node
    protected String displayContent; //
    protected String additionalInfo; // the information we can be extracted by other ways.

    //region <setter>
    public void setType(NodeType type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDisplayContent(String displayContent){
        this.displayContent = displayContent;
    }

    public void setAdditionalInfo(String additionalInfo){
        this.additionalInfo = additionalInfo;
    }
    //endregion <setter>


    //region <getter>
    public int getId(){
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getDisplayContent(){
        return displayContent;
    }

    public String getAdditionalInfo(){
        return additionalInfo;
    }
    //endregion <getter>

    public Node(NodeType type , String content , int id){
        this.type = type;
        this.content = content;
        this.displayContent = content;
        this.id = id;
        additionalInfo = CodeVisitor.getVariableType(content);
    }

    /**
     * @param node
     * @return 2:  if two nodes' are completely identical and the content are not a class name.
     *              I think this condition will be more reliable to predict two nodes are identical
     *          1: if two nodes' are completely identical and the content are a class name
     *          0.5: if two nodes' are partially identical
     *          0: if two nodes' are completely different
     */
    public double compare(Node node){
        String content = this.getContent();
        String anotherContent = node.getContent();

        if(content.compareTo(anotherContent) == 0) {
            if(ClassNameList.classList.contains(content))
                return 1;
            else
                return 1;
        }

        Set<String> words1 = new HashSet<String>();
        words1.addAll(Stemmer.stem(content + " " + this.getAdditionalInfo()));

        Set<String> words2 = new HashSet<String>();
        words2.addAll(Stemmer.stem(anotherContent + " " + node.getAdditionalInfo()));

        for(String word1 : words1){
            for(String word2 : words2 ){
                if(word1.compareTo(word2) == 0){
                    return 0.5;
                }
            }
        }
        return 0;
    }
}


// region <grammar>
/**
 */
// endregion <grammar>

//region <construct the tree of the root>

//endregion <construct the tree of the root>

//region <construct the tree of >
//endregion <construct the tree of>


