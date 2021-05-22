package manager.smartparser.converteradapters;

import manager.smartparser.ConverterAdapter;

/**
 * ConverterAdapter that convert String to Integer
 * 
 * @author Nejma Smatti
 */
public class IntegerConverterAdapter extends ConverterAdapter<Integer> {
	@Override
	public Integer get_value(String value) {
		return Integer.parseInt(value);
	}
}
