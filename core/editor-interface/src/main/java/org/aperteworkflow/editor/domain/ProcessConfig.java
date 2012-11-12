package org.aperteworkflow.editor.domain;

import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Main configuration for the process editor application
 */
public class ProcessConfig implements Serializable {

    private List<Permission> processPermissions;
    private List<Queue> queues;
    
    private String taskItemClass;
    private String description;
    private String comment;
    private String dictionary;
    
    // This Map should be parametrized as <Language, String> however Jackson does not support
    // this out of the box, @see http://stackoverflow.com/questions/6371092/can-not-find-a-map-key-deserializer-for-type-simple-type-class-com-comcast-i
    // we use this dirty approach instead of registering custom module for Language class serialization
    private Map<String, String> messages;

    private byte[] processIcon;
    

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

    public byte[] getProcessIcon() {
        return processIcon;
    }

    public void setProcessIcon(byte[] processIcon) {
        this.processIcon = Lang2.noCopy(processIcon);
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}

	public String getTaskItemClass() {
		return taskItemClass;
	} 

	public void setTaskItemClass(String taskItemClass) {
		this.taskItemClass = taskItemClass;
	}

}
