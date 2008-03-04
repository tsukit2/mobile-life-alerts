package com.lifealert.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.lifealert.R;
import com.lifealert.R.id;
import com.lifealert.R.layout;
import com.lifealert.R.string;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.ContentURI;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.TextView;

public class LifeAlerts_ChateActivity extends Activity {

	private ContentURI newPersonURI;
	private ContentURI newPhoneURI;
	private final int SELECT_EMERGENCY_ID = Menu.FIRST;
	private final int CALL_EMERGENCY_ID = Menu.FIRST + 1;
	private final int EMAIL_EMERGENCY_ID = Menu.FIRST + 2;
	private final int ACTIVITY_SELECT_CONTACT = 0;
	private final String EMERGENCY_CONTACT_FILE = "emergencyContactFile.txt";
	private TextView mainTextView;
	//private static Long personId;
	
	public static String PERSON_ID = "person_id";
	public static String SELECTED_PHONE = "selected_phone";
	public static String SELECTED_NAME = "selected_name";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
    	// clean all contacts first before starting
    	initializeContactsTestData();
    	
    	//Display the screen
    	displayInitialScreen();
        
               
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SELECT_EMERGENCY_ID, R.string.select_emergency);
        menu.add(0, CALL_EMERGENCY_ID, R.string.call_emergency);
        menu.add(0, EMAIL_EMERGENCY_ID, R.string.email_emergency);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, Item item) {
        super.onMenuItemSelected(featureId, item);
        switch(item.getId()) {
        case SELECT_EMERGENCY_ID:
        	selectEmergency();
        	break;
        case CALL_EMERGENCY_ID:
            callEmergencyNumber();
            break;
        case EMAIL_EMERGENCY_ID:
        	//emailEmergencyContact();
        	break;
    	}	
        
        return true;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
               
        switch(requestCode) {
        case ACTIVITY_SELECT_CONTACT:
        	
        	if (extras != null) {
	        	//Process the selected contact info
	            Long id = extras.getLong(PERSON_ID);
	            String phoneNumber = extras.getString(SELECTED_PHONE);
	            String name = extras.getString(SELECTED_NAME);
	            
	        	//The old way:  personId = id;
	
	            // Store values into a file for future retrieval
	            try {
	            	FileOutputStream outFile = openFileOutput(EMERGENCY_CONTACT_FILE, MODE_PRIVATE);
	            	outFile.write(id.toString().getBytes());
	
	            	//Display the selected contact on the screen
	                String selectedContact =  name + ", " + phoneNumber; 
	                mainTextView = (TextView) findViewById(R.id.main_text);
	        		mainTextView.setText(R.string.selected_emergency_contact);
	        		mainTextView.append("\n\n\t" + selectedContact);
	            }
	            catch (IOException e) {
	            	Log.e("Cannot output to emergency contact file: " + e.getMessage(), e.getStackTrace().toString());
	            }            
        	}
        	else {
        		displayInitialScreen();
        	}
            break;
        }
        
    }
    
    
    /**
     * Navigate to the screen to select an emergency number
     */
    private void selectEmergency() {
    	Intent intent = new Intent(this, SelectEmergencyNumberActivity.class);
    	startSubActivity(intent, ACTIVITY_SELECT_CONTACT);
    }

    /**
     * Navigate to the Dialer activity screen and call the emergency contact
     * TODO: Need to figure out how to play the recorded audio over the phone.
     */
    private void callEmergencyNumber() {
    	//Get the emergency contact from the file
    	Long personId = null;
    	
    	try {
    		FileInputStream inFile = openFileInput(EMERGENCY_CONTACT_FILE);
    		BufferedReader buf = new BufferedReader(new InputStreamReader(inFile));
    		personId = new Long(buf.readLine());
    	}
    	catch(IOException e) {
        	Log.e("Cannot read emergency contact file: " + e.getMessage(), e.getStackTrace().toString());    		
    	}
    	
    	//Dial the assigned emergency contact number
    	if (personId != null) {
    		
	    	Intent intent = new Intent(android.content.Intent.CALL_ACTION);
	        ContentURI phoneURIString = Contacts.Phones.CONTENT_URI;
	    	phoneURIString = phoneURIString.addId(new Long(personId));
	    	
	        intent.setData(phoneURIString);
	        startActivity(intent);
	        
    	}
    	else {
    		mainTextView = (TextView) findViewById(R.id.main_text);
    		mainTextView.setText(R.string.no_emergency_contact);
    	}
    }

    /**
     * Navigate to the Dialer activity screen and call the emergency contact
     * TODO: Need to figure out how to play the recorded audio over the phone.
     */
    private void emailEmergencyContact() {
    	//TODO: EMAIL or SMS to emergency contact
    	/*
    	Intent intent = new Intent(android.content.Intent.SENDTO_ACTION);
    	intent.setData();
    	startActivity(intent);
    	*/
    }
    
    /**
     * Initialize the test contacts data
     */
    private void initializeContactsTestData() {
    	// delete the contacts from phone
    	getContentResolver().delete(Contacts.People.CONTENT_URI, null, null);
    	
        // add a test person to the database
        addNewContactHelper("Chate Luu", 
				  "(415) 222 5953",
				  Contacts.Phones.WORK_TYPE,
				  "chate.luu@wellsfargo.com",
				  Contacts.ContactMethods.EMAIL_KIND_WORK_TYPE);
        addNewContactHelper("Jane Doe", 
				  "(415) 999 1111",
				  Contacts.Phones.HOME_TYPE,
				  "janedoe@company.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE);
        addNewContactHelper("Toppsy Kretts", 
				  "(415) 000 9999",
				  Contacts.Phones.MOBILE_TYPE,
				  "topsecret@secret.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE); 
        addNewContactHelper("Who the hell is this!?!", 
				  "(555) 239 0340",
				  Contacts.Phones.MOBILE_TYPE,
				  "whoisthis@dontknow.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE); 
    }
    
    /**
     * Helper class for inserting contacts data
     * @param name = name of the person
     * @param phoneNumber = phone number of person
     * @param phoneType = type of phone number
     * @param email = email of the person
     * @param emailType = type of email
     */
    private void addNewContactHelper(String name, String phoneNumber, int phoneType, String email, int emailType) {
    	ContentValues values = new ContentValues();
        values.put(Contacts.People.NAME, name);
        
        newPersonURI = getContentResolver().insert(Contacts.People.CONTENT_URI, values);
        //String newPersonURIString = newPersonURI.toString();
        String personPathLeaf = newPersonURI.getPathLeaf();
        
        // add the phone contact to the test person above
        values.clear();
        values.put(Contacts.Phones.PERSON_ID, personPathLeaf);
        values.put(Contacts.Phones.NUMBER, phoneNumber);
        values.put(Contacts.Phones.TYPE, phoneType);
        
        newPhoneURI = getContentResolver().insert(Contacts.Phones.CONTENT_URI, values);
        
        // add an email to the test person above
        values.clear();
        values.put(Contacts.ContactMethods.PERSON_ID, personPathLeaf);
        values.put(Contacts.ContactMethods.KIND, Contacts.ContactMethods.EMAIL_KIND);
        values.put(Contacts.ContactMethods.TYPE, emailType);
        values.put(Contacts.ContactMethods.DATA, email);
        
        ContentURI newEmailURI = getContentResolver().insert(newPersonURI.addPath(Contacts.ContactMethods.CONTENT_URI.getPath()), values);
    }
    
    public void displayInitialScreen() {
    	setContentView(R.layout.main);
        mainTextView = (TextView) findViewById(R.id.main_text);
        mainTextView.setAlignment(android.text.Layout.Alignment.ALIGN_CENTER);
        mainTextView.setText(R.string.intro_greeting);
    }
}