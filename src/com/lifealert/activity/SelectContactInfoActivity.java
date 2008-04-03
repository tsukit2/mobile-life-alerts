package com.lifealert.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lifealert.ContactInfo;
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
            new ArrayAdapter<String>(this, 
                  android.R.layout.simple_list_item_1
                  /*R.layout.notes_row*/, contactItems);
        setListAdapter(contacts);

	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        super.onListItemClick(l, v, position, id);
        ContactInfo personInfo = (ContactInfo) nameIdHash.get(position);
        
        Long personId = personInfo.getPersonId();
        String name = personInfo.getName();
        
        String phoneNumber = personInfo.getPhoneNumber();
        phoneNumber = (phoneNumber == null) ? "" : phoneNumber;
        
        String email = personInfo.getEmail();
        email = (email == null) ? "" : email;
        
        String address = personInfo.getAddress(); 
        address = (address == null) ? "" : address;
        
        // Store the selected contact info in the AppConfiguration
        AppConfiguration.beginBatchEdit();
        if (ConfigurationActivity.USER_CONTACT_TYPE.equals(contactType)) {
        	//Store User Contact info
        	AppConfiguration.setUserContactId(personId);
        	AppConfiguration.setUserName(name);
        	AppConfiguration.setUserPhone(phoneNumber);
        	AppConfiguration.setUserEmail(email);
        	AppConfiguration.setUserAddress(address);
        }
        else {
        	//Store Emergency Contact info
        	AppConfiguration.setEmergencyContactId(personId);
        	AppConfiguration.setEmergencyName(name);
        	AppConfiguration.setEmergencyPhone(phoneNumber);        		
        	AppConfiguration.setEmergencyEmail(email);
        	AppConfiguration.setEmergencyAddress(address);
        }
        AppConfiguration.commitBatchEdit();
        setResult(RESULT_OK);
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
    	Cursor managedContactMethodsCursor = null;
    	
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
		String[] contactMethodsProjection = new String[] {
		    android.provider.BaseColumns._ID
		    , android.provider.Contacts.People._ID
		    , android.provider.Contacts.People.ContactMethods.KIND
		    , android.provider.Contacts.People.ContactMethods.TYPE
		    , android.provider.Contacts.People.ContactMethods.DATA
		    , android.provider.Contacts.PeopleColumns.NAME
		};
		
		managedContactMethodsCursor = managedQuery(android.provider.Contacts.ContactMethods.CONTENT_URI
				, contactMethodsProjection
				, null //android.provider.Contacts.ContactMethods.PERSON_ID+"=\'"+Personid+"\'" /***/
				, null); 

		// Prepare the data columns
		int idColumn = managedPhoneCursor.getColumnIndex(android.provider.BaseColumns._ID);
	    int nameColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.PeopleColumns.NAME); 
	    int phoneNumberColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.PhonesColumns.NUMBER);
	    int phoneNumberTypeColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.PhonesColumns.TYPE);
	    int phonePersonIdColumn = managedPhoneCursor.getColumnIndex(android.provider.Contacts.People._ID);
	    int contactMethodsPersonIdColumn = managedContactMethodsCursor.getColumnIndex(android.provider.Contacts.People._ID);
	    int contactMethodsKindColumn = managedContactMethodsCursor.getColumnIndex(android.provider.Contacts.People.ContactMethods.KIND);
		int contactMethodsTypeColumn = managedContactMethodsCursor.getColumnIndex(android.provider.Contacts.People.ContactMethods.TYPE);
		int contactMethodsDataColumn = managedContactMethodsCursor.getColumnIndex(android.provider.Contacts.People.ContactMethods.DATA);
		int contactMethodPeopleNameColumn = managedContactMethodsCursor.getColumnIndex(android.provider.Contacts.PeopleColumns.NAME);

		/*************************************************************/
		//Loop through to get the name and phone contact data first
		ContactInfo contactInfo;
		HashMap<String, ContactInfo> contactHash = new HashMap();
		
		Long personId;
		String name, phoneNumber, phoneNumberType;
		while (managedPhoneCursor.next()) {
			personId = managedPhoneCursor.getLong(phonePersonIdColumn);
			name = managedPhoneCursor.getString(nameColumn);
			phoneNumber = managedPhoneCursor.getString(phoneNumberColumn);
			phoneNumberType = managedPhoneCursor.getString(phoneNumberTypeColumn);
			
			//Store the name and phone number into a hash first. We will get the associated email + address next
			//Log.d("Preparing phone data = ", personId + ", " + name + ", " + phoneNumber + ", " + phoneNumberType);
			contactInfo = new ContactInfo(personId, name, phoneNumber, phoneNumberType);
			contactHash.put(name, contactInfo);
		}
		
		//Loop through to get the email and address contact data next
		int kind, type; 
		String data;
		while (managedContactMethodsCursor.next()) {
			personId = managedContactMethodsCursor.getLong(contactMethodsPersonIdColumn);
			kind = managedContactMethodsCursor.getInt(contactMethodsKindColumn);
			type = managedContactMethodsCursor.getInt(contactMethodsTypeColumn);
			data = managedContactMethodsCursor.getString(contactMethodsDataColumn);
			name = managedContactMethodsCursor.getString(contactMethodPeopleNameColumn);
			
			//Log.d("Preparing contact methods data = ", personId + ", " + kind + ", " + type + ", " + data + ", " + name);
			
			//Get the stored contactInfo object with same personId
			contactInfo = contactHash.get(name);
			
			if (kind == android.provider.Contacts.ContactMethods.EMAIL_KIND) {
				//Store the email address and type
				contactInfo.setEmailType(""+type);
				contactInfo.setEmail(data);
				contactHash.put(name, contactInfo);
			}
			else if (kind == android.provider.Contacts.ContactMethods.POSTAL_KIND) {
				//Store the physical address and type
				contactInfo.setAddressType(""+type);
				contactInfo.setAddress(data);
				contactHash.put(name, contactInfo);
			}
			else {
				; //Hit a data kind that we don't care. Don't do anything, but log it incase.
			}	
		}
		
		// Sort the contacts, based on name (which is the key to the HashMap), ASC order
		Map sortedMap = new TreeMap(contactHash);
		
		//Setup what should be displayed on the screen now
		Iterator<ContactInfo> iter = sortedMap.values().iterator();
		String displayString;
		while (iter.hasNext()) {
			contactInfo = iter.next();
			displayString = contactInfo.toString();
			returnContacts.add(displayString);
			nameIdHash.put(returnContacts.indexOf(displayString), contactInfo);
		}
		
	    return returnContacts;
    }
}
