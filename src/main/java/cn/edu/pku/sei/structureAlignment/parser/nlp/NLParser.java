package cn.edu.pku.sei.structureAlignment.parser.nlp;

import cn.edu.pku.sei.structureAlignment.feature.ContainClassFeature;
import cn.edu.pku.sei.structureAlignment.feature.Feature;
import cn.edu.pku.sei.structureAlignment.feature.MethodInvocationFeature;
import cn.edu.pku.sei.structureAlignment.tree.TextStructureTree;
import cn.edu.pku.sei.structureAlignment.util.Stemmer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.DocumentPreprocessor;
import mySql.SqlConnector;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by oliver on 2017/12/23.
 */
public class NLParser  {
    static LexicalizedParser parser = null;
    private String nlText;
    private TextStructureTree textStructureTree;
    private edu.stanford.nlp.trees.Tree nlTree;


    public Map<String , String> word2class;

    private List<Feature> features;

    static SqlConnector conn ;
    static GraphDatabaseService graphDb ;
    static{
        String[] options = {};
        parser = LexicalizedParser.loadModel("nl parser models\\englishPCFG.ser.gz" , options);

        // region <connect to database>
        ResourceBundle bundle = ResourceBundle.getBundle("database");
        String url = bundle.getString("luceneAPI_url");
        String user = bundle.getString("luceneAPI_user");
        String pwd = bundle.getString("luceneAPI_pwd");
        String driver = bundle.getString("luceneAPI_driver");
        String table = bundle.getString("luceneAPI_table");

        conn = new SqlConnector(url , user , pwd , driver);
        //conn.start();
        String sql = "Select name from " + table + " where name = ? or stemmedName = ?";
        //conn.setPreparedStatement(sql);
        //conn.close();
        // endregion <connect to database>

        String graphDBPath = bundle.getString("graphdb");
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(graphDBPath));

    }

    /**
     * when nlText or parser change, we need to call update to update the content of this class instance
     */
    private void update(){
        features = null;

        assert(nlText.length() > 0);

        if(textStructureTree != null)
            textStructureTree = null;
        parseForNLTree();

        word2class = getClassDictionary(nlText);


    }

    public static void main(String[] args){
        NLParser p = new NLParser("Delete 1 document");
        p.getUniversalDependency();
        p.getTextStructureTree().print();
        String subject = p.getSubjectiveNoun();
        String verb = p.getVerb();
        List<String> nouns = p.getNonSubjectiveNoun();

        System.out.println("subject: " + subject);
        System.out.println("verb: " + verb);
        for(String noun : nouns){
            System.out.print(noun + " ");
        }

        List<MethodInvocationFeature> featur = p.getFeature_verbsToFeature();
        List<Feature> features = p.getFeatures();
        List<Dependency> de = p.getUniversalDependency();

    }

    public NLParser(String text){
        this.nlText = text;
        update();
    }

    public void setNlText(String text){
        this.nlText = text;
        update();
    }

    public void loadModel(String path){
        String[] options = {};
        parser = LexicalizedParser.loadModel(path , options);
        update();
    }

    /**
     * parseForNLTree will be called automatically in update, when some content of this class is changed
     */
    private void parseForNLTree(){
        try{
            nlTree = parser.parse(nlText);
        }catch(Exception e){
            e.printStackTrace();
            nlTree = null;
        }
    }

    public static Map<String , String> getClassDictionary(String text){
        String[] tokens = text.split("[^a-zA-Z_0-9]");
        Set<String> words = new HashSet<>();
        for(String token : tokens)
            words.add(token);
        return getPossibleClass(words);
    }

    public edu.stanford.nlp.trees.Tree getNLTree(){
        return nlTree;
    }

    public TextStructureTree getTextStructureTree(){
        if(textStructureTree == null){
            try{
                edu.stanford.nlp.trees.Tree textTree = getNLTree();
                textStructureTree = new TextStructureTree(0);
                textStructureTree.construct(new Sentence(nlText));
                return textStructureTree;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }else{
            return textStructureTree;
        }
    }

    private boolean contains(String[] set , String str){
        for(String s : set){
            if(s.compareTo(str) == 0)
                return true;
        }

        return false;
    }

    public String getVerb(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};
        String[] verbWordLevels = {"VB" , "VBD" , "VBG" , "VBN" , "VBP" , "VBZ"};


        String result = "";

        try{
            // get clause level child
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                for(Tree child : children){
                    label = child.label().toString();

                    // phrase level
                    if(label.compareTo("VP") == 0){
                        Tree[] vpChildren = child.children();
                        for(Tree vpChild : vpChildren){
                            label = vpChild.label().toString();
                            if(contains(verbWordLevels , label)){
                                result = vpChild.getLeaves().get(0).toString();
                                return result;
                            }
                        }
                    }
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;

    }



    public String getSubjectiveNoun(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};
        String[] nounWordLevels = {"NN" , "NNS" , "NNP" , "NNPS" };

        try{
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                for(Tree child : children){
                    label = child.label().toString();
                    if(label.compareTo("VP") == 0)
                        break;
                    else if(label.compareTo("NP") == 0){
                        Tree[] npChildren = child.children();
                        for(Tree npChild : npChildren){
                            label = npChild.label().toString();
                            if(contains(nounWordLevels , label)){
                                return npChild.getLeaves().get(0).toString();
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getNonSubjectiveNoun(){
        String[] clauseLevels = {"S" , "SBAR" , "SBARQ" , "SINV" , "SQ"};

        List<String> result = new ArrayList<>();

        try{
            edu.stanford.nlp.trees.Tree clauseTree = nlTree.children()[0];
            String label = clauseTree.label().toString();
            if(contains(clauseLevels , label)){
                Tree[] children = clauseTree.children();
                // 遇到动词之后的所有名词都会被返回
                boolean meetVerb = false;
                for(Tree child : children){
                    label = child.label().toString();
                    if(label.compareTo("VP") == 0){
                        meetVerb = true;
                    }else if(meetVerb){
                        result.addAll(findAllNouns(child));
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public List<String> findAllNouns(Tree tree){
        String[] nounWordLevels = {"NN" , "NNS" , "NNP" , "NNPS" };
        List<String> result = new ArrayList<>();
        String label = tree.label().toString();
        if(contains(nounWordLevels , label)){
            result.add(tree.getLeaves().get(0).toString());
        }else{
            if(tree.children().length > 0){
                Tree[] children = tree.children();
                for(Tree child : children){
                    result.addAll(findAllNouns(child));
                }
            }
        }

        return result;
    }

    public List<String> findAllVerb(Tree tree){
        String[] verbWordLevels = {"VB" , "VBD" , "VBG" , "VBN" , "VBP" , "VBZ"};
        List<String> result = new ArrayList<>();
        String label = tree.label().toString();
        if(contains(verbWordLevels , label)){
            result.add(tree.getLeaves().get(0).toString().toLowerCase());
        }else{
            if(tree.children().length > 0){
                Tree[] children = tree.children();
                for(Tree child : children){
                    result.addAll(findAllVerb(child));
                }
            }
        }
        return result;
    }

    public List<Dependency> getUniversalDependency(){
        return getTextStructureTree().dependencies;
    }

    public List<Feature> getFeatures(){
        /*if(features != null) return features;
        else features = new ArrayList<>();

        //region <how many class names are occurring in the text>
        Feature temp = getFeature_containClass();
        if(temp != null)
            features.add(temp);

        //endregion <how many class names are occurring in the text>
        List<MethodInvocationFeature> ltemp = getFeature_verbsToFeature();
        if(ltemp != null)
            features.addAll(ltemp);
        //region<find the verbs and class , relate verb to class>

        //endregion<find the verbs and class , relate verb to class>

        //region <feature base on universal dependency>
        List<Dependency> dependencies = getUniversalDependency();
        for(Dependency dependency : dependencies){
            String relationType = dependency.relation.shortName;
            if(relationType.compareTo( "nmod") == 0){
                Feature feature = getFeature_nmod(dependency);
                if(feature != null)
                    features.add(feature);
            }
        }
        //endregion <feature based on universal dependency>

        return features;*/
        return null;
    }

    ContainClassFeature getFeature_containClass(){
        boolean signal = false;
        ContainClassFeature result = new ContainClassFeature();

        if(word2class.size() > 0){
            for(String word : word2class.keySet())
                result.addClass(word2class.get(word));

            signal = true;
        }

        return signal ? result : null;
    }

    /**
     * the nl text will contain verbs and classes, so this function is try to find the verbs which can be related to some classes
     * in two ways:
     *      1.verb mays to a function , and one of the parameter's type is a class
     *      2.one class has a method which can may to a verb
     * @return
     */
    List<MethodInvocationFeature> getFeature_verbsToFeature(){
        return null;
        /*
        List<MethodInvocationFeature> result = new ArrayList<>();
        List<String> verbs = Stemmer.stem(findAllVerb(this.nlTree));

        Set<String> typeSet = new HashSet<>();
        for(String word : word2class.keySet()){
            typeSet.add(word2class.get(word));
        }

        try(Transaction tx = graphDb.beginTx()) {
            for (String verb : verbs) {
                String cypher = "Match (m:Method) where m.name = '" + verb +"' return m.params";
                Result parameters = graphDb.execute(cypher);
                Set<String> candidateTypes = new HashSet<>();

                while(parameters.hasNext()){
                    // params are in format : Type paraName,Type paraName ...
                    String[] types = parameters.next().get("m.params").toString().split(",");
                    for(String temp : types){
                        String type = temp.trim().split(" ")[0];
                        if(typeSet.contains(type) && !candidateTypes.contains(type)) {
                            MethodInvocationFeature feature = new MethodInvocationFeature();
                            candidateTypes.add(type);
                            feature.addIdentifierFeature(verb);
                            feature.addIdentifierFeature(type);
                            result.add(feature);
                        }else{
                            cypher = "Match (c1:Class)-[r1:extend]->(c2:Class)<-[r2:param]-(m:Method) where (c2.name = 'NAME1' and m.name = 'NAME2') return c1.name";
                            cypher = cypher.replace("NAME1" , type);
                            cypher = cypher.replace("NAME2" , verb);

                            Result children = graphDb.execute(cypher);
                            while(children.hasNext()){
                                type = children.next().get("c1.name").toString();
                                if(typeSet.contains(type) && !candidateTypes.contains(type)){
                                    MethodInvocationFeature feature = new MethodInvocationFeature();

                                    feature.addIdentifierFeature(verb);
                                    feature.addIdentifierFeature(type);
                                    result.add(feature);
                                    candidateTypes.add(type);
                                }
                            }

                        }
                    }
                }


            }

            for(String type: typeSet){
                for(String verb : verbs) {
                    String cypher = "Match (m:Method)-[]->(c:Class) where (m.name = '" + verb + "' and c.name = '" + type + "') return c";
                    if (graphDb.execute(cypher).hasNext()) {
                        MethodInvocationFeature feature = new MethodInvocationFeature();
                        feature.addIdentifierFeature(verb);
                        feature.addIdentifierFeature(type);
                        result.add(feature);
                    }
                }
            }
        }


        return result.size() > 0 ? result : null;*/
    }

    //region <get feature for different dependency>
    MethodInvocationFeature getFeature_nmod(Dependency dependency){
        boolean signal = false;

        MethodInvocationFeature result = new MethodInvocationFeature();

        Set<String> inspectors = new HashSet<>();
        Set<String> inspectees = new HashSet<>();



        /*
        if(word2class.containsKey(dependency.source.word)){
            result.addReturnFeature(word2class.get(dependency.source.word));
            signal = true;
        }

        if(word2class.containsKey(dependency.target.word)){
            result.addIdentifierFeature(word2class.get(dependency.target.word));
            signal = true;
        }*/
        //沒有加parameter

        return signal ? result : null;
    }
    //endregion <get feature for different dependency>


    /**
     * when words is passed into this function, it will try to find all possible class
     *
     * @param words :
     * @return is a map from String to String
     */
    public static Map<String , String> getPossibleClass(Set<String> words){
        Map<String , String> result = new HashMap<>();
        Map<String , String> resultNeedToTest = new HashMap<>();
        try {
            conn.start();
            int wordSize = words.size();
            if(wordSize > 0){
                Set<String> inspectors = new HashSet<>();
                Set<String> inspectees = new HashSet<>();
                String sql = "Select distinct name from api where type = 'CLASS' and ( NAMES )";
                String names = "1 = 2 ";
                for(String word : words){
                    names += "or name = '" + word +"' ";
                }
                conn.setPreparedStatement(sql.replace("NAMES" , names));

                ResultSet rs = conn.executeQuery();
                if(rs != null){
                    while(rs.next()){
                        String name = rs.getString(1);
                        result.put(name , name);
                        inspectors.add(name);
                        words.remove(name);
                    }
                }

                sql = "Select distinct name from api where type = 'CLASS' and stemmedName = ?";
                conn.setPreparedStatement(sql);
                for(String word : words){
                    conn.setString(1 , Stemmer.stemSingleWord(word));
                    rs = conn.executeQuery();
                    if(rs != null){
                        while(rs.next()) {
                            String name = rs.getString(1);
                            resultNeedToTest.put(word, name);
                            inspectees.add(name);
                        }
                    }
                }

                inspectees = possibleClassFilter(inspectors , inspectees);
                Iterator<Map.Entry<String , String>>it = resultNeedToTest.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry<String , String> entry = it.next();
                    if(!inspectees.contains(entry.getValue())){
                        it.remove();
                    }
                }

                result.putAll(resultNeedToTest);

            }

        }catch (Exception e){
            e.printStackTrace();
            result.clear();
        }finally {
            conn.close();
        }

        return result;
    }

    /**
     * when we use a group of words find some possible class, the result will contain some outliers
     * so, I need to find some way to filter these outliers
     * the way I take now is: there will be classes with 100% confidence, like the word BooleanQuery, it definitely match the class BooleanQuery,
     * so we use these 100% confidence classes to filter out candidate outliers, that is , if the candidate has no relation (extends) with any other class.
     * it will be filtered out
     * @return
     */
    static Set<String> possibleClassFilter(Set<String> inspectors, Set<String> inspectees ){
        Set<String> possibleClasses = new HashSet<>();

        try(Transaction tx = graphDb.beginTx()){
            String cypher = "Match (name1:Class)-[]->(name2:Class) ";
            cypher += "where (name1.name = 'NAME1' and name2.name = 'NAME2') or (name1.name = 'NAME2' and name2.name = 'NAME1') ";
            cypher += "return name1";

            for(String inspectee : inspectees){
                for(String inspector : inspectors){
                    Result result = graphDb.execute(cypher.replace("NAME1" , inspectee).replace("NAME2" , inspector));
                    if(result.hasNext()){
                        possibleClasses.add(inspectee);
                        break;
                    }
                }
            }
        }

        return possibleClasses;
    }

    public static List<String> splitIntoSentences(String string){
        if(string.trim().length() ==0 )
            return null;

        List<String> result = new ArrayList<>();
        Reader reader = new StringReader(string);
        DocumentPreprocessor dp = new DocumentPreprocessor(reader);
        for(List<HasWord> sentence : dp){
            result.add(SentenceUtils.listToString(sentence));
        }

        return result;
    }


}
