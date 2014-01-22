package org.aperteworkflow.files.model;


import pl.net.bluesoft.util.lang.Formats;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDTO {
    private Long id;
    private Long processInstanceId;
    private String name;
    private String description;
    private String createDate;
    private String creatorLogin;

    public FilesRepositoryItemDTO(FilesRepositoryItem frItem) {
        setId(frItem.getId());
        setProcessInstanceId(frItem.getProcessInstance().getId());
        setName(frItem.getName());
        setDescription(frItem.getDescription());

        setCreateDate(Formats.formatFullDate(frItem.getCreateDate()));
        setCreatorLogin(frItem.getCreatorLogin());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreatorLogin() { return creatorLogin; }

    public void setCreatorLogin(String creatorLogin) { this.creatorLogin = creatorLogin; }
}
