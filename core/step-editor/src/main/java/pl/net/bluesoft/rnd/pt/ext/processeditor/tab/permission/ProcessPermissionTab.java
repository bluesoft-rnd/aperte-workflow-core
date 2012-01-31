package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import pl.net.bluesoft.rnd.pt.ext.processeditor.domain.ProcessConfig;
import pl.net.bluesoft.rnd.pt.ext.processeditor.json.ProcessConfigJSONHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;

import java.util.Collection;

public class ProcessPermissionTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;

    private PermissionEditor permissionEditor;
    private ProcessConfigJSONHandler processConfigHandler;
    private ProcessPermissionProvider permissionProvider;

    public ProcessPermissionTab() {
        initComponent();
    }

    private void initComponent() {
        processConfigHandler = ProcessConfigJSONHandler.getInstance();

        permissionProvider = new ProcessPermissionProvider();

        permissionEditor = new PermissionEditor();
        permissionEditor.setProvider(permissionProvider);

        setMargin(true);
        addComponent(permissionEditor);
    }

    @Override
    public void loadData() {
        // load the permissions
        permissionProvider.setPermissions(processConfig.getProcessPermissions());

        // load the editor
        permissionEditor.loadData();
    }

    @Override
    public void saveData() {
        // save the editor
        permissionEditor.saveData();

        // save permissions
        processConfig.setProcessPermissions(permissionEditor.getPermissions());
    }

    @Override
    public Collection<String> validateData() {
        return permissionEditor.validateData();
    }

    public ProcessConfig getProcessConfig() {
        return processConfig;
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }
}
