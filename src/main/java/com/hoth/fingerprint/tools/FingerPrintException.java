package com.hoth.fingerprint.tools;

public class FingerPrintException extends Exception {

	private static final long serialVersionUID = -829097821834065662L;
	
	public FingerPrintException() {
		super();
	}
 	
	public FingerPrintException(String message) {
		super(message);
	}
	
	public FingerPrintException(Throwable cause) {
		super(cause);
	}
	
	public FingerPrintException(String message, Throwable cause) {
		super(message, cause);
	}
}
