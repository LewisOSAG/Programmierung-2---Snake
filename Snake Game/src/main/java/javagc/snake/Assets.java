package javagc.snake;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javagc.snake.AssetsLogic.FruitLogic;

/**
 * This class contains all the graphical components of the game assets
 * 
 * In this version it only holds components of the fruit asset
 * An instance of the Fruitclass combined with an instance of the FruitLogic class (contained in AssetsLogic) makes a complete fruit
 */
public class Assets {

	// An instance of AssetsLogic to get access to its contents
	private AssetsLogic assets = new AssetsLogic();

	// Holds the size of a grid cell
	private final double scale;
	
	// Holds the fruits to be rendered on the pane
	private List<Fruit> fruits;

	// The runnable that handles all the assets
	private Runnable assetTask;
	
	// The Pane the assets get rendered on
	private Pane gameField;
	
	/**
	 * This class contains the FruitLogic and the shape of a fruit
	 */
	public static class Fruit {
		
		// An instance of FruitLogic for the logical part of the fruit
		private final FruitLogic fruit;

		// A Shape for the graphical part of the fruit
		private Shape shape;

		/**
		 * Creates a fruit
		 * 
		 * @param fruit	-	the FruitLogic for the fruit
		 * @param scale	-	the scale of the grid
		 */
		public Fruit(FruitLogic fruit, Double scale) {
			this.fruit = fruit;
			double fruitSize = scale/5 * 4;

			shape = new Circle((fruit.getPos().getX())*scale+fruitSize/2, (fruit.getPos().getY())*scale+fruitSize/2, fruitSize/2);

			Color color = Color.TRANSPARENT;
			switch(fruit.getType()) {
			case APPLE:
				color = Color.CHARTREUSE;
				break;
			case CHERRY:
				color = Color.CRIMSON;
				break;
			case LEMON:
				color = Color.GOLD;
				break;
			case PLUM:
				color = Color.INDIGO;
				break;
			case WATERMELON:
				color = Color.DARKGREEN;
				break;
			}

			shape.setFill(color);
		}
		
		/**
		 * Getter for the FruitLogic
		 * 
		 * @return	-	returns a reference to its FruitLogic
		 */
		public synchronized FruitLogic getFruit() {
			return fruit;
		}

		/**
		 * Getter for the shape
		 * 
		 * @return	-	returns a reference to its shape
		 */
		public Shape getShape() {
			return shape;
		}
		
		/**
		 * Setter for the shape
		 * 
		 * @param shape	-	the shape thats to be set
		 */
		public void setShape(Shape shape) {
			this.shape = shape;
		}
	}

	/**
	 * Creates an instance of Assets
	 * 
	 * @param gameField	-	Pane for rendering
	 * @param scale		-	grid scale
	 */
	public Assets(Pane gameField, Double scale) {
		this.gameField = gameField;
		this.scale = scale;

		fruits = new ArrayList<>();
	}
	
	/**
	 * Getter for the fruits list
	 * 
	 * @return	-	returns a reference to the fruits list
	 */

	public List<Fruit> getFruits() {
		return fruits;
	}

	/**
	 * Getter for the asset runnable
	 * 
	 * @return	-	returns the runnable for asset handling
	 */
	public Runnable getTask() {
		return assetTask;
	}

	public void addFruit(Pane pane) {
		Fruit fruit = new Fruit(assets.generateRandomFruit(pane.getMinWidth()/scale, pane.getMinHeight()/scale), scale);
		synchronized(this) {
			fruits.add(fruit);
		}

		Platform.runLater(() -> {
			gameField.getChildren().add(fruit.getShape());
		});
	}

	/**
	 * Removes the given fruit from the Pane and the fruit list
	 * 
	 * @param fruit	-	the fruit that should be removed
	 */
	public void removeFruit(Fruit fruit) {
		assets.removeFruit(fruit.getFruit());

		synchronized(fruits) {
			fruits.remove(fruit);
		}

		Platform.runLater(() -> {
			gameField.getChildren().remove(fruit.getShape());
		});
	}

	/**
	 * Creates a Runnable for execution in main
	 * 
	 * @param paneWidth		-	width of the pane the fruit shall be rendered on
	 * @param paneHeight	-	height of the pane the fruit shall be rendered on
	 */
	public void createAssetTask() {
		assetTask = new Runnable() {
			@Override
			public void run() {
				Platform.runLater(() -> {
					addFruit(gameField);
				});
			}
		};
	}
}
