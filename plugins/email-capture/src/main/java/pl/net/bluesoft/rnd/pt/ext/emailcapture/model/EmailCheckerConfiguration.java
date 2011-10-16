package pl.net.bluesoft.rnd.pt.ext.emailcapture.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_email_checker_config")
public class EmailCheckerConfiguration extends PersistentEntity {
	@Column(length = 4000)
	private String mailSessionProperties;

	@Column
	private String automaticUser;

	@OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="configuration_id")
	private Set<EmailCheckerRuleConfiguration> rules = new HashSet();

	public String getAutomaticUser() {
		return automaticUser;
	}

	public void setAutomaticUser(String automaticUser) {
		this.automaticUser = automaticUser;
	}

	public String getMailSessionProperties() {
		return mailSessionProperties;
	}

	public void setMailSessionProperties(String mailSessionProperties) {
		this.mailSessionProperties = mailSessionProperties;
	}

	public Set<EmailCheckerRuleConfiguration> getRules() {
		return rules;
	}

	public void setRules(Set<EmailCheckerRuleConfiguration> rules) {
		this.rules = rules;
	}
}
