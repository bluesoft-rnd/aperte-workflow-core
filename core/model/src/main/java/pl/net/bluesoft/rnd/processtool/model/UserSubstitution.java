package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * User: POlszewski
 * Date: 2011-08-30
 * Time: 22:01:27
 */
@Entity
@Table(name="pt_user_substitution")
public class UserSubstitution extends AbstractPersistentEntity {
	public static final String _USER_LOGIN = "userLogin";
	public static final String _USER_SUBSTITUTE_LOGIN = "userSubstituteLogin";
	public static final String _DATE_FROM = "dateFrom";
	public static final String _DATE_TO = "dateTo";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_USER_SUBST")
			}
	)
	@Column(name = "id")
	protected Long id;

    private String userLogin;
    private String userSubstituteLogin;
    private Date dateFrom;
    private Date dateTo;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserSubstituteLogin() {
		return userSubstituteLogin;
	}

	public void setUserSubstituteLogin(String userSubstituteLogin) {
		this.userSubstituteLogin = userSubstituteLogin;
	}

	public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }
}
