package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Creates 3D model from a world description.
 * 
 * @author Armin Reichert
 */
public class MazeBuilder {

	private static final int N = 4;
	private static final double BLOCKSIZE = (double) TS / N;

	public static V2i northOf(int tileX, int tileY, int i) {
		int dy = i / N == 0 ? -1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	public static V2i southOf(int tileX, int tileY, int i) {
		int dy = i / N == N - 1 ? 1 : 0;
		return new V2i(tileX, tileY + dy);
	}

	public static V2i westOf(int tileX, int tileY, int i) {
		int dx = i % N == 0 ? -1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	public static V2i eastOf(int tileX, int tileY, int i) {
		int dx = i % N == N - 1 ? 1 : 0;
		return new V2i(tileX + dx, tileY);
	}

	private int xSize;
	private int ySize;
	private byte[][] blocks;
	private PhongMaterial material;
	private List<Box> walls;
	private double wallSizeZ = BLOCKSIZE;

	public MazeBuilder() {
		material = new PhongMaterial();
	}

	public List<Box> getWalls() {
		return Collections.unmodifiableList(walls);
	}

	public void setWallSizeZ(double wallSizeZ) {
		this.wallSizeZ = wallSizeZ;
	}

	public void setWallMaterial(PhongMaterial material) {
		this.material = material;
	}

	public void build(PacManGameWorld world) {
		long start, end;
		double millis;
		walls = new ArrayList<>();

		start = System.nanoTime();
		scan(world);
		end = System.nanoTime();
		millis = (end - start) * 10e-6;
		log("Maze scanning took %.0f milliseconds", millis);

		start = System.nanoTime();
		createHorizontalWalls(world);
		createVerticalWalls(world);
		end = System.nanoTime();
		millis = (end - start) * 10e-6;
		log("Maze wall building took %.0f milliseconds", millis);
	}

	private void scan(PacManGameWorld world) {
		xSize = N * world.numCols();
		ySize = N * world.numRows();
		blocks = new byte[ySize][xSize];
		// find blocks inside walls
		for (int y = 0; y < ySize; ++y) {
			for (int x = 0; x < xSize; ++x) {
				V2i tile = new V2i(x / N, y / N);
				if (world.isWall(tile)) {
					int i = (y % N) * N + (x % N);
					blocks[y][x] = (byte) i;
				} else {
					blocks[y][x] = (byte) -1;
				}
			}
		}
		// clear blocks inside wall regions
		for (int y = 0; y < ySize; ++y) {
			int tileY = y / N;
			for (int x = 0; x < xSize; ++x) {
				int tileX = x / N;
				int i = (y % N) * N + (x % N);
				V2i n = northOf(tileX, tileY, i), e = eastOf(tileX, tileY, i), s = southOf(tileX, tileY, i),
						w = westOf(tileX, tileY, i);
				if (world.isWall(n) && world.isWall(e) && world.isWall(s) && world.isWall(w)) {
					V2i se = southOf(e.x, e.y, i);
					V2i sw = southOf(w.x, w.y, i);
					V2i ne = northOf(e.x, e.y, i);
					V2i nw = northOf(w.x, w.y, i);
					if (world.isWall(se) && !world.isWall(nw) || !world.isWall(se) && world.isWall(nw)
							|| world.isWall(sw) && !world.isWall(ne) || !world.isWall(sw) && world.isWall(ne)) {
						// keep corner of wall region
					} else {
						blocks[y][x] = -1;
					}
				}
			}
		}
	}

	private void addWall(int leftX, int topY, int numBlocksX, int numBlocksY) {
		if (numBlocksX == 1 && numBlocksY == 1) {
			return; // ignore 1x1 walls because they could be part of a larger wall in other orientation
		}
		Box wall = createWall(leftX, topY, material, numBlocksX, numBlocksY);
		walls.add(wall);
 	}

	private Box createWall(int leftX, int topY, PhongMaterial material, int numBlocksX, int numBlocksY) {
		Box wall = new Box(numBlocksX * BLOCKSIZE, numBlocksY * BLOCKSIZE, wallSizeZ);
		wall.setMaterial(material);
		wall.setTranslateX(leftX * BLOCKSIZE + (numBlocksX - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateY(topY * BLOCKSIZE + (numBlocksY - 4) * 0.5 * BLOCKSIZE);
		wall.setTranslateZ(1.5);
		wall.drawModeProperty().bind(Env.$drawMode);
		return wall;
	}

	private void createHorizontalWalls(PacManGameWorld world) {
		int xSize = blocks[0].length, ySize = blocks.length;
		for (int y = 0; y < ySize; ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < xSize; ++x) {
				byte cell = blocks[y][x];
				if (cell != -1) {
					if (leftX == -1) {
						leftX = x;
						sizeX = 1;
					} else {
						sizeX++;
					}
				} else {
					if (leftX != -1) {
						addWall(leftX, y, sizeX, 1);
						leftX = -1;
					}
				}
				if (x == xSize - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1);
					leftX = -1;
				}
			}
			if (y == ySize - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1);
				leftX = -1;
			}
		}
	}

	private void createVerticalWalls(PacManGameWorld world) {
		int xSize = blocks[0].length, ySize = blocks.length;
		for (int x = 0; x < xSize; ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < ySize; ++y) {
				byte cell = blocks[y][x];
				if (cell != -1) {
					if (topY == -1) {
						topY = y;
						sizeY = 1;
					} else {
						sizeY++;
					}
				} else {
					if (topY != -1) {
						addWall(x, topY, 1, sizeY);
						topY = -1;
					}
				}
				if (y == ySize - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY);
					topY = -1;
				}
			}
			if (x == xSize - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY);
				topY = -1;
			}
		}
	}
}