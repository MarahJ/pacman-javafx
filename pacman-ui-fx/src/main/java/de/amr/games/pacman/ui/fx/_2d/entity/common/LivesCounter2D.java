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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 2D lives counter.
 * 
 * @author Armin Reichert
 */
public class LivesCounter2D implements Renderable2D {

	private final Rendering2D rendering;
	private final int maxLivesDisplayed = 5;

	public int x;
	public int y;
	public int lives;

	public LivesCounter2D(Rendering2D rendering) {
		this.rendering = rendering;
	}

	@Override
	public void render(GraphicsContext g) {
		var sprite = rendering.getLifeSprite();
		for (int i = 0; i < Math.min(lives, maxLivesDisplayed); ++i) {
			rendering.renderSprite(g, sprite, x + t(2 * i), y);
		}
		if (lives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (lives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}
}