package org.aperteworkflow.editor.ui.queue;

import org.aperteworkflow.editor.domain.Queue;

public interface QueueHandler {
 
    void addQueue(Queue queue);
    
    void removeQueue(Queue queue);
    
}
