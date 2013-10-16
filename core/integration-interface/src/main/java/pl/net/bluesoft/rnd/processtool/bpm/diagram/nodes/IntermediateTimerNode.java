package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

public class IntermediateTimerNode extends Node {
	
	public IntermediateTimerNode() {}

	public IntermediateTimerNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitIntermediateTimerNode(this);
	}
}
