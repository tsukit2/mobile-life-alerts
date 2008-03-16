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

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

public class CallForHelpActivity extends Activity {
   
   private final static int RECEIVER_NOTIFICATION_ID = 1;

   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      setContentView(R.layout.callhelp);

      //Register the PhoneStateIntentReceiver
      //TODO: Not sure if this registration needs to be go before or after the phone number dialing/calling
      PhoneStateIntentReceiver phoneStateIntentReceiver = new PhoneStateIntentReceiver();

      Handler serviceStateHandler = new Handler() {
           @Override
           public void handleMessage(Message msg) {
        	  Log.e("PhoneCallStateNotified", msg.toString()); 
              
              switch (msg.what) {
              	 case RECEIVER_NOTIFICATION_ID:
              	 	//TODO: Do something here 
              		//(e.g. detect what state change and react to it)
              		break;
                 default:
                 	break;
        	  }
           }
      };

      phoneStateIntentReceiver = new PhoneStateIntentReceiver(this, serviceStateHandler);
      phoneStateIntentReceiver.notifyPhoneCallState(RECEIVER_NOTIFICATION_ID);
      /*
       TODO:  We can also get notification of service state changes or signal strength changes if needed 
      phoneStateIntentReceiver.notifyServiceState(RECEIVER_NOTIFICATION_ID);
      phoneStateIntentReceiver.notifySignalStrength(RECEIVER_NOTIFICATION_ID);
      */
      phoneStateIntentReceiver.registerIntent();
      
      // let's make a phone call now
      try {
    	  
    	 //Prepare the dialer IPhone interface 
    	 IServiceManager sm = ServiceManagerNative.getDefault();
         IPhone phoneService = IPhone.Stub.asInterface(sm.getService("phone"));
         
         if (!phoneService.isRadioOn()) {
            phoneService.toggleRadioOnOff();
         }
         
         //Call either 911 or assigned emergency number
         String emergencyNumber;
         if (AppConfiguration.getCall911()) {
        	 emergencyNumber = getString(R.string.phone_Number_911);
         }
         else {
        	 emergencyNumber = AppConfiguration.getEmergencyPhone();
         }
         
         phoneService.dial(emergencyNumber);
         phoneService.call(emergencyNumber);
         
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

}
