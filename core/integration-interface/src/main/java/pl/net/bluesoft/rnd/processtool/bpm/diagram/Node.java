package pl.net.bluesoft.rnd.processtool.bpm.diagram;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes.*;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:04
 */
public abstract class Node {
	private String name;
	private Rectangle boundary;
	private String backgroundColor;

	protected Node() {}

	protected Node(String name, Rectangle boundary, String backgroundColor) {
		this.name = name;
		this.boundary = boundary;
		this.backgroundColor = backgroundColor;
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
