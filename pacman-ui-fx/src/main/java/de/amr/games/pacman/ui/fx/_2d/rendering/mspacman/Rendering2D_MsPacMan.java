/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.rendering.mspacman;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Ms. Pac-Man game-specific rendering.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_MsPacMan extends Rendering2D {

	private static final Color[] MAZE_TOP_COLOR = { //
			Color.rgb(255, 183, 174), //
			Color.rgb(71, 183, 255), //
			Color.rgb(222, 151, 81), //
			Color.rgb(33, 33, 255), //
			Color.rgb(255, 183, 255), //
			Color.rgb(255, 183, 174), //
	};

	private static final Color[] MAZE_SIDE_COLOR = { //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 183, 81), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0), //
	};

	private static final Color[] FOOD_COLOR = { //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(0, 255, 255), //
			Color.rgb(222, 222, 255), //
	};

	private final List<Rectangle2D> mazeFullSprites;
	private final List<Rectangle2D> mazeEmptySprites;
	private final List<Image> mazeFlashImages;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_MsPacMan() {
		super("/mspacman/graphics/sprites.png", 16);

		//@formatter:off
		symbolSprites = Map.of(
			MsPacManGame.CHERRIES,   regionRight(3,0),
			MsPacManGame.STRAWBERRY, regionRight(4,0),
			MsPacManGame.PEACH,      regionRight(5,0),
			MsPacManGame.PRETZEL,    regionRight(6,0),
			MsPacManGame.APPLE,      regionRight(7,0),
			MsPacManGame.PEAR,       regionRight(8,0),
			MsPacManGame.BANANA,     regionRight(9,0)
		);

		bonusValueSprites = Map.of(
			 100, regionRight(3, 1), 
			 200, regionRight(4, 1), 
			 500, regionRight(5, 1), 
			 700, regionRight(6, 1), 
			1000, regionRight(7, 1), 
			2000, regionRight(8, 1),
			5000, regionRight(9, 1)
		);

		bountyNumberSprites = Map.of(
			 200, regionRight(0, 8), 
			 400, regionRight(1, 8), 
			 800, regionRight(2, 8), 
			1600, regionRight(3, 8)
		);
		//@formatter:on

		mazeFullSprites = new ArrayList<>(6);
		mazeEmptySprites = new ArrayList<>(6);
		mazeFlashImages = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Rectangle2D mazeFullRegion = new Rectangle2D(0, 248 * mazeIndex, 226, 248);
			Rectangle2D mazeEmptyRegion = new Rectangle2D(226, 248 * mazeIndex, 226, 248);
			// TODO can we avoid copying image data?
			Image mazeFlashImage = colorsExchanged(createSubImage(mazeEmptyRegion), //
					Map.of( //
							getMazeSideColor(mazeIndex + 1), Color.WHITE, //
							getMazeTopColor(mazeIndex + 1), Color.BLACK));
			mazeFullSprites.add(mazeFullRegion);
			mazeEmptySprites.add(mazeEmptyRegion);
			mazeFlashImages.add(mazeFlashImage);
		}
	}

	@Override
	public Color getMazeTopColor(int mazeNumber) {
		return MAZE_TOP_COLOR[mazeNumber - 1];
	}

	@Override
	public Color getMazeSideColor(int mazeNumber) {
		return MAZE_SIDE_COLOR[mazeNumber - 1];
	}

	@Override
	public void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y) {
		renderSprite(g, mazeFullSprites.get(mazeNumber - 1), x, y);
	}

	@Override
	public void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y) {
		renderSprite(g, mazeEmptySprites.get(mazeNumber - 1), x, y);
	}

	@Override
	public void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFlashImages.get(mazeNumber - 1), x, y);
	}

	@Override
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLOR[mazeNumber - 1];
	}

	@Override
	public Map<Integer, Rectangle2D> getBonusValuesSprites() {
		return bonusValueSprites;
	}

	@Override
	public Map<Integer, Rectangle2D> getBountyNumberSprites() {
		return bountyNumberSprites;
	}

	@Override
	public Map<String, Rectangle2D> getSymbolSprites() {
		return symbolSprites;
	}

	/*
	 * Animations.
	 */

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> munchingAnimations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			TimedSequence<Rectangle2D> munching = TimedSequence
					.of(regionRight(0, d), regionRight(0, d), regionRight(1, d), regionRight(2, d)).frameDuration(2).endless();
			munchingAnimations.put(dir, munching);
		}
		return munchingAnimations;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(regionRight(0, 3), regionRight(0, 0), regionRight(0, 1), regionRight(0, 2))
				.frameDuration(10).repetitions(2);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> kickingAnimations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			TimedSequence<Rectangle2D> kicking = TimedSequence
					.of(regionRight(2 * d, 4 + ghostID), regionRight(2 * d + 1, 4 + ghostID)).frameDuration(4).endless();
			kickingAnimations.put(dir, kicking);
		}
		return kickingAnimations;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(regionRight(8, 4), regionRight(9, 4)).frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(regionRight(8, 4), regionRight(9, 4), regionRight(10, 4), regionRight(11, 4))
				.frameDuration(4);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> eyesAnimation = new EnumMap<>(Direction.class);
		Direction.stream().forEach(dir -> eyesAnimation.put(dir, TimedSequence.of(regionRight(8 + dirIndex(dir), 5))));
		return eyesAnimation;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> createSpouseMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			pacManMunchingAnim.put(dir,
					TimedSequence.of(regionRight(0, 9 + d), regionRight(1, 9 + d), regionRight(2, 9)).frameDuration(2).endless());
		}
		return pacManMunchingAnim;
	}

	public TimedSequence<Rectangle2D> createFlapAnimation() {
		return TimedSequence.of( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	public TimedSequence<Rectangle2D> createStorkFlyingAnimation() {
		return TimedSequence.of(new Rectangle2D(489, 176, 32, 16), new Rectangle2D(521, 176, 32, 16)).endless()
				.frameDuration(10);
	}

	public TimedSequence<Integer> createBonusAnimation() {
		return TimedSequence.of(2, -2).frameDuration(20).endless();
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return regionRight(1, 0);
	}

	public Rectangle2D getHeart() {
		return regionRight(2, 10);
	}

	public Rectangle2D getJunior() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	public Rectangle2D getBlueBag() {
		return new Rectangle2D(488, 199, 8, 8);
	}

	private Rectangle2D regionRight(int col, int row) {
		return region(456, 0, col, row, 1, 1);
	}
}