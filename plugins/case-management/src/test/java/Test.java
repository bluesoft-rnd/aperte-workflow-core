import com.thoughtworks.xstream.XStream;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pkuciapski on 2014-04-29.
 */
public class Test {
    public static void main(String[] args) {
        String s = "[XY] abc";
        String regex = "\\[(.*)\\].*";
        System.out.println(s.matches(regex));
        Matcher m = Pattern.compile(regex).matcher(s);
        if (m.find())
            System.out.println(m.group(1));


        /*final XStream x = new XStream();
        x.aliasPackage("config", CaseDefinition.class
                .getPackage().getName());
        x.useAttributeFor(String.class);
        x.useAttributeFor(Boolean.class);
        x.useAttributeFor(Integer.class);
        x.omitField(CaseStateDefinition.class, "definition");
        x.omitField(CaseStateWidget.class, "definition");
        x.omitField(CaseStateWidget.class, "permissions");

        // create definition
        final CaseDefinition def = new CaseDefinition();
        def.setName("TestDefinition");
        final CaseStateDefinition state1 = new CaseStateDefinition();
        state1.setName("State1");
        state1.setDefinition(def);

        final CaseStateWidget widget1 = new CaseStateWidget();
        widget1.setClassName("TabSheet");
        widget1.setPriority(0);
        final CaseStateWidget widget11 = new CaseStateWidget();
        widget11.setClassName("Widget1");
        widget11.setPriority(1);
        widget1.getChildren().add(widget11);
        state1.getWidgets().add(widget1);
        final CaseStateWidgetAttribute widget11attr1 = new CaseStateWidgetAttribute();
        final CaseStateWidgetAttribute widget11attr2 = new CaseStateWidgetAttribute();
        widget11attr1.setKey("attribute1");
        widget11attr1.setValue("value1");
        widget11attr2.setKey("attribute2");
        widget11attr2.setValue("value2");
        widget11.getAttributes().add(widget11attr1);
        widget11.getAttributes().add(widget11attr2);
        final CaseStateDefinition state2 = new CaseStateDefinition();
        state2.setName("State2");
        state2.setDefinition(def);

        def.setInitialState(state1);
        def.getPossibleStates().add(state1);
        def.getPossibleStates().add(state2);
        final String xml = x.toXML(def);
        System.out.println(xml);

        final CaseDefinition def2 = (CaseDefinition) x.fromXML(xml);
        System.out.println(def2);*/
    }
}
