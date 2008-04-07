package com.lifealert;

public class ActionStatusEnum {
	
	//Possible enums
	public enum Status { COMPLETED
						, FAILED
						, SKIPPED };
	public enum Actions { EMERGENCY_DETECTED
						, CALL_EMERGENCY_CONTACT
						, CALL_911
						, EMERGENCY_EMAIL_SENT };
}
