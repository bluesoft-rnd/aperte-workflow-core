package pl.net.bluesoft.rnd.processtool.bpm.diagram;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:04
 */
public abstract class Node {
	private String id;
	private String name;
	private Rectangle boundary;
	private String backgroundColor;
	private Status status = Status.NOT_VISITED;
	private final Set<Transition> incomingTransitions = new LinkedHashSet<Transition>();
	private final Set<Transition> outcomingTransitions = new LinkedHashSet<Transition>();

	public enum Status {
		NOT_VISITED,
		VISITED,
		PENDING
	}

	protected Node() {}

	protected Node(String name, Rectangle boundary, String backgroundColor) {
		this.id = name;
		this.name = name;
		this.boundary = boundary;
		this.backgroundColor = backgroundColor;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Node setName(String name) {
		this.name = name;
		return this;
	}

	public Rectangle getBoundary() {
		return boundary;
	}

	public Node setBoundary(Rectangle boundary) {
		this.boundary = boundary;
		return this;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public Node setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public Node setStatus(Status status) {
		this.status = status;
		return this;
	}
	
	public boolean isActive() {
		if(status == Status.PENDING)
			return true;
		return false;
	}

	public Set<Transition> getIncomingTransitions() {
		return Collections.unmodifiableSet(incomingTransitions);
	}

	public Set<Transition> getOutcomingTransitions() {
		return Collections.unmodifiableSet(outcomingTransitions);
	}

	public void addIncomingTransition(Transition transition) {
		incomingTransitions.add(transition);
	}

	public void addOutcomingTransition(Transition transition) {
		outcomingTransitions.add(transition);
	}

	public Transition getOutcomingTransition(String transitionId) {
		for (Transition transition : outcomingTransitions) {
			if (transition.getId().equals(transitionId)) {
				return transition;
			}
		}
		return null;
	}

	public Node copyBasicProperties(Node node) {
		this.id = node.id;
		this.name = node.name;
		this.boundary = node.boundary;
		this.backgroundColor = node.backgroundColor;
		this.status = node.status;
		return this;
	}
	
	public double getRightEdge() {
		return boundary.getX() + boundary.getWidth();
	}
	
	public double getBottomEdge() {
		return boundary.getY() + boundary.getHeight();
	}

	public interface Visitor {
		void visitHumanTaskNode(HumanTaskNode node);
		void visitAutoTaskNode(AutoTaskNode node);
		void visitSubprocess(SubprocessNode subprocessNode);
		void visitExclusiveGateway(ExclusiveGateway node);
		void visitParallelGateway(ParallelGateway node);
		void visitStartNode(StartNode node);
		void visitStartTimerNode(StartTimerNode node);
		void visitIntermediateTimerNode(IntermediateTimerNode node);
		void visitIntermediateSignalNode(IntermediateSignalNode node);
		void visitEndNode(EndNode node);
	}

	public abstract void visit(Visitor visitor);
}
