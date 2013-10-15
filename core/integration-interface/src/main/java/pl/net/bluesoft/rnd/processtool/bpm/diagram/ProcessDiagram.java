package pl.net.bluesoft.rnd.processtool.bpm.diagram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:02
 */
public class ProcessDiagram {
	private final Map<String, Node> nodes = new LinkedHashMap<String, Node>();
	private final List<Transition> transitions = new ArrayList<Transition>();
	private static final int MARGIN_SIZE = 25;

	public Collection<Node> getNodes() {
		return Collections.unmodifiableCollection(nodes.values());
	}

	public Collection<Transition> getTransitions() {
		return Collections.unmodifiableCollection(transitions);
	}

	public ProcessDiagram addNode(Node node) {
		nodes.put(node.getId(), node);
		return this;
	}

	public ProcessDiagram addTransition(Transition transition) {
		transitions.add(transition);
		return this;
	}

	public Node getNode(String nodeId) {
		return nodes.get(nodeId);
	}
	
	
	public double getWidth() {
		Node rightmost = null;
		for(Node node : nodes.values()) {
			if(rightmost == null || rightmost.getRightEdge() < node.getRightEdge())
				rightmost = node;
		}
		return rightmost.getRightEdge() + MARGIN_SIZE;
	}
	
	public double getHeight() {
		Node lowest = null;
		for(Node node : nodes.values()) {
			if(lowest == null || lowest.getBottomEdge() < node.getBottomEdge())
				lowest = node;
		}
		return lowest.getBottomEdge() + MARGIN_SIZE;
	}

}
