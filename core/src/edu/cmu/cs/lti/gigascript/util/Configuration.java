package edu.cmu.cs.lti.gigascript.util;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zhengzhongliu
 * Date: 2/25/14
 * Time: 1:17 AM
 */
public class Configuration {
    private final static Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    private File configFile;
    private Properties properties;


    public Configuration(File configurationFile) throws IOException {
        configFile = configurationFile;
        if (!configFile.exists()){
            throw new IOException("Cannot read config file at : "+configFile.getCanonicalPath());
        }
        properties = new Properties();
        properties.load(new FileInputStream(configurationFile));
    }

    public String getOrElse(String key, String defaultValue){
        String value = properties.getProperty(key,defaultValue);

        if (value == null){
            try {
                throw new ConfigurationException(key + " not specified in "+configFile.getCanonicalPath());
            } catch (ConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (key.endsWith(".dir")) {
            if (!value.endsWith("/")){
                value += "/";
            }
        }

        return value;
    }

    public String get(String key) {
        return getOrElse(key,null);
    }

    public boolean getBoolean(String key) {
        String value =  getOrElse(key,"true");
        return value.equals("true");
    }

    public int getInt(String key){
        String value = get(key);
        if (value == null){
            return 1;
        }
        return Integer.parseInt(value);
    }
}
