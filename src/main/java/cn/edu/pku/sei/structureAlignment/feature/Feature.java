package cn.edu.pku.sei.structureAlignment.feature;

import cn.edu.pku.sei.structureAlignment.CodeLineRelation.CodeLineRelationGraph;
import cn.edu.pku.sei.structureAlignment.tree.CodeStructureTree;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;

import java.util.Map;

/**
 * Created by oliver on 2018/1/18.
 */

public abstract class Feature {
    public static double defaultWeight = 4.0;

    public double weight;

    public double getWeight(){
        return weight;
    }

    public abstract boolean getFeature(TextStructureTree textTree);

    public abstract boolean getFeature(String nlText , Object... arguments);

    public abstract double match(CodeStructureTree codeStructureTree);
}
