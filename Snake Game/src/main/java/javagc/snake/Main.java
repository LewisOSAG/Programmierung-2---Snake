package javagc.snake;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javagc.snake.Point.ORIENTATION;
import javagc.snake.SnakeLogic.KeyBindings;

public class Main extends Application {

	private List<Snake> players = new ArrayList<>();
	private int playerNum;
	private ScheduledThreadPoolExecutor timer;
	private List<ScheduledFuture<?>> futureTasks;
	private Connection connection;
	DbContext dbContext;
	private Stage stage;
	private HBox menu;
	private BorderPane defScreen;
	private BorderPane setScreen;
	private BorderPane gameScreen;
	private AtomicBoolean gameRunning;

	private final double objectScale = 12.5;

	private Settings settingsWindow = new Settings();
	private SnakeMenu sMenu = new SnakeMenu();
	private Assets assets;

	private SnakeCollisionHandler colHandler;
	private ScoreChangedListener sCL;

	@Override
	public void start(Stage stage) {
		this.stage = stage;
		connectToDb();
		gameRunning = new AtomicBoolean(false);
		menu = createMenu();
		defScreen = createWidgets(createContent());
		setScreen = createSettings();

		timer = new ScheduledThreadPoolExecutor(5);
		futureTasks = new ArrayList<>();

		var root = defScreen;
		Scene scene = new Scene(root);

		stage.setTitle("JFX Snake");	
		stage.setScene(scene);
		stage.setOnCloseRequest((e) -> {
			onQuit(null);
		});

		stage.setResizable(false);
		stage.show();
	}

