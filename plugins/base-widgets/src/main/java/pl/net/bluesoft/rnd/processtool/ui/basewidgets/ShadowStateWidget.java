package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.ui.TextArea;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredPropertyConfigurator;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Widget to show task view from another step or process
 *
 * @author mpawlak@bluesoft.net.pl
 */
@AliasName(name = "ShadowStateWidget")
public class ShadowStateWidget extends ProcessHtmlWidget
{
    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "widget.shadow.widget.property.widgetsDefinition.name",
            descriptionKey = "widget.shadow.widget.property.widgetsDefinition.description"
    )
    private String processStateConfigurationId;

    @AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "widget.shadow.widget.property.forcePrivileges.name",
            descriptionKey = "widget.shadow.widget.property.forcePrivileges.description"
    )
    private Boolean forcePrivileges;

	public ShadowStateWidget(IBundleResourceProvider bundleResourceProvider) {

	}
}
