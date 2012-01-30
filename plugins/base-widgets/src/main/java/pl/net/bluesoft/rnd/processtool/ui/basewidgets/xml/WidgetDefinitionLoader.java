package pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml;

import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;
import pl.net.bluesoft.rnd.pt.utils.xml.OXHelper;

public class WidgetDefinitionLoader extends OXHelper {
    private static WidgetDefinitionLoader instance = new WidgetDefinitionLoader();

    public static WidgetDefinitionLoader getInstance() {
        return instance;
    }

    @Override
    public Class[] getSupportedClasses() {
        return new Class[] {
                AlignElement.class,
                CheckBoxWidgetElement.class,
                DateWidgetElement.class,
                FormWidgetElement.class,
                GridWidgetElement.class,
                HasWidgetsElement.class,
                HorizontalLayoutWidgetElement.class,
                InputWidgetElement.class,
                ItemElement.class,
                LabelWidgetElement.class,
                LinkWidgetElement.class,
//                ScriptElement.class, ! not supported yet!
                SelectWidgetElement.class,
                TextAreaWidgetElement.class,
                UploadWidgetElement.class,
                VerticalLayoutWidgetElement.class,
                WidgetElement.class,
                WidgetsDefinitionElement.class,
        };
    }
}
