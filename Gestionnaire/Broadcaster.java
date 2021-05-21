package manager;

/**
 * Class that contains every informations of a broadcaster
 * 
 * @author Nejma Smatti
 */
public class Broadcaster {
	/**
	 * Variable that store first ip of this broadcaster
	 */
	private String ip1;
	/**
	 * Variable that store second ip of this broadcaster
	 */
	private String ip2;
	/**
	 * Variable that store the id of this broadcaster
	 */
	private String id;
	/**
	 * Variable that store first port of this broadcaster
	 */
	private int port1;
	/**
	 * Variable that store second port of this broadcaster
	 */
	private int port2;

	/**
	 * Constructor of Broadcaster
	 *  
	 * @param ip1 First ip
	 * @param ip2 Second ip
	 * @param id Broadcaster's id
	 * @param port1 First port
	 * @param port2 Second port
	 */
	public Broadcaster(String ip1, String ip2, String id, int port1, int port2) {
		this.ip1 = ip1;
		this.ip2 = ip2;
		this.id = id;
		this.port1 = port1;
		this.port2 = port2;
	}

	/**
	 * Method used to get this broadcaster's first ip
	 * 
	 * @return First ip
	 */
	public String get_ip1() {
		return ip1;
	}

	/**
	 * Method used to get this broadcaster's second ip
	 * 
	 * @return Second ip
	 */
	public String get_ip2() {
		return ip2;
	}

	/**
	 * Method used to get this broadcaster's id
	 * 
	 * @return Broadcaster's id
	 */
	public String get_id() {
		return id;
	}
	
	/**
	 * Method used to get this broadcaster's first port
	 * 
	 * @return First port
	 */
	public int get_port1() {
		return port1;
	}

	/**
	 * Method used to get this broadcaster's second port
	 * 
	 * @return Second port
	 */
	public int get_port2() {
		return port2;
	}

}
