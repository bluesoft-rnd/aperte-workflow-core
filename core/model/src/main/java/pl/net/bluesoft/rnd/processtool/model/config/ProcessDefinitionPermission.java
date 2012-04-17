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
@Table(name="pt_process_def_prms")
public class ProcessDefinitionPermission extends AbstractPermission {

//    @XmlTransient
	@ManyToOne
	@JoinColumn(name="definition_id")
	private ProcessDefinitionConfig definition;

    @XmlTransient
    public ProcessDefinitionConfig getDefinition() {
        return definition;
    }

//    @XmlTransient
    public void setDefinition(ProcessDefinitionConfig definition) {
        this.definition = definition;
    }
}
