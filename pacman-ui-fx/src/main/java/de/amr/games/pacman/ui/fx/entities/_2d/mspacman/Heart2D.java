package de.amr.games.pacman.ui.fx.entities._2d.mspacman;

import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.ui.fx.entities._2d.Renderable2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_MsPacMan;
import javafx.scene.canvas.GraphicsContext;

/**
 * The heart displayed in Ms. Pac-Man intermission scene 1.
 * 
 * @author Armin Reichert
 */
public class Heart2D extends Renderable2D<GameRendering2D_MsPacMan> {

	private final GameEntity heart;

	public Heart2D(GameEntity heart, GameRendering2D_MsPacMan rendering) {
		super(rendering);
		this.heart = heart;
	}

	@Override
	public void render(GraphicsContext gc) {
		renderEntity(gc, heart, rendering.getHeart());
	}
}