package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;

/**
 * @author tlipski@bluesoft.net.pl
 */
class WidgetInfoDnDWrapper extends DragAndDropWrapper {

    WidgetItem widgetItem;

    WidgetInfoDnDWrapper(Component root, WidgetItem widgetItem) {
        super(root);
        this.widgetItem = widgetItem;
    }
}