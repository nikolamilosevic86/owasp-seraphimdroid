package org.owasp.seraphimdroid.outgoingcalls;

public class CallAttempt {
	private String number;
	private boolean allowed;
	private long timestamp;

	CallAttempt(String number, boolean allowed) {
		this.number = number;
		this.allowed = allowed;
		this.timestamp = System.currentTimeMillis()/1000;
	}

	public String getNumber() {
		return number;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
