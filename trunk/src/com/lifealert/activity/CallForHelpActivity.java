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

/**
 * This class makes the calls to the emergency contact and 911 (if needed).
 * Navigate to the SendEmail activity screen next.
 * 
 * @author Chate Luu, Sukit Tretriluxana
 */
public class CallForHelpActivity extends Activity {

   // State variable declarations
   private final int CALL_EMERGENCY_CONTACT = 0;
   private final int CALL_911_NUMBER = 1;
   private final int COMPLETED_CALLS = 2;
   private final int SEND_EMAIL = 3;
   private final int SHOW_SUMMARY = 4;

   // Variable declarations
   private int currentState;
   private final static int RECEIVER_NOTIFICATION_ID = 1;
   private PhoneStateIntentReceiver phoneStateIntentReceiver;
   private IServiceManager sm;
   private IPhone phoneService;
   String emergencyNumber;
   private boolean needToCall911 = false;
   private static int idleCounter = 0; // Override with value from R class
   private int callMaxCounter = 0; // Override with value from R class
   private int callCounter;
   private MediaPlayer player;
   private Bundle extras;
   private boolean callMade;

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

      // Get the Bundle extras
      extras = getIntent().getExtras();
      if (extras == null)
         extras = new Bundle();

      // Initialize the media player
      try {
         player = MediaPlayer.create(this, R.raw.help_voicemail);
         player.setLooping(1);
      } catch (Exception ex) {
         Log.e(getClass().getName(), ex.getMessage(), ex);
         throw new RuntimeException(ex);
      }

      // Initialize current state
      currentState = CALL_EMERGENCY_CONTACT;

      // Get the max number of times to call each emergency number
      callMaxCounter = (new Integer(getString(R.string.call_max_conter)));

      // Register the PhoneStateIntentReceiver
      phoneStateIntentReceiver = new PhoneStateIntentReceiver(this,
            new ServiceStateHandler());
      phoneStateIntentReceiver.notifyPhoneCallState(RECEIVER_NOTIFICATION_ID);
      phoneStateIntentReceiver.registerIntent();

      // Prepare the dialer IPhone interface
      sm = ServiceManagerNative.getDefault();

      // Set call 911 flag or assigned emergency number
      if (AppConfiguration.getCall911()) {
         needToCall911 = true;
      }

      // Reformat the phone number to the Phone Intent's preference
      emergencyNumber = formatPhoneNumber(AppConfiguration.getEmergencyPhone());

