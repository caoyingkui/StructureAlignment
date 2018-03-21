import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by oliver on 2018/3/7.
 */
public class Test {

    public static void main(String[] args) {
        /*ASTParser parser = ASTParser.newParser(AST.JLS8);
        String code = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("code.txt")));
            String line;
            while((line = reader.readLine()) != null)
                code += ( line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);


        Block block = (Block) parser.createAST(null);*/




        String url = "jdbc:mysql://127.0.0.1:3306/lucene";
        String user = "root";
        String pwd = "woxnsk";
        String driver = "com.mysql.jdbc.Driver";

        SqlConnector conn = new SqlConnector(url , user , pwd , driver);
        conn = new SqlConnector(url , user , pwd , driver);
        conn.start();

        conn.setPreparedStatement("select name from api where type = 'CLASS'");

        ResultSet rs = conn.executeQuery();

        Map<String , Integer> map = new HashMap<>();

        try {
            while (rs.next()) {
                String name = rs.getString(1);
                if(!map.containsKey(name))
                    map.put(name , 1);
                else
                    map.put(name , map.get(name) + 1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        int count = 0 ;
        for(String name : map.keySet()){
            if(map.get(name) > 1){
                System.out.println(name + "  " + map.get(name));
                count ++;
            }
        }
        System.out.println(count);
     }

    @Override
    public String toString() {
        return "i like testing!";
    }
}
