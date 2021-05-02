public class Real_Sensor extends Sensor {

	// parameterized constructor to initialize real sensor
	public Real_Sensor(int range, Location_Sensor currDir, int locationRobotX, int locationRobotY, int robotX,
			int robotY) {
		super(range, currDir, locationRobotX, locationRobotY, robotX, robotY);
	}

	// method to sense location
	public boolean senseLoc(Map map, int x, int y, int distance_robot, boolean hitWall) {
		int score = 0;

		// conditional statements to assign score
		if (distance_robot == 1)
			score = -34;
		else if (distance_robot == 2)
			score = -21;
		else if (distance_robot == 3)
			score = -8;
		else if (distance_robot == 4)
			score = -5;
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

		if (x < Map.map_width && y < Map.map_height && x >= 0 && y >= 0) {
			// assign a positive score if wall hit
			// this prevents robot to go there since expensive
			if (hitWall)
				score = -score;
			map.setScore(x, y, score);
		}

		return hitWall;
	}

	public boolean Sense(Map map, int data, int[][] mapConfirmed) {
		int newX = 0;
		int newY = 0;

		// is true after robot hits a wall to prevent further sensing
		boolean hitWall = false;
		boolean wallHitExtra = false;

		for (int i = 1; i <= senseRange; i++) {
			// ensure it is within the map
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

			// hitWall will be true when sensor senses a wall
			if (!hitWall) {
				// when the sensor senses wall, then everything after that will be given score 0
				if (i == data) {
					hitWall = true;
					if (senseLoc(map, newX, newY, 0, hitWall) && i == 1)
						wallHitExtra = true;
				}
				senseLoc(map, newX, newY, i, hitWall);
			} else
				// send a 0 to signify that this is behind a wall
				senseLoc(map, newX, newY, 0, hitWall);
		}

		// update the map score after "sensing"
		map.mapAndScoreUpdate();

		return wallHitExtra;
	}
}