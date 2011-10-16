package pl.net.bluesoft.rnd.processtool.model.config;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Configuration of a process definition.
 *
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_definition_config")
public class ProcessDefinitionConfig extends PersistentEntity implements Serializable {
	private String processName;
	private String description;
	private String bpmDefinitionKey;

	private String comment;

	@ManyToOne
	@JoinColumn(name="creator_id")
	private UserData creator;

	private Date createDate;

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="definition_id")
	private Set<ProcessStateConfiguration> states;

    @Lob
    private byte[] processLogo;
	
	/**
	 * latest definition of process with processName ensures uniqueness and versioning of definitions
	 */
	private Boolean latest;

    public byte[] getProcessLogo() {
        return processLogo;
    }

    public void setProcessLogo(byte[] processLogo) {
        this.processLogo = processLogo;
    }

    public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBpmDefinitionKey() {
		return bpmDefinitionKey;
	}

	public void setBpmDefinitionKey(String bpmDefinitionKey) {
		this.bpmDefinitionKey = bpmDefinitionKey;
	}

	public Boolean getLatest() {
		return latest;
	}

	public void setLatest(Boolean latest) {
		this.latest = latest;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public UserData getCreator() {
		return creator;
	}

	public void setCreator(UserData creator) {
		this.creator = creator;
	}

	public Set<ProcessStateConfiguration> getStates() {
		return states;
	}

	public void setStates(Set<ProcessStateConfiguration> states) {
		this.states = states;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
