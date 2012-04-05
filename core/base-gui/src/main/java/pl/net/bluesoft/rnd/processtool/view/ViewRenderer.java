package pl.net.bluesoft.rnd.processtool.view;

import com.vaadin.ui.Component;

import java.util.Map;

public interface ViewRenderer {
    String getViewId();
    Component render(Map<String, ?> viewData);
    void refreshData();
}
