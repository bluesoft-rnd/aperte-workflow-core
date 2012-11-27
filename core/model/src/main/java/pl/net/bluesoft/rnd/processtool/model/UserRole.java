package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author: amichalak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_user_roles")
public class UserRole extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_USER_ROLE")
			}
	)
	@Column(name = "id")
	protected Long id;

    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserData user;

    public UserRole() {
    }

    public UserRole(UserData user, String name, String description) {
        this.user = user;
        this.name = name;
        this.description = description;
    }

    @Column(nullable = false)
    private String name;
    private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlTransient
    public UserData getUser() {
        return user;
    }

//    @XmlTransient
    public void setUser(UserData user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
