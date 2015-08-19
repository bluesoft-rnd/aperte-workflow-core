package org.aperteworkflow.webapi.main.ui;

import freemarker.template.utility.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmPseudoTask;
import pl.net.bluesoft.rnd.processtool.plugins.ActionPermissionChecker;
import pl.net.bluesoft.rnd.processtool.plugins.QueueBean;
import pl.net.bluesoft.rnd.processtool.plugins.TaskPermissionChecker;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Html builder for the task view
 *
 * @author mpawlak@bluesoft.net.pl
 */
public class TaskViewBuilder extends AbstractViewBuilder<TaskViewBuilder> {
    private BpmTask task;
    private List<ProcessStateAction> actions;
    private String version;
    private String description;

    @Override
    protected IAttributesProvider getViewedObject() {
        return this.task;
    }

    @Override
    protected void buildAdditionalData(final Document document) {
        addVersionNumber(document);
    }


    private void addVersionNumber(Document document) {
        Element versionNumber = document.createElement("div")
                .attr("id", "versionList")
                .attr("class", "process-version");
        document.appendChild(versionNumber);

        versionNumber.append(description + " v. " + version);
    }

    @Override
    protected void buildSpecificActionButtons(final Element specificActionButtons) {

        /* Check if task is from queue */
        if (isTaskHasNoOwner())
        {
            if(hasPermissionToAction("claim"))
                addClaimActionButton(specificActionButtons);
        }

        /* Check if user, who is checking the task, is the assigned person */
        else if (isUserAssignedToTask() || isSubstitutingUser()) {
            for (ProcessStateAction action : actions) {
				if (hasPermissionToAction(action.getBpmName())) {
					processAction(action, specificActionButtons);
				}
			}
        }
    }

	protected boolean isSubstitutingUser() {
        return ctx.getUserSubstitutionDAO().isSubstitutedBy(task.getAssignee(), user.getLogin());
    }

	private boolean hasPermissionToAction(String actionName) {
		for (ActionPermissionChecker permissionChecker : processToolRegistry.getGuiRegistry().getActionPermissionCheckers()) {
			Boolean result = permissionChecker.hasPermission(actionName, task, getQueueBeans(), user);
			if (result != null && !result) {
				return false;
			}
		}
		return true;
	}

    @Override
    protected void addSpecificHtmlWidgetData(final Map<String, Object> viewData, final IAttributesProvider viewedObject) {
        viewData.put(IHtmlTemplateProvider.PROCESS_PARAMTER, viewedObject.getProcessInstance());
        viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
    }

    @Override
    protected boolean isUserAssignedToViewedObject() {
        return isUserAssignedToTask();
    }

    private void processAction(ProcessStateAction action, Element parent) {

        String actionButtonId = "action-button-" + action.getBpmName();

        String actionLabel = action.getLabel();
        if (actionLabel == null)
            actionLabel = "label";
            //TODO make autohide
        else if (actionLabel.equals("hide"))
            return;

        String actionType = action.getActionType();
        if (actionType == null || actionType.isEmpty())
            actionType = "primary";

        String iconName = action.getAttributeValue(ProcessStateAction.ATTR_ICON_NAME);
        if (iconName == null || iconName.isEmpty())
            iconName = "arrow-right";

		String btnClass = "btn btn-" + actionType;
		String styleName = action.getAttributeValue("styleName");

		if (hasText(styleName)) {
			btnClass += ' ' + styleName;
		}

		Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", btnClass)
                .attr("disabled", "true")
                .attr("type", "button")
                .attr("id", actionButtonId)
                .attr("data-toggle", "tooltip")
                .attr("data-placement", "bottom")
                .attr("title", i18Source.getMessage(action.getDescription()));
        parent.appendChild(buttonNode);


        Element actionButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-" + iconName);

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
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip();");
    }

    private void addClaimActionButton(Element parent) {
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
                .append("').tooltip({placement: 'bottom', title: '").append(i18Source.getMessage("button.claim.descrition")).append("'});");
    }

