package pl.net.bluesoft.rnd.processtool.bpm.diagram;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:09
 */
public final class Point {
	private final double x, y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double squareOfDistance(Point p) {
		return (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Point point = (Point)o;

		if (Double.compare(point.x, x) != 0) return false;
		if (Double.compare(point.y, y) != 0) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ')';
	}
}
