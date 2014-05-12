package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;

/**
 *
 * Comments for process
 *
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_comment")
public class ProcessComment extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_COMMENT")
			}
	)
    @Index(name="idx_p_comment_id")
	@Column(name = "id")
	protected Long id;

	@Column(name ="comment_name")
    @Index(name="idx_p_comment_comment")
    private String comment;

    @Column(name = "comment_body", length = Integer.MAX_VALUE)
	@Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    private String body;

    @Column(name = "author_login")
	private String authorLogin;

    @Column(name = "comment_type")
    private String commentType;

    @Column(name = "author_full_name")
    private String authorFullName;

    @Column(name = "substitute_login")
	private String substituteLogin;

    @Column(name = "substitute_full_name")
    private String substituteFullName;

	private String processState;
	private Date createTime;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getProcessState() {
		return processState;
	}

	public void setProcessState(String processState) {
		this.processState = processState;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private ProcessInstance processInstance;

	public String getAuthorLogin() {
		return authorLogin;
	}

	public void setAuthorLogin(String authorLogin) {
		this.authorLogin = authorLogin;
	}

    public String getSubstituteLogin() {
        return substituteLogin;
    }

    public void setSubstituteLogin(String substituteLogin) {
        this.substituteLogin = substituteLogin;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public String getSubstituteFullName() {
        return substituteFullName;
    }

    public void setSubstituteFullName(String substituteFullName) {
        this.substituteFullName = substituteFullName;
    }
}
