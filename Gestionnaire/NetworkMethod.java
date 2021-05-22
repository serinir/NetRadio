package manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface used to define a method as NetworkMethod.
 * 
 * @author Nejma Smatti
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface NetworkMethod {
	/**
	 * Name that is used to retrieve which method a client wants to call when we receive its packet.
	 * 
	 * @return Name of this NetworkMethod
	 */
	public String name();
	/**
	 * Weight that is used to avoid clients from flooding high consumption packets
	 * 
	 * @return Weight of this NetworkMethod
	 */
	public double weight() default 1;
}