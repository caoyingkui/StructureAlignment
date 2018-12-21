package cn.edu.pku.sei.structureAlignment.parser.code;

import cn.edu.pku.sei.structureAlignment.database.ApiDB;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import mySql.SqlConnector;

import java.sql.ResultSet;
import java.util.*;

/**
 * Created by oliver on 2018/7/3.
 */
public class ClassJavadoc {
    public static Map<String , String> javadoc = new HashMap<>();
    public static Map<String , Set<String>> javadocWordSet = new HashMap<>();
    static{
        try {
            ApiDB.conn.setPreparedStatement("select name , javadoc from api where type = 'CLASS'");
            ResultSet rs = ApiDB.conn.executeQuery();
            System.out.println(new Date());
            while (rs.next()) {
                javadoc.put(rs.getString("name").toLowerCase(), rs.getString("javadoc"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getJavadoc(String className){
        return javadoc.getOrDefault(className.toLowerCase() , "");
    }

    public static boolean contains(String className){
        return javadoc.containsKey(className.toLowerCase());
    }

    public static boolean javadocContains(String className , String verb, Set<String> objectSet){
        if(!javadoc.containsKey(className))
            return false;
        String doc = javadoc.get(className);
        List<String> words = Stemmer.stem(doc);
        String v = Stemmer.stemSingleWord(verb);
        Set<String> objects = new HashSet<>();
        for(String object : objectSet){
            String temp = Stemmer.stemSingleWord(object);
            objects.add(temp);
        }

        for(int i = 0 ; i < words.size() ; i ++){
            if(words.get(i).compareTo(v) == 0){
                int index = i - 3 >= 0 ? i - 3 : 0;
                int end = i + 3 < words.size() ? i + 3 : words.size() ;
                for( ;index < end ; index ++ ){
                    if(objects.contains(words.get(index)) ){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    public static <T extends Collection<String> > boolean javadocContains(String name ,  T... wordSets ){
        String className =name.toLowerCase();
        if(!javadoc.containsKey(className))
            return false;
        Set<String> set = null;
        if(!javadocWordSet.containsKey(className)){
            String doc = javadoc.get(className);
            set = new HashSet<>();
            set.addAll(Stemmer.stem(javadoc.get(className)));
            javadocWordSet.put(className , set);
        }else
            set = javadocWordSet.get(className);

        boolean result = true;
        for(T wordSet : wordSets){
            result =false;
            for(String word : wordSet){
                if(set.contains(word)) {
                    result = true;
                    break;
                }
            }
            if( !result )
                break;
        }

        return result;
    }

}
