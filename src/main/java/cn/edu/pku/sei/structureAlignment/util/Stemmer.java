package cn.edu.pku.sei.structureAlignment.util;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Created by oliver on 2017/12/27.
 */
public class Stemmer {
    public static void  main(String[] args){

        String words = "exists existing";


        for(String token : stem(words)){
            System.out.println(token);
        }
    }

    public static String stemSingleWord(String word){
        String result = "";
        try{
            EnglishStemmer stemmer = new EnglishStemmer();
            stemmer.setCurrent(word);
            stemmer.stem();
            result = stemmer.getCurrent();
        }catch (Exception e){
            result = "";
            e.printStackTrace();
        }

        return result;
    }

    public static List<String> stem(String sentence){
        List<String> result = new ArrayList<String>();
        try {
            result = new ArrayList<String>();
            List<String> tokens = tokenize(sentence);

            EnglishStemmer stemmer = new EnglishStemmer();

            for(String token : tokens){
                stemmer.setCurrent(token);
                stemmer.stem();
                result.add(stemmer.getCurrent());
            }


        }catch(Exception e){
            result.clear();
            e.printStackTrace();
        }
        return result;
    }

    public static List<String> stem(List<String> words){
        try{
            List<String> result = new ArrayList<String>();
            EnglishStemmer stemmer = new EnglishStemmer();
            for(String word : words){
                stemmer.setCurrent(word.toLowerCase());
                stemmer.stem();
                result.add(stemmer.getCurrent());
            }

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> tokenize(String sentence){
        try{
            String camelCasePattern = "([^\\p{L}\\d]+)|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)|(?<=[\\p{L}&&[^\\p{Lu}]])(?=\\p{Lu})|(?<=\\p{Lu})(?=\\p{Lu}[\\p{L}&&[^\\p{Lu}]])";
            sentence = String.join(" " , sentence.split(camelCasePattern));
            List<String> result = new ArrayList<String>();

            Analyzer analyzer = new StopAnalyzer();

            TokenStream tokenStream = analyzer.tokenStream("" , new StringReader(sentence));
            CharTermAttribute cta = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while(tokenStream.incrementToken()){
                String term = cta.toString();
                result.add(term);
            }

            return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static double compare(String str1 , String str2){
        String[] tokens1 = stem(tokenize(str1)).toArray(new String[0]);
        String[] tokens2 = stem(tokenize(str2)).toArray(new String[0]);

        CosineSimilarity similarity = new CosineSimilarity();

        Map<CharSequence , Integer> left = Arrays.stream(tokens1).collect(Collectors.toMap(c -> c , c -> 1 , Integer::sum));

        Map<CharSequence , Integer> right = Arrays.stream(tokens2).collect(Collectors.toMap(c -> c , c -> 1 , Integer::sum));

        return similarity.cosineSimilarity(left , right);


        /*
        int count= 0;
        double result = 0;
        if(tokens1.size() > tokens2.size()) {
            for (String token1 : tokens1) {
                for (String token2 : tokens2) {
                    if(token1.compareTo(token2) == 0) {
                        count ++;
                        break;
                    }
                }
            }

            result = ((double) count) / tokens1.size();
        }else{
            for(String token2 : tokens2){
                for(String token1 : tokens1){
                    if(token2.compareTo(token1) == 0){
                        count ++ ;
                        break;
                    }
                }
            }

            result = ((double) count) / tokens2.size();
        }

        return result;*/
    }
}
