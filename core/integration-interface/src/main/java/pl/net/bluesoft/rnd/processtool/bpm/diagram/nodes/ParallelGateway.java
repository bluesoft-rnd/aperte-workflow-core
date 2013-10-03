package pl.net.bluesoft.rnd.processtool.bpm.diagram.nodes;

import pl.net.bluesoft.rnd.processtool.bpm.diagram.Node;
import pl.net.bluesoft.rnd.processtool.bpm.diagram.Rectangle;

/**
 * User: POlszewski
 * Date: 2013-10-02
 * Time: 09:31
 */
public class ParallelGateway extends Node {
	public ParallelGateway() {
	}

	public ParallelGateway(String name, Rectangle boundary, String backgroundColor) {
		super(name, boundary, backgroundColor);
	}

	@Override
	public void visit(Visitor visitor) {
		visitor.visitParallelGateway(this);
	}
}
