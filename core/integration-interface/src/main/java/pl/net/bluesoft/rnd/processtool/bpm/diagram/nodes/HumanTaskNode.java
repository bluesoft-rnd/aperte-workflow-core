package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;

/**
 * User: POlszewski
 * Date: 2013-10-02
 * Time: 08:34
 */
public class HumanTaskNode extends Node {
	public HumanTaskNode() {
	}

	public HumanTaskNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitHumanTaskNode(this);
	}
}
