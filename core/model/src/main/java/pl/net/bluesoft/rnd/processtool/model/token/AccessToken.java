package pl.net.bluesoft.rnd.processtool.model.token;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GenericGenerator;

import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

/**
 * Token which is used to fast login to system
 * 
 * @author mpawlak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_access_token")
public class AccessToken extends AbstractPersistentEntity 
{
	private static final long serialVersionUID = -2222806682614652003L;
	
	public static final String _TOKEN = "token";
	public static final String _TASK_ID = "taskId";
	public static final String _USER_LOGIN = "userLogin";
	public static final String _GENERATION_DATE = "generationDate";
	
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_TOKEN")
			}
	)
	
	/** Entity ID */
	@Column(name = "id")
	protected Long id;

	/** Token value */
	@Column(name ="token")
    private String token;
	
	/** Token value */
	@Column(name ="task_id")
	private Long taskId;

	/** Token user's login */
	@JoinColumn(name= "user_login")
	private String userLogin;
	
	/** Action to perform */
	@JoinColumn(name= "action_name")
	private String actionName;

	/** Token generation time */
	@Generated(GenerationTime.INSERT)
	@Column(name = "generation_date")
	private Date generationDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUser() {
		return userLogin;
	}

	public void setUser(String userLogin) {
		this.userLogin = userLogin;
	}

	public Date getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(Date generationDate) {
		this.generationDate = generationDate;
	}
	
	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
}
