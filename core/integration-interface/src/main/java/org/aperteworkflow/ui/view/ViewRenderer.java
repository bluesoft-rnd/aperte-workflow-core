package org.aperteworkflow.ui.view;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;

import java.util.Map;

public interface ViewRenderer {
    String getViewId();
    Component render(Map<String, ?> viewData);
    void refreshData();
    void setViewCallback(ViewCallback viewCallback);
    void setUp(Application application);

    Object getViewData();
    String getTitle();
    void handleDisplayAction();

    void setBpmSession(ProcessToolBpmSession bpmSession);
}
