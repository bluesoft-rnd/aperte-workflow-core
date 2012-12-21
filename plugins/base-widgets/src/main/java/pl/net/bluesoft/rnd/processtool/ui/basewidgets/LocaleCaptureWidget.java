package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import java.util.Collection;
import java.util.Locale;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="LocaleCapture")
@ChildrenAllowed(false)
@AperteDoc(humanNameKey="widget.locale_capture.name", descriptionKey="widget.locale_capture.description", icon="locale.png")
public class LocaleCaptureWidget extends BaseProcessToolWidget implements ProcessToolVaadinRenderable, ProcessToolDataWidget {

    @AutoWiredProperty(required=false)
    @AperteDoc(
            humanNameKey="widget.locale_capture.property.local_key.name",
            descriptionKey="widget.locale_capture.property.local_key.description"
    )
	private String localeKey = "java.util.Locale";

	private Label lbl = new Label();

	@Override
	public Component render() {
		return lbl;
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		throw new IllegalArgumentException("Not supported!");
	}

	@Override
	public Collection<String> validateData(BpmTask task, boolean skipRequired) {
		return null; //nothing to validate
	}

	@Override
	public void saveData(BpmTask task) {
        ProcessInstance pi = task.getProcessInstance();
		setSimpleAttribute(localeKey, nvl(lbl.getApplication().getLocale(), Locale.getDefault()).toString(), pi);
	}

	@Override
	public void loadData(BpmTask task) {
		//nothing
	}

	public String getLocaleKey() {
		return localeKey;
	}

	public void setLocaleKey(String localeKey) {
		this.localeKey = localeKey;
	}

}
