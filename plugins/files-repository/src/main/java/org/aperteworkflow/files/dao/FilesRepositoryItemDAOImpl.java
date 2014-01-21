package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Collection;
import java.util.Date;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryItemDAOImpl extends SimpleHibernateBean<FilesRepositoryItem> implements FilesRepositoryItemDAO {

    private ProcessInstanceDAO processInstanceDAO;


    public FilesRepositoryItemDAOImpl(Session session, ProcessInstanceDAO processInstanceDAO) {
        super(session);
        this.processInstanceDAO = processInstanceDAO;
    }

    public FilesRepositoryItemDAOImpl(Session session) {
        this(session, ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO());
    }

    @Override
    public FilesRepositoryItem addItem(Long processInstanceId, String name, String description, String relativePath, String creatorLogin) {
        return addItem(processInstanceDAO.getProcessInstance(processInstanceId), name, description, relativePath, creatorLogin);
    }

    public FilesRepositoryItem addItem(ProcessInstance processInstance, String name, String description, String relativePath, String creatorLogin) {
        FilesRepositoryItem item = new FilesRepositoryItem();
        item.setProcessInstance(processInstance);
        item.setName(name);
        item.setDescription(description);
        item.setRelativePath(relativePath);
        item.setCreateDate(new Date());
        item.setCreatorLogin(creatorLogin);
        saveOrUpdate(item);
        return item;
    }

    @Override
    public Collection<FilesRepositoryItem> getItemsFor(Long processInstanceId) {
        DetachedCriteria criteria = getDetachedCriteria();
        criteria.createAlias("processInstance", "pi")
                .add(Restrictions.eq("pi.id", processInstanceId));
        return criteria.getExecutableCriteria(getSession()).list();
    }

    @Override
    public void deleteById(Long id) {
        delete(getItemById(id));
    }

    @Override
    public void updateDescriptionById(Long id, String description) {
        FilesRepositoryItem item = getItemById(id);
        item.setDescription(description);
        saveOrUpdate(item);
    }

    @Override
    public FilesRepositoryItem getItemById(Long id) {
        return loadById(id);
    }

    public ProcessInstanceDAO getProcessInstanceDAO() {
        return processInstanceDAO;
    }

    public void setProcessInstanceDAO(ProcessInstanceDAO processInstanceDAO) {
        this.processInstanceDAO = processInstanceDAO;
    }
}
