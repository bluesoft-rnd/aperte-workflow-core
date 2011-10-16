package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;

/**
 * @author tlipski@bluesoft.net.pl
 */

@Entity
@Table(name="pt_user_data")
public class UserData extends PersistentEntity {
	@Column(unique = true)
	private String login;
	private String realName;
	private String email;
	private String jobTitle;
	private String company;
	private String department;
	private String superior;
	private Long companyId;

	public UserData() {
	}

	public UserData(String login, String realName, String email) {
		this.login = login;
		this.realName = realName;
		this.email = email;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
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

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String mpk) {
		this.department = mpk;
	}

	public String getSuperior() {
		return superior;
	}

	public void setSuperior(String superior) {
		this.superior = superior;
	}

	public String getCompany() {
		return company;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
}
