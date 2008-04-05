package com.lifealert.activity;

import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SummaryActivity extends Activity {

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		TextView t1 = (TextView)findViewById(R.id.summary_911_called);
		if (!AppConfiguration.getCall911()) {
			t1.setVisibility(View.GONE);
		}
		
	}
	
}
