package pl.net.bluesoft.rnd.pt.utils.jdbc.exception;

public class DataSourceDiscoveryFailedException extends Exception {
    public DataSourceDiscoveryFailedException() {
    }

    public DataSourceDiscoveryFailedException(String message) {
        super(message);
    }

    public DataSourceDiscoveryFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceDiscoveryFailedException(Throwable cause) {
        super(cause);
    }
}
