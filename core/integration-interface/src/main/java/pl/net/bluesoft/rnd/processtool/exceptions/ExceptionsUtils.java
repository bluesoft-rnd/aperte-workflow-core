package pl.net.bluesoft.rnd.processtool.exceptions;

/**
 * Created by mpawlak@bluesoft.net.pl on 2014-12-09.
 */
public class ExceptionsUtils
{
    public static boolean isExceptionOfClassExistis(Throwable rootException, Class<? extends Throwable> clazz)
    {
        if(rootException.getClass().equals(clazz))
            return true;

        if(rootException.getCause() == null)
            return false;

        return isExceptionOfClassExistis(rootException.getCause(), clazz);
    }

    public static <T extends Throwable> T getExceptionByClassFromStack(Throwable rootException, Class<T> clazz)
    {
        if(rootException.getClass().equals(clazz))
            return (T)rootException;

        if(rootException.getCause() == null)
            return null;

        return getExceptionByClassFromStack(rootException.getCause(), clazz);
    }
}
