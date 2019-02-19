import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class ConfigurationParametersManager {

    private static final String CONFIGURATION_FILE =
        "ConfigurationParameters.properties";    

    private static Map parameters;
    
    static {

        try {
        
            Class configurationParametersManagerClass = 
                ConfigurationParametersManager.class;
            ClassLoader classLoader =
                configurationParametersManagerClass.getClassLoader();
            InputStream inputStream =
                classLoader.getResourceAsStream(CONFIGURATION_FILE);
            Properties properties = new Properties();
            properties.load(inputStream);
            inputStream.close();
            parameters = new HashMap(properties);
            
        } catch (Exception e) { 
            System.out.println("Error al leer el fichero de configuracion");
        }
        
    }
       
    private ConfigurationParametersManager() {}
    
    public static String getParameter(String name) {
    
        String value = (String) parameters.get(name);

        if (value == null) System.out.println("Parametro no encontrado"); 
        return value;
                                
    }     
     
}
