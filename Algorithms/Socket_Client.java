
// specify all imports
import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.*;

public class Socket_Client {

	// define variables to be used like host IP, input stream, socket to use etc
	DataInputStream input = null;
	PrintStream output = null;
	String IP_Addr;
	int Port;
	InetAddress host;
	Socket socket = null;

	// paramterized constructor to initiate socket client using IP and the port
	public Socket_Client(String IP_Addr, int Port) {
		this.IP_Addr = IP_Addr;
		this.Port = Port;
	}

	// method to connect to RPi device
	// return true if connection was successful else return false
	public boolean connectToDevice() {

		// specify a timeout
		// if connection not established within this time, return false
		int timeout = 10000;
		try {
			socket = new Socket();

			// establish socket connection
			InetSocketAddress ISA = new InetSocketAddress(IP_Addr, Port);
			socket.connect(ISA, timeout);

			// specify input and output streams
			input = new DataInputStream(socket.getInputStream());
			output = new PrintStream(socket.getOutputStream());

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	// send packet data to RPi for further processing
	public int send(String packetData) {
		try {
			output.flush();
			return 0;
		} catch (Exception e) {

			e.printStackTrace();

			// re-establish connection if socket is not connected
			while (socket.isClosed()) {
				connectToDevice();
			}

			// attempt to send packet again
			send(packetData);
		}
		return 0;
	}

	// receive packet
	public String receivePacket(boolean resentflag, String Data) {
		String instruction = null;
		try {
			do {
				while (input.available() == 0) {
					Thread.sleep(100);
				}
				instruction = input.readLine();
			} while (instruction == null || instruction.equalsIgnoreCase(""));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return instruction;
	}

	// close socket connection
	public void closeConnection() {
		try {
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}