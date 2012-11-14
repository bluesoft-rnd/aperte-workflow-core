package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
public abstract class AbstractPersistentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

	public static final String _ID = "id";

    public abstract Long getId();
    public abstract void setId(Long id);
}
