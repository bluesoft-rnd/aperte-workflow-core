package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryItem;

import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryItemDAO {
    FilesRepositoryItem addItem(Long processInstanceId, String name, String description, String relativePath, String contentType, String creatorLogin);

    Collection<FilesRepositoryItem> getItemsFor(Long processInstanceId);

    void deleteById(Long id);

    void updateDescriptionById(Long id, String description);

    FilesRepositoryItem getItemById(Long id);
}
