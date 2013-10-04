package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.*;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes.*;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-10-03
 * Time: 14:25
 */
public class ProcessDiagramParser extends DefaultHandler {
	private final Map<String, Node> nodesByUid = new LinkedHashMap<String, Node>();
	private final Map<String, Transition> transitionsByUid = new LinkedHashMap<String, Transition>();
	private String currentNodeUid;
	private Node currentNode;
	private Transition currentTransition;
	private Map<Transition, String> sourceRefs = new HashMap<Transition, String>();
	private Map<Transition, String> targetRefs = new HashMap<Transition, String>();

	public ProcessDiagram parse(InputStream input) {
		try {
			getSAXParser().parse(input, this);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		ProcessDiagram result = new ProcessDiagram();

		for (Node node : nodesByUid.values()) {
			result.addNode(node);
		}
		for (Transition transition : transitionsByUid.values()) {
			transition.setSource(nodesByUid.get(sourceRefs.get(transition)));
			transition.setTarget(nodesByUid.get(targetRefs.get(transition)));
			result.addTransition(transition);
		}
		return result;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ("startEvent".equalsIgnoreCase(localName)) {
			setCurrentNode(new StartNode(), attributes);
		}
		else if ("endEvent".equalsIgnoreCase(localName)) {
			setCurrentNode(new EndNode(), attributes);
		}
		else if ("userTask".equalsIgnoreCase(localName)) {
			setCurrentNode(new HumanTaskNode(), attributes);
		}
		else if ("scriptTask".equalsIgnoreCase(localName)) {
			setCurrentNode(new AutoTaskNode(), attributes);
		}
		else if ("exclusiveGateway".equalsIgnoreCase(localName)) {
			setCurrentNode(new ExclusiveGateway(), attributes);
		}
		else if ("parallelGateway".equalsIgnoreCase(localName)) {
			setCurrentNode(new ParallelGateway(), attributes);
		}
		else if ("callActivity".equalsIgnoreCase(localName)) {
			setCurrentNode(new SubprocessNode(), attributes);
		}
		else if ("intermediateCatchEvent".equalsIgnoreCase(localName)) {
			setCurrentNode(new IntermediateSignalNode(), attributes);
		}
		else if ("timerEventDefinition".equalsIgnoreCase(localName) && currentNode instanceof StartNode) {
			replaceCurrentNode(new StartTimerNode());
		}
		else if ("timerEventDefinition".equalsIgnoreCase(localName) && currentNode instanceof IntermediateSignalNode) {
			replaceCurrentNode(new IntermediateTimerNode());
		}
		else if ("sequenceFlow".equalsIgnoreCase(localName)) {
			setCurrentTransition(new Transition(), attributes);
		}
		else if ("BPMNShape".equalsIgnoreCase(localName)) {
			String nodeId = getValue(attributes, "bpmnElement");
			if (nodeId != null) {
				currentNode = nodesByUid.get(nodeId);
			}
		}
		else if ("Bounds".equalsIgnoreCase(localName) && currentNode != null) {
			double x = Double.parseDouble(getValue(attributes, "x"));
			double y = Double.parseDouble(getValue(attributes, "y"));
			double width = Double.parseDouble(getValue(attributes, "width"));
			double height = Double.parseDouble(getValue(attributes, "height"));
			currentNode.setBoundary(new Rectangle(x, y, width, height));
		}
		else if ("BPMNEdge".equalsIgnoreCase(localName)) {
			String transitionId = getValue(attributes, "bpmnElement");
			if (transitionId != null) {
				currentTransition = transitionsByUid.get(transitionId);
			}
		}
		else if ("waypoint".equalsIgnoreCase(localName) && currentTransition != null) {
			double x = Double.parseDouble(getValue(attributes, "x"));
			double y = Double.parseDouble(getValue(attributes, "y"));
		 	currentTransition.addPoint(new Point(x, y));
		}
		else if ("signavioMetaData".equalsIgnoreCase(localName)) {
			if (currentNode != null && "bgcolor".equals(getValue(attributes, "metaKey"))) {
				currentNode.setBackgroundColor(withoutHash(getValue(attributes, "metaValue")));
			}
		}
	}

	private static String withoutHash(String value) {
		return value != null && value.startsWith("#") ? value.substring(1) : null;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("startEvent".equalsIgnoreCase(localName) ||
			"endEvent".equalsIgnoreCase(localName) ||
			"userTask".equalsIgnoreCase(localName) ||
			"scriptTask".equalsIgnoreCase(localName) ||
			"exclusiveGateway".equalsIgnoreCase(localName) ||
			"parallelGateway".equalsIgnoreCase(localName) ||
			"callActivity".equalsIgnoreCase(localName) ||
			"intermediateCatchEvent".equalsIgnoreCase(localName)) {
			currentNodeUid = null;
			currentNode = null;
		}
		if ("sequenceFlow".equalsIgnoreCase(localName)) {
			currentTransition = null;
		}
	}

	private void setCurrentNode(Node node, Attributes attributes) {
		this.currentNodeUid = getValue(attributes, "id");

		node.setId(getValue(attributes, "name"));
		node.setName(node.getId());

		nodesByUid.put(currentNodeUid, node);
		this.currentNode = node;
	}

	private void setCurrentTransition(Transition transition, Attributes attributes) {
		transition.setId(getValue(attributes, "name"));
		transition.setName(transition.getId());
		transitionsByUid.put(getValue(attributes, "id"), transition);
		sourceRefs.put(transition, getValue(attributes, "sourceRef"));
		targetRefs.put(transition, getValue(attributes, "targetRef"));
		this.currentTransition = transition;
	}

	private void replaceCurrentNode(Node node) {
		currentNode = node.copyBasicProperties(currentNode);
		nodesByUid.put(currentNodeUid, node);
	}

	private static String getValue(Attributes attributes, String localName) {
		for (int i = 0; i < attributes.getLength(); ++i) {
			if (localName.equalsIgnoreCase(attributes.getLocalName(i))) {
				return attributes.getValue(i);
			}
		}
		return null;
	}

	private SAXParser getSAXParser() {
		SAXParserFactory factory = createSAXParserFactory();
		SAXParser parser;
		// not jaxp1.2 compliant so turn off validation
		try {
			factory.setValidating(false);
			parser = factory.newSAXParser();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (!parser.isNamespaceAware()) {
			throw new RuntimeException("parser must be namespace-aware");
		}
		return parser;
	}

	private SAXParserFactory createSAXParserFactory() {
		SAXParserFactory factory;

		try {
			factory = SAXParserFactory.newInstance();
		}
		catch (FactoryConfigurationError e) {
			// obscure JDK1.5 bug where FactoryFinder in the JRE returns a null ClassLoader, so fall back to hard coded xerces.
			// https://stg.network.org/bugzilla/show_bug.cgi?id=47169
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4633368
			try {
				factory = (SAXParserFactory)Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
			}
			catch (Exception e1) {
				throw new RuntimeException("Unable to create new DOM Document", e1);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to create new DOM Document", e);
		}

		factory.setNamespaceAware(true);
		return factory;
	}
}
