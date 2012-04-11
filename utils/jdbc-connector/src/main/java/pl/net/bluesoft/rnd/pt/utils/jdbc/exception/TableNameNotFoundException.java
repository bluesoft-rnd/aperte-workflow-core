package pl.net.bluesoft.rnd.pt.utils.jdbc.exception;

public class TableNameNotFoundException extends RuntimeException {
    public TableNameNotFoundException() {
    }

    public TableNameNotFoundException(String message) {
        super(message);
    }

    public TableNameNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableNameNotFoundException(Throwable cause) {
        super(cause);
    }
}
