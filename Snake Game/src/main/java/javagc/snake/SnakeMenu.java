package javagc.snake;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Menü mit zugehörigen Widgets und Daten
 * @author Sebastian Hensel / 1924763
 */
public class SnakeMenu
{
	HBox root;
	int playerCount;
	String[] playerNames;
	Color[] playerColors;
	Label[] labels = new Label[4];
	int[] scores = {0, 0, 0, 0};
	
	public SnakeMenu()
	{
	}
	
	void createMenuWidgets(HBox root, int playerCount, String[] playerNames, Color[] playerColors)
	{
		/**
		 * Erstellt Widgets der Menüzeile
		 */
		this.root = root;
		this.playerCount = playerCount;
		this.playerNames = playerNames;
		this.playerColors = playerColors;
				
		root.setSpacing(16);
		
		//player points display
		HBox playerPoints = new HBox();
		playerPoints.setSpacing(16);
		for(int i=0; i<playerCount; i++)
		{
			labels[i] = new Label(playerNames[i] + ": " + scores[i] + " P");
			labels[i].setStyle("-fx-text-fill: #" +  String.format("%06X", colorToInt(playerColors[i])));
			labels[i].setFont(new Font(20));
			playerPoints.getChildren().add(labels[i]);
		}

		root.getChildren().add(playerPoints);
	}
	
	public void updatePlayerLabels()
	{
		/**
		 * Aktualisiert die Spielernamen
		 */
		for(int i=0; i<playerCount; i++)
		{
			labels[i].setText(playerNames[i] + ": " + scores[i] + " P");
		}
	}
	
	private int colorToInt(Color col)
	{
		/**
		 * Gibt Farbe in Hexadezimalform aus JavaFX-Farbe zurück
		 */
		int c;
		c = ((int) Math.round((Double) col.getRed() * 255) << 16);
		c += ((int) Math.round((Double) col.getGreen() * 255) << 8);
		c += ((int) Math.round((Double) col.getBlue() * 255));
		return c;
	}
	
	public void updateScore(int nv, int playerNumber)
	{
		/**
		 * Aktualisiert die Punktzahl eines Spielers
		 */
		scores[playerNumber] = nv;
		labels[playerNumber].setText(playerNames[playerNumber] + ": " + scores[playerNumber] + " P");
	}
	
	public void setScores()
	{
		/**
		 * Setzt die Punktzahlen zurück
		 */
		for(int i=0; i<scores.length; i++)
		{
			scores[i] = 0;
		}
	}
	public int[] getScores()
	{
		/**
		 * Gibt aktuellen Punktestand aller Spieler zurück
		 */
		return scores;
	}
}
