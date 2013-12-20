package pl.net.bluesoft.rnd.processtool.hibernate.lock;

import pl.net.bluesoft.rnd.processtool.model.OperationLockMode;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class OperationOptions
{
    private String lockName;
    private OperationLockMode mode;
    private Integer expireAfterMinutes;

    public OperationOptions(String lockName, OperationLockMode mode, Integer expireAfterMinutes) {
        this.lockName = lockName;
        this.mode = mode;
        this.expireAfterMinutes = expireAfterMinutes;
    }

    public String getLockName() {
        return lockName;
    }

    public OperationLockMode getMode() {
        return mode;
    }

    public Integer getExpireAfterMinutes() {
        return expireAfterMinutes;
    }
}
