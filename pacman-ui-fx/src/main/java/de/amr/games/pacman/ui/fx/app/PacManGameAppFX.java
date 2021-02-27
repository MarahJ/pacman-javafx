package de.amr.games.pacman.ui.fx.app;

import java.io.IOException;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The Pac-Man game app running in a JavaFX UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	static boolean ms_pacman;

	public static void main(String[] args) {
		if (args.length > 0) {
			ms_pacman = "-mspacman".equals(args[0]);
		}
		launch(args);
	}

	@Override
	public void start(Stage stage) throws IOException {
		try {
			PacManGameController controller = new PacManGameController();
			controller.play(ms_pacman ? GameType.MS_PACMAN : GameType.PACMAN);
			controller.addView(new PacManGameUI_JavaFX(stage, controller, 2.0));
			controller.showViews();
			controller.startGameLoop();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}