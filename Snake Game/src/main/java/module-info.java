module javagc.snake {
	// with that in module one can launch the main JavaFx stuff in a
	// Main JavaFx class (not forcing to use a separate class)
	exports javagc.snake;
    
	requires transitive javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
    requires java.sql;

    // for fxml
	// requires transitive javafx.fxml;
	// opens javagc.snake to javafx.fxml;
}
