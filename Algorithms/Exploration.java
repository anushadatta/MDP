import java.util.Stack;

public class Exploration {

	// Check robot's previous move
	Facing previousFacing;

	// Stack to trace back (store previous steps)
	Stack<Action> back_track_steps;

	// Compute number of steps need to back-track
	int back_track_steps_count;
	boolean back_tracking;

	// Stack to trace back (store previous steps)
	Stack<Facing> back_track_facing;

	private static final int listOfActionsSize = 4;

	Viz viz;
	Socket_Client sc = null;
	boolean robot_simulator;
	Interface_Robot robot;
	Map map;

	ExplorationState state;
	boolean exploreUnexplored = false; // IMPORTANT

	// User selected speed (number of steps/sec)
	float robot_steps_per_second;
	boolean return_to_start = false;

	// Count number of sequential moveForward() & tell robot to
	// calibrate when a certain number is reached
	int move_forward_count;
	int time_to_sc;

	// get the next unexplored area to go to, coordinates will be {x of grid,y of
	// grid, x of grid that i will go to "look" at target block, y of looking grid}
	int[] nextUnexploredArea;

	// Robot's Start location
	int robot_start_X;
	int robot_start_Y;

	// Values of the offset for the block
	Stack<int[]> offset_values_stack;

	// % robot has to explore map, before terminating exploration
	float stop_percentage = 100;

	// Prevent double front calibration for robot
	boolean has_robot_just_fc;
	// Stack to store unexplored areas of grid, arrays to contain {x,y}
	Stack<int[]> stackUnexplored;

	// Store list of possible actions for robot
	Action listOfActions[];
	int actions_iterator;

	// A* for next unexplored area
	A_star_search path_to_unexplored_map_area;

	// Check if robot movement, to prevent end of exploration immediately
	boolean has_robot_moved;

	// True when robot is going towards an unexplored area in real time
	boolean robot_move_towards_block;

	boolean has_unexplored_areas = false;

	// Time program has to execute, before terminating exploration
	float time_before_stop;
	long time_since_latest_update;
	long time_since_start;
	long last_update_time;

	float time_minute;
	float time_second;

	public enum ExplorationState {
		INITIAL_EXPLORATION, CLEARING_UNKNOWN,
	}

	public enum Action {
		NO_ACTION, TURN_LEFT, TURN_RIGHT, TURN_UP, TURN_DOWN, MOVE_FORWARD,
	}

	public Exploration(Socket_Client sc, boolean robot_simulator, Interface_Robot robot, Viz viz, Map inMap) {
		this.robot_simulator = robot_simulator;
		if (robot_simulator)
			this.sc = sc;
		this.robot = robot;
		this.map = inMap;
		this.viz = viz;

		state = ExplorationState.INITIAL_EXPLORATION;

		// Initialise list to have all "NO.ACTION" items
		listOfActions = new Action[listOfActionsSize];
		for (int i = 0; i < listOfActionsSize; i++)
			listOfActions[i] = Action.NO_ACTION;

		back_track_steps = new Stack<Action>();
		back_track_facing = new Stack<Facing>();

		move_forward_count = 0;
		time_to_sc = 3; // side calibrate

		back_tracking = false;
		back_track_steps_count = 0;

		actions_iterator = -1;

		// Initialise to False to prevent exploration termination happening immediately
		has_robot_moved = false;

		// Initialise to False to prevent robot calibration twice
		has_robot_just_fc = false;

		// Initialise stack
		stackUnexplored = new Stack<int[]>();

		// Values stored for checking of offset
		offset_values_stack = new Stack<int[]>();
		initialise_offset_values_stack();

		robot_move_towards_block = false;

		// Variables to control the flow of exploration, mainly for checklist
		robot_steps_per_second = 30f;

		// % of map explored before stopping

		// Time to stop simulations
		time_minute = 5;
		time_second = 45;

		// time_before_stop = time_minute * 60000 + time_second * 1000;

		time_since_latest_update = 0;
		time_since_start = 0;
		last_update_time = System.currentTimeMillis();
	}

