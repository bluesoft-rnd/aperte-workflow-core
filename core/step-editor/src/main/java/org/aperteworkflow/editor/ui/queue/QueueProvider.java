package org.aperteworkflow.editor.ui.queue;


import org.aperteworkflow.editor.domain.Queue;

import java.util.List;

public interface QueueProvider extends QueueHandler {

    List<Queue> getQueues();

    void setQueues(List<Queue> queues);
    
}
