package org.aperteworkflow.admin.controller;

/**
 * @author: lgajowy@bluesoft.net.pl
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