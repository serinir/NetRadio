package manager.smartparser;

import java.util.HashMap;

import manager.smartparser.parseradapters.DoubleConverterAdapter;
import manager.smartparser.parseradapters.IntegerConverterAdapter;
import manager.smartparser.parseradapters.StringConverterAdapter;

/**
 * Class used to convert a string to a specific type.
 * @see ConverterAdapter
 * 
 * @author Nejma Smatti
 */
public final class SmartConverter {
	/**
	 * Variable that contains every {@link ConverterAdapter} and their binded types
	 */
	private static HashMap<Class<?>, ConverterAdapter<?>> converter_adapters = new HashMap<Class<?>, ConverterAdapter<?>>();
	
	/**
	 * Static method that register defaults {@link ConverterAdapter}
	 */
	static {
		register_adapter(Double.class, new DoubleConverterAdapter());
		register_adapter(double.class, new DoubleConverterAdapter());
		register_adapter(Integer.class, new IntegerConverterAdapter());
		register_adapter(int.class, new IntegerConverterAdapter());
		register_adapter(String.class, new StringConverterAdapter());
	}
	
	/**
	 * Method used to bind a {@link ConverterAdapter} to a type.
	 * 
	 * @param type The type of the object that should see this {@link ConverterAdapter} binded to it
	 * @param converter_adapter The {@link ConverterAdapter} that will be used each time we {@link #convert(Class, String)} an object of this type
	 */
	public static void register_adapter(Class<?> type, ConverterAdapter<?> converter_adapter) {
		converter_adapters.put(type, converter_adapter);
	}
	
	/**
	 * Method used to convert a string to its associated type.
	 * The specified type require a registered {@link ConverterAdapter} using {@link #register_adapter(Class, ConverterAdapter)}
	 * where class should be the same type as our type parameter.
	 *
	 * @param <T> The generic type of the object to be converted.
	 * @param type The type of the object to be converted.
	 * @param value The representation of the object to be converted as string.
	 * @return The converted value or null if no {@link ConverterAdapter} for type parameter has been found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Class<T> type, String value) {
		ConverterAdapter<T> converter_adapter = (ConverterAdapter<T>) converter_adapters.get(type);
		if(converter_adapter != null) {
			return converter_adapter.get_value(value);
		} else {
			return null;
		}
	}
}
