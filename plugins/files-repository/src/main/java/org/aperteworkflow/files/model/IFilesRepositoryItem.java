package org.aperteworkflow.files.model;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

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

    Long getParentObjectId();

    void setName(String s);

    void setRelativePath(String s);

    void setDescription(String s);

    String getRelativePath();

    String getContentType();

    void setParentObject(IAttributesProvider parentObject);

    IAttributesProvider getParentObject();

    void setContentType(String contentType);
}
