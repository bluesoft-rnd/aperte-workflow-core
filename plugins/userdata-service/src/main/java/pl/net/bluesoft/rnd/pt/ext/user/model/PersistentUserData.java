package pl.net.bluesoft.rnd.pt.ext.user.model;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.join;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_user_data")
public class PersistentUserData extends AbstractPersistentEntity implements UserData {
	public static final String _LOGIN = "login";
	public static final String _REAL_NAME = "realName";
	public static final String _FILTERED_NAME = "filteredName";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_USER_DATA")
			}
	)
	@Column(name = "id")
	protected Long id;

	@Column(unique = true)
	private String login;
	private String firstName;
	private String lastName;
	private String email;
	private String jobTitle;
	private Long companyId;
    private Long liferayUserId;

	/** Type of the queues */
	@ElementCollection(fetch = FetchType.EAGER)
	@Enumerated(EnumType.STRING)
	@JoinTable(name = "pt_user_roles", joinColumns = @JoinColumn(name = "user_id"))
	private Set<String> roles = new HashSet<String>();


	public PersistentUserData() {
	}

	public PersistentUserData(String login, String realName, String email) {
		this.login = login;
		this.email = email;
        String[] names = realName.split("\\s", 2);
        if (names.length == 2) {
            this.firstName = names[0];
            this.lastName = names[1];
	}
        else if (names.length == 1) {
            this.firstName = "";
            this.lastName = names[0];
        }
	}

    public PersistentUserData(String login, String firstName, String lastName, String email) {
        this.login = login;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

    @Override
	public Set<String> getRoles() {
        return roles != null ? roles : (roles = new HashSet<String>());
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

	public void addRole(String name) {
        roles.add(name);
    }

	@Override
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

    @Override
	public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	@Override
	public String getRealName() {
		return join(" ", firstName, lastName);
	}

    public void setLastName(String lastName) {
        this.lastName = lastName;
	}

    @Override
	public String getLastName() {
		return lastName;
	}

	@Override
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	@Override
	public Long getCompanyId() {
		return companyId;
	}

    @Override
    public Object getAttribute(String key) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

    public Long getLiferayUserId() {
        return liferayUserId;
	}

    public void setLiferayUserId(Long liferayUserId) {
        this.liferayUserId = liferayUserId;
	}

    /**
     * Name used in comboboxes to find user
     * @return
     */
    @Override
	public String getFilteredName() {
		return firstName + ' ' + lastName + " (" + login + ')';
	}

	@Override
	public String toString() {
		return "UserData [login=" + login + ", getRealName()=" + getRealName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (login == null ? 0 : login.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersistentUserData other = (PersistentUserData) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		return true;
	}

    @Override
	public boolean hasRole(String roleName) {
        return roles.contains(roleName);
    }
}
