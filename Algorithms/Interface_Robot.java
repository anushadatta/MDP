
// specify required imports
import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public abstract class Interface_Robot {

	Viz viz;
	// the x and y coorsinates of the robot
	int x;
	int y;

	// the x and y coordinates to draw the robot on screen
	int x_g, y_g;
	// radius of the circle to draw the robot
	final static int radius = 90;

	// the direction the robot is face_dir
	Direction face_dir;

	Map map;
	final int map_width = 15;
	final int map_height = 20;

	// the fastest path between 2 points
	Stack<Node> fastestPath = new Stack<Node>();
	// the converted int instructions from the above stack array
	Stack<Integer> instructionsForFastestPath = new Stack<Integer>();

	// define abstract functions to implement later
	// use to provide only function signatures
	public abstract void addSensors(Real_Sensor[] sensors);

	public abstract void addSensors(Sensor[] sensors);

	public abstract void surroundingSense();

	public abstract void SenseRobotLocation();

	public abstract void moveInstructionRobot();// move robot forward

	public abstract void leftTurn();

	public abstract void rightTurn();

	public abstract boolean retrieve_fastest_instruction(Stack<Node> fast);

	public abstract void removeSensors();

	public abstract void reverse();

	public abstract void rightSideCali();

	public abstract void leftSideCali();

	public abstract void frontCali();

	public abstract void initCali();

	public abstract void sendDesc();

	public abstract boolean fpStep();

	public abstract void setSpeed(float robot_steps_per_second);

	// set the fastest path for the robot to follow
	public void setFastestInstruction(Stack<Node> fast, int targetX, int targetY) {

		// to input instructions into the stack
		Direction tempFacing = face_dir;
		int tempX = x;
		int tempY = y;
		Node next = null;

		// loop until the fast (which is the stack) is empty
		while (true) {
			// if the stack empty, then nodes have been fully converted
			// hence, break
			if (fast.empty())
				break;
			else {
				// retrieve next node from the stack
				next = (Node) fast.pop();

				// conditional statements to decide direction to turn to
				// call getShortestTurnDirection to get fastest steps to turn
				if (next.getX() > tempX) {
					if (tempFacing != Direction.RIGHT)
						tempFacing = getShortestTurnInstruction(tempFacing, Direction.RIGHT);

					tempX += 1;
				} else if (next.getX() < tempX) {
					if (tempFacing != Direction.LEFT)
						tempFacing = getShortestTurnInstruction(tempFacing, Direction.LEFT);

					tempX -= 1;
				} else if (next.getY() < tempY) {
					if (tempFacing != Direction.UP)
						tempFacing = getShortestTurnInstruction(tempFacing, Direction.UP);

					tempY -= 1;
				} else if (next.getY() > tempY) {
					if (tempFacing != Direction.DOWN)
						tempFacing = getShortestTurnInstruction(tempFacing, Direction.DOWN);

					tempY += 1;
				}

				// add instruction for the shortest path to stack
				instructionsForFastestPath.add(Packet.forward_inst);
			}
		}
	}

	// simulate a right turn by the robot, by changing direction
	public Direction simulateTurnRight(Direction tempFacing) {
		switch (tempFacing) {
		case RIGHT:
			tempFacing = Direction.DOWN;
			break;
		case LEFT:
			tempFacing = Direction.UP;
			break;
		case UP:
			tempFacing = Direction.RIGHT;
			break;
		case DOWN:
			tempFacing = Direction.LEFT;
			break;
		}
		return tempFacing;
	}

	// simulate a left turn by the robot, by changing direction
	public Direction simulateTurnLeft(Direction tempFacing) {
		switch (tempFacing) {
		case RIGHT:
			tempFacing = Direction.UP;
			break;
		case LEFT:
			tempFacing = Direction.DOWN;
			break;
		case UP:
			tempFacing = Direction.LEFT;
			break;
		case DOWN:
			tempFacing = Direction.RIGHT;
		}
		return tempFacing;
	}

	// method to get shortest way to turn to a specific direction
	// answer specific to current direction robot is face_dir
	public Direction getShortestTurnInstruction(Direction tempFacing, Direction targetFacing) {
		Stack<Direction> listOfTurnInstructions = new Stack<Direction>();

		// conditional statements specifying direction to turn
		if (tempFacing == Direction.RIGHT) {
			if (targetFacing == Direction.UP)
				instructionsForFastestPath.add(Packet.left_inst);

			else if (targetFacing == Direction.DOWN)
				instructionsForFastestPath.add(Packet.right_inst);

			else if (targetFacing == Direction.LEFT) {
				instructionsForFastestPath.add(Packet.left_inst);
				instructionsForFastestPath.add(Packet.left_inst);
			}

		} else if (tempFacing == Direction.LEFT) {
			if (targetFacing == Direction.UP)
				instructionsForFastestPath.add(Packet.right_inst);

			else if (targetFacing == Direction.DOWN)
				instructionsForFastestPath.add(Packet.left_inst);

			else if (targetFacing == Direction.RIGHT) {
				instructionsForFastestPath.add(Packet.left_inst);
				instructionsForFastestPath.add(Packet.left_inst);
			}

		} else if (tempFacing == Direction.UP) {
			if (targetFacing == Direction.LEFT)
				instructionsForFastestPath.add(Packet.left_inst);

			else if (targetFacing == Direction.RIGHT)
				instructionsForFastestPath.add(Packet.right_inst);

			else if (targetFacing == Direction.DOWN) {
				instructionsForFastestPath.add(Packet.left_inst);
				instructionsForFastestPath.add(Packet.left_inst);
			}

		} else if (tempFacing == Direction.DOWN) {
			if (targetFacing == Direction.RIGHT)
				instructionsForFastestPath.add(Packet.left_inst);

			else if (targetFacing == Direction.LEFT)
				instructionsForFastestPath.add(Packet.right_inst);

			else if (targetFacing == Direction.UP) {
				instructionsForFastestPath.add(Packet.left_inst);
				instructionsForFastestPath.add(Packet.left_inst);
			}

		}
		return targetFacing;
	}

	// returns true if the left side has blocks to calibrate
	public boolean rightSideCaliPossible() {
		if (face_dir == Direction.LEFT && isBlocked(x - 1, y - 2) && isBlocked(x + 1, y - 2))
			return true;
		else if (face_dir == Direction.RIGHT && isBlocked(x - 1, y + 2) && isBlocked(x + 1, y + 2))
			return true;
		else if (face_dir == Direction.DOWN && isBlocked(x - 2, y - 1) && isBlocked(x - 2, y + 1))
			return true;
		else if (face_dir == Direction.UP && isBlocked(x + 2, y - 1) && isBlocked(x + 2, y + 1))
			return true;

		return false;
	}

	// returns true if the front of the robot has blocks to calibrate
	public boolean canFront_Calibrate() {
		if (face_dir == Direction.LEFT && isBlocked(x - 2, y - 1) && isBlocked(x - 2, y) && isBlocked(x - 2, y + 1))
			return true;
		else if (face_dir == Direction.RIGHT && isBlocked(x + 2, y - 1) && isBlocked(x + 2, y)
				&& isBlocked(x + 2, y + 1))
			return true;
		else if (face_dir == Direction.DOWN && isBlocked(x - 1, y + 2) && isBlocked(x, y + 2)
				&& isBlocked(x + 1, y + 2))
			return true;
		else if (face_dir == Direction.UP && isBlocked(x - 1, y - 2) && isBlocked(x, y - 2) && isBlocked(x + 1, y - 2))
			return true;

		return false;
	}

	// returns true if the right side of the robot has block to calibrate
	public boolean leftSideCaliPossible() {
		if (face_dir == Direction.LEFT && isBlocked(x - 1, y + 2) && isBlocked(x + 1, y + 2))
			return true;
		else if (face_dir == Direction.RIGHT && isBlocked(x - 1, y - 2) && isBlocked(x + 1, y - 2))
			return true;
		else if (face_dir == Direction.DOWN && isBlocked(x + 2, y - 1) && isBlocked(x + 2, y + 1))
			return true;
		else if (face_dir == Direction.UP && isBlocked(x - 2, y - 1) && isBlocked(x - 2, y + 1))
			return true;

		return false;
	}

	// return the class variable x
	public int getX() {
		return x;
	}

	// return the class variable y
	public int getY() {
		return y;
	}

	// set position of the robot and face_dir direction
	public void setRobotPos(int x, int y, Direction face_dir) {
		this.x = x;
		this.y = y;
		this.face_dir = face_dir;
	}

	// get the visualizaton
	public Viz getViz() {
		return viz;
	}

	// set direction in which robot is face_dir
	public void setface(Direction face_dir) {
		this.face_dir = face_dir;
	}

	// set visualization
	public void setViz(Viz viz) {
		this.viz = viz;
	}

	// method to return whether front has a wall/obstacle
	public boolean isObstacleOrWallFront() {
		switch (face_dir) {
		case UP:
			if (isBlocked(x - 1, y - 2) || isBlocked(x, y - 2) || isBlocked(x + 1, y - 2))
				return true;
			break;
		case DOWN:
			if (isBlocked(x - 1, y + 2) || isBlocked(x, y + 2) || isBlocked(x + 1, y + 2))
				return true;
			break;
		case LEFT:
			if (isBlocked(x - 2, y - 1) || isBlocked(x - 2, y) || isBlocked(x - 2, y + 1))
				return true;
			break;
		case RIGHT:
			if (isBlocked(x + 2, y - 1) || isBlocked(x + 2, y) || isBlocked(x + 2, y + 1)) {
				return true;
			}
			break;

		}
		return false;
	}

	// return true if left bound is being violated
	public boolean checkLeftBound(int xi, int yi) {
		if ((xi < 0)) {
			return true;
		}
		return false;
	}

	// return true if top bound is being violated
	public boolean checkTopBound(int xi, int yi) {
		if ((yi < 0)) {
			return true;
		}
		return false;
	}

	// return true if bottom bound is being violated
	public boolean checkBottomBound(int xi, int yi) {
		if (yi > (map_height)) {
			return true;
		}
		return false;
	}

	// return true if right bound is being violated
	public boolean checkRightBound(int xi, int yi) {
		if (xi > (map_width)) {
			return true;
		}
		return false;
	}

	// return true if out of bounds or obstacle at given coordinates
	public boolean checkObstacle(int xi, int yi) {
		if (yi >= map_height || xi >= map_width || yi < 0 || xi < 0) {
			return true;
		} else if (map.get_grid_map_array()[yi][xi] == Explored_Types.convertTypeToInt("OBSTACLE")) {
			return true;
		}
		return false;
	}

	// return true if robot can move in specified direction
	public boolean isAbleToMove(Direction dir) {
		return isAbleToMove(dir, this.x, this.y);
	}

	public boolean isAbleToMove(Direction dir, int x, int y) {
		boolean canMove = true;

		switch (dir) {
		case LEFT:
			for (int i = -1; i < 2; i++) {
				// if part of the robot is out of bounds or going to hit a wall
				if (checkLeftBound(x - 1, y) || checkObstacle(x - 1 - 1, y + i)) {
					canMove = false;
					break;
				}
			}
			break;

		case RIGHT:
			for (int i = -1; i < 2; i++) {
				// if part of the robot is out of bounds or going to hit a wall
				if (checkRightBound(x + 1, y) || checkObstacle(x + 1 + 1, y + i)) {
					canMove = false;
					break;
				}
			}
			break;

		case UP:
			for (int i = -1; i < 2; i++) {
				// if part of the robot is out of bounds or going to hit a wall
				if (checkTopBound(x, y - 1) || checkObstacle(x + i, y - 1 - 1)) {
					canMove = false;
					break;
				}
			}
			break;

		case DOWN:

			for (int i = -1; i < 2; i++) {
				// if part of the robot is out of bounds or going to hit a wall
				if (checkBottomBound(x, y + 1) || checkObstacle(x + i, y + 1 + 1)) {
					canMove = false;
					break;
				}
			}
			break;
		}
		return canMove;
	}

	// return 1 if obstacle present, 0 if no obstacle and -1 if out of bounds
	public boolean isBlocked(int xi, int yi) {
		boolean rflag = false, tflag = false, bflag = false, lflag = false, obflag = false;
		lflag = checkLeftBound(xi, yi);
		tflag = checkTopBound(xi, yi);
		rflag = checkRightBound(xi, yi);
		bflag = checkBottomBound(xi, yi);
		obflag = checkObstacle(xi, yi);
		if (lflag || tflag || rflag || bflag || obflag) {
			return true;
		}

		if (map.get_grid_map_array()[yi][xi] == Explored_Types.convertTypeToInt("UN_EMPTY")
				|| map.get_grid_map_array()[yi][xi] == Explored_Types.convertTypeToInt("UN_OBSTACLE"))
			return true;

		return false;
	}

	// return false if robot cannot move to given coordinates
	public boolean canRobotMoveHere(int x, int y) {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (isBlocked(x + i, y + j))
					return false;
			}
		}

		return true;
	}

	// draw the robot, using graphics
	public void paintRobot(Graphics graphics) {
		graphics.setColor(Color.RED);
		x_g = 10 + (x - 1) * Map.sizeofsquare;
		y_g = 10 + (y - 1) * Map.sizeofsquare;
		graphics.fillArc(x_g, y_g, radius, radius, 0, 360);

		graphics.setColor(Color.BLUE);

		int dirOffsetX = 0;
		int dirOffsetY = 0;

		if (face_dir == Direction.UP)
			dirOffsetY = -30;
		else if (face_dir == Direction.DOWN)
			dirOffsetY = 38;
		else if (face_dir == Direction.LEFT)
			dirOffsetX = -30;
		else if (face_dir == Direction.RIGHT)
			dirOffsetX = 38;

		graphics.fillArc(x_g + 30 + dirOffsetX, y_g + 30 + dirOffsetY, 20, 20, 0, 360);
	}

	public abstract boolean retrieve_reset_wanted();
}