package org.aperteworkflow.scripting;

import pl.net.bluesoft.rnd.util.func.Func;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/23/12
 * Time: 5:32 PM
 */
public class ScriptProcessorRegistry {

    private final Map<String,Func<ScriptProcessor>> processorMap = new HashMap<String,Func<ScriptProcessor>>();

    public void registerProcessor(String name, Func<ScriptProcessor> processor){
        processorMap.put(name, processor);
    }
    
    public void unregisterProcessor(String name){
        processorMap.remove(name);
    }

    public ScriptProcessor getScriptProcessor(String name, String url, String code){
        if(processorMap.get(name) == null)
            return null;
        ScriptProcessor processor = processorMap.get(name).invoke();

        try {
            processor.configure(url, code);
            return processor;
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
