package com.lifealert.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ContentURI;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

public class ShakeAlertActivity extends Activity {
   private int timeLeft;
   private boolean okay;

   @Override
   protected void onCreate(Bundle icicle) {
      // let the parent go first
      super.onCreate(icicle);
      setContentView(R.layout.alert);
      
      // wire the button click
      ((Button) findViewById(R.id.alert_okay_button)).setOnClickListener(onOkayClicked);
   }
   
   private void updateTimeLeft() {
      // update the screen
      ((TextView) findViewById(R.id.alert_timeleft)).setText(String.valueOf(timeLeft));
      
      // play sound every 10 second
      if (timeLeft % 10 == 0 && timeLeft > 0) {
         playSound();
      }
   }

   @Override
   protected void onStart() {
      // let the parent start first 
      super.onStart();
      
      // put the service on hold
      ShakeDetectorService.setOnHold(true);

      // initialize the time
      timeLeft = 30;
      okay = false;
      updateTimeLeft();
      
      // now start the timing
      handler.sendMessageDelayed(handler.obtainMessage(), 1000);
   }

   private void playSound() {
      // call out to the user
      MediaPlayer mp = MediaPlayer.create(getApplication(), R.raw.alertvoice);
      mp.prepare();
      mp.start();
   }

   @Override
   protected void onStop() {
      // if somehow we get here not from the button, we make sure that we stop the timer
      // as well and assume that the user is okay
      okay = true;
      
      // then let the service continue one
      ShakeDetectorService.setOnHold(false);
      super.onStop();
   }
   
   private OnClickListener onOkayClicked = new OnClickListener() {
      @Override
      public void onClick(View view) {
         // mark that we're okay now
         okay = true;
   
         // notify user
         NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notMan.notifyWithText(R.string.config_is_incomplete,
               getText(R.string.alert_okay_notification),
               NotificationManager.LENGTH_SHORT,
               null);
         
         // finish this activity
         finish();
      }
   };
   
   private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         // update time left
         --timeLeft;
         updateTimeLeft();
         
         // then determine if we need to do it again
         if (!okay) {
            if (timeLeft > 0) {
               sendMessageDelayed(obtainMessage(), 1000);
            } else {
               // let's the ball rolling to call for help
               Intent intent = new Intent(getApplication(), CallForHelpActivity.class);
               startActivity(intent);
            }
         }
      }
   };

   /**
    * Navigate to the Dialer activity screen and call the emergency contact
    * TODO: Need to figure out how to play the recorded audio over the phone.
    */
   private void callEmergencyNumber() {
      // Get the emergency contact from the AppConfiguration
      Long personToContact = AppConfiguration.getEmergencyContactId();

      if (personToContact == null) {
         // Set contact to user of this phone, if emergency contact doesn't
         // exist
         personToContact = AppConfiguration.getUserContactId();
      }

      // Dial the assigned emergency contact number
      Intent intent = new Intent(android.content.Intent.CALL_ACTION);
      ContentURI phoneURIString = Contacts.Phones.CONTENT_URI;
      phoneURIString = phoneURIString.addId(personToContact);

      intent.setData(phoneURIString);
      startActivity(intent);

   }
}
