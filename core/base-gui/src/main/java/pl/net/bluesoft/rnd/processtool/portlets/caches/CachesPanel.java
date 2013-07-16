package pl.net.bluesoft.rnd.processtool.portlets.caches;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 13:35
 */
public class CachesPanel extends VerticalLayout implements Button.ClickListener {
	private I18NSource i18NSource;

	private Button invalidateCachesBtn;

	public CachesPanel(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
		buildLayout();
	}

	private void buildLayout() {
		invalidateCachesBtn = new Button(i18NSource.getMessage("Wyczyść cache"));
		invalidateCachesBtn.addListener(this);
		addComponent(invalidateCachesBtn);
	}

	@Override
	public void buttonClick(Button.ClickEvent event) {
		if (event.getButton() == invalidateCachesBtn) {
			//LiferayBridge.invalidateCaches();
		}
	}
}
