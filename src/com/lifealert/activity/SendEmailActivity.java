package com.lifealert.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lifealert.ActionStatusEnum;
import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

/**
 * Activity for sending out the emergency email
 * to the user designated emergency contact.
 * @author Chate Luu, Sukit Tretriluxana
 */
public class SendEmailActivity extends Activity {

   private String emergencyEmail;
   private TextView textView;
   private ProgressBar progressBar;
   private Bundle extras;
   private Thread thread;
   private boolean successful;

   private Handler finishEmailingHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         if (msg.what == 0) {
            textView.setText(successful ? R.string.email_sent_email : R.string.email_failed);
            finishEmailingHandler.sendMessageDelayed(obtainMessage(1), 1000);
         } else {
            Intent intent = new Intent(getApplication(), SummaryActivity.class);
            intent.putExtras(extras);
            startActivity(intent);
            finish();
         }
      }
   };

   @Override
   protected void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      
      //Get the Bundle extras
	  extras = getIntent().getExtras();

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
         textView.setText(R.string.email_sending_email);

         // Send the email in a separate thread
         thread = new Thread(new Runnable() {
            public void run() {
               successful = false;
               sendEmergencyEmail(emergencyEmail);
               finishEmailingHandler.sendEmptyMessage(0);
            } // end run
         });
         thread.start();

      } else {
         // No emergency email set. Display message.
         textView.setText(R.string.email_no_email_set);
      }
   }

   @Override
   protected void onStart() {
      super.onStart();
      
      // put the service on hold
      ShakeDetectorService.setOnHold(true);
      
   }

   @Override
   protected void onStop() {
      super.onStop();

      // put the service off hold
      ShakeDetectorService.setOnHold(false);
      
      // finish it
      if (thread != null && thread.isAlive()) {
         thread.interrupt();
      }
      finish();
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

         GmailSender gmailSender = new GmailSender(sender, senderPassword);
         gmailSender.sendMail(subject, body, sender, recipients);
         extras.putString(ActionStatusEnum.Actions.EMERGENCY_EMAIL_SENT.toString()
					, ActionStatusEnum.Status.COMPLETED.toString());
         successful = true;
      } catch (Exception ex) {
         Log.e(getClass().getName(), ex.getMessage(), ex);
//         textView.setText(R.string.email_failed);
         extras.putString(ActionStatusEnum.Actions.EMERGENCY_EMAIL_SENT.toString()
					, ActionStatusEnum.Status.FAILED.toString());
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

      String location = getString(R.string.geocode_location_address);

      finalBody.append("My current location appear to be:\n");
      finalBody.append("\t" + location);
      finalBody.append("\n\n");
      finalBody.append("From,\n" + userName);

      return finalBody.toString();
   }
}
