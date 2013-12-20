package pl.net.bluesoft.rnd.processtool.hibernate.lock.exception;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public class AquireOperationLockException extends Exception
{
    public AquireOperationLockException() {
    }

    public AquireOperationLockException(String message) {
        super(message);
    }

    public AquireOperationLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public AquireOperationLockException(Throwable cause) {
        super(cause);
    }
}
