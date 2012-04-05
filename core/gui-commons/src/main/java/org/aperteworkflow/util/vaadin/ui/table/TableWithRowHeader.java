package org.aperteworkflow.util.vaadin.ui.table;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Table.ColumnResizeEvent;
import com.vaadin.ui.Table.ColumnResizeListener;

public class TableWithRowHeader extends CustomComponent implements ItemSetChangeListener, ColumnResizeListener {
	private Table mainTable;
	private Table headerTable;
	private Integer rowHeight;
	private Integer headerHeight;
	private Panel panel;
	private VerticalLayout vl;

	public TableWithRowHeader(final Table mainTable, Table headerTable, Integer rowHeight, Integer headerHeight, String styleName) {
		super();
		this.headerHeight = headerHeight;
		this.rowHeight = rowHeight;
		this.mainTable = mainTable;
		this.headerTable = headerTable;

		mainTable.setImmediate(true);
		mainTable.addListener((ItemSetChangeListener)this);
		mainTable.addListener((ColumnResizeListener)this);

		panel = new Panel();
		panel.setScrollable(true);

		HorizontalLayout hl = new HorizontalLayout();
		hl.setStyleName(styleName);
		hl.setSpacing(false);
		hl.setMargin(true);
		hl.setSizeFull();

		hl.addComponent(headerTable);
		hl.addComponent(panel);

		hl.setExpandRatio(headerTable, 0);
		hl.setExpandRatio(panel, 1);

		updateWidth();
		updateHeight();

		setCompositionRoot(hl);
	}

	@Override
	public void containerItemSetChange(ItemSetChangeEvent event) {
		updateHeight();
	}

	@Override
	public void columnResize(ColumnResizeEvent event) {
		updateWidth();
	}

	private void updateWidth() {
		mainTable.setSizeUndefined();
		vl = new VerticalLayout();
		vl.setSpacing(false);
		vl.setMargin(false);
		vl.setSizeUndefined();
		vl.addComponent(mainTable);
		panel.setContent(vl);
	}

	void updateHeight(){
		int items = headerTable.getContainerDataSource().getItemIds().size();
		headerTable.setHeight(headerHeight + rowHeight * items + 2, Sizeable.UNITS_PIXELS);
		mainTable.setHeight(headerHeight + rowHeight * items + 2, Sizeable.UNITS_PIXELS);
		panel.setHeight(headerHeight + rowHeight * items + 20, Sizeable.UNITS_PIXELS);
	}
}
