package manager.smartparser.converteradapters;

import manager.smartparser.ConverterAdapter;

/**
 * ConverterAdapter that convert String to Double
 * 
 * @author Nejma Smatti
 */
public class DoubleConverterAdapter extends ConverterAdapter<Double> {
	@Override
	public Double get_value(String value) {
		return Double.parseDouble(value);
	}
}
