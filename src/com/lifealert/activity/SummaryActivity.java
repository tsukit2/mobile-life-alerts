package com.lifealert.activity;

import com.lifealert.ActionStatusEnum;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;
import com.lifealert.service.ShakeDetectorService;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SummaryActivity extends Activity {

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
      
      // finish it off
      finish();
   }

   private Bundle extras;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.summary);
		
		extras = getIntent().getExtras();
		
		//Emergency detected summary
		TextView textEmergencyDetectedStatus 
				= (TextView)findViewById(R.id.summary_emergency_detected_status);
		textEmergencyDetectedStatus.setText(extras.getString(ActionStatusEnum.Actions.EMERGENCY_DETECTED.toString()));
		
		//Emergency contact called summary
		TextView textEmergencyNumberCalledStatus 
				= (TextView)findViewById(R.id.summary_emergency_num_called_status);
		textEmergencyNumberCalledStatus.setText(extras.getString(ActionStatusEnum.Actions.CALL_EMERGENCY_CONTACT.toString()));
		
		//911 number called summary
		TextView text911CalledStatus 
				= (TextView)findViewById(R.id.summary_911_called_status);
		text911CalledStatus.setText(extras.getString(ActionStatusEnum.Actions.CALL_911.toString()));
		
		//Emergency email sent summary
		TextView textEmergencyEmailSentStatus 
				= (TextView)findViewById(R.id.summary_email_sent_status);
		textEmergencyEmailSentStatus.setText(extras.getString(ActionStatusEnum.Actions.EMERGENCY_EMAIL_SENT.toString()));
		
		//Populate the user summary
		TextView userSummary = (TextView)findViewById(R.id.summary_user);
		StringBuffer sb = new StringBuffer();
		sb.append(getString(R.string.summary_name) + AppConfiguration.getUserName() + "\n");
		sb.append(getString(R.string.summary_phone) + AppConfiguration.getUserPhone() + "\n");
		sb.append(getString(R.string.summary_address) + AppConfiguration.getUserAddress() + "\n");
		sb.append(getString(R.string.summary_email) + AppConfiguration.getUserEmail());
		userSummary.setText(sb.toString());
		
		//Populate the emegency contact summary
		TextView emergencyContactSummary = (TextView)findViewById(R.id.summary_emergency_contact);
		sb = new StringBuffer();
		sb.append(getString(R.string.summary_name) + AppConfiguration.getEmergencyName() + "\n");
		sb.append(getString(R.string.summary_phone) + AppConfiguration.getEmergencyPhone() + "\n");
		sb.append(getString(R.string.summary_address) + AppConfiguration.getEmergencyAddress() + "\n");
		sb.append(getString(R.string.summary_email) + AppConfiguration.getEmergencyEmail());
		emergencyContactSummary.setText(sb.toString());
	}
	
}
