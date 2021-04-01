package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.animation.TimedSequence;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Energizer2D extends GameEntity2D {

	private final V2i tile;
	private final TimedSequence<Boolean> blinkingAnimation;

	public Energizer2D(V2i tile) {
		this.tile = tile;
		blinkingAnimation = TimedSequence.pulse().frameDuration(15);
	}

	public TimedSequence<Boolean> getBlinkingAnimation() {
		return blinkingAnimation;
	}

	public void render(GraphicsContext g) {
		if (!blinkingAnimation.animate()) {
			g.setFill(Color.BLACK); // TODO could be other color
			g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
		}
	}
}