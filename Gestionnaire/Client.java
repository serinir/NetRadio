package manager;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import manager.smartparser.SmartConverter;

/**
 * Class that represent a client connection that handle client's communication
 * and manage its data.
 * 
 * @author Nejma Smatti
 */
public class Client {
	/**
	 * Constant that contains the time (in seconds) since last answer before a
	 * client is considered as disconnected.
	 */
	public static final int KEEPALIVE_TIME = 20;
	/**
	 * Constant that contains the time (in seconds) since the last keep alive packet
	 * before we can send it again.
	 */
	public static final int SEND_KEEPALIVE_TIME = 5;
	/**
	 * Constant that contains max packets per second that can be accepted
	 */
	public static final int MAX_PACKETS_PER_SECOND = 3;
	/**
	 * Constant that contains the max weight of a transaction
	 */
	public static final int MAX_WEIGHT_TRANSACTION = 5;

	/**
	 * Variable that contains every {@link NetworkMethod} of this class
	 */
	private static HashMap<String, NetworkMethodData> network_methods = new HashMap<String, NetworkMethodData>();

	/**
	 * Variable that store the socket of this client.
	 */
	private Socket socket;
	/**
	 * Variable that store the last time from last keep alive packet received.
	 */
	private long keepalive;
	/**
	 * Variable that store the last time we sent a packet to client to ask if he was
	 * still alive.
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
	 * Variable that represent the {@link Broadcaster} instance of this client if
	 * this user is currently broadcasting. Can be null, check
	 * {@link #is_broadcasting()}.
	 */
	private Broadcaster broadcasting;
	/**
	 * Variable that store the current packet weight.
	 */
	private double packet_weight;
	/**
	 * Variable used to store the last transaction start time.
	 */
	private long transaction_time;
	/**
	 * Variable that is used to kill the Client.
	 */
	private boolean killed = false;

