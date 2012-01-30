package pl.net.bluesoft.rnd.pt.ext.processeditor.json;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import java.util.ArrayList;
import java.util.List;

public class ProcessConfigJSONHandler {

    private static ProcessConfigJSONHandler instance;

    public static ProcessConfigJSONHandler getInstance() {
        if (instance == null) {
            instance = new ProcessConfigJSONHandler();
        }
        return instance;
    }
    
    
    public List<AbstractPermission> getPermissions(String json) {
        List<AbstractPermission> permissions = new ArrayList<AbstractPermission>();

        return permissions;
    }
        
    public String toJSON(List<AbstractPermission> permissions) {

        return "";
    }

}
