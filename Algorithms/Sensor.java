
// specifying imports
import java.util.HashMap;

public class Sensor {

	// declare class variables
	private static final int map_width = 15;
	private static final int map_height = 20;
	boolean checkHittingWall;
	int[] sensor_XY = new int[2];
	HashMap<int[], int[]> leftCoords;
	HashMap<int[], int[]> rightCoords;
	int senseRange;
	Location_Sensor currDir;
	int locationRobotX;
	int locationRobotY;
	int robotX;
	int robotY;

	// parameterized constructor to initialize Sensor
	public Sensor(int senseRange, Location_Sensor currDir, int locationRobotX, int locationRobotY, int robotX,
			int robotY) {
		super();
		this.locationRobotX = locationRobotX;
		this.locationRobotY = locationRobotY;
		this.robotX = robotX;
		this.robotY = robotY;
		this.checkHittingWall = false;
		this.senseRange = senseRange;
		this.currDir = currDir;
	}

	// set initial direction
	public void initialDirection() {
		switch (currDir) {
		case DOWN:
			locationRobotX = 1;
			locationRobotY = 0;
			break;
		case BOTTOM_LEFT:
			locationRobotX = 1;
			locationRobotY = -1;
			break;
		case LEFT:
			locationRobotX = 0;
			locationRobotY = -1;
			break;
		case TOP_LEFT:
			locationRobotX = -1;
			locationRobotY = -1;
			break;
		case TOP:
			locationRobotX = -1;
			locationRobotY = 0;
			break;
		case TOP_RIGHT:
			locationRobotX = -1;
			locationRobotY = 1;
			break;
		case RIGHT:
			locationRobotX = 1;
			locationRobotY = -1;
			break;
		case BOTTOM_RIGHT:
			locationRobotX = 1;
			locationRobotY = 1;
			break;
		}
	}

	// change direction to left
	public void changeToLeft() {
		switch (currDir) {
		case TOP:
			currDir = currDir.LEFT;
			break;
		case TOP_RIGHT:
			currDir = currDir.TOP_LEFT;
			break;
		case RIGHT:
			currDir = currDir.TOP;
			break;
		case BOTTOM_RIGHT:
			currDir = currDir.TOP_RIGHT;
			break;
		case DOWN:
			currDir = currDir.RIGHT;
			break;
		case BOTTOM_LEFT:
			currDir = currDir.BOTTOM_RIGHT;
			break;
		case LEFT:
			currDir = currDir.DOWN;
			break;
		case TOP_LEFT:
			currDir = currDir.BOTTOM_LEFT;
			break;
		}

		if (locationRobotX == 1 && locationRobotY == -1) {
			locationRobotX = -1;
			locationRobotY = -1;
		} else if (locationRobotX == 1 && locationRobotY == 0) {
			locationRobotX = 0;
			locationRobotY = -1;
		} else if (locationRobotX == 1 && locationRobotY == 1) {
			locationRobotX = 1;
			locationRobotY = -1;
		} else if (locationRobotX == 0 && locationRobotY == 1) {
			locationRobotX = 1;
			locationRobotY = 0;
		} else if (locationRobotX == -1 && locationRobotY == 1) {
			locationRobotX = 1;
			locationRobotY = 1;
		} else if (locationRobotX == -1 && locationRobotY == 0) {
			locationRobotX = 0;
			locationRobotY = 1;
		} else if (locationRobotX == -1 && locationRobotY == -1) {
			locationRobotX = -1;
			locationRobotY = 1;
		} else if (locationRobotX == 0 && locationRobotY == -1) {
			locationRobotX = -1;
			locationRobotY = 0;
		}
	}