    public TaskViewBuilder setActions(List<ProcessStateAction> actions) {
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

    public TaskViewBuilder setTask(BpmTask task) {
        this.task = task;
        return this;
    }

    protected boolean hasUserPriviledgesToViewTask()
    {

        // default permission checking
        if (task.getPotentialOwners().contains(user.getLogin()))
            return true;

        if(StringUtils.isEmpty(task.getAssignee())) {
            for (String queueName : userQueues)
                if (task.getQueues().contains(queueName))
                    return true;
        }
        else if(task.getAssignee().equals(user.getLogin()))
            return true;

        return hasUserRightsToTask();
    }

    private boolean hasUserRightsToTask() {
		for (TaskPermissionChecker taskPermissionChecker : processToolRegistry.getGuiRegistry().getTaskPermissionCheckers()) {
			Boolean hasPermission = taskPermissionChecker.hasPermission(user, getQueueBeans(), task);

			if (hasPermission != null) {
				return hasPermission;
			}
		}

		// default permission checking
		if (task.getPotentialOwners().contains(user.getLogin()))
            return true;

        for (String queueName : userQueues)
            if (task.getQueues().contains(queueName))
                return true;

        return false;
    }

    protected Set<QueueBean> getQueueBeans()
    {
        Set<QueueBean> queueBeans = new HashSet<QueueBean>();
        for(ProcessQueueConfig queueConfig: ctx.getProcessDefinitionDAO().getQueueConfigs())
            for(String taskQueueName: task.getQueues())
                if(queueConfig.getName().equals(taskQueueName))
                {
                    QueueBean queueBean = new QueueBean()
                            .setQueueName(taskQueueName);

                    for(ProcessQueueRight queueRight: queueConfig.getRights())
                        queueBean.addRole(queueRight.getRoleName());

                    queueBeans.add(queueBean);
                }

        return queueBeans;
    }

    private boolean isPseudoTask()
    {
        return task != null && task instanceof BpmPseudoTask;
    }


    private boolean isTaskHasNoOwner() {
        return task.getAssignee() == null || task.getAssignee().isEmpty();
    }

    private boolean isTaskFinished() {
        return task.isFinished();
    }

    private boolean isUserAssignedToTask() {
        return user.getLogin().equals(task.getAssignee());
    }

    @Override
    protected TaskViewBuilder getThis() {
        return this;
    }

    @Override
    protected boolean showGenericButtons() {
        return !isPseudoTask();
    }


    @Override
    protected String getViewedObjectId() {
        return this.task.getInternalTaskId();
    }

    @Override
    protected boolean isViewedObjectClosed() {
        return isTaskFinished();
    }

    @Override
    protected String getSaveButtonMessageKey() {
        return "button.save.process.data";
    }

    @Override
    protected String getSaveButtonDescriptionKey() {
        return "button.save.process.desc";
    }

    @Override
    protected String getCancelButtonMessageKey()
    {
        if(isTaskHasNoOwner())
            return "button.exit.noowner";
        else
            return "button.exit";
    }

    @Override
    protected String getActionsListHtmlId() {
        return "actions-list";
    }

    @Override
    protected String getSaveButtonHtmlId() {
        return "action-button-save";
    }

    @Override
    protected String getActionsGenericListHtmlId() {
        return "actions-generic-list";
    }

    @Override
    protected String getVaadinWidgetsHtmlId() {
        return "vaadin-widgets";
    }

    @Override
    protected String getCancelButtonClickFunction() {
        return "onCancelButton";
    }

    @Override
    protected String getCancelButtonHtmlId() {
        return "action-button-cancel";
    }

    @Override
    protected String getActionsSpecificListHtmlId() {
        return "actions-process-list";
    }

    @Override
    protected boolean isUserCanPerformActions() {
        return isUserAssignedToTask() || isSubstitutingUser();
    }

    @Override
    protected String getSaveButtonClickFunction() {
        return "onSaveButton";
    }
}
