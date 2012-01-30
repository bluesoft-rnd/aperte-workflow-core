package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_state_prms")
public class ProcessStatePermission extends AbstractPermission {
    @ManyToOne
    @JoinColumn(name = "state_id")
    private ProcessStateConfiguration config;

    public ProcessStateConfiguration getConfig() {
        return config;
    }

    public void setConfig(ProcessStateConfiguration config) {
        this.config = config;
    }
}
