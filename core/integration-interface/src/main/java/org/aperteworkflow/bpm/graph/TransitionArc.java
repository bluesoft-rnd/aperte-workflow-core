package org.aperteworkflow.bpm.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TransitionArc implements GraphElement{
    private String name;
    private String id;
    private String destination;
    private String source;

    private List<TransitionArcPoint> path = new ArrayList<TransitionArcPoint>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name; 
    }

    public void addPoint(int x, int y) {
        path.add(new TransitionArcPoint(x, y));
    }
    public List<TransitionArcPoint> getPath() {
        return path;
    }

    public void setPath(List<TransitionArcPoint> path) {
        this.path = path;
    }
    
    public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public GraphElement cloneNode() {
        TransitionArc clone = new TransitionArc();
        clone.name = name;
        clone.id = id;
        clone.destination = destination;
        clone.source = source;
        for (TransitionArcPoint tap : path) {
            clone.addPoint(tap.getX(), tap.getY());
        }
        return clone;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
		this.id = id;
	}

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("TransitionArc{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ",\n path=");
        for (TransitionArcPoint p : path) {
            s.append(p).append(" ");
        }
        return s.append('}').toString();
    }


}
