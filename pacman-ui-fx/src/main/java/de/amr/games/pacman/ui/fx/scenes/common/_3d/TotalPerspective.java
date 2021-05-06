package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class TotalPerspective implements PlayScenePerspective {

	private final Camera camera;

	public TotalPerspective(SubScene subScene) {
		camera = subScene.getCamera();
		ManualCameraController cameraController = new ManualCameraController(subScene.getCamera());
		subScene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
	}

	@Override
	public Camera camera() {
		return camera;
	}

	@Override
	public void reset() {
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);
		camera.setTranslateX(0);
		camera.setTranslateY(270);
		camera.setTranslateZ(-460);
	}

	@Override
	public void follow(Node target) {
	}

	@Override
	public String toString() {
		return "Total";
	}
}