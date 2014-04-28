package org.aperteworkflow.webapi.main.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.IStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.List;

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

    /**
     * Builder for javascripts
     */
    protected StringBuilder scriptBuilder = new StringBuilder(1024);

    protected int vaadinWidgetsCount = 0;

    protected abstract T getThis();

    protected abstract void buildWidgets(final Document document, final Element widgetsNode);

    public StringBuilder build() throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        scriptBuilder.append("<script type=\"text/javascript\">");
        final Document document = Jsoup.parse("");

        final Element widgetsNode = document.createElement("div")
                .attr("id", "vaadin-widgets")
                .attr("class", "vaadin-widgets-view");
        document.appendChild(widgetsNode);

        buildWidgets(document, widgetsNode);

        stringBuilder.append(document.toString());

        scriptBuilder.append("vaadinWidgetsCount = ").append(vaadinWidgetsCount).append(';');
        scriptBuilder.append("</script>");
        stringBuilder.append(scriptBuilder);

        return stringBuilder;

    }

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

        /* Check if the viewed object is in a terminal state */
        if (isObjectClosed()) {
            buildCancelActionButton(genericActionButtons);
            return;
        }
        //buildSaveActionButton(genericActionButtons);

        buildCancelActionButton(genericActionButtons);
    }

    /**
     * Check if the object being viewed is in the terminal state.
     *
     * @return
     */
    protected abstract boolean isObjectClosed();

    protected void buildSaveActionButton(final Element parent) {
        String actionButtonId = "action-button-save";

        Element buttonNode = parent.ownerDocument().createElement("button")
                .attr("class", "btn btn-warning")
                .attr("disabled", "true")
                .attr("id", actionButtonId);

        Element saveButtonIcon = parent.ownerDocument().createElement("span")
                .attr("class", "glyphicon glyphicon-floppy-save");

        parent.appendChild(buttonNode);
        buttonNode.appendChild(saveButtonIcon);

        buttonNode.appendText(i18Source.getMessage(getSaveButtonMessageKey()));

        scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { onSaveButton('").append(getObjectId()).append("');  });");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(getSaveButtonDescriptionKey())).append("'});");
    }

    protected abstract String getSaveButtonDescriptionKey();

    protected abstract String getSaveButtonMessageKey();

    protected void buildCancelActionButton(final Element parent) {
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

        buttonNode.appendText(i18Source.getMessage(getCancelButtonMessageKey()));

        scriptBuilder.append("$('#").append(actionButtonId).append("').click(function() { onCancelButton();  });");
        scriptBuilder.append("$('#").append(actionButtonId).append("').tooltip({title: '").append(i18Source.getMessage(getCancelButtonMessageKey())).append("'});");
    }

    protected abstract String getCancelButtonMessageKey();

    /**
     * Get the id of the viewed object.
     * @return
     */
    protected abstract String getObjectId();

}
