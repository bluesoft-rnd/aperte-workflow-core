package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.SimpleWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;
import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Maps;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;


@AliasName(name = "UserSubstitutionRequestWidget", type = WidgetType.Html)
@WidgetGroup("common")
@AperteDoc(humanNameKey="widget.substitution.request.name", descriptionKey="widget.substitution.request.description")
@ChildrenAllowed(false)
public class UserSubstitutionRequestWidget extends ProcessHtmlWidget
{
    @AutoWiredProperty
    private boolean requestMode = true;

    public UserSubstitutionRequestWidget(IBundleResourceProvider bundleResourceProvider) {
        setContentProvider(new FileWidgetContentProvider("process-substitution-request.html", bundleResourceProvider));
        addDataHandler(new SimpleWidgetDataHandler());
    }
}
