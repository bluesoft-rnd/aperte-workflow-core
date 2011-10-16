package pl.net.bluesoft.rnd.pt.ext.filescapture.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Agata Taraszkiewicz
 */
@Entity
@Table(name = "pt_files_checker_config")
public class FilesCheckerConfiguration extends PersistentEntity {
    @Column(length = 4000)
    private String filesProperties;

    @Column
    private String automaticUser;

    @OneToMany(cascade = {CascadeType.ALL})
	@JoinColumn(name="configuration_id")
	private Set<FilesCheckerRuleConfiguration> rules = new HashSet();

    public String getAutomaticUser() {
        return automaticUser;
    }

    public void setAutomaticUser(String automaticUser) {
        this.automaticUser = automaticUser;
    }

    public String getFilesProperties() {
        return filesProperties;
    }

    public void setFilesProperties(String filesProperties) {
        this.filesProperties = filesProperties;
    }

    public Set<FilesCheckerRuleConfiguration> getRules() {
        return rules;
    }

    public void setRules(Set<FilesCheckerRuleConfiguration> rules) {
        this.rules = rules;
    }
}
