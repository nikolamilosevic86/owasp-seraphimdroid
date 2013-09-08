package org.owasp.seraphimdroid.outgoingcalls;

import java.util.ArrayList;

/**
 * Prototype database for call logs
 */
public class CallLogPrototype {
	private static ArrayList<CallAttempt> callAttempts = new ArrayList<CallAttempt>();

	private CallLogPrototype() {}

	public static void addCallAttempt(CallAttempt call) {
		callAttempts.add(call);
	}

	public static CallAttempt getLastCallAttempt() {
		if (!callAttempts.isEmpty()) {
			return callAttempts.get(callAttempts.size() - 1);
		}
		return null;
	}
}
