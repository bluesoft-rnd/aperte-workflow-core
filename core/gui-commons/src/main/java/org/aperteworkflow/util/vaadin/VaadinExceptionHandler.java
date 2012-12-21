package org.aperteworkflow.util.vaadin;

/**
 * Created by IntelliJ IDEA.
 * User: tomek
 * Date: 4/23/11
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public interface VaadinExceptionHandler {

    class Util {
        public static void onException(Object handler, Throwable e) {
            if (handler instanceof VaadinExceptionHandler) {
                ((VaadinExceptionHandler)handler).onThrowable(e);
            } else {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }

        public static void withErrorHandling(Object handler, Runnable x) {
            try {
                x.run();
            }
            catch (Exception e) {
                onException(handler, e);
            }
        }
    }

    void onThrowable(Throwable e);
}
