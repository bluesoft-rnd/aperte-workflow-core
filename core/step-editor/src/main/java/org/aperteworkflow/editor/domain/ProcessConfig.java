package org.aperteworkflow.editor.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Main configuration for the process editor application
 */
public class ProcessConfig implements Serializable {

    private List<Permission> processPermissions;
    private List<Queue> queues;
    private String messages;

    public List<Permission> getProcessPermissions() {
        return processPermissions;
    }

    public void setProcessPermissions(List<Permission> processPermissions) {
        this.processPermissions = processPermissions;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
}
