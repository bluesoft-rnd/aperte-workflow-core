package pl.net.bluesoft.rnd.processtool.plugins;

public class PluginManagementException extends RuntimeException {

    public PluginManagementException() {
    }

    public PluginManagementException(String message) {
        super(message);
    }

    public PluginManagementException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginManagementException(Throwable cause) {
        super(cause);
    }

}
