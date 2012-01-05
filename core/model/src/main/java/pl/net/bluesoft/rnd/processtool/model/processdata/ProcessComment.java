package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import javax.persistence.*;
import java.util.Date;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_comment")
public class ProcessComment extends PersistentEntity {
	@Column(name ="comment_name")
    private String comment;

    @Column(name = "comment_body", length = Integer.MAX_VALUE)
	@Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    private String body;

	@ManyToOne(cascade = {})
	@JoinColumn(name= "author_id")
	private UserData author;

    @ManyToOne(cascade = {})
	@JoinColumn(name= "author_substitute_id")
	private UserData authorSubstitute;

	private String processState;
	private Date createTime;

	public String getProcessState() {
		return processState;
	}

	public void setProcessState(String processState) {
		this.processState = processState;
	}

	@ManyToOne
	@JoinColumn(name = "comments_id")
	private ProcessComments comments;


	public UserData getAuthor() {
		return author;
	}

	public void setAuthor(UserData author) {
		this.author = author;
	}

    public UserData getAuthorSubstitute() {
        return authorSubstitute;
    }

    public void setAuthorSubstitute(UserData authorSubstitute) {
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

	public ProcessComments getComments() {
		return comments;
	}

	public void setComments(ProcessComments comments) {
		this.comments = comments;
	}
}
