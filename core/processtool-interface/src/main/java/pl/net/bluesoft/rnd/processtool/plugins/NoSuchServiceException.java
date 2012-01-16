package pl.net.bluesoft.rnd.processtool.plugins;

public class NoSuchServiceException extends RuntimeException {
    public NoSuchServiceException() {
    }

    public NoSuchServiceException(String message) {
        super(message);
    }

    public NoSuchServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchServiceException(Throwable cause) {
        super(cause);
    }
}
