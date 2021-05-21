package manager.smartparser;

/**
 * Class used to convert a string to a specific type.
 * 
 * @author Nejma Smatti
 *
 * @param <T> The generic type in which our string should be converted in.
 */
public abstract class ConverterAdapter<T> {
	/**
	 * Method that returns the value of the specified string.
	 * 
	 * @param value Value that should get converted.
	 * @return The converted value.
	 */
	public abstract T get_value(String value);
}
