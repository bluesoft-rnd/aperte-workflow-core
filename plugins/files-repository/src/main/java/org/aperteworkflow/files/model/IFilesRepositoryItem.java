package org.aperteworkflow.files.model;

import java.util.Date;

/**
 * Created by pkuciapski on 2014-05-09.
 */
public interface IFilesRepositoryItem {
    Long getId();

    String getName();

    String getDescription();

    Date getCreateDate();

    String getCreatorLogin();

    void setName(String s);

    void setRelativePath(String s);

    void setDescription(String s);

    String getRelativePath();

    String getContentType();

    void setContentType(String contentType);

    void setSendWithMail(Boolean sendWithMail);

    Boolean getSendWithMail();

	String getAttachedEntityType();
	void setAttachedEntityType(String attachedEntityType);
	Long getAttachedEntityId();
	void setAttachedEntityId(Long attachedEntityId);

	void attachToEntity(String entityType, Long entityId);

    String getGroupId();
    void setGroupId(String groupId);
}
