package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.*;
import java.util.Date;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_lock_operation", uniqueConstraints = {
        @UniqueConstraint(columnNames = "lock_name")})
public class OperationLock extends PersistentEntity
{
    /** Lock name, there can be only one lock with given name in database */
    @Column(name = "lock_name")
    private String lockName;

    @Column(name = "lock_mode")
    @Enumerated(EnumType.STRING)
    private OperationLockMode lockMode;

    /** Lock acquisition time */
    @Column(name = "lock_date")
    private Date lockDate;

    /**  Acquisition max time, lock will be released forcefully after this date */
    @Column(name = "lock_release_date")
    private Date lockReleaseDate;

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public OperationLockMode getLockMode() {
        return lockMode;
    }

    public void setLockMode(OperationLockMode lockMode) {
        this.lockMode = lockMode;
    }

    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    public Date getLockReleaseDate() {
        return lockReleaseDate;
    }

    public void setLockReleaseDate(Date lockReleaseDate) {
        this.lockReleaseDate = lockReleaseDate;
    }
}
