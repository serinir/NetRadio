package gestionnaire;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Gestionnaire {
	public static final int KEEPALIVE_TIME = 20;
	public static final int SEND_KEEPALIVE_TIME = 5;
	public static int port = 4242;
	private static ArrayList<Diffuseur> diffuseurs = new ArrayList<Diffuseur>(100);
	private static ArrayList<Client> clients = new ArrayList<Client>();

	public static void disconnect(Client client) {
		client.disconnect();
		clients.remove(client);
		if (client instanceof Diffuseur) {
			diffuseurs.remove((Diffuseur) client);
		}
	}

	public static void listen_new_client() {
		new Thread(new Runnable() {
			@Override
			public void run() {

				ServerSocket server;
				try {
					server = new ServerSocket(port);
					while (true) {
						Socket socket = server.accept();
						clients.add(new Client(socket));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public static void manage_client() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					for (Client client : clients) {
						if (client.is_alive()) {
							if (client.has_data()) {
								String content = client.read_data();
								ArrayList<String> args = new ArrayList<String>();
								int index;
								while ((index = content.indexOf(' ')) != -1) {
									args.add(content.substring(0, index));
									content = content.substring(index);
								}
								args.add(content);
								String packet_type = args.get(0);
								if (packet_type.equals("LIST")) {
									client.send("LINB " + diffuseurs.size());
									for (Diffuseur diffuseur : diffuseurs) {
										client.send("ITEM " + diffuseur.get_id() + ' ' + diffuseur.get_ip1() + ' '
												+ diffuseur.get_port1() + ' ' + diffuseur.get_ip2() + ' '
												+ diffuseur.get_port2());
									}

								} else if (packet_type.equals("REGI")) {
									if (diffuseurs.size() < 100) {
										Diffuseur diffuseur = new Diffuseur(client.getSocket(), args.get(1),
												args.get(2), args.get(3), Integer.parseInt(args.get(4)),
												Integer.parseInt(args.get(5)));
										clients.add(diffuseur);
										diffuseurs.add(diffuseur);
										disconnect(client);
										diffuseur.send("REOK");
									} else {
										client.send("RENO");

									}
								} else if (packet_type.equals("IMOK")) {
									client.reset_keepalive();
								}
							}
						} else {
							disconnect(client);
						}
					}
				}
			}

		}).start();

	}

	public static void main(String[] args) {
		try {
			Path path = Paths.get(System.getProperty("user.dir")+"config.txt");
			if(Files.exists(path)) {
			List <String> content = Files.readAllLines(path);
			port=Integer.parseInt(content.get(0));
			}
		}
			catch (IOException e) {
			e.printStackTrace();
		}
		listen_new_client();
		manage_client();
	}

}