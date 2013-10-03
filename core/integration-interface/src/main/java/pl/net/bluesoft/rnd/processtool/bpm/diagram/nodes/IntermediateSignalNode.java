package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

public class IntermediateSignalNode extends Node {
	
	public IntermediateSignalNode() {}

	public IntermediateSignalNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitIntermediateSignalNode(this);
	}
}
