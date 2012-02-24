package org.aperteworkflow.scripting;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/23/12
 * Time: 2:54 PM
 */
public interface ScriptProcessor {

    void processFields(Map<String, Object> fields) throws Exception;

    void configure(String url, String code) throws InstantiationException, IllegalAccessException;
}
