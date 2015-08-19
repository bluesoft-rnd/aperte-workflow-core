package org.aperteworkflow.admin.controller;

import org.aperteworkflow.ui.help.datatable.JQueryDataTable;
import org.aperteworkflow.ui.help.datatable.JQueryDataTableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.DataPagingBean;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * 
 * Substitution operations controller for admin portlet
 * 
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name = "processDefinitionsController")
public class ProcessDefinitionsController implements IOsgiWebController {
	@Autowired
	protected IPortalUserSource portalUserSource;

	@Autowired
	protected ProcessToolRegistry processToolRegistry;

	@ControllerMethod(action = "loadProcessDefinitions")
	public GenericResultBean loadProcessDefinitions(
			final OsgiWebRequest invocation) {


		JQueryDataTable dataTable = JQueryDataTableUtil
				.analyzeRequest(invocation.getRequest().getParameterMap());


		ProcessDefinitionDAO dao = getThreadProcessToolContext()
				.getProcessDefinitionDAO();
		List<ProcessDefinitionConfig> latestConfigurations = new ArrayList<ProcessDefinitionConfig>(
				dao.getActiveConfigurations());
		Collections.sort(latestConfigurations,
				ProcessDefinitionConfig.DEFAULT_COMPARATOR);

		List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();

		for (ProcessDefinitionConfig processDefinitionConfig : latestConfigurations) {
			definitions.add(new ProcessDefinition(processDefinitionConfig));
		}

		DataPagingBean<ProcessDefinition> dataPagingBean = new DataPagingBean<ProcessDefinition>(
				definitions, definitions.size(), dataTable.getDraw());

		return dataPagingBean;
	}


}