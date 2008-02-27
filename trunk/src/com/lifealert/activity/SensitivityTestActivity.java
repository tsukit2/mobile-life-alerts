package com.lifealert.activity;

import org.openintents.hardware.Sensors;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.lifealert.R;

public class SensitivityTestActivity extends Activity implements Runnable {

   private static final int[] SENSITIVITY_BARS = {
      R.id.sensitivity_bar_0, 
      R.id.sensitivity_bar_1, 
      R.id.sensitivity_bar_2, 
      R.id.sensitivity_bar_3, 
      R.id.sensitivity_bar_4, 
      R.id.sensitivity_bar_5, 
      R.id.sensitivity_bar_6, 
      R.id.sensitivity_bar_7, 
      R.id.sensitivity_bar_8, 
      R.id.sensitivity_bar_9, 
      R.id.sensitivity_bar_10, 
   };
   
   private static final int[] SENSITIVITY_BAR_COLORS = {
      R.color.sensitivity_bar_0, 
      R.color.sensitivity_bar_1, 
      R.color.sensitivity_bar_2, 
      R.color.sensitivity_bar_3, 
      R.color.sensitivity_bar_4, 
      R.color.sensitivity_bar_5, 
      R.color.sensitivity_bar_6, 
      R.color.sensitivity_bar_7, 
      R.color.sensitivity_bar_8, 
      R.color.sensitivity_bar_9, 
      R.color.sensitivity_bar_10, 
   };
   
   private View[] bars;
   private boolean testing;

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
      
      // reset bar color
      resetBarColor();
      
      // now start testing
      org.openintents.provider.Hardware.mContentResolver = getContentResolver();
      startTesting();
   }
   
   private void startTesting() {
      testing = true;
      new Thread(this).start();
   }
   
   private void resetBarColor() {
      // reset the bar colors
      for (int i = 0; i < bars.length; ++i) {
         bars[i].setBackground(R.color.sensitivity_bar_inactive);
      }
   }
   
   private void updateBarColor(int barVal) {
      // set the bar color if it's equal or beyond the given bar val
      for (int i = 0; i < bars.length; ++i) {
         bars[i].setBackground(i < barVal ? SENSITIVITY_BAR_COLORS[i] : R.color.sensitivity_bar_inactive);
      }
   }
   
   private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         updateBarColor(msg.what);
      }
   };

   @Override
   public void run() {
      try {
         // enable the accelerometer
         Sensors.connectSimulator();
         Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);

         // draw the value from the accelero0meter
         float[][] data = new float[2][3];
         int prevBarVal = 0;
         for (int x1 = 0, x2 = 0; testing; x2 = x1, x1 = (x1+1) % 2) {
            // read the raw value
            Sensors.readSensor(Sensors.SENSOR_ACCELEROMETER, data[x1]);
            float rawVal = Math.max(
                  Math.max(Math.abs(data[x1][0] - data[x2][0]), Math.abs(data[x1][1] - data[x2][1])),
                  Math.abs(data[x1][2] - data[x2][2]));
            
            // scale the value to the bar and update bar if need to
            int barVal = Math.min(Math.round(rawVal / 0.2f), 11);
            if (barVal != prevBarVal) {
               handler.sendEmptyMessage(barVal);
               prevBarVal = barVal;
            }
            
            // wait a little bit before try again
            Thread.sleep(500);
         }
         
         // disable the accelerometer
         Sensors.disconnectSimulator();
         Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
      } catch (InterruptedException ex) {
         Log.e("LifeAlert", ex.getMessage(), ex);
      }
   }
   
}
