package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSound;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class IntermissionScene1 extends AbstractPacManGameScene<PacManSceneRendering> {

	enum Phase {
		BLINKY_CHASING_PACMAN, BIGPACMAN_CHASING_BLINKY;
	}

	private final int baselineY = t(20);
	private final Ghost blinky;
	private final Pac pac;
	private final Animation<Rectangle2D> bigPac;
	private Phase phase;

	public IntermissionScene1(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling) {
		super(game, soundManager, width, height, scaling);
		rendering = new PacManSceneRendering(g);
		blinky = new Ghost(0, "Blinky", Direction.LEFT);
		pac = new Pac("Pac-Man", Direction.LEFT);
		bigPac = Animation.of(tileRegion(2, 1, 2, 2), tileRegion(4, 1, 2, 2), tileRegion(6, 1, 2, 2));
		bigPac.endless().frameDuration(4).run();
	}

	@Override
	public void start() {
		log("Start of intermission scene %s at tick %d", this, clock.ticksTotal);

		pac.visible = true;
		pac.dead = false;
		pac.couldMove = true;
		pac.position = new V2f(t(30), baselineY);
		pac.speed = 1;
		pac.dir = LEFT;

		blinky.visible = true;
		blinky.state = HUNTING_PAC;
		blinky.position = pac.position.sum(t(4), 0);
		blinky.speed = pac.speed * 1.04f;
		blinky.dir = blinky.wishDir = LEFT;

		rendering.pacMunching(pac).forEach(Animation::restart);
		rendering.ghostKickingToDir(blinky, blinky.dir).restart();
		rendering.ghostFrightenedToDir(blinky, blinky.dir).restart();
		soundManager.loop(PacManGameSound.INTERMISSION_1, 2);

		phase = Phase.BLINKY_CHASING_PACMAN;
	}

	@Override
	public void update() {
		switch (phase) {
		case BLINKY_CHASING_PACMAN:
			if (pac.position.x < -50) {
				pac.dir = RIGHT;
				pac.position = new V2f(-20, baselineY);
				pac.speed = 0;
				blinky.dir = blinky.wishDir = RIGHT;
				blinky.position = new V2f(-20, baselineY);
				blinky.speed = 0.8f;
				blinky.state = FRIGHTENED;
				phase = Phase.BIGPACMAN_CHASING_BLINKY;
			}
			break;
		case BIGPACMAN_CHASING_BLINKY:
			if ((int) blinky.position.x + 4 == t(14)) {
				pac.speed = blinky.speed * 1.9f;
			}
			if (pac.position.x > t(28) + 100) {
				game.state.duration(0);
			}
			break;
		default:
			break;
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		rendering.drawGhost(blinky, game);
		if (phase == Phase.BLINKY_CHASING_PACMAN) {
			rendering.drawPac(pac, game);
		} else {
			rendering.drawRegion(bigPac.animate(), pac.position.x - 12, pac.position.y - 22);
		}
		rendering.drawLevelCounter(game, t(25), t(34));
	}
}