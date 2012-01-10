package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.Property;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.WidgetConfigFormFieldFactory;

public class ProcessDataBlockFieldFactory extends WidgetConfigFormFieldFactory {
	private static final long	serialVersionUID	= 2864743076049633628L;

	@Override
	public Field createField(Property<?> property) {
		return super.createField(property, TextArea.class);
	}
}
