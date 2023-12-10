package javagc.snake;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javagc.snake.AssetsLogic.FruitLogic.FRUITTYPE;

/**
 * This class contains all the logical components of the game assets
 * 
 * In this version it only holds components of the fruit asset
 * The Fruit class in Assets holds a FruitLogic Object and thus makes a complete fruit ready to render
 */
public class AssetsLogic {
	
	// Holds the instances of FruitLogic of the fruits that are rendered on the Pane
	private List<FruitLogic> fruits  = new ArrayList<>();
	
	/**
	 * This class contains the type and position property of a fruit
	 * and methods for identification and comparison
	 */
	public static class FruitLogic {
		
		/**
		 * This enum defines the amount of points a certain type of fruit
		 * adds to the score of the player upon collection
		 */
		public static enum FRUITTYPE {
			APPLE(3), CHERRY(1), LEMON(2), PLUM(2), WATERMELON(5);
			
			private final int points;
			
			private FRUITTYPE(int points) {
				this.points = points;
			}
		}
		// Holds the type of the fruit
		private FRUITTYPE type;
		// Holds the position of the fruit
		private Point pos;
		
		/**
		 * Creates a nem FuitLogic instance that holds the logical components of a fruit
		 * 
		 * @param type	-	type of the fruit (See enum FRUITTYPE)
		 * @param x		- 	position on the x axis
		 * @param y		-	position on the y axis
		 */
		public FruitLogic(FRUITTYPE type, Double x, Double y) {
			this.type = type;
			pos = new Point(x, y, null);
		}
		
		/**
		 * Generates a unique hashcode for every FruitLogic instance with the same position and type 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		
		/**
		 * Implemets a safe comparison method for FruitLogic objects
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FruitLogic other = (FruitLogic) obj;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
		/**
		 * Getter for the type of the fruit
		 * 
		 * @return	-	returns the type (see enum FRUITTYPE)
		 */
		public FRUITTYPE getType() {
			return type;
		}
		/**
		 * Getter for the amount of points the fruit yields
		 * 
		 * @return	-	returns the points (see enum FRUITTYPE)
		 */
		public synchronized int getPoints() {
			return type.points;
		}
		/**
		 * Getter for the position of the fruit
		 * 
		 * @return	-	returns the position (see class Point)
		 */
		public synchronized Point getPos() {
			return pos;
		}
		/**
		 * Setter for the position of the fruit
		 * 
		 * @param pos	-	the position as a Point (see class Point)
		 */
		public synchronized void setPos(Point pos) {
			this.pos = pos;
		}
	}

	/**
	 * Getter for the fruits list
	 * 
	 * @return	-	returns a reference to the fruits list
	 */
	public synchronized List<FruitLogic> getFruits() {
		return fruits;
	}

	/**
	 * Generates a random fruit with random position and type
	 * 
	 * @param paneWidth		-	width of the pane the fruit shall be rendered on
	 * @param paneHeight	-	height of the pane the fruit shall be rendered on
	 * @return				-	returns a reference to the newly created fruit object
	 */
	public FruitLogic generateRandomFruit(double paneWidth, double paneHeight) {
		Random rand = new Random();
		
		double x = rand.nextInt((int)paneWidth);
		double y = rand.nextInt((int)paneHeight);
		int type = rand.nextInt(FRUITTYPE.values().length);
		
		return new FruitLogic(FRUITTYPE.values()[type], x, y);
	}
	/**
	 * Removes an instance of FruitLogic from the fruits list
	 * 
	 * @param fruit	-	the FruitLogic object that should be removed
	 */
	public synchronized void removeFruit(FruitLogic fruit) {
		fruits.remove(fruit);
	}
}
