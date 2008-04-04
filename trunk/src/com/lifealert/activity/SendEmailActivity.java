package com.lifealert.activity;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SendEmailActivity extends Activity {
	
	private TextView textView;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.emailhelp);
        
		emailHandler.sendEmptyMessage(0);
				
	}

	
    private Handler emailHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         //Get the emergency contact email address
         String emergencyEmail = AppConfiguration.getEmergencyEmail();

         if (emergencyEmail != null && !"".equals(emergencyEmail)) {
        	 
        	// Request for the progress bar to be shown in the title
        	setProgressBarVisibility(true);
        	textView = (TextView) findViewById(R.id.email_help);
            //textView.setAlignment(android.text.Layout.Alignment.ALIGN_CENTER);
            //textView.setText(R.string.sending_email);
            
            sendEmergencyEmail(emergencyEmail);
            
            setProgressBarVisibility(false);
	    	textView.setText(R.string.sent_email);
	    	
    		
            //Move to the final screen
		    //Intent intent = new Intent(getApplication(), AppCompleteActivity.class);
		    //startActivity(intent);
		    
		    //finish();
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
		
		//ProgressDialog.show(SendEmailActivity.this,
        //        "Send Emergency Email Message", "Please wait while sending email...", true, true);
			
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
