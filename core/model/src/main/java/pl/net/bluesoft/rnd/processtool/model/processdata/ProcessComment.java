package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;

/**
 * @author tlipski@bluesoft.net.pl
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
	@Column(name = "id")
	protected Long id;

	@Column(name ="comment_name")
    private String comment;

    @Column(name = "comment_body", length = Integer.MAX_VALUE)
	@Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    private String body;

	private String author;
	private String authorSubstitute;

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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

    public String getAuthorSubstitute() {
        return authorSubstitute;
    }

    public void setAuthorSubstitute(String authorSubstitute) {
        this.authorSubstitute = authorSubstitute;
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
}
