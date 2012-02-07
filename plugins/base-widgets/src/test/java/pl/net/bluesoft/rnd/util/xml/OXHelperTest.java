package pl.net.bluesoft.rnd.util.xml;

import org.junit.Test;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.WidgetDefinitionLoader;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.XmlConstants;
import pl.net.bluesoft.rnd.processtool.ui.basewidgets.xml.jaxb.*;

import java.util.List;

public class OXHelperTest {
    private WidgetDefinitionLoader helper = WidgetDefinitionLoader.getInstance();

    @Test
    public void testRemoveCDATA() {
        String text = "<![CDATA[<b>jakis</b> html]]>";
        System.out.println(WidgetDefinitionLoader.removeCDATATag(text));
    }

    @Test
    public void testSerializeWidgets2() {
        WidgetsDefinitionElement wd = createWidgetsDefinition();

        String xml = helper.marshall(wd);

        System.out.println(xml);
    }

    private WidgetsDefinitionElement createWidgetsDefinition() {
        WidgetsDefinitionElement wd = new WidgetsDefinitionElement();
        List<WidgetElement> widgets = wd.getWidgets();

        LabelWidgetElement label = new LabelWidgetElement();
        label.setMode(3);
        label.setText("Instrukcja <b>obsługi bloku</b> danych.");

        widgets.add(label);

        InputWidgetElement input = new InputWidgetElement();
        input.setBind("customer_name");
        input.setCaption("processdata.block.customer.name");
        input.setWidth("300px");
        input.setReadonly(true);

        widgets.add(input);

        input = new InputWidgetElement();
        input.setBind("customer_surname");
        input.setCaption("processdata.block.customer.surname");
        input.setWidth("300px");

        widgets.add(input);

        label = new LabelWidgetElement();
        label.setText("<![CDATA[<b>jakis tekst</b>]]>");
        label.setCaption("niby caption");

        widgets.add(label);

        HorizontalLayoutWidgetElement hl = new HorizontalLayoutWidgetElement();
        widgets.add(hl);

        DateWidgetElement date = new DateWidgetElement();
        date.setFormat("dd-MM-yyyy");
        date.setNotAfter("05-02-2011");
        date.setNotBefore("30-01-2011");

        hl.getWidgets().add(date);

        LinkWidgetElement link = new LinkWidgetElement();
        link.setCaption("Link do Google");
        link.setUrl("http://www.google.pl");

        hl.getWidgets().add(link);

        GridWidgetElement grid = new GridWidgetElement();
        grid.setCols(2);
        grid.setRows(2);

        CheckBoxWidgetElement cb = new CheckBoxWidgetElement();
        cb.setDefaultSelect(true);
        cb.setCaption("cb1");
        grid.getWidgets().add(cb);

        cb = new CheckBoxWidgetElement();
        cb.setDefaultSelect(false);
        cb.setCaption("cb2");
        grid.getWidgets().add(cb);

        cb = new CheckBoxWidgetElement();
        cb.setDefaultSelect(false);
        cb.setCaption("cb3");
        grid.getWidgets().add(cb);

        cb = new CheckBoxWidgetElement();
        cb.setDefaultSelect(true);
        cb.setCaption("cb4");
        grid.getWidgets().add(cb);

        VerticalLayoutWidgetElement vl = new VerticalLayoutWidgetElement();
        widgets.add(vl);

        AlignElement ae = new AlignElement();
        ae.setPos(XmlConstants.ALIGN_POS_RIGHT_TOP);
        ae.getWidgets().add(grid);

        AbstractSelectWidgetElement sw = new ComboboxSelectElementWidget();
        sw.setDefaultSelect(2);
        sw.getValues().add(new ItemElement("item1", "value1"));
        sw.getValues().add(new ItemElement("item2", "value2"));
        sw.getValues().add(new ItemElement("item3", "value3"));
        sw.getValues().add(new ItemElement("item4", "value4"));

        ae.getWidgets().add(sw);

        sw = new RadioButtonSelectElementWidget();
        sw.setDefaultSelect(1);
        sw.setDict("test_lang");
        sw.setProvider("db");

        vl.getWidgets().add(ae);
        vl.getWidgets().add(sw);
        return wd;
    }

    @Test
    public void testSerializeWidgets() {
        WidgetsDefinitionElement wd = new WidgetsDefinitionElement();
        wd.setAttributeClass("pl.net.bluesoft.rnd.processtool.test.CustomerData");
        List<WidgetElement> widgets = wd.getWidgets();

        LabelWidgetElement label = new LabelWidgetElement();
        label.setMode(3);
        label.setText("<![CDATA[Instrukcja <b>obsługi bloku</b> danych.]]>");

        widgets.add(label);

        InputWidgetElement input = new InputWidgetElement();
        input.setBind("customer.name");
        input.setCaption("processdata.block.customer.name");
        input.setWidth("300px");

        widgets.add(input);

        input = new InputWidgetElement();
        input.setBind("customer.surname");
        input.setCaption("processdata.block.customer.surname");
        input.setWidth("300px");

        widgets.add(input);

        input = new InputWidgetElement();
        input.setBind("customer.address.street");
        input.setCaption("processdata.block.customer.address.street");
        input.setWidth("300px");

        widgets.add(input);

        input = new InputWidgetElement();
        input.setBind("customer.address.houseNumber");
        input.setCaption("processdata.block.customer.address.house");
        input.setRegexp("[0-9]+");
        input.setWidth("300px");

        widgets.add(input);

        input = new InputWidgetElement();
        input.setBind("customer.address.flatNumber");
        input.setPrompt("Wpisz coś");
        input.setCaption("processdata.block.customer.address.flat");
        input.setWidth("300px");

        widgets.add(input);

        UploadWidgetElement upload = new UploadWidgetElement();
        upload.setCaption("processdata.block.attachment");
        upload.setBind("attachment");
        upload.setAttributeClass("pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttachmentAttribute");

        widgets.add(upload);

        String xml = helper.marshall(wd);

        System.out.println(xml);

    }

}
