package com.lifealert.activity;

import org.openintents.hardware.Sensors;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.config.Sensitivity;
import com.lifealert.service.ShakeDetectorService;

public class ConfigurationActivity extends Activity {
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle icicle) {
      // set the content view
      super.onCreate(icicle);
      setContentView(R.layout.configuration);
    
      // TODO: move this to app main: enable the accelerometer
      org.openintents.provider.Hardware.mContentResolver = getContentResolver();
      Sensors.connectSimulator();
      Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
      
      // TODO: remove this
      AppConfiguration.init(this);

      Button vmButton = (Button) findViewById(R.id.config_voiceMail);
      vmButton.setOnClickListener(voiceMailClicked);

      Button senButton = (Button) findViewById(R.id.config_sensitivity);
      senButton.setOnClickListener(sensitivityClicked);
      
      Button actButton = (Button) findViewById(R.id.config_activiate);
      actButton.setOnClickListener(activateClicked);
   }
   
   @Override
   protected void onStart() {
      super.onStart();
      populateConfigurations();
   }
   
   @Override
   protected void onStop() {
      super.onStop();
      saveConfigurations();
   }
   
   private void populateConfigurations() {
      // load off the information and set to the control
      String userName = AppConfiguration.getUserName();
      ((TextView) findViewById(R.id.config_userName)).setText(userName);
      
      String userAddr = AppConfiguration.getUserAddress();
      ((TextView) findViewById(R.id.config_userAddr)).setText(userAddr);
      
      String userPhone = AppConfiguration.getUserPhone();
      ((TextView) findViewById(R.id.config_userPhone)).setText(userPhone);

      String erName = AppConfiguration.getEmergencyName();
      ((TextView) findViewById(R.id.config_emergencyName)).setText(erName);
      
      String erAddr = AppConfiguration.getEmergencyAddress();
      ((TextView) findViewById(R.id.config_emergencyAddr)).setText(erAddr);
      
      String erPhone = AppConfiguration.getEmergencyPhone();
      ((TextView) findViewById(R.id.config_emergencyPhone)).setText(erPhone);
      
      Boolean call911 = AppConfiguration.getCall911();
      ((CheckBox) findViewById(R.id.config_call911)).setChecked(call911 != null && call911.booleanValue());
      
      String txtMsg = AppConfiguration.getTextMsg();
      ((TextView) findViewById(R.id.config_textMsg)).setText(txtMsg);
      
      String voiceMsg = AppConfiguration.getVoiceMailPath();
      ((TextView) findViewById(R.id.config_voiceMailStatus)).setText(
            getText(voiceMsg == null 
                  ? R.string.config_voicemsg_norecorded
                  : R.string.config_voicemsg_recorded));
      
      Sensitivity sens = AppConfiguration.getSensitivity();
      ((TextView) findViewById(R.id.config_sensitivity_status)).setText(
            sens == null ? getText(R.string.config_sensitivity_status)
                  : sens.getLabel());
      
      ((TextView) findViewById(R.id.config_status))
            .setText(ShakeDetectorService.isRunning() ? R.string.config_systemstatus_active
                  : R.string.config_systemstatus_inactive);

      ((Button) findViewById(R.id.config_activiate))
            .setText(ShakeDetectorService.isRunning() ? R.string.config_systemaction_deactivate
                  : R.string.config_systemaction_activate);
      
   }

   private void saveConfigurations() {
      // save off the information 
      AppConfiguration.setUserName(
            ((TextView) findViewById(R.id.config_userName)).getText().toString());
      
      AppConfiguration.setUserAddress(
            ((TextView) findViewById(R.id.config_userAddr)).getText().toString());

      AppConfiguration.setUserPhone(
            ((TextView) findViewById(R.id.config_userPhone)).getText().toString());

      AppConfiguration.setEmergencyName(
            ((TextView) findViewById(R.id.config_emergencyName)).getText().toString());

      AppConfiguration.setEmergencyAddress(
            ((TextView) findViewById(R.id.config_emergencyAddr)).getText().toString());

      AppConfiguration.setEmergencyPhone(
            ((TextView) findViewById(R.id.config_emergencyPhone)).getText().toString());
      
      AppConfiguration.setCall911(
            ((CheckBox) findViewById(R.id.config_call911)).isChecked());
      
      AppConfiguration.setTextMsg(
            ((TextView) findViewById(R.id.config_textMsg)).getText().toString());
      
   }
   
   private boolean configCompleted() {
      // TODO: put more code here
      return true;
   }

   private OnClickListener voiceMailClicked = new OnClickListener() {
      public void onClick(View view) {
         NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
         notMan.notifyWithText(R.string.notification_no_voicemail,
               getText(R.string.notification_no_voicemail),
               NotificationManager.LENGTH_SHORT,
               null);
      }
   };

   private OnClickListener sensitivityClicked = new OnClickListener() {
      public void onClick(View view) {
         Intent intent = new Intent(getApplication(), SensitivityTestActivity.class);
         startSubActivity(intent, 0);
      }
   };
   
   private OnClickListener activateClicked = new OnClickListener() {
      public void onClick(View view) {
         if (ShakeDetectorService.isRunning()) {
            Intent intent = new Intent(getApplication(), ShakeDetectorService.class);
            stopService(intent);
            ((TextView) findViewById(R.id.config_status)).setText(R.string.config_systemstatus_inactive);
            ((Button) findViewById(R.id.config_activiate)).setText(R.string.config_systemaction_activate);
         } else {
            if (configCompleted()) {
               // if config is completed, then the service can start
               Intent intent = new Intent(getApplication(), ShakeDetectorService.class);
               startService(intent, null);
               ((TextView) findViewById(R.id.config_status)).setText(R.string.config_systemstatus_active);
               ((Button) findViewById(R.id.config_activiate)).setText(R.string.config_systemaction_deactivate);
            } else {
               NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
               notMan.notifyWithText(R.string.config_is_incomplete,
                     getText(R.string.config_is_incomplete),
                     NotificationManager.LENGTH_SHORT,
                     null);
            }
         }
      }
   };
   
}
