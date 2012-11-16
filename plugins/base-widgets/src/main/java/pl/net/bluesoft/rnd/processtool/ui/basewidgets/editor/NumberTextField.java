package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import org.apache.commons.beanutils.ConvertUtils;

import com.vaadin.data.Property;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.TextField;

public class NumberTextField extends TextField {
	@Override
	public void setPropertyDataSource(final Property newDataSource) {
		super.setPropertyDataSource(getPropertyFormatter(newDataSource));
	}

	protected PropertyFormatter getPropertyFormatter(final Property newDataSource) {
		return new PropertyFormatter(newDataSource) {
			@Override
			public String format(Object value) {
				if (value == null) {
					return getNullRepresentation();
				}
				return (String) ConvertUtils.convert(value, String.class);
			}

			@Override
			public Object parse(String formattedValue) throws Exception {
				return ConvertUtils.convert(formattedValue, newDataSource.getType());
			}
		};
	}
}
