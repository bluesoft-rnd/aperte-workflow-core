package pl.net.bluesoft.rnd.processtool.bpm.diagram;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:04
 */
public final class Rectangle {
	private final double x, y, width, height;

	public Rectangle(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public Point getCentre() {
		return new Point(x + width / 2, y + height / 2);
	}

	public Point getLeftTop() {
		return new Point(x, y);
	}

	public Point getRightTop() {
		return new Point(x + width, y);
	}

	public Point getLeftBottom() {
		return new Point(x, y + height);
	}

	public Point getRightBottom() {
		return new Point(x + width, y + height);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Rectangle rectangle = (Rectangle)o;

		if (Double.compare(rectangle.height, height) != 0) return false;
		if (Double.compare(rectangle.width, width) != 0) return false;
		if (Double.compare(rectangle.x, x) != 0) return false;
		if (Double.compare(rectangle.y, y) != 0) return false;

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
		temp = Double.doubleToLongBits(width);
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(height);
		result = 31 * result + (int)(temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "[" + x + ", " + y + ", " + width + ", " + height + ']';
	}
}
