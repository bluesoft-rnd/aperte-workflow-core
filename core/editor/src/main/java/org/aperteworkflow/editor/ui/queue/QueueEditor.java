package org.aperteworkflow.editor.ui.queue;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Queue;
import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.*;

public class QueueEditor extends VerticalLayout implements QueueHandler, DataHandler {

    private QueueProvider provider;

    private TextField addQueueNameField;
    private Button addQueueButton;
    private Label queueDescriptionLabel;
    private Map<Queue, SingleQueueEditor> queueEditors;

    public QueueEditor() {
        queueEditors = new HashMap<Queue, SingleQueueEditor>();
        initComponent();
        initLayout();
    }

    private void initComponent() {
        final I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        queueDescriptionLabel = new Label(messages.getMessage("queue.editor.description"));

        addQueueNameField = new TextField();
        addQueueNameField.setNullRepresentation("");
        addQueueNameField.setInputPrompt(messages.getMessage("queue.editor.name.prompt"));

        addQueueButton = VaadinUtility.button(messages.getMessage("queue.editor.add"), new Runnable() {
            @Override
            public void run() {
                String queueName = (String) addQueueNameField.getValue();
                if (queueName == null || queueName.trim().isEmpty()) {
                    getApplication().getMainWindow().showNotification(
                            messages.getMessage("queue.new.no.name"),
                            Window.Notification.TYPE_TRAY_NOTIFICATION
                    );
                    return;
                }

                addQueueNameField.setValue(null);

                Queue queue = new Queue();
                queue.setName(queueName);
                addQueue(queue);
            }
        });
    }

    private void initLayout() {
        setSpacing(true);

        addComponent(queueDescriptionLabel);

        HorizontalLayout addQueueLayout = new HorizontalLayout();
        addQueueLayout.setSpacing(true);
        addQueueLayout.addComponent(addQueueNameField);
        addQueueLayout.addComponent(addQueueButton);

        addComponent(addQueueLayout);
    }

    public QueueProvider getProvider() {
        return provider;
    }

    public void setProvider(QueueProvider provider) {
        this.provider = provider;
    }

    @Override
    public void loadData() {
        List<Queue> queues = provider.getQueues();
        if (queues != null && !queues.isEmpty()) {
            for (Queue queue : queues) {
                addQueue(queue);
            }
        }
    }

    @Override
    public void saveData() {
        List<Queue> queues = new ArrayList<Queue>(queueEditors.keySet());
        provider.setQueues(queues);
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    @Override
    public void addQueue(Queue queue) {
        if (queueEditors.containsKey(queue)) {
            // duplicated queue, do not add editor
            return;
        }

        SingleQueueEditor editor = new SingleQueueEditor(queue, this);
        editor.loadData();

        queueEditors.put(queue, editor);
        addComponent(editor);
    }

    @Override
    public void removeQueue(Queue queue) {
        if (!queueEditors.containsKey(queue)) {
            // nothing to remove from editors
            return;
        }

        SingleQueueEditor editor = queueEditors.get(queue);
        queueEditors.remove(queue);
        removeComponent(editor);
    }
}
