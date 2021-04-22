package gestionnaire;

import java.net.*;

public class Diffuseur extends Client {
	private String ip1;
	private String ip2;
	private String id;
	private int port1;
	private int port2;

	public Diffuseur(Socket socket, String ip1, String ip2, String id, int port1, int port2) {
		super(socket);
		this.ip1 = ip1;
		this.ip2 = ip2;
		this.id = id;
		this.port1 = port1;
		this.port2 = port2;
	}

	public String get_ip1() {
		return ip1;
	}

	public String get_ip2() {
		return ip2;
	}

	public String get_id() {
		return id;
	}

	public int get_port1() {
		return port1;
	}

	public int get_port2() {
		return port2;
	}

}
