package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author: amichalak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_user_roles")
public class UserRole extends PersistentEntity {
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
