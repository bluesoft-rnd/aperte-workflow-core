package pl.net.bluesoft.rnd.pt.ext.user.model;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author: amichalak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_user_role")
public class PersistentUserRole extends AbstractPersistentEntity {
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

    public PersistentUserRole() {
    }

    public PersistentUserRole(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Column(nullable = false)
    private String name;
    private String description;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
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
