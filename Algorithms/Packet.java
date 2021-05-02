public class Packet {

	Direction Direction = null;
	int[] sensors_data = null;
	int packet_type = 0;
	int x = 0;
	int y = 0;

	// Map instructions together
	final static char ARD = 'A';
	final static char AND = 'B';
	final static char PC = 'P';
	final static char RPI = 'R';

	// Mapping to packet instructions
	public static final int cali = 5;
	final static int forward_inst = 1;
	final static int right_inst = 2;
	final static int left_inst = 3;
	final static int reverse_inst = 4;

	// Define packet type of packets
	final static int set_wp = 5;
	final static int robot_pos = 6;
	final static int setObs = 7;
	final static int begin_explore = 1;
	final static int begin_fp = 2;
	final static int stop_inst = 3;
	final static int reset_inst = 4;

	final static String Ack = "ok";
	final static String command = "cmd";
	final static String set = "set";
	final static String delimiter = ":";
	final static String Stat = "stat";

	// Robot movements
	final static String FORWARD = "forward";
	final static String RIGHT = "right";
	final static String LEFT = "left";
	final static String REVERSE = "reverse";

	final static String frontCali = "A:cmd:fc";
	final static String initCali = "A:cmd:ic";
	final static String cornerCali = "A:cmd:frontc";
	final static String sideCali = "A:cmd:sc";
	final static String leftCali = "A:cmd:lsc";

	final static String StartExplorationType = "explore";
	final static String StartExplorationTypeOkARD = "A:ok:start_explore";
	final static String StartExplorationTypeOk = "B:ok:start_explore";
	final static String StartExplorationTypeFin = "B:ok:finish_explore";

	// Android: Stop robot
	final static String Reset = "reset";
	final static String ResetOK = "B:ok:reset";

	final static String Stop = "stop";
	final static String StopOk = "B:ok:stop";

	final static String AndroidLeft = AND + delimiter + Stat + delimiter + LEFT;
	final static String AndroidRight = AND + delimiter + Stat + delimiter + RIGHT;
	final static String AndroidForward = AND + delimiter + Stat + delimiter + FORWARD;
	final static String AndroidReverse = AND + delimiter + Stat + delimiter + REVERSE;

	// Fastest Path instrcutions from Android
	final static String StartFastestPath = "path";
	final static String FPAck = "start_path";
	final static String FPFinish = "B:ok:finish_path";
	final static String startExplore = AND + delimiter + Ack + delimiter + StartExplorationTypeOk;

	// Map Obstacles
	final static String Map = "map";
	final static String Block = "block";

	// Process string to configure robot to this X,Y coordinates on map
	final static String SetRobotPos = "startposition";
	final static String SetRobotPosOk = "B:ok:startposition";
	final static String SetWayPoint = "waypoint";
	final static String SetWayPointOK = "ok:waypoint";

	final static String ArduinoLeft = ARD + delimiter + command + delimiter + LEFT;
	final static String ArduinoRight = ARD + delimiter + command + delimiter + RIGHT;
	final static String ArduinoForward = ARD + delimiter + command + delimiter + FORWARD;
	final static String ArduinoReverse = ARD + delimiter + command + delimiter + REVERSE;

	final static int GETMAPi = 10;

	final static String MAPDESCRIPTORCMD = "B:map:set:";

	final static String StartFastestPathTypeOkANDROID = "A:ok:start_path";
	final static String StartFastestPathTypeOkARDURINO = "B:ok:start_path";

	public Direction getDirection() {
		return Direction;
	}

	public void setDirection(Direction direction) {
		Direction = direction;
	}

	// Handle packets with only one packet_type
	public Packet(int packet_type) {
		this.packet_type = packet_type;
	}

	// Navigating Grid Obstacles
	public Packet(int packet_type, int x, int y, Direction direction, int[] sensorData) {
		super();
		Direction = direction;
		sensors_data = sensorData;
		this.packet_type = packet_type;
		this.x = x;
		this.y = y;
	}

	// Robot position and face_dir direction
	public Packet(int packet_type, int x, int y, Direction direction) {
		this.packet_type = packet_type;
		Direction = direction;
		this.x = x;
		this.y = y;
	}

	// Waypoint data
	public Packet(int packet_type, int x, int y) {
		this.x = x;
		this.y = y;
		this.packet_type = packet_type;
	}

	public Packet(int setobstacle2, int[] data) {
		this.packet_type = setobstacle2;
		this.sensors_data = data;
	}

	public void setSensorData(int[] sensorData) {
		sensors_data = sensorData;
	}

	public int[] getSensorData() {
		return sensors_data;
	}

	public void setType(int packet_type) {
		this.packet_type = packet_type;
	}

	public int getType() {
		return packet_type;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
