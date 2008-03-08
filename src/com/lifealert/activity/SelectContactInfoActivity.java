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

public class SelectContactInfoActivity extends ListActivity {
	
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
        	AppConfiguration.setUserAddress(personInfo.getEmail());
        }
        else {
        	//Store Emergency Contact info
        	AppConfiguration.setEmergencyContactId(personInfo.getPersonId());
        	AppConfiguration.setEmergencyName(personInfo.getName());
        	AppConfiguration.setEmergencyPhone(personInfo.getPhoneNumber());
        	AppConfiguration.setEmergencyAddress(personInfo.getEmail());
        }
        	   
        setResult(RESULT_OK); //, null, bundle);
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
    
    /**
     * This method obtains the contacts info (e.g. name, phone number, email, etc.) from the 
     * Contacts application in Android.
     * @return List of contact data (formatted in the way that they will be displayed in GUI.
     */
    public List<String> getContacts() {
    	
		List<String> returnContacts = new ArrayList<String>();
    	Cursor managedPhoneCursor = null;
    	Cursor managedEmailCursor = null;
    	
    	// Query the list of contacts phones	    
		String[] phoneContactProjection = new String[] {
		    android.provider.BaseColumns._ID
		    , android.provider.Contacts.PeopleColumns.NAME
		    , android.provider.Contacts.PhonesColumns.NUMBER
		    , android.provider.Contacts.PhonesColumns.TYPE
		    , android.provider.Contacts.People._ID //The person contact ID
		};
    	
		managedPhoneCursor = managedQuery( android.provider.Contacts.Phones.CONTENT_URI
								, phoneContactProjection //Which columns to return. 
		                        , null       // WHERE clause--no need.
		                        , android.provider.Contacts.PeopleColumns.NAME + " ASC"); // Order-by clause.

		// Also query the list of emails tied to the same contact list
		String[] emailContactProjection = new String[] {
		    android.provider.BaseColumns._ID
		    , android.provider.Contacts.People._ID
		    , android.provider.Contacts.ContactMethods.DATA
		};
		
		managedEmailCursor = managedQuery(android.provider.Contacts.ContactMethods.CONTENT_URI
				, emailContactProjection
				, null //android.provider.Contacts.ContactMethods.PERSON_ID+"=\'"+Personid+"\'"
				, null); 

		// Prepare the data columns
		int idColumn = managedPhoneCursor.getColumnIndex(android.provider.BaseColumns._ID);
	    int nameColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.PeopleColumns.NAME); 
	    int phoneColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.PhonesColumns.NUMBER);
	    int phonePersonIdColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.People._ID);
		int emailColumn = managedEmailCursor.getColumnIndex(android.provider.Contacts.ContactMethods.DATA);
	    int emailPersonIdColumn = managedEmailCursor.getColumnIndex(android.provider.Contacts.People._ID);
		
		//Store the email in a hash so that we can search for it based on the person ID
	    HashMap emailList = new HashMap();
        while (managedEmailCursor.next()) {
	    	//If at least a contact, loop through and display to user
        	emailList.put(managedEmailCursor.getLong(emailPersonIdColumn)
        			    , managedEmailCursor.getString(emailColumn));
    	    
	    }
	    
		// We need a list of name, phone, and email for each of the contact items
	    Long id;
	    String name; 
	    String phoneNumber;
	    String emailAddress;
	    Long phonePersonId;
	    String contactDisplayFormat;
	    EmergencyPersonInfo personInfo;
	    while (managedPhoneCursor.next()) {
	    	
	    	//If at least a contact, loop through and display to user
    	    id = managedPhoneCursor.getLong(idColumn);
	    	name = managedPhoneCursor.getString(nameColumn);
	        phoneNumber = managedPhoneCursor.getString(phoneColumn);
	        phonePersonId = managedPhoneCursor.getLong(phonePersonIdColumn);
	        emailAddress = (String) emailList.get(phonePersonId);
	        contactDisplayFormat = name 
	        				     + "\n\t" + phoneNumber 
	        				     + "\n\t" + emailAddress;  
	        
	        // Add this contact so that it can be displayed
	        returnContacts.add(contactDisplayFormat);
	        
	        // Add this contact to the hash
	        personInfo = new EmergencyPersonInfo(id, name, phoneNumber, emailAddress);
	        nameIdHash.put(returnContacts.indexOf(contactDisplayFormat), personInfo);    
	    }
	    
	    return returnContacts;
    }
}
