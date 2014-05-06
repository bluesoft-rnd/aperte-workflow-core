package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Created by pkuciapski on 2014-04-29.
 */
public interface IAttributesProvider {
    // todo
    ProcessInstance getProcessInstance();

    String getSimpleAttributeValue(String key);

    String getExternalKey();
}
