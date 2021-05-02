
// specify all imports and JAR files we will use
import java.util.Arrays;
import java.util.Queue;

public class Factory_Packets implements Runnable {

	// create class variables
	static Socket_Client sc = null;
	int delay = 0;
	int waypoint_x = 0, waypoint_y = 0;
	boolean camRun = true;
	// multi-threading with reference to a queue of strings/commands
	Queue<Packet> buffer;
	String ip = "192.168.9.9";
	String prev_packet = null;

	int port = 8081;
	final static int forward = 1;
	final static int right_inst = 2;
	final static int left_inst = 3;
	final static int reverse_inst = 4;
	public static final int cali = 5;
	boolean explore_flag = false;

	// method to set the previous packet
	public void setPrevPacket(String Prev) {
		prev_packet = Prev;
	}

	// default constructor
	public Factory_Packets() {
	}

	// parameterized constructor to connect to device and create buffer
	public Factory_Packets(Queue<Packet> buffer) {
		sc = new Socket_Client(ip, port);
		sc.connectToDevice();
		this.buffer = buffer;

	}

	// method to reconnect to device
	public void reconnectToDevice() {
		sc.closeConnection();
		sc.connectToDevice();
	}

	@Override
	public void run() {
		while (true) {
			listen();
		}

	}

	// start listening for packets from the socket
	public void listen() {
		boolean flag = true;
		String data = null;

		// while no data received, keep probing for the packet
		while (data == null) {
			data = sc.receivePacket(explore_flag, prev_packet);
		}

		String removeDataString = data.replace("P:", "");
		if (explore_flag == false)
			processPacket(removeDataString);
		else
			receiveSensorReadingsOrStop(removeDataString);

	}

	// process the packets received
	public void processPacket(String packetString) {
		String[] splitPacket = packetString.split(Packet.delimiter);
		if (splitPacket[1].equalsIgnoreCase(Packet.StartFastestPath)) {
			buffer.add(new Packet(Packet.begin_fp));
		} else if (splitPacket[1].equalsIgnoreCase(Packet.Stop)) {
			// interrupt exploration, stop robot
			buffer.add(new Packet(Packet.stop_inst));
			sc.send(Packet.StopOk);
		} else if (splitPacket[1].equalsIgnoreCase(Packet.StartExplorationType)) {
			buffer.add(new Packet(Packet.begin_explore));
			sc.send(Packet.StartExplorationTypeOk + "$");
			sc.send(Packet.StartExplorationTypeOkARD + ":" + 0 + "$");
			explore_flag = true;
		} else if (splitPacket[1].equalsIgnoreCase(Packet.Reset)) {
			// reset map
			buffer.add(new Packet(Packet.reset_inst));
			sc.send(Packet.ResetOK);

		} else if (splitPacket[0].equals(Packet.set)) {
			if (splitPacket[1].equalsIgnoreCase(Packet.SetRobotPos)) {
				sc.send(Packet.SetRobotPosOk);
			} else if (splitPacket[1].equalsIgnoreCase(Packet.SetWayPoint)) {
				String[] waypoint = splitPacket[2].replace("[", "").replace("]", "").split(",");
				int x = Integer.parseInt(waypoint[0]);
				int y = Integer.parseInt(waypoint[1]);
				buffer.add(new Packet(Packet.set_wp, x, y));
			}
		}
	}

	public void receiveSensorReadingsOrStop(String packetString) {
		String[] commandSplit = packetString.split(Packet.delimiter);
		if (commandSplit[0].equalsIgnoreCase(Packet.Map)) {
			if (commandSplit[1].equalsIgnoreCase("sensor")) {
				int[] data = new int[6];
				String[] sensr_readings = commandSplit[2].replace(" ", "").replace("[", "").replace("]", "").split(",");
				for (int i = 0; i < sensr_readings.length; i++) {
					data[i] = Integer.parseInt(sensr_readings[i]);
				}
				buffer.add(new Packet(Packet.setObs, data));

			}
		} else if (commandSplit[1].equalsIgnoreCase(Packet.Stop)) {
			// interrupt exploration
			sc.send(Packet.StopOk);
			explore_flag = false;
			buffer.add(new Packet(Packet.stop_inst));
		}
	}

	// set exploration flag
	public void setFlag(boolean flag) {
		this.explore_flag = flag;
	}

	// get the last packet
	public Packet latestPacket() {
		if (buffer.isEmpty())
			return null;
		return buffer.remove();
	}

