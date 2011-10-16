package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class PersistentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
