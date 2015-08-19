package org.aperteworkflow.admin.controller;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import static pl.net.bluesoft.util.lang.FormatUtil.formatFullDate;

public class ProcessDefinition {
	private Long id;
	private String description;
	private String bpmDefinitionKey;
	private String comment;
	private int bpmDefinitionVersion;
	private String creatorLogin;
	private String createDate;
	private boolean enabled;

	public ProcessDefinition(ProcessDefinitionConfig processDefinitionConfig) {
		id = processDefinitionConfig.getId();
		description = processDefinitionConfig.getDescription();
		bpmDefinitionKey = processDefinitionConfig.getBpmDefinitionKey();
		comment = processDefinitionConfig.getComment();
		bpmDefinitionVersion = processDefinitionConfig
				.getBpmDefinitionVersion();
		creatorLogin = processDefinitionConfig.getCreatorLogin();
		createDate = formatFullDate(processDefinitionConfig.getCreateDate());
		enabled = processDefinitionConfig.isEnabled();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBpmDefinitionKey() {
		return bpmDefinitionKey;
	}

	public void setBpmDefinitionKey(String bpmDefinitionKey) {
		this.bpmDefinitionKey = bpmDefinitionKey;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getBpmDefinitionVersion() {
		return bpmDefinitionVersion;
	}

	public void setBpmDefinitionVersion(int bpmDefinitionVersion) {
		this.bpmDefinitionVersion = bpmDefinitionVersion;
	}

	public String getCreatorLogin() {
		return creatorLogin;
	}

	public void setCreatorLogin(String creatorLogin) {
		this.creatorLogin = creatorLogin;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	

}
