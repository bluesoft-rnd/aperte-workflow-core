package org.aperteworkflow.webapi.main.ui;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.QueuesNameUtil;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Html builder for the task view 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class TaskViewBuilder 
{
	private BpmTask task;
	private List<ProcessStateWidget> widgets;
    private List<ProcessStateAction> actions;
    private String version;
    private String  description;
	private I18NSource i18Source;
	private UserData user;
    private ProcessToolContext ctx;
    private ProcessToolBpmSession bpmSession;
    private Collection<String> userQueues;
	
	@Autowired
	private ProcessToolRegistry processToolRegistry;
	
	@Autowired
	private IHtmlTemplateProvider templateProvider;

    @Autowired
    private IUserSource userSource;
	
	/** Builder for javascripts */
	private StringBuilder scriptBuilder = new StringBuilder(1024);
	
	private int vaadinWidgetsCount = 0;

    public TaskViewBuilder()
	{
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	public StringBuilder processView() throws IOException
    {
        StringBuilder stringBuilder = new StringBuilder();

		scriptBuilder.append("<script type=\"text/javascript\">");

		Document document = Jsoup.parse("");


		Element widgetsNode = document.createElement("div")
				.attr("id", "vaadin-widgets")
				.attr("class", "vaadin-widgets-view");
		document.appendChild(widgetsNode);

		for(ProcessStateWidget widget: widgets)
		{
            WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                    .setParent(widgetsNode)
                    .setWidget(widget)
                    .setProcessInstance(task.getProcessInstance())
                    .setForcePrivileges(false)
                    .setPrivileges(new ArrayList<String>());

			processWidget(childBean);
		}

        addActionButtons(document);

        addVersionNumber(document);

        stringBuilder.append(document.toString());
		
		scriptBuilder.append("vaadinWidgetsCount = ");
		scriptBuilder.append(vaadinWidgetsCount);
		scriptBuilder.append(';');
		scriptBuilder.append("</script>");
        stringBuilder.append(scriptBuilder);

        return  stringBuilder;
	}

    private void addVersionNumber(Document document) {
        Element versionNumber = document.createElement("div")
                .attr("id", "versionList")
                .attr("class", "process-version");
        document.appendChild(versionNumber);

        versionNumber.append(description + " v. " + version);
    }

    /** Add actions buttons compared to user privileges and process state */
    private void addActionButtons(Document document)
    {
        Element actionsNode = document.createElement("div")
                .attr("id", "actions-list")
                .attr("class", "actions-view");
        document.appendChild(actionsNode);

        Element genericActionButtons = document.createElement("div")
                .attr("id", "actions-generic-list")
                .attr("class", "btn-group  pull-left actions-generic-view");

        Element processActionButtons = document.createElement("div")
                .attr("id", "actions-process-list")
                .attr("class", "btn-group  pull-right actions-process-view");

        actionsNode.appendChild(genericActionButtons);
        actionsNode.appendChild(processActionButtons);

        /* Check if task is finished */
        if(isTaskFinished())
        {
            addCancelActionButton(genericActionButtons);
            return;
        }

        /* Check if task is from queue */
        if(isTaskHasNoOwner() && hasUserRightsToTask())
        {
            addClaimActionButton(processActionButtons);
        }

        /* Check if user, who is checking the task, is the assigned person */
        if(isUserAssignedToTask() || isSubstitutingUser())
        {
            addSaveActionButton(genericActionButtons);
            for(ProcessStateAction action: actions)
                processAction(action, processActionButtons);
        }

        addCancelActionButton(genericActionButtons);
    }


    private boolean isSubstitutingUser()
    {
        return ctx.getUserSubstitutionDAO().isSubstitutedBy(task.getAssignee(), user.getLogin());
    }
	
	private void processWidget(WidgetHierarchyBean widgetHierarchyBean)
	{
        ProcessStateWidget widget = widgetHierarchyBean.getWidget();
        ProcessInstance processInstance = widgetHierarchyBean.getProcessInstance();
        Element parent =  widgetHierarchyBean.getParent();

		String aliasName = widget.getClassName();

        ProcessHtmlWidget processHtmlWidget = processToolRegistry.getGuiRegistry().getHtmlWidget(aliasName);
		
		/* Sort widgets by prority */
		List<ProcessStateWidget> children = new ArrayList<ProcessStateWidget>(widget.getChildren());
		Collections.sort(children, new Comparator<ProcessStateWidget>() {
			@Override
			public int compare(ProcessStateWidget widget1, ProcessStateWidget widget2) {
				return widget1.getPriority().compareTo(widget2.getPriority());
			}
		});
		
		/* Check if widget is based on html */
		String widgetTemplateBody = templateProvider.getTemplate(aliasName);

        if(aliasName.equals("ShadowStateWidget"))
        {
             ProcessStateWidgetAttribute processStateConfigurationIdAttribute =
                     widget.getAttributeByName("processStateConfigurationId");

            ProcessStateWidgetAttribute forcePrivilegesAttribute =
                    widget.getAttributeByName("forcePrivileges");

            Boolean forcePrivileges = Boolean.parseBoolean(forcePrivilegesAttribute.getValue());

            String  attributeName = processStateConfigurationIdAttribute.getValue();
            String  processStateConfigurationId = processInstance.getRootProcessInstance().getSimpleAttributeValue(attributeName);

            ProcessStateConfiguration processStateConfiguration =
                    ctx.getProcessDefinitionDAO().getCachedProcessStateConfiguration(Long.parseLong(processStateConfigurationId));



            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", "vertical_layout"+widget.getId());
            parent.appendChild(divContentNode);

            for(ProcessStateWidget childWidget: processStateConfiguration.getWidgets())
            {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(childWidget)
                        .setProcessInstance(processInstance.getRootProcessInstance())
                        .setForcePrivileges(forcePrivileges)
                        .setPrivileges(getPrivileges(widget));

                processWidget(childBean);
            }


        }

		else if(aliasName.equals("TabSheet"))
		{
			String tabId = "tab_sheet_"+widget.getId();
			String divContentId = "div_content_"+widget.getId();

			Element ulNode = parent.ownerDocument().createElement("ul")
				.attr("id", tabId)
				.attr("class", "nav nav-tabs");
			parent.appendChild(ulNode);
			
			Element divContentNode = parent.ownerDocument().createElement("div")
				.attr("id", divContentId)
				.attr("class", "tab-content");
			parent.appendChild(divContentNode);
			
			boolean isFirst = true;

			for(ProcessStateWidget child: children)
			{
				String caption = aliasName;
				/* Set caption from attributes */
				ProcessStateWidgetAttribute attribute = child.getAttributeByName("caption");
				if(attribute != null)
					caption = i18Source.getMessage(attribute.getValue());
				
				String childId = "tab" + child.getId();
				
				/* Li tab element */
				Element liNode = parent.ownerDocument().createElement("li");
				ulNode.appendChild(liNode);
				
				Element aNode = parent.ownerDocument().createElement("a")
						.attr("id", "tab_link_"+childId)
						.attr("href", '#' +childId)
						.attr("data-toggle", "tab")
						.append(caption);
				
				liNode.appendChild(aNode);
				
				scriptBuilder.append("$('#tab_link_").append(childId).append("').on('shown', function (e) { onTabChange(e); });");
				
				/* Content element */
				Element divTabContentNode = parent.ownerDocument().createElement("div")
						.attr("id", childId)
						.attr("class", isFirst ? "tab-pane active" : "tab-pane");
				divContentNode.appendChild(divTabContentNode);
				
				if(isFirst)
					isFirst = false;

                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divTabContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());
				
				processWidget(childBean);
			}
			
			scriptBuilder.append("$('#").append(tabId).append(" a:first').tab('show');");
		}
		else if(aliasName.equals("VerticalLayout"))
		{
			Element divContentNode = parent.ownerDocument().createElement("div")
				.attr("id", "vertical_layout"+widget.getId());
			parent.appendChild(divContentNode);
			
			for(ProcessStateWidget child: children)
			{
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

				processWidget(childBean);
			}
		}
		else if(aliasName.equals("SwitchWidgets")){
			List<ProcessStateWidget> sortedList = new ArrayList<ProcessStateWidget>(children);
			
			ProcessStateWidget filteredChild = filterChildren(task, sortedList, widget);

			if (filteredChild != null) {
				Element divContentNode = parent.ownerDocument().createElement("div")
						.attr("id", "switch_widget" + widget.getId());
				parent.appendChild(divContentNode);

                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(filteredChild)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

				processWidget(childBean);
			}
		}
        /* HTML Widget */
		else if(processHtmlWidget != null)
		{
            Collection<String> privileges;
            if(widgetHierarchyBean.isForcePrivileges())
                privileges = widgetHierarchyBean.getPrivileges();
            else
                privileges = getPrivileges(widget);

            Map<String, Object> viewData = new HashMap<String, Object>();
			viewData.put(IHtmlTemplateProvider.PROCESS_PARAMTER, processInstance);
			viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
			viewData.put(IHtmlTemplateProvider.USER_PARAMTER, user);
            viewData.put(IHtmlTemplateProvider.USER_SOURCE_PARAMTER, userSource);
			viewData.put(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER, i18Source);
			viewData.put(IHtmlTemplateProvider.WIDGET_NAME_PARAMETER, aliasName);
			viewData.put(IHtmlTemplateProvider.PRIVILEGES_PARAMETER, privileges);
			viewData.put(IHtmlTemplateProvider.WIDGET_ID_PARAMETER, widget.getId().toString());
            viewData.put(IHtmlTemplateProvider.DICTIONARIES_DAO_PARAMETER, ctx.getProcessDictionaryDAO());
            viewData.put(IHtmlTemplateProvider.BPM_SESSION_PARAMETER, bpmSession);

            for(ProcessStateWidgetAttribute attribute: widget.getAttributes())
                viewData.put(attribute.getName(), attribute.getValue());

            /* Add custom attributes from widget data providers */
            for(IWidgetDataProvider dataProvider: processHtmlWidget.getDataProviders())
                viewData.putAll(dataProvider.getData(task));

			String processedView = templateProvider.processTemplate(aliasName, viewData);

			Element divContentNode = parent.ownerDocument().createElement("div")
					.append(processedView)
					.attr("class", "html-widget-view")
					.attr("id", "html-"+widget.getId());
				parent.appendChild(divContentNode);

			for(ProcessStateWidget child: children)
			{
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

				processWidget(childBean);
			}
		}
		else
		{
			vaadinWidgetsCount++;
			//http://localhost:8080
			String vaadinWidgetUrl = "/aperteworkflow/widget/"+task.getInternalTaskId()+"_"+widget.getId()+"/?widgetId=" + widget.getId() + "&taskId="+task.getInternalTaskId();
			 
			Element iFrameNode = parent.ownerDocument().createElement("iframe")
					.attr("src", vaadinWidgetUrl)
					.attr("autoResize", "true")
					.attr("id", "iframe-vaadin-"+widget.getId())
					.attr("frameborder", "0")
					.attr("taskId", task.getInternalTaskId())
					.attr("widgetId", widget.getId().toString())
					.attr("class", "vaadin-widget-view")
					.attr("widgetLoaded", "false")
					.attr("name", widget.getId().toString());
			parent.appendChild(iFrameNode);
			
			scriptBuilder.append("$('#iframe-vaadin-").append(widget.getId()).append("').load(function() {onLoadIFrame($(this)); });");

			for(ProcessStateWidget child: children)
			{
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(iFrameNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

				processWidget(childBean);
			}
		}
	}

    private static class WidgetHierarchyBean
    {
        private ProcessStateWidget widget;
        private Element parent;
        private ProcessInstance processInstance;
        private boolean forcePrivileges;
        private Collection<String> privileges;

        public ProcessStateWidget getWidget() {
            return widget;
        }

        public WidgetHierarchyBean setWidget(ProcessStateWidget widget) {
            this.widget = widget;
            return this;
        }

        public Element getParent() {
            return parent;
        }

        public WidgetHierarchyBean setParent(Element parent) {
            this.parent = parent;
            return this;
        }

        public ProcessInstance getProcessInstance() {
            return processInstance;
        }

        public WidgetHierarchyBean setProcessInstance(ProcessInstance processInstance) {
            this.processInstance = processInstance;
            return this;
        }

        public boolean isForcePrivileges() {
            return forcePrivileges;
        }

        public WidgetHierarchyBean setForcePrivileges(boolean forcePrivileges) {
            this.forcePrivileges = forcePrivileges;
            return this;
        }

        public Collection<String> getPrivileges() {
            return privileges;
        }

        public WidgetHierarchyBean setPrivileges(Collection<String> privileges) {
            this.privileges = privileges;
            return this;
        }
    }

    private Collection<String> getPrivileges(ProcessStateWidget widget)
    {
        Collection<String> privileges = new ArrayList<String>();

        if(!isUserAssignedToTask() || isTaskFinished())
            return privileges;

        for(ProcessStateWidgetPermission permission: widget.getPermissions())
        {
            if (permission.getRoleName().contains("*") || user.hasRole(permission.getRoleName())) {
				privileges.add(permission.getPrivilegeName());
			}
        }
        return privileges;
    }

	private void processAction(ProcessStateAction action, Element parent)
	{

		String actionButtonId = "action-button-" + action.getBpmName();

        String actionLabel = action.getLabel();
        if(actionLabel == null)
            actionLabel = "label";
        //TODO make autohide
        else if(actionLabel.equals("hide"))
            return;

        String actionType = action.getActionType();
        if(actionType == null || actionType.isEmpty())
            actionType = "primary";

        String iconName = action.getAttributeValue(ProcessStateAction.ATTR_ICON_NAME);
        if(iconName == null || iconName.isEmpty())
            iconName = "arrow-right";
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.attr("class", "btn btn-" + actionType)
				.attr("disabled", "true")
				.attr("type", "button")
				.attr("id", actionButtonId);
		parent.appendChild(buttonNode);



        Element actionButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-"+iconName);

        parent.appendChild(buttonNode);
        buttonNode.appendChild(actionButtonIcon);

        buttonNode.appendText(i18Source.getMessage(actionLabel));
			
		scriptBuilder
                .append("$('#")
                .append(actionButtonId)
                .append("').click(function() { disableButtons(); performAction(this, '")
                .append(action.getBpmName())
                .append("', ")
                .append(action.getSkipSaving())
                .append(", ")
                .append(action.getCommentNeeded())
                .append(", ")
                .append(action.getChangeOwner())
                .append(", '")
                .append(action.getChangeOwnerAttributeName())
                .append("', '")
                .append(task.getInternalTaskId())
                .append("');  });");
		scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(action.getDescription())).append("'});");
	}
	
	private void addSaveActionButton(Element parent)
	{
		String actionButtonId = "action-button-save";
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.attr("class", "btn btn-warning")
				.attr("disabled", "true")
				.attr("id", actionButtonId);

        Element saveButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-floppy-save");

		parent.appendChild(buttonNode);
        buttonNode.appendChild(saveButtonIcon);

        buttonNode.appendText(i18Source.getMessage("button.save.process.data"));
			
		scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { onSaveButton('").append(task.getInternalTaskId()).append("');  });");
		scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage("button.save.process.desc")).append("'});");
	}
	
	private void addCancelActionButton(Element parent)
	{
		String actionButtonId = "action-button-cancel";
		
		Element buttonNode = parent.ownerDocument().createElement("button")
				.attr("class", "btn btn-info")
				.attr("disabled", "true")
				.attr("id", actionButtonId);
		parent.appendChild(buttonNode);

        Element cancelButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-home");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(cancelButtonIcon);

        buttonNode.appendText(i18Source.getMessage("button.exit"));

		scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { onCancelButton();  });");
		scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage("button.exit")).append("'});");
	}

    private void addClaimActionButton(Element parent)
    {
        String actionButtonId = "action-button-claim";

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-warning")
                .attr("disabled", "true")
                .attr("id", actionButtonId);
        parent.appendChild(buttonNode);

        Element cancelButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-download");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(cancelButtonIcon);

        buttonNode.appendText(i18Source.getMessage("button.claim"));

        Long processStateConfigurationId = task.getCurrentProcessStateConfiguration().getId();

        scriptBuilder.append("$('#").append(actionButtonId)
                .append("').click(function() { claimTaskFromQueue('#action-button-claim','null', '")
                .append(processStateConfigurationId).append("','")
                .append(task.getInternalTaskId())
                .append("'); });")
                .append("$('#").append(actionButtonId)
                .append("').tooltip({title: '").append(i18Source.getMessage("button.claim.descrition")).append("'});");
    }

	public TaskViewBuilder setWidgets(List<ProcessStateWidget> widgets) 
	{
		this.widgets = widgets;
		return this;
	}

	public TaskViewBuilder setActions(List<ProcessStateAction> actions) 
	{
		this.actions = actions;
		return this;
	}
    public TaskViewBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskViewBuilder setVersion(String version) {
        this.version = version;
        return this;
    }
	
	public TaskViewBuilder setI18Source(I18NSource i18Source) 
	{
		this.i18Source = i18Source;
		return this;
	}

	public TaskViewBuilder setTask(BpmTask task) 
	{
		this.task = task;
		return this;
	}

	public TaskViewBuilder setUser(UserData user) 
	{
		this.user = user;
		return this;
	}

    public TaskViewBuilder setUserQueues(Collection<String> userQueues) {
        this.userQueues = userQueues;
        return this;
    }

    public TaskViewBuilder setCtx(ProcessToolContext ctx) {
        this.ctx = ctx;
        return this;
    }

    public ProcessStateWidget filterChildren(BpmTask task, List<ProcessStateWidget> sortedList, ProcessStateWidget psw) {
    	String selectorKey = psw.getAttributeByName("selectorKey").getValue();
    	String conditions = psw.getAttributeByName("conditions").getValue();
		String selectorValue = task.getProcessInstance().getInheritedSimpleAttributeValue(selectorKey);

		if(!hasText(selectorValue)) {
			return null;
		}
		
		String[] conditionsArray = conditions.split("[,; ]+");

		for (int i = 0; i < conditionsArray.length; i++) {
			if (selectorValue.equals(conditionsArray[i].trim())) {
				return i < sortedList.size() ? sortedList.get(i) : null;
			}
		}
		return null;
	}
    
    public TaskViewBuilder setBpmSession(ProcessToolBpmSession bpmSession) {
    	this.bpmSession = bpmSession;
    	return this;
    }

    private boolean hasUserRightsToTask()
    {
        if(task.getPotentialOwners().contains(user.getLogin()))
            return true;

        for(String queueName:  userQueues)
            if(task.getQueues().contains(queueName))
                return true;

        return false;
    }

    private boolean isTaskHasNoOwner()
    {
        return task.getAssignee() == null || task.getAssignee().isEmpty();
    }

    private boolean isTaskFinished()
    {
        return task.isFinished();
    }

    private boolean isUserAssignedToTask()
    {
        return user.getLogin().equals(task.getAssignee());
    }

}
