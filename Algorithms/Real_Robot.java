
// specify all imports
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Real_Robot extends Interface_Robot {

	// declare class variables

	int[][] final_map = new int[][] { { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 }, { 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 },
			{ 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7 } };

	boolean sideCalibrated = false;
	boolean frontCalibrated = false;
	boolean stepped = true;
	int count = 0;
	boolean reset_requested = false;
	Real_Sensor[] Sen;
	Factory_Packets pf = null;
	int rightSideCaliCount = 0;
	int frontCalibrateCount = 0;
	int sideCalibrateNum = 3;
	int numFrontCali = 3;
	float robot_steps_per_second = 10f;

	// method to set robot spped (steps per time_second)
	public void setSpeed(float robot_steps_per_second) {
		this.robot_steps_per_second = robot_steps_per_second;
	}

	// parameterized constructor to initialize real robot
	public Real_Robot(int x, int y, Direction face_dir, Map map, Factory_Packets pf) {
		super();
		this.face_dir = face_dir;
		this.map = map;
		this.pf = pf;
		this.x = x;
		this.y = y;
		SenseRobotLocation();
	}

	// retrieve direction (integer, as enum)
	public int getDirectionNum() {
		switch (face_dir) {
		case UP:
			return 0;
		case RIGHT:
			return 1;
		case DOWN:
			return 2;
		case LEFT:
			return 3;
		default:
			return -1;
		}
	}

	// add sensors to the robot by initalizing it
	public void addSensors(Real_Sensor[] sensors) {
		this.Sen = sensors;
	}

	// update robot sensors
	public void updateSensor() {
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].updateLoc(x, y);
		}
	}

	// deactivate robot sensors by re-assigning array
	public void removeSensors() {
		Sen = new Real_Sensor[0];
	}

	// move the robot
	public void moveInstructionRobot() {

		int movementDistance = 1;

		if (face_dir == Direction.DOWN) {
			y += movementDistance;
			final_map[y - 1][x - 1] = 1;
			final_map[y - 1][x] = 1;
			final_map[y - 1][x + 1] = 1;
		} else if (face_dir == Direction.RIGHT) {
			x += movementDistance;
			final_map[y][x - 1] = 1;
			final_map[y][x] = 1;
			final_map[y][x + 1] = 1;
		} else if (face_dir == Direction.UP) {
			y -= movementDistance;
			final_map[y + 1][x - 1] = 1;
			final_map[y + 1][x] = 1;
			final_map[y + 1][x + 1] = 1;
		} else if (face_dir == Direction.LEFT) {
			x -= movementDistance;
			final_map[y][x - 1] = 1;
			final_map[y][x] = 1;
			final_map[y][x + 1] = 1;
		}

		if (stepped) {
			count++;
			if (count % 4 == 0) {
				sendDesc();
			}
			pf.singleRobotMoveInstruction(Packet.forward_inst, x, y, getDirectionNum());
			// update the location for the robot in the sensors
			updateSensor();
			// make sensors sense the surrounding
			surroundingSense();
		}
		// call repaint method as robot has moved
		viz.repaint();
	}

	// make the robot go in reverse direction
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

		if (stepped) {
			pf.singleRobotMoveInstruction(Packet.reverse_inst, x, y, getDirectionNum());
			// update the location for the robot in the sensors
			updateSensor();
			// make sensors sense the surrounding
			surroundingSense();
		}
		// call repaint method as robot had gone in reverse direction (changed position)
		viz.repaint();
	}

	public boolean retrieve_reset_wanted() {
		return this.reset_requested;
	}

	// make robot sense surrounding, through packets
	public void surroundingSense() {
		Packet pck = null;

		while (pck == null || pck.packet_type != Packet.setObs) {
			pf.listen();

			pck = pf.latestPacket();
			if (pck == null) {
				continue;
			}

			if (pck.packet_type == Packet.reset_inst) {
				this.reset_requested = true;
				this.map.resetMap();
				x = 1;
				y = 18;
				face_dir = Direction.RIGHT;
				this.viz.repaint();
				return;
			}
		}
		int[] data = pck.getSensorData();
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].Sense(map, data[i], final_map);
		}
		viz.repaint();
	}

	// make the robot turn right
	public void rightTurn() {

		switch (face_dir) {
		// turn right
		case UP:
			face_dir = Direction.RIGHT;
			break;
		case DOWN:
			face_dir = Direction.LEFT;
			break;
		case RIGHT:
			face_dir = Direction.DOWN;
			break;
		case LEFT:
			face_dir = Direction.UP;
			break;
		}

		for (int i = 0; i < Sen.length; i++) {
			Sen[i].changeToRight();
		}
		if (stepped) {
			pf.singleRobotMoveInstruction(Packet.right_inst, x, y, getDirectionNum());
			// update location for the robot in the sensors
			updateSensor();
			// make sensors sense the surrounding
			surroundingSense();
		}
		// call repaint as robot has moved right
		viz.repaint();
	}

	// make robot turn left
	public void leftTurn() {

		switch (face_dir) {
		case UP:
			face_dir = Direction.LEFT;
			break;
		case DOWN:
			face_dir = Direction.RIGHT;
			break;
		case RIGHT:
			face_dir = Direction.UP;
			break;
		case LEFT:
			face_dir = Direction.DOWN;
			break;
		}
		// change sensor direction to follow robot
		for (int i = 0; i < Sen.length; i++) {
			Sen[i].changeToLeft();
		}
		if (stepped) {
			pf.singleRobotMoveInstruction(Packet.left_inst, x, y, getDirectionNum());
			// update the location for the robot in the sensors
			updateSensor();
			// make sensors "sense" the surrounding
			surroundingSense();
		}
		// call repaint as robot has moved left
		viz.repaint();
	}

	// make robot sense location
	public void SenseRobotLocation() {
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++)
				map.get_grid_map_array()[y + i][x + j] = Explored_Types.convertTypeToInt("EMPTY");
		}
	}

	// get fastest path instructions
	public boolean retrieve_fastest_instruction(Stack<Node> fast) {
		stepped = false;

		int counttocalibrate = 0;
		Queue<Integer> instruction = new LinkedList<Integer>();

		if (fast == null) {

			return false;
		}

		// while there are instructions in the fastest path stack
		while (!fast.isEmpty()) {
			Node next = (Node) fast.pop();
			counttocalibrate++;

			if (next.getX() < x) {
				switch (face_dir) {
				// turn right the fastest way
				// to face left
				case RIGHT:
					leftTurn();
					leftTurn();
					instruction.add(Packet.left_inst);
					instruction.add(Packet.left_inst);
					break;
				case LEFT:
					break;
				case UP:
					leftTurn();
					instruction.add(Packet.left_inst);
					break;
				case DOWN:
					rightTurn();
					instruction.add(Packet.right_inst);
					break;
				}

			} else if (next.getY() > y) {
				while (face_dir != Direction.DOWN) {
					switch (face_dir) {
					// turn right the fastest way
					case RIGHT:
						rightTurn();
						instruction.add(Packet.right_inst);
						break;
					case LEFT:
						leftTurn();
						instruction.add(Packet.left_inst);
						break;
					case UP:
						leftTurn();
						instruction.add(Packet.left_inst);
						leftTurn();
						instruction.add(Packet.left_inst);
						break;
					case DOWN:
						break;
					}
				}

			} else if (next.getX() > x) {
				switch (face_dir) {
				// turn right the fastest way
				case UP:
					rightTurn();
					instruction.add(Packet.right_inst);
					break;
				case DOWN:
					leftTurn();
					instruction.add(Packet.left_inst);
					break;
				case RIGHT:
					break;
				case LEFT:
					leftTurn();
					instruction.add(Packet.left_inst);
					leftTurn();
					instruction.add(Packet.left_inst);
					break;
				}

			} else if (next.getY() < y) {
				// face_dir up
				switch (face_dir) {
				// to face up
				case RIGHT:
					leftTurn();
					instruction.add(Packet.left_inst);
					break;
				case LEFT:
					rightTurn();
					instruction.add(Packet.right_inst);
					break;
				case UP:
					break;
				case DOWN:
					leftTurn();
					instruction.add(Packet.left_inst);
					leftTurn();
					instruction.add(Packet.left_inst);
					break;
				}

			}

			moveInstructionRobot();
			instruction.add(Packet.forward_inst);

		}
		stepped = true;
		pf.moveArduino(instruction);
		return true;

	}

	// perform fastest path step
	public boolean fpStep() {
		if (instructionsForFastestPath.isEmpty())
			return true;
		// if not empty then continue doing the path
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
	public void addSensors(Sensor[] sensors) {
	}

	@Override
	public void leftSideCali() {

		pf.leftCali(x, y, (getDirectionNum() - 1) % 4);
		surroundingSense();
	}

	@Override
	public void rightSideCali() {
		pf.rightSideCali(x, y, (getDirectionNum() + 1) % 4);
		surroundingSense();
	}

	@Override
	public void frontCali() {

		pf.frontCali(x, y, (getDirectionNum() + 1) % 4);
		surroundingSense();
	}

	@Override
	public void initCali() {
		switch (face_dir) {
		// ensure the robot is face_dir the left wall for calibration
		case RIGHT:
			leftTurn();
			leftTurn();
			break;
		case LEFT:
			break;
		case UP:
			leftTurn();
			break;
		case DOWN:
			rightTurn();
			break;

		}
		pf.initialCalibrate();
		face_dir = Direction.RIGHT;
		// update the android orientation
		String instructionString2 = Packet.AndroidLeft + Packet.delimiter + "1" + "$";
		pf.sendCMD(instructionString2);
		pf.sendCMD(instructionString2);
		viz.repaint();

	}

	// send whole map
	@Override
	public void sendDesc() {
		pf.sendDescriptor(map);
	}
}