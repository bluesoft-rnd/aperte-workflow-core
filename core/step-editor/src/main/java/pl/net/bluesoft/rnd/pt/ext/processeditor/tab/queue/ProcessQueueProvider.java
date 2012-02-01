package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.queue;

import org.aperteworkflow.editor.domain.Queue;
import org.aperteworkflow.editor.ui.queue.QueueProvider;

import java.util.List;

public class ProcessQueueProvider implements QueueProvider {

    private List<Queue> queues;
    
    @Override
    public List<Queue> getQueues() {
        return queues;
    }

    @Override
    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }

    @Override
    public void addQueue(Queue queue) {
        queues.add(queue);
    }

    @Override
    public void removeQueue(Queue queue) {
        queues.remove(queue);
    }
}
