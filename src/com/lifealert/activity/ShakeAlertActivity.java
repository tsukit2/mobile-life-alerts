package com.lifealert.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lifealert.ActionStatusEnum;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

/**
 * Activity prompted the user to confirm whether 
 * an actual emergency situation has occurred or not.
 * If not, navigate back to what the user was doing and
 * place MLM application back to background monitoring mode.
 * If yes, start of the emergency contacting procedures.
 * @author Chate Luu, Sukit Tretriluxana
 */
public class ShakeAlertActivity extends Activity {
   private int timeLeft;
   private boolean okay;
   private MediaPlayer player;

   @Override
   protected void onCreate(Bundle icicle) {
      // let the parent go first
      super.onCreate(icicle);
      setContentView(R.layout.alert);
      
      // wire the button click
      Button okayButton = (Button) findViewById(R.id.alert_okay_button);
      okayButton.setOnClickListener(onOkayClicked);
      okayButton.requestFocus();
      
      // load the file
      try {
         player = MediaPlayer.create(this, R.raw.alertvoice);
      } catch (Exception ex) {
         Log.e("Life", ex.getMessage(), ex);
         throw new RuntimeException(ex);
      }
   }

   @Override
   protected void onDestroy() {
      player.release();
      super.onDestroy();
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
      timeLeft = (new Integer(getString(R.string.alert_time_left)));
      okay = false;
      updateTimeLeft();
      
      // now start the timing
      handler.sendMessageDelayed(handler.obtainMessage(), 1000);
   }

   private void playSound() {
      // call out to the user
      player.seekTo(0);
      player.start();
   }

   @Override
   protected void onStop() {
      // if somehow we get here not from the button, we make sure that we stop the timer
      // as well and assume that the user is okay
      okay = true;
      
      // then let the service continue one
      ShakeDetectorService.setOnHold(false);
      super.onStop();
      
      // then finish it
      finish();
   }
   
   private OnClickListener onOkayClicked = new OnClickListener() {
      @Override
      public void onClick(View view) {
         // mark that we're okay now
         okay = true;
   
         // notify user
         Toast.makeText(ShakeAlertActivity.this, R.string.alert_okay_notification, Toast.LENGTH_SHORT).show();
         
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
               intent.putExtras(initializeActionStatusBundle());
               startActivity(intent);
               finish();
            }
         }
      }

   };
   

	private Bundle initializeActionStatusBundle() {
		Bundle bundle = new Bundle();
		bundle.putString(ActionStatusEnum.Actions.EMERGENCY_DETECTED.toString()
						, ActionStatusEnum.Status.COMPLETED.toString());
		bundle.putString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT.toString()
						, ActionStatusEnum.Status.SKIPPED.toString());
		bundle.putString(ActionStatusEnum.Actions.CALL_911.toString()
   	    				, ActionStatusEnum.Status.SKIPPED.toString());
		bundle.putString(ActionStatusEnum.Actions.EMERGENCY_EMAIL_SENT.toString()
						, ActionStatusEnum.Status.SKIPPED.toString());
		
		return bundle;
	}
}
