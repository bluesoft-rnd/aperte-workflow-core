package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:08
 */
public class EndNode extends Node {
	public EndNode() {
	}

	public EndNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitEndNode(this);
	}
}
