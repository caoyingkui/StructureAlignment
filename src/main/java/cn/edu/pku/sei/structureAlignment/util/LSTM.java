package cn.edu.pku.sei.structureAlignment.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oliver on 2018/7/19.
 */
public class LSTM {

    public static class SIM{
        public int code;
        public int comment;
        public double sim;
        public SIM(int row, int col, double similarity){
            code = row;
            comment = col;
            sim = similarity;
        }
    }
    public static String currentFile= null;
    public static int m = -1;
    public static int n = -1;
    public static Map<String, List<SIM>> cases = new HashMap<>();
    public static String filePath = "result.txt";

    static{
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
            String line = "";
            line = reader.readLine();
            while(true){
                if(line == null) break;
                String fileName = line.substring(1);
                List<SIM> pairs = new ArrayList<>();
                while(true){
                    line = reader.readLine();
                    //System.out.println(line);
                    if(line == null || line.charAt(0) == '#')
                        break;
                    String[] nums = line.trim().split(" ");
                    SIM sim = new SIM(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]), Double.parseDouble(nums[2]));
                    pairs.add(sim);
                }

                cases.put(fileName, pairs);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public LSTM(){
        ;
    }
}
