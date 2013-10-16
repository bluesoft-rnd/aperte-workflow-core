package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:04
 */
public class StartNode extends Node {
	public StartNode() {
	}

	public StartNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitStartNode(this);
	}
}
