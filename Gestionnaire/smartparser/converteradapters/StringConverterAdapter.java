package manager.smartparser.converteradapters;

import manager.smartparser.ConverterAdapter;

/**
 * ConverterAdapter that convert String to String
 * 
 * @author Nejma Smatti
 */
public class StringConverterAdapter extends ConverterAdapter<String> {
	@Override
	public String get_value(String value) {
		return value;
	}
}
