package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.aperteworkflow.util.vaadin.VaadinUtility.getLocalizedMessage;

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

    private ProcessToolBpmSession bpmSession;
    private I18NSource messageSource;

    @ControllerMethod(action = "findProcessInstances")
    public GenericResultBean findProcessInstances(final OsgiWebRequest invocation) {

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        messageSource = requestContext.getMessageSource();
        bpmSession = requestContext.getBpmSession();

        Map<String, String[]> parameterMap = invocation.getRequest().getParameterMap();
        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(parameterMap);

        String searchCriteria = invocation.getRequest().getParameter("filter");
        Boolean isOnlyActive = "true".equals(invocation.getRequest().getParameter("onlyActive"));

        List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(invocation.getProcessToolContext().getProcessInstanceDAO()
                .searchProcesses(searchCriteria, dataTable.getPageOffset(), dataTable.getPageLength(), isOnlyActive, null, null));

        final List<ProcessInstanceBean> processInstanceBeans = createProcessInstanceBeansList(processInstances);
        DataPagingBean<ProcessInstanceBean> dataPagingBean = new DataPagingBean<ProcessInstanceBean>(processInstanceBeans, processInstanceBeans.size(), dataTable.getEcho());
        return dataPagingBean;
    }

    private List<ProcessInstanceBean> createProcessInstanceBeansList(List<ProcessInstance> processInstances) {
        final List<ProcessInstanceBean> processInstanceBeans = new ArrayList<ProcessInstanceBean>();
        for (ProcessInstance instance : processInstances) {
            List<ProcessInstanceBean> beans = ProcessInstanceBean.createBeans(instance, messageSource, bpmSession);
            processInstanceBeans.addAll(beans);
        }
        return processInstanceBeans;
    }

    @ControllerMethod(action = "cancelProcessInstance")
    public GenericResultBean cancelProcessInstance(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        bpmSession = requestContext.getBpmSession();
        bpmSession.adminCancelProcessInstance(invocation.getRequest().getParameter("processInstanceId"));
        return result;
    }

    @ControllerMethod(action = "showProcessMap")
    public GenericResultBean showProcessMap(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        bpmSession = requestContext.getBpmSession();

        //BufferedImage read;
        //final byte[] png = bpmSession.getProcessMapImage(pi); //todo - supply with processinstance

        return result;
    }

}
