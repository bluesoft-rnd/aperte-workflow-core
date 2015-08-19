package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-05-08.
 */
@Entity
@Table(name = "pt_case_comment", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_comment",
        indexes = {
                @Index(name = "idx_pt_case_comment_pk",
                        columnNames = {"id"}
                ),
                @Index(name = "idx_pt_case_com_attr_id", columnNames = "case_comment_id")
        })
public class CaseComment extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseComment.class.getAnnotation(Table.class).name();
    @Column(name = "comment_body", length = Integer.MAX_VALUE)
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String body;

    @Column(name = "author_login")
    private String authorLogin;

    @Column(name = "author_full_name")
    private String authorFullName;

    @Column(name = "comment_type")
    private String commentType;

    @Column(name = "process_state")
    private String processState;

    @Column(name = "create_date")
    private Date createDate;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public void setAuthorLogin(String authorLogin) {
        this.authorLogin = authorLogin;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public String getProcessState() {
        return processState;
    }

    public void setProcessState(String processState) {
        this.processState = processState;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getCreateTime() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public String getFormattedDate(String format)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        //TODO user timezone
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        return simpleDateFormat.format(getCreateTime());
    }
}
