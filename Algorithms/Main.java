import javax.swing.JFrame;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.Scanner;

enum OperatingSystem {
	Windows, Linux
}

enum State {
	IDLE, WAITINGFORCOMMAND, EXPLORATION, FASTESTPATHHOME, FASTESTPATH, DONE, RESETFASTESTPATHHOME,
	SENDINGMAPDESCRIPTOR,
}

public class Main {

	public static void main(String[] args) {

		// Initialisation of program objects & variables
		Interface_Robot theRobot;
		Viz viz = new Viz();
		State currentState = State.WAITINGFORCOMMAND;
		Factory_Packets pf = null;
		Queue<Packet> recvPackets = null;
		A_star_search as = null;
		Node waypoint = null;

		// Initialisation of program objects & variables
		JFrame frame = null;

		String OS = System.getProperty("os.name").toLowerCase();

		OperatingSystem theOS = OperatingSystem.Windows;
		int wayx = 1;
		int wayy = 1;

		// variable to indicate whether exploration for fastest path is done or not
		// boolean explorationForFastestPathDone = false;

		if (OS.indexOf("win") >= 0)
			theOS = OperatingSystem.Windows;
		else if ((OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0))
			theOS = OperatingSystem.Linux;

		if (theOS == OperatingSystem.Windows) {
			frame = new JFrame("MDP Simulator");
			frame.setSize(600, 820);
		}

		Map map = new Map();

		////////////////////////// robot_simulator variable //////////////////////////
		boolean robot_simulator = true;

		if (robot_simulator) {
			int[][] test = new int[][] { { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0 },
					{ 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0 },
					{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
					{ 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

			// Generated to facilitate debugging
			Grid_Map_Iterator.print_explored_results_to_file("test.txt", test);
			Grid_Map_Iterator.array_to_hex((test));
			map.setMapArray(Grid_Map_Iterator.parse_text_file_for_map_info("p1Hex.txt", "p2Hex.txt"));

		}

		// Activate rendering frame for simulation
		if (robot_simulator) {
			// Initialise robot simulation
			theRobot = new Robot(1, 18, Direction.RIGHT, map);

			viz.setRobot(theRobot);
			theRobot.setViz(viz);
			theRobot.setSpeed(10f);

			Sensor s1 = new Sensor(3, Location_Sensor.RIGHT, 1, 1, theRobot.x, theRobot.y);
			Sensor s2 = new Sensor(3, Location_Sensor.RIGHT, 1, 0, theRobot.x, theRobot.y);
			Sensor s3 = new Sensor(3, Location_Sensor.DOWN, 1, 0, theRobot.x, theRobot.y);
			Sensor s4 = new Sensor(3, Location_Sensor.RIGHT, 1, -1, theRobot.x, theRobot.y);
			Sensor s5 = new Sensor(3, Location_Sensor.DOWN, -1, 0, theRobot.x, theRobot.y);
			Sensor s6 = new Sensor(5, Location_Sensor.TOP, 1, -1, theRobot.x, theRobot.y);

			Sensor[] Sensors = { s1, s2, s3, s4, s5, s6 };
			theRobot.addSensors(Sensors);

			if (theOS == OperatingSystem.Windows) {
				frame.getContentPane().add(viz);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(true);
			}
		} else {

			// set up real robot, sensors & communications
			recvPackets = new LinkedList<Packet>();
			pf = new Factory_Packets(recvPackets);
			theRobot = new Real_Robot(1, 18, Direction.RIGHT, map, pf);

			Real_Sensor s1 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, 1, theRobot.x, theRobot.y);
			Real_Sensor s2 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, 0, theRobot.x, theRobot.y);
			Real_Sensor s3 = new Real_Sensor(4, Location_Sensor.DOWN, 1, 1, theRobot.x, theRobot.y);
			Real_Sensor s4 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, -1, theRobot.x, theRobot.y);
			Real_Sensor s5 = new Real_Sensor(4, Location_Sensor.DOWN, -1, 1, theRobot.x, theRobot.y);
			Real_Sensor s6 = new Real_Sensor(5, Location_Sensor.TOP, 1, -1, theRobot.x, theRobot.y);

			Real_Sensor[] Sensors = { s1, s2, s3, s4, s5, s6 };
			theRobot.addSensors(Sensors);
			viz.setRobot(theRobot);
			theRobot.setViz(viz);

			if (theOS == OperatingSystem.Windows) {
				frame.getContentPane().add(viz);
				frame.setVisible(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setResizable(true);

				currentState = State.WAITINGFORCOMMAND;
			}

		}

		Exploration exe = new Exploration(null, robot_simulator, theRobot, viz, map);
		exe.initialise_robot_start_position(1, 18);

		while (currentState != State.DONE) {
			switch (currentState) {

			case IDLE:
				break;

			case WAITINGFORCOMMAND:

				// Command line interface for robot_simulator
				if (robot_simulator) {
					Scanner sc = new Scanner(System.in);

					int scanType = sc.nextInt();

					if (scanType == 1) {
						wayx = sc.nextInt();
						wayy = sc.nextInt();

						// Determine waypoint
						waypoint = new Node(wayx, wayy);
						map.set_way_point(wayx, wayy);

					} else if (scanType == 2) {
						// Determine robot position
						int getx = sc.nextInt();
						int gety = sc.nextInt();

						// Determine waypoint
						theRobot.setRobotPos(getx, gety, Direction.RIGHT);

					} else if (scanType == 3) {
						currentState = State.EXPLORATION;

					} else if (scanType == 4) {
						currentState = State.FASTESTPATH;

					} else if (scanType == 5) {

						map.resetMap();
						theRobot.setface(Direction.RIGHT);
						theRobot.x = 1;
						theRobot.y = 18;
						map.resetMap();
						viz.repaint();

					} else if (scanType == 6) {
						theRobot.sendDesc();

					} else if (scanType == 7) {

						float robot_steps_per_second = sc.nextInt();
						theRobot.setSpeed(robot_steps_per_second);

					} else if (scanType == 8) {

						float maxPercent = sc.nextFloat();
						exe.set_maze_coverage_percentage(maxPercent);

					} else if (scanType == 9) {

						String time = sc.next();
						String[] parts = time.split(":");
						float time_minutes = Float.parseFloat(parts[0]);
						float time_seconds = Float.parseFloat(parts[1]);
						exe.set_exploration_time_limit(time_minutes, time_seconds);

					}

					break;

				} else {
					// RPi commands being sent to real robot

					pf.listen();
					if (recvPackets.isEmpty()) {
						continue;
					}

					Packet pkt = recvPackets.remove();

					if (pkt.getType() == Packet.set_wp) {
						wayx = pkt.getX();
						wayy = 19 - pkt.getY();

						// Assign waypoint position for robot

						waypoint = new Node(wayx, wayy);
						map.set_way_point(wayx, wayy);

						// once waypoint received, perform exploration, get fastest path based on it

						// Initialise robot simulation
						Robot simRobot = new Robot(1, 18, Direction.RIGHT, map);

						viz.setRobot(simRobot);
						simRobot.setViz(viz);
						simRobot.setSpeed(10f);

						// SENSOR POSITIONS: 3 front, 2 right, 1 (long range) left
						Sensor sim1 = new Sensor(3, Location_Sensor.RIGHT, 1, 1, simRobot.x, simRobot.y);
						Sensor sim2 = new Sensor(3, Location_Sensor.RIGHT, 1, 0, simRobot.x, simRobot.y);
						Sensor sim3 = new Sensor(3, Location_Sensor.DOWN, 1, 0, simRobot.x, simRobot.y);
						Sensor sim4 = new Sensor(3, Location_Sensor.RIGHT, 1, -1, simRobot.x, simRobot.y);
						Sensor sim5 = new Sensor(3, Location_Sensor.DOWN, -1, 0, simRobot.x, simRobot.y);
						Sensor sim6 = new Sensor(5, Location_Sensor.TOP, 1, -1, simRobot.x, simRobot.y);

						Sensor[] simSensors = { sim1, sim2, sim3, sim4, sim5, sim6 };
						simRobot.addSensors(simSensors);

						if (theOS == OperatingSystem.Windows) {
							frame.getContentPane().add(viz);
							frame.setVisible(true);
							frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							frame.setResizable(true);
						}

						Exploration simexe = new Exploration(null, true, simRobot, viz, map);
						simexe.initialise_robot_start_position(1, 18);
						simexe.DoExp();

						// calculate P1 and P2
						// send to Android so that they can update their virtual map
						Grid_Map_Iterator.print_explored_results_to_file("theExplored.txt", map.get_grid_map_array());
						Grid_Map_Iterator.print_explored_results_to_hex("ExplorationHex.txt");
						Grid_Map_Iterator.print_obstacle_results_to_file(map.get_grid_map_array(), "theObstacle.txt");
						Grid_Map_Iterator.print_obstacle_results_to_hex("ObstacleHex.txt");

						// send exploration mdf to android for them to update the map
						pf.sendCMD("B:stat:Exploration mdf:" + Grid_Map_Iterator.P1_map_descriptor_hex + "$");
						pf.sendCMD("B:stat:Obstacle mdf:" + Grid_Map_Iterator.P2_map_descriptor_hex + "$");

						// send waypoint to android for them to update the map
						pf.sendCMD("B:stat:waypoint:" + String.valueOf(wayx) + ":" + String.valueOf(19 - wayy) + "$");

						// once exploration complete, reset viz to original REAL robot
						viz.setRobot(theRobot);
						viz.repaint();

						currentState = State.WAITINGFORCOMMAND;

					} else if (pkt.getType() == Packet.begin_explore) {
						currentState = State.EXPLORATION;

					} else if (pkt.getType() == Packet.stop_inst) {
						currentState = State.FASTESTPATHHOME;

					} else if (pkt.getType() == Packet.reset_inst) {
						currentState = State.RESETFASTESTPATHHOME;

						map.resetMap();
						theRobot.setface(Direction.RIGHT);
						theRobot.x = 1;
						theRobot.y = 18;
						map.resetMap();
						viz.repaint();

					} else if (pkt.getType() == Packet.begin_fp) {
						currentState = State.FASTESTPATH;

					} else if (pkt.getType() == Packet.GETMAPi)
						theRobot.sendDesc();

					else if (pkt.getType() == Packet.robot_pos) {
						// Assign robot position

						theRobot.setRobotPos(pkt.getX(), pkt.getY(), pkt.getDirection());

					} else {

						continue;
					}
					break;
				}

			case FASTESTPATHHOME:

				// Revise nodes and create new A* solution path
				map.map_update();
				A_star_search as1 = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
						map.get_node_with_xy_coordinates(1, 18));

				// Transmit instructions to robot
				theRobot.retrieve_fastest_instruction(as1.retrieve_fastest_path());

				if (robot_simulator)
					currentState = State.FASTESTPATH;
				else
					currentState = State.WAITINGFORCOMMAND;
				break;

			case EXPLORATION:
				// Initialise algorithmic exploration, invoke begin_explore()

				if (!robot_simulator)
					theRobot.surroundingSense();

				int DoExpResult = exe.DoExp();

				if (robot_simulator) {

					// Exploration completes, robot returns to start position again and return TRUE
					if (DoExpResult == 1) {
						Scanner sc = new Scanner(System.in);
						theRobot.removeSensors();

						int choice = sc.nextInt();

						if (choice == 1)
							currentState = State.FASTESTPATH;
						else
							currentState = State.WAITINGFORCOMMAND;

					}
				} else {

					// Exploration completes, robot returns to start position again and return TRUE
					if (DoExpResult == 1) {

						// Transmit packet to convey exploration is complete
						pf.sc.send(Packet.StartExplorationTypeFin + "$");

						// Transmit map descriptor information
						Grid_Map_Iterator.print_explored_results_to_file("theExplored.txt", map.get_grid_map_array());
						Grid_Map_Iterator.print_explored_results_to_hex("ExplorationHex.txt");
						Grid_Map_Iterator.print_obstacle_results_to_file(map.get_grid_map_array(), "theObstacle.txt");
						Grid_Map_Iterator.print_obstacle_results_to_hex("ObstacleHex.txt");
						pf.sendCMD("B:stat:Exploration mdf:" + Grid_Map_Iterator.P1_map_descriptor_hex + "$");
						pf.sendCMD("B:stat:Obstacle mdf:" + Grid_Map_Iterator.P2_map_descriptor_hex + "$");
						pf.sendCMD("B:stat:finish_exe_mdf$");
						pf.sendCMD("A:cmd:cali_final$");
						currentState = State.WAITINGFORCOMMAND;

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						pf.setFlag(false);

					} else if (DoExpResult == -1) {
						currentState = State.WAITINGFORCOMMAND;
						as = null;
						waypoint = null;

						// Interface_Robot theRobot;
						theRobot = new Real_Robot(1, 18, Direction.RIGHT, map, pf);

						Real_Sensor s1 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, 1, theRobot.x, theRobot.y);
						Real_Sensor s2 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, 0, theRobot.x, theRobot.y);
						Real_Sensor s3 = new Real_Sensor(4, Location_Sensor.DOWN, 1, 1, theRobot.x, theRobot.y);
						Real_Sensor s4 = new Real_Sensor(4, Location_Sensor.RIGHT, 1, -1, theRobot.x, theRobot.y);
						Real_Sensor s5 = new Real_Sensor(4, Location_Sensor.DOWN, -1, 1, theRobot.x, theRobot.y);
						Real_Sensor s6 = new Real_Sensor(5, Location_Sensor.TOP, 1, -1, theRobot.x, theRobot.y);

						Real_Sensor[] Sensors = { s1, s2, s3, s4, s5, s6 };
						theRobot.addSensors(Sensors);
						viz.setRobot(theRobot);
						theRobot.setViz(viz);

						map.resetMap();

						exe = new Exploration(null, robot_simulator, theRobot, viz, map);
						exe.initialise_robot_start_position(1, 18);
					}
				}

				if (DoExpResult != 1)
					currentState = State.WAITINGFORCOMMAND;
				break;

			case RESETFASTESTPATHHOME:
				// Revise nodes and create new A* solution path
				map.map_update();
				A_star_search as3 = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
						map.get_node_with_xy_coordinates(1, 18));

