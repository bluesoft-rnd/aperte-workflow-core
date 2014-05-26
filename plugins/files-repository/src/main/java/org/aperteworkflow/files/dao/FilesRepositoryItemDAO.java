package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryItemDAO {
    FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath, String contentType, String creatorLogin, FilesRepositoryAttributeFactory factory);

    Collection<FilesRepositoryItem> getItemsFor(IAttributesProvider provider);

    void deleteById(IAttributesProvider provider, Long itemId);

    void updateDescriptionById(Long itemId, String description);

    FilesRepositoryItem getItemById(Long id);
}
