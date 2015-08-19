package pl.net.bluesoft.rnd.processtool.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard exception for business fail-overs
 * Created by mpawlak@bluesoft.net.pl on 2014-12-09.
 */
public class BusinessException extends RuntimeException
{
    private List<String> parameters = new ArrayList<String>();

    public BusinessException() {
    }

    public BusinessException(String message, String ... params) {
        super(message);

        for(String parameter: params)
            parameters.add(parameter);
    }

    public String[] getParameters()
    {
        return (String[])parameters.toArray();
    }



    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
}
