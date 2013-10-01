package org.aperteworkflow.editor.domain;

import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Main configuration for the process editor application
 */
public class ProcessConfig implements Serializable {
    private List<Permission> processPermissions;
    private List<Queue> queues;

    private String description;
    private String comment;
    private String dictionary;
    private String version;
    
    // This Map should be parametrized as <Language, String> however Jackson does not support
    // this out of the box, @see http://stackoverflow.com/questions/6371092/can-not-find-a-map-key-deserializer-for-type-simple-type-class-com-comcast-i
    // we use this dirty approach instead of registering custom module for Language class serialization
    private Map<String, String> messages;

    private byte[] processIcon;
	private String defaultLanguage;
	private String defaultStepInfo;


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
    // TODO: Remove after tests
	public String getDictionary() {
		return dictionary;
	}
    // TODO: Remove after tests
	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public String getDefaultStepInfo() {
		return defaultStepInfo;
	}

	public void setDefaultStepInfo(String defaultStepInfo) {
		this.defaultStepInfo = defaultStepInfo;
	}

	public Set<String> getUsedLanguages() {
		Set<String> result = new TreeSet<String>();

		for (String locale : messages.keySet()) {
			result.add(locale.split("_")[0].trim().toLowerCase());
		}
		return result;
	}
}
