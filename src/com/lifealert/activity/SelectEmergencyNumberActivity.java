package com.lifealert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lifealert.EmergencyPersonInfo;
import com.lifealert.R;
import com.lifealert.R.layout;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectEmergencyNumberActivity extends ListActivity {
	
	private HashMap nameIdHash = new HashMap();
	private List<String> items = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.select_emergency);
		
		// Start to get the list of contacts	    
		String[] projection = new String[] {
		    android.provider.BaseColumns._ID,
		    android.provider.Contacts.PeopleColumns.NAME,
		    android.provider.Contacts.PhonesColumns.NUMBER,
		    android.provider.Contacts.PhonesColumns.TYPE,
		};

		// Best way to retrieve a query; returns a managed query. 
		Cursor managedCursor = managedQuery( android.provider.Contacts.Phones.CONTENT_URI,
		                        projection, //Which columns to return. 
		                        null,       // WHERE clause--we won't specify.
		                        android.provider.Contacts.PeopleColumns.NAME + " ASC"); // Order-by clause.

		
    	int idColumn = managedCursor.getColumnIndex(android.provider.BaseColumns._ID);
	    int nameColumn = managedCursor.getColumnIndex(android.provider.Contacts.PeopleColumns.NAME); 
	    int phoneColumn = managedCursor.getColumnIndex(android.provider.Contacts.PhonesColumns.NUMBER);
	    
		// We need a list of names + phone for the list items
	    Long id;
	    String name; 
	    String phoneNumber;
	    String entry;
	    EmergencyPersonInfo personInfo;
	    
	    // Check if any contacts exist
	    if (managedCursor.first()) {
	    	//If at least a contact, loop through and display to user
	    	
	    	do {
		        // Get the field values
		        id = managedCursor.getLong(idColumn);
		    	name = managedCursor.getString(nameColumn);
		        phoneNumber = managedCursor.getString(phoneColumn);
		        entry = name + ", " + phoneNumber;
		        
		        // Add this contact so that it can be displayed
		        items.add(entry);
		        
		        // Add this contact to the hash
		        personInfo = new EmergencyPersonInfo(id, name, phoneNumber);
		        nameIdHash.put(items.indexOf(entry), personInfo);
	    	}
		    while (managedCursor.next());
	    } 
	    else {
	    	//If there are no contacts, inform user so
           	items.add(getString(R.string.no_emergency_contact));
	    }
	    
        // Now create an array adapter and set it to display using our row
        ArrayAdapter<String> contacts = 
            new ArrayAdapter<String>(this, R.layout.notes_row, items);
        setListAdapter(contacts);

	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        super.onListItemClick(l, v, position, id);
        Bundle bundle = new Bundle();
        
        EmergencyPersonInfo personInfo = (EmergencyPersonInfo) nameIdHash.get(position);
        bundle.putLong(LifeAlerts_ChateActivity.PERSON_ID, personInfo.getPersonId());
        bundle.putString(LifeAlerts_ChateActivity.SELECTED_PHONE, personInfo.getPhoneNumber());
        bundle.putString(LifeAlerts_ChateActivity.SELECTED_NAME, personInfo.getName());
       
        setResult(RESULT_OK, null, bundle);
        finish();      
        
    }	

    @Override
    protected void onFreeze(Bundle outState) {
        super.onFreeze(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
         
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
}
