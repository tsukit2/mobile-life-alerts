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
		
		TextView text911Called = (TextView)findViewById(R.id.summary_911_called);
		if (!AppConfiguration.getCall911()) {
			text911Called.setVisibility(View.GONE);
		}
		
	}
	
}
