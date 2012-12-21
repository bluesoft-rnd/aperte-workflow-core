package pl.net.bluesoft.rnd.processtool.view.impl;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import org.aperteworkflow.ui.view.ViewCallback;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.ui.view.ViewRenderer;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;

public abstract class ComponentPaneRenderer<T extends Component & VaadinUtility.Refreshable> implements ViewRenderer {
    protected Class<T> viewClass;
    protected T pane;
    protected ViewCallback viewCallback;

    @Override
    public void setBpmSession(ProcessToolBpmSession bpmSession) {
        //nothing
    }

    @Override
    public void setViewCallback(ViewCallback viewCallback) {
        this.viewCallback = viewCallback;
    }

    @Override
    public void setUp(Application application) {
        //nothing
    }

    @Override
    public Object getViewData() {
        return null;//not needed in this case
    }

    public ComponentPaneRenderer(T pane) {
        this.viewClass = (Class<T>) pane.getClass();
        this.pane = pane;
    }

    @Override
    public String getViewId() {
        return viewClass.getName();
    }

    @Override
    public void refreshData() {
        pane.refreshData();
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void handleDisplayAction() {

    }
}
