package pl.net.bluesoft.rnd.processtool.bpm.diagram;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:02
 */
public class ProcessDiagram {
	private final List<Node> nodes = new ArrayList<Node>();
	private final List<Transition> transitions = new ArrayList<Transition>();

	public List<Node> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public List<Transition> getTransitions() {
		return Collections.unmodifiableList(transitions);
	}

	public ProcessDiagram addNode(Node node) {
		nodes.add(node);
		return this;
	}

	public ProcessDiagram addTransition(Transition transition) {
		transitions.add(transition);
		return this;
	}

	public static ProcessDiagram getSampleDiagram() {
		return new ProcessDiagram()
				.addNode(new StartNode("Start", new Rectangle(25, 115, 30, 30), "ffffff"))
				.addNode(new HumanTaskNode("Complain", new Rectangle(145, 90, 100, 80), "ffffff"))
				.addNode(new HumanTaskNode("Recommendation_1", new Rectangle(400, 25, 100, 80), "ffffff"))
				.addNode(new HumanTaskNode("Recommendation_2", new Rectangle(400, 220, 100, 80), "ffffff"))
				.addNode(new HumanTaskNode("Make_Decission", new Rectangle(700, 110, 100, 80), "ffffff"))
				.addNode(new HumanTaskNode("Accept", new Rectangle(1000, 110, 100, 80), "99CC00"))
				.addNode(new HumanTaskNode("Reject", new Rectangle(1000, 250, 100, 80), "FF6600"))
				.addNode(new ParallelGateway("fork", new Rectangle(325, 110, 40, 40), "ffffff"))
				.addNode(new ParallelGateway("join", new Rectangle(595, 130, 40, 40), "ffffff"))
				.addNode(new ExclusiveGateway("Decision", new Rectangle(880, 130, 40, 40), "ffffff"))
				.addNode(new EndNode("End", new Rectangle(1150, 136, 28, 28), "ffffff"))
				.addNode(new EndNode("End", new Rectangle(1150, 276, 28, 28), "ffffff"))
				.addTransition(new Transition("Start").addPoint(-40, 110).addPoint(115, 110))
				.addTransition(new Transition("toUpper").addPoint(265, 110).addPoint(345, 65).addPoint(370, 345))
				.addTransition(new Transition("toLower").addPoint(265, 110).addPoint(345, 260).addPoint(370, 240))
				.addTransition(new Transition("Decision").addPoint(800, 130).addPoint(820, 130))
				.addTransition(new Transition("Reject2").addPoint(820, 130).addPoint(900, 290).addPoint(970, 270))
				.addTransition(new Transition("End").addPoint(1084, 270).addPoint(1084, 270))
				.addTransition(new Transition("Process").addPoint(245, 110).addPoint(265, 110))
				.addTransition(new Transition("Complete2").addPoint(500, 250).addPoint(615, 260).addPoint(535, 130))
				.addTransition(new Transition("Accept2").addPoint(920, 130).addPoint(970, 130))
				.addTransition(new Transition("merged").addPoint(635, 130).addPoint(670, 130))
				.addTransition(new Transition("End").addPoint(1084, 130).addPoint(1084, 130))
				.addTransition(new Transition("Complete").addPoint(500, 54).addPoint(615, 66).addPoint(535, 130));
	}
}
