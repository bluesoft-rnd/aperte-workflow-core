package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

final class TreeDropHandler implements DropHandler {

    private static final long serialVersionUID = -1252687997956419353L;
    private Tree tree;
    private HierarchicalContainer container;

    public TreeDropHandler(Tree tree, HierarchicalContainer container) {
        this.tree = tree;
        this.container = container;
    }

    public AcceptCriterion getAcceptCriterion() {
        return AcceptAll.get();
    }

    public void drop(DragAndDropEvent dropEvent) {
        Transferable t = dropEvent.getTransferable();
        Component src = t.getSourceComponent();
        Object sourceItemId;
        HierarchicalContainer container = (HierarchicalContainer) tree.getContainerDataSource();
        if (src instanceof WidgetInfoDnDWrapper) { //add widget
            WidgetInfoDnDWrapper dragAndDropWrapper = (WidgetInfoDnDWrapper) src;
            WidgetItem wi = dragAndDropWrapper.widgetItem;
            Object widgetElement = new WidgetItemInStep(wi);
            Item subItem = container.addItem(widgetElement);
            subItem.getItemProperty("name").setValue(wi.getName());
            container.setChildrenAllowed(widgetElement, nvl(wi.getChildrenAllowed(), false));
            sourceItemId = widgetElement;
        } else {
            if (src != tree || !(t instanceof DataBoundTransferable)) {
                return;
            }
            sourceItemId = ((DataBoundTransferable) t).getItemId();
        }

        Tree.TreeTargetDetails dropData = ((Tree.TreeTargetDetails) dropEvent.getTargetDetails());
        Object targetItemId = dropData.getItemIdOver();
        VerticalDropLocation location = dropData.getDropLocation();
        if (container.getParent(targetItemId) == null) {  //the can be only one! ... root element2
            location = VerticalDropLocation.MIDDLE;
        }
        moveNode(sourceItemId, targetItemId, location);
    }


    private void moveNode(Object sourceItemId, Object targetItemId,
                             VerticalDropLocation location) {
        if (sourceItemId == null || targetItemId == null) return;
        if (location == VerticalDropLocation.MIDDLE) {
            if (!container.areChildrenAllowed(targetItemId)) return;
            if (container.setParent(sourceItemId, targetItemId)
                    && container.hasChildren(targetItemId)) {
                container.moveAfterSibling(sourceItemId, null);
            }
        } else if (location == VerticalDropLocation.TOP) {
            Object parentId = container.getParent(targetItemId);
            if (!container.areChildrenAllowed(parentId)) return;
            if (container.setParent(sourceItemId, parentId)) {
                container.moveAfterSibling(sourceItemId, targetItemId);
                container.moveAfterSibling(targetItemId, sourceItemId);
            }
        } else if (location == VerticalDropLocation.BOTTOM) {
            Object parentId = container.getParent(targetItemId);
            if (!container.areChildrenAllowed(parentId)) return;
            if (container.setParent(sourceItemId, parentId)) {
                container.moveAfterSibling(sourceItemId, targetItemId);
            }
        }
        tree.expandItem(sourceItemId);
        tree.expandItem(targetItemId);
    }
}