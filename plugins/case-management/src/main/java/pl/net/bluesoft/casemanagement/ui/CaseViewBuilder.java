package pl.net.bluesoft.casemanagement.ui;

import org.aperteworkflow.webapi.main.ui.AbstractViewBuilder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.casemanagement.util.CaseProcessUtil;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.IStateWidget;
import pl.net.bluesoft.rnd.processtool.plugins.QueueBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;

import java.util.*;


/**
 * Created by pkuciapski on 2014-04-28.
 */
public class CaseViewBuilder extends AbstractViewBuilder<CaseViewBuilder> {
	private static CaseViewBuilderFactory factory;

	protected final Case caseInstance;

    protected CaseViewBuilder(Case caseInstance) {
        this.caseInstance = caseInstance;
    }

	public static void setFactory(CaseViewBuilderFactory factory) {
		CaseViewBuilder.factory = factory;
	}

	public static CaseViewBuilder create(Case caseInstance) {
		return factory != null ? factory.create(caseInstance) : new CaseViewBuilder(caseInstance);
	}

	@Override
	protected void buildWidgets(Document document, Element widgetsNode) {
		super.buildWidgets(document, widgetsNode);
	}

	@Override
    protected CaseViewBuilder getThis() {
        return this;
    }

    @Override
    protected boolean showGenericButtons() {
        return true;
    }

    @Override
    protected IAttributesProvider getViewedObject() {
        return this.caseInstance;
    }

    @Override
    protected void addSpecificHtmlWidgetData(final Map<String, Object> viewData, final IAttributesProvider viewedObject) {
        viewData.put(IHtmlTemplateProvider.CASE_PARAMETER, caseInstance);
    }

    @Override
    protected void buildAdditionalData(final Document document) {
        // no additional data to show for a case
    }

    @Override
    protected Set<QueueBean> getQueueBeans()
    {
        return new HashSet<QueueBean>();
    }

    @Override
    protected String getViewedObjectId() {
        return String.valueOf(caseInstance.getId());
    }

    @Override
    protected boolean isViewedObjectClosed() {
        return false;
    }

    @Override
    protected String getSaveButtonDescriptionKey() {
        return "case.management.button.save.desc";
    }

    @Override
    protected String getSaveButtonMessageKey() {
        return "case.management.button.save";
    }

    @Override
    protected String getCancelButtonMessageKey() {
        return "case.management.button.close";
    }

    @Override
    protected boolean isSubstitutingUser() {
        return false;
    }

    @Override
    protected String getActionsListHtmlId() {
        return "case-actions-list";
    }

    @Override
    protected String getSaveButtonHtmlId() {
        return "case-action-button-save";
    }

    @Override
    protected String getActionsGenericListHtmlId() {
        return "case-actions-generic-list";
    }

    @Override
    protected String getVaadinWidgetsHtmlId() {
        return "case-vaadin-widgets";
    }

    @Override
    protected String getCancelButtonClickFunction() {
        return "caseManagement.onCloseButton";
    }

    @Override
    protected String getCancelButtonHtmlId() {
        return "case-action-button-cancel";
    }

    @Override
    protected boolean isUserAssignedToViewedObject() {
        return true;
    }

    @Override
    protected boolean isUserCanPerformActions() {
		if (!hasCurrentStageEditPrivilege()) {
			return false;
		}

		List<String> privileges = new ArrayList<String>();
		privileges.add(CaseStateRole.PRIVILEGE_EDIT);

		CasePrivilegeHandlers.INSTANCE.handleEdit(caseInstance, user, privileges);

		return privileges.contains(CaseStateRole.PRIVILEGE_EDIT);
    }

    protected boolean hasUserPriviledgesToViewTask()
    {
        List<String> privileges = new ArrayList<String>();
        privileges.add(CaseStateRole.PRIVILEGE_VIEW);

        CasePrivilegeHandlers.INSTANCE.handleView(caseInstance, user, privileges);

        return privileges.contains(CaseStateRole.PRIVILEGE_VIEW);
    }

	private boolean hasCurrentStageEditPrivilege() {
		if (this.caseInstance.getCurrentStage() != null) {
			for (CaseStateRole role : this.caseInstance.getCurrentStage().getCaseStateDefinition().getRoles()) {
				if (CaseStateRole.PRIVILEGE_EDIT.equals(role.getPrivilegeName()) && (role.getRoleName().contains("*") || user.hasRole(role.getRoleName()))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected Collection<String> getPrivileges(IStateWidget widget) {
        Collection<String> privileges = super.getPrivileges(widget);
        // if user has am EDIT privilege, it forces all widgets privileges to EDIT
        if (isUserCanPerformActions()) {
            privileges.add(CaseStateRole.PRIVILEGE_EDIT);
        }
		CasePrivilegeHandlers.INSTANCE.handleEdit(caseInstance, user, privileges);
        return privileges;
    }

	@Override
	protected void buildSpecificActionButtons(final Element specificActionButtons) {
        CaseStateDefinition currentState = caseInstance.getCurrentStage().getCaseStateDefinition();
        List<CaseStateProcess> sortedProcesses = CaseProcessUtil.getSortedProcessesByPriority(currentState.getProcesses());
        for(CaseStateProcess process : sortedProcesses) {
            String bId = "action-button-".concat(process.getBpmDefinitionKey());
            String bClass = process.getProcessActionType();
            String bIcon = process.getProcessIcon();
            String bTitle = process.getProcessLabel();
            String bAction = "caseManagement.startProcess(\""+caseInstance.getId()+"\",\""+process.getBpmDefinitionKey()+"\");";
            createButton(specificActionButtons, bId, bClass, bIcon, bTitle, bTitle, bAction);
        }
    }


	protected void createButton(Element parent, String actionButtonId, String buttonClass, String iconClass,
                              String messageKey, String descriptionKey, String clickFunction) {
        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", buttonClass != null ? "btn btn-" + buttonClass : "btn")
                .attr("disabled", "true")
                .attr("id", actionButtonId)
                .attr("data-toggle", "tooltip")
                .attr("data-placement", "bottom")
                .attr("title", i18Source.getMessage(descriptionKey));

        Element buttonIcon = parent.ownerDocument().createElement("span")
                .attr("class", iconClass != null ? "glyphicon glyphicon-" + iconClass : "glyphicon");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(buttonIcon);

        buttonNode.appendText(i18Source.getMessage(messageKey));

        scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() {").append(clickFunction).append("});");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip();");
    }

    @Override
    protected String getActionsSpecificListHtmlId() {
        return "case-actions-specific-list";
    }

    @Override
    protected String getSaveButtonClickFunction() {
        return "caseManagement.onSaveButton";
    }
}
