package pl.net.bluesoft.rnd.pt.ext.processeditor.domain;

import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;

import java.io.Serializable;
import java.util.List;

/**
 * Main configuration for the process editor
 */
public class ProcessConfig implements Serializable {

    private List<AbstractPermission> processPermissions;
    private List<Queue> queues;

    public List<AbstractPermission> getProcessPermissions() {
        return processPermissions;
    }

    public void setProcessPermissions(List<AbstractPermission> processPermissions) {
        this.processPermissions = processPermissions;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public void setQueues(List<Queue> queues) {
        this.queues = queues;
    }
}
