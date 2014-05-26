package org.aperteworkflow.files.widget.dataprovider;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryDataProvider implements IWidgetDataProvider {
    private static final String PROCESS_INSTANCE_FILES_PARAMETER = "processInstanceFiles";

    private static final String FILES_PARAMETER = "files";

    @Autowired
    protected IFilesRepositoryFacade filesRepoFacade;

    public FilesRepositoryDataProvider() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public Map<String, Object> getData(IAttributesProvider provider) {
        Map<String, Object> data = new HashMap<String, Object>();

        ProcessInstance processInstance = provider.getProcessInstance();
        if (processInstance != null)
            data.put(PROCESS_INSTANCE_FILES_PARAMETER, filesRepoFacade.getFilesList(processInstance));
        else
            data.put(FILES_PARAMETER, filesRepoFacade.getFilesList(provider));

        return data;
    }

}
