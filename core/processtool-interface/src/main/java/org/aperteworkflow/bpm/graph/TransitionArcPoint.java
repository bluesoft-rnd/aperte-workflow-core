package org.aperteworkflow.bpm.graph;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class TransitionArcPoint {
    private int x,y;

    public TransitionArcPoint() {
    }

    public TransitionArcPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public TransitionArcPoint cloneNode() {
        return new TransitionArcPoint(x,y);
    }

    @Override
    public String toString() {
        return "TransitionArcPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
