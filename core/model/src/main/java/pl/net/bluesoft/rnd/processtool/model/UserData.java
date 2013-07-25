package pl.net.bluesoft.rnd.processtool.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.FormatUtil.join;

/**
 * @author tlipski@bluesoft.net.pl
 */

public class UserData {
	public static final String _LOGIN = "login";
	public static final String _REAL_NAME = "realName";
	public static final String _FILTERED_NAME = "filteredName";

	private String login;
	private String firstName;
	private String lastName;
	private String email;
	private String jobTitle;
	private String department;
	private String superior;
	private Long companyId;

    private Set<String> roles = new HashSet<String>();

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

    public Set<String> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public Set<String> getRoleNames() {
		return getRoles();
	}

	public void addRoleName(String name) {
        roles.add(name);
    }

	public boolean containsRole(String roleName) {
		return roles.contains(roleName);
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

	/**
     * Name used in comboboxes to find user
     * @return
     */
    public String getFilteredName() {
		return firstName + ' ' + lastName + " (" + login + ')';
	}

	public boolean hasRole(String roleName) {
		return roles.contains(roleName);
	}

	@Override
	public String toString() {
		return "UserData [login=" + login + ", getRealName()=" + getRealName() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		return true;
	}
}
