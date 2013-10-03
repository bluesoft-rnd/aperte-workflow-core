package pl.net.bluesoft.rnd.processtool.bpm.diagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:06
 */
public class Transition {
	private String name;
	private final List<Point> points = new ArrayList<Point>();

	public Transition() {}

	public Transition(String name) {
		this.name = name;
	}

	public List<Point> getPoints() {
		return Collections.unmodifiableList(points);
	}

	public Transition addPoint(Point point) {
		this.points.add(point);
		return this;
	}

	public Transition addPoint(double x, double y) {
		return addPoint(new Point(x, y));
	}
}
