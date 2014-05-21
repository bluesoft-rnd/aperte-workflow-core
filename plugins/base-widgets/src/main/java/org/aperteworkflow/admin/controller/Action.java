package org.aperteworkflow.admin.controller;

/**
 * Created by lukasz on 5/21/14.
 */
public class Action {
    String actionName;
    String actionTitle;

    public Action(String actionName, String actionTitle) {
        this.actionName = actionName;
        this.actionTitle = actionTitle;
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionTitle() {
        return actionTitle;
    }
}