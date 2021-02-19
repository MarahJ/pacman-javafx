package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends GameScene {

	static class GhostPortrait {

		Ghost ghost;
		String character;
		boolean characterVisible;
		boolean nicknameVisible;
		Color color;
	}

	enum Phase {

		BEGIN, GHOST_GALLERY, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY;

		final CountdownTimer timer = new CountdownTimer();
	}

	private final PacMan_SceneRendering rendering = PacManGameFXUI.PACMAN_RENDERING;
	private final Animation<Boolean> blinking = Animation.pulse().frameDuration(20).restart();

	private final int topY = t(6);

	private GhostPortrait[] gallery;
	private int presentedGhostIndex;
	private long ghostKilledTime;
	private Pac pac;
	private Ghost[] ghosts;

	private Phase phase;

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phase.timer.setDuration(Long.MAX_VALUE);
	}

	public PacMan_IntroScene(Group root, double width, double height, double scaling) {
		super(root, width, height, scaling);
	}

	@Override
	public void start() {
		gallery = new GhostPortrait[4];
		for (int i = 0; i < 4; ++i) {
			gallery[i] = new GhostPortrait();
		}
		gallery[0].ghost = new Ghost(0, "Blinky", Direction.RIGHT);
		gallery[0].character = "SHADOW";
		gallery[0].color = Color.RED;

		gallery[1].ghost = new Ghost(1, "Pinky", Direction.RIGHT);
		gallery[1].character = "SPEEDY";
		gallery[1].color = Color.PINK;

		gallery[2].ghost = new Ghost(2, "Inky", Direction.RIGHT);
		gallery[2].character = "BASHFUL";
		gallery[2].color = Color.CYAN;

		gallery[3].ghost = new Ghost(3, "Clyde", Direction.RIGHT);
		gallery[3].character = "POKEY";
		gallery[3].color = Color.ORANGE;

		pac = new Pac("Ms. Pac-Man", Direction.LEFT);

		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.LEFT), //
				new Ghost(1, "Pinky", Direction.LEFT), //
				new Ghost(2, "Inky", Direction.LEFT), //
				new Ghost(3, "Clyde", Direction.LEFT), //
		};

		enterPhase(Phase.BEGIN);
	}

	@Override
	public void update() {
		pac.move();
		for (Ghost ghost : ghosts) {
			ghost.move();
		}
		switch (phase) {
		case BEGIN:
			if (phase.timer.running() == clock.sec(2)) {
				presentGhost(0);
				enterPhase(Phase.GHOST_GALLERY);
			}
			break;
		case GHOST_GALLERY:
			if (phase.timer.running() == clock.sec(0.5)) {
				gallery[presentedGhostIndex].characterVisible = true;
			}
			if (phase.timer.running() == clock.sec(1)) {
				gallery[presentedGhostIndex].nicknameVisible = true;
			}
			if (phase.timer.running() == clock.sec(2)) {
				if (presentedGhostIndex < 3) {
					presentGhost(presentedGhostIndex + 1);
					enterPhase(Phase.GHOST_GALLERY);
				} else {
					startGhostsChasingPac();
					enterPhase(Phase.CHASING_PAC);
				}
			}
			break;
		case CHASING_PAC:
			if (pac.position.x < t(2)) {
				startPacChasingGhosts();
				enterPhase(Phase.CHASING_GHOSTS);
			}
			break;
		case CHASING_GHOSTS:
			if (pac.position.x > t(28)) {
				enterPhase(Phase.READY_TO_PLAY);
			}
			if (clock.ticksTotal - ghostKilledTime == clock.sec(0.25)) {
				ghostKilledTime = 0;
				pac.visible = true;
				pac.speed = 1;
				for (Ghost ghost : ghosts) {
					if (ghost.state == GhostState.DEAD) {
						ghost.visible = false;
					}
				}
			}
			for (Ghost ghost : ghosts) {
				if (pac.meets(ghost) && ghost.state != GhostState.DEAD) {
					ghost.state = GhostState.DEAD;
					ghost.bounty = (int) Math.pow(2, ghost.id + 1) * 100;
					pac.visible = false;
					pac.speed = 0;
					ghostKilledTime = clock.ticksTotal;
				}
			}
			break;
		case READY_TO_PLAY:
			blinking.animate();
			if (phase.timer.running() == clock.sec(5)) {
				game.attractMode = true;
				log("Entering attract mode at %d", clock.ticksTotal);
			}
			break;
		default:
			break;
		}
		phase.timer.run();
	}

	private void startGhostsChasingPac() {
		pac.position = new V2f(t(28), t(22));
		pac.visible = true;
		pac.speed = 1;
		pac.dir = Direction.LEFT;
		pac.couldMove = true;
		rendering.pacMunching(pac).forEach(Animation::restart);

		for (Ghost ghost : ghosts) {
			ghost.position = pac.position.sum(8 + (ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.dir = ghost.wishDir = Direction.LEFT;
			ghost.speed = pac.speed * 1.05f;
			ghost.state = GhostState.HUNTING_PAC;
			rendering.ghostsKicking(Stream.of(ghosts)).forEach(Animation::restart);
		}
	}

	private void startPacChasingGhosts() {
		pac.dir = Direction.RIGHT;
		for (Ghost ghost : ghosts) {
			ghost.dir = ghost.wishDir = Direction.RIGHT;
			ghost.speed = 0.5f;
		}
	}

	@Override
	public void render() {
		clear();
		rendering.drawScore(g, game, true);
		drawGallery();
		if (phase == Phase.CHASING_PAC) {
			if (blinking.animate()) {
				g.setFill(Color.PINK);
				g.fillOval(t(2), pac.position.y, TS, TS);
			}
		}
		drawGuys();
		if (phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(11, 26);
		}
		if (phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(32);
		}
	}

	private void drawGuys() {
		rendering.drawPac(g, pac);
		for (Ghost ghost : ghosts) {
			rendering.drawGhost(g, ghost, pac.powerTicksLeft > 0);
		}
	}

	private void presentGhost(int ghostIndex) {
		presentedGhostIndex = ghostIndex;
		gallery[presentedGhostIndex].ghost.visible = true;
	}

	private void drawGallery() {
		int x = t(2);
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("CHARACTER", t(6), topY);
		g.fillText("/", t(16), topY);
		g.fillText("NICKNAME", t(18), topY);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = gallery[i];
			if (portrait.ghost.visible) {
				int y = topY + t(2 + 3 * i);
				Rectangle2D ghostTile = rendering.ghostKickingToDir(portrait.ghost, Direction.RIGHT).frame(0);
				rendering.drawRegion(g, rendering.toRegion(ghostTile), x, y - 4);
				g.setFill(portrait.color);
				g.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					g.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					g.fillText("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private void drawPressKeyToStart(int yTile) {
		if (blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(int tileX, int tileY) {
		if (blinking.frame()) {
			g.setFill(Color.PINK);
			g.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			g.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("10", t(tileX + 2), t(tileY));
		g.fillText("50", t(tileX + 2), t(tileY + 2));
		g.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		g.fillText("PTS", t(tileX + 5), t(tileY));
		g.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}