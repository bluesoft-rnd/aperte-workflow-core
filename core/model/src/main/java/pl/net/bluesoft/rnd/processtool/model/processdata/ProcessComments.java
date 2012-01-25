package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.aperteworkflow.search.ProcessInstanceSearchAttribute;
import org.aperteworkflow.search.Searchable;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;

import javax.persistence.*;
import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_process_comments")
public class ProcessComments extends ProcessInstanceAttribute implements Searchable {

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

    @Override
    public Collection<ProcessInstanceSearchAttribute> getAttributes() {
        List<ProcessInstanceSearchAttribute> attrs = new ArrayList<ProcessInstanceSearchAttribute>();
        for (ProcessComment pc : comments) {
            attrs.add(new ProcessInstanceSearchAttribute("comment_body", pc.getBody()));
            attrs.add(new ProcessInstanceSearchAttribute("comment_title", pc.getComment()));
        }
        return attrs;
    }
}
