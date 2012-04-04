package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class PersistentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(
            name = "idGenerator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "initial_value", value = "" + 1),
                    @Parameter(name = "value_column", value = "_DB_ID"),
                    @Parameter(name = "sequence_name", value = "DB_SEQ_ID")
            }
    )
    @Column(name = "id")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

/*    @Version
    @Column(name = "optlock")
    protected Integer optLock;

    public Integer getOptLock() {
        return optLock;
    }

    public void setOptLock(Integer optLock) {
        this.optLock = optLock;
    }*/
}
