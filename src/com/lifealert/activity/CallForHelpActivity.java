package com.lifealert.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.IServiceManager;
import android.os.Message;
import android.os.ServiceManagerNative;
import android.telephony.IPhone;
import android.telephony.PhoneStateIntentReceiver;
import android.util.Log;
import android.widget.Toast;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

public class CallForHelpActivity extends Activity {
	
	//Variable declarations
	private final static int RECEIVER_NOTIFICATION_ID = 1;
	private PhoneStateIntentReceiver phoneStateIntentReceiver;
	private IServiceManager sm;
	private IPhone phoneService;
	private boolean call911Next = false;
	private boolean called911 = false;
	private boolean calledEmergency = false;
	private static int idleCounter = 0;

	private Handler idleHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// update time left
			--idleCounter;

			// then determine if we need to do it again
			if (idleCounter > 0) {
				sendMessageDelayed(obtainMessage(), 1000);
			} 
		}
	};
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.callhelp);

		//Register the PhoneStateIntentReceiver
		phoneStateIntentReceiver = new PhoneStateIntentReceiver(this, new ServiceStateHandler());
		phoneStateIntentReceiver.notifyPhoneCallState(RECEIVER_NOTIFICATION_ID);
		phoneStateIntentReceiver.registerIntent();

		// let's make a phone call now
		try {

			//Prepare the dialer IPhone interface 
			sm = ServiceManagerNative.getDefault();


			//Set call 911 flag or assigned emergency number
			if (AppConfiguration.getCall911()) {
				call911Next = true;
			}

			//Call emergency number
			String emergencyNumber = formatPhoneNumber(AppConfiguration.getEmergencyPhone());         
			callPhoneNumber(emergencyNumber, true);

		} catch (Exception ex) {
			Log.e("Life", ex.getMessage(), ex);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// put the service on hold
		ShakeDetectorService.setOnHold(true);
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		// put the service off hold
		ShakeDetectorService.setOnHold(false);
	}

	/**
	 * Call the phone number in the input
	 * @param number
	 * @throws Exception
	 */
	private void callPhoneNumber(String number, boolean watchTime) throws Exception {
		if (phoneService != null) {
			phoneService.endCall(true);
		}

		phoneService = IPhone.Stub.asInterface(sm.getService("phone"));

		if (!phoneService.isRadioOn()) {
			phoneService.toggleRadioOnOff();
		}

		phoneService.dial(number);
		phoneService.call(number);

		// now start the timing if need to
		idleCounter = 0;
		if (watchTime) {
			idleCounter = 30;
			idleHandler.sendMessageDelayed(idleHandler.obtainMessage(), 1000);
		}
	}

	/**
	 * Format the phone number and remove all characters that are not integer.
	 * Return back a string containing only integers, since the IPhone interface
	 * needs to dial the number in such format
	 * @param originalNumber
	 * @return String of integers
	 */
	private String formatPhoneNumber(String originalNumber) {
		String newNumber = "";

		Integer integer;
		for (int i = 0; i < originalNumber.length(); i++) {
			try {
				integer = new Integer(originalNumber.substring(i, i+1));
				newNumber = newNumber + integer.intValue();
			}
			catch(NumberFormatException e) {
				; //do nothing
			}
		}

		return newNumber;
	}	

	/**
	 * ServiceStateHandler - private internal class
	 * @author Chate Luu
	 * A class for handling the PhoneStateIntentReceiver notifications
	 */
	private class ServiceStateHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			Log.e("PhoneCallStateNotified", msg.toString()); 

			switch (msg.what) {
				case RECEIVER_NOTIFICATION_ID:
	
					//Detect phone state changes  
					switch (phoneStateIntentReceiver.getPhoneState()) {
						case OFFHOOK:
							Log.d(getClass().getName(), "****Phone picked up!");
							calledEmergency = true;
							
							/*
							//Calling 911 if phone is picked up or hanged up
							if (call911Next && !called911) {
								try {
									Thread.sleep(20000);
				
									//So that 911 won't get called again
									call911Next = false; 
									called911 = true;
				
									callPhoneNumber(getString(R.string.phone_Number_911));
								}
								catch(Exception ex) {
									Log.e("Life", ex.getMessage(), ex);
								}
							}
							*/
		
							break;
						case RINGING:
							break;
						case IDLE:
							Log.d(getClass().getName(), "****Phone idle!");
		
							if (idleCounter > 0 && calledEmergency) {
								if (call911Next && !called911) {
									Toast.makeText(CallForHelpActivity.this, "Assuming the line is busy, we next call 911", Toast.LENGTH_SHORT).show();
		
									// So that 911 won't get called again
									call911Next = false;
									called911 = true;
		
									try {
										callPhoneNumber(getString(R.string.phone_Number_911),
												false);
									} catch(Exception ex) {
										Log.e("Life", ex.getMessage(), ex);
									}
								}
							}
		
							break;
						default:
							Log.d(getClass().getName(), "****Some unknown phone state!");
						break;
						} //end inner switch
					break;
				default:
					break;
			}  //end outer switch
			
		} //end handleMessage method
		
	} //end inner class  

}