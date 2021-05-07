package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getMazeWallColor;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.LivesCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * 3D-scene displaying the maze and the game play. Used in both game variants.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	static final int PERSPECTIVE_TOTAL = 0;
	static final int PERSPECTIVE_FOLLOWING_PLAYER = 1;
	static final int PERSPECTIVE_NEAR_PLAYER = 2;

	static final int LIVES_COUNTER_MAX = 5;
	static final int WALL_HEIGHT = 4;

	private final SubScene subSceneFX;
	private final PlayScene3DAnimations animations;
	private final PlayScenePerspective[] perspectives;

	private PacManGameController gameController;
	private int selectedPerspective;

	Maze3D maze3D;
	Player3D player3D;
	Map<Ghost, Ghost3D> ghosts3D;
	Bonus3D bonus3D;
	ScoreNotReally3D score3D;
	LevelCounter3D levelCounter3D;
	LivesCounter3D livesCounter3D;

	public PlayScene3D(SoundManager sounds) {
		animations = new PlayScene3DAnimations(this, sounds);
		subSceneFX = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		subSceneFX.setCamera(new PerspectiveCamera(true));
		perspectives = new PlayScenePerspective[] { //
				new TotalPerspective(subSceneFX), //
				new FollowingPlayerPerspective(subSceneFX), //
				new NearPlayerPerspective(subSceneFX) };
		selectPerspective(PERSPECTIVE_FOLLOWING_PLAYER);
		subSceneFX.addEventHandler(KeyEvent.KEY_PRESSED, event -> selectedPerspective().handle(event));
	}

	@Override
	public void init() {
		log("%s: init", this);

		final var r2D = game().variant() == GameVariant.MS_PACMAN ? MsPacManScenes.RENDERING : PacManScenes.RENDERING;
		final var width = PacManGameWorld.DEFAULT_WIDTH * TS;
		final var height = PacManGameWorld.DEFAULT_HEIGHT * TS;

		maze3D = new Maze3D(width, height);
		maze3D.setResolution(8);
		maze3D.setWallHeight(4);
		maze3D.setFloorTexture(new Image(getClass().getResourceAsStream("/common/escher-texture.jpg")));
		maze3D.setWallColor(getMazeWallColor(game().variant(), game().currentLevel().mazeNumber));
		animations.buildMaze();

		player3D = new Player3D(game().player());
		ghosts3D = game().ghosts().collect(Collectors.toMap(Function.identity(), ghost -> new Ghost3D(ghost, r2D)));
		bonus3D = new Bonus3D(game().variant(), r2D);

		score3D = new ScoreNotReally3D();
		livesCounter3D = new LivesCounter3D(new V2i(2, 1));
		livesCounter3D.setMaxEntries(LIVES_COUNTER_MAX);
		if (gameController.isAttractMode()) {
			score3D.setHiscoreOnly(true);
			livesCounter3D.setVisible(false);
		} else {
			score3D.setHiscoreOnly(false);
			livesCounter3D.setVisible(true);
		}

		levelCounter3D = new LevelCounter3D(new V2i(25, 1), r2D);
		levelCounter3D.update(game());

		final var ambientLight = new AmbientLight();
		final var playerLight = new PointLight();
		playerLight.translateXProperty().bind(player3D.translateXProperty());
		playerLight.translateYProperty().bind(player3D.translateYProperty());
		playerLight.setTranslateZ(-4);

		final var contentRoot = new Group();
		contentRoot.setTranslateX(-0.5 * width);
		contentRoot.setTranslateY(-0.5 * height);
		contentRoot.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		contentRoot.getChildren().addAll(ghosts3D.values());
		contentRoot.getChildren().addAll(ambientLight, playerLight);

		subSceneFX.setRoot(new Group(contentRoot, new CoordinateSystem(subSceneFX.getWidth())));
	}

	@Override
	public void update() {
		score3D.update(game());
		livesCounter3D.update(game());
		player3D.update();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus());
		// Keep score text in plain sight. TODO: is this the recommended way to do this?
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.setRotate(subSceneFX.getCamera().getRotate());

		selectedPerspective().follow(player3D);
		animations.update();
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		animations.onGameEvent(gameEvent);
	}

	@Override
	public void end() {
		log("%s: end", this);
	}

	public PlayScenePerspective selectedPerspective() {
		return perspectives[selectedPerspective];
	}

	public void selectPerspective(int index) {
		selectedPerspective = index;
		selectedPerspective().reset();
	}

	public void nextPerspective() {
		selectPerspective((selectedPerspective + 1) % perspectives.length);
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public SubScene getSubSceneFX() {
		return subSceneFX;
	}

	@Override
	public void resize(double width, double height) {
		// data binding does the job
	}

}