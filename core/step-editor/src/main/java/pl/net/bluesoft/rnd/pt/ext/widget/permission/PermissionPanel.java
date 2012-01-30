package pl.net.bluesoft.rnd.pt.ext.widget.permission;


import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.Reindeer;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.HorizontalBox;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Component used for editing list of {@link AbstractPermission}
 */
public class PermissionPanel extends GridLayout implements DataHandler {

    /**
     * Command to remove single permission from the list
     */
    private class RemovePermissionCommand implements MenuBar.Command {
        @Override
        public void menuSelected(MenuBar.MenuItem selectedItem) {
            PermissionWrapper wrapper = (PermissionWrapper) permissionTable.getValue();
            unregisterPermissionWrapper(wrapper);
            loadPermissionForm(null);
            removeMenuItem.setEnabled(false);
        }
    }

    /**
     * Command to add new single permission to the list
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
            PermissionWrapper wrapper = new PermissionWrapper(definition);
            wrapper.setPriviledgeNameEditable(permissionProvider.isNewDefinitionAllowed());
            registerPermissionWrapper(wrapper);
        }
    }

    /**
     * Listener allowing to react for permission selection change
     */
    private class SelectPermissionListener implements ItemClickEvent.ItemClickListener {
        @Override
        public void itemClick(ItemClickEvent event) {
            removeMenuItem.setEnabled(true);
            BeanItem<PermissionWrapper> beanItem = (BeanItem<PermissionWrapper>) event.getItem();
            PermissionWrapper wrapper = beanItem.getBean();
            loadPermissionForm(wrapper);
        }
    }

    private MenuBar menuBar;
    private MenuBar.MenuItem addMenuItem;
    private MenuBar.MenuItem removeMenuItem;
    private PermissionWrapperTable permissionTable;
    private Map<PermissionWrapper, PermissionWrapperForm> permissionFormMap;
    private PermissionWrapperForm permissionForm;
    private PermissionProvider permissionProvider;
    
    public PermissionPanel() {
        super(3, 2);
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        permissionTable = new PermissionWrapperTable();
        permissionTable.addStyleName(Reindeer.TABLE_BORDERLESS);
        permissionTable.setWidth("100%");
        permissionTable.addListener(new SelectPermissionListener());

        permissionFormMap = new HashMap<PermissionWrapper, PermissionWrapperForm>();
        
        menuBar = new MenuBar();
        menuBar.setWidth("100%");
        addMenuItem = menuBar.addItem(messages.getMessage("permission.add"), null);
        removeMenuItem = menuBar.addItem(messages.getMessage("permission.remove"), new RemovePermissionCommand());
        removeMenuItem.setEnabled(false);
    }

    private void initLayout() {
        setWidth("100%");
        setColumnExpandRatio(0, 0.2f);
        setColumnExpandRatio(1, 0);
        setColumnExpandRatio(2, 0.8f);

        addComponent(menuBar, 0, 0, 2, 0);
        addComponent(new HorizontalBox("20px"), 1, 1);
        addComponent(permissionTable, 0, 1);
    }

    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(PermissionProvider permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    @Override
    public void loadData() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

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

        permissionTable.getDataSourceContainer().removeAllItems();
        if (permissionProvider.getPermissions() != null) {
            for (AbstractPermission abstractPermission : permissionProvider.getPermissions()) {
                PermissionWrapper wrapper = new PermissionWrapper(abstractPermission);
                wrapper.setPriviledgeNameEditable(permissionProvider.isNewDefinitionAllowed());
                registerPermissionWrapper(wrapper);
            }
        }
    }
    
    private void registerPermissionWrapper(PermissionWrapper wrapper) {
        BeanItem<PermissionWrapper> bean = permissionTable.getDataSourceContainer().addBean(wrapper);
        permissionFormMap.put(wrapper, new PermissionWrapperForm(bean));
    }
    
    private void unregisterPermissionWrapper(PermissionWrapper wrapper) {
        permissionTable.getDataSourceContainer().removeItem(wrapper);
        permissionFormMap.remove(wrapper);
    }

    private void loadPermissionForm(PermissionWrapper wrapper) {
        if (wrapper != null && permissionForm == permissionFormMap.get(wrapper)) {
            // Nothing to change because we display the same form as shown
            return;
        }

        if (permissionForm != null) {
            removeComponent(permissionForm);
            permissionForm = null;
        }

        if (wrapper != null) {
            permissionForm = permissionFormMap.get(wrapper);
            addComponent(permissionForm, 2, 1);

            // This is just a workaround for strange behaviour of Table component when refreshing
            // itself. This is can be related to: http://dev.vaadin.com/ticket/8298
            permissionTable.select(wrapper);
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
