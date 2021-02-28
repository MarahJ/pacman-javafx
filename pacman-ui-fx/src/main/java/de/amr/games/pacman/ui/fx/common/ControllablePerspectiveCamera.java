package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;

/**
 * Perspective camera that an be controlled using keys.
 * 
 * @author Armin Reichert
 */
public class ControllablePerspectiveCamera extends PerspectiveCamera {

	public StringProperty infoProperty = new SimpleStringProperty();

	public void onKeyPressed(KeyEvent e) {
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				setTranslateX(0);
				setTranslateY(0);
				setTranslateZ(0);
				break;
			case LEFT:
				setTranslateX(getTranslateX() + 10);
				log("Cam moves LEFT");
				break;
			case RIGHT:
				setTranslateX(getTranslateX() - 10);
				log("Cam moves RIGHT");
				break;
			case UP:
				setTranslateY(getTranslateY() + 10);
				log("Cam moves UP");
				break;
			case DOWN:
				setTranslateY(getTranslateY() - 10);
				log("Cam moves DOWN");
				break;
			case PLUS:
				setTranslateZ(getTranslateZ() + 10);
				log("Cam zoomes IN");
				break;
			case MINUS:
				setTranslateZ(getTranslateZ() - 10);
				log("Cam zoomes OUT");
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				setRotate(getRotate() - 1);
				log("Cam rotates FORWARD");
				break;
			case UP:
				setRotate(getRotate() + 1);
				log("Cam rotates BACKWARDS");
				break;
			default:
				break;
			}
		}
		infoProperty.setValue(getInfo());
	}

	public String getInfo() {
		return String.format("Camera\nx:%3.0f y:%3.0f z:%3.0f rot:%3.0f", getTranslateX(), getTranslateY(), getTranslateZ(),
				getRotate());
	}
}