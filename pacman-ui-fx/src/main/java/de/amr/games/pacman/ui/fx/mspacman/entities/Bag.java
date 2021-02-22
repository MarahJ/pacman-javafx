package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.rendering.MsPacMan_Rendering;
import javafx.scene.canvas.GraphicsContext;

/**
 * Blue bag dropped by the storch in intermission scene 3, contains Pac-Man junior.
 * 
 * @author Armin Reichert
 */
public class Bag extends GameEntity {

	final MsPacMan_Rendering rendering = PacManGameUI_JavaFX.RENDERING_MSPACMAN;

	public boolean released = false;
	public boolean open = false;
	public int bounces = 0;

	@Override
	public void move() {
		if (released) {
			velocity = velocity.sum(0, 0.04f); // gravity
		}
		super.move();
	}

	public void draw(GraphicsContext g) {
		if (open) {
			rendering.drawJunior(g, this);
		} else {
			rendering.drawBag(g, this);
		}
	}
}