package pl.net.bluesoft.rnd.processtool.portlets.caches;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.cache.CacheProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 13:35
 */
public class CachesPanel extends VerticalLayout {
	private I18NSource i18NSource;

	public CachesPanel(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
		buildLayout();
	}

	private void buildLayout() {
		for (final Map.Entry<String, CacheProvider> entry : from(getRegistry().getCacheProviders().entrySet())
				.orderBy(new F<Map.Entry<String, CacheProvider>, String>() {
					@Override
					public String invoke(Map.Entry<String, CacheProvider> x) {
						return x.getKey();
					}
				})) {
			HorizontalLayout hl = new HorizontalLayout();

			Button invalidateCachesBtn = new Button("Invalidate");
			invalidateCachesBtn.addListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					entry.getValue().invalidateCache();
				}
			});

			hl.addComponent(new Label(entry.getKey()));
			hl.addComponent(invalidateCachesBtn);
			addComponent(hl);
		}
	}
}
