package com.lifealert.activity;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class SendEmailActivity extends Activity {
	
	private String emergencyEmail;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.emailhelp);
		TextView textView = (TextView) findViewById(R.id.email_help);
			
		//Get the emergency contact email address
        emergencyEmail = AppConfiguration.getEmergencyEmail();
        if (emergencyEmail != null && !"".equals(emergencyEmail)) {
        	new Thread(new Runnable() {
                public void run() {
                	sendEmergencyEmail(emergencyEmail);                    
                } //end run
            }).start();

        }
        else {
           //No emergency email set. Display message.
           textView.setText(R.string.no_email_set);
        }
        

    	//Update text to completed (although we're not sure if the email
    	//in the other thread got sent already or not. The other thread cannot
    	//set the text on the UI of this thread.
    	ProgressDialog.show(SendEmailActivity.this,
                "Send Emergency Email Message", getString(R.string.sent_email), true, true);

	}
	
	/**
	 * Send an emergency message to the emergency contact email
	 * @param emergencyEmail
	 */
	private void sendEmergencyEmail(String emergencyEmail) {
			
		String subject = "EMERGENCY -- NEED HELP -- " + AppConfiguration.getUserName();
		String body = AppConfiguration.getTextMsg();
		String sender = AppConfiguration.getUserEmail();
		String recipients = AppConfiguration.getEmergencyEmail();

		//TODO: Remove hardcoded password of user's Gmail account
		GmailSender gmailSender = new GmailSender(recipients, "moblielifealerts_01"); 
		try {
			gmailSender.sendMail(subject, body, sender, recipients);
		}
		catch (Exception ex) {
			Toast.makeText(this, getClass().getName(), Toast.LENGTH_SHORT).show();
			Log.e(getClass().getName(), ex.getMessage(), ex);
		}

	}	
}
