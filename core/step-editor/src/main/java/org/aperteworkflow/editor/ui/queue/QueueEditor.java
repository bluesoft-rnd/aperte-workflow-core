package org.aperteworkflow.editor.ui.queue;

import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

public class QueueEditor extends VerticalLayout implements DataHandler {

    private Label queueDescriptionLabel;

    private Form newQueueForm;

    public QueueEditor() {
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        queueDescriptionLabel = new Label(messages.getMessage("queue.editor.description"));
    }

    private void initLayout() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void saveData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> validateData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
