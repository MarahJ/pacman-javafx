package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.Rendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Common interface for the scene renderings.
 * 
 * @author Armin Reichert
 */
public abstract class DefaultRendering implements Rendering<GraphicsContext, Color>, PacManGameAnimations {

	/** Spritesheet raster size */
	public static final int RASTER = 16;

	public static Rectangle2D tileRegion(int tileX, int tileY) {
		return tileRegion(tileX, tileY, 1, 1);
	}

	public static Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * RASTER, tileY * RASTER, cols * RASTER, rows * RASTER);
	}

	protected Image spritesheet;
	protected Font scoreFont;
	protected List<Animation<Image>> mazeFlashingAnim;
	protected Animation<Boolean> energizerBlinking;
	protected List<Rectangle2D> symbolRegions;
	protected Map<Integer, Rectangle2D> bonusValueRegions;
	protected Map<Integer, Rectangle2D> bountyValueRegions;
	protected Map<Direction, Animation<Rectangle2D>> pacManMunchingAnim;
	protected Animation<Rectangle2D> pacDyingAnim;
	protected List<Map<Direction, Animation<Rectangle2D>>> ghostsKickingAnim;
	protected Map<Direction, Animation<Rectangle2D>> ghostEyesAnim;
	protected Animation<Rectangle2D> ghostBlueAnim;
	protected Animation<Rectangle2D> ghostFlashingAnim;

	public DefaultRendering(String spritesheetURL) {
		spritesheet = new Image(spritesheetURL);
		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);
		energizerBlinking = Animation.pulse().frameDuration(15);
	}

	/* Tile region relative to given origin */
	public Rectangle2D tileRegionAt(int originX, int originY, int tileX, int tileY, int tilesWidth, int tilesHeight) {
		return new Rectangle2D(originX + tileX * RASTER, originY + tileY * RASTER, tilesWidth * RASTER,
				tilesHeight * RASTER);
	}

	protected int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	protected Direction ensureDirection(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	public Rectangle2D bonusSpriteRegion(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolRegions.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValueRegions.get(bonus.points);
		}
		return null;
	}

	public Rectangle2D pacSpriteRegion(Pac pac) {
		if (pac.dead) {
			return playerDying().hasStarted() ? playerDying().animate() : (Rectangle2D) playerMunching(pac, pac.dir).frame();
		}
		if (pac.speed == 0) {
			return (Rectangle2D) playerMunching(pac, pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return (Rectangle2D) playerMunching(pac, pac.dir).frame(1);
		}
		return (Rectangle2D) playerMunching(pac, pac.dir).animate();
	}

	public Rectangle2D ghostSpriteRegion(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return bountyValueRegions.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		// Looks towards wish dir!
		return ghostKicking(ghost, ghost.wishDir).animate();
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	public void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		g.drawImage(spritesheet, r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(), r.getHeight());
	}

	/**
	 * Draws a guy centered over his collision box.
	 * 
	 * @param g      the graphics context
	 * @param guy    the guy
	 * @param region sprite region in spritsheet
	 */
	protected void drawGuy(GraphicsContext g, GameEntity guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - region.getWidth() / 2 + 4, guy.position.y - region.getHeight() / 2 + 4, region.getWidth(),
					region.getHeight());
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			drawLifeCounterSymbol(g, x + t(2 * i), y);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (game.lives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}

	@Override
	public void drawPlayer(GraphicsContext g, Pac pac) {
		drawGuy(g, pac, pacSpriteRegion(pac));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened) {
		drawGuy(g, ghost, ghostSpriteRegion(ghost, frightened));
	}

	@Override
	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	@Override
	public void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	@Override
	public void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles) {
		if (energizerBlinking.animate()) {
			energizerTiles.forEach(tile -> drawTileCovered(g, tile));
		}
	}

	@Override
	public void drawGameState(GraphicsContext g, GameModel game) {
		if (game.state == PacManGameState.GAME_OVER || game.attractMode) {
			g.setFont(scoreFont);
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (game.state == PacManGameState.READY) {
			g.setFont(scoreFont);
			g.setFill(Color.YELLOW);
			g.fillText("READY", t(11), t(21));
		}
	}

	@Override
	public void drawScore(GraphicsContext g, GameModel game, boolean titleOnly) {
		g.setFont(getScoreFont());
		g.translate(0, 2);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGH SCORE", t(15), t(1));
		g.translate(0, 1);
		if (!titleOnly) {
			Color pointsColor = getMazeWallColor(game.level.mazeNumber - 1);
			if (pointsColor == Color.BLACK) {
				pointsColor = Color.YELLOW;
			}
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -3);
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbolRegions.get(game.levelSymbols.get(level - 1));
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
					region.getWidth(), region.getHeight());
			x -= t(2);
		}
	}

	// Animations

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
		return mazeFlashingAnim.get(mazeNumber - 1);
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return mazeFlashingAnim.stream().map(Animation.class::cast);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}

	@Override
	public Animation<Rectangle2D> playerDying() {
		return pacDyingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostKicking(Ghost ghost, Direction dir) {
		return ghostsKickingAnim.get(ghost.id).get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightened(Ghost ghost, Direction dir) {
		return ghostBlueAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyesAnim.get(ensureDirection(dir));
	}
}