package pl.net.bluesoft.rnd.processtool.template;

public class ProcessToolTemplateErrorException extends RuntimeException {
    public ProcessToolTemplateErrorException() {
    }

    public ProcessToolTemplateErrorException(String message) {
        super(message);
    }

    public ProcessToolTemplateErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessToolTemplateErrorException(Throwable cause) {
        super(cause);
    }
}
