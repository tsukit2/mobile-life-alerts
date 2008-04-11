package com.lifealert.activity;

import org.openintents.hardware.Sensors;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.config.Sensitivity;
import com.lifealert.service.ShakeDetectorService;

public class SensitivityTestActivity extends Activity implements Runnable {

   private static final int[] SENSITIVITY_BARS = { R.id.sensitivity_bar_0,
         R.id.sensitivity_bar_1, R.id.sensitivity_bar_2,
         R.id.sensitivity_bar_3, R.id.sensitivity_bar_4,
         R.id.sensitivity_bar_5, R.id.sensitivity_bar_6,
         R.id.sensitivity_bar_7, R.id.sensitivity_bar_8,
         R.id.sensitivity_bar_9, R.id.sensitivity_bar_10, };

   private static final int[] SENSITIVITY_BAR_COLORS = {
         R.color.sensitivity_bar_0, R.color.sensitivity_bar_1,
         R.color.sensitivity_bar_2, R.color.sensitivity_bar_3,
         R.color.sensitivity_bar_4, R.color.sensitivity_bar_5,
         R.color.sensitivity_bar_6, R.color.sensitivity_bar_7,
         R.color.sensitivity_bar_8, R.color.sensitivity_bar_9,
         R.color.sensitivity_bar_10, };

   private static final int[] SENSITIVITY_RADIOS = {
         R.id.sensitivity_verysensitive, R.id.sensitivity_sensitive,
         R.id.sensitivity_normal, R.id.sensitivity_somewhat,
         R.id.sensitivity_notsensitive, };

   private View[] bars;
   private boolean testing;
   private RadioButton[] rbuttons;
   private Sensitivity curSensitivity;
   private LinearLayout statusText;
   private Thread thread;


   @Override
   protected void onCreate(Bundle icicle) {
      // set the content view
      super.onCreate(icicle);
      setContentView(R.layout.sensitivity);
      
      // initialize the bar references so it's faster to access
      bars = new View[11];
      for (int i = 0; i < bars.length; ++i) {
         bars[i] = findViewById(SENSITIVITY_BARS[i]);
      }

      // initialize the rbutton so we can control the check of the button
      rbuttons = new RadioButton[5];
      for (int i = 0; i < rbuttons.length; ++i) {
         rbuttons[i] = (RadioButton) findViewById(SENSITIVITY_RADIOS[i]);
      }

      // get a hold of the status status panel
      statusText = (LinearLayout) findViewById(R.id.sensitivity_status);

      // make selection. Choose normal if if it's not already there
      curSensitivity = AppConfiguration.getSensitivity();
      if (curSensitivity == null) {
         AppConfiguration.setSensitivity(curSensitivity = Sensitivity.NORMAL);
      }
      setRadioButtons(curSensitivity);

      // reset bar color
      resetBarColor();

      // wire the event handler
      for (int i = 0; i < rbuttons.length; ++i) {
         rbuttons[i].setOnClickListener(onRadioClicked);
      }

      Button testButton = (Button) findViewById(R.id.sensitivity_testagain);
      testButton.setOnClickListener(onTestClicked);

      // now start testing
      startTesting();
   }
   
   @Override
   protected void onStart() {
      super.onStart();
      startTestingThread();
   }

   @Override
   protected void onStop() {
      super.onStop();
      stopTestingThread();
   }

   private OnClickListener onRadioClicked = new OnClickListener() {
      private boolean clicking;

      public void onClick(View view) {
         if (!clicking) {
            clicking = true;
            for (int i = 0; i < rbuttons.length; ++i) {
               if (rbuttons[i] == view) {
                  // set the sensitivity
                  AppConfiguration.setSensitivity(curSensitivity = Sensitivity
                        .values()[i]);
                  rbuttons[i].requestFocus();
                  
                  // update the shake detector
                  ShakeDetectorService.setSensitivity(curSensitivity);
                  
                  // this is unfortunate that we have to do this
                  if (!rbuttons[i].isChecked()) {
                     rbuttons[i].setChecked(true);
                  }
               } else {
                  rbuttons[i].setChecked(false);
               }
            }
            clicking = false;
         }
      }
   };

   private OnClickListener onTestClicked = new OnClickListener() {
      public void onClick(View view) {
         startTesting();
      }
   };

   private void setRadioButtons(Sensitivity sen) {
      for (int i = 0; i < rbuttons.length; ++i) {
         if (i == sen.ordinal()) {
            rbuttons[i].setChecked(true);
            rbuttons[i].requestFocus();
         } else {
            rbuttons[i].setChecked(false);
         }
      }
   }

   private void startTesting() {
      // notify the user first
      Toast.makeText(this, R.string.sensitivity_testing_insession, Toast.LENGTH_SHORT).show();
//      NotificationManager man = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//      man.notifyWithText(R.string.sensitivity_testing_insession,
//            getString(R.string.sensitivity_testing_insession),
//            NotificationManager.LENGTH_SHORT, null);
      
      // turn off the status
      statusText.setVisibility(View.INVISIBLE);
   }
   
   private void startTestingThread() {
      testing = true;
      thread = new Thread(this);
      thread.start();
   }
   
   private void stopTestingThread() {
      testing = false;
      try {
         if (thread != null) thread.join();
      } catch (InterruptedException e) {
         Log.e("Life", e.getMessage(), e);
         throw new RuntimeException(e);
      }
   }
   
   private void testSatisfied() {
      // then start the testing session
      statusText.setVisibility(View.VISIBLE);
   }

   private void resetBarColor() {
      // reset the bar colors
      for (int i = 0; i < bars.length; ++i) {
         bars[i].setBackground(R.color.sensitivity_bar_inactive);
         bars[i].invalidate();
      }
   }

   private void updateBarColor(int barVal) {
      // set the bar color if it's equal or beyond the given bar val
      for (int i = 0; i < bars.length; ++i) {
         bars[i].setBackground(i < barVal ? SENSITIVITY_BAR_COLORS[i]
               : R.color.sensitivity_bar_inactive);
         bars[i].invalidate();
      }
   }

   private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         // update the bar color
         updateBarColor(msg.what);

         // check if the sensitivity is met
         if ((msg.what - 3) / 2 >= curSensitivity.ordinal()) {
            Log.e("***", msg.what + ", " + curSensitivity.ordinal(), null);
            testSatisfied();
         }
      }
   };

   @Override
   public void run() {
      try {
         // put the shake alert on hold
         ShakeDetectorService.setOnHold(true);
         
         // draw the value from the accelerometer
         float[][] data = new float[2][3];
         int prevBarVal = 0;
         for (int x1 = 0, x2 = 0; testing; x2 = x1, x1 = (x1 + 1) % 2) {
            // read the raw value
            Sensors.readSensor(Sensors.SENSOR_ACCELEROMETER, data[x1]);
            float rawVal = Math.max(Math.max(Math
                  .abs(data[x1][0] - data[x2][0]), Math.abs(data[x1][1]
                  - data[x2][1])), Math.abs(data[x1][2] - data[x2][2]));

            // scale the value to the bar and update bar if need to
            int barVal = Math.min(Math.round(rawVal / Sensitivity.SCALE), 11);
            if (barVal != prevBarVal) {
               handler.sendEmptyMessage(barVal);
               prevBarVal = barVal;
            }

            // wait a little bit before try again
            Thread.sleep(500);
         }

      } catch (InterruptedException ex) {
         Log.e("LifeAlert", ex.getMessage(), ex);
      } finally {
         // put the shake alert back off hold
         ShakeDetectorService.setOnHold(false);
      }
   }

}
