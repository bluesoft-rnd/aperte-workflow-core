package pl.net.bluesoft.rnd.processtool.model.config;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_state_prms")
public class ProcessStatePermission extends AbstractPermission {
//    @XmlTransient
    @ManyToOne
    @JoinColumn(name = "state_id")
    private ProcessStateConfiguration config;

    @XmlTransient
    public ProcessStateConfiguration getConfig() {
        return config;
    }

//    @XmlTransient
    public void setConfig(ProcessStateConfiguration config) {
        this.config = config;
    }
}
