package org.aperteworkflow.help.impl;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import org.vaadin.jonatan.contexthelp.Placement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mwysocki_bls
 * Date: 8/30/11
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableWithHelp extends Table implements Table.HeaderClickListener {
	private HelpFactory helpFactory;
	private boolean helpActivated = false;
	private Component helpPosition = this;
	private Map<Object, String> helpMap = new HashMap<Object, String>();

	public TableWithHelp(HelpFactory helpFactory) {
		super();
		init(helpFactory);
	}

	private void init(HelpFactory helpFactory) {
		this.helpFactory = helpFactory;
		setSortDisabled(true);
	}

	public TableWithHelp(String caption, HelpFactory helpFactory) {
		super(caption);
		init(helpFactory);
	}

	public TableWithHelp(String caption, Container dataSource, HelpFactory helpFactory) {
		super(caption, dataSource);
		init(helpFactory);
	}

	public void addHelpFor(Object propertyId, String key) {
		initHelp();

			setColumnIcon(propertyId, helpFactory.helpIcon(8));
			helpFactory.logHelpKey(key);
			helpMap.put(propertyId, key);
	}

	private void initHelp() {
		if (!helpActivated) {
			helpActivated = true;
			addListener((HeaderClickListener) this);
			initComponentHelp();
		}
	}

	private void initComponentHelp() {
		helpFactory.getContextHelp().addHelpForComponent(helpPosition, "help.empty", Placement.ABOVE);
	}

	@Override
	public void headerClick(HeaderClickEvent event) {
		Object propertyId = event.getPropertyId();
		if (helpMap.containsKey(propertyId)) {
			helpFactory.showHelp(helpPosition, helpMap.get(propertyId), Placement.ABOVE);
		}
	}

	public HelpFactory getHelpFactory() {
		return helpFactory;
	}

	public void setHelpFactory(HelpFactory helpFactory) {
		this.helpFactory = helpFactory;
	}

	public void setHelpPosition(Component helpPosition) {
		this.helpPosition = helpPosition;
		initComponentHelp();
	}
}
