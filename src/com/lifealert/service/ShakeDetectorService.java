package com.lifealert.service;

import org.openintents.hardware.Sensors;

import com.lifealert.activity.ShakeAlertActivity;
import com.lifealert.config.AppConfiguration;
import com.lifealert.config.Sensitivity;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * The background service for detecting an emergency situation
 * on the user mobile device where MLM is installed.
 * @author Chate Luu, Sukit Tretriluxana
 */
public class ShakeDetectorService extends Service implements Runnable {
   private static boolean working;
   private static boolean onHold;
   private static Sensitivity curSensitivity;
   
   /**
    * @return if the service is running.
    */
   public static boolean isRunning() {
      return working;
   }
   
   public static void setOnHold(boolean onHold) {
      ShakeDetectorService.onHold = onHold;
   }
   
   public static boolean isOnHold() {
      return onHold;
   }
   
   public static void setSensitivity(Sensitivity sens) {
      ShakeDetectorService.curSensitivity = sens;
   }
   
   @Override
   protected void onStart(int startId, Bundle arguments) {
      // let the parent do it first
      super.onStart(startId, arguments);

      // now initialize it self
      new Thread(this).start();
   }

   @Override
   protected void onCreate() {
      // TODO Auto-generated method stub
      super.onCreate();
      AppConfiguration.init(getApplication());

      // pre-obtain the sensitivity
      curSensitivity = AppConfiguration.getSensitivity();
   }

   @Override
   protected void onDestroy() {
      working = false;
      super.onDestroy();
   }

   @Override
   public void run() {
      // start the flag
      working = true;
      
      try {
         // draw the value from the accelerometer
         float[][] data = new float[2][3];
         for (int x1 = 0, x2 = 0; working; x2 = x1, x1 = (x1 + 1) % 2) {
            // read the raw value
            Sensors.readSensor(Sensors.SENSOR_ACCELEROMETER, data[x1]);
            float rawVal = Math.max(Math.max(Math
                  .abs(data[x1][0] - data[x2][0]), Math.abs(data[x1][1]
                  - data[x2][1])), Math.abs(data[x1][2] - data[x2][2]));

            // determine if it meets the desired sensitivity
            if (rawVal >= curSensitivity.getVal() && !onHold) {
               // if so, kick off the alert
               Intent intent = new Intent(getApplication(), ShakeAlertActivity.class);
               intent.setLaunchFlags(Intent.NEW_TASK_LAUNCH);
               startActivity(intent);
            	
               // put this service on hold
               onHold = true;
            }

            // wait a little bit before try again
            Thread.sleep(500);
         }
      } catch (InterruptedException e) {
         Log.e("Alert", e.getMessage(), e);
      } finally {
         // always reset the flag and finish itself
         working = false;
         stopSelf();
      }
   }

   @Override
   public IBinder onBind(Intent arg0) {
      // TODO Auto-generated method stub
      return null;
   }
}
