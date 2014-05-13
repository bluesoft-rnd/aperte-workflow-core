package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryAttributes;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.FilesRepositoryProcessAttribute;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.util.*;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDAOImpl extends SimpleHibernateBean<IFilesRepositoryAttribute> implements FilesRepositoryItemDAO {

    public FilesRepositoryItemDAOImpl(Session session) {
        super(session);
    }

    @Override
    public FilesRepositoryItem addItem(IAttributesConsumer consumer, String name, String description, String relativePath, String contentType, String creatorLogin, FilesRepositoryAttributeFactory factory) {
        FilesRepositoryItem item = new FilesRepositoryItem();
        item.setName(name);
        item.setDescription(description);
        item.setRelativePath(relativePath);
        item.setContentType(contentType);
        item.setCreateDate(new Date());
        item.setCreatorLogin(creatorLogin);
        IFilesRepositoryAttribute attribute = (IFilesRepositoryAttribute) consumer.getAttribute(FilesRepositoryAttributes.FILES.value());
        if (attribute == null) {
            attribute = factory.create();
            attribute.setKey(FilesRepositoryAttributes.FILES.value());
            consumer.setAttribute(FilesRepositoryAttributes.FILES.value(), attribute);
        }
        attribute.getFilesRepositoryItems().add(item);
        getSession().saveOrUpdate(item);
        return item;
    }

    @Override
    public Collection<FilesRepositoryItem> getItemsFor(IAttributesProvider provider) {
        IFilesRepositoryAttribute filesAttribute = (IFilesRepositoryAttribute) provider.getAttribute(FilesRepositoryAttributes.FILES.value());
        if (filesAttribute == null)
            return new HashSet<FilesRepositoryItem>();
        getSession().refresh(filesAttribute);
        return filesAttribute.getFilesRepositoryItems();
    }

    @Override
    public void deleteById(IAttributesProvider provider, Long itemId) {
        FilesRepositoryItem item = getItemById(itemId);
        IFilesRepositoryAttribute filesAttribute = (IFilesRepositoryAttribute) provider.getAttribute(FilesRepositoryAttributes.FILES.value());
        filesAttribute.removeItem(item);
        getSession().delete(item);
    }

    @Override
    public void updateDescriptionById(Long id, String description) {
        FilesRepositoryItem item = getItemById(id);
        item.setDescription(description);
        getSession().saveOrUpdate(item);
    }

    @Override
    public FilesRepositoryItem getItemById(Long id) {
        return (FilesRepositoryItem) getSession().get(FilesRepositoryItem.class, id);
    }

}
