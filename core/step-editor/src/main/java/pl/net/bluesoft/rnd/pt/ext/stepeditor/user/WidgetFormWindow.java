package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import org.aperteworkflow.editor.ui.permission.PermissionProvider;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;

import java.util.Collection;
import java.util.Locale;
import java.util.logging.Logger;

public class WidgetFormWindow extends Panel  {

	private static final long serialVersionUID = -916309904329553267L;
    private static final Logger logger = Logger.getLogger(WidgetFormWindow.class.getName());
	
	public void loadWidget(final WidgetItemInStep widget) {
		removeAllComponents();
        setStyleName(Reindeer.PANEL_LIGHT);
		if (widget == null) {
		    setCaption("");
		    return;
        }
        VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(new Label(widget.getWidgetItem().getDescription()));        
		if ((widget.getProperties() == null || widget.getProperties().size() == 0) && (widget.getPermissions() == null || widget.getPermissions().size() == 0)) {
			layout.addComponent(new Label(Messages.getString("form.no.parameters.defined")));
		} else {
            TabSheet ts = new TabSheet();
            ts.setWidth("100%");
            if (widget.hasProperties()) {
                Form form = new Form();
                form.setImmediate(true);
                WidgetConfigFormFieldFactory fieldFactory = new WidgetConfigFormFieldFactory();
    			for (Property<?> property : widget.getProperties()) {
    				final Field field = fieldFactory.createField(property);
    				form.addField(property, field);
    			}
                ts.addTab(form, Messages.getString("form.properties"));
            }
            if (widget.hasPermissions()) {
                PermissionEditor permissionEditor = new PermissionEditor();
                permissionEditor.setProvider(new PermissionProvider() {
                    @Override
                    public Collection<Permission> getPermissions() {
                        return widget.getPermissions();
                    }

                    @Override
                    public Collection<PermissionDefinition> getPermissionDefinitions() {
                        return widget.getPermissionDefinitions();
                    }

                    @Override
                    public boolean isNewPermissionDefinitionAllowed() {
                        return false;
                    }
                });
                permissionEditor.loadData();
                ts.addTab(permissionEditor, Messages.getString("form.permissions"));

            }
            layout.addComponent(ts);
		}

	}

}
