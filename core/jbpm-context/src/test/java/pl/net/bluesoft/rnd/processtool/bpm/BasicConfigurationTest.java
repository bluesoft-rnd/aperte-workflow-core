package pl.net.bluesoft.rnd.processtool.bpm;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;
import pl.net.bluesoft.rnd.pt.ext.jbpm.ProcessToolContextFactoryImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */

public class BasicConfigurationTest {

    private static final Logger logger = Logger.getLogger(BasicConfigurationTest.class.getName());
    
	ProcessToolContextFactory ptcf = new ProcessToolContextFactoryImpl(new ProcessToolRegistryImpl());

	@Test
	public void testConfigDump() {
		ptcf.withProcessToolContext(new ProcessToolContextCallback() {
			@Override
			public void withContext(ProcessToolContext ctx) {
				ProcessDefinitionDAO processDefinitionDao = ctx.getProcessDefinitionDAO();
				Collection<ProcessDefinitionConfig> configs = processDefinitionDao.getActiveConfigurations();
				for (ProcessDefinitionConfig cfg : configs) {
					cleanSets(cfg, new HashSet());
				}
				XStream xstream = new XStream();
				xstream.aliasPackage("config", ProcessDefinitionConfig.class.getPackage().getName());
				xstream.useAttributeFor(String.class);
				xstream.useAttributeFor(Boolean.class);
				xstream.useAttributeFor(Integer.class);
				String s = xstream.toXML(configs);
				ByteArrayOutputStream baos = printPrettyXml(s);
				
				Collection<ProcessQueueConfig> processQueueConfigCollection = processDefinitionDao.getQueueConfigs();
				for (ProcessQueueConfig c : processQueueConfigCollection) {
					cleanSets(c, new HashSet());
				}
				printPrettyXml(xstream.toXML(processQueueConfigCollection));

				Collection<ProcessDefinitionConfig> configs2 = (Collection<ProcessDefinitionConfig>) xstream.fromXML(s);
				Assert.assertEquals(configs.size(), configs2.size());
				Collection<ProcessDefinitionConfig> configs3 = (Collection<ProcessDefinitionConfig>) xstream.fromXML(baos.toString());
				Assert.assertEquals(configs.size(), configs3.size());
			}
		});

	}

	private ByteArrayOutputStream printPrettyXml(String s) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(new StringInputStream(s));
			Element el = document.getDocumentElement();
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

	private void cleanUpXml(Element documentElement) {

		NodeList childNodes = documentElement.getChildNodes();
		int len = childNodes.getLength();
		for (int i=0; i < len; i++) {
			Node node = childNodes.item(i);
			if (node == null)
				continue;
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
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

	private void cleanSets(Object cfg, Set<Object> cleaned) {

		if(cleaned.contains(cfg) || cfg == null) {
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
					val = new HashSet((Collection)val);
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
}
