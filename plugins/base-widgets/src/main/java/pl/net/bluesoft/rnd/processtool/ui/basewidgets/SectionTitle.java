package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.rnd.util.StepUtil;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Map;

/**
 * Created by lukasz on 6/2/14.
 */
@AperteDoc(humanNameKey = "widget.section_title_html.name", descriptionKey = "widget.section_title_html.description")
@WidgetGroup("base-widgets")
@AliasName(name = "SectionTitle", type = WidgetType.Html)
public class SectionTitle extends ProcessHtmlWidget {

    @AutoWiredProperty
    private String title;

    public SectionTitle(IBundleResourceProvider bundleResourceProvider)
    {
        addDataHandler(new SimpleWidgetDataHandler());
        setContentProvider(new FileWidgetContentProvider("section_title.html", bundleResourceProvider));
    }

	@Override
	public void getViewData(Map<String, Object> viewData) {
		I18NSource i18NSource = (I18NSource)viewData.get(IHtmlTemplateProvider.MESSAGE_SOURCE_PARAMETER);
		IAttributesProvider attributesProvider = (IAttributesProvider)viewData.get(IHtmlTemplateProvider.ATTRIBUTES_PROVIDER);

		String sectionTitle = i18NSource.getMessage((String)viewData.get("title"));

		sectionTitle = StepUtil.substituteVariables(sectionTitle, attributesProvider);
		viewData.put("sectionTitle", sectionTitle);
	}
}