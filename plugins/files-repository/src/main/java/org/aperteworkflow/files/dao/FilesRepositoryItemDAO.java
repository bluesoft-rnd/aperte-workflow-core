package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.IFilesRepositoryItem;

import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryItemDAO {
    IFilesRepositoryItem addItem(Long processInstanceId, String name, String description, String relativePath, String contentType, String creatorLogin);

    Collection<? extends IFilesRepositoryItem> getItemsFor(Long parentObjectId);

    void deleteById(Long id);

    void updateDescriptionById(Long id, String description);

    IFilesRepositoryItem getItemById(Long id);
}
