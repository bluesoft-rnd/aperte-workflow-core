package pl.net.bluesoft.rnd.processtool.model;

import java.io.Serializable;
import java.util.*;

import static pl.net.bluesoft.util.lang.FormatUtil.join;

/**
 * User: POlszewski
 * Date: 2013-07-25
 * Time: 15:20
 */
public class UserDataBean implements UserData {
	private String login;
	private String firstName;
	private String lastName;
	private String email;
	private String jobTitle;
	private Long companyId;
    private Map<String, Object> attributes = new HashMap<String, Object>();

	private Set<String> roles = new HashSet<String>();

	public UserDataBean() {
	}

	public UserDataBean(String login) {
		this.login = login;
	}

	public UserDataBean(UserData userData) {
		this.login = userData.getLogin();
		this.firstName = userData.getFirstName();
		this.lastName = userData.getLastName();
		this.email = userData.getEmail();
		this.jobTitle = userData.getJobTitle();
		this.companyId = userData.getCompanyId();
		this.roles.addAll(userData.getRoles());
        this.attributes.putAll(userData.getAttributes());
	}



	public UserDataBean(String login, String realName, String email) {
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

	public UserDataBean(String login, String firstName, String lastName, String email) {
		this.login = login;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
	}

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
	public Set<String> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	public void addRole(String name) {
		roles.add(name);
	}

	@Override
	public boolean hasRole(String roleName) {
		return roles.contains(roleName);
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
        if (attributes != null)
            return attributes.get(key);
        else
            return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setCompanyId(Long companyId) {
		this.companyId = companyId;
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
		UserDataBean other = (UserDataBean) obj;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		return true;
	}
}
