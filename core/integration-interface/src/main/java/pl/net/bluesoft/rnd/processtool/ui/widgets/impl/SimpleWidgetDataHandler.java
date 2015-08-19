package pl.net.bluesoft.rnd.processtool.ui.widgets.impl;

import pl.net.bluesoft.rnd.processtool.auditlog.AuditLogContext;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IKeysToIgnoreProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.util.Collection;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Simple data handler mapping given data to simple attributes
 *
 * @author mpawlak@bluesoft.net.pl
 */
public class SimpleWidgetDataHandler implements IWidgetDataHandler {
    private static final String TYPE_SIMPLE = "simple";
    private static final String TYPE_SIMPLE_LARGE = "large";

    private IKeysToIgnoreProvider keysToIgnoreProvider = null;

    @Override
    public void handleWidgetData(IAttributesConsumer consumer, WidgetData data) {
		ProcessInstance process = consumer.getProcessInstance();

		Collection<WidgetDataEntry> dataEntries = data.getEntriesByType(TYPE_SIMPLE);
		dataEntries.addAll(data.getEntriesByType(TYPE_SIMPLE_LARGE));

		for (WidgetDataEntry widgetData : dataEntries) {
			String key = widgetData.getKey();

			ProcessInstance processToSave = widgetData.getSaveToRoot() ? getRootProcess(process) : process;
			IAttributesConsumer consumerToSave = nvl(processToSave, consumer);

			if (!isIgnored(key)) {
				String oldValue = getOldValue(consumerToSave, widgetData);
				String newValue = widgetData.getValue();

				AuditLogContext.get().addSimple(key, oldValue, newValue);
			}

			setNewValue(consumerToSave, widgetData);
		}
	}

	private boolean isIgnored(String key) {
		return keysToIgnoreProvider != null && keysToIgnoreProvider.getKeysToIgnore().contains(key);
	}

	private static ProcessInstance getRootProcess(ProcessInstance process) {
		return process != null ? nvl(process.getRootProcessInstance(), process) : null;
	}

	private String getOldValue(IAttributesProvider process, WidgetDataEntry data) {
        if (TYPE_SIMPLE.equals(data.getType())) {
			return process.getSimpleAttributeValue(data.getKey());
		}
        else if (TYPE_SIMPLE_LARGE.equals(data.getType())) {
			return process.getSimpleLargeAttributeValue(data.getKey());
		}
        return null;
    }

	private void setNewValue(IAttributesConsumer process, WidgetDataEntry data) {
        String escapedData = data.getValue();

        if (TYPE_SIMPLE.equals(data.getType()))
            process.setSimpleAttribute(data.getKey(), escapedData);
        else if (TYPE_SIMPLE_LARGE.equals(data.getType()))
            process.setSimpleLargeAttribute(data.getKey(), escapedData);
    }

    public void setKeysToIgnore(IKeysToIgnoreProvider provider) {
        this.keysToIgnoreProvider = provider;
    }
}
