package pl.net.bluesoft.rnd.processtool.ui.generic;

import com.vaadin.ui.*;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 21:08
 */
public class GenericUserPortletSettingsPanel extends VerticalLayout {
	private final Set<SaveListener> listeners = new HashSet<SaveListener>();

	private I18NSource i18NSource;
	private TwinColSelect select;

	public interface SaveListener {
		void onSave();
	}

	public GenericUserPortletSettingsPanel(I18NSource i18NSource, String[] selectedViewKeys, Collection<GenericPortletViewRenderer> registeredViews) {
		this.i18NSource = i18NSource;

		addComponent(new Label(getMessage("settings")));
		addComponent(select = createSelection(selectedViewKeys, registeredViews));
		addComponent(new Button(getMessage("save"), new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				for (SaveListener listener : listeners) {
					listener.onSave();
				}
			}
		}));
	}

	private TwinColSelect createSelection(String[] selectedViewKeys, Collection<GenericPortletViewRenderer> registeredViews) {
		TwinColSelect select = new TwinColSelect(getMessage("select.views"));
		select.setLeftColumnCaption(getMessage("available.views"));
		select.setRightColumnCaption(getMessage("selected.views"));
		select.setImmediate(true);
		select.addContainerProperty("name", String.class, "");
		for (GenericPortletViewRenderer viewRenderer : registeredViews) {
			select.addItem(viewRenderer.getKey()).getItemProperty("name").setValue(viewRenderer.getName(i18NSource));
		}
		select.setValue(from(selectedViewKeys).toSet());
		select.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
		select.setItemCaptionPropertyId("name");
		return select;
	}

	public void addListener(SaveListener listener) {
		listeners.add(listener);
	}

	public String[] getSelectedViewKeys() {
		return Lang2.toStringArray(from((Collection<String>)select.getValue()));
	}

	private String getMessage(String key) {
		return i18NSource.getMessage("generic.portlet." + key);
	}
}
