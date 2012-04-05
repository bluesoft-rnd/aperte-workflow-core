package pl.net.bluesoft.rnd.processtool.view.impl;

import com.vaadin.ui.Component;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.processtool.view.ViewRenderer;

public abstract class ComponentPaneRenderer<T extends Component & VaadinUtility.Refreshable> implements ViewRenderer {
    protected final Class<T> viewClass;
    protected final T pane;

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
}
