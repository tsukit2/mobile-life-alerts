package com.lifealert.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.lifealert.R;

public class ConfigurationActivity extends Activity {
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle icicle) {
      // set the content view
      super.onCreate(icicle);
      setContentView(R.layout.configuration);
      
      Spinner s1 = (Spinner) findViewById(R.id.sensitivity);
      ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
              this, R.array.sensitivities, android.R.layout.simple_spinner_item);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      s1.setAdapter(adapter);
      s1.setSelection(2);
      
      Button vmButton = (Button) findViewById(R.id.voiceMail);
      vmButton.setOnClickListener(voiceMailClicked);
         
      
   }
   
   private OnClickListener voiceMailClicked = new OnClickListener() {
      public void onClick(View view) {
         NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notMan.notifyWithText(R.string.notification_no_voicemail,
               getText(R.string.notification_no_voicemail),
               NotificationManager.LENGTH_LONG,
               null);
      }
   };
   
//   private OnClickListener sensitivityClicked = new OnClickListener(){
//      public void onClick(View arg0) {
//         startSubActivity(intent, requestCode)
//      }
//   };
}
