package cn.edu.pku.sei.structureAlignment.feature;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;

import java.util.*;

/**
 * Created by oliver on 2018/1/19.
 */
public class ContainClassFeature {
    List<Set<String>> classes ;

    public ContainClassFeature(){
        classes = new ArrayList<Set<String>>();
    }

    public void addClass(Set<String> clazz){
        classes.add(clazz);
    }

    public void addClass(String clazz){
        Set<String> temp = new HashSet<>();
        temp.add(clazz);
        classes.add(temp);
    }

    public int getClassSize(){
        return classes.size();
    }


    /**
     * In natural language, these may be several classes we can extract.
     * So, if there are some classes in the language, that is to say, the matched code snippet should contain all these classes
     * the field classes is a list a class which need to match, the element of this list is set,
     * the reason I choose set as the element of this list is :
     *    1: for a word in natural language, we may find multiple classes which will be matched by api name or stemmedName( name and stemmedName are column name in the database)
     *    2: for feature extension. we can match a word to all possible class
     * when match funtion works, only one string of the set need to be matched.
     * @param graph
     * @return
     */
    public boolean match(CodeLineRelationGraph graph) {

        int classNumber = classes.size();
        int count = 0;

        String code = "";
        for(CodeStructureTree tree : graph.getCodeLineTrees()){
            code += tree.getCode() + " ";
        }

        // find all the class in the code snippet
        Set<String> codeClassSet = new HashSet<>();
        Map<String , String> word2class = NLParser. getClassDictionary(code);
        for(String word : word2class.keySet()){
            codeClassSet.add(word2class.get(word));
        }

        // match the class one by one
        for(Set<String> clazz : classes){
            // only one class of clazz need to matched
            for(String candidate : clazz){
                if(codeClassSet.contains(candidate) ){
                    count ++ ;
                    break;
                }
            }
        }

        if(classNumber == count )
            return true;
        else
            return false;
    }



    public double getWeight() {
        return 0;
    }


    public boolean getFeature(String nlText) {
        return false;
    }


    public boolean getFeature(String nlText, Object... arguments) {
        return false;
    }


    public double match(CodeStructureTree codeStructureTree) {
        return 1;
    }
}
