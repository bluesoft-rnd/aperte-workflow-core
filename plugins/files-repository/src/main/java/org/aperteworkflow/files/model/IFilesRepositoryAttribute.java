package org.aperteworkflow.files.model;

import java.util.Set;

/**
 * Created by pkuciapski on 2014-05-12.
 */
public interface IFilesRepositoryAttribute {
    void setKey(String key);

    public Set<FilesRepositoryItem> getFilesRepositoryItems();

    String getParentObjectPropertyName();

    Long getParentObjectId();

    Long getId();

    void removeItem(FilesRepositoryItem item);

}