				// Transmit instructions to robot
				theRobot.retrieve_fastest_instruction(as3.retrieve_fastest_path());

				map.resetMap();
				theRobot.x = 1;
				theRobot.y = 18;

				currentState = State.WAITINGFORCOMMAND;

				break;

			case SENDINGMAPDESCRIPTOR:

				Grid_Map_Iterator.print_explored_results_to_file("theExplored.txt", map.get_grid_map_array());
				Grid_Map_Iterator.print_explored_results_to_hex("ExplorationHex.txt");

				Grid_Map_Iterator.print_obstacle_results_to_file(map.get_grid_map_array(), "theObstacle.txt");
				Grid_Map_Iterator.print_obstacle_results_to_hex("ObstacleHex.txt");
				if (!robot_simulator) {
					pf.sendCMD("B:stat:Exploration mdf:" + Grid_Map_Iterator.P1_map_descriptor_hex + "$");
					pf.sendCMD("B:stat:Obstacle mdf:" + Grid_Map_Iterator.P2_map_descriptor_hex + "$");
				}
				currentState = State.WAITINGFORCOMMAND;

			case FASTESTPATH:

				if (robot_simulator) {
					theRobot.initCali();

					// Revise nodes and create new A* solution path
					map.map_update();
					waypoint = map.get_node_with_xy_coordinates(wayx, wayy);
					A_star_search as31 = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
							waypoint);
					A_star_search as2 = new A_star_search(waypoint, map.get_node_with_xy_coordinates(13, 1));
					Stack<Node> as31GFP = as31.retrieve_fastest_path();

