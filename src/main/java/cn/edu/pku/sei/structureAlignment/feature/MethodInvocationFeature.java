package cn.edu.pku.sei.structureAlignment.feature;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.NodeType;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oliver on 2018/1/18.
 */
public class MethodInvocationFeature extends Feature{
    Set<String> qualifiedNameFeature;
    Set<String> returnFeature;
    Set<String> identifierFeature;
    Set<String> parameterFeature;

    public static void main(String[] args){
        MethodInvocationFeature feature = new MethodInvocationFeature();
        feature.getFeature("Looking at your code I think you need to add each query " +
                "(one for the SearchKey and one for the Type) to the BooleanQuery like below");
    }

    public MethodInvocationFeature(){
        returnFeature = null;
        identifierFeature = null;
        parameterFeature = null;
    }

    public void addReturnFeature(String feature){
        if(returnFeature == null)
            returnFeature = new HashSet<>();
        returnFeature.add(feature);
    }

    public void addIdentifierFeature(String feature){
        if(identifierFeature == null)
            identifierFeature = new HashSet<>();
        identifierFeature.add(feature);
    }

    public void addParameterFeature(String feature){
        if(parameterFeature == null)
            parameterFeature = new HashSet<>();
        parameterFeature.add(feature);
    }

    public void setReturnFeature(Set<String> classes){
        returnFeature = new HashSet<>();
        returnFeature.addAll(classes);
    }

    public void setIdentifierFeature(Set<String> classes){
        identifierFeature = new HashSet<>();
        identifierFeature.addAll(classes);
    }

    public void setParameterFeature(Set<String> parameterFeature){
        this.parameterFeature = new HashSet<String>();

        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(parameterFeature);
        parameterFeature.addAll(Stemmer.stem(temp));
    }

    public boolean match(CodeLineRelationGraph graph) {
        boolean result = false;
        for(CodeStructureTree tree : graph.getCodeLineTrees()){
            String code = tree.getCode();
            List<String> tokens = Stemmer.stem(code);

            if(returnFeature != null){
                result = false;
                for(String clazz : returnFeature){
                    if(tokens.contains(clazz)){
                        result = true;
                        break;
                    }
                }
            }

            if(!result) continue;

            if(identifierFeature != null){
                result = false;
                for(String clazz : identifierFeature){
                    if(tokens.contains(clazz)){
                        result = true;
                        break;
                    }
                }
            }

            if(!result) continue;

            if(parameterFeature != null){
                result = false;
                for(String clazz : parameterFeature){
                    if(tokens.contains(clazz)) {
                        result = true;
                        break;
                    }
                }
            }

            if(result) break;
        }

        return result;
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean getFeature(String nlText) {

        NLParser nlParser = new NLParser(nlText);
        TextStructureTree textTree = nlParser.getTextStructureTree();

        List<Dependency> dobjDependencies = textTree.getDependency("direct object");
        List<Dependency> nmodDependencies = textTree.getDependency("nominal modifier");

        for(Dependency dependency : dobjDependencies){
            int verbId = dependency.source.id;



        }





        return false;
    }

    @Override
    public boolean getFeature(String nlText, Object... arguments) {
        return false;
    }

    @Override
    public boolean match(CodeStructureTree codeStructureTree) {
        return false;
    }
}
