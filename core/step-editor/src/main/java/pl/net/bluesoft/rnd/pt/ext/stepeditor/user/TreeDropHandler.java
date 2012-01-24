package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.data.Item;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.Or;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.TargetItemAllowsChildren;

final class TreeDropHandler implements DropHandler {

	private static final long serialVersionUID = -1252687997956419353L;

	private Tree stepTree;
	private Tree availableTree;

	public TreeDropHandler(Tree stepTree, Tree availableTree) {
		this.stepTree = stepTree;
		this.availableTree = availableTree;
	}

	public void drop(DragAndDropEvent dropEvent) {
		// criteria verify that this is safe
		DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();

		Component sourceComponent = t.getSourceComponent();
		Object sourceItemId = t.getItemId();

		AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
		Object targetItemId = dropData.getItemIdOver();

		if (sourceComponent == availableTree && sourceItemId instanceof WidgetItem && targetItemId != null) {
			WidgetItem widgetItem = (WidgetItem) sourceItemId;
			// copy item from table to category
			final WidgetItemInStep itemId = new WidgetItemInStep(widgetItem);
			Item newItem = stepTree.addItem(itemId);
			newItem.getItemProperty("name").setValue(widgetItem.getName());
			newItem.getItemProperty("icon").setValue(availableTree.getItem(widgetItem).getItemProperty("icon").getValue());
			stepTree.setParent(itemId, targetItemId);
			stepTree.setChildrenAllowed(itemId, widgetItem.getChildrenAllowed());
			stepTree.expandItem(targetItemId);

		} else if (sourceComponent == stepTree && sourceItemId != targetItemId) {
			stepTree.setParent(sourceItemId, targetItemId);
			stepTree.expandItem(targetItemId);
		}
	}

	public AcceptCriterion getAcceptCriterion() {
		return new And(new Or(new SourceIs(availableTree), new SourceIs(stepTree)), TargetItemAllowsChildren.get(), AcceptItem.ALL);
	}

}