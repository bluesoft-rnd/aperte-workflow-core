package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableColumn;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Process Instance operations controller for admin portlet.
 *
 * @author: lgajowy@bluesoft.net.pl
 */

@OsgiController(name = "processInstanceController")
public class ProcessInstanceController implements IOsgiWebController {

    private Logger logger = Logger.getLogger(ProcessInstanceController.class.getName());

    @Autowired
    protected IPortalUserSource portalUserSource;

    @Autowired
    protected ProcessToolRegistry processToolRegistry;
    private Map<String, String[]> parameterMap;

    @ControllerMethod(action = "findProcessInstances")
    public GenericResultBean findProcessInstances(final OsgiWebRequest invocation) {

        IProcessToolRequestContext requestContext = invocation.getProcessToolRequestContext();
        ProcessToolContext context = invocation.getProcessToolContext();

        JQueryDataTable dataTable = JQueryDataTableUtil.analyzeRequest(invocation.getRequest().getParameterMap());
        JQueryDataTableColumn sortingColumn = dataTable.getFirstSortingColumn();

        List<ProcessInstance> processInstances;
        processInstances = new ArrayList<ProcessInstance>(getThreadProcessToolContext().getProcessInstanceDAO()
                .searchProcesses("a", dataTable.getPageOffset(),dataTable.getPageLength(), false, null, null)); //todo: filter, onlyRunning

        DataPagingBean<ProcessInstance> dataPagingBean =
                new DataPagingBean<ProcessInstance>(processInstances, processInstances.size(), dataTable.getEcho());

        return dataPagingBean;
    }
}
