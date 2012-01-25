package pl.net.bluesoft.rnd.pt.ext.actioneditor;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Form;
import com.vaadin.ui.VerticalLayout;

public class AttributesForm extends Form {

	private VerticalLayout mainLayout;

	public AttributesForm() {
		super();
		mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.setSizeUndefined();
		mainLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		setLayout(mainLayout);
	}

	@Override
	protected void attachField(Object propertyId, com.vaadin.ui.Field field) {
		mainLayout.addComponent(field);
	}

}
