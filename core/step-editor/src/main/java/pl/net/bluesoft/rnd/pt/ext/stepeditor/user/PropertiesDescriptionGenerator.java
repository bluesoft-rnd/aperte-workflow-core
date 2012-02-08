package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

final class PropertiesDescriptionGenerator implements ItemDescriptionGenerator {
	private static final long serialVersionUID = -5377194182481601578L;

	@Override
	public String generateDescription(Component source, Object itemId, Object propertyId) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		WidgetItemInStep item = (WidgetItemInStep) itemId;

		String parameters = messages.getMessage("stepTree.no.parameters.defined");

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