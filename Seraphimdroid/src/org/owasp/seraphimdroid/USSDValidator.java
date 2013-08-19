package org.owasp.seraphimdroid;

import java.util.Arrays;
import java.util.List;

public class USSDValidator {

	private static List<String> harmfulCodes = Arrays.asList(
			"*#7780#", "*#7780%23",//Factory Reset
			"*2767*3855#", "*2767*3855%23", //Full Factory Reset
			"*#*#7780#*#*"  // Factory data reset
	);

	public static boolean isSafeUssd(String number) {
		return harmfulCodes.contains(number) ? false : true;
	}

}
