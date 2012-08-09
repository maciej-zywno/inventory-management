package com.ebayinventory.converter;

public class StringArrayUtils {

	public static boolean isEmpty(String[] line) {
		return line.length == 0 || allEmptyString(line);
	}

	public static boolean allEmptyString(String[] line) {
		for (String string : line) {
			if (string != null && !string.isEmpty()) {
				return false;
			}
		}
		return true;
	}

}
