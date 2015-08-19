package org.aperteworkflow.samples.application.widgets.view;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.casemanagement.CaseManagementFacadeImpl;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetType;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dominik Dębowczyk on 2015-08-05.
 */
@AliasName(name = "CaseApplicationInfo", type = WidgetType.Html)
@WidgetGroup("application-process")
public class ApplicationInfoWidget extends ProcessHtmlWidget {

    @Autowired
    private ProcessToolRegistry registry;

    @Autowired
    CaseManagementFacadeImpl caseManagementFacade;

    public ApplicationInfoWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("case-application-info.html", bundleResourceProvider));
        addDataProvider(new IWidgetDataProvider() {
            @Override
            public Map<String, Object> getData(IAttributesProvider provider, Map<String, Object> baseViewData) {
                Map<String, Object> result = new HashMap<String, Object>();
                result.put("caseManagementFacade", caseManagementFacade);
                return result;
            }
        });
        SimpleWidgetDataHandler simpleWidgetDataHandler = new SimpleWidgetDataHandler();
        addDataHandler(simpleWidgetDataHandler);

    }
}


