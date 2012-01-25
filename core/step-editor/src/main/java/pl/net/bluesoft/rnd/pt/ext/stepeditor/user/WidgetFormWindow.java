package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;

import java.util.Locale;
import java.util.logging.Logger;

public class WidgetFormWindow extends Panel  {

	private static final long serialVersionUID = -916309904329553267L;
    private static final Logger logger = Logger.getLogger(WidgetFormWindow.class.getName());

	public class WidgetForm extends Form {
		
		private VerticalLayout propertiesLayout;
		private VerticalLayout permissionsLayout;
		
		public WidgetForm() {
			super();

            permissionsLayout = new VerticalLayout();
            permissionsLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            permissionsLayout.setSpacing(true);

            propertiesLayout = new VerticalLayout();
            propertiesLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            propertiesLayout.setSpacing(true);

			HorizontalLayout mainLayout = new HorizontalLayout();
			mainLayout.setMargin(true);
			mainLayout.setSpacing(true);
            mainLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
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
                logger.severe("Unexpected property type: " + p.getPropertyType());
				break;
			}
        }
		
		public void addToPermissionsLayout(Component c) {
			permissionsLayout.addComponent(c);
		}
        
        public void addToPropertiesLayout(Component c) {
            propertiesLayout.addComponent(c);
        }
	}
	
	public void loadWidget(final WidgetItemInStep widget, Locale locale) {
		removeAllComponents();
		if (widget == null) {
		    setCaption("");
		    return;
        }

        setCaption(widget.getWidgetItem().getName());

        VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(new Label(widget.getWidgetItem().getDescription()));

		WidgetForm form = new WidgetForm();

		if ((widget.getProperties() == null || widget.getProperties().size() == 0) && (widget.getPermissions() == null || widget.getPermissions().size() == 0)) {
			layout.addComponent(new Label(Messages.getString("form.no.parameters.defined")));
		} else {
			form.setImmediate(true);

            if (widget.hasProperties()) {
                form.addToPropertiesLayout(new Label("<b>" + Messages.getString("form.properties") + "</b>", Label.CONTENT_XHTML));
            }
			if (widget.hasPermissions()) {
			    form.addToPermissionsLayout(new Label("<b>" + Messages.getString("form.permissions") + "</b>", Label.CONTENT_XHTML));
			}

            WidgetConfigFormFieldFactory fieldFactory = new WidgetConfigFormFieldFactory();
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
