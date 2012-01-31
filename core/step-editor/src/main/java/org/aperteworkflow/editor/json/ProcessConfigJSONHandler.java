package org.aperteworkflow.editor.json;

import org.aperteworkflow.editor.domain.ProcessConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ProcessConfigJSONHandler implements Serializable {

    private static ProcessConfigJSONHandler instance;

    /**
     * Singleton access
     * @return instance
     */
    public static ProcessConfigJSONHandler getInstance() {
        if (instance == null) {
            instance = new ProcessConfigJSONHandler();
        }
        return instance;
    }

    // Look out, ObjectMapper is not Serializable
    private transient ObjectMapper mapper = new ObjectMapper();

    protected ProcessConfigJSONHandler() {

    }

    public String toJSON(ProcessConfig processConfig) {
        if (processConfig == null) {
            return "";
        }

        try {
            return mapper.writeValueAsString(processConfig);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write ProcessConfig to JSON", e);
        }
    }
    
    public ProcessConfig toObject(String json) {
        if (json == null) {
            return new ProcessConfig();
        }

        try {
            return mapper.readValue(json, ProcessConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ProcessConfig from JSON", e);
        }
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        mapper = new ObjectMapper();
    }

}
