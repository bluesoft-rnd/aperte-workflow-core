package org.aperteworkflow.files.controller;

import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.dao.FilesRepositoryProcessAttributeFactoryImpl;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

@OsgiController(name = "filescontroller")
public class FilesController extends AbstractFilesController implements IOsgiWebController {

    @Override
    protected IAttributesConsumer getAttributesConsumer(Long id) {
        return getThreadProcessToolContext().getProcessInstanceDAO().getProcessInstance(id);
    }

    @Override
    protected IAttributesProvider getAttributesProvider(Long id) {
        return getThreadProcessToolContext().getProcessInstanceDAO().getProcessInstance(id);
    }

    @Override
    protected FilesRepositoryAttributeFactory getAttributesFactory() {
        return FilesRepositoryProcessAttributeFactoryImpl.INSTANCE;
    }
}