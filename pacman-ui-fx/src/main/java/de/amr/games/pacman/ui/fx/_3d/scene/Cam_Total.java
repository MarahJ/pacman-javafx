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
package de.amr.games.pacman.ui.fx._3d.scene;

import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx.util.ManualCameraController;
import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Shows a total of the complete game. Using the keyboard, can zoom in and out
 * and change angle.
 * 
 * @author Armin Reichert
 */
public class Cam_Total extends ManualCameraController implements PlaySceneCam {

	public Cam_Total(Camera cam) {
		super(cam);
	}

	@Override
	public void handle(KeyEvent event) {
		super.handleKeyEvent(event);
	}

	@Override
	public void reset() {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(30);
		cam.setTranslateX(0);
		cam.setTranslateY(270);
		cam.setTranslateZ(-460);
	}

	@Override
	public void follow(Player3D player3D) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}