	/**
	 * Static method called once Client is used for the first time, its job is to register every NetworkMethod of Client's class
	 */
	static {
		// For each method of Client
		for (Method method : Client.class.getMethods()) {
			// If this method as NetworkMethod annotation
			if (method.isAnnotationPresent(NetworkMethod.class)) {
				NetworkMethod network_method = method.getAnnotation(NetworkMethod.class);
				// Registering it
				network_methods.put(network_method.name(),
						new NetworkMethodData(method, network_method));
			}
		}
	}

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
	 * Method used to add packet weight. This method will automatically kill the client if he sent more than {@value #MAX_PACKETS_PER_SECOND} packets per second.
	 * This method checks if client exceeded the max packets per second when {@link #packet_weight} is over {@value #MAX_WEIGHT_TRANSACTION}.
	 * 
	 * @param weight Weight to add to this client
	 */
	public void add_packet_weight(double weight) {
		// If transaction time hasn't been set
		if(transaction_time == 0) {
			// Defining transaction time to current time
			transaction_time = System.currentTimeMillis();
		}
		packet_weight += weight;
		// If packet weight is over max weight transaction
		if(packet_weight > MAX_WEIGHT_TRANSACTION) {
			// Getting packets sent per second ratio
			double packets = packet_weight / ((double) (System.currentTimeMillis() - transaction_time) / 1000d);
			// If packets sent per second ratio doesn't exceed MAX_PACKETS_PER_SECOND
			if (packets <= MAX_PACKETS_PER_SECOND) {
				transaction_time = System.currentTimeMillis();
				packet_weight = 0;
			} 
			// Else if packets sent per second ratio doesn't exceed MAX_PACKETS_PER_SECOND
			else {
				// Killing this client
				kill();
			}
		}
	}
	
	/**
	 * Method used to kill this Client.
	 * This client will be automatically disconnected from server at next {@link Manager#manage_client} frame.
	 */
	public void kill() {
		killed = true;
	}
	
	/**
	 * Network method used that send broadcasters' informations to client. 
	 * This method is called once we receive a packet that has for packet type : "LIST".
	 */
	@NetworkMethod(name = "LIST", weight = 1)
	public void sendBroadcastersList() {
		send("LINB " + Manager.get_instance().get_broadcasters().size());
		for (Broadcaster diffuseur : Manager.get_instance().get_broadcasters()) {
			send("ITEM " + diffuseur.get_id() + ' ' + diffuseur.get_ip1() + ' ' + diffuseur.get_port1() + ' '
					+ diffuseur.get_ip2() + ' ' + diffuseur.get_port2());
		}
	}

	/**
	 * Network method used to set this client's broadcasting channel. 
	 * This method is called once we receive a packet that has for packet type : "REGI".
	 * 
	 * @param ip1 First ip
	 * @param ip2 Second ip
	 * @param id Broadcaster id
	 * @param port1 First port
	 * @param port2 Second port
	 */
	@NetworkMethod(name = "REGI", weight = 1.5)
	public void set_broadcasting(String id, String ip1, int port1, String ip2, int port2) {
		// If we aren't broadcasting
		if (!is_broadcasting()) {
			// Creating broadcaster using specified arguments by client
			Broadcaster broadcaster = new Broadcaster(ip1, ip2, id, port1, port2);
			if(Manager.get_instance().register_broadcaster(broadcaster)) {
				// Setting broadcasting of client
				broadcasting = broadcaster;
				// Sending OK answer
				send("REOK");
			} else {
				// Sending NO answer
				send("RENO");
			}
		}
		// Else if we are already broadcasting
		else {
			// Sending NO answer
			send("RENO");
		}
	}

	/**
	 * Network method that reset keep alive time. 
	 * This method is called once we receive a packet that has for packet type : "IMOK".
	 */
	@NetworkMethod(name = "IMOK", weight = 0)
	public void reset_keepalive() {
		keepalive = System.currentTimeMillis();
		should_send_keepalive = System.currentTimeMillis();
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
	 * Method used to get the {@link Broadcaster} instance of this client. Can be
	 * null, check {@link #is_broadcasting()}.
	 * 
	 * @return {@link Broadcaster} instance of this client if broadcasting, null otherwise
	 */
	public Broadcaster get_broadcasting() {
		return broadcasting;
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
	 * Method used to check if this client is still alive. If
	 * {@link #should_send_keepalive()} is true, we will automatically send a packet
	 * to this client to ask if he's still alive
	 * 
	 * @return True if time since last keep alive packet received is under {@value #KEEPALIVE_TIME} seconds, false otherwise
	 */
	public boolean is_alive() {
		if (should_send_keepalive()) {
			should_send_keepalive = System.currentTimeMillis();
			send_keepalive();
		}
		return !killed && (System.currentTimeMillis() - keepalive) / 1000 < KEEPALIVE_TIME;
	}

	/**
	 * Method used to check if we should send a packet to this client to check if
	 * he's still alive
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
		print_writer.println(content);
		print_writer.flush();
	}

	/**
	 * Method used to check if this client sent data
	 * 
	 * @return True if client has data pending in {@link #buffered_reader}, false otherwise
	 */
	public boolean has_data() {
		try {
			return !socket.isClosed() && buffered_reader.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Method used to manage data sent by client.
	 * This method will automatically detect packet's type and its arguments and dispatch it to the corresponding {@link NetworkMethod}.
	 * 
	 * @see SmartConverter
	 * 
	 * @throws IndexOutOfBoundsException Throws an IndexOutOfBoundsException if there is more or not enough arguments to call the specific {@link NetworkMethod}.
	 * @throws IllegalArgumentException Throws an IllegalArgumentException if there is no NetworkMethod with the specified name in packet.
	 * @throws Exception Throws an exception if an unexpected behavior happened.
	 */
	public void manage_data() throws IndexOutOfBoundsException, IllegalArgumentException, Exception {
		// If this client has data
		if (has_data()) {
			// Reading client data
			String content = buffered_reader.readLine();
			System.out.println("Packet received : " + content);
			// Splitting received content base on space char
			String[] args = content.split(" ");
			// Getting packet type
			String network_method_name = args[0];
			// If there is a NetworkMethod that has for name packet_type
			if (network_methods.containsKey(network_method_name)) {
				// Getting associated NetworkMethodData to packet_type
				NetworkMethodData network_method_data = network_methods.get(network_method_name);
				Method method = network_method_data.get_method();
				Object[] parameters = new Object[method.getParameterCount()];
				// If required parameters size are equals to received args size
				if(parameters.length == args.length - 1) {
					// For each parameters
					for(int i=0; i<parameters.length; ++i) {
						// Converting received data to corresponding parameter's type
						parameters[i] =	SmartConverter.convert(method.getParameters()[i].getType(), args[i + 1]);
					}
					// Adding packet_weight to this transaction depending on this network method weight
					add_packet_weight(network_method_data.get_network_method().weight());
					// Invoking method this instance and with our parameters
					method.invoke(this, parameters);
				} 
				// Else if required parameters size are over or under received args size
				else {
					// Throwing IndexOutOfBoundsException
					throw new IndexOutOfBoundsException("Invalid parameters count");
				}
			} 
			// If there is no NetworkMethod that has for name packet_type
			else {
				// Adding a default packet weight to this transaction
				add_packet_weight(1);
				// Throwing IllegalArgumentException
				throw new IllegalArgumentException("Invalid network method name");
			}
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
