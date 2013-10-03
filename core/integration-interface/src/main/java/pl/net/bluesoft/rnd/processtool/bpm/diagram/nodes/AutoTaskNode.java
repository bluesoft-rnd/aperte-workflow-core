package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;

public class AutoTaskNode extends Node {

	public AutoTaskNode() {}

	public AutoTaskNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}
	
	@Override
	public void visit(Visitor visitor) {
		visitor.visitAutoTaskNode(this);
		
	}

}
