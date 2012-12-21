package pl.net.bluesoft.rnd.processtool.dict.exception;

public class DictionaryLoadingException extends RuntimeException {
    public DictionaryLoadingException() {
    }

    public DictionaryLoadingException(String message) {
        super(message);
    }

    public DictionaryLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DictionaryLoadingException(Throwable cause) {
        super(cause);
    }
}