	// change direction to right
	public void changeToRight() {
		switch (currDir) {
		case TOP:
			currDir = currDir.RIGHT;
			break;
		case TOP_RIGHT:
			currDir = currDir.BOTTOM_RIGHT;
			break;
		case RIGHT:
			currDir = currDir.DOWN;
			break;
		case BOTTOM_RIGHT:
			currDir = currDir.BOTTOM_LEFT;
			break;
		case DOWN:
			currDir = currDir.LEFT;
			break;
		case BOTTOM_LEFT:
			currDir = currDir.TOP_LEFT;
			break;
		case LEFT:
			currDir = currDir.TOP;
			break;
		case TOP_LEFT:
			currDir = currDir.TOP_RIGHT;
			break;
		}

		if (locationRobotX == 1 && locationRobotY == -1) {
			locationRobotX = 1;
			locationRobotY = 1;
		} else if (locationRobotX == 1 && locationRobotY == 0) {
			locationRobotX = 0;
			locationRobotY = 1;
		} else if (locationRobotX == 1 && locationRobotY == 1) {
			locationRobotX = -1;
			locationRobotY = 1;
		} else if (locationRobotX == 0 && locationRobotY == 1) {
			locationRobotX = -1;
			locationRobotY = 0;
		} else if (locationRobotX == -1 && locationRobotY == 1) {
			locationRobotX = -1;
			locationRobotY = -1;
		} else if (locationRobotX == -1 && locationRobotY == 0) {
			locationRobotX = 0;
			locationRobotY = -1;
		} else if (locationRobotX == -1 && locationRobotY == -1) {
			locationRobotX = 1;
			locationRobotY = -1;
		} else if (locationRobotX == 0 && locationRobotY == -1) {
			locationRobotX = 1;
			locationRobotY = 0;
		}

	}

	// update location of the robot
	public void updateLoc(int x, int y) {
		robotX = x;
		robotY = y;
	}

	// make robot sense the location
	public boolean senseLoc(Map map, int x, int y, int distance_robot) {
		boolean hitWall = false;

		int score = 0;

		if (distance_robot == 1)
			score = -80;
		else if (distance_robot == 2)
			score = -30;
		else if (distance_robot == 3)
			score = -9;
		else if (distance_robot == 4)
			score = -4;
		else if (distance_robot == 5)
			score = -2;
		else if (distance_robot == 6)
			score = -2;
		else if (distance_robot == 7)
			score = -2;
		else if (distance_robot == 8)
			score = -2;
		else if (distance_robot == 9)
			score = -2;

		if (x >= 0 && y >= 0 && x < map_width && y < map_height) {
			// make the score positive to indicate that it is a block
			if (map.simulated_map[y][x] == Explored_Types.convertTypeToInt("OBSTACLE")
					|| map.simulated_map[y][x] == Explored_Types.convertTypeToInt("UN_OBSTACLE")) {
				score = -score;
				hitWall = true;
			}
			map.setScore(x, y, score);
		} else
			hitWall = true;

		return hitWall;
	}

	public boolean Sense(Map map, int data, int[][] notWorkinghe) {
		// have to make sure does not overshoot boundary of environment
		int newX = 0;
		int newY = 0;

		// is true after robot hits a wall, to prevent further sensing
		boolean hitWall = false;
		boolean wallHitExtra = false;

		for (int i = 1; i < senseRange + 1; i++) {
			// make sure it is in the map senseRange and bound
			if (currDir == Location_Sensor.LEFT) {
				newX = robotX + locationRobotX - i;
				newY = robotY + locationRobotY;
			} else if (currDir == Location_Sensor.TOP) {
				newX = robotX + locationRobotX;
				newY = robotY + locationRobotY - i;
			} else if (currDir == Location_Sensor.RIGHT) {
				newX = robotX + locationRobotX + i;
				newY = robotY + locationRobotY;
			} else {
				newX = robotX + locationRobotX;
				newY = robotY + locationRobotY + i;
			}

			// hitWall will be true when senselocation returns a true
			// that indicates a wall has been encountered
			if (!hitWall) {
				hitWall = senseLoc(map, newX, newY, i);
				if (senseLoc(map, newX, newY, 0) && i == 1)
					wallHitExtra = true;
			} else
				// send a 0 to signify that this is behind a wall
				senseLoc(map, newX, newY, 0);
		}

		// update the map score after sensing
		map.mapAndScoreUpdate();
		return wallHitExtra;
	}
}