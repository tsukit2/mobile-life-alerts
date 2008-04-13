package com.lifealert;

/**
 * Contain enums used for the Mobile Life Monitor app.
 * @author Chate Luu, Sukit Tretriluxana
 */
public class ActionStatusEnum {
	
	public enum Status { COMPLETED
						, FAILED
						, SKIPPED };
	public enum Actions { EMERGENCY_DETECTED
						, CALL_EMERGENCY_CONTACT
						, CALL_911
						, EMERGENCY_EMAIL_SENT };
}
