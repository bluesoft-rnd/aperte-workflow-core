package org.aperteworkflow.editor.processeditor.tab.message;


import com.vaadin.ui.*;

public class ConfirmWindow extends Window implements Button.ClickListener {

    private Label messageLabel;
    private Button cancelButton;
    private Button confirmButton;
    
    public ConfirmWindow() {
        initComponent();
        initLayout();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        close();
    }

    private void initLayout() {
        setModal(true);

        // use custom GridLayout to position elements
        GridLayout layout = new GridLayout(2, 2);
        layout.addComponent(messageLabel, 0, 0, 1, 0);
        layout.addComponent(cancelButton, 0, 1);
        layout.addComponent(confirmButton, 1, 1);
        layout.setComponentAlignment(cancelButton, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(confirmButton, Alignment.MIDDLE_CENTER);
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setSpacing(true);

        // make the window adjust to the content size
        layout.setSizeUndefined();

        setContent(layout);
    }

    private void initComponent() {
        confirmButton = new Button("confirm.window.confirm");
        confirmButton.addListener(this);
        
        cancelButton = new Button("confirm.window.cancel");
        cancelButton.addListener(this);
        
        messageLabel = new Label("confirm.window.message.default");
        messageLabel.setContentMode(Label.CONTENT_XHTML);
    }
    
    public void addConfirmListener(Button.ClickListener listener) {
        confirmButton.addListener(listener);
    }
    
    public void addCancelListener(Button.ClickListener listener) {
        cancelButton.addListener(listener);
    }
    
    public void setConfirmCaption(String caption) {
        confirmButton.setCaption(caption);
    }

    public void setCancelCaption(String caption) {
        cancelButton.setCaption(caption);
    }

    public void setMessageValue(String value) {
       messageLabel.setValue(value);
    }


}
