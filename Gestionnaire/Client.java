//package home.ghosto.Bureau.Cours.S6.PR.Projet.netradio.Gestionnaire;

import java.io.*;
import java.net.Socket;

/**
 * Class that represent a client connection that handle client's communication and manage its data
 * 
 * @author Nejma
 */
public class Client {
	/**
	 * Constant that contains the time (in seconds) since last answer before a client is considered as disconnected.
	 */
	public static final int KEEPALIVE_TIME = 20;
	/**
	 * Constant that contains the time (in seconds) since the last keep alive packet before we can send it again.
	 */
	public static final int SEND_KEEPALIVE_TIME = 5;
	
	/**
	 * Variable that store the socket of this client.
	 */
	private Socket socket;
	/**
	 * Variable that store the last time from last keep alive packet received.
	 */
	private long keepalive;
	/**
	 * Variable that store the last time we sent a packet to client to ask if he was still alive.
	 */
	private long should_send_keepalive;
	/**
	 * Variable that represent the input stream (read) of our client.
	 */
	private BufferedReader buffered_reader;
	/**
	 * Variable that represent the output stream (write) of our client.
	 */
	private PrintWriter print_writer;
	/**
	 * Variable that represent the {@link Broadcaster} instance of this client if this user is currently broadcasting. 
	 * Can be null, check {@link #is_broadcasting()}.
	 */
	private Broadcaster broadcasting;

	/**
	 * Constructor of Client
	 * 
	 * @param socket Socket related to this client
	 */
	public Client(Socket socket) {
		this.socket = socket;
		try {
			print_writer = new PrintWriter(socket.getOutputStream(), true);
			buffered_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		keepalive = System.currentTimeMillis();
	}
	
	/**
	 * Method used to check if this user is broadcasting.
	 * 
	 * @return True if {@link #get_broadcasting()} is null, false otherwise
	 */
	public boolean is_broadcasting() {
		return broadcasting != null;
	}
	
	/**
	 * Method used to get the {@link Broadcaster} instance of this client.
	 * Can be null, check {@link #is_broadcasting()}.
	 * 
	 * @return {@link Broadcaster} instance of this client if broadcasting, null otherwise
	 */
	public Broadcaster get_broadcasting() {
		return broadcasting;
	}
	
	/**
	 * Method used to define the {@link Broadcaster} instance of this client.
	 * 
	 * @param broadcasting {@link Broadcaster} instance that this client will use
	 */
	public void set_broadcasting(Broadcaster broadcasting) {
		this.broadcasting = broadcasting;
	}

	/**
	 * Method used to get the socket related to this client
	 * 
	 * @return The socket connected to this client
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Method used to reset time from last keep alive packet received
	 */
	public void reset_keepalive() {
		keepalive = System.currentTimeMillis();
		should_send_keepalive = System.currentTimeMillis();
	}

	/**
	 * Method used to check if this client is still alive.
	 * If {@link #should_send_keepalive()} is true, we will automatically send a packet to this client to ask if he's still alive
	 * 
	 * @return True if time since last keep alive packet is < {@value #KEEPALIVE_TIME} seconds, false otherwise
	 */
	public boolean is_alive() {
		if (should_send_keepalive()) {
			should_send_keepalive=System.currentTimeMillis();
			send_keepalive();
		}
		return (System.currentTimeMillis() - keepalive) / 1000 < KEEPALIVE_TIME;

	}

	/**
	 * Method used to check if we should send a packet to this client to check if he's still alive
	 * 
	 * @return True if time since last packet sent to ask if client is still alive is > {@value #SEND_KEEPALIVE_TIME} seconds, false otherwise
	 */
	public boolean should_send_keepalive() {
		return (System.currentTimeMillis() - should_send_keepalive) / 1000 > SEND_KEEPALIVE_TIME;
	}

	/**
	 * Method used to send a packet to ask to client if he's still alive
	 */
	public void send_keepalive() {
		send("RUOK");
	}

	/**
	 * Method used to send a packet to this client
	 * 
	 * @param content Content of the packet we want to send to this client
	 */
	public void send(String content) {
		print_writer.write(content);
		print_writer.flush();
	}

	/**
	 * Method used to check if this client sent data
	 * 
	 * @return True if client has data pending in {@link #buffered_reader}, false otherwise
	 */
	public boolean has_data() {
		try {
			return buffered_reader.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Method used to get data received from client
	 * 
	 * @return Data sent by client, or null if there is an exception or no data
	 */
	public String read_data() {
		if(has_data()) {
			try {
				return buffered_reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Method used to disconnect this user
	 */
	public void disconnect() {
		try {
			buffered_reader.close();
			print_writer.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
