package org.aperteworkflow.webapi.main.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.*;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-04-28.
 */
public abstract class AbstractViewBuilder<T extends AbstractViewBuilder> {
    protected List<? extends IStateWidget> widgets;
    protected I18NSource i18Source;
    protected UserData user;
    protected ProcessToolContext ctx;
    protected Collection<String> userQueues;
    protected ProcessToolBpmSession bpmSession;

    @Autowired
    protected ProcessToolRegistry processToolRegistry;

    @Autowired
    protected IHtmlTemplateProvider templateProvider;

    @Autowired
    protected IUserSource userSource;

    @Autowired
    protected IDictionaryFacade dictionaryFacade;

    protected AbstractViewBuilder() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    /**
     * Builder for javascripts
     */
    protected StringBuilder scriptBuilder = new StringBuilder(1024);

    protected int vaadinWidgetsCount = 0;

    protected abstract T getThis();

    public StringBuilder build() throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        scriptBuilder.append("<script type=\"text/javascript\">");
        final Document document = Jsoup.parse("");

        final Element widgetsNode = document.createElement("div")
                .attr("id", getVaadinWidgetsHtmlId())
                .attr("class", "vaadin-widgets-view");
        document.appendChild(widgetsNode);

        buildWidgets(document, widgetsNode);

        buildActionButtons(document);

        buildAdditionalData(document);

        stringBuilder.append(document.toString());
        scriptBuilder.append("vaadinWidgetsCount = ").append(vaadinWidgetsCount).append(';');
        scriptBuilder.append("</script>");
        stringBuilder.append(scriptBuilder);

