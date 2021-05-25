// package manager;
//package home.ghosto.Bureau.Cours.S6.PR.Projet.netradio.Gestionnaire;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * Class that manage connections with clients / broadcasters and packet's handling.
 * 
 * @author Nejma
 */
public class Manager {
	/**
	 * Constant that contains the maximum amount of broadcasters this manager can handle.
	 */
	public static final int MAX_BROADCASTERS = 100;
	
	/**
	 * Variable that contains the instance of the main manager.
	 */
	private static Manager _instance;
	
	/**
	 * Variable that contains the port where this manager is running at.
	 */
	public int port;
	/**
	 * Variable that contains every broadcaster subscribed to this manager.
	 */
	private ArrayList<Broadcaster> broadcasters = new ArrayList<Broadcaster>(MAX_BROADCASTERS);
	/**
	 * Variable that contains every clients subscribed to this manager.
	 */
	private ArrayList<Client> clients = new ArrayList<Client>();
	/**
	 * Variable that queue clients that need to get disconnected. 
	 * Pending disconnections will be managed in {@link #manage_client()}.
	 */
	private ArrayList<Client> to_disconnect = new ArrayList<Client>();
	/**
	 * Variable that queue clients that are waiting for being registered to this manager. 
	 * Pending connections will be managed in {@link #manage_client()}.
	 */
	private ArrayList<Client> to_connect = new ArrayList<Client>();
		
	/**
	 * Manager's constructor.
	 *  
	 * @param port Port where this manager should listen to.
	 */
	public Manager(int port)
	{
		this.port = port;
		listen_new_client();
		manage_client();
	}
	
	/**
	 * Method used to get main instance of the manager.
	 * 
	 * @return Main instance of this manager.
	 */
	public static Manager getInstance()
	{
		return _instance;
	}

	/**
	 * Method used to get every client subscribed to this manager.
	 * 
	 * @return Clients subscribed to this manager.
	 */
	public synchronized ArrayList<Client> get_clients() {
		return clients;
	}

	/**
	 * Method used to get every broadcaster subscribed to this manager.
	 * 
	 * @return Clients subscribed to this manager.
	 */
	public synchronized ArrayList<Broadcaster> get_broadcasters() {
		return broadcasters;
	}

	/**
	 * Method used to add a client in the queue of clients that are waiting for being registered.
	 * Pending connections will be managed in {@link #manage_client()}.
	 *
	 * @return Client that we want to register.
	 */
	public void connect(Client client) {
		synchronized (to_connect) {
			to_connect.add(client);
		}
	}

	/**
	 * Method used to add a client in the queue of clients that need to get disconnected.
	 * Pending disconnections will be managed in {@link #manage_client()}.
	 * 
	 * @param client Client that need to get disconnected.
	 */
	public void disconnect(Client client) {
		synchronized (to_disconnect) {
			to_disconnect.add(client);
		}
	}

	/**
	 * Method that will start a thread to listen to every incoming connections and creating the associated {@link Client} instance
	 * and registering it using {@link #connect(Client)}
	 */
	private void listen_new_client() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ServerSocket server;
				try {
					server = new ServerSocket(port);
					System.out.println("Listening on port " + port);
					while (true) {
						Socket socket = server.accept();
						System.out.println("Accepted connection from " + socket.getRemoteSocketAddress().toString());
						connect(new Client(socket));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	public String setting(String ip){
        ArrayList<String> out = new ArrayList<String>() ;
        var subips= ip.split("\\.");
        for (var e : subips){
            while (e.length()<3){
                e = "0"+e;
            }
            out.add(e);
        }
        return String.join(".",out);
    }
	/**
	 * Method used to listen to incoming packets from {@link #clients} 
	 * and manage queues of connections ({@link #to_connect} and disconnections {@link #to_disconnect}.
	 */
	private void manage_client() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (to_disconnect) {
						// Disconnecting every client in the queue
						for (Client client : to_disconnect) {
							client.disconnect();
							if(client.get_broadcasting() != null) {
								broadcasters.remove(client.get_broadcasting());
							}
							clients.remove(client);
						}
						to_disconnect.clear();
					}
					synchronized (to_connect) {
						// Connecting every client in the queue
						for (Client client : to_connect) {
							clients.add(client);
						}
						to_connect.clear();
					}
					// For each registered client
					for (Client client : get_clients()) {
						// If client is still alive
						if (client.is_alive()) {
							// If client sent data
							if (client.has_data()) {
								final String data = client.read_data();
								new Thread
								(
									new Runnable()
									{
										@Override
										public void run()
										{
											String content = data;
											// System.out.println(content);
											ArrayList<String> args = new ArrayList<String>();
											int index;
											// Parsing arguments and adding them in args
											while ((index = content.indexOf(' ')) != -1) {
												args.add(content.substring(0, index));
												content = content.substring(index+1);
												// System.out.println(content);
											}
											args.add(content);
											String packet_type = args.get(0);
											// System.out.println(packet_type);
											// If user is asking to view the list of every broadcasters
											if (packet_type.equals("LIST")) {
												client.send("LINB " + broadcasters.size());
												for (Broadcaster diffuseur : get_broadcasters()) {
													 client.send("ITEM " + diffuseur.get_id() + ' ' + setting(diffuseur.get_ip1()) + ' '
															+ diffuseur.get_port1() + ' ' + setting(diffuseur.get_ip2()) + ' '
															+ diffuseur.get_port2());
												}
												try {
													client.getSocket().close();
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											} 
											// Else if user is trying to register
											else if (packet_type.equals("REGI")) {
												// If client isn't broadcasting and manager didn't reach max clients capacity
												// System.out.println("Regi arrivÃ©");
												if (!client.is_broadcasting() && broadcasters.size() < MAX_BROADCASTERS) {
													// Creating broadcaster using specified arguments by client
													Broadcaster broadcaster = new Broadcaster(client.getSocket(), args.get(2),
															args.get(4), args.get(1), Integer.parseInt(args.get(3)),
															Integer.parseInt(args.get(5)));
													// Registering broadcaster
													broadcasters.add(broadcaster);
													// Setting broadcasting of client
													client.set_broadcasting(broadcaster);
													// Sending OK answer
													client.send("REOK");
												} 
												// Else if client is already broadcasting or manager reached max clients capacity
												else {
													// Sending NO answer
													client.send("RENO");

												}
											}
											// Else if user send a keep alive packet
											else if (packet_type.equals("IMOK")) {
												// Reseting keep alive
												client.reset_keepalive();
											}
										}

									}
								).start();
							}
						} 
						// Else, if client isn't alive anymore
						else {
							// Adding it in disconnection queue
							disconnect(client);
						}
					}
				}
			}

		}).start();

	}

	public static void main(String[] args) {
		int port = 4242;
		try {
			// Getting config file
			Path path = Paths.get(System.getProperty("user.dir") + "config.txt");
			if (Files.exists(path)) {
				List<String> content = Files.readAllLines(path);
				// Parsing port from config
				port = Integer.parseInt(content.get(0));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Creating main Manager's instance
		_instance = new Manager(port);
	}
}