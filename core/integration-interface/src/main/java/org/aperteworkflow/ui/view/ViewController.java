package org.aperteworkflow.ui.view;

import java.util.Map;

public interface ViewController {
    void displayPreviousView();
    void displayCurrentView();
    void addView(ViewRenderer listener);
    void displayView(String viewId);
    void displayView(String viewId, Map<String, ?> viewData);
    void displayView(String viewId, Map<String, ?> viewData, boolean forward);
    void removeView(String viewId);
    void addViewListener(ViewListener listener);
    void removeViewListener(ViewListener listener);
    void refreshCurrentView();
}
