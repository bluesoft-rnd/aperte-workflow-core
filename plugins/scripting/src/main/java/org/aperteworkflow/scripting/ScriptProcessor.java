package org.aperteworkflow.scripting;

import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/23/12
 * Time: 2:54 PM
 */
public interface ScriptProcessor {

    Map<String, Object> process(Map<String, Object> vars, InputStream script) throws Exception;
    void validate(InputStream script) throws ScriptValidationException;

}
