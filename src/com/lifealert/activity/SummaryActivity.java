package com.lifealert.activity;

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
		
		TextView text911Called = (TextView)findViewById(R.id.summary_911_called);
		if (!AppConfiguration.getCall911()) {
			text911Called.setVisibility(View.GONE);
		}
		
	}
	
}
