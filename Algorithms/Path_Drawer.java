
// specify required imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Stack;

public final class Path_Drawer {

	// declare variables like x and y coordinates of robot, the path etc
	static Stack<Node> path;
	static Stack<int[]> stackUnexplored;
	static int robotX;
	static int robotY;

	// default constructor for the class
	Path_Drawer() {
	}

	// update class variables
	public static void update(int robotx, int roboty, Stack<Node> inputPath) {
		robotX = robotx;
		robotY = roboty;
		path = inputPath;
	}

	// update unexplored areas
	public static void updateUnexplored(Stack<int[]> InUnexploredAreas) {
		stackUnexplored = InUnexploredAreas;
	}

	// remove path after drawing
	public static void removePath() {
		path.removeAllElements();
	}

	// method to draw the grid for the robot_simulator
	public static void gridDraw(Graphics graphs) {

		// if no unexplored area, stop further processing and return
		if (stackUnexplored == null || stackUnexplored.empty())
			return;

		// set color to cyan
		graphs.setColor(Color.RED);

		// draw the the grids
		for (int i = 0; i < stackUnexplored.size(); i++) {
			graphs.fillRect(10 + (stackUnexplored.get(i)[0]) * Map.sizeofsquare,
					10 + (stackUnexplored.get(i)[1]) * Map.sizeofsquare, 38, 38);
		}
	}

	// method to draw path based on graphs specifications obtained
	public static void draw(Graphics graphs) {

		// set the stroke size
		Graphics2D g = (Graphics2D) graphs;
		g.setStroke(new BasicStroke(10));

		// set color to cyan
		graphs.setColor(Color.CYAN);

		// draw the first line from the robot to the first node
		graphs.drawLine(30 + robotX * Map.sizeofsquare, 30 + robotY * Map.sizeofsquare,
				30 + path.get(path.size() - 1).getX() * Map.sizeofsquare,
				30 + path.get(path.size() - 1).getY() * Map.sizeofsquare);

		// draw the rest of the lines
		for (int i = 0; i < path.size() - 1; i++) {
			graphs.drawLine(30 + path.get(i).getX() * Map.sizeofsquare, 30 + path.get(i).getY() * Map.sizeofsquare,
					30 + path.get(i + 1).getX() * Map.sizeofsquare, 30 + path.get(i + 1).getY() * Map.sizeofsquare);
		}
	}
}