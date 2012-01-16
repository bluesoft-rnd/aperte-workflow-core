package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;

import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;

import com.vaadin.ui.Component;
import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;

final class PropertiesDescriptionGenerator implements ItemDescriptionGenerator {
	private static final long serialVersionUID = -5377194182481601578L;

	@Override
	public String generateDescription(Component source, Object itemId, Object propertyId) {
		WidgetItemInStep item = (WidgetItemInStep) itemId;

		String parameters = Messages.getString("stepTree.no.parameters.defined");

		if (item.getProperties() != null && item.getProperties().size() > 0) {
			Collection<?> properties = CollectionUtils.collect(item.getProperties(), new Transformer() {
				@Override
				public Object transform(Object arg0) {
					Property<?> property = (Property<?>) arg0;
					return property.getName() + ": " + (property.getValue() == null ? "" : property.getValue());
				}
			});

			parameters = StringUtils.join(properties.toArray(), "<br/>");
		}

		return "<b>" + item.getWidgetItem().getDescription() + "</b><br/>" + parameters;
	}
}