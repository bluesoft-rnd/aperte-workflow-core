package pl.net.bluesoft.casemanagement.processor;

import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleLargeAttribute;
import pl.net.bluesoft.rnd.processtool.plugins.AttributesMapper;
import pl.net.bluesoft.rnd.processtool.plugins.IAttributesMapper;

/**
 * User: POlszewski
 * Date: 2014-06-11
 */
@AttributesMapper(forAttributeClass = ProcessInstanceSimpleLargeAttribute.class)
public class ProcessInstanceSimpleLargeAttributeMapper implements IAttributesMapper<ProcessInstanceSimpleLargeAttribute> {
    @Override
    public void map(ProcessInstanceSimpleLargeAttribute attribute, IAttributesConsumer consumer, IAttributesProvider provider) {
		String oldValue = consumer.getSimpleAttributeValue(attribute.getKey());
		String newValue = attribute.getValue();

        consumer.setSimpleLargeAttribute(attribute.getKey(), newValue);
		AuditLogContext.get().addSimple(attribute.getKey(), oldValue, newValue);
    }
}
