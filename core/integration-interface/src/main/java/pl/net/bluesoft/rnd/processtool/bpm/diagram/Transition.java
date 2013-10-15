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
	private String id;
	private String name;
	private final List<Point> points = new ArrayList<Point>();
	private Status status = Status.NOT_VISITED;
	private Node source, target;

	public enum Status {
		NOT_VISITED,
		VISITED
	}

	public Transition() {}

	public Transition(String name) {
		this.id = name;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
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

	public int getPointCount() {
		return points.size();
	}

	public Point getPoint(int i) {
		return points.get(i);
	}

	public void setPoint(int i, Point p) {
		points.set(i, p);
	}

	public Status getStatus() {
		return status;
	}

	public Transition setStatus(Status status) {
		this.status = status;
		return this;
	}

	public boolean isVisited() {
		if(Status.VISITED.equals(status)) {
			return true;
		} else {
			return false;
		}
	}

	public Node getSource() {
		return source;
	}

	public Transition setSource(Node source) {
		this.source = source;
		source.addOutcomingTransition(this);
		return this;
	}

	public Node getTarget() {
		return target;
	}

	public Transition setTarget(Node target) {
		this.target = target;
		target.addIncomingTransition(this);
		return this;
	}
}
