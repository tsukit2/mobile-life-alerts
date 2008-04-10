package com.lifealert.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IServiceManager;
import android.os.Message;
import android.os.ServiceManagerNative;
import android.telephony.IPhone;
import android.telephony.PhoneStateIntentReceiver;
import android.util.Log;
import android.widget.Toast;

import com.lifealert.ActionStatusEnum;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

public class CallForHelpActivity extends Activity {
	
	//State variable declarations
	private final int CALL_EMERGENCY_CONTACT = 0;
	private final int CALL_911_NUMBER = 1;
	private final int COMPLETED_CALLS = 2;
	private final int SEND_EMAIL = 3;
	private final int SHOW_SUMMARY = 4;
	
	//Variable declarations
	private int currentState;
	private final static int RECEIVER_NOTIFICATION_ID = 1;
	private PhoneStateIntentReceiver phoneStateIntentReceiver;
	private IServiceManager sm;
	private IPhone phoneService;
	String emergencyNumber;
	private boolean needToCall911 = false;
	private boolean calledEmergency = false;
	private static int idleCounter = 0; //Override with value from R class
	private int callEmergencyMax = 0; //Override with value from R class
	private int callCounter;
	private MediaPlayer player;
	private Bundle extras;
	
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
		
		//Get the Bundle extras
		extras = getIntent().getExtras();
		
		//Initialize the media player
		try {
			player = MediaPlayer.create(this, R.raw.help_voicemail);
	    } catch (Exception ex) {
	    	Log.e(getClass().getName(), ex.getMessage(), ex);
	        throw new RuntimeException(ex);
	    }
		
		//Initialize current state
		currentState = CALL_EMERGENCY_CONTACT;
			
		//Get the max number of times to call each emergency number
		callEmergencyMax = (new Integer(getString(R.string.call_emergency_max_conter)));
		
		//Register the PhoneStateIntentReceiver
		phoneStateIntentReceiver = new PhoneStateIntentReceiver(this, new ServiceStateHandler());
		phoneStateIntentReceiver.notifyPhoneCallState(RECEIVER_NOTIFICATION_ID);
		phoneStateIntentReceiver.registerIntent();

		//Prepare the dialer IPhone interface 
		sm = ServiceManagerNative.getDefault();
		
		//Set call 911 flag or assigned emergency number
		if (AppConfiguration.getCall911()) {
			needToCall911 = true;
		}
		
		//Reformat the phone number to the Phone Intent's preference
		emergencyNumber = formatPhoneNumber(AppConfiguration.getEmergencyPhone());
		
		//Make the first call
		callCounter = -1;
		idleCounter = 1;
		calledEmergency = true;
		handleNextCall(false);
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
		else {
		   phoneService = IPhone.Stub.asInterface(sm.getService("phone"));
		}

		if (!phoneService.isRadioOn()) {
			phoneService.toggleRadioOnOff();
		}

		phoneService.dial(number);
		phoneService.call(number);

		// now start the timing if need to
		idleCounter = 0;
		if (watchTime) {
			idleCounter = (new Integer(getString(R.string.call_emergency_idle_max_counter))).intValue();
	        idleHandler.removeMessages(idleHandler.obtainMessage().what);
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
	 * Go to the Send Email Activity screen.
	 */
	private void navigateToNextActivity() {
		if (currentState == COMPLETED_CALLS) {
			 
		     //Clean up everything
		     try {
		    	 phoneService.endCall(true);
		     }
			 catch (Exception ex) {
				 Log.e("Life", ex.getMessage(), ex);
			 }
			 idleHandler.removeMessages(idleHandler.obtainMessage().what);
		     phoneStateIntentReceiver.unregisterIntent();
		     player.release();
 		     
		     //Navigate over to send email activity
		     currentState = SEND_EMAIL;
		     Intent intent = new Intent(getApplication(), SendEmailActivity.class);
 		     
 		     intent.putExtras(extras);
		     startActivity(intent);
			 
		     //Make sure to finish this so it won't come back
		     finish();
		}
	}

	/**
	 * Play the emergency voice message the user recorded
	 */
	private void playEmergencyVoiceMessage() {
		player.seekTo(0);
		player.start();
	}

	/**
	 * Handle the logic on which number to call next
	 */
	protected void handleNextCall(boolean displayMsgBetweenCalls) {
		
		if (idleCounter > 0 && calledEmergency) {
			
			callCounter = callCounter + 1;
			calledEmergency=false;
			
			if (currentState == CALL_EMERGENCY_CONTACT) {
				extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT.toString()
						, ActionStatusEnum.Status.FAILED.toString());
			}
			else {
				extras.putString(ActionStatusEnum.Actions.CALL_911.toString()
						, ActionStatusEnum.Status.FAILED.toString());
			}
			
			//Set the next state if call counter max reached
			if (callCounter == callEmergencyMax) {
				if (currentState == CALL_EMERGENCY_CONTACT && needToCall911) {
					
					currentState = CALL_911_NUMBER;
					callCounter = 0;
				}
				else {
					currentState = COMPLETED_CALLS;	  
				}
			}
			
			//Check which number to call, depending on the current state
			if (currentState == CALL_911_NUMBER) {
				if (needToCall911) {
					//Call 911
					Toast.makeText(CallForHelpActivity.this, "Assuming the line is busy."
									+ " We next call 911.", Toast.LENGTH_SHORT).show();
					   
   					try {
   						callPhoneNumber(getString(R.string.phone_Number_911),true);
   						playEmergencyVoiceMessage();
   					} catch(Exception ex) {
   						Log.e(getClass().getName(), ex.getMessage(), ex);
   						extras.putString(ActionStatusEnum.Actions.CALL_911.toString()
   										, ActionStatusEnum.Status.FAILED.toString());
   					}
   					
   					extras.putString(ActionStatusEnum.Actions.CALL_911.toString()
									, ActionStatusEnum.Status.COMPLETED.toString());
				} else {
					currentState = COMPLETED_CALLS;
				}
			}
			else if (currentState == CALL_EMERGENCY_CONTACT) {
				
				//Call Emergency contact number
				if (displayMsgBetweenCalls) {
					Toast.makeText(CallForHelpActivity.this, "Assuming the line is busy."
						+ " We next call Emergency Contact " + emergencyNumber, Toast.LENGTH_SHORT).show();
				}
				
				try {
					callPhoneNumber(emergencyNumber, true);
					playEmergencyVoiceMessage();
				} catch(Exception ex) {
					Log.e(getClass().getName(), ex.getMessage(), ex);
					extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT.toString()
								, ActionStatusEnum.Status.FAILED.toString());
				}
				
				extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT.toString()
								, ActionStatusEnum.Status.COMPLETED.toString());
			}
			else {
				; //Do nothing
			}
		}
		else if (idleCounter <= 0 && calledEmergency) {
			currentState = COMPLETED_CALLS;
			navigateToNextActivity();
		}

	} //end handle911Call method
	
	
	/**
	 * ServiceStateHandler - private internal class
	 * @author Chate Luu
	 * A class for handling the PhoneStateIntentReceiver notifications
	 */
	private class ServiceStateHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
				case RECEIVER_NOTIFICATION_ID:	
					//Detect phone state changes  
					switch (phoneStateIntentReceiver.getPhoneState()) {
						case OFFHOOK:
							Log.d(getClass().getName(), "****Phone OFFHOOK!");
							calledEmergency = true;
							break;
						case RINGING:
							Log.d(getClass().getName(), "****Phone RINGING!");
							break;
						case IDLE:
							Log.d(getClass().getName(), "****Phone IDLE!");
							handleNextCall(true);
							navigateToNextActivity();
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