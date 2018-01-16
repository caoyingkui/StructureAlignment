package cn.edu.pku.sei.structureAlignment.parser;

import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oliver on 2017/12/23.
 */
public class NLParser {
    LexicalizedParser parser = null;
    String nlText;
    TextStructureTree textStructureTree;
    edu.stanford.nlp.trees.Tree nlTree;

    public static void main(String[] args){
        NLParser p = new NLParser("searches a Lucene index");
        p.getTextStructureTree().print();
        String subject = p.getSubjectiveNoun();
        String verb = p.getVerb();
        List<String> nouns = p.getNonSubjectiveNoun();

        System.out.println("subject: " + subject);
        System.out.println("verb: " + verb);
        for(String noun : nouns){
            System.out.print(noun + " ");
        }

    }


    public NLParser(String text){
        this.nlText = text;
        String[] options = {};
        parser = LexicalizedParser.loadModel("nl parser models\\englishPCFG.ser.gz" , options);
        parseForNLTree();
        textStructureTree = null;
    }

    public void setNlText(String text){
        this.nlText = text;
        parseForNLTree();
        if(textStructureTree != null)
            textStructureTree = null;
    }

    public void loadModel(String path){
        String[] options = {};
        parser = LexicalizedParser.loadModel(path , options);
        parseForNLTree();
        if(textStructureTree != null)
            textStructureTree = null;
    }

    private void parseForNLTree(){
        try{
            nlTree = parser.parse(nlText);
        }catch(Exception e){
            e.printStackTrace();
            nlTree = null;
        }
    }

    public edu.stanford.nlp.trees.Tree getNLTree(){
        return nlTree;
    }

    public TextStructureTree getTextStructureTree(){
        if(textStructureTree == null){
            try{
                edu.stanford.nlp.trees.Tree textTree = getNLTree();
                textStructureTree = new TextStructureTree(0);
                textStructureTree.construct(new Sentence(nlText));
                return textStructureTree;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return textStructureTree;
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


    private boolean contains(String[] set , String str){
        for(String s : set){
            if(s.compareTo(str) == 0)
                return true;
        }

        return false;
    }

    public String getVerb(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};
        String[] verbWordLevels = {"VB" , "VBD" , "VBG" , "VBN" , "VBP" , "VBZ"};


        String result = "";

        try{
            // get clause level child
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                for(Tree child : children){
                    label = child.label().toString();

                    // phrase level
                    if(label.compareTo("VP") == 0){
                        Tree[] vpChildren = child.children();
                        for(Tree vpChild : vpChildren){
                            label = vpChild.label().toString();
                            if(contains(verbWordLevels , label)){
                                result = vpChild.getLeaves().get(0).toString();
                                return result;
                            }
                        }
                    }
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }

    public String getSubjectiveNoun(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};
        String[] nounWordLevels = {"NN" , "NNS" , "NNP" , "NNPS" };

        try{
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                for(Tree child : children){
                    label = child.label().toString();
                    if(label.compareTo("VP") == 0)
                        break;
                    else if(label.compareTo("NP") == 0){
                        Tree[] npChildren = child.children();
                        for(Tree npChild : npChildren){
                            label = npChild.label().toString();
                            if(contains(nounWordLevels , label)){
                                return npChild.getLeaves().get(0).toString();
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getNonSubjectiveNoun(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};

        List<String> result = new ArrayList<>();

        try{
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                // 遇到动词之后的所有名词都会被返回
                boolean meetVerb = false;
                for(Tree child : children){
                    label = child.label().toString();
                    if(label.compareTo("VP") == 0){
                        meetVerb = true;
                    }else if(meetVerb){
                        result.addAll(findAllNouns(child));
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public List<String> findAllNouns(Tree tree){
        String[] nounWordLevels = {"NN" , "NNS" , "NNP" , "NNPS" };
        List<String> result = new ArrayList<>();
        String label = tree.label().toString();
        if(contains(nounWordLevels , label)){
            result.add(tree.getLeaves().get(0).toString());
        }else{
            if(tree.children().length > 0){
                Tree[] children = tree.children();
                for(Tree child : children){
                    result.addAll(findAllNouns(child));
                }
            }
        }

        return result;
    }
}
