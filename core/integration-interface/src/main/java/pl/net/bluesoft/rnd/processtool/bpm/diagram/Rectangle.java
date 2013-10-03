package pl.net.bluesoft.rnd.processtool.bpm.diagram;

/**
 * User: POlszewski
 * Date: 2013-10-01
 * Time: 16:04
 */
public class Rectangle {
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
}
