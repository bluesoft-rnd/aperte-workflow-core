package pl.net.bluesoft.rnd.processtool.model.processdata;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_comments")
public class ProcessComments extends ProcessInstanceAttribute {

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="comments_id")
	private Set<ProcessComment> comments;

	public Set<ProcessComment> getComments() {
		if (comments == null)
			comments = new HashSet<ProcessComment>();
		return comments;
	}

	public void setComments(Set<ProcessComment> comments) {
		this.comments = comments;
	}
}