	public void cornerCali() {
		sc.send(Packet.cornerCali);
		setPrevPacket(Packet.cornerCali);
	}

	public void rightSideCali(int x, int y, int directionNum) {

		sc.send(Packet.sideCali + "$");
		setPrevPacket(Packet.sideCali);
	}

	public void frontCali(int x, int y, int directionNum) {

		sc.send(Packet.frontCali + "$");
		setPrevPacket(Packet.frontCali);
	}

	public void leftCali(int x, int y, int directionNum) {

		sc.send(Packet.leftCali);
		setPrevPacket(Packet.leftCali);
	}

	public void initialCalibrate() {
		sc.send(Packet.initCali);
	}

	public boolean moveArduino(Queue<Integer> instructions) {
		String toSend = null;
		int count = 1;
		int temp = 0;
		if (instructions == null || instructions.isEmpty())
			return false;

		int subinstruct = instructions.remove();

		// perform while an instruction exists
		while (!instructions.isEmpty()) {
			temp = instructions.remove();
			if (subinstruct == temp && count < 10 && subinstruct == Packet.forward_inst) {
				count++;
				if (!instructions.isEmpty()) {
					continue;
				}
			}
			if (subinstruct == forward) {
				toSend = Packet.ArduinoForward + Packet.delimiter + count + "$";
			} else if (subinstruct == right_inst) {
				toSend = Packet.ArduinoRight + Packet.delimiter + 0 + "$";
			} else if (subinstruct == left_inst) {
				toSend = Packet.ArduinoLeft + Packet.delimiter + 0 + "$";
			}
			sc.send(toSend);
			count = 1;

			// if all instructions have been processed
			if (instructions.isEmpty() && subinstruct != temp) {
				if (temp == forward) {
					toSend = Packet.ArduinoForward + Packet.delimiter + count + "$";
				} else if (temp == right_inst) {
					toSend = Packet.ArduinoRight + Packet.delimiter + 0 + "$";
				} else if (temp == left_inst) {
					toSend = Packet.ArduinoLeft + Packet.delimiter + 0 + "$";

				}
				sc.send(toSend);
				break;
			}
			subinstruct = temp;
		}

		cornerCali();
		return true;
	}

	// send the whole map packet
	public void sendDescriptor(Map mapP) {
		// transpose array
		int[][] map = mapP.get_grid_map_array();
		String mapCmd = Packet.MAPDESCRIPTORCMD + "[";
		int[][] newMapArray = new int[Map.map_width][Map.map_height];
		for (int i = 0; i < Map.map_height; i++) {
			for (int j = 0; j < Map.map_width; j++) {
				newMapArray[j][i] = map[i][j];
			}
		}
		for (int i = 0; i < Map.map_width; i++) {
			mapCmd += Arrays.toString(newMapArray[i]);
			if (i != Map.map_width - 1)
				mapCmd += ",";
		}
		mapCmd += "]$";
		sc.send(mapCmd);
	}

	public boolean isFacingWall(int x, int y, int directionNum) {

		if ((y == 1 && directionNum == 0) || // face_dir top wall
				(y == 18 && directionNum == 2) || // face_dir bottom wall
				(x == 1 && directionNum == 3) || // face_dir left wall
				(x == 13 && directionNum == 1)) // face_dir right wall
			return true;
		else
			return false;

	}

	public boolean singleRobotMoveInstruction(int instruction, int x, int y, int directionNum) {
		// for one by one exploration, create one packet for a single movement
		// send to both android and arduino
		String instructionString = null;
		String instructionString_Android = null;
		if (instruction == forward) {
			instructionString = Packet.ArduinoForward;
			instructionString_Android = Packet.AndroidForward;

		} else if (instruction == right_inst) {
			instructionString = Packet.ArduinoRight;
			instructionString_Android = Packet.AndroidRight;

		} else if (instruction == left_inst) {
			instructionString = Packet.ArduinoLeft;
			instructionString_Android = Packet.AndroidLeft;

		} else if (instruction == reverse_inst) {
			instructionString = Packet.ArduinoReverse;
			instructionString_Android = Packet.AndroidReverse;

		} else {

			return false;
		}

		instructionString = instructionString + Packet.delimiter + "1" + "$";
		sc.send(instructionString);

		instructionString_Android = instructionString_Android + Packet.delimiter + "1" + "$";
		sc.send(instructionString_Android);

		setPrevPacket(instructionString);
		return true;
	}

	// send command packet
	public void sendCMD(String cmd) {
		sc.send(cmd);
	}
}