package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AperteMessageBundle {

    private List<ResourceBundle> errorCodeBundles;

    public AperteMessageBundle() {
        errorCodeBundles = new ArrayList<ResourceBundle>();
    }
    
    public String getMessage(String key) {
        for (ResourceBundle bundle : errorCodeBundles) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                // do nothing
            }
        }
        return key;
    }

    /**
     * Loads properties file from classpath and adds it to the message pool
     *
     * @param name Name of the classpath resource
     */
    public void addPropertiesFromClasspath(String name) {
        InputStream is = getClass().getResourceAsStream(name);
        if (is != null) {
            try {
                errorCodeBundles.add(new PropertyResourceBundle(is));
            } catch (IOException e) {
                // TODO use logger
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }
    
}
