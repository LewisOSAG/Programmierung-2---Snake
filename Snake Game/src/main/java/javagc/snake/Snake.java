package javagc.snake;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javagc.snake.Point.ORIENTATION;
import javagc.snake.SnakeLogic.KeyBindings;

/**
 * Snake ist die Visualisierungs- und Hauptklasse des Snakes. 
 * Diese Klasse wird von der Application aufgerufen um den Snake zu erstellen und später zu handlen.
 * Hält dazu intern eine SnakeLogic
 * Snake definiert das Aussehen des Snakes
 * 
 * @author user_Matness
 *
 */
public class Snake {

	private SnakeLogic player;
	private int playerNum;
	private Color playerColor;
	private Runnable task;

	private List<Shape> snake = new ArrayList<>();

	private Pane gameField;

	private ScoreChangedListener sCL = null;

	private final double partSize; // steuert die Größe des Snakes (10 ist guter Wert, 20 noch ok, also am Besten Wert zwischen 10 und 20 nehmen)
	private final double spacing; // Abstand zwischen den einzelnen Elementen
	private final double scale;

	public SnakeLogic getPlayer() {
		return player;
	}

	public int getPLayerNum() {
		return playerNum;
	}

	public Runnable getTask() {
		return task;
	}

	public List<Shape> getSnake() {
		return snake;
	}

	public double getPartSize() {
		return partSize;
	}

	public double getSpacing() {
		return spacing;
	}

	public double getX() {
		return player.getSnake().get(0).getX()*scale;
	}

	public double getY() {
		return player.getSnake().get(0).getY()*scale;
	}

	public Snake(Double x, Double y, Double scale, ORIENTATION initialOrientation, Color playerColor, KeyBindings keyBindings, int playerNum, Pane gameField) {
		this.scale = scale;
		partSize = scale/5 * 4;
		spacing = scale/4;

		// SnakeLogic soll nur an ganzzahligen Koordinaten erzeugt werden...
		player = new SnakeLogic((Integer.valueOf((int)(x/scale))).doubleValue(), (Integer.valueOf((int)(y/scale))).doubleValue(), initialOrientation, keyBindings);
		this.playerColor = playerColor;
		this.playerNum = playerNum;
		this.gameField = gameField;

		createSnake();
		createSnakeTask();
	}


	public void addScore(Integer scorePlus) {
		player.addScore(scorePlus);
		if(sCL != null) {
			sCL.scoreChanged(player.getScore(), playerNum);
		}
	}

	public void addScoreChangedListener(ScoreChangedListener sCL) {
		this.sCL = sCL;
	}

	/**
	 * Funktion zur Verarbeitung des wachsens eines Snakes
	 * 
	 * @param ae
	 * ae macht es möglich growSnake() als EventHandler an eine Aktion zu hängen.
	 */
	public void growSnake() {
		// lasse Snake wachsen
		Point p = player.grow();

		// erzeuge Visualisierung für das neue Element
		Rectangle rect = addSnakePart(p);

		// füge neues Visualisierungselement dem gameField hinzu
		Platform.runLater(() -> {
			gameField.getChildren().add(rect);
		});
	}
	private Rectangle addSnakePart(Point p) {
		Rectangle rect = new Rectangle(p.getX()*scale, p.getY()*scale, partSize, partSize);
		rect.setFill(playerColor);
		snake.add(rect);

		return rect;
	}

	/**
	 * Funktion zum Erzeugen der Snake-Visualisierung
	 */
	private void createSnake() {
		for(Point p : player.getSnake()) {
			addSnakePart(p);
		}
	}

	public void destroySnake() {
		player.stopSnake();

		Platform.runLater(() -> {
			gameField.getChildren().removeAll(getSnake());
		});
	}

	/**
	 * Funktion zum Erzeugen des Snake Runnables, der für die Animation des Snakes verantwortlich ist
	 */
	private void createSnakeTask() {
		task = new Runnable() {
			@Override
			public void run() {
				if(player.isRunning()) {
					player.move();

					Platform.runLater(() -> {
						for(int i = 0; i < getSnake().size(); i++) {
							snake.get(i).relocate(player.getSnake().get(i).getX()*scale, player.getSnake().get(i).getY()*scale);
						}
					});
				}
			}
		};
	}
}
