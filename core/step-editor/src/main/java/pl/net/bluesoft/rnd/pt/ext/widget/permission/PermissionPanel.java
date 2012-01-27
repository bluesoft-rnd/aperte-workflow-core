package pl.net.bluesoft.rnd.pt.ext.widget.permission;


import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.Collection;

public class PermissionPanel extends VerticalLayout implements DataHandler {

    /**
     * Remove single permission from the list
     */
    private class RemovePermissionCommand implements MenuBar.Command {
        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {
            Object o = permissionTable.getValue();
        }
    }

    /**
     * Add a new permission
     */
    private class AddPermissionCommand implements MenuBar.Command {

        private PermissionDefinition definition;

        private AddPermissionCommand() {

        }

        private AddPermissionCommand(PermissionDefinition definition) {
            this.definition = definition;
        }

        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {
            if (definition == null) {
                permissionTableContainer.addBean(new PermissionWrapper());
            } else {
                permissionTableContainer.addBean(new PermissionWrapper());
            }
        }
    }

    private class PermissionTableColumnGenerator implements Table.ColumnGenerator {
        @Override
        public Object generateCell(Table source, Object itemId, Object columnId) {
            BeanItem<PermissionWrapper> beanItem = (BeanItem<PermissionWrapper>) itemId;
            PermissionWrapper wrapper = beanItem.getBean();
            wrapper.setPriviledgeNameEditable(permissionProvider.isNewDefinitionAllowed());
            PermissionWrapperForm form = new PermissionWrapperForm(wrapper);
            return form;
        }
    }

    private MenuBar menuBar;
    private MenuBar.MenuItem addMenuItem;
    private MenuBar.MenuItem removeMenuItem;
    private Table permissionTable;
    private BeanItemContainer<PermissionWrapper> permissionTableContainer;
    private PermissionProvider permissionProvider;
    
    public PermissionPanel() {
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = VaadinUtility.getThreadI18nSource();

        menuBar = new MenuBar();
        permissionTable = new Table();
        permissionTable.addGeneratedColumn("foo", new PermissionTableColumnGenerator());

        addMenuItem = menuBar.addItem(messages.getMessage("permission.add"), null);
        removeMenuItem = menuBar.addItem(messages.getMessage("permission.remove"), null);
    }

    private void initLayout() {
        addComponent(menuBar);
        addComponent(permissionTable);
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    @Override
    public void loadData() {
        I18NSource messages = VaadinUtility.getThreadI18nSource();

        addMenuItem.removeChildren();
        if (permissionProvider.getPermissionDefinitions() != null) {
            for (PermissionDefinition definition : permissionProvider.getPermissionDefinitions()) {
                addMenuItem.addItem(definition.getKey(), new AddPermissionCommand(definition));
            }
        }

        if (permissionProvider.isNewDefinitionAllowed()) {
            addMenuItem.addSeparator();
            addMenuItem.addItem(messages.getMessage("permission.new"), new AddPermissionCommand());
        }

        permissionTableContainer.removeAllItems();
        if (permissionProvider.getPermissions() != null) {
            for (AbstractPermission abstractPermission : permissionProvider.getPermissions()) {
                PermissionWrapper wrapper = new PermissionWrapper(abstractPermission);
                wrapper.setPriviledgeNameEditable(permissionProvider.isNewDefinitionAllowed());
                permissionTableContainer.addBean(wrapper);
            }
        }
    }

    @Override
    public void saveData() {

    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
