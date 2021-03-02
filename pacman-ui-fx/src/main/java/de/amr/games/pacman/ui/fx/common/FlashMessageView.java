package de.amr.games.pacman.ui.fx.common;

import java.util.ArrayDeque;
import java.util.Deque;

import de.amr.games.pacman.ui.FlashMessage;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class FlashMessageView extends HBox {

	private final Deque<FlashMessage> messagesQ = new ArrayDeque<>();
	private final Text display = new Text();
	private Color displayColor;

	public FlashMessageView() {
		displayColor = Color.WHEAT;
		display.setFont(Font.font("Sans", FontWeight.BOLD, 24));
		setAlignment(Pos.CENTER);
		getChildren().add(display);
	}

	public void showMessage(String message, long ticks) {
		messagesQ.add(new FlashMessage(message, ticks));
	}

	public void update() {
		if (messagesQ.isEmpty()) {
			setVisible(false);
		} else {
			setVisible(true);
			FlashMessage message = messagesQ.peek();
			if (message.timer.expired()) {
				messagesQ.remove();
				return;
			}
			double alpha = Math.cos((message.timer.running() * Math.PI / 2.0) / message.timer.getDuration());
			display.setFill(displayColor.deriveColor(0, 1, 1, alpha));
			display.setText(message.text);
			message.timer.run();
		}
	}
}