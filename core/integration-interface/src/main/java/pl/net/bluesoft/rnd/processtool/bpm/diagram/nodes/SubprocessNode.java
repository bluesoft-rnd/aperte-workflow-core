package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;

/**
 * User: POlszewski
 * Date: 2013-10-02
 * Time: 10:23
 */
public class SubprocessNode extends Node {
	public SubprocessNode() {
	}

	public SubprocessNode(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitSubprocess(this);
	}
}
