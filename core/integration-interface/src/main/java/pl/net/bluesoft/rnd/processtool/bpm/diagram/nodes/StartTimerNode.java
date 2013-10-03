package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

public class StartTimerNode extends Node {
	
	public StartTimerNode() {}

	public StartTimerNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitStartTimerNode(this);
	}
}