	public void set_maze_coverage_percentage(float percentage) {
		stop_percentage = percentage;
	}

	public void set_exploration_time_limit(float mins, float secs) {
		time_minute = mins;
		time_second = secs;
	}

	int get_explored_map() {
		// Tracks the number of map grids that are explored/DISCOVERED
		int explored = 0;
		for (int i = 0; i < Map.map_height; i++)
			for (int j = 0; j < Map.map_width; j++)
				if (map.get_grid_map_array()[i][j] == Explored_Types.convertTypeToInt("EMPTY")
						|| map.get_grid_map_array()[i][j] == Explored_Types.convertTypeToInt("OBSTACLE"))
					explored++;

		return explored;
	}

	boolean check_if_coordinate_in_stack(int[] coordinate_of_map) {
		int[] unexplored; // temp variable

		for (int i = 0; i < stackUnexplored.size(); i++) {

			unexplored = stackUnexplored.get(i);

			if (unexplored[0] == coordinate_of_map[0] && unexplored[1] == coordinate_of_map[1]
					&& unexplored[2] == coordinate_of_map[2] && unexplored[3] == coordinate_of_map[3])
				return true;
		}
		return false;
	}

	public void initialise_robot_start_position(int x, int y) {
		robot_start_X = x;
		robot_start_Y = y;
	}

	int[] get_coordinate_closest_to_unexplored_area(int x, int y) {

		for (int i = 0; i < offset_values_stack.size(); i++) {
			// if the robot can move to that spot, then calculate the cost and store it
			if (robot.canRobotMoveHere(x + offset_values_stack.get(i)[0], y + offset_values_stack.get(i)[1])) {
				return new int[] { x, y, x + offset_values_stack.get(i)[0], y + +offset_values_stack.get(i)[1] };
			}
		}

		// Convey error by returning {-1,-1}
		// Error signifiance: need to explore other paths before exploring the block
		return new int[] { -1, -1, -1, -1 };
	}

