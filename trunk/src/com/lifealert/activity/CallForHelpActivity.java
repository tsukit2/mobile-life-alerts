package com.lifealert.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.IServiceManager;
import android.os.ServiceManagerNative;
import android.telephony.IPhone;
import android.util.Log;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

public class CallForHelpActivity extends Activity {

   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      setContentView(R.layout.callhelp);

      // let's make a phone call now
      try {
         IServiceManager sm = ServiceManagerNative.getDefault();
         IPhone phoneService = IPhone.Stub.asInterface(sm.getService("phone"));
         
         if (!phoneService.isRadioOn()) {
            phoneService.toggleRadioOnOff();
         }
         
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
