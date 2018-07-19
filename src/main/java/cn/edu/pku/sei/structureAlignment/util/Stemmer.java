package cn.edu.pku.sei.structureAlignment.util;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by oliver on 2017/12/27.
 */
public class Stemmer {
    public static void  main(String[] args){

        String words = "Applying a boost there doesn't really accomplish anything. Boosts are used to enhance the impact of a subquery within the overall query. So it would be something like this:. There, your mlt query would be boosted to 50 times it's normal score relative to the other subquery. The particular reason that changing the boost may even result in lower scores is because of the queryNorm scoring factor. The overall score is multiplied by:";

        string2sentence(filterHtmlTags(words));
        System.out.println(filterHtmlTags(words));
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

    public static String filterHtmlTags(String text){
        StringBuilder result = new StringBuilder("");

        Pattern linkPattern = Pattern.compile("(<a[^>]*>([^<]*)</a>)|([a-zA-z]+://[^\\s]*)");
        Matcher matcher = linkPattern.matcher(text);

        int start = -1;
        int length = 0;
        while(matcher.find()){
            length = matcher.start() - start - 1;
            if(length > 0){
                result.append(text.substring(start + 1 , matcher.start()));
            }

            result.append(" " + matcher.group(2) + " ");
            start = matcher.end() - 1;
        }

        if(text.length() - start - 1 > 0){
            result.append(text.substring(start + 1 , text.length()));
        }

        return result.toString();

    }

    public static List<String> string2sentence(String string){
        List<String> result = new ArrayList<>();
        Reader reader = new StringReader(string);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        List<String> sentenceList = new ArrayList<String>();

        for (List<HasWord> sentence : dp) {
            // SentenceUtils not Sentence
            String sentenceString = SentenceUtils.listToString(sentence);
            result.add(sentenceString);
        }

        return result;
    }

    public static List<String> camelCase(String string){
        string = string.trim();
        String camelCasePattern = "([^\\p{L}\\d]+)|(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)|(?<=[\\p{L}&&[^\\p{Lu}]])(?=\\p{Lu})|(?<=\\p{Lu})(?=\\p{Lu}[\\p{L}&&[^\\p{Lu}]])";
        String[] subs = string.split(camelCasePattern);

        List<String> result = new ArrayList<>();
        for(String sub : subs){
            result.add(sub.toLowerCase());
        }
        return  result;
    }

    public static boolean equal(String word , String... wordSet){
        String s1 = Stemmer.stemSingleWord(word).toLowerCase();
        for(String word2 : wordSet){
            String s2 = Stemmer.stemSingleWord(word2).toLowerCase();
            if(s1.compareTo(s2) == 0)
                return true;
        }

        return false;
    }
}
