package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * User: POlszewski
 * Date: 2011-08-30
 * Time: 22:01:27
 */
@Entity
@Table(name="pt_user_substitution")
public class UserSubstitution extends PersistentEntity {
	public static final String _USER = "user";
	public static final String _USER_SUBSTITUTE = "userSubstitute";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserData user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_substitute_id")
    private UserData userSubstitute;
    private Date dateFrom;
    private Date dateTo;

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
