package org.aperteworkflow.bpm.graph;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class StateNode implements GraphElement {
    private String label;
    private boolean unfinished;
    private int x,y;
    private int width,height;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isUnfinished() {
        return unfinished;
    }

    public void setUnfinished(boolean unfinished) {
        this.unfinished = unfinished;
    }

    public StateNode cloneNode() {
        StateNode clone = new StateNode();
        clone.label = label;
        clone.x = x;
        clone.y = y;
        clone.width = width;
        clone.height = height;
        return clone;
    }

    @Override
    public String toString() {
        return "StateNode{" +
                "label='" + label + '\'' +
                ", unfinished=" + unfinished +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
