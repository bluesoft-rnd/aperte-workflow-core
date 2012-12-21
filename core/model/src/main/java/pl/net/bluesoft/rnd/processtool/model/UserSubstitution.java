package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
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
	public static final String _USER = "user";
	public static final String _USER_SUBSTITUTE = "userSubstitute";

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserData user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_substitute_id")
    private UserData userSubstitute;
    private Date dateFrom;
    private Date dateTo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public UserData getUserSubstitute() {
        return userSubstitute;
    }

    public void setUserSubstitute(UserData userSubstitute) {
        this.userSubstitute = userSubstitute;
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
