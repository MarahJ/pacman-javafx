package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.ui.fx.scenes.common.CameraType;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

public class Env {

	public static SimpleBooleanProperty $measureTime = new SimpleBooleanProperty(false);
	public static SimpleBooleanProperty $paused = new SimpleBooleanProperty(false);
	public static SimpleIntegerProperty $slowdown = new SimpleIntegerProperty(1);
	public static SimpleBooleanProperty $infoViewVisible = new SimpleBooleanProperty(false);
	public static SimpleObjectProperty<DrawMode> $drawMode = new SimpleObjectProperty<DrawMode>(DrawMode.FILL);
	public static SimpleBooleanProperty $use3DScenes = new SimpleBooleanProperty(true);
	public static SimpleBooleanProperty $showAxes = new SimpleBooleanProperty(false);
	public static SimpleObjectProperty<CameraType> $cameraType = new SimpleObjectProperty<CameraType>(
			CameraType.MOVEABLE);
}