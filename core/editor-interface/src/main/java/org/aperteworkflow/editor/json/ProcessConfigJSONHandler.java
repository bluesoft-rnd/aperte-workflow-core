package org.aperteworkflow.editor.json;

import org.apache.commons.codec.binary.Base64;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ProcessConfigJSONHandler implements Serializable {

    /**
     * Charset used when dealing with messages saved in .properties files
     */
    private static final String MESSAGES_CHARSET = "US-ASCII";

    /**
     * Singleton
     */
    private static ProcessConfigJSONHandler instance;

    /**
     * Singleton access
     * @return instance
     */
    public static synchronized ProcessConfigJSONHandler getInstance() {
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
            // encode fields with problematic characters as Base64
            if (processConfig.getComment() != null) {
                byte[] bytes = processConfig.getComment().getBytes();
                processConfig.setComment(new String(Base64.encodeBase64URLSafe(bytes)));
            }
            if (processConfig.getMessages() != null) {
                for (String langCode : processConfig.getMessages().keySet()) {
                    String msg = processConfig.getMessages().get(langCode);
                    if (msg != null) {
                        byte[] bytes = msg.getBytes(MESSAGES_CHARSET);
                        msg = new String(Base64.encodeBase64URLSafe(bytes), MESSAGES_CHARSET);
                        processConfig.getMessages().put(langCode, msg);
                    }
                }
            }
            if (processConfig.getDictionary() != null) {
                byte[] dictionaryInBytes = processConfig.getDictionary().getBytes();
                processConfig.setDictionary(new String(Base64.encodeBase64URLSafe(dictionaryInBytes)));
            }

            // encode as JSON
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
            // decode from JSON
            ProcessConfig processConfig = mapper.readValue(json, ProcessConfig.class);

            // decode Base64 encoded fields
            if (processConfig.getComment() != null) {
                byte[] bytes = processConfig.getComment().getBytes();
                processConfig.setComment(new String(Base64.decodeBase64(bytes)));
            }
            if (processConfig.getMessages() != null) {
                for (String langCode : processConfig.getMessages().keySet()) {
                    String msg = processConfig.getMessages().get(langCode);
                    if (msg != null) {
                        byte[] bytes = msg.getBytes(MESSAGES_CHARSET);
                        msg = new String(Base64.decodeBase64(bytes), MESSAGES_CHARSET);
                        processConfig.getMessages().put(langCode, msg);
                    }
                }
            }
            if (processConfig.getDictionary() != null) {
                byte[] codedDictionaryInBytes = processConfig.getDictionary().getBytes();
                processConfig.setDictionary(new String(Base64.decodeBase64(codedDictionaryInBytes)));
            }

            return processConfig;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read ProcessConfig from JSON", e);
        }
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        mapper = new ObjectMapper();
    }

}
