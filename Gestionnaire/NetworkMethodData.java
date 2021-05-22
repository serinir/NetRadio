package manager;

import java.lang.reflect.Method;

/**
 * Class that contains every informations about a {@link NetworkMethod}
 * 
 * @author Nejma Smatti
 */
public class NetworkMethodData {
	/**
	 * Method object associated to our {@link NetworkMethod}
	 */
	private Method method;
	/**
	 * Variable that store the concerned {@link NetworkMethod}
	 */
	private NetworkMethod network_method;
	
	/**
	 * NetworkMethodData constructor
	 * 
	 * @param method The method associated to the {@link NetworkMethod}
	 * @param network_method The concerned {@link NetworkMethod}
	 */
	public NetworkMethodData(Method method, NetworkMethod network_method) {
		this.method = method;
		this.network_method = network_method;
	}
	
	/**
	 * Method used to get the method associated to our {@link NetworkMethod}
	 * 
	 * @return The method object associated to our {@link NetworkMethod}
	 */
	public Method get_method()
	{
		return method;
	}
	
	/**
	 * Method used to get the concerned {@link NetworkMethod}
	 * 
	 * @return The concerned {@link NetworkMethod}
	 */
	public NetworkMethod get_network_method()
	{
		return network_method;
	}
}
