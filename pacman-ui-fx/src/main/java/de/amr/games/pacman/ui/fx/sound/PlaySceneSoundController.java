package de.amr.games.pacman.ui.fx.sound;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.GhostReturningHomeEvent;
import de.amr.games.pacman.controller.event.PacManFoundFoodEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.ui.PacManGameSound;
import javafx.scene.media.AudioClip;

/**
 * Controls sound for 2D and 3D play scenes in reaction to game events.
 * 
 * TODO not sure if sound should be handled separately from the visual
 * animations
 * 
 * @author Armin Reichert
 */
public class PlaySceneSoundController {

	public final SoundManager sounds;
	private PacManGameController gameController;

	public PlaySceneSoundController(SoundManager sounds) {
		this.sounds = sounds;
	}

	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	private AbstractGameModel game() {
		return gameController.game();
	}

	public void update() {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameController.state == PacManGameState.HUNTING) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game().player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
					log("Munching sound clip %s stopped", munching);
				}
			}
		}
	}

	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (newState == PacManGameState.READY) {
			sounds.stopAll();
			if (!gameController.isAttractMode() && !gameController.isGameRunning()) {
				gameController.stateTimer().resetSeconds(4.5);
				sounds.play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			sounds.stopAll();
		}

		// enter GHOST_DYING
		if (newState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
		}
	}

	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameController.isAttractMode()) {
			return;
		}
		sounds.setMuted(false);

		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
			}
			PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
			if (!sounds.getClip(siren).isPlaying())
				sounds.loop(siren, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof PacManGainsPowerEvent) {
			sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		}

		else if (gameEvent instanceof PacManFoundFoodEvent) {
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
				Logging.log("Munching sound clip %s started", munching);
			}
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			sounds.play(PacManGameSound.EXTRA_LIFE);
		}

		else if (gameEvent instanceof GhostReturningHomeEvent) {
			sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
		}
	}
}