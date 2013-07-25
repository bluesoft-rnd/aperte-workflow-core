package pl.net.bluesoft.rnd.pt.ext.user.model;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Transformer;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.join;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_user_data")
public class UserData extends AbstractPersistentEntity {
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
	private String department;
	private String superior;
	private Long companyId;
    private Long liferayUserId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserRole> roles;

	public UserData() {
	}

	public UserData(String login, String realName, String email) {
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

    public UserData(String login, String firstName, String lastName, String email) {
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

    public Set<UserRole> getRoles() {
        return roles != null ? roles : (roles = new HashSet<UserRole>());
    }

    public void setRoles(Set<UserRole> roles) {
        this.roles = roles;
    }

    public Set<String> getRoleNames() {
		return Collections.collect(getRoles(), new Transformer<UserRole, String>() {
            @Override
            public String transform(UserRole obj) {
                return obj.getName();
            }
        }, new HashSet<String>());
	}

	public void setRoleNames(Set<String> roleNames) {
        Set<String> existingRoles = new HashSet<String>();
        for (Iterator<UserRole> it = getRoles().iterator(); it.hasNext(); ) {
            UserRole role = it.next();
            if (!roleNames.contains(role.getName())) {
                it.remove();
            }
            else {
                existingRoles.add(role.getName());
            }
        }
        for (String name : roleNames) {
            if (!existingRoles.contains(name)) {
                addRoleName(name);
            }
        }
	}

	public void addRoleName(String name) {
        addRoleName(name, null);
    }

    public void addRoleName(String name, String description) {
        UserRole role = null;
        for (UserRole r : getRoles()) {
            if (r.getName().equals(name)) {
                role = r;
                break;
            }
        }
        if (role == null) {
            role = new UserRole(this, name, description);
            getRoles().add(role);
        }
        else {
            role.setName(name);
            role.setDescription(description);
        }
    }

	public boolean containsRole(String roleName) {
		return getRoleNames().contains(roleName);
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

	public String getRealName() {
		return join(" ", firstName, lastName);
	}

    public void setLastName(String lastName) {
        this.lastName = lastName;
	}

    public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public Long getCompanyId() {
		return companyId;
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
		UserData other = (UserData) obj;
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getSuperior() {
		return superior;
	}

	public void setSuperior(String superior) {
		this.superior = superior;
	}


    public boolean hasRole(String roleName)
    {
        for(UserRole role: roles)
            if(role.getName().equals(roleName))
                return true;

        return false;
    }
}
