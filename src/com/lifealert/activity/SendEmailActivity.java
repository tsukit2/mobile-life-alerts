package com.lifealert.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

public class SendEmailActivity extends Activity {

   private String emergencyEmail;
   private TextView textView;
   private ProgressBar progressBar;

   private Handler finishEmailingHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         if (msg.what == 0) {
            textView.setText(R.string.sent_email);
            finishEmailingHandler.sendMessageDelayed(obtainMessage(1), 1000);
         } else {
            Intent intent = new Intent(getApplication(), SummaryActivity.class);
            startActivity(intent);
            finish();
         }
      }
   };

   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);

      // Request for the progress bar to be shown in the title
      requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

      setContentView(R.layout.emailhelp);
      textView = (TextView) findViewById(R.id.email_help);
      textView.setAlignment(android.text.Layout.Alignment.ALIGN_CENTER);
      progressBar = (ProgressBar) findViewById(R.id.progress_bar);
      setProgressBarVisibility(true);

      // Get the emergency contact email address
      emergencyEmail = AppConfiguration.getEmergencyEmail();

      if (emergencyEmail != null && !"".equals(emergencyEmail)) {
         // Set the screen display
         textView.setText(R.string.sending_email);

         // Send the email in a separate thread
         new Thread(new Runnable() {
            public void run() {
               sendEmergencyEmail(emergencyEmail);
               finishEmailingHandler.sendEmptyMessage(0);
            } // end run
         }).start();

      } else {
         // No emergency email set. Display message.
         textView.setText(R.string.no_email_set);
      }
   }

   /**
    * Send an emergency message to the emergency contact email
    * 
    * @param emergencyEmail
    */
   private void sendEmergencyEmail(String emergencyEmail) {
      try {
         String subject = getString(R.string.email_subject_line)
               + AppConfiguration.getUserName();
         String sender = AppConfiguration.getUserEmail();
         String senderPassword = AppConfiguration.getUserEmailPassword();
         String recipients = AppConfiguration.getEmergencyEmail();
         String body = formatEmailBody(AppConfiguration.getUserName(),
               AppConfiguration.getEmergencyName(), AppConfiguration
                     .getTextMsg());

         // TODO: Remove hardcoded password of user's Gmail account
         GmailSender gmailSender = new GmailSender(sender, senderPassword);
         gmailSender.sendMail(subject, body, sender, recipients);
      } catch (Exception ex) {
         Log.e(getClass().getName(), ex.getMessage(), ex);
         textView.setText(R.string.email_failed);
      }

   }

   /**
    * Format the emergency email body
    * 
    * @param userName
    * @param emergencyName
    * @param originalMsg
    * @return
    * @throws IOException
    */
   private String formatEmailBody(String userName, String emergencyName,
         String originalMsg) throws IOException {
      StringBuffer finalBody = new StringBuffer();
      finalBody.append(emergencyName + ",\n\n");
      finalBody.append(originalMsg + "\n\n");

//      LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//      // LocationProvider provider = locMan.getProviders().get(0);
//      // //locMan.getBestProvider(new Criteria());
//      Location curLoc = locMan.getCurrentLocation("gps");
//      String location = new Geocoder().getFromLocation(curLoc.getLatitude(),
//            curLoc.getLongitude())[0].toString();
      String location = getString(R.string.geocode_location_address);

      finalBody.append("My current location appear to be:\n");
      finalBody.append("\t" + location);
      finalBody.append("\n\n");
      finalBody.append("From,\n" + userName);

      return finalBody.toString();
   }
}
