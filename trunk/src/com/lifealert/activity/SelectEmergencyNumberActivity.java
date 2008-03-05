package com.lifealert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lifealert.EmergencyPersonInfo;
import com.lifealert.R;
import com.lifealert.config.AppConfiguration;

public class SelectEmergencyNumberActivity extends ListActivity {
	
	private HashMap nameIdHash = new HashMap();
	private Bundle extras = null;
	private String contactType = null;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.select_emergency);
		
		// Get the bundle's extra data passed from the invoking class
		extras = getIntent().getExtras();
        if (extras != null) {
            contactType = extras.getString(ConfigurationActivity.CONTACT_TYPE);
        }
		
		// Get contacts
		List<String> contactItems = getContacts();
	    
        // Now create an array adapter and set it to display using our row
        ArrayAdapter<String> contacts = 
            new ArrayAdapter<String>(this, R.layout.notes_row, contactItems);
        setListAdapter(contacts);

	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        super.onListItemClick(l, v, position, id);
        EmergencyPersonInfo personInfo = (EmergencyPersonInfo) nameIdHash.get(position);
        
        // Store the selected contact info in the AppConfiguration
        if (ConfigurationActivity.USER_CONTACT_TYPE.equals(contactType)) {
        	//Store User Contact info
        	AppConfiguration.setUserContactId(personInfo.getPersonId());
        	AppConfiguration.setUserName(personInfo.getName());
        	AppConfiguration.setUserPhone(personInfo.getPhoneNumber());
        	//TODO: Need to get the email retrieval to work 
        	//AppConfiguration.setUserAddress(personInfo.getEmail());
        }
        else {
        	//Store Emergency Contact info
        	AppConfiguration.setEmergencyContactId(personInfo.getPersonId());
        	AppConfiguration.setEmergencyName(personInfo.getName());
        	AppConfiguration.setEmergencyPhone(personInfo.getPhoneNumber());
        	//TODO: Need to get the email retrieval to work 
        	//AppConfiguration.setEmergencyAddress(personInfo.getEmail());
        }
        
        /****************************************************************/
        // TODO:  Remove this block of code (between the **** lines) for production version
        // Store the selected info into the bean class
        Bundle bundle = new Bundle();
        
        bundle.putLong(LifeAlerts_ChateActivity.PERSON_ID, personInfo.getPersonId());
        bundle.putString(LifeAlerts_ChateActivity.SELECTED_PHONE, personInfo.getPhoneNumber());
        bundle.putString(LifeAlerts_ChateActivity.SELECTED_NAME, personInfo.getName());
        /****************************************************************/
        
        	   
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
    
    public List<String> getContacts() {
    	
    	List<String> returnContacts = new ArrayList<String>();
    	
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
	    while (managedCursor.next()) {
	    	//If at least a contact, loop through and display to user
    	    id = managedCursor.getLong(idColumn);
	    	name = managedCursor.getString(nameColumn);
	        phoneNumber = managedCursor.getString(phoneColumn);
	        entry = name + ", " + phoneNumber;
	        
	        // Add this contact so that it can be displayed
	        returnContacts.add(entry);
	        
	        // Add this contact to the hash
	        personInfo = new EmergencyPersonInfo(id, name, phoneNumber);
	        nameIdHash.put(returnContacts.indexOf(entry), personInfo);    
	    }
	    
	    return returnContacts;
    }
}
