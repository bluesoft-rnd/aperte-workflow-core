package org.aperteworkflow.files.widget.dataprovider;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryDataProvider implements IWidgetDataProvider
    {
        private static final String PROCESS_INSTANCE_FILES_PARAMETER = "processInstanceFiles";

        @Autowired
        protected IFilesRepositoryFacade filesRepoFacade;

        public FilesRepositoryDataProvider()
        {
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        }

        @Override
        public Map<String, Object> getData(BpmTask task)
        {
            Map<String, Object> data = new HashMap<String, Object>();

            ProcessInstance processInstance = task.getProcessInstance();

            data.put(PROCESS_INSTANCE_FILES_PARAMETER, filesRepoFacade.getFilesList(processInstance.getId()));

            return data;
        }

}
