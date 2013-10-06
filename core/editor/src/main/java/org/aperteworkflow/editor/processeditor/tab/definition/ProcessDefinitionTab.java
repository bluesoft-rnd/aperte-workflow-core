package org.aperteworkflow.editor.processeditor.tab.definition;

import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

import static org.aperteworkflow.util.vaadin.VaadinUtility.htmlLabel;
import static org.aperteworkflow.util.vaadin.VaadinUtility.styled;

public class ProcessDefinitionTab extends VerticalLayout implements DataHandler {
    private ProcessConfig processConfig;

	private Label externalKeyLabel;
	private Label externalKeyInfoLabel;
	private TextField externalKeyField;

	private Label commentLabel;
    private Label commentInfoLabel;
    private RichTextArea commentArea;
    
    private Label descriptionLabel;
    private Label descriptionInfoLabel;
    private TextField descriptionField;

	public ProcessDefinitionTab() {
        initComponent();
        initLayout();
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

		externalKeyLabel = styled(new Label(messages.getMessage("process.externalKeyPattern.label")), "h2");
		externalKeyInfoLabel = htmlLabel(messages.getMessage("process.externalKeyPattern.description"));

		externalKeyField = new TextField();
		externalKeyField.setWidth("100%");
		externalKeyField.setNullRepresentation("");

        descriptionLabel = styled(new Label(messages.getMessage("process.definition.description")), "h2");
        descriptionInfoLabel = htmlLabel(messages.getMessage("process.definition.description.info"));
        descriptionField = new TextField();
        descriptionField.setNullRepresentation("");
        descriptionField.setWidth("100%");

        commentLabel = styled(new Label(messages.getMessage("process.definition.comment")), "h2");
        commentInfoLabel = htmlLabel(messages.getMessage("process.definition.comment.info"));
        commentArea = new RichTextArea();
        commentArea.setNullRepresentation("");
        commentArea.setWidth("100%");
    }

    private void initLayout() {
        setSpacing(true);
        setMargin(true);

		addComponent(externalKeyLabel);
		addComponent(externalKeyInfoLabel);
		addComponent(externalKeyField);

        addComponent(descriptionLabel);
        addComponent(descriptionInfoLabel);
        addComponent(descriptionField);

        addComponent(commentLabel);
        addComponent(commentInfoLabel);
        addComponent(commentArea);
    }

    @Override
    public void loadData() 
    {
		externalKeyField.setValue(processConfig.getExternalKeyPattern());
        commentArea.setValue(processConfig.getComment());
        descriptionField.setValue(processConfig.getDescription());
    }

    @Override
    public void saveData() {
		processConfig.setExternalKeyPattern((String)externalKeyField.getValue());
        processConfig.setComment((String) commentArea.getValue());
        processConfig.setDescription((String) descriptionField.getValue());
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