	private void connectToDb (){
		try {
			//Creates a local db in the users home directory
			String url = "jdbc:h2:~/test";
			String username = "sa";
			String pw = "123";
			connection = DriverManager.getConnection(url, username, pw);
			System.out.println("Connected to db");
			createSqlTable(connection);
			String sql = "SELECT * FROM usertable";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String currentUser= resultSet.getString("username");
				System.out.println("User:" + currentUser);
			}
			dbContext = new DbContext(connection);
			Map<String, Integer> scoreBoardData = loadScoreboardData();
			System.out.println(scoreBoardData);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Terminate application...");
			System.exit(0);
		}
	}

	private void createSqlTable (Connection connection) throws SQLException {
		String createUserTableQuery = "CREATE TABLE IF NOT EXISTS usertable "
				 + "(username VARCHAR(32), "
				 + "score INT, "
                 + "color VARCHAR(12), "
				 + "ctrlUp VARCHAR(1), "
				 + "ctrlDown VARCHAR(1), "
				 + "ctrlLeft VARCHAR(1), "
				 + "ctrlRight VARCHAR(1) "
				 + ");";
		Statement statement = connection.createStatement();
		statement.executeUpdate(createUserTableQuery);
	}

	private Pane createContent() {
		var canvas = new Pane();

		canvas.setMinSize(900, 750);
		canvas.setBackground(new Background(new BackgroundFill(Paint.valueOf("BLACK"), null, null)));

		return canvas;
	}
	private BorderPane createWidgets(Pane contentScreen) {
		var root = new BorderPane();

		root.setTop(menu);
		root.setCenter(contentScreen);
		root.centerProperty();

		return root;
	}

	private BorderPane createSettings() {
		var root = new BorderPane();

		root.setBackground(new Background(
				new BackgroundFill(
						new LinearGradient(0, 0, 0, 1, true,
								CycleMethod.NO_CYCLE,
								new Stop(0, Color.web("#FEFEFE")),
								new Stop(1, Color.web("#FFCFCF"))
								), null, null)));
		var ret = new Button("Back to Game");
		ret.setAlignment(Pos.BOTTOM_RIGHT);
		ret.setOnAction(this::onBackToGame);
		var ctrlBox = new HBox(ret);
		ctrlBox.setMaxWidth(Double.MAX_VALUE);
		ctrlBox.setPadding(new Insets(5));
		ctrlBox.setSpacing(5);
		ctrlBox.setAlignment(Pos.CENTER_RIGHT);
		root.setBottom(ctrlBox);

		//Check if dbContext is null
		Optional.ofNullable(dbContext).ifPresent(ctx -> settingsWindow.createSettingsWidgets(root, dbContext));

		return root;
	}
	private void updateSettings() {
		//Check if dbContext is null
		Optional.ofNullable(dbContext).ifPresent(ctx -> settingsWindow.createSettingsWidgets(setScreen, dbContext));
	}

	private HBox createMenu() {
		HBox menuBox = new HBox();
		var menuBar = new MenuBar();

		Menu game = new Menu("Game");
		MenuItem settings = new MenuItem("Settings");
		settings.setOnAction(this::onOpenSettings);
		MenuItem startGame = new MenuItem("Start New Game");
		startGame.setOnAction(this::onStartNewGame);
		startGame.setAccelerator(new KeyCodeCombination(KeyCode.F2));

		game.getItems().addAll(settings, startGame);
		if(gameScreen != null) {
			MenuItem gameManip;

			if(!gameRunning.get()) {
				gameManip = new MenuItem("Continue Game");
				gameManip.setOnAction(this::onGameToActiveState);
			}
			else {
				gameManip = new MenuItem("Pause Game");
				gameManip.setOnAction(this::onGameToPausedState);
				gameManip.setAccelerator(new KeyCodeCombination(KeyCode.ESCAPE));
			}
			game.getItems().add(gameManip);
		}
		menuBar.getMenus().add(game);
		menuBox.getChildren().add(menuBar);

		//SnakeMenu erstellen
		sMenu.createMenuWidgets(menuBox, settingsWindow.playerCount, settingsWindow.playerNames, settingsWindow.playerColors);

		return menuBox;
	}
	private void updateMenu(BorderPane root) {
		menu = createMenu();
		root.setTop(menu);
	}

	private BorderPane createGOScreen() {
		var root = new BorderPane();
		VBox box = new VBox();
		HBox scores = new HBox();

		root.setBackground(new Background(new BackgroundFill(Paint.valueOf("WHITE"), null, null)));
		var ret = new Button("Back to Game");
		ret.setOnAction(this::onBackToGame);
		Label go = new Label("Game Over!");
		go.setFont(new Font(100));

		if(playerNum == 1) {
			Label scr = new Label("Score: " + sMenu.getScores()[0]);
			scr.setFont(new Font(40));
			savePlayerScore(settingsWindow.playerNames[0],sMenu.getScores()[0]);
			scores.getChildren().add(scr);
		} else {
			for(int i = 0; i < playerNum; i++) {
				Label scr = new Label("Player " + (i+1) + ": " + sMenu.getScores()[i] + "    ");
				scr.setFont(new Font(20));
				savePlayerScore(settingsWindow.playerNames[i],sMenu.getScores()[i]);
				scores.getChildren().add(scr);
			}
		}		
		scores.setAlignment(Pos.CENTER);
		box.setAlignment(Pos.CENTER);
		box.setSpacing(30);
		box.getChildren().addAll(go, scores, ret);
		root.setCenter(box);

		return root;
	}

	private void savePlayerScore (String playerName, int score) {
		System.out.println(players);
		String sql = "UPDATE usertable " +
				"SET score = "+ score+" WHERE username='"+playerName+"'";
		try {
			connection.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Map<String, Integer> loadScoreboardData () {
		String sql = "SELECT * FROM  usertable " +
				"ORDER BY score";
		Map<String, Integer> userscoreMap = new HashMap<String, Integer>();
		try {
			Statement statement = connection.createStatement();
			statement.setMaxRows(10);
			ResultSet resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String username = resultSet.getString("username");
				int score = resultSet.getInt("score");
				userscoreMap.put(username, score);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return userscoreMap;
	}

	private Pane createGameEnv(Integer playerNum) {
		sMenu.setScores();
		var gameField = createContent();
		assets = new Assets(gameField, objectScale);

		if(playerNum > 0) {
			this.playerNum = playerNum;

			players.clear();
			for(Integer i = Integer.valueOf(0); i < playerNum; i++) {
				double xOffset, yOffset;

				switch(i) {
				case 0:
					xOffset = objectScale*6;
					yOffset = 0;
					break;
				case 1:
					xOffset = 0;
					yOffset = -objectScale*6;
					break;
				case 2: 
					xOffset = -objectScale*6;
					yOffset = 0;
					break;
				case 3:
					xOffset = 0;
					yOffset = objectScale*6;
					break;
				default:
					xOffset = 0;
					yOffset = 0;
					break;
				}
				KeyBindings keyBindings = new KeyBindings(settingsWindow.getPlayerNControlUp(i), settingsWindow.getPlayerNControlLeft(i), settingsWindow.getPlayerNControlDown(i), settingsWindow.getPlayerNControlRight(i));
				Snake player = new Snake(gameField.getMinWidth()/2+xOffset, gameField.getMinHeight()/2+yOffset, objectScale, ORIENTATION.values()[i], settingsWindow.getPlayerColors()[i], keyBindings, i, gameField);
				players.add(player);
				gameField.getChildren().addAll(player.getSnake());
				stage.addEventHandler(KeyEvent.KEY_PRESSED, player.getPlayer().getHandler());

				sCL = new ScoreChangedListener() {
					@Override
					public void scoreChanged(int newVal, int playerNum) {
						Platform.runLater(() -> {
							sMenu.updateScore(newVal, playerNum);
						});
					}
				};
				player.addScoreChangedListener(sCL);

				Runnable r = () -> {
					try {
						player.getTask().run();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				};

				futureTasks.add(timer.scheduleAtFixedRate(r, 0, 1000/32, TimeUnit.MILLISECONDS)); // 32 FPS
			}
			colHandler = new SnakeCollisionHandler(players, assets, gameField);
			assets.createAssetTask();

			Runnable rf = () -> {
				try {
					if(gameRunning.get())
						assets.getTask().run();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			};
			futureTasks.add(timer.scheduleAtFixedRate(rf, 10, 2500, TimeUnit.MILLISECONDS)); // One Fruit every 2.5 seconds

			Runnable rc = () -> {
				try {
					if(gameRunning.get()) {
						colHandler.collide();
						Platform.runLater(() -> {
							checkForGameOver();
						});
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			};
			futureTasks.add(timer.scheduleAtFixedRate(rc, 0, 1000/32, TimeUnit.MILLISECONDS)); // 32 FPS
		}

		return gameField;
	}

	private void checkForGameOver() {
		if(players.size() == 0) {
			onGameOver(null);
		}
	}

	private void onOpenSettings(ActionEvent ae) {
		onExitGame(null);
		updateSettings();
		stage.getScene().setRoot(setScreen);
	}
	private void onExitGame(ActionEvent ae) {
		if(gameScreen != null) {
			onGameToPausedState(null);
			defScreen = gameScreen;
		}
	}
	private void onBackToGame(ActionEvent ae) {
		updateMenu(defScreen);
		stage.getScene().setRoot(defScreen);
	}
	private void onStartNewGame(ActionEvent ae) {
		killActiveTasks();
		gameScreen = createWidgets(createGameEnv(Integer.valueOf(settingsWindow.playerCount)));
		onGameToActiveState(null);
		stage.getScene().setRoot(gameScreen);
	}
	private void onGameToPausedState(ActionEvent ae) {
		if(gameScreen != null) {
			setGameRunning(false);
			updateMenu(gameScreen);
		}
	}
	private void onGameToActiveState(ActionEvent ae) {	
		if(gameScreen != null) {
			setGameRunning(true);
			updateMenu(gameScreen);
		}
	}
	public void onGameOver(ActionEvent ae) {
		killActiveTasks();	
		defScreen = createWidgets(createContent());
		gameScreen = null;	
		stage.getScene().setRoot(createGOScreen());
	}
	private void onQuit(ActionEvent ae) {
		Platform.exit();
		System.exit(0);
	}

	private void killActiveTasks() {
		if(futureTasks.size() != 0) {
			for(ScheduledFuture<?> future : futureTasks) {
				future.cancel(true);
			}
		}
	}

	private void setGameRunning(boolean running) {
		for(Snake sg : players) {
			sg.getPlayer().setRunning(running);
		}
		gameRunning.set(running);
	}

	public static void main(String[] args) {
		launch(args);
	}

}
