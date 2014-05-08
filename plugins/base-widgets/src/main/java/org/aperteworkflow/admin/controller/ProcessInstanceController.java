package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Process Instance operations controller for admin portlet.
 *
 * @author: lgajowy@bluesoft.net.pl
 */

@OsgiController(name = "processInstanceController")
public class ProcessInstanceController implements IOsgiWebController {

    @Autowired
    protected IPortalUserSource portalUserSource;
    @Autowired
    protected ProcessToolRegistry processToolRegistry;

    private Logger logger = Logger.getLogger(ProcessInstanceController.class.getName());

    private String filterCriteria = "";
    private Boolean onlyActive = false;
    private Map<String,String[]> parameterMap;

    @ControllerMethod(action = "findProcessInstances")
    public GenericResultBean findProcessInstances(final OsgiWebRequest invocation) {

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        I18NSource messageSource = requestContext.getMessageSource();
        parameterMap = invocation.getRequest().getParameterMap();
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(parameterMap);

        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(invocation.getProcessToolContext().getProcessInstanceDAO()
                .searchProcesses(filterCriteria, dataTable.getPageOffset(), dataTable.getPageLength(), onlyActive, null, null)); //todo: filter, onlyActive

        final List<ProcessInstanceBean> processInstanceBeans = createProcessInstanceBeansList(messageSource, processInstances);
        DataPagingBean<ProcessInstanceBean> dataPagingBean = new DataPagingBean<ProcessInstanceBean>(processInstanceBeans, processInstanceBeans.size(), dataTable.getEcho());
        return dataPagingBean;
    }

    private List<ProcessInstanceBean> createProcessInstanceBeansList(I18NSource messageSource, List<ProcessInstance> processInstances) {
        final List<ProcessInstanceBean> processInstanceBeans = new ArrayList<ProcessInstanceBean>();
        for (ProcessInstance instance : processInstances) {
            ProcessInstanceBean instanceBean = ProcessInstanceBean.createFrom(instance, messageSource);
            processInstanceBeans.add(instanceBean);
        }
        return processInstanceBeans;
    }
}
