package gestionnaire;

import java.io.*;
import java.net.Socket;

public class Client {
	private Socket socket;
	private long keepalive;
	private long should_send_keepalive;
	private BufferedReader br;
	private PrintWriter pw;

	public Client(Socket socket) {
		this.socket = socket;
		try {
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void reset_keepalive() {
		keepalive = System.currentTimeMillis();
	}

	public boolean is_alive() {
		if (should_send_keepalive()) {
			send_keepalive();
		}
		return (System.currentTimeMillis() - keepalive) / 1000 < Gestionnaire.KEEPALIVE_TIME;

	}

	public boolean should_send_keepalive() {
		return (System.currentTimeMillis() - should_send_keepalive) / 1000 > Gestionnaire.SEND_KEEPALIVE_TIME;
	}

	public void send_keepalive() {
		send("RUOK");
	}

	public void send(String content) {
		pw.print(content);
		pw.flush();
	}

	public boolean has_data() {
		try {
			return br.ready();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String read_data() {
		try {
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void disconnect() {
		try {
			br.close();
			pw.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
