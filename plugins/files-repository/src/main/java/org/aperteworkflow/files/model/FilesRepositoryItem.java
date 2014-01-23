package org.aperteworkflow.files.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;

/**
 * @author pwysocki@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_files_repository_item")
@org.hibernate.annotations.Table(
        appliesTo="pt_files_repository_item",
        indexes = {
                @Index(name = "idx_pt_repository_item_pk",
                        columnNames = {"id"}
                )
        })
public class FilesRepositoryItem extends PersistentEntity {

    public static final String COLUMN_PROCESS_INSTANCE_ID = "process_instance_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_RELATIVE_PATH = "relative_path";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATE_DATE = "create_date";
    public static final String COLUMN_CREATOR_LOGIN = "creator_login";
    public static final String COLUMN_CONTENT_TYPE = "content_type";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = COLUMN_PROCESS_INSTANCE_ID)
    private ProcessInstance processInstance;

    @Column(name = COLUMN_NAME, nullable = false)
    @Index(name="idx_pt_files_name")
    private String name;

    @Column(name = COLUMN_RELATIVE_PATH, nullable = false)
    private String relativePath;

    @Column(name = COLUMN_DESCRIPTION)
    private String description;

    @Column(name = COLUMN_CONTENT_TYPE, nullable = false)
    private String contentType;

    @Column(name = COLUMN_CREATE_DATE, nullable = false)
    private Date createDate;

    @Column(name = COLUMN_CREATOR_LOGIN, nullable = false)
    @Index(name="idx_pt_files_creator_login")
    private String creatorLogin;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCreatorLogin() {
        return creatorLogin;
    }

    public void setCreatorLogin(String creatorLogin) {
        this.creatorLogin = creatorLogin;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
