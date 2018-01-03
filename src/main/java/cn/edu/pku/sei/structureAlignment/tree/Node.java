package cn.edu.pku.sei.structureAlignment.tree;

import cn.edu.pku.sei.structureAlignment.util.Stemmer;

import java.util.List;

/**
 * Created by oliver on 2017/12/23.
 */
public class Node {
    protected int id;
    protected String content;
    protected NodeType type;
    protected String displayContent;

    //region <setter>

    public void setContent(String content) {
        this.content = content;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public void setDisplayContent(String displayContent){
        this.displayContent = displayContent;
    }
    //endregion <setter>


    //region <getter>
    public String getContent() {
        return content;
    }

    public NodeType getType() {
        return type;
    }

    public int getId(){
        return id;
    }

    public String getDisplayContent(){
        return this.displayContent;
    }
    //endregion <getter>

    public Node(NodeType type , String content , int id){
        this.type = type;
        this.content = content;
        this.id = id;
    }

    public double compare(Node node){
        String anotherContent = node.getContent();

        List<String> words1 = Stemmer.stem(content);
        List<String> words2 = Stemmer.stem(anotherContent);

        int max = words1.size() > words2.size() ? words1.size() : words2.size();

        double count = 0.0;

        for(String word1 : words1){
            if(words2.contains(word1)) count ++;
        }

        return max != 0 ? count/max : 0;

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


