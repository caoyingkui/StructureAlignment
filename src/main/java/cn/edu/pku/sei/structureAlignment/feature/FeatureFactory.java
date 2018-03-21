package cn.edu.pku.sei.structureAlignment.feature;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by oliver on 2018/3/11.
 */
public class FeatureFactory {

    public static void main(String[] args){
        getFeatures("Create a IndexWriter");
    }

    public static List<Feature> getFeatures(String nlText){
        List<Feature>  result = new ArrayList<>();

        Reflections reflections = new Reflections("cn.edu.pku.sei.structureAlignment.feature");
        Set<Class<? extends Feature>> featureClasses = reflections.getSubTypesOf(Feature.class);

        try{
            for(Class<? extends Feature> clazz : featureClasses) {
                Feature feature = clazz.newInstance();
                if( feature.getFeature(nlText) ){
                    result.add(feature);
                }
            }


        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
