package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.function.Supplier;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.rendering.GameRendering3D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * 3D ghost shape.
 * 
 * @author Armin Reichert
 */
public class Ghost3D implements Supplier<Node> {

	private final Ghost ghost;
	private Group root;
	private MeshView meshView;
	private Text bountyText;
	private Group pearlChain;

	public Ghost3D(Ghost ghost) {
		this.ghost = ghost;
		createMeshView();
		createBountyText();
		createPearlChain();
		root = new Group(meshView, bountyText, pearlChain);
		displayColored(GameRendering3D_Assets.ghostColor(ghost.id));
	}

	private void createMeshView() {
		meshView = GameRendering3D_Assets.createGhostMeshView(ghost.id);
		meshView.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
	}

	private void createBountyText() {
		bountyText = new Text();
		bountyText.setText(String.valueOf(ghost.bounty));
		bountyText.setFont(Font.font("Sans", FontWeight.MEDIUM, 8));
		bountyText.setFill(Color.CYAN);
		bountyText.setTranslateZ(-1.5 * TS);
	}

	private void createPearlChain() {
		PhongMaterial skin = GameRendering3D_Assets.ghostSkin(ghost.id);
		Sphere[] pearls = new Sphere[3];
		for (int i = 0; i < pearls.length; ++i) {
			pearls[i] = new Sphere(1);
			pearls[i].setMaterial(skin);
			pearls[i].setTranslateX(i * 3);
		}
		pearlChain = new Group(pearls);
		pearlChain.getTransforms().add(new Translate(-3, 0, 0));
	}

	@Override
	public Node get() {
		return root;
	}

	private void selectChild(int index) {
		for (int i = 0; i < 3; ++i) {
			root.getChildren().get(i).setVisible(i == index);
		}
	}

	private void setMeshColor(Color color) {
		PhongMaterial material = new PhongMaterial(color);
		meshView.setMaterial(material);
	}

	public void update() {
		if (ghost.bounty > 0) {
			displayAsBounty();
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			displayReturningHome();
			updateTransforms();
		} else if (ghost.is(GhostState.FRIGHTENED)) {
			displayColored(Color.CORNFLOWERBLUE);
			updateTransforms();
		} else {
			displayColored(GameRendering3D_Assets.ghostColor(ghost.id));
			updateTransforms();
		}
	}

	public void displayColored(Color color) {
		setMeshColor(color);
		selectChild(0);
	}

	public void displayAsBounty() {
		bountyText.setText("" + ghost.bounty);
		selectChild(1);
	}

	public void displayReturningHome() {
		pearlChain.setRotationAxis(Rotate.Z_AXIS);
		pearlChain.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		selectChild(2);
	}

	private void updateTransforms() {
		root.setVisible(ghost.visible);
		root.setTranslateX(ghost.position.x);
		root.setTranslateY(ghost.position.y);
		root.setViewOrder(-(ghost.position.y + 5));
		root.setRotationAxis(Rotate.Y_AXIS);
		root.setRotate(0);
		switch (ghost.dir) {
		case LEFT:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(180);
			break;
		case RIGHT:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(0);
			break;
		case UP:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(-90);
			break;
		case DOWN:
			root.setRotationAxis(Rotate.Z_AXIS);
			root.setRotate(90);
			break;
		default:
			break;
		}
	}
}