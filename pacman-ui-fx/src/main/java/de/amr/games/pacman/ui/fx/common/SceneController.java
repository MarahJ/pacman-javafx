package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.PacManGameSounds;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import javafx.scene.Camera;

public class SceneController {

	public final EnumMap<GameType, FXRendering> renderings = new EnumMap<>(GameType.class);
	public final EnumMap<GameType, SoundManager> sounds = new EnumMap<>(GameType.class);

	private Map<PacManGameState, List<Class<? extends GameScene>>> pacManSceneClasses = new HashMap<>();
	private Map<PacManGameState, List<Class<? extends GameScene>>> msPacManSceneClasses = new HashMap<>();

	public SceneController() {
		renderings.put(MS_PACMAN, new MsPacMan_StandardRendering());
		sounds.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));

		renderings.put(PACMAN, new PacMan_StandardRendering());
		sounds.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));

		pacManSceneClasses.put(PacManGameState.INTERMISSION, Arrays.asList(PacMan_IntermissionScene1.class,
				PacMan_IntermissionScene2.class, PacMan_IntermissionScene3.class));
		pacManSceneClasses.put(PacManGameState.INTRO, Arrays.asList(PacMan_IntroScene.class));
		Stream.of(PacManGameState.values())
				.filter(state -> state != PacManGameState.INTRO && state != PacManGameState.INTERMISSION)
				.forEach(state -> pacManSceneClasses.put(state, Arrays.asList(PlayScene2D.class, PlayScene3D.class)));

		msPacManSceneClasses.put(PacManGameState.INTERMISSION, Arrays.asList(MsPacMan_IntermissionScene1.class,
				MsPacMan_IntermissionScene2.class, MsPacMan_IntermissionScene3.class));
		msPacManSceneClasses.put(PacManGameState.INTRO, Arrays.asList(MsPacMan_IntroScene.class));
		Stream.of(PacManGameState.values())
				.filter(state -> state != PacManGameState.INTRO && state != PacManGameState.INTERMISSION)
				.forEach(state -> msPacManSceneClasses.put(state, Arrays.asList(PlayScene2D.class, PlayScene3D.class)));
	}

	public boolean isSuitableScene(PacManGameController controller, GameScene scene) {
		if (controller.isPlaying(GameType.PACMAN)) {
			return pacManSceneClasses.get(controller.getGame().state).contains(scene.getClass());
		} else {
			return msPacManSceneClasses.get(controller.getGame().state).contains(scene.getClass());
		}
	}

	public GameScene createGameScene(PacManGameController controller, Camera camera, double height, boolean _3D) {
		GameModel game = controller.getGame();
		if (controller.isPlaying(PACMAN)) {
			FXRendering r = renderings.get(PACMAN);
			SoundManager s = sounds.get(PACMAN);
			switch (game.state) {
			case INTRO:
				return new PacMan_IntroScene(camera, controller, r, s);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new PacMan_IntermissionScene1(camera, controller, r, s);
				}
				if (game.intermissionNumber == 2) {
					return new PacMan_IntermissionScene2(camera, controller, r, s);
				}
				if (game.intermissionNumber == 3) {
					return new PacMan_IntermissionScene3(camera, controller, r, s);
				}
				throw new IllegalStateException();
			default:
				return _3D ? new PlayScene3D(controller, height) : new PlayScene2D(camera, controller, r, s);
			}
		} else if (controller.isPlaying(MS_PACMAN)) {
			FXRendering r = renderings.get(MS_PACMAN);
			SoundManager s = sounds.get(MS_PACMAN);
			switch (game.state) {
			case INTRO:
				return new MsPacMan_IntroScene(camera, controller, r, s);
			case INTERMISSION:
				if (game.intermissionNumber == 1) {
					return new MsPacMan_IntermissionScene1(camera, controller, r, s);
				}
				if (game.intermissionNumber == 2) {
					return new MsPacMan_IntermissionScene2(camera, controller, r, s);
				}
				if (game.intermissionNumber == 3) {
					return new MsPacMan_IntermissionScene3(camera, controller, r, s);
				}
				throw new IllegalStateException();
			default:
				return _3D ? new PlayScene3D(controller, height) : new PlayScene2D(camera, controller, r, s);
			}
		}
		throw new IllegalStateException();
	}

	// TODO this will not work if intermission scenes have both 2 and 3D versions, but it's ok for now
	public boolean are2DAnd3DVersionsAvailable(GameType gameType, PacManGameState state) {
		List<Class<? extends GameScene>> sceneClasses = gameType.equals(PACMAN) ? pacManSceneClasses.get(state)
				: msPacManSceneClasses.get(state);
		//@formatter:off
		return sceneClasses.stream().anyMatch(sceneClass -> GameScene2D.class.isAssignableFrom(sceneClass))
				&& sceneClasses.stream().anyMatch(sceneClass -> GameScene3D.class.isAssignableFrom(sceneClass));
		//@formatter:on
	}
}