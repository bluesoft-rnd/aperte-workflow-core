package pl.net.bluesoft.rnd.pt.ext.processeditor;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other.OtherTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission.ProcessPermissionTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.queue.QueueTab;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Main panel for process editor application
 */
public class ProcessEditorPanel extends VerticalLayout {

    private TabSheet tabSheet;
    private OtherTab otherTab;
    private ProcessPermissionTab permissionTab;

    public ProcessEditorPanel() {
        initComponents();
        initLayout();
    }

    private void initLayout() {
        addComponent(tabSheet);
    }

    private void initComponents() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(permissionTab = new ProcessPermissionTab(), messages.getMessage("process.editor.process.permissions"));
        tabSheet.addTab(new QueueTab(), messages.getMessage("process.editor.queues"));
        tabSheet.addTab(otherTab = new OtherTab(), messages.getMessage("process.editor.other"));
    }

    public void setProcessDir(String processDir) {
        otherTab.setProcessDir(processDir);
    }

    public void setProcessConfig(String processConfig) {
        permissionTab.setProcessConfig(processConfig);
    }
}
