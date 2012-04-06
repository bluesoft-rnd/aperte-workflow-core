package pl.net.bluesoft.rnd.pt.ext.sched.service;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class SchedulerServiceInternalError extends RuntimeException {
    public SchedulerServiceInternalError() {
    }

    public SchedulerServiceInternalError(String message) {
        super(message);
    }

    public SchedulerServiceInternalError(String message, Throwable cause) {
        super(message, cause);
    }

    public SchedulerServiceInternalError(Throwable cause) {
        super(cause);
    }
}
