package pl.net.bluesoft.rnd.processtool.bpm;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ParseJbpm4PdlFileTest {

    private static final Logger logger = Logger.getLogger(ParseJbpm4PdlFileTest.class.getName());
    
    public static void main(String[] args) throws Exception {

        ProcessDefinitionConfig pdc = new ProcessDefinitionConfig();

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new FileInputStream(args[0]));
        Element rootElement = doc.getRootElement();
        pdc.setBpmDefinitionKey(rootElement.getAttributeValue("name"));
        pdc.setDescription(rootElement.getAttributeValue("name"));
        pdc.setProcessName(rootElement.getAttributeValue("name"));
        HashSet<ProcessStateConfiguration> states = new HashSet<ProcessStateConfiguration>();
        pdc.setStates(states);
        for (Object o : rootElement.getChildren("task")) {
            Element el = (Element) o;
            String taskName = el.getAttributeValue("name");
            ProcessStateConfiguration psc = new ProcessStateConfiguration();
            psc.setDefinition(pdc);
            psc.setCommentary(taskName);
            psc.setDescription(taskName);
            psc.setName(taskName);

            ProcessStateWidget processStateWidget = new ProcessStateWidget();
            processStateWidget.setClassName("VerticalLayoput");
            processStateWidget.setPriority(1);
            psc.getWidgets().add(processStateWidget);

            HashSet<ProcessStateAction> actions = new HashSet<ProcessStateAction>();
            psc.setActions(actions);
            if (taskName == null) continue;
            int cnt = 0;
            for (Object subO : el.getChildren("transition")) {
                Element subEl = (Element) subO;
                String name = subEl.getAttributeValue("name");
                String text = subEl.getAttributeValue("to");
                ProcessStateAction psa = new ProcessStateAction();
                psa.setAutohide(true);
                psa.setBpmName(name);
                psa.setDescription(name);
                psa.setLabel(name);
                psa.setPriority(cnt++);

                actions.add(psa);
            }
            states.add(psc);
        }

        cleanSets(pdc, new HashSet());

        XStream xstream = new XStream();
        xstream.aliasPackage("config", ProcessDefinitionConfig.class.getPackage().getName());
        xstream.useAttributeFor(String.class);
        xstream.useAttributeFor(Boolean.class);
        xstream.useAttributeFor(Integer.class);
        String s = xstream.toXML(pdc);
        printPrettyXml(s);
    }

    private static void cleanSets(Object cfg, Set<Object> cleaned) {

        if (cleaned.contains(cfg) || cfg == null) {
            return;
        }
        cleaned.add(cfg);
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(cfg);
        for (PropertyDescriptor f : descriptors) {
//		Field[] fields = cfg.getClass().getDeclaredFields();
//		for (Field f : fields) {
            try {
                Object val = PropertyUtils.getProperty(cfg, f.getName());
                if (val == null) continue;
                Class cls = val.getClass();
                if (cls.getName().startsWith("pl.net.bluesoft"))
                    cleanSets(val, cleaned);

                if (val instanceof org.hibernate.collection.PersistentSet) {
                    val = new HashSet((Collection) val);
                    PropertyUtils.setProperty(cfg, f.getName(), val);
                }
                if (val instanceof Iterable) {
                    Iterable c = (Iterable) val;
                    for (Object o : c) {
                        cleanSets(o, cleaned);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }


	private static ByteArrayOutputStream printPrettyXml(String s) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.parse(new StringInputStream(s));
			org.w3c.dom.Element el = document.getDocumentElement();
			cleanUpXml(el);
			DOMSource domSource = new DOMSource(document);
			StreamResult streamResult = new StreamResult(baos);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			serializer.transform(domSource, streamResult);
			logger.info(baos.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return baos;
	}

	private static void cleanUpXml(org.w3c.dom.Element documentElement) {

		NodeList childNodes = documentElement.getChildNodes();
		int len = childNodes.getLength();
		for (int i=0; i < len; i++) {
			Node node = childNodes.item(i);
			if (node == null)
				continue;
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				org.w3c.dom.Element e = (org.w3c.dom.Element) node;
				String nodename = e.getNodeName();
				if (nodename.equals("id") || nodename.equals("createDate")  || nodename.equals("latest")) {
					documentElement.removeChild(node);
					i--;
					continue;
				}
				if (e.getChildNodes().getLength() == 0) {
					documentElement.removeChild(node);
					i--;
					continue;
				}
				cleanUpXml(e);
				continue;
			}
			if (node.getNodeType() == Node.TEXT_NODE) {
				org.w3c.dom.Text text = (Text) node;
				if (text.getWholeText().matches("^\\s+$")) {
					documentElement.removeChild(node);
					i--;
					continue;
				}
			}


		}
	}
}
