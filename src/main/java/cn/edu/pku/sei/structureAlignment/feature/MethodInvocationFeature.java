package cn.edu.pku.sei.structureAlignment.feature;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.parser.nlp.Dependency;
import cn.edu.pku.sei.structureAlignment.parser.nlp.NLParser;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.NodeType;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;

import java.util.*;

/**
 * Created by oliver on 2018/1/18.
 */
public class MethodInvocationFeature extends Feature {
    Map<String , Set<String>> verbObjectGroup ;

    public MethodInvocationFeature(){
        verbObjectGroup = new HashMap<>();
    }

    @Override
    public double getWeight() {
        return super.getWeight();
    }

    @Override
    public boolean getFeature(TextStructureTree textTree) {
        boolean result = false;
        List<Dependency> dependencies = textTree.getDependency("direct object");
        if(dependencies.size() > 0 ){
            result = true;
            for(Dependency dependency : dependencies){
                String verb = dependency.getSource().toLowerCase();
                String object = dependency.getTarget().toLowerCase();

                if(verbObjectGroup.containsKey(verb)){
                    verbObjectGroup.get(verb).add(object);
                }else{
                    Set<String> objects = new HashSet<>();
                    objects.add(object);
                    verbObjectGroup.put(verb , objects);
                }
            }
        }
        return result;
    }

    @Override
    public boolean getFeature(String nlText, Object... arguments) {
        return false;
    }

    @Override
    public double match(CodeStructureTree codeStructureTree) {
        return 4 * findMethodInvocation(codeStructureTree);
    }

    private double findMethodInvocation(CodeStructureTree codeTree){
        double result = 0 ;
        List<CodeStructureTree> children = codeTree.getChildren();
        if(children.size() == 0)
            return 0;
        else{
            if(codeTree.root.getType() == NodeType.CODE_MethodInvocation){
                for(String verb : verbObjectGroup.keySet()){
                    double findVerbResult = findVerb(codeTree , verb);
                    if(findVerbResult >= 0)
                        result += findVerbResult * findObjects(codeTree , verbObjectGroup.get(verb));
                }
                return result;
            }else{
                for(CodeStructureTree child : children){
                    result += findMethodInvocation(child);
                }
                return result;
            }
        }
    }

    private double findVerb(CodeStructureTree codeTree , String verb ){
        double result = 0;
        List<CodeStructureTree> children = codeTree.getChildren();
        if(children.size() == 0){
            String content = codeTree.getContent().toLowerCase();
            if(content.compareTo(verb) == 0 ||
                    content.contains(verb)){
                return 1;
            }
        }else{
            for(CodeStructureTree child : children){
                result = findVerb(child , verb);
                if(result > 0)
                    return result;
            }

        }
        return 0;
    }

    private double findObjects(CodeStructureTree codeTree , Set<String> objects){
        double result = 0;
        if(objects.size() == 0)
            return 0;
        List<CodeStructureTree> children = codeTree.getChildren();
        if(children.size() == 0){
            String content = codeTree.getContent().toLowerCase();
            if(objects.contains(content)){
                objects.remove(content);
                return 1;
            }else{
                return 0;
            }
        }else{
            for(CodeStructureTree child : children){
                result += findObjects(codeTree , objects);
            }
            return result;
        }
    }
}
