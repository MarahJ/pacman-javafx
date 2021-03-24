package de.amr.games.pacman.ui.fx.scenes.common.scene2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * This is where the action is.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private class LevelCompleteAnimation {

		private final SequentialTransition sequence;
		private final Timeline flashing;
		private TimedSequence<?> mazeFlashing; // TODO get rid og this
		private int imageIndex;

		public LevelCompleteAnimation(int numFlashes) {
			// get the maze images which are displayed alternating to create the flashing effect
			GameVariant variant = gameController.gameVariant();
			GameModel game = gameController.game();
			mazeFlashing = Assets2D.RENDERING_2D.get(variant).mazeAnimations().mazeFlashing(game.level.mazeNumber);
			imageIndex = 1;

			flashing = new Timeline(new KeyFrame(Duration.millis(150), e -> {
				imageIndex = (imageIndex + 1) % 2;
			}));
			flashing.setCycleCount(2 * numFlashes);

			PauseTransition start = new PauseTransition(Duration.seconds(2));
			start.setOnFinished(e -> game.player.visible = false);
			PauseTransition end = new PauseTransition(Duration.seconds(1));
			sequence = new SequentialTransition(start, flashing, end);
			sequence.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		}

		public Image getCurrentMazeImage() {
			return (Image) mazeFlashing.frame(imageIndex);
		}

		public void play() {
			sequence.playFromStart();
		}

		public boolean isRunning() {
			return sequence.getStatus() == Status.RUNNING;
		}

		public Duration getTotalDuration() {
			return sequence.getTotalDuration();
		}
	}

	private LevelCompleteAnimation levelCompleteAnimation;

	public PlayScene2D(PacManGameRendering2D rendering, SoundManager sounds) {
		super(rendering, sounds);
	}

	private void playAnimationPlayerDying() {
		GameModel game = gameController.game();
		game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		rendering.playerAnimations().playerDying().delay(120).onStart(() -> {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			if (gameController.isGameRunning()) {
				sounds.play(PacManGameSound.PACMAN_DEATH);
			}
		}).restart();
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
		GameModel game = gameController.game();
		game.player.powerTimer.addEventListener(e -> {
			if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
				game.ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
					TimedSequence<?> flashing = rendering.ghostAnimations().ghostFlashing(ghost);
					long frameTime = e.ticks / (game.level.numFlashes * flashing.numFrames());
					flashing.frameDuration(frameTime).repetitions(game.level.numFlashes).restart();
				});
			}
		});
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		GameVariant variant = gameController.gameVariant();
		GameModel game = gameController.game();
		// enter READY state
		if (newState == PacManGameState.READY) {
			rendering.resetAllAnimations(game);
			if (!gameController.isGameRunning() && !gameController.isAttractMode()) {
				gameController.stateTimer().resetSeconds(4.5);
				Assets2D.SOUND.get(variant).play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter HUNTING state
		if (newState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().restart();
			rendering.playerAnimations().playerMunching(game.player).forEach(TimedSequence::restart);
			gameController.game().ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::restart);
		}

		// exit HUNTING state
		if (oldState == PacManGameState.HUNTING) {
			rendering.mazeAnimations().energizerBlinking().reset();
		}

		// enter PACMAN_DYING state
		if (newState == PacManGameState.PACMAN_DYING) {
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING state
		if (newState == PacManGameState.GHOST_DYING) {
			rendering.mazeAnimations().energizerBlinking().restart();
		}

		// enter LEVEL_COMPLETE state
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			game.ghosts().forEach(ghost -> ghost.visible = false);
			levelCompleteAnimation = new LevelCompleteAnimation(game.level.numFlashes);
			double totalDuration = levelCompleteAnimation.getTotalDuration().toSeconds();
			log("Total LEVEL_COMPLETE animation duration: %f", totalDuration);
			gameController.stateTimer().resetSeconds(totalDuration);
			levelCompleteAnimation.play();
		}

		// enter LEVEL_STARTING state
		if (newState == PacManGameState.LEVEL_STARTING) {
			gameController.stateTimer().forceExpiration();
		}

		// enter GAME_OVER state
		if (newState == PacManGameState.GAME_OVER) {
			game.ghosts().flatMap(rendering.ghostAnimations()::ghostKicking).forEach(TimedSequence::reset);
		}
	}

	@Override
	public void update() {
		super.update();
		render(gameController.game());
	}

	private void render(GameModel game) {
		if (levelCompleteAnimation == null || !levelCompleteAnimation.isRunning()) {
			rendering.drawMaze(gc, game.level.mazeNumber, 0, t(3), false);
			rendering.drawFoodTiles(gc, game.level.world.tiles().filter(game.level.world::isFoodTile),
					game.level::containsEatenFood);
			rendering.drawEnergizerTiles(gc, game.level.world.energizerTiles());
		} else {
			gc.drawImage(levelCompleteAnimation.getCurrentMazeImage(), 0, t(3));
		}
		if (gameController.isGameRunning()) {
			rendering.drawLivesCounter(gc, game, t(2), t(34));
			rendering.drawGameState(gc, game, gameController.state);
			rendering.drawScore(gc, game, false);
		} else {
			rendering.drawGameState(gc, game, PacManGameState.GAME_OVER);
			rendering.drawScore(gc, game, true);
		}
		rendering.drawBonus(gc, game.bonus);
		rendering.drawPlayer(gc, game.player);
		game.ghosts().forEach(ghost -> rendering.drawGhost(gc, ghost, game.player.powerTimer.isRunning()));
		rendering.drawLevelCounter(gc, game, t(25), t(34));
	}
}