      // Make the first call
      callCounter = -1;
      idleCounter = 1;
      handleNextCall();
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
      // ShakeDetectorService.setOnHold(false);
   }

   @Override
   protected void onDestroy() {
      // put the service off hold
      ShakeDetectorService.setOnHold(false);
      super.onDestroy();
   }
   
   /**
    * Call the phone number in the input
    * 
    * @param number
    * @throws Exception
    */
   private void callPhoneNumber(String number) throws Exception {
      if (phoneService != null) {
         phoneService.endCall(true);
      } else {
         phoneService = IPhone.Stub.asInterface(sm.getService("phone"));
      }

      if (!phoneService.isRadioOn()) {
         phoneService.toggleRadioOnOff();
      }

      // phoneService.dial(number);
      callMade = false;
      phoneService.call(number);

      // now start the timing if need to
      idleCounter = (new Integer(
            getString(R.string.call_emergency_idle_max_counter))).intValue();
      idleHandler.removeMessages(idleHandler.obtainMessage().what);
      idleHandler.sendMessageDelayed(idleHandler.obtainMessage(), 1000);
   }

   /**
    * Format the phone number and remove all characters that are not integer.
    * Return back a string containing only integers, since the IPhone interface
    * needs to dial the number in such format
    * 
    * @param originalNumber
    * @return String of integers
    */
   private String formatPhoneNumber(String originalNumber) {
      String newNumber = "";

      Integer integer;
      for (int i = 0; i < originalNumber.length(); i++) {
         try {
            integer = new Integer(originalNumber.substring(i, i + 1));
            newNumber = newNumber + integer.intValue();
         } catch (NumberFormatException e) {
            ; // do nothing
         }
      }

      return newNumber;
   }

   /**
    * Go to the Send Email Activity screen.
    */
   private void navigateToNextActivity() {
      switch (currentState) {
      case CALL_EMERGENCY_CONTACT:
         try {
            callPhoneNumber(emergencyNumber);
         } catch (Exception ex) {
            Log.e(getClass().getName(), ex.getMessage(), ex);
            extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT
                  .toString(), ActionStatusEnum.Status.FAILED.toString());
         }
         break;

      case CALL_911_NUMBER:
         try {
            callPhoneNumber(getString(R.string.phone_Number_911));
         } catch (Exception ex) {
            Log.e(getClass().getName(), ex.getMessage(), ex);
            extras.putString(ActionStatusEnum.Actions.CALL_911.toString(),
                  ActionStatusEnum.Status.FAILED.toString());
         }
         break;

      case COMPLETED_CALLS:
         // Clean up everything
         try {
            phoneService.endCall(true);
         } catch (Exception ex) {
            Log.e("Life", ex.getMessage(), ex);
         }
         idleHandler.removeMessages(idleHandler.obtainMessage().what);
         phoneStateIntentReceiver.unregisterIntent();
         player.release();

         // Navigate over to send email activity
         currentState = SEND_EMAIL;
         Intent intent = new Intent(getApplication(), SendEmailActivity.class);

         if (extras.size() > 0) intent.putExtras(extras);
         startActivity(intent);

         // Make sure to finish this so it won't come back
         finish();
         break;
      }
   }

   /**
    * Play the emergency voice message the user recorded
    */
   private void playEmergencyVoiceMessage() {
      if (!player.isPlaying()) {
         player.start();
      }
   }

   private void stopPlayingEmergencyVoiceMessage() {
      if (player.isPlaying()) {
         player.pause();
      }
   }

   /**
    * Handle the logic on which number to call next
    */
   protected void handleNextCall() {
      switch (currentState) {
      case CALL_EMERGENCY_CONTACT:
         if (idleCounter > 0) {
            callCounter++;
            if (callCounter >= callMaxCounter) {
               extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT
                     .toString(), ActionStatusEnum.Status.FAILED.toString());
               if (needToCall911) {
                  callCounter = 0;
                  currentState = CALL_911_NUMBER;
                  Toast.makeText(CallForHelpActivity.this,
                        "Assuming the line is busy." + " We next call 911.",
                        Toast.LENGTH_SHORT).show();
               } else {
                  currentState = COMPLETED_CALLS;
               }
            } else if (callCounter > 0) {
               Toast.makeText(
                     CallForHelpActivity.this,
                     "Assuming the line is busy. Try calling the emergency contact again: "
                           + emergencyNumber, Toast.LENGTH_SHORT).show();
            }
         } else {
            extras.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT
                  .toString(), ActionStatusEnum.Status.COMPLETED.toString());
            currentState = COMPLETED_CALLS;
         }
         break;

      case CALL_911_NUMBER:
         if (idleCounter > 0) {
            callCounter++;
            if (callCounter >= callMaxCounter) {
               extras.putString(ActionStatusEnum.Actions.CALL_911.toString(),
                     ActionStatusEnum.Status.FAILED.toString());
               currentState = COMPLETED_CALLS;
            } else if (callCounter > 0) {
               Toast.makeText(CallForHelpActivity.this,
                     "Assuming the line is busy. Try calling 911 again.",
                     Toast.LENGTH_SHORT).show();
            }
         } else {
            extras.putString(ActionStatusEnum.Actions.CALL_911.toString(),
                  ActionStatusEnum.Status.COMPLETED.toString());
            currentState = COMPLETED_CALLS;
         }
         break;
      }

      // take the action based on the state
      navigateToNextActivity();
   }

   /**
    * ServiceStateHandler - private internal class
    * 
    * @author Chate Luu A class for handling the PhoneStateIntentReceiver
    *         notifications
    */
   private class ServiceStateHandler extends Handler {

      @Override
      public void handleMessage(Message msg) {

         switch (msg.what) {
         case RECEIVER_NOTIFICATION_ID:
            // Detect phone state changes
            switch (phoneStateIntentReceiver.getPhoneState()) {
            case OFFHOOK:
               Log.d(getClass().getName(), "****Phone OFFHOOK!");
               // Toast.makeText(CallForHelpActivity.this, "Off hook",
               // Toast.LENGTH_SHORT);
               if (!callMade) {
                  callMade = true;
                  playEmergencyVoiceMessage();
               }
               break;
            case RINGING:
               Log.d(getClass().getName(), "****Phone RINGING!");
               break;
            case IDLE:
               Log.d(getClass().getName(), "****Phone IDLE!");
               if (callMade) {
                  callMade = false;
                  stopPlayingEmergencyVoiceMessage();
                  handleNextCall();
               }
               break;
            default:
               Log.d(getClass().getName(), "****Some unknown phone state!");
               break;
            } // end inner switch
            break;
         } // end outer switch
      } // end handleMessage method
   } // end inner class

}