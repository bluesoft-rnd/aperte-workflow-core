package org.aperteworkflow.files.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author pwysocki@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_files_repository_item")
@org.hibernate.annotations.Table(
        appliesTo = "pt_files_repository_item",
        indexes = {
                @Index(name = "idx_pt_repository_item_pk",
                        columnNames = {"id"}
                )
        })
public class FilesRepositoryItem implements IFilesRepositoryItem {
    public static final String COLUMN_PROCESS_INSTANCE_ID = "process_instance_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_RELATIVE_PATH = "relative_path";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CREATE_DATE = "create_date";
    public static final String COLUMN_CREATOR_LOGIN = "creator_login";
    public static final String COLUMN_CONTENT_TYPE = "content_type";
    public static final String COLUMN_SEND_WITH_MAIL = "send_with_mail";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="db_seq_pt_files_repository_item")
	@SequenceGenerator(name="db_seq_pt_files_repository_item", sequenceName="db_seq_pt_files_repository_item", allocationSize=1)
	@Column(name = "id")
	protected Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    @Column(name = COLUMN_NAME, nullable = false)
    @Index(name = "idx_pt_files_name")
    private String name;

    @Column(name = COLUMN_RELATIVE_PATH, nullable = false)
    private String relativePath;

    @Column(name = COLUMN_DESCRIPTION)
    private String description;

    @Column(name = COLUMN_CONTENT_TYPE, nullable = false)
    private String contentType;

    @Column(name = COLUMN_SEND_WITH_MAIL)
    private Boolean sendWithMail = false;

    @Column(name = COLUMN_CREATE_DATE, nullable = false)
    private Date createDate;

    @Column(name = COLUMN_CREATOR_LOGIN, nullable = false)
    @Index(name = "idx_pt_files_creator_login")
    private String creatorLogin;

	@Column(name = "att_entity_type", length = 10)
	private String attachedEntityType;

	@Column(name = "att_entity_id")
	private Long attachedEntityId;

	@Column(name = "group_id", length = 20)
	private String groupId;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getRelativePath() {
		return relativePath;
	}

	@Override
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public Boolean getSendWithMail() {
		return sendWithMail != null ? sendWithMail : false;
	}

	@Override
	public void setSendWithMail(Boolean sendWithMail) {
		this.sendWithMail = sendWithMail;
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Override
	public String getCreatorLogin() {
		return creatorLogin;
	}

	public void setCreatorLogin(String creatorLogin) {
		this.creatorLogin = creatorLogin;
	}

	public String getAttachedEntityType() {
		return attachedEntityType;
	}

	public void setAttachedEntityType(String attachedEntityType) {
		this.attachedEntityType = attachedEntityType;
	}

	public Long getAttachedEntityId() {
		return attachedEntityId;
	}

	public void setAttachedEntityId(Long attachedEntityId) {
		this.attachedEntityId = attachedEntityId;
	}

	@Override
	public void attachToEntity(String entityType, Long entityId) {
		this.attachedEntityType = entityType;
		this.attachedEntityId = entityId;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
}