	public void initialise_offset_values_stack() {

		int[] offset;

		// down side
		offset = new int[] { -1, 1 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, 1 };
		offset_values_stack.push(offset);
		offset = new int[] { 1, 1 };
		offset_values_stack.push(offset);

		// right side
		offset = new int[] { 1, 1 };
		offset_values_stack.push(offset);
		offset = new int[] { 1, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { 1, -1 };
		offset_values_stack.push(offset);

		// up side
		offset = new int[] { 1, -1 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, -1 };
		offset_values_stack.push(offset);
		offset = new int[] { -1, -1 };
		offset_values_stack.push(offset);

		// left side
		offset = new int[] { -1, -1 };
		offset_values_stack.push(offset);
		offset = new int[] { -1, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { -1, 1 };
		offset_values_stack.push(offset);

		// down side
		offset = new int[] { -1, 2 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, 2 };
		offset_values_stack.push(offset);
		offset = new int[] { 1, 2 };
		offset_values_stack.push(offset);

		// right side
		offset = new int[] { 2, 1 };
		offset_values_stack.push(offset);
		offset = new int[] { 2, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { 2, -1 };
		offset_values_stack.push(offset);

		// up side
		offset = new int[] { 1, -2 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, -2 };
		offset_values_stack.push(offset);
		offset = new int[] { -1, -2 };
		offset_values_stack.push(offset);

		// left side
		offset = new int[] { -2, -1 };
		offset_values_stack.push(offset);
		offset = new int[] { -2, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { -2, 1 };
		offset_values_stack.push(offset);

		// down side
		offset = new int[] { -1, 3 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, 3 };
		offset_values_stack.push(offset);
		offset = new int[] { 1, 3 };
		offset_values_stack.push(offset);

		// right side
		offset = new int[] { 3, 1 };
		offset_values_stack.push(offset);
		offset = new int[] { 3, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { 3, -1 };
		offset_values_stack.push(offset);

		// up side
		offset = new int[] { 1, -3 };
		offset_values_stack.push(offset);
		offset = new int[] { 0, -3 };
		offset_values_stack.push(offset);
		offset = new int[] { -1, -3 };
		offset_values_stack.push(offset);

		// left side
		offset = new int[] { -3, -1 };
		offset_values_stack.push(offset);
		offset = new int[] { -3, 0 };
		offset_values_stack.push(offset);
		offset = new int[] { -3, 1 };
		offset_values_stack.push(offset);
	}

	void enter_all_unexplored_areas() {
		// Load grid map array to work with
		int[][] grid_map_array = map.get_grid_map_array();

		int[] coordinate_of_map;
		for (int y = 0; y < Map.map_height; y++) {
			for (int x = 0; x < Map.map_width; x++) {
				if (grid_map_array[y][x] == Explored_Types.convertTypeToInt("UN_EMPTY")
						|| grid_map_array[y][x] == Explored_Types.convertTypeToInt("UN_OBSTACLE")) {

					// Input areas explored by robot, next to unexplored areas,
					// and feasible for robot to fit/move in
					coordinate_of_map = get_coordinate_closest_to_unexplored_area(x, y);

					// Don't push onto stack if (values == -1) OR (values already in stack )
					if (coordinate_of_map[0] != -1 && coordinate_of_map[1] != -1
							&& !check_if_coordinate_in_stack(coordinate_of_map))
						stackUnexplored.push(coordinate_of_map);
				}
			}
		}

	}

	void updateUnexplored() {
		// Load grid map array to work with
		int[][] grid_map_array = map.get_grid_map_array();

		// Iterate through entire list to remove those that were halfway explored
		for (int i = 0; i < stackUnexplored.size(); i++) {

			// Remove area from stack if area NOT unexplored (i.e. it was explored on the
			// way)
			if (grid_map_array[stackUnexplored.get(i)[1]][stackUnexplored.get(i)[0]] == Explored_Types
					.convertTypeToInt("EMPTY")
					|| grid_map_array[stackUnexplored.get(i)[1]][stackUnexplored.get(i)[0]] == Explored_Types
							.convertTypeToInt("OBSTACLE")) {

				stackUnexplored.remove(i);
				i--;
			}
		}

		// Include all new unexplored areas now available to explore
		enter_all_unexplored_areas();
	}

	public int calculate_backtrack_steps_count() {

		Direction tempFacing = robot.face_dir;
		Action curAction;
		int tempX = robot.x;
		int tempY = robot.y;
		int numSteps = 0;
		int iterator = back_track_steps.size() - 1;

		while (iterator >= 0) {
			curAction = back_track_steps.get(iterator);

			switch (curAction) {

			case TURN_LEFT:
				tempFacing = robot.simulateTurnRight(tempFacing);

				numSteps++;
				break;

			case MOVE_FORWARD:
				int movementDistance = 1;

				if (tempFacing == Direction.UP)
					tempY += movementDistance;
				else if (tempFacing == Direction.DOWN)
					tempY -= movementDistance;
				else if (tempFacing == Direction.RIGHT)
					tempX -= movementDistance;
				else if (tempFacing == Direction.LEFT)
					tempX += movementDistance;

				numSteps++;

				// Break & return steps if obstacle encountered
				if (haveObstaclesAroundRobot(tempX, tempY))
					return numSteps;
				break;
			case TURN_RIGHT:
				tempFacing = robot.simulateTurnLeft(tempFacing);

				numSteps++;
				break;

			default:
				break;

			}

			iterator--;
		}

		// Return total number of steps to move, before returning beside wall to rerun
		// algo
		return numSteps;
	}

	public boolean haveObstaclesAroundRobot(int tempX, int tempY) {

		if (robot.isAbleToMove(Direction.UP, tempX, tempY) || robot.isAbleToMove(Direction.DOWN, tempX, tempY)
				|| robot.isAbleToMove(Direction.LEFT, tempX, tempY)
				|| robot.isAbleToMove(Direction.RIGHT, tempX, tempY)) {

			return true;
		}

		return false;
	}

	void perform_stored_actions() {

		// Instead of calculations in begin_explore(), robot executes actions stored
		// here
		switch (listOfActions[actions_iterator]) {

		case TURN_RIGHT:
			robot.rightTurn();

			back_track_steps.push(Action.TURN_RIGHT);
			break;

		case MOVE_FORWARD:

			robot.moveInstructionRobot();

			back_track_steps.push(Action.MOVE_FORWARD);
			move_forward_count++;
			break;

		case TURN_LEFT:
			//
			robot.leftTurn();

			back_track_steps.push(Action.TURN_LEFT);
			break;

		default:
			break;
		}

		actions_iterator -= 1;
	}

	public void DoIETurnRight() {

		has_robot_just_fc = false;
		actions_iterator = 0;

		// Check if can front calibrate & left calibrate (has 3 blocks)
		if (robot.canFront_Calibrate() && robot.leftSideCaliPossible() && !has_robot_just_fc) {
			robot.frontCali();
			has_robot_just_fc = true;
			move_forward_count = 0;

		}
		// Check if can only left calibrate after a certain no. of steps
		else if (robot.leftSideCaliPossible() && !has_robot_just_fc) {
			robot.leftSideCali();
			has_robot_just_fc = true;
			move_forward_count = 0;

		}
		// Turn to right after all calibrations complete
		robot.rightTurn();

		back_track_steps.push(Action.TURN_RIGHT);

		// listOfActions[1] = Action.TURN_RIGHT;
		listOfActions[0] = Action.MOVE_FORWARD;

	}

	public void DoIEBackTrack() {
		if (!back_tracking) {
			back_tracking = true;
			back_track_steps_count = calculate_backtrack_steps_count();
		}

		if (back_track_steps.empty()) {

			return;
		}
		Action prevAction = back_track_steps.pop();

		switch (prevAction) {
		case TURN_LEFT:
			robot.rightTurn();
			break;

		case TURN_RIGHT:
			robot.leftTurn();
			break;

		case MOVE_FORWARD:
			robot.reverse();
			break;
		default:

			break;
		}

		back_track_steps_count--;

		if (back_track_steps_count <= 0)
			back_tracking = false;
	}

	public void DoIEMoveForward(Facing face_dir) {

		has_robot_just_fc = false;
		boolean has_robot_calibrated = false;

		// once time_to_sc
		if (move_forward_count >= time_to_sc) {

			// Check if blocks next to robot, using side sensors
			if (robot.rightSideCaliPossible()) {

				robot.rightSideCali();

				// Counter reset
				move_forward_count = 0;
				has_robot_calibrated = true;

			} else if (robot.leftSideCaliPossible()) {

				robot.leftSideCali();
				move_forward_count = 0;
				has_robot_calibrated = true;
			}
		}

		if (!has_robot_calibrated) {
			// Just move forward
			robot.moveInstructionRobot();
			previousFacing = face_dir;
			back_track_facing.push(face_dir);
			has_robot_moved = true;
			move_forward_count++;

			back_track_steps.push(Action.MOVE_FORWARD);
		}

	}

	// True: return 1
	// False: return 0
	// Reset: return -1
	public int start_initial_exploration() {

		// Only continue if no stored actions, else execute stored actions
		if (actions_iterator != -1) {

			perform_stored_actions();

			return 0;
		}

		// Do not execute other actions when backtracking
		if (back_tracking) {
			DoIEBackTrack();

			return 0;
		}

		switch (robot.face_dir) {

		case UP:
			// Do stored actions, if previously face_dir up & can move right
			if (robot.isAbleToMove(Direction.RIGHT) && previousFacing == Facing.UP)
				DoIETurnRight();

			// Move up, if up is clear & right is a wall
			else if (!robot.isAbleToMove(Direction.RIGHT) && robot.isAbleToMove(Direction.UP))
				DoIEMoveForward(Facing.UP);

			// Turn left to face left, if can't move up or right
			else if (!robot.isAbleToMove(Direction.RIGHT) && !robot.isAbleToMove(Direction.UP))
				DoIETurnLeft();

			// Backtrack if no wall next to robot
			else
				DoIEBackTrack();

			break;

		case LEFT:

			// Do stored actions, if robot previously face_dir left wall & can move up
			if (robot.isAbleToMove(Direction.UP) && previousFacing == Facing.LEFT)
				DoIETurnRight();

			// Move left, if left is clear and wall is above
			else if (!robot.isAbleToMove(Direction.UP) && robot.isAbleToMove(Direction.LEFT))
				DoIEMoveForward(Facing.LEFT);

			// Turn left to face down, if can't move left or up
			else if (!robot.isAbleToMove(Direction.UP) && !robot.isAbleToMove(Direction.LEFT))
				DoIETurnLeft();

			// Backtrack if no wall beside robot
			else
				DoIEBackTrack();

			break;

		case DOWN:
			if (robot.isAbleToMove(Direction.LEFT) && previousFacing == Facing.DOWN)
				DoIETurnRight();

			// Move right, if down is clear & left is a wall
			else if (!robot.isAbleToMove(Direction.LEFT) && robot.isAbleToMove(Direction.DOWN))
				DoIEMoveForward(Facing.DOWN);

			else if (!robot.isAbleToMove(Direction.LEFT) && !robot.isAbleToMove(Direction.DOWN))
				DoIETurnLeft();

			// Backtrack if no wall next to robot
			else
				DoIEBackTrack();

			break;

		case RIGHT:
			// if can move down and it was face_dir right previously, execute stored actions
			if (robot.isAbleToMove(Direction.DOWN) && previousFacing == Facing.RIGHT)
				DoIETurnRight();

			// Move right, if right is clear & down is a wall
			else if (!robot.isAbleToMove(Direction.DOWN) && robot.isAbleToMove(Direction.RIGHT))
				DoIEMoveForward(Facing.RIGHT);

			// Turn left to face up, if can't move right or down
			else if (!robot.isAbleToMove(Direction.DOWN) && !robot.isAbleToMove(Direction.RIGHT))
				DoIETurnLeft();

			// Backtrack if no wall next to robot
			else
				DoIEBackTrack();
			break;

		default:
			return 0;
		}

		// Robot requested reset, request granted and new Robot created
		if (robot.retrieve_reset_wanted()) {

			return -1;
		}

		// When robot moves, check if robot at start location to terminate exploration
		if (has_robot_moved && robot.getX() == robot_start_X && robot.getY() == robot_start_Y) {

			return 1;
		}
		return 0;
	}

	public void DoIETurnLeft() {
		actions_iterator = 0;
		listOfActions[0] = Action.TURN_LEFT;

		// Check if can front calibrate (has 3 blocks right infront of robot)
		if (robot.canFront_Calibrate() && robot.rightSideCaliPossible() && !has_robot_just_fc) {

			robot.frontCali();
			has_robot_just_fc = true;
			// Counter reset
			move_forward_count = 0;

		} else if (robot.rightSideCaliPossible() && !has_robot_just_fc) {

			robot.rightSideCali();
			has_robot_just_fc = true;
			move_forward_count = 0;
		}

	}

	// Optimise to minimise no. of turns taken when robot navigating unknown maze
	public boolean clear_unknown() {

		// Iterate step by step, if robot going to a block
		if (robot_move_towards_block) {

			// Function returns true if robot has reached unexplored area
			if (robot.fpStep()) {

				// Ensure robot is face_dir the un-explored area before completing current path
				// if(robot.isFacingArea(nextUnexploredArea[0], nextUnexploredArea[1]))
				robot_move_towards_block = false;
			}

		} else {

			// If array == empty, check if robot returned to start location, else return to
			// start
			if (stackUnexplored.empty()) {

				if (robot.x == robot_start_X && robot.y == robot_start_Y) {

					if (return_to_start)
						Path_Drawer.removePath();
					fastest_path_map_adjustment();

					return true;

				}
				// Continue exploring if grid not fully explored/still unexplored
				else if (get_explored_map() < 290)// != map.map_height*map.map_width)
				{

					enter_all_unexplored_areas();

					return_to_start = false;

				} else {

					path_to_unexplored_map_area = new A_star_search(map.get_node_with_xy_coordinates(robot.x, robot.y),
							map.get_node_with_xy_coordinates(robot_start_X, robot_start_Y));

					// Transmit to robot to store instructions
					Stack<Node> fast = path_to_unexplored_map_area.retrieve_fastest_path();
					robot.setFastestInstruction(fast, robot_start_X, robot_start_Y);

					// Update pathdrawer for simulation graphics
					Path_Drawer.update(robot.x, robot.y, path_to_unexplored_map_area.retrieve_fastest_path());
					return_to_start = true;

					robot_move_towards_block = true;
				}
			} else {
				has_unexplored_areas = true;

				// Update grid nodes for A*
				map.map_update();

				// Update stackUnexplored<Stack> (to clear blocks encountered on the way
				// to
				// other blocks)
				updateUnexplored();

				int closest_cost = 999;
				int closest_area_index = 0;

				if (!stackUnexplored.empty()) {

					for (int i = 0; i < stackUnexplored.size(); i++) {
						// put the x and y value of the unexplored area
						// [0]=x value of unexplored area
						// [1]=y value of unexplored area
						// [2]=x value of coordinate of point next to unexplored area that the robot can
						// access
						// [3]=y value of coordinate of point next to unexplored area that the robot can
						// access
						path_to_unexplored_map_area = new A_star_search(
								map.get_node_with_xy_coordinates(robot.x, robot.y),
								map.get_node_with_xy_coordinates(stackUnexplored.get(i)[2], stackUnexplored.get(i)[3]));

						path_to_unexplored_map_area.retrieve_fastest_path();
						int cost = path_to_unexplored_map_area.getCost();

						// Update {closest_area_index} if cost < current lowest
						if (cost < closest_cost) {
							closest_cost = cost;
							closest_area_index = i;
						}
					}

					path_to_unexplored_map_area = new A_star_search(map.get_node_with_xy_coordinates(robot.x, robot.y),
							map.get_node_with_xy_coordinates(stackUnexplored.get(closest_area_index)[2],
									stackUnexplored.get(closest_area_index)[3]));

					nextUnexploredArea = stackUnexplored.remove(closest_area_index);

					// Transmit to robot to store instructions
					Stack<Node> fast = path_to_unexplored_map_area.retrieve_fastest_path();

					robot.setFastestInstruction(fast, nextUnexploredArea[0], nextUnexploredArea[1]);

					// Update pathdrawer for simulation graphics
					Path_Drawer.update(robot.x, robot.y, path_to_unexplored_map_area.retrieve_fastest_path());
				}
				robot_move_towards_block = true;
				return_to_start = false;
			}
		}
		return false;

	}

	public void fastest_path_map_adjustment() {

		// Count total number of obstacles
		// If obstacles > 30, remove those with lowest score
		// Also set all unexplored areas to explored with score of 1

		// Load grid map array
		int[][] grid_map_array = map.get_grid_map_array();

		Stack<Integer[]> obstacle_with_scores = new Stack<Integer[]>();

		for (int y = 0; y < Map.map_height; y++) {
			for (int x = 0; x < Map.map_width; x++) {

				if (grid_map_array[y][x] == Explored_Types.convertTypeToInt("OBSTACLE")) {
					Integer[] obstacle = new Integer[3];
					obstacle[2] = map.map_scores[y][x];
					obstacle[0] = x;
					obstacle[1] = y;

					obstacle_with_scores.push(obstacle);
				}

				// Mark unexplored areas as explored
				if (grid_map_array[y][x] == Explored_Types.convertTypeToInt("UN_EMPTY")
						|| grid_map_array[y][x] == Explored_Types.convertTypeToInt("UN_OBSTACLE")) {
					map.map_scores[y][x] = -1;
				}
			}
		}

		int minimum_score_index = 0;
		int minimum_score = 999;

		// Remove obstacles if (blocks > 30)
		while (obstacle_with_scores.size() > 30) {
			minimum_score = 999;
			minimum_score_index = 0;

			// Iterate list to find obstacle with lowest score
			for (int i = 0; i < obstacle_with_scores.size(); i++) {

				// if (current_score < recorded minimum_score), then replace it
				if (obstacle_with_scores.get(i)[2] < minimum_score) {
					minimum_score_index = i;
					minimum_score = obstacle_with_scores.get(i)[2];
				}
			}

			// set obstacle with minimum score to an empty block
			map.map_scores[obstacle_with_scores.get(minimum_score_index)[1]][obstacle_with_scores
					.get(minimum_score_index)[0]] = -1;

			// Remove obstacle with minimum score
			obstacle_with_scores.remove(minimum_score_index);
		}

		// Revise the score map
		map.mapAndScoreUpdate();
		for (int i = 0; i < map.grid_map_array.length; i++) {
			for (int j = 0; j < map.grid_map_array[i].length; j++) {

			}

		}
		map.set_map_to_map();
		map.FPoptimise();

		for (int i = 0; i < map.map_array_2.length; i++) {
			for (int j = 0; j < map.map_array_2[i].length; j++) {

			}

		}
	}

	// Robot reaches start position: return 1
	// False: return 0
	// Reset: return 1
	public int DoExp() {

		// Record time since start of simulation
		long startTime = System.currentTimeMillis();
		time_before_stop = time_minute * 60000 + time_second * 1000;

		try {
			while (true) {

				////////////////////////////////// variables for the control of
				////////////////////////////////// exploration(checklist)///////////////////////////

				// will have to comment out this section to allow robot to go back to start
				// during exploration

				time_since_latest_update = System.currentTimeMillis() - last_update_time;
				time_since_start += time_since_latest_update;
				time_since_start = System.currentTimeMillis() - startTime;
				last_update_time = System.currentTimeMillis();

				if (time_since_start >= time_before_stop) {

					return 1;
				}

				float percentageExplored = ((float) get_explored_map() / (float) (Map.map_height * Map.map_width))
						* 100;

				// if (percentageExplored >= stop_percentage)
				// return 1;

				////////////////////////////////// end of variables for the control of
				////////////////////////////////// exploration(checklist)///////////////////////////

				// Only do sleeps when in simulation
				if (robot_simulator)
					Thread.sleep((long) (500 / robot_steps_per_second));

				switch (state) {
				case INITIAL_EXPLORATION:

					// After robot reaches start location, start_initial_exploration() returns 1
					int DoInitialExplorationResult = start_initial_exploration();

					if (DoInitialExplorationResult == 1) {
						robot.sendDesc();

						if (System.currentTimeMillis() - startTime < 225 * 1000 && exploreUnexplored) {

							state = ExplorationState.CLEARING_UNKNOWN;
							enter_all_unexplored_areas();
							break;

						} else {

							fastest_path_map_adjustment();
							// map.map_update();
							viz.repaint();
							return 1;
						}

					} else if (DoInitialExplorationResult == -1) {

						return -1;
					}

					map.map_update();
					break;

				case CLEARING_UNKNOWN:
					//

					// Method returns true (1) when robot completes clearing map & returns to start
					// location
					// For debugging purposes, robot_steps_per_second set to 2f
					robot_steps_per_second = 2f;
					if (clear_unknown()) {
						return 1;
					}
					map.map_update();
					Path_Drawer.updateUnexplored(stackUnexplored);

					break;
				}

				// Update simulation graphics after every iteration
				viz.repaint();
			}

		} catch (Exception e) {

		}

		// return false by default
		return 0;

	}

}
