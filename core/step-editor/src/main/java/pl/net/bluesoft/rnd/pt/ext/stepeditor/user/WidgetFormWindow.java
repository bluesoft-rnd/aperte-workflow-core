package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.util.Locale;

import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;

import com.vaadin.ui.*;

public class WidgetFormWindow extends Panel  {

	private static final long serialVersionUID = -916309904329553267L;
	

	public WidgetFormWindow() {
		super();
	}

	public class WidgetForm extends Form {
		
		private Layout propertiesLayout = new VerticalLayout();
		private Layout permissionsLayout = new VerticalLayout();
		
		public WidgetForm() {
			super();
			HorizontalLayout mainLayout = new HorizontalLayout();
			mainLayout.setMargin(true);
			mainLayout.setSpacing(true);
			mainLayout.setSizeUndefined();
			mainLayout.setWidth(800, UNITS_PIXELS);
		    mainLayout.addComponent(propertiesLayout);
		    mainLayout.addComponent(permissionsLayout);
		    setLayout(mainLayout);
		}
		
		@Override
        protected void attachField(Object property, Field field) {
            Property p = (Property) property;
			switch (p.getPropertyType()) {
			case PERMISSION:
				permissionsLayout.addComponent(field);
				break;
			case PROPERTY:
				propertiesLayout.addComponent(field);
				break;
			default:
				break;
			}
        }
		
		public void addToPermissionsLayout(Component c) {
			permissionsLayout.addComponent(c);
		}
	}
	
	public void loadWidget(final WidgetItemInStep widget, Locale locale) {
		removeAllComponents();
		VerticalLayout layout = (VerticalLayout) getContent();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeUndefined();
		layout.setWidth(800, UNITS_PIXELS);
		
		if (widget == null) {
		  setCaption("");
		  return;
		}
		else {
		  setCaption(widget.getWidgetItem().getName());
		}

		layout.removeAllComponents();
		
		layout.addComponent(new Label(widget.getWidgetItem().getDescription()));

		WidgetForm form = new WidgetForm();

		if ((widget.getProperties() == null || widget.getProperties().size() == 0) && (widget.getPermissions() == null || widget.getPermissions().size() == 0)) {
			layout.addComponent(new Label(Messages.getString("form.no.parameters.defined")));
		} else {
			form.setCaption(Messages.getString("form.parameters"));
			form.setImmediate(true);
			if (widget.hasPermissions()) {
			   form.addToPermissionsLayout(new Label(Messages.getString("form.permissions")));
			}
			WidgetConfigFormFieldFactory fieldFactory = null;

			if (widget.getWidgetItem().getConfigurator() != null) {
				try {
					fieldFactory = widget.getWidgetItem().getConfigurator().newInstance();
					if(fieldFactory != null){
						fieldFactory.setI18NProviders(widget.getWidgetItem().getBundle().getI18NProviders());
						fieldFactory.setLocale(locale);
					}
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
			}

			if (fieldFactory == null) {
				fieldFactory = new WidgetConfigFormFieldFactory();
			}

			for (Property<?> property : widget.getProperties()) {
				final Field field = fieldFactory.createField(property);
				form.addField(property, field);
			}
			for (Property<?> perm : widget.getPermissions()) {
				final Field field = fieldFactory.createField(perm);
				form.addField(perm, field);
			}
		}

		layout.addComponent(form);
	}

}
