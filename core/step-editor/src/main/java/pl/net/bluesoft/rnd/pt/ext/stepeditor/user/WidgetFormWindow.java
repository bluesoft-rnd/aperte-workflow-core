package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;



import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import org.aperteworkflow.editor.ui.permission.PermissionProvider;

import pl.net.bluesoft.rnd.pt.ext.widget.property.PropertiesPanel;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;


public class WidgetFormWindow extends Panel  {

	private static final long serialVersionUID = -916309904329553267L;
    private static final Logger logger = Logger.getLogger(WidgetFormWindow.class.getName());
	
	public void loadWidget(final WidgetItemInStep widget) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		removeAllComponents();
        setStyleName(Reindeer.PANEL_LIGHT);
		if (widget == null) {
		    setCaption("");
		    return;
        }
        VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(new Label(widget.getWidgetItem().getDescription()));        
		if ((widget.getProperties() == null || widget.getProperties().size() == 0) && (widget.getPermissions() == null || widget.getPermissions().size() == 0)) {
			layout.addComponent(new Label(messages.getMessage("form.no.parameters.defined")));
		} else {
            TabSheet ts = new TabSheet();
            ts.setWidth("100%");
            if (widget.hasProperties()) {
                PropertiesPanel form = new PropertiesPanel();
            	form.init(widget.getWidgetItem().getClassInfo());
            	form.refreshForm(false, widget.getProperties());
                ts.addTab(form, messages.getMessage("form.properties"));
            }
            if (widget.hasPermissions()) {
                PermissionEditor permissionEditor = new PermissionEditor();
                permissionEditor.setMargin(true);
                permissionEditor.setProvider(new PermissionProvider() {
                    @Override
                    public Collection<Permission> getPermissions() {
                        return new LinkedHashSet<Permission>(widget.getPermissions());
                    }

                    @Override
                    public Collection<PermissionDefinition> getPermissionDefinitions() {
                        return widget.getPermissionDefinitions();
                    }

                    @Override
                    public boolean isNewPermissionDefinitionAllowed() {
                        return false;
                    }

                    @Override
                    public void addPermission(Permission permission) {
                        Set<Permission> newPermissions = new LinkedHashSet<Permission>(widget.getPermissions());
                        newPermissions.add(permission);
                        logger.info("addPermission: ");
                        for (Permission pp : newPermissions) {
                            logger.info(pp.toString());
                        }
                        logger.info("addPermission: finished");
                        widget.setPermissions(new ArrayList<Permission>(newPermissions));
                    }

                    @Override
                    public void removePermission(Permission permission) {
                        Set<Permission> newPermissions = new LinkedHashSet<Permission>(widget.getPermissions());
                        newPermissions.remove(permission);
                        logger.info("removePermission: ");
                        for (Permission pp : newPermissions) {
                            logger.info(pp.toString());
                        }
                        logger.info("removePermission: finished");
                        widget.setPermissions(new ArrayList<Permission>(newPermissions));
                    }
                });
                permissionEditor.loadData();
                ts.addTab(permissionEditor, messages.getMessage("form.permissions"));

            }
            layout.addComponent(ts);
		}

	}

}
