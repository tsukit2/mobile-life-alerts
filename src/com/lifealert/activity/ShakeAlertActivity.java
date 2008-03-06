package com.lifealert.activity;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

import android.app.Activity;
import android.content.Intent;
import android.net.ContentURI;
import android.os.Bundle;
import android.provider.Contacts;

public class ShakeAlertActivity extends Activity {

   @Override
   protected void onCreate(Bundle icicle) {
      // let the parent go first
      super.onCreate(icicle);
//      setContentView(R.layout.alert);
      
      // TODO: need more logic to confirm with the user first before making
      // the actual call. For now, simply call it
      callEmergencyNumber();
   }

   @Override
   protected void onStart() {
      ShakeDetectorService.setOnHold(true);
      super.onStart();
   }

   @Override
   protected void onStop() {
      ShakeDetectorService.setOnHold(false);
      super.onStop();
   }

   /**
    * Navigate to the Dialer activity screen and call the emergency contact
    * TODO: Need to figure out how to play the recorded audio over the phone.
    */
   private void callEmergencyNumber() {
      //Get the emergency contact from the AppConfiguration 
        Long personToContact = AppConfiguration.getEmergencyContactId();
        
        if (personToContact == null) {
         // Set contact to user of this phone, if emergency contact doesn't exist
         personToContact = AppConfiguration.getUserContactId();
        }
        
     //Dial the assigned emergency contact number
      Intent intent = new Intent(android.content.Intent.CALL_ACTION);
      ContentURI phoneURIString = Contacts.Phones.CONTENT_URI;
      phoneURIString = phoneURIString.addId(personToContact);
      
      intent.setData(phoneURIString);
      startActivity(intent);
      
   }   
}
