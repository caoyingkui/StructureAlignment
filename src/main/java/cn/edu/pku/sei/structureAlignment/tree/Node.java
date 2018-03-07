package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.parser.code.CodeVisitor;
import cn.edu.pku.sei.structureAlignment.util.ClassNameList;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;

import java.util.*;

/**
 * Created by oliver on 2017/12/23.
 */
public class Node {
    protected int id;
    protected NodeType type;
    protected String content;  // the original text of a node
    protected List<String> alternatives;
    protected String displayContent; //
    protected String additionalInfo; // the information we can be extracted by other ways.

    //region <setter>
    public void setType(NodeType type) {
        this.type = type;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void addAlternatives(String content){
        alternatives.add(content.trim().toLowerCase());
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

        alternatives = new ArrayList<>();
        String typeInfo = CodeVisitor.getVariableType(content);
        if(typeInfo.compareTo("") != 0) {
            alternatives.add(typeInfo.toLowerCase());
        }

        this.additionalInfo = "";
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

        //remove the punctuation "
        if(this.type == NodeType.CODE_StringLiteral) content = content.length() > 2 ? content.substring(1 , content.length() - 1) : "";

        if(node.type == NodeType.CODE_StringLiteral) anotherContent = anotherContent.length() > 2 ? content.substring(1 , content.length() - 1) : "" ;

        if(content.trim().toLowerCase().compareTo(anotherContent.trim().toLowerCase()) == 0 || this.alternativesContains(anotherContent)) {
            return 2;
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

    public boolean alternativesContains(String content){
        content = content.trim().toLowerCase();
        return alternatives.contains(content);
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


