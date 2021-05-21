package dotenvparser;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * ConfigEnv : retrieve env variable from an xml file default to "./.env.xml" 
 * if no path is given to the constructor as an argument
 *  the envirenment variables are read using the get methode.
 * USAGE :
 * Map<String,String> cfe = ConfigEnv.load_variables(path);
 * String port = cfe.get("port");
 */
public class ConfigEnv {
    /** 
     * the load_variables(String path) private method return the variables found 
     * in a well constructed .env.xml file containgin a config tag:
     * <config>
     * <variable_0>value_0</variable_0>
     * <variable_1>value_1</variable_1>
     * .
     * .
     * .
     * <variable_n>value_n</variable_n>
     * </config>
    */
    public static Map<String,String> load_variables(String path) throws Exception {
        Map<String,String> env = new HashMap<>();

        InputStream xmlenv = new FileInputStream(path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document env_vars = db.parse(xmlenv);
        
        env_vars.getDocumentElement().normalize();
        
        NodeList variables = env_vars.getElementsByTagName("config").item(0).getChildNodes();
        for(int i=0 ; i< variables.getLength();i++){
            if (variables.item(i).getNodeName().equals("GEST")){
                NodeList gestionnairies =  env_vars.getElementsByTagName("ADDR_GEST");
                env.put("N_GEST", String.valueOf(gestionnairies.getLength()));
                for (int j=0;j<gestionnairies.getLength();j++){
                    var elem = gestionnairies.item(j).getChildNodes();
                    env.put("GEST_ADDR"+String.valueOf(j),elem.item(1).getTextContent()+":"+elem.item(3).getTextContent());
                }
            }else
            env.put(variables.item(i).getNodeName(),variables.item(i).getTextContent());
        }
        // System.out.println(env.get("GEST_ADDR1"));
        return env;
    }
}