package org.aperteworkflow.editor.processeditor.tab.definition;

import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.htmlLabel;
import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.styled;

public class ProcessDefinitionTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;
    
    private Label commentLabel;
    private Label commentInfoLabel;
    private RichTextArea commentArea;
    
    public ProcessDefinitionTab() {
        initComponent();
        initLayout();
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        commentLabel = styled(new Label(messages.getMessage("process.definition.comment")), "h2");
        commentInfoLabel = htmlLabel(messages.getMessage("process.definition.comment.info"));
        commentArea = new RichTextArea();
        commentArea.setNullRepresentation("");
        commentArea.setWidth("100%");
    }

    private void initLayout() {
        setSpacing(true);
        setMargin(true);

        addComponent(commentLabel);
        addComponent(commentInfoLabel);
        addComponent(commentArea);
    }

    @Override
    public void loadData() {
        commentArea.setValue(processConfig.getComment());
    }

    @Override
    public void saveData() {
        processConfig.setComment((String) commentArea.getValue());
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

}
