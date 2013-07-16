package org.aperteworkflow.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * User: POlszewski
 * Date: 2013-07-03
 * Time: 13:23
 */
public class SimpleXmlTransformer {
	private final InputStream inputStream;
	private Document document;

	public SimpleXmlTransformer(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public interface AttributeTransformer {
		String transform(String attributeValue);
	}

	public void transformAttributes(String xPathSelector, AttributeTransformer transformer) {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		try {
			XPathExpression expr = xpath.compile(xPathSelector);
			NodeList nodes = (NodeList)expr.evaluate(getDocument(), XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); ++i) {
				Node node = nodes.item(i);
				if (node instanceof Attr) {
					Attr attr = (Attr)node;
					attr.setValue(transformer.transform(attr.getValue()));
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(getDocument());
			transformer.transform(source, result);

			return result.getWriter().toString();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Document getDocument() throws ParserConfigurationException, SAXException, IOException {
		if (document == null) {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			document = docBuilder.parse(inputStream);
		}
		return document;
	}
}