					if (as31GFP.isEmpty()) {
						A_star_search as4 = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
								map.get_node_with_xy_coordinates(13, 1));
						Path_Drawer.update(theRobot.x, theRobot.y, as4.retrieve_fastest_path());
						theRobot.retrieve_fastest_instruction(as4.retrieve_fastest_path());
						Path_Drawer.removePath();

					} else {
						Path_Drawer.update(theRobot.x, theRobot.y, as31GFP);
						theRobot.retrieve_fastest_instruction(as31.retrieve_fastest_path());
						Path_Drawer.update(theRobot.x, theRobot.y, as2.retrieve_fastest_path());
						theRobot.retrieve_fastest_instruction(as2.retrieve_fastest_path());
						Path_Drawer.removePath();

					}
					// Transmit instructions to robot
					currentState = State.SENDINGMAPDESCRIPTOR;

				} else {
					map.map_update();
					Stack<Node> stack = null;

					if (waypoint == null) {

						as = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
								map.get_node_with_xy_coordinates(13, 1));
						stack = as.retrieve_fastest_path();
						theRobot.retrieve_fastest_instruction(stack);

					} else {
						int x1 = waypoint.getX();
						int y1 = waypoint.getY();

						waypoint = map.get_node_with_xy_coordinates(x1, y1);
						as = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y), waypoint);
						A_star_search as2 = new A_star_search(waypoint, map.get_node_with_xy_coordinates(13, 1));
						stack = as2.retrieve_fastest_path();
						Stack<Node> stack2 = as.retrieve_fastest_path();

						if (!stack.isEmpty() && !stack2.isEmpty()) {

							stack.addAll(stack2);
							theRobot.retrieve_fastest_instruction(stack);

						} else {

							as = new A_star_search(map.get_node_with_xy_coordinates(theRobot.x, theRobot.y),
									map.get_node_with_xy_coordinates(13, 1));
							stack = as.retrieve_fastest_path();
							theRobot.retrieve_fastest_instruction(stack);
						}
					}

					// Transmit all data packet to RPi
					viz.repaint();
					pf.sc.send(Packet.FPFinish + "$");

				}
				break;

			}
		}
	}

	Socket_Client cs = new Socket_Client("192.168.9.9", 8081);
}
