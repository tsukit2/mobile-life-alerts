package com.lifealert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectEmergencyNumber extends ListActivity {
	
	private HashMap nameIdHash = new HashMap();
	private List<String> items = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.select_emergency);
		
		// Get the list of contacts
	    

		// An array specifying which columns to return. 
		// The provider exposes a list of column names it returns for a specific
		// query, or you can get all columns and iterate through them. 
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
	    while (managedCursor.next()) {
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
        bundle.putLong(LifeAlerts_Chate.PERSON_ID, personInfo.getPersonId());
        bundle.putString(LifeAlerts_Chate.SELECTED_PHONE, personInfo.getPhoneNumber());
        bundle.putString(LifeAlerts_Chate.SELECTED_NAME, personInfo.getName());
       
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
