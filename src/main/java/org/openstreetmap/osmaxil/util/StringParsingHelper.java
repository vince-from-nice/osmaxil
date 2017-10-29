package org.openstreetmap.osmaxil.util;

import org.apache.log4j.Logger;
import org.openstreetmap.osmaxil.Application;

public class StringParsingHelper {

	static protected final Logger LOGGER = Logger.getLogger(Application.class);

	// Stupid but useful methods for catching and logging exception individually

	static public double parseDouble(String s, String name) {
		double result = 0;
		try {
			result = Double.parseDouble(s);
		} catch (Exception e) {
			LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
		}
		return result;
	}

	static public int parseInt(String s, String name) {
		int result = 0;
		try {
			result = Integer.parseInt(s);
		} catch (Exception e) {
			LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
		}
		return result;
	}

	static public float parseFloat(String s, String name) {
		float result = 0;
		try {
			result = Float.parseFloat(s);
		} catch (Exception e) {
			LOGGER.warn("Unable to parse " + name + ": " + e.getMessage());
		}
		return result;
	}
}
