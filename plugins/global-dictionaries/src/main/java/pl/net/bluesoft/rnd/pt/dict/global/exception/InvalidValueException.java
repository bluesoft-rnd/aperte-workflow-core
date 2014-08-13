package pl.net.bluesoft.rnd.pt.dict.global.exception;

/**
 * Created by pkuciapski on 2014-06-05.
 */
public class InvalidValueException extends Exception {
    public InvalidValueException(String s) {
        super(s);
    }

    public InvalidValueException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidValueException(Throwable throwable) {
        super(throwable);
    }
}
