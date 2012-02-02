package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.message;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;

import java.util.Collection;

public class MessageTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;

    private TrivialMessageEditor editor;

    public MessageTab() {
        initComponent();
    }

    private void initComponent() {
        editor = new TrivialMessageEditor();

        setMargin(true);
        addComponent(editor);
    }

    @Override
    public void loadData() {
        editor.setMessagesContent(processConfig.getMessages());

        editor.loadData();
    }

    @Override
    public void saveData() {
        editor.saveData();

        processConfig.setMessages(editor.getMessagesContent());
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

}
