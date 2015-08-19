package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryItemDAO {
    FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath,
                                String contentType, String creatorLogin, FilesRepositoryAttributeFactory factory);

    FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath,
                                String contentType, String creatorLogin, Boolean sendAsEmail, String groupId, FilesRepositoryAttributeFactory factory);
    Collection<FilesRepositoryItem> getItemsFor(IAttributesProvider provider);

    void deleteById(IAttributesProvider provider, Long itemId);

    void updateDescription(IFilesRepositoryItem item, String description);

    FilesRepositoryItem getItemById(Long id);

    void updateSendWithMail(IFilesRepositoryItem item, Boolean sendWithMail);

	boolean hasAnyFileWithName(String relativePath);
}
