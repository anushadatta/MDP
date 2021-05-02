
// import required packages
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

// inherit from JComponent, the base class for all Swing components
// use this the visualize and create the robot_simulator for various tasks
public class Viz extends JComponent {
	public Interface_Robot robot;

	// default constuctor
	public Viz() {
	}

	// paramterized constructor
	public Viz(Interface_Robot robot) {
		this.robot = robot;
	}

	// return the robot
	public Interface_Robot getRobot() {
		return robot;
	}

	// specify the robot
	public void setRobot(Interface_Robot robot) {
		this.robot = robot;
	}

	// method to draw the components and perform visualization
	protected void paintComponent(Graphics g) {
		robot.map.gridPainting(g);
		robot.paintRobot(g);
		Path_Drawer.gridDraw(g);
		Path_Drawer.draw(g);
		super.paintComponent(g);
	}
}