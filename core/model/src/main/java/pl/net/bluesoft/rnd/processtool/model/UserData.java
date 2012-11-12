package pl.net.bluesoft.rnd.processtool.model;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;
import static pl.net.bluesoft.util.lang.FormatUtil.join;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Predicate;
import pl.net.bluesoft.util.lang.Transformer;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_user_data")
public class UserData extends UserAttributesSupport {
	@Column(unique = true)
	private String login;
	private String firstName;
	private String lastName;
	private String email;
	private String jobTitle;
	private String company;
	private String department;
	private String superior;
	private Long companyId;
    private Long liferayUserId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserAttribute> attributes;

    @Transient
    private Set<UserAttribute> orphans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
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

    public Set<String> getMainAttributeKeys() {
        return getMainAttributesMap().keySet();
    }

    public Set<UserAttribute> getMainAttributes() {
        return Collections.filter(getAttributes(), new Predicate<UserAttribute>() {
            @Override
            public boolean apply(UserAttribute input) {
                return input.getParent() == null;
            }
        }, new HashSet<UserAttribute>());
    }

    public Map<String, UserAttribute> getMainAttributesMap() {
        return Collections.transform(getAttributes(), new Transformer<UserAttribute, String>() {
            @Override
            public String transform(UserAttribute obj) {
                return obj.getParent() == null ? obj.getKey() : null;
            }
        });
    }

    @Override
    public UserAttribute findAttribute(final String key) {
        return findAttribute(getMainAttributes(), UserAttributePredicates.matchKey(key));
    }

    @Override
    public UserAttribute findAttribute(final String key, final String value) {
        return findAttribute(getMainAttributes(), UserAttributePredicates.matchKeyValue(key, value));
    }

    void addChild(UserAttribute a) {
        if (!getAttributes().contains(a)) {
            getAttributes().add(a);
        }
        if (!a.getAttributes().isEmpty()) {
            for (UserAttribute b : a.getAttributes()) {
                addChild(b);
            }
        }
        getOrphans().remove(a);
    }

    @Override
    public UserData getUser() {
        return this;
    }

    void addOrphan(UserAttribute a) {
        getAttributes().remove(a);
        if (a.getId() != null) {
            getOrphans().add(a);
        }
    }

    public Set<UserAttribute> getOrphans() {
        return orphans != null ? orphans : (orphans = new HashSet<UserAttribute>());
    }

    public void setOrphans(Set<UserAttribute> orphans) {
        this.orphans = orphans;
    }

    public void removeAllOrphans() {
        if (orphans != null) {
            orphans.clear();
        }
    }

    public Set<UserAttribute> getAttributes() {
        return attributes != null ? attributes : (attributes = new HashSet<UserAttribute>());
    }

    public void setAttributes(Set<UserAttribute> attributes) {
        this.attributes = attributes;
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

	public void setCompany(String company) {
		this.company = company;
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
		return firstName + " " + lastName + " (" + login + ")";
	}

	@Override
	public String toString() {
		return "UserData [login=" + login + ", getRealName()=" + getRealName() + "]";
	}
}
