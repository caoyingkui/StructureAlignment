package cn.edu.pku.sei.structureAlignment.parser.nlp;

import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;

/**
 * Created by oliver on 2018/1/18.
 */
public class Dependency {
    public class Word{
        private String[] verbTags = {"VB" , "VBD" , "VBG" , "VBP", "VBZ"};
        private String[] nounTags = {"NN" , "NNS" , "NNP" , "NNPS"};

        public String word;
        public String posTag;

        public int id;

        public Word(IndexedWord indexedWord){
            word = indexedWord.word();
            posTag = indexedWord.tag();
        }

        public boolean isVerb(){
            for(String tag : verbTags){
                if(posTag.compareTo(tag) == 0)
                    return true;
            }
            return false;
        }

        public boolean isNoun(){
            for(String tag : nounTags){
                if(posTag.compareTo(tag) == 0)
                    return true;
            }

            return false;
        }

    }

    public class Relation{
        public String shortName;
        public String longName;

        public Relation(GrammaticalRelation grammaticalRelation){
            shortName = grammaticalRelation.getShortName();
            longName = grammaticalRelation.getLongName();
        }

    }


    public Word source;
    public Word target;
    public Relation relation;

    public Dependency(SemanticGraphEdge edge){
        source = new Word(edge.getSource());
        target = new Word(edge.getTarget());
        relation = new Relation(edge.getRelation());
    }

    public String getSource(){
        return source.word;
    }

    public String getTarget(){
        return target.word;
    }

    public String getRelation(){
        return relation.longName;
    }


}
