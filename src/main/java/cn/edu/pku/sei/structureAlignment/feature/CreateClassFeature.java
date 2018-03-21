package cn.edu.pku.sei.structureAlignment.feature;

import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.NodeType;
import cn.edu.pku.sei.structureAlignment.tree.Tree;
import cn.edu.pku.sei.structureAlignment.util.ClassNameList;

import java.util.*;

/**
 * Created by oliver on 2018/3/10.
 */
public class CreateClassFeature extends Feature {

    Set<String> classSet ;

    public CreateClassFeature(){
        classSet = new HashSet<>();
        weight = Feature.defaultWeight;
    }

    public CreateClassFeature(double weight){
        this.weight = weight;
    }

    @Override
    public  boolean getFeature(String nlText ){
        NLParser nlParser = new NLParser(nlText);

        if(nlText.toLowerCase().contains("create")){
            List<Dependency> dependencies = nlParser.getUniversalDependency();
            for(Dependency dependency : dependencies){
                if(dependency.getSource().toLowerCase().compareTo("create") == 0){
                    String target = dependency.getTarget();
                    if(ClassNameList.contains(target)){
                        classSet.add(target);
                    }
                }
            }

            if(classSet.size() == 0){
                Map<String , String> word2class = nlParser.getWord2class();
                for(String token : word2class.keySet()){
                    classSet.add(word2class.get(token));
                }

            }
        }

        if(classSet.size() == 0) return false;
        else return true;
    }

    @Override
    public boolean getFeature(String nlText, Object... arguments) {
        return false;
    }

    @Override
    public boolean match(CodeStructureTree codeStructureTree) {

        List<CodeStructureTree> trees = findClassInstanceCreationNode(codeStructureTree);

        for(CodeStructureTree tree : trees){
            String code = tree.getCode();
            for(String clazz : classSet){
                if(code.contains(clazz))
                    return true;
            }
        }
        return false;
    }

    private List<CodeStructureTree> findClassInstanceCreationNode(CodeStructureTree tree){
        List<CodeStructureTree> result = new ArrayList<>();
        if(tree.root.getType() == NodeType.CODE_ClassInstanceCreation){
            result.add(tree);
        }else{
            for(Tree child : tree.getChildren()){
                result.addAll(findClassInstanceCreationNode((CodeStructureTree) child));
            }
        }
        return result;
    }

}
