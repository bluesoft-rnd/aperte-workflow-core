package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.And;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.ui.AbstractSelect.AcceptItem;
import com.vaadin.ui.Tree;

final class TreeDeleteHandler implements DropHandler {

	private static final long		serialVersionUID	= -1252687997956419353L;

	private UserStepEditorWindow	stepEditorWindow;

	private Tree	stepTree;

	public TreeDeleteHandler(UserStepEditorWindow stepEditorWindow, Tree stepTree) {
		this.stepEditorWindow = stepEditorWindow;
		this.stepTree = stepTree;
	}

	public void drop(DragAndDropEvent dropEvent) {
		// criteria verify that this is safe
		DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();
		
		Object sourceItemId = t.getItemId();
		throw new RuntimeException("This shit is never used");
		//stepEditorWindow.deleteTreeItem(sourceItemId);
	}

	public AcceptCriterion getAcceptCriterion() {
		return new And(new SourceIs(stepTree), AcceptItem.ALL);
	}

}