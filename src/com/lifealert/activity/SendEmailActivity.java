package com.lifealert.activity;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SendEmailActivity extends Activity {
	
	private String emergencyEmail;
	private TextView textView;
	private ProgressBar progressBar;
	private static boolean emailSent = false;
	
	private Handler emailHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			sendMessageDelayed(obtainMessage(), 1000);
			
			if (emailSent) {
				textView.setText(R.string.sent_email);
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
		textView.setText(R.string.sending_email);
		progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		setProgressBarVisibility(true);
		
		//Start the handler
		emailHandler.removeMessages(emailHandler.obtainMessage().what);
		emailHandler.sendMessageDelayed(emailHandler.obtainMessage(), 1000);
		
		//Get the emergency contact email address
        emergencyEmail = AppConfiguration.getEmergencyEmail();
        if (emergencyEmail != null && !"".equals(emergencyEmail)) {
        	new Thread(new Runnable() {
                public void run() {
                	sendEmergencyEmail(emergencyEmail);                    
                	emailSent = true;
                } //end run
            }).start();

        }
        else {
           //No emergency email set. Display message.
           textView.setText(R.string.no_email_set);
        }
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
			Log.e(getClass().getName(), ex.getMessage(), ex);
			textView.setText(R.string.email_failed);
		}

	}	
}
