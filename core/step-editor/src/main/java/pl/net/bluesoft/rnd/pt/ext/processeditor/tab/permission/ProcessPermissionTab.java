package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import pl.net.bluesoft.rnd.pt.ext.processeditor.json.ProcessConfigJSONHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;

import java.util.Collection;

public class ProcessPermissionTab extends VerticalLayout implements DataHandler {

    private String processConfig;
    private PermissionEditor permissionEditor;
    private ProcessConfigJSONHandler processConfigHandler;
    private ProcessPermissionProvider permissionProvider;

    public ProcessPermissionTab() {
        initComponent();
    }

    private void initComponent() {
        processConfigHandler = ProcessConfigJSONHandler.getInstance();

        permissionProvider = new ProcessPermissionProvider();
        permissionProvider.setPermissions(processConfigHandler.getPermissions(processConfig));

        permissionEditor = new PermissionEditor();
        permissionEditor.setProvider(permissionProvider);

        setMargin(true);
        addComponent(permissionEditor);
    }

    @Override
    public void loadData() {
        // read the permissions from the json
        permissionProvider.setPermissions(ProcessConfigJSONHandler.getInstance().getPermissions(processConfig));

        permissionEditor.loadData();
    }

    @Override
    public void saveData() {
        permissionEditor.saveData();
    }

    @Override
    public Collection<String> validateData() {
        return permissionEditor.validateData();
    }

    public void setProcessConfig(String processConfig) {
        this.processConfig = processConfig;
    }
}
