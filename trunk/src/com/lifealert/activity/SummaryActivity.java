package com.lifealert.activity;

import com.lifealert.ActionStatusEnum;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SummaryActivity extends Activity {

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
		
		
	}
	
}