        return stringBuilder;

    }

    protected abstract String getVaadinWidgetsHtmlId();

    protected void buildWidgets(final Document document, final Element widgetsNode) {
        for (IStateWidget widget : widgets) {
            WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                    .setParent(widgetsNode)
                    .setWidget(widget)
                    .setProcessInstance(getViewedObject().getProcessInstance())
                    .setForcePrivileges(false)
                    .setPrivileges(new ArrayList<String>());

            buildWidget(childBean);
        }
    }

    protected abstract IAttributesProvider getViewedObject();

    protected void buildWidget(final WidgetHierarchyBean widgetHierarchyBean) {
        IStateWidget widget = widgetHierarchyBean.getWidget();
        ProcessInstance processInstance = widgetHierarchyBean.getProcessInstance();
        Element parent = widgetHierarchyBean.getParent();

        String aliasName = widget.getClassName();

        ProcessHtmlWidget processHtmlWidget = processToolRegistry.getGuiRegistry().getHtmlWidget(aliasName);

		/* Sort widgets by prority */
        List<IStateWidget> children = new ArrayList<IStateWidget>(widget.getChildren());
        Collections.sort(children, new Comparator<IStateWidget>() {
            @Override
            public int compare(IStateWidget widget1, IStateWidget widget2) {
                return widget1.getPriority().compareTo(widget2.getPriority());
            }
        });

		/* Check if widget is based on html */
        String widgetTemplateBody = templateProvider.getTemplate(aliasName);

        if (aliasName.equals("ShadowStateWidget")) {
            IStateWidgetAttribute processStateConfigurationIdAttribute =
                    widget.getAttributeByName("processStateConfigurationId");

            IStateWidgetAttribute forcePrivilegesAttribute =
                    widget.getAttributeByName("forcePrivileges");

            Boolean forcePrivileges = Boolean.parseBoolean(forcePrivilegesAttribute.getValue());

            String attributeName = processStateConfigurationIdAttribute.getValue();
            String processStateConfigurationId = processInstance.getRootProcessInstance().getSimpleAttributeValue(attributeName);

            ProcessStateConfiguration processStateConfiguration =
                    ctx.getProcessDefinitionDAO().getCachedProcessStateConfiguration(Long.parseLong(processStateConfigurationId));


            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", "vertical_layout" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget childWidget : processStateConfiguration.getWidgets()) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(childWidget)
                        .setProcessInstance(processInstance.getRootProcessInstance())
                        .setForcePrivileges(forcePrivileges)
                        .setPrivileges(getPrivileges(widget));

                buildWidget(childBean);
            }


        } else if (aliasName.equals("TabSheet")) {
            String tabId = "tab_sheet_" + widget.getId();
            String divContentId = "div_content_" + widget.getId();

            Element ulNode = parent.ownerDocument().createElement("ul")
                    .attr("id", tabId)
                    .attr("class", "nav nav-tabs");
            parent.appendChild(ulNode);

            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", divContentId)
                    .attr("class", "tab-content");
            parent.appendChild(divContentNode);

            boolean isFirst = true;

            for (IStateWidget child : children) {
                String caption = aliasName;
                /* Set caption from attributes */
                IStateWidgetAttribute attribute = child.getAttributeByName("caption");
                if (attribute != null)
                    caption = i18Source.getMessage(attribute.getValue());

                String childId = "tab" + child.getId();

				/* Li tab element */
                Element liNode = parent.ownerDocument().createElement("li");
                ulNode.appendChild(liNode);

                Element aNode = parent.ownerDocument().createElement("a")
                        .attr("id", "tab_link_" + childId)
                        .attr("href", '#' + childId)
                        .attr("data-toggle", "tab")
                        .append(caption);

                liNode.appendChild(aNode);

                scriptBuilder.append("$('#tab_link_").append(childId).append("').on('shown', function (e) { onTabChange(e); });");

				/* Content element */
                Element divTabContentNode = parent.ownerDocument().createElement("div")
                        .attr("id", childId)
                        .attr("class", isFirst ? "tab-pane active" : "tab-pane");
                divContentNode.appendChild(divTabContentNode);

                if (isFirst)
                    isFirst = false;

                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divTabContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                buildWidget(childBean);
            }

            scriptBuilder.append("$('#").append(tabId).append(" a:first').tab('show');");
        } else if (aliasName.equals("VerticalLayout")) {
            Element divContentNode = parent.ownerDocument().createElement("div")
                    .attr("id", "vertical_layout" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                buildWidget(childBean);
            }
        } else if (aliasName.equals("SwitchWidgets")) {
            List<IStateWidget> sortedList = new ArrayList<IStateWidget>(children);

            IStateWidget filteredChild = filterChildren(getViewedObject(), sortedList, widget);

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

                buildWidget(childBean);
            }
        }
        /* HTML Widget */
        else if (processHtmlWidget != null) {
            Collection<String> privileges;
            if (widgetHierarchyBean.isForcePrivileges())
                privileges = widgetHierarchyBean.getPrivileges();
            else
                privileges = getPrivileges(widget);

            Map<String, Object> viewData = new HashMap<String, Object>();
            // viewData.put(IHtmlTemplateProvider.PROCESS_PARAMTER, processInstance);
            // viewData.put(IHtmlTemplateProvider.TASK_PARAMTER, task);
            viewData.put(IHtmlTemplateProvider.ATTRIBUTES_PROVIDER, getViewedObject());
            addSpecificHtmlWidgetData(viewData, getViewedObject());
            viewData.put(IHtmlTemplateProvider.USER_PARAMTER, user);
            viewData.put(IHtmlTemplateProvider.USER_SOURCE_PARAMTER, userSource);
            viewData.put(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER, i18Source);
            viewData.put(IHtmlTemplateProvider.WIDGET_NAME_PARAMETER, aliasName);
            viewData.put(IHtmlTemplateProvider.PRIVILEGES_PARAMETER, privileges);
            viewData.put(IHtmlTemplateProvider.WIDGET_ID_PARAMETER, widget.getId().toString());
            viewData.put(IHtmlTemplateProvider.DICTIONARIES_DAO_PARAMETER, ctx.getProcessDictionaryDAO());
            viewData.put(IHtmlTemplateProvider.DICTIONARIES_FACADE, dictionaryFacade);
            viewData.put(IHtmlTemplateProvider.BPM_SESSION_PARAMETER, bpmSession);

            for (IStateWidgetAttribute attribute : widget.getAttributes())
                viewData.put(attribute.getName(), attribute.getValue());

            /* Add custom attributes from widget data providers */
            for (IWidgetDataProvider dataProvider : processHtmlWidget.getDataProviders()) {
                SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(dataProvider);
                viewData.putAll(dataProvider.getData(getViewedObject()));
            }

            String processedView = templateProvider.processTemplate(aliasName, viewData);

            Element divContentNode = parent.ownerDocument().createElement("div")
                    .append(processedView)
                    .attr("class", "html-widget-view")
                    .attr("id", "html-" + widget.getId());
            parent.appendChild(divContentNode);

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(divContentNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                buildWidget(childBean);
            }
        } else {
            vaadinWidgetsCount++;
            //http://localhost:8080
            String vaadinWidgetUrl = "/aperteworkflow/widget/" + getViewedObjectId() + "_" + widget.getId() + "/?widgetId=" + widget.getId() + "&taskId=" + getViewedObjectId();

            Element iFrameNode = parent.ownerDocument().createElement("iframe")
                    .attr("src", vaadinWidgetUrl)
                    .attr("autoResize", "true")
                    .attr("id", "iframe-vaadin-" + widget.getId())
                    .attr("frameborder", "0")
                    .attr("taskId", getViewedObjectId())
                    .attr("widgetId", widget.getId().toString())
                    .attr("class", "vaadin-widget-view")
                    .attr("widgetLoaded", "false")
                    .attr("name", widget.getId().toString());
            parent.appendChild(iFrameNode);

            scriptBuilder.append("$('#iframe-vaadin-").append(widget.getId()).append("').load(function() {onLoadIFrame($(this)); });");

            for (IStateWidget child : children) {
                WidgetHierarchyBean childBean = new WidgetHierarchyBean()
                        .setParent(iFrameNode)
                        .setWidget(child)
                        .setProcessInstance(processInstance)
                        .setForcePrivileges(widgetHierarchyBean.isForcePrivileges())
                        .setPrivileges(widgetHierarchyBean.getPrivileges());

                buildWidget(childBean);
            }
        }
    }

    protected abstract void addSpecificHtmlWidgetData(final Map<String, Object> viewData, IAttributesProvider viewedObject);

    public IStateWidget filterChildren(IAttributesProvider viewedObject, List<IStateWidget> sortedList, IStateWidget sw) {
        String selectorKey = sw.getAttributeByName("selectorKey").getValue();
        String conditions = sw.getAttributeByName("conditions").getValue();
        String selectorValue = viewedObject.getProcessInstance().getInheritedSimpleAttributeValue(selectorKey);

        if (!hasText(selectorValue)) {
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

    protected abstract void buildAdditionalData(Document document);

    public T setWidgets(List<? extends IStateWidget> widgets) {
        this.widgets = widgets;
        return getThis();
    }

    public T setI18Source(I18NSource i18Source) {
        this.i18Source = i18Source;
        return getThis();
    }

    public T setUser(UserData user) {
        this.user = user;
        return getThis();
    }

    public T setCtx(ProcessToolContext ctx) {
        this.ctx = ctx;
        return getThis();
    }

    public T setUserQueues(Collection<String> userQueues) {
        this.userQueues = userQueues;
        return getThis();
    }

    public T setBpmSession(ProcessToolBpmSession bpmSession) {
        this.bpmSession = bpmSession;
        return getThis();
    }

    /**
     * Add actions buttons to the output document.
     */
    protected void buildActionButtons(final Document document) {
        Element actionsNode = document.createElement("div")
                //.attr("id", "actions-list")
                .attr("id", getActionsListHtmlId())
                .attr("class", "actions-view");
        document.appendChild(actionsNode);

        Element genericActionButtons = document.createElement("div")
                .attr("id", getActionsGenericListHtmlId())
                .attr("class", "btn-group  pull-left actions-generic-view");

        Element specificActionButtons = document.createElement("div")
                .attr("id", getActionsSpecificListHtmlId())
                .attr("class", "btn-group  pull-right actions-process-view");

        actionsNode.appendChild(genericActionButtons);
        actionsNode.appendChild(specificActionButtons);

        /* Check if the viewed object is in a terminal state */
        if (isViewedObjectClosed()) {
            buildCancelActionButton(genericActionButtons);
            return;
        }

        // build a view specific buttons
        buildSpecificActionButtons(specificActionButtons);

        // build a save button
        if (isUserCanPerformActions()) {
            buildSaveActionButton(genericActionButtons);
        }

        buildCancelActionButton(genericActionButtons);
    }

    protected abstract boolean isUserCanPerformActions();

    protected abstract void buildSpecificActionButtons(final Element specificActionButtons);

    protected abstract String getActionsSpecificListHtmlId();

    protected abstract String getActionsGenericListHtmlId();

    protected abstract String getActionsListHtmlId();

    /**
     * Check if the object being viewed is in the terminal state.
     *
     * @return
     */
    protected abstract boolean isViewedObjectClosed();

    protected void buildSaveActionButton(final Element parent) {
        //String actionButtonId = "action-button-save";
        String actionButtonId = getSaveButtonHtmlId();

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-warning")
                .attr("disabled", "true")
                .attr("id", actionButtonId);

        Element saveButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-floppy-save");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(saveButtonIcon);

        buttonNode.appendText(i18Source.getMessage(getSaveButtonMessageKey()));

        scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { " + getSaveButtonClickFunction() + "('").append(getViewedObjectId()).append("');  });");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(getSaveButtonDescriptionKey())).append("'});");
    }

    protected abstract String getSaveButtonClickFunction();

    protected abstract String getSaveButtonHtmlId();

    protected abstract String getSaveButtonDescriptionKey();

    protected abstract String getSaveButtonMessageKey();

    protected void buildCancelActionButton(final Element parent) {
        //String actionButtonId = "action-button-cancel";
        String actionButtonId = getCancelButtonHtmlId();

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-info")
                .attr("disabled", "true")
                .attr("id", actionButtonId);
        parent.appendChild(buttonNode);

        Element cancelButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-home");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(cancelButtonIcon);

        buttonNode.appendText(i18Source.getMessage(getCancelButtonMessageKey()));

        scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { " + getCancelButtonClickFunction() + "();  });");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(getCancelButtonMessageKey())).append("'});");
    }

    protected abstract String getCancelButtonHtmlId();

    protected abstract String getCancelButtonClickFunction();

    protected abstract String getCancelButtonMessageKey();

    /**
     * Get the id of the viewed object.
     *
     * @return
     */
    protected abstract String getViewedObjectId();

    protected Collection<String> getPrivileges(IStateWidget widget) {
        Collection<String> privileges = new ArrayList<String>();
        if (!isUserAssignedToViewedObject() || isViewedObjectClosed())
            return privileges;

        for (IPermission permission : widget.getPermissions()) {
            if (permission.getRoleName().contains("*") || user.hasRole(permission.getRoleName())) {
                privileges.add(permission.getPrivilegeName());
            }
        }
        return privileges;
    }

    protected abstract boolean isUserAssignedToViewedObject();

    protected static class WidgetHierarchyBean {
        private IStateWidget widget;
        private Element parent;
        private ProcessInstance processInstance;
        private boolean forcePrivileges;
        private Collection<String> privileges;

        public IStateWidget getWidget() {
            return widget;
        }

        public WidgetHierarchyBean setWidget(IStateWidget widget) {
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

}
