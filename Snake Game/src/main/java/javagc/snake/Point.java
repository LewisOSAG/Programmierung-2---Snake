package javagc.snake;

/**
 * Klasse Point hält den Ort eines bestimmten Elementes
 * Sie kann auch als Element einer instructionQueue verwendet werden, siehe SnakeLogic
 * 
 * @author user_Matness
 * 
 */
public class Point {

	private Double x;
	private Double y;

	public static enum ORIENTATION {
		ORIENTATION_EAST, ORIENTATION_NORTH, ORIENTATION_WEST, ORIENTATION_SOUTH;
	}
	private ORIENTATION orientation;

	private ORIENTATION currInstruction = null;

	public synchronized Double getX() {
		return x;
	}
	public synchronized void setX(Double x) {
		this.x = x;
	}

	public synchronized Double getY() {
		return y;
	}
	public synchronized void setY(Double y) {
		this.y = y;
	}

	public synchronized ORIENTATION getOrientation() {
		return orientation;
	}
	public synchronized void setOrientation(ORIENTATION orientation) {
		this.orientation = orientation;
	}

	public synchronized ORIENTATION getCurrInstruction() {
		return currInstruction;
	}
	public synchronized void setCurrInstruction(ORIENTATION currInstruction) {
		this.currInstruction = currInstruction;
	}
	
	public Point(Double x, Double y, ORIENTATION initialOrientation) {
		this.x = x;
		this.y = y;
		this.orientation = initialOrientation;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		double threshold = 0.45;
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (Math.abs(x-other.x) > threshold)
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (Math.abs(y-other.y) > threshold)
			return false;
		return true;
	}
}
