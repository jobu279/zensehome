package zensemqtt;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Execute {
	private static final int RETRY_TIMEOUT = 300;
	private static final int SOCKET_TIMEOUT = 3000;
	private static final Object lockObj = new Object();
	static boolean debug = false; // set true to get extra system.out.println info

	
	private static Socket socket = null;
	private static PrintWriter writer = null;
	private static BufferedReader reader = null;

	public static int commandLogin(int code) {
		String output = executeCommand("Login", code);
		
		if (output != null && output.contains(">>Login Refused"))
			return Integer.parseInt(output.substring(16).replace("<<", "").trim());
		
		return (output != null && output.contains(">>Login Ok<<")) ? 0 : -1;
	}
	
	public static boolean commandLogout() {
		return executeCommand("Logout") != null;
	}
	
	public static boolean commandSetOn(int id) {
		return executeCommand("Set", id, "1") != null;
	}
	
	public static boolean commandSetOff(int id) {
		return executeCommand("Set", id, "0") != null;
	}
	
	public static boolean commandSetFade(int id, String level) {
		return executeCommand("Fade", id, level) != null;
	}
	
	public static boolean commandSetSimulationOn() {
		return executeCommand("Sim On") != null;
	}
	
	public static boolean commandSetSimulationOff() {
		return executeCommand("Sim Off") != null;
	}
	
	private static int[] commandGetDevices() {
		String output = executeCommand("Get Devices");
		
		if (output != null && output.contains(">>Get Devices ")) {
			String[] ids = output.substring(14).replace("<<", "").split(",");
			int[] devices = new int[ids.length];
			
			for (int i = 0; i < devices.length; i++) {
				devices[i] = Integer.parseInt(ids[i].trim());
			}
			
			return devices;
		}
		
		return new int[0];
	}
	
	private static int commandGetType(int id) {
		String output = null;
		int retry = 0;
		
		while (output == null && retry < 3) {
			output = executeCommand("Get Type", id);
			retry++;
		}
		
		if (output != null && output.contains(">>Get Type ")) {
			output = output.substring(11).replace("<<", "").trim();
			return output == "?" ? -1 : Integer.parseInt(output);
		}
		
		return -1;
	}
	

	private static String commandGetRoom(int id) {
		String output = null;
		int retry = 0;
		
		while (output == null && retry < 3) {
			output = executeCommand("Get Room", id);
			retry++;
		}
		
		if (output != null && output.contains(">>Get Room ")) {
			return output.substring(12).replace("'<<", "").trim();
		}
		
		return null;
	}
	
	private static String commandGetName(int id) {
		String output = null;
		int retry = 0;
		
		while (output == null && retry < 3) {
			output = executeCommand("Get Name", id);
			retry++;
		}
		
		if (output != null && output.contains(">>Get Name ")) {
			return output.substring(12).replace("'<<", "").trim();
		}
		
		return null;
	}
	
	// standard kommando til at hive status ud af alle enheder
	private static String getStatus() {
		String output = executeCommand("Get Status");
		
		if (output != null && output.contains(">>Get Status ")) {
			output = output.substring(13).replace("<<", "").trim();
			
			String[] values = output.split(" ");
			output = "";
			
			for (String value : values) {
				value = value.trim().substring(2);
				int status = Integer.parseInt(value, 16);
				String tmpStatus = Integer.toBinaryString(status).trim();
				
				while (tmpStatus.length() < 32)
					tmpStatus = "0" + tmpStatus;
				
				output = tmpStatus + output;
			}
			
			return output;
		}
		
		return null;
	}
	

	
	public static boolean openSocket(String host, int port) {
		int retry = 0;
    	if (debug == true) {System.out.println("Host: " + host + ":" + port);}


		while (socket == null && retry < 3) {
	    	if (debug == true) {System.out.println("Retry no.: " + retry);}
	    	if (debug == true) {System.out.println("1 - Socket: " + socket);}
			socket = getSocket(host, port);
			if (socket == null) {
		    	if (debug == true) {System.out.println("Retrying...");}
				retry++;
				
				try {
					Thread.sleep(RETRY_TIMEOUT);
				} catch (InterruptedException e) { }
			}
		}
		
		if (socket != null && socket.isConnected()) {
	    	if (debug == true) {System.out.println("Socket is connected.");}
			try {
				socket.setSoTimeout(SOCKET_TIMEOUT);
				writer = new PrintWriter(socket.getOutputStream(), true);
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 8 * 1024);
				
				return true;
			} catch (Exception e) {
				try {
					socket.close();
				} catch (IOException ioE) { ioE.printStackTrace(); }
			}
		}
		
		return false;
	}
	
	public static void closeSocket() {
		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
			
			if (reader != null) {
				reader.close();
				reader = null;
			}
			
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (Exception e) { }
	}
	
	private static Socket getSocket(String host, int port) {
		Socket socket = new Socket();
		SocketAddress address = new InetSocketAddress(host, port);
		
		try {
			socket.connect(address, SOCKET_TIMEOUT);
		} catch (IOException e) {
			return null;
		}
		
		return socket;
	}
	
	private static String executeCommand(String command) {
		return executeCommand(command, -1);
	}
	
	private static String executeCommand(String command, int id) {
		return executeCommand(command, id, null);
	}
	
	private static String executeCommand(String command, int id, String parameter) {
		String output = null;
		
		if (socket != null) {
			String input = ">>" + command + (id == -1 ? "" : " " + id) + (parameter == null ? "" : " " + parameter) + "<<";
			
			writer.println(input);
			
			try {
				output = reader.readLine();
			} catch (IOException e) { }
			
			if (output == null || output.contains("Timeout")) {
				output = null;
			}
		}
		
		return output;
	}
	
	
	
}
