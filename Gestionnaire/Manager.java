package manager;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.io.*;

/**
 * Class that manage connections with clients / broadcasters and packet's
 * handling.
 * 
 * @author Nejma Smatti
 */
public class Manager {
	/**
	 * Constant that contains the maximum amount of broadcasters this manager can
	 * handle.
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
	 * Variable that contains every broadcaster subscribed to this manager. This
	 * variable is volatile to set access to this variable as atomic, this will
	 * ensure us to always get latest value of this variable when calling
	 * {@link #get_broadcasters()} even if the method is called from another thread.
	 */
	private volatile ArrayList<Broadcaster> broadcasters = new ArrayList<Broadcaster>(MAX_BROADCASTERS);
	/**
	 * Variable that contains every clients subscribed to this manager. This
	 * variable is volatile to set access to this variable as atomic, this will
	 * ensure us to always get latest value of this variable when calling
	 * {@link #get_clients()} even if the method is called from another thread.
	 */
	private volatile ArrayList<Client> clients = new ArrayList<Client>();
	/**
	 * Variable that queue clients that need to get disconnected. Pending
	 * disconnections will be managed in {@link #manage_client()}.
	 */
	private ArrayList<Client> to_disconnect = new ArrayList<Client>();
	/**
	 * Variable that queue clients that are waiting for being registered to this
	 * manager. Pending connections will be managed in {@link #manage_client()}.
	 */
	private ArrayList<Client> to_connect = new ArrayList<Client>();
	/**
	 * Variable that contains the thread pool that will execute every client's tasks.
	 * This will automatically dispatch tasks in available threads and considerably restrict thread 
	 * size comparing to a simple thread per client system that wont be able to handle a large volume of users.
	 */
	private ThreadPoolExecutor thread_pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	/**
	 * Manager's constructor.
	 * 
	 * @param port Port where this manager should listen to.
	 */
	public Manager(int port) {
		this.port = port;
		listen_new_client();
		manage_client();
	}

	/**
	 * Method used to get main instance of the manager.
	 * 
	 * @return Main instance of this manager.
	 */
	public static Manager get_instance() {
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
	 * Method used to register a broadcast to broadcasters list.
	 * 
	 * @param broadcaster Broadcaster that we want to register.
	 * @return True if broadcaster has been registered, false otherwise.
	 */
	public boolean register_broadcaster(Broadcaster broadcaster) {
		if (broadcasters.size() < MAX_BROADCASTERS) {
			// Registering broadcaster
			get_broadcasters().add(broadcaster);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method used to add a client in the queue of clients that are waiting for
	 * being registered. Pending connections will be managed in
	 * {@link #manage_client()}.
	 *
	 * @param client Client that we want to register.
	 */
	public void connect(Client client) {
		synchronized (to_connect) {
			to_connect.add(client);
		}
	}

	/**
	 * Method used to add a client in the queue of clients that need to get
	 * disconnected. Pending disconnections will be managed in
	 * {@link #manage_client()}.
	 * 
	 * @param client Client that need to get disconnected.
	 */
	public void disconnect(Client client) {
		synchronized (to_disconnect) {
			to_disconnect.add(client);
		}
	}

	/**
	 * Method that will start a thread to listen to every incoming connections and
	 * creating the associated {@link Client} instance and registering it using
	 * {@link #connect(Client)}
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

	/**
	 * Method used to listen to incoming packets from {@link #clients} and manage
	 * queues of connections ({@link #to_connect} and disconnections
	 * {@link #to_disconnect}.
	 */
	private void manage_client() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					synchronized (to_disconnect) {
						// Disconnecting every client in the queue
						for (Client client : to_disconnect) {
							if (client.is_broadcasting()) {
								get_broadcasters().remove(client.get_broadcasting());
							}
							clients.remove(client);
							client.disconnect();
						}
						to_disconnect.clear();
					}
					synchronized (to_connect) {
						// Connecting every client in the queue
						for (Client client : to_connect) {
							clients.add(client);
						}
						// Defining thread pool size to 1 thread every 10 clients
						thread_pool.setCorePoolSize(clients.size() / 10 + 1);
						to_connect.clear();
					}
					// For each registered client
					for (Client client : get_clients()) {
						// If client is still alive
						if (client.is_alive()) {
							// If client sent data
							if (client.has_data()) {
								// Submitting task in our thread pool
								thread_pool.submit(() -> {
									try {
										client.manage_data();
									} catch (Exception e) {
										e.printStackTrace();
										client.send("FAIL");
									}
								});
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

	/**
	 * Main method of our Manager
	 * 
	 * @param args Process args
	 */
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
