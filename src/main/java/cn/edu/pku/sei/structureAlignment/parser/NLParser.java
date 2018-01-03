package cn.edu.pku.sei.structureAlignment.parser;

import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/**
 * Created by oliver on 2017/12/23.
 */
public class NLParser {
    LexicalizedParser parser = null;
    String nlText;
    TextStructureTree tree;

    public NLParser(String text){
        this.nlText = text ;
        String[] options = {};
        parser = LexicalizedParser.loadModel("nl parser models\\wsjFactored.ser.gz" , options);
        tree = null;
    }

    public void setNlText(String text){
        this.nlText = text;
        tree = null;
    }

    public void loadModel(String path){
        String[] options = {};
        parser = LexicalizedParser.loadModel(path , options);
    }

    public edu.stanford.nlp.trees.Tree parse(){
        try{
            edu.stanford.nlp.trees.Tree result = parser.parse(nlText);
            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public TextStructureTree getTree(){
        if(tree == null){
            try{
                edu.stanford.nlp.trees.Tree textTree = parse();
                tree = new TextStructureTree(0);
                tree.construct(textTree , null);
                return tree;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return tree;
        }
    }

    /**
     * compare the similarity between the str1 and str2
     * @param str1
     * @param str2
     * @return
     */
    static double compare(String str1, String str2){
        return 0  ;
    }


}
