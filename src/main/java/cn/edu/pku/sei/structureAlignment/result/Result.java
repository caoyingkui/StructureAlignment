package cn.edu.pku.sei.structureAlignment.result;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oliver on 2018/3/28.
 */
public class Result {
    public Map<Integer , ResultItem> items;

    public Result(){
        items = new HashMap<>();
    }

    public void print(){
        System.out.println("    " + "example | completely | partly | no | wrong | precision | recall");

        for(Integer itemName : items.keySet()){
            ResultItem item = items.get(itemName);
            System.out.printf(itemName + " %7d | %9d | %6d | %3d | %5d | %.2f | %.2f\n" ,
                    item.exampleCount , item.completelyMatch , item.partlyMatch , item.noMatch , item.wronglyMatch ,
                    (float)(item.completelyMatch + item.partlyMatch ) / (item.completelyMatch + item.partlyMatch + item.wronglyMatch) ,
                    (float)(item.completelyMatch + item.partlyMatch) / (item.exampleCount));

        }
    }
}
