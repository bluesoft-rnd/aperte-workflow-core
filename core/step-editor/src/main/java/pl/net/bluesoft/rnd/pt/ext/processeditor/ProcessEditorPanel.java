package pl.net.bluesoft.rnd.pt.ext.processeditor;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other.OtherTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.queue.QueueTab;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

/**
 * Main panel for process editor application
 */
public class ProcessEditorPanel extends VerticalLayout {

    private TabSheet tabSheet;

    public ProcessEditorPanel() {
        initComponents();
        initLayout();
    }

    private void initLayout() {
        addComponent(tabSheet);
    }

    private void initComponents() {
        I18NSource messages = VaadinUtility.getThreadI18nSource();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(new QueueTab().init(), messages.getMessage("processeditor.queues"));
        tabSheet.addTab(new OtherTab().init(), messages.getMessage("processeditor.other"));

    }

}
