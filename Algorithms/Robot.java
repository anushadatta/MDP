
// specifying imports
import java.util.Stack;

public class Robot extends Interface_Robot {

	// declare class variables
	Sensor[] Sen;
	boolean hitWallFront = false;
	boolean hitWallRight = false;
	int rightSideCaliCount = 0;
	int frontCalibrateCount = 0;
	int sideCalibrateNum = 3;
	int numFrontCali = 3;
	float robot_steps_per_second = 1f;
	boolean frontCalibrated = false;
	boolean sideCalibrated = false;

	// parameterized contructor to initialize robot variables
	public Robot(int x, int y, Direction face_dir, Map map) {
		// starting postition
		super();
		this.x = x;
		this.y = y;
		this.face_dir = face_dir;
		this.map = map;
		hitWallFront = false;
		hitWallRight = false;
		instructionsForFastestPath = new Stack<Integer>();
		SenseRobotLocation();
	}

	// add sensors to the dummy robot
	public void addSensors(Sensor[] sensors) {
		this.Sen = sensors;
		// sense the surrounding
		surroundingSense();
	}

	// update robot sensors with the new location
	public void updateSensor() {
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].updateLoc(x, y);
		}
	}

	// deactivate sensors
	public void removeSensors() {
		Sen = new Sensor[0];
	}

	// specify robot speed as steps per time_second
	public void setSpeed(float robot_steps_per_second) {
		this.robot_steps_per_second = robot_steps_per_second;
	}

	// move robot
	public void moveInstructionRobot() {
		int movementDistance = 1;
		if (face_dir == Direction.UP)
			y -= movementDistance;
		else if (face_dir == Direction.DOWN)
			y += movementDistance;
		else if (face_dir == Direction.RIGHT)
			x += movementDistance;
		else if (face_dir == Direction.LEFT)
			x -= movementDistance;

		// update the location for the robot in the sensors
		updateSensor();

		// make robot sense surroundings
		surroundingSense();

	}

	// make robot reverse
	public void reverse() {
		int movementDistance = 1;
		if (face_dir == Direction.UP)
			y += movementDistance;
		else if (face_dir == Direction.DOWN)
			y -= movementDistance;
		else if (face_dir == Direction.RIGHT)
			x -= movementDistance;
		else if (face_dir == Direction.LEFT)
			x += movementDistance;

		// update the location for the robot in the sensors
		updateSensor();

		// make robot sense surrounding
		surroundingSense();

	}

	// method to allow to robot to sense the surrounding
	public void surroundingSense() {
		boolean sensePlaceHolder;
		boolean sensePlaceHolder1;
		int countF = 0;
		int countR = 0;

		for (int i = 0; i < Sen.length; i++) {
			sensePlaceHolder = Sen[i].Sense(map, 0, null);
			sensePlaceHolder1 = Sen[i].Sense(map, 0, null);

			// front wall hit
			if ((i <= 1 || i == 3) && sensePlaceHolder) {
				countF++;
				if (countF == 3)
					hitWallFront = true;
				else
					hitWallFront = false;
			}

			// right wall hit
			if ((i == 2 || i == 4) && sensePlaceHolder1) {
				countR++;
				if (countR == 2)
					hitWallRight = true;
				else
					hitWallRight = false;
			}
		}

		if (hitWallFront && hitWallRight) {
			// calibrate both front and side
			frontCali();
			rightSideCali();
			hitWallFront = false;
			hitWallRight = false;
		}
	}

	public void SenseRobotLocation() {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++)
				map.get_grid_map_array()[y + i][x + j] = Explored_Types.convertTypeToInt("EMPTY");
		}
	}

	// make the robot turn right
	public void rightTurn() {
		switch (face_dir) {
		case RIGHT:
			face_dir = Direction.DOWN;
			break;
		case LEFT:
			face_dir = Direction.UP;
			break;
		case UP:
			face_dir = Direction.RIGHT;
			break;
		case DOWN:
			face_dir = Direction.LEFT;
			break;
		}
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].changeToRight();
		}
		surroundingSense();
	}

	// make the robot turn left
	public void leftTurn() {
		switch (face_dir) {
		case RIGHT:
			face_dir = Direction.UP;
			break;
		case LEFT:
			face_dir = Direction.DOWN;
			break;
		case UP:
			face_dir = Direction.LEFT;
			break;
		case DOWN:
			face_dir = Direction.RIGHT;
		}
		// change sensor direction to follow robot
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].changeToLeft();
		}
		surroundingSense();
	}

	// get fastest path instructions
	public boolean retrieve_fastest_instruction(Stack<Node> fast) {
		byte[] instruction = new byte[100];
		int instcount = 0;
		if (fast == null)
			return true;
		while (!fast.isEmpty()) {
			Node two = (Node) fast.pop();
			try {
				Thread.sleep((long) (1000 / robot_steps_per_second));
				if (two.getX() > x) {
					while (face_dir != Direction.RIGHT) {
						rightTurn();
					}
					moveInstructionRobot();
				} else if (two.getX() < x) {
					while (face_dir != Direction.LEFT) {
						leftTurn();
					}
					moveInstructionRobot();
				} else if (two.getY() < y) {
					while (face_dir != Direction.UP) {
						leftTurn();
					}
					moveInstructionRobot();
				} else { // if(two.getY() < one.getY())
					while (face_dir != Direction.DOWN) {
						rightTurn();
					}
					moveInstructionRobot();
				}
				viz.repaint();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	// perform step indicated in the fastest path
	public boolean fpStep() {

		// if no more instructions, then fastest path is performed
		if (instructionsForFastestPath.isEmpty())
			return true;
		else {
			frontCalibrated = false;
			sideCalibrated = false;
			int instruction = (Integer) instructionsForFastestPath.remove(0);
			switch (instruction) {
			case Packet.right_inst:
				rightTurn();
				break;
			case Packet.left_inst:
				leftTurn();
				break;
			case Packet.forward_inst:
				if (rightSideCaliCount >= sideCalibrateNum) {
					if (rightSideCaliPossible()) {

						rightSideCali();
						rightSideCaliCount = 0;
					} else if (leftSideCaliPossible()) {

						leftSideCali();
						rightSideCaliCount = 0;
					}
					sideCalibrated = true;
				}
				if (frontCalibrateCount >= numFrontCali) {
					if (canFront_Calibrate()) {

						frontCali();
						frontCalibrateCount = 0;
						frontCalibrated = true;
					}
				}
				if (!frontCalibrated)
					frontCalibrateCount++;
				if (!sideCalibrated)
					rightSideCaliCount++;
				moveInstructionRobot();
				break;
			}
		}
		return false;
	}

	@Override
	public void initCali() {
	}

	@Override
	public void sendDesc() {
		// sendDescriptor(map);
	}

	@Override
	public void rightSideCali() {

	}

	@Override
	public void leftSideCali() {

	}

	@Override
	public void frontCali() {

	}

	@Override
	public void addSensors(Real_Sensor[] sensors) {
	}

	@Override
	public boolean isObstacleOrWallFront() {
		return false;
	}

	@Override
	public boolean retrieve_reset_wanted() {
		return false;
	}
}
