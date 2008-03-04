package com.lifealert.activity;

import com.lifealert.R;
import com.lifealert.service.ShakeDetectorService;

import android.app.Activity;
import android.os.Bundle;

public class ShakeAlertActivity extends Activity {

   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      setContentView(R.layout.alert);
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

}
