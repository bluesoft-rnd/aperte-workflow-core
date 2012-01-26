package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.queue;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

public class QueueTab extends GridLayout {

    private Button addQueueButton;
    private Button removeQueueButton;
    private GridLayout layout;

    public QueueTab() {
        super(3, 2);
        initComponents();
        initLayout();
    }

    private void initComponents() {
        I18NSource messages = VaadinUtility.getThreadI18nSource();

        addQueueButton = VaadinUtility.smallButton(messages.getMessage("processeditor.queue.add"));
        removeQueueButton = VaadinUtility.smallButton(messages.getMessage("processeditor.queue.remove"));
    }

    private void initLayout() {

    }


}
