package pl.net.bluesoft.rnd.pt.ext.widget.property;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

public class PropertiesForm extends Form {
	
	
	private FormLayout mainLayout;
	
	

	public PropertiesForm() {
		super();
		mainLayout = new FormLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainLayout.setSizeUndefined();
		mainLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		setLayout(mainLayout);
	}

	@Override
	protected void attachField(Object propertyId, Field field) {
		mainLayout.addComponent(field);
	}

	public void addComponent(Component c) {
		mainLayout.addComponent(c);
	}
	
	
}