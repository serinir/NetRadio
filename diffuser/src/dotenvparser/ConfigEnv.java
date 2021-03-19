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
 * ConfigEnv cfe = new ConfigEnv();
 * String port = cfe.get("port");
 */
public class ConfigEnv {
    private String path;
    private Map<String,String> env;

    public ConfigEnv(String path){
        this.path = path;
        try {
            load_variable();
        } catch (Exception e) { 
            this.env = null; //Maybe we need to manage this otherwise
        }
    }

    public ConfigEnv(){
        this.path = "./.env";
        try {
            load_variable();
        } catch (Exception e) {
           this.env = null;//Maybe we need to manage this otherwise
        }
    }
    /** 
     * the load_variable() private method load the variables found 
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
    private void load_variable() throws Exception {
        this.env = new HashMap<>();

        InputStream xmlenv = new FileInputStream(this.path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document env = db.parse(xmlenv);
        
        env.getDocumentElement().normalize();
        
        NodeList variables = env.getElementsByTagName("config").item(0).getChildNodes();
        for(int i=0 ; i< variables.getLength();i++){
            this.env.put(variables.item(i).getNodeName(),variables.item(i).getTextContent());
        }
    }
    //just a getter or an encupsaltion method...kinda the same tho.
    public String get(String variable){
        return this.env.get(variable);
    }
}