package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.rendering.MsPacMan_Rendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	final MsPacMan_Rendering rendering = PacManGameUI_JavaFX.RENDERING_MSPACMAN;
	final Font font = Font.font(rendering.getScoreFont().getName(), FontWeight.THIN, 8);
	public final Animation<Rectangle2D> animation = Animation.of( //
			new Rectangle2D(456, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(520, 208, 32, 32), //
			new Rectangle2D(488, 208, 32, 32), //
			new Rectangle2D(456, 208, 32, 32)//
	).repetitions(1).frameDuration(4);

	final int sceneNumber;
	final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
	}

	public void draw(GraphicsContext g) {
		if (visible) {
			rendering.drawSprite(g, animation.animate(), position.x, position.y);
			g.setFont(font);
			g.setFill(Color.rgb(222, 222, 225, 0.75));
			g.fillText(sceneNumber + "", position.x + 20, position.y + 30);
			g.setFont(rendering.getScoreFont());
			g.fillText(sceneTitle, position.x + 40, position.y + 20);
		}
	}
}