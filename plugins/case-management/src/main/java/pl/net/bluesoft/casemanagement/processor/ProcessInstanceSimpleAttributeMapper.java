package pl.net.bluesoft.casemanagement.processor;

import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.plugins.AttributesMapper;
import pl.net.bluesoft.rnd.processtool.plugins.IAttributesMapper;

/**
 * Created by pkuciapski on 2014-05-08.
 */
@AttributesMapper(forAttributeClass = ProcessInstanceSimpleAttribute.class)
public class ProcessInstanceSimpleAttributeMapper implements IAttributesMapper<ProcessInstanceSimpleAttribute> {

    @Override
    public void map(ProcessInstanceSimpleAttribute attribute, IAttributesConsumer consumer, IAttributesProvider provider) {
       	String oldValue = consumer.getSimpleAttributeValue(attribute.getKey());
		String newValue = attribute.getValue();

        consumer.setSimpleAttribute(attribute.getKey(), newValue);
		AuditLogContext.get().addSimple(attribute.getKey(), oldValue, newValue);
    }
}
