package com.compiler.util;

import org.apache.commons.lang3.StringUtils;

import com.compiler.constants.Constants;

public class ParameterValidationUtil {

	public static boolean isParameterFormatted(String parameter) {
		if (StringUtils.containsWhitespace(parameter)) {
			return false;
		}
		// null , "" and " " return true
		if (StringUtils.isBlank(parameter)) {
			return false;
		}
		if (parameter.length() < Constants.SHORTEST_PARAMETER_SIZE
				|| parameter.length() > Constants.LONGEST_PARAMETER_SIZE) {
			return false;
		}
		return true;
	}

	// it will return true if it is alphanumeric else it will return false
	public static boolean isAlphaNumeric(String s) {
		String pattern = "^[a-zA-Z0-9]*$";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}

	public static boolean isMailAddressFormatted(String parameter) {
		if (StringUtils.containsWhitespace(parameter)) {
			return false;
		}
		// null , "" and " " return true
		if (StringUtils.isBlank(parameter)) {
			return false;
		}
		return true;
	}
}
