package com.lifealert.activity;

import org.openintents.hardware.Sensors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.config.Sensitivity;
import com.lifealert.service.ShakeDetectorService;

public class ConfigurationActivity extends Activity {
	
   //The "Bundle" key to be used by the SelectEmergencyNumber activity
   public static final String CONTACT_TYPE = "contactType";  
   public static final String USER_CONTACT_TYPE = "userContactType";
   public static final String EMERGENCY_CONTACT_TYPE = "emergencyContactType";
	
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle icicle) {
      // set the content view
      super.onCreate(icicle);
      setContentView(R.layout.configuration);

      // initialize the app config so the rest of the app can use
      AppConfiguration.init(this);

      // the following is to initialize the tabs in the config screen
      Context context = this;
      Resources res = context.getResources();
      
      TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
      tabHost.setup();
      
      TabSpec tabspec = tabHost.newTabSpec("about");
      tabspec.setIndicator(res.getString(R.string.config_about), res.getDrawable(R.drawable.config_about));
      tabspec.setContent(R.id.about);
      tabHost.addTab(tabspec);
      
      tabspec = tabHost.newTabSpec("settings");
      tabspec.setIndicator(res.getString(R.string.config_settings), res.getDrawable(R.drawable.config_settings));
      tabspec.setContent(R.id.settings);
      tabHost.addTab(tabspec);
      
      tabHost.setCurrentTab(0);

      // if the service is not already running, make sure to initialize the simulator
      if (!ShakeDetectorService.isRunning()) {
         org.openintents.provider.Hardware.mContentResolver = getContentResolver();
         Sensors.connectSimulator();
         Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
      }

      // wire the event listeners
      Button uiButton = (Button) findViewById(R.id.config_userInfo);
      uiButton.setOnClickListener(userInfoClicked);
      
      Button eiButton = (Button) findViewById(R.id.config_emergencyInfo);
      eiButton.setOnClickListener(emergencyInfoClicked);
      
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

      String userEmail = AppConfiguration.getUserEmail();
      ((TextView) findViewById(R.id.config_userEmail)).setText(userEmail);

      String userEmailPassword = AppConfiguration.getUserEmailPassword();
      ((TextView) findViewById(R.id.config_userEmailPassword)).setText(userEmailPassword);
      
      String erName = AppConfiguration.getEmergencyName();
      ((TextView) findViewById(R.id.config_emergencyName)).setText(erName);
      
      String erAddr = AppConfiguration.getEmergencyAddress();
      ((TextView) findViewById(R.id.config_emergencyAddr)).setText(erAddr);
      
      String erPhone = AppConfiguration.getEmergencyPhone();
      ((TextView) findViewById(R.id.config_emergencyPhone)).setText(erPhone);
      
      String erEmail = AppConfiguration.getEmergencyEmail();
      ((TextView) findViewById(R.id.config_emergencyEmail)).setText(erEmail);
      
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

      setStatusColor(ShakeDetectorService.isRunning());

      ((Button) findViewById(R.id.config_activiate))
            .setText(ShakeDetectorService.isRunning() ? R.string.config_systemaction_deactivate
                  : R.string.config_systemaction_activate);
      
   }
   
   private void setStatusColor(boolean flag) {
      TextView status = (TextView) findViewById(R.id.config_status);
      status.setText(flag ? R.string.config_systemstatus_active
            : R.string.config_systemstatus_inactive);
      status.setTextColor(getResources().getColor(
            flag ? R.color.config_status_on : R.color.config_status_off));
   }

   private void saveConfigurations() {
      // save off the information 
      AppConfiguration.beginBatchEdit();
      
      AppConfiguration.setUserName(
            ((TextView) findViewById(R.id.config_userName)).getText().toString());
      
      AppConfiguration.setUserAddress(
            ((TextView) findViewById(R.id.config_userAddr)).getText().toString());

      AppConfiguration.setUserPhone(
            ((TextView) findViewById(R.id.config_userPhone)).getText().toString());

      AppConfiguration.setUserEmail(
            ((TextView) findViewById(R.id.config_userEmail)).getText().toString());
      
      AppConfiguration.setUserEmailPassword(
              ((TextView) findViewById(R.id.config_userEmailPassword)).getText().toString());
      
      AppConfiguration.setEmergencyName(
            ((TextView) findViewById(R.id.config_emergencyName)).getText().toString());

      AppConfiguration.setEmergencyAddress(
            ((TextView) findViewById(R.id.config_emergencyAddr)).getText().toString());

      AppConfiguration.setEmergencyPhone(
            ((TextView) findViewById(R.id.config_emergencyPhone)).getText().toString());
      
      AppConfiguration.setEmergencyEmail(
            ((TextView) findViewById(R.id.config_emergencyEmail)).getText().toString());

      AppConfiguration.setCall911(
            ((CheckBox) findViewById(R.id.config_call911)).isChecked());
      
      AppConfiguration.setTextMsg(
            ((TextView) findViewById(R.id.config_textMsg)).getText().toString());
      
      AppConfiguration.commitBatchEdit();
   }
   
   private boolean configCompleted() {
      // save off the information 
      String userName = ((TextView) findViewById(R.id.config_userName)).getText().toString();
      if (userName == null || "".equals(userName)) {
         return false;
      }
      
      String userAddr = ((TextView) findViewById(R.id.config_userAddr)).getText().toString();
      if (userAddr == null || "".equals(userAddr)) {
         return false;
      }

      String userPhone = ((TextView) findViewById(R.id.config_userPhone)).getText().toString();
      if (userPhone == null || "".equals(userPhone)) {
         return false;
      }

      String userEmail = ((TextView) findViewById(R.id.config_userEmail)).getText().toString();
      if (userEmail == null || "".equals(userEmail)) {
         return false;
      }
      
      String userEmailPassword = ((TextView) findViewById(R.id.config_userEmailPassword)).getText().toString();
      if (userEmailPassword == null || "".equals(userEmailPassword)) {
         return false;
      }

      String emergencyName = ((TextView) findViewById(R.id.config_emergencyName)).getText().toString();
      if (emergencyName == null || "".equals(emergencyName)) {
         return false;
      }

      String emergencyAddr = ((TextView) findViewById(R.id.config_emergencyAddr)).getText().toString();
      if (emergencyAddr == null || "".equals(emergencyAddr)) {
         return false;
      }

      String emergencyPhone = ((TextView) findViewById(R.id.config_emergencyPhone)).getText().toString();
      if (emergencyPhone == null || "".equals(emergencyPhone)) {
         return false;
      }

      String emergencyEmail = ((TextView) findViewById(R.id.config_emergencyEmail)).getText().toString();
      if (emergencyEmail == null || "".equals(emergencyEmail)) {
         return false;
      }

      String textMsg = ((TextView) findViewById(R.id.config_textMsg)).getText().toString();
      if (textMsg == null || "".equals(textMsg)) {
         return false;
      }

      String voiceMsg = AppConfiguration.getVoiceMailPath();
      if (textMsg == null || "".equals(voiceMsg)) {
         return false;
      }

      if (AppConfiguration.getSensitivity() == null) {
         return false;
      }
      
      // if we reach here, it's good to go
      return true;
   }

   private OnClickListener voiceMailClicked = new OnClickListener() {
      public void onClick(View view) {
         // add value refresh the screen
         AppConfiguration.setVoiceMailPath("dummy");
         String voiceMsg = AppConfiguration.getVoiceMailPath();
         ((TextView) findViewById(R.id.config_voiceMailStatus)).setText(
               getText(voiceMsg == null 
                     ? R.string.config_voicemsg_norecorded
                     : R.string.config_voicemsg_recorded));

         // now notify the user that we cannot record the voice
         Toast.makeText(ConfigurationActivity.this, R.string.notification_no_voicemail, Toast.LENGTH_SHORT).show();

         
         //         NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//         notMan.notifyWithText(R.string.notification_no_voicemail,
//               getText(R.string.notification_no_voicemail),
//               NotificationManager.LENGTH_SHORT,
//               null);
      }
   };

   private OnClickListener sensitivityClicked = new OnClickListener() {
      public void onClick(View view) {
         Intent intent = new Intent(getApplication(), SensitivityTestActivity.class);
         startSubActivity(intent, 0);
      }
   };
   
   private OnClickListener userInfoClicked = new OnClickListener() {
      public void onClick(View view) {
         Intent intent = new Intent(getApplication(), SelectContactInfoActivity.class);
         intent.putExtra(CONTACT_TYPE, USER_CONTACT_TYPE);
         startSubActivity(intent, 0);
      }
   };
   
   private OnClickListener emergencyInfoClicked = new OnClickListener() {
      public void onClick(View view) {
         Intent intent = new Intent(getApplication(), SelectContactInfoActivity.class);
         intent.putExtra(CONTACT_TYPE, EMERGENCY_CONTACT_TYPE);
         startSubActivity(intent, 0);
      }
   };
   
   
   private OnClickListener activateClicked = new OnClickListener() {
      public void onClick(View view) {
         if (ShakeDetectorService.isRunning()) {
            Intent intent = new Intent(getApplication(), ShakeDetectorService.class);
            stopService(intent);
            setStatusColor(false);
            ((Button) findViewById(R.id.config_activiate)).setText(R.string.config_systemaction_activate);
         } else {
            if (configCompleted()) {
               // if config is completed, then the service can start
               saveConfigurations();
               Intent intent = new Intent(getApplication(), ShakeDetectorService.class);
               startService(intent, null);
               setStatusColor(true);
               ((Button) findViewById(R.id.config_activiate)).setText(R.string.config_systemaction_deactivate);
            } else {
               Toast.makeText(ConfigurationActivity.this, R.string.config_is_incomplete, Toast.LENGTH_SHORT).show();
//               NotificationManager notMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//               notMan.notifyWithText(R.string.config_is_incomplete,
//                     getText(R.string.config_is_incomplete),
//                     NotificationManager.LENGTH_SHORT,
//                     null);
            }
         }
      }
   };
   
}
