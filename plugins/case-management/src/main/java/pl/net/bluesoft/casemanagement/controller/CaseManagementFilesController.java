package pl.net.bluesoft.casemanagement.controller;

import org.aperteworkflow.files.controller.AbstractFilesController;
import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import pl.net.bluesoft.casemanagement.dao.CaseDAOImpl;
import pl.net.bluesoft.casemanagement.dao.FilesRepositoryCaseAttributeFactoryImpl;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;

/**
 * Created by pkuciapski on 2014-05-13.
 */
@OsgiController(name = "casefilescontroller")
public class CaseManagementFilesController extends AbstractFilesController {
    @Override
    protected IAttributesConsumer getAttributesConsumer(Long id) {
        return new CaseDAOImpl(getSession()).getCaseById(id);
    }

    @Override
    protected IAttributesProvider getAttributesProvider(Long id) {
        return getAttributesConsumer(id);
    }

    private org.hibernate.Session getSession() {
        return ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
    }

    @Override
    protected FilesRepositoryAttributeFactory getAttributesFactory() {
        return new FilesRepositoryCaseAttributeFactoryImpl();
    }
}

