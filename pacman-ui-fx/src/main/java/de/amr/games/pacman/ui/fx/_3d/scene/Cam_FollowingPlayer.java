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
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;

/**
 * Follows the player but still shows a larger portion of the maze.
 * 
 * @author Armin Reichert
 */
public class Cam_FollowingPlayer implements PlaySceneCam {

	private final Camera cam;

	public Cam_FollowingPlayer(Camera cam) {
		this.cam = cam;
	}

	@Override
	public void reset() {
		cam.setNearClip(0.1);
		cam.setFarClip(10000.0);
		cam.setRotationAxis(Rotate.X_AXIS);
		cam.setRotate(30);
		cam.setTranslateZ(-250);
	}

	@Override
	public void follow(Player3D player3D) {
		cam.setTranslateX(Math.min(10, approach(cam.getTranslateX(), player3D.getTranslateX())));
		cam.setTranslateY(Math.max(50, approach(cam.getTranslateY(), player3D.getTranslateY())));
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}