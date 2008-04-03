package com.lifealert.activity;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class SendEmailActivity extends Activity {

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.emailhelp);
		Log.e("Life", "It's in the email activity");
//		emailHandler.sendEmptyMessage(0);
	}
	
   private Handler emailHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         //Get the emergency contact email address
         String emergencyEmail = AppConfiguration.getEmergencyEmail();

         if (emergencyEmail != null && !"".equals(emergencyEmail)) {
            Toast.makeText(SendEmailActivity.this
                  , "Sending an emergency email to the following email adress: " + emergencyEmail
                  , Toast.LENGTH_SHORT).show();

            sendEmergencyEmail(emergencyEmail);
            
         }
         else {
            //No emergency email set. Display message.
            Toast.makeText(SendEmailActivity.this
                  , "No emergency email set. No email was sent.", Toast.LENGTH_LONG).show();
         }
      }
   };
	
	
	/**
	 * Send an emergency message to the emergency contact email
	 * @param emergencyEmail
	 */
	private void sendEmergencyEmail(String emergencyEmail) {
		String subject = "EMERGENCY -- NEED HELP -- " + AppConfiguration.getUserName();
		String body = AppConfiguration.getTextMsg();
		String sender = AppConfiguration.getUserEmail();
		String recipients = AppConfiguration.getEmergencyEmail();

		GmailSender gmailSender = new GmailSender("tsukit", "nanarmja");
		try {
			gmailSender.sendMail(subject, body, sender, recipients);
		}
		catch (Exception ex) {
			Toast.makeText(this, getClass().getName(), Toast.LENGTH_SHORT).show();
			Log.e(getClass().getName(), ex.getMessage(), ex);
		}
	}	
}
