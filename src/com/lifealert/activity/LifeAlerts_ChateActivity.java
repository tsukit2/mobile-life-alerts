package com.lifealert.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.ContentURI;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IServiceManager;
import android.os.ServiceManagerNative;
import android.provider.Contacts;
import android.telephony.IPhone;
import android.util.Log;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.TextView;

import com.lifealert.GmailSender;
import com.lifealert.R;
import com.lifealert.activity.ConfigurationActivity;
import com.lifealert.activity.SelectContactInfoActivity;
import com.lifealert.config.AppConfiguration;

public class LifeAlerts_ChateActivity extends Activity {

	private ContentURI newPersonURI;
	private ContentURI newPhoneURI;
	private final int SELECT_EMERGENCY_ID = Menu.FIRST;
	private final int CALL_EMERGENCY_ID = Menu.FIRST + 1;
	private final int EMAIL_EMERGENCY_ID = Menu.FIRST + 2;
	private final int ACTIVITY_SELECT_CONTACT = 0;
	private final String EMERGENCY_CONTACT_FILE = "emergencyContactFile.txt";
	private final String TEST_GMAIL_USERNAME = "mobilelifealerts";
	private final String TEST_GMAIL_PASSWORD = "mobilelifealerts";
	private TextView mainTextView;
	//private static Long personId;
	
	public static String PERSON_ID = "person_id";
	public static String SELECTED_PHONE = "selected_phone";
	public static String SELECTED_NAME = "selected_name";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        // Clean up the contacts first
        cleanTestData();
        
    	// Create the test Contacts
    	initializeContactsTestData();
    	
    	// Initialize the AppConfiguration object
    	AppConfiguration.init(this);
    	
    	// Display the screen
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
            callEmergencyNumberIPhone();
            break;
        case EMAIL_EMERGENCY_ID:
        	emailEmergencyContact();
        	break;
    	}	
        
        return true;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, String data, Bundle extras) {
        super.onActivityResult(requestCode, resultCode, data, extras);
               
        switch(requestCode) {
        case ACTIVITY_SELECT_CONTACT:
        	
        	if (!"".equals(AppConfiguration.getEmergencyName())) {
	        	//Process the selected contact info
	            String name = AppConfiguration.getEmergencyName();
	            String phoneNumber = AppConfiguration.getEmergencyPhone();
	            String email = AppConfiguration.getEmergencyAddress();
	            
	
            	//Display the selected contact on the screen
                String selectedContact =  name + "\n\t" + phoneNumber + "\n\t" + email; 
                mainTextView = (TextView) findViewById(R.id.main_text);
        		mainTextView.setText(R.string.selected_emergency_contact);
        		mainTextView.append("\n\n\t" + selectedContact);
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
    	
        Intent intent = new Intent(getApplication(), SelectContactInfoActivity.class);
        intent.putExtra(ConfigurationActivity.CONTACT_TYPE, ConfigurationActivity.EMERGENCY_CONTACT_TYPE);
        startSubActivity(intent, ACTIVITY_SELECT_CONTACT);
    }

    /**
     * Navigate to the Dialer activity screen and call the emergency contact
     * TODO: Need to figure out how to play the recorded audio over the phone.
     */
    private void callEmergencyNumber() {
        //Get the emergency contact from the AppConfiguration 
        //Long personToContact = AppConfiguration.getEmergencyContactId();
        String numberToContact = AppConfiguration.getEmergencyPhone();
  	   
        if (numberToContact == null) {
      	  // Set contact to user of this phone, if emergency contact doesn't exist
      	  numberToContact = AppConfiguration.getUserPhone();
        }
        
        ContentURI contentUri = ContentURI.create(numberToContact);
          
        //Dial the assigned emergency contact number
        Intent intent = new Intent(android.content.Intent.CALL_ACTION);
        //ContentURI phoneURIString = Contacts.Phones.CONTENT_URI;
        //phoneURIString = phoneURIString.addId(personToContact);
        
        intent.setData(contentUri/*phoneURIString*/);
        startActivity(intent);
    }
    
    public void callEmergencyNumberIPhone() {
    	
    	IServiceManager sm = ServiceManagerNative.getDefault();
    	
    	try {
    		IPhone phoneService = IPhone.Stub.asInterface(sm.getService("phone"));
        	
    		String numberToContact = AppConfiguration.getEmergencyPhone();
    	  	   
            if (numberToContact == null) {
          	  // Set contact to user of this phone, if emergency contact doesn't exist
          	  numberToContact = AppConfiguration.getUserPhone();
            }
            
            phoneService.dial(numberToContact);
    	}
    	catch (DeadObjectException e) {
    		Log.e("callEmergencyNumberIPhone failure", e.getMessage());
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
    	String subject = "Test from moblie life alerts app";
    	String body = "Testing to see if this email goes through.";
    	String sender = "Mobile Life Alerts App";
    	String recipients = "Test Gmail Account";
    	
    	GmailSender gmailSender = new GmailSender(TEST_GMAIL_USERNAME, TEST_GMAIL_PASSWORD);
    	try {
    		gmailSender.sendMail(subject, body, sender, recipients);
    	}
    	catch (Exception e) {
    		Log.e("SendGmail has error", e.getMessage(), e);
    	}
    }
    
    /**
     * Initialize the test contacts data
     */
    private void initializeContactsTestData() {
    	
        // add a test person to the database
        addNewContactHelper("Chate Luu", 
				  "(415) 222 5953",
				  Contacts.Phones.WORK_TYPE,
				  "chate.luu@wellsfargo.com",
				  Contacts.ContactMethods.EMAIL_KIND_WORK_TYPE,
				  "455 Market St.\nSan Francisco, CA 94105");
        addNewContactHelper("Jane Doe", 
				  "(415) 999 1111",
				  Contacts.Phones.HOME_TYPE,
				  "janedoe@company.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE,
				  "333 Market St.\nSan Francisco, CA 94105");
        addNewContactHelper("Toppsy Kretts", 
				  "(415) 000 9999",
				  Contacts.Phones.MOBILE_TYPE,
				  "topsecret@secret.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE,
				  "525 Market St.\nSan Francisco, CA 94105"); 
        addNewContactHelper("Who the hell is this!?!", 
				  "(555) 239 0340",
				  Contacts.Phones.MOBILE_TYPE,
				  "whoisthis@dontknow.com",
				  Contacts.ContactMethods.EMAIL_KIND_HOME_TYPE,
				  "45 Fremont St.\nSan Francisco, CA 94105"); 
    }
    
    /**
     * Clean up the contacts
     */
    public void cleanTestData() {
    	// delete the contacts from phone
    	getContentResolver().delete(Contacts.People.CONTENT_URI, null, null);
    }
    
    /**
     * Helper class for inserting contacts data
     * @param name = name of the person
     * @param phoneNumber = phone number of person
     * @param phoneType = type of phone number
     * @param email = email of the person
     * @param emailType = type of email
     */
    private void addNewContactHelper(String name, String phoneNumber, int phoneType,
    								 String email, int emailType, String address) {
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

        // add a physical address to the test person above
        values.clear();
        values.put(Contacts.ContactMethods.PERSON_ID, personPathLeaf);
        values.put(Contacts.ContactMethods.KIND, Contacts.ContactMethods.POSTAL_KIND);
        values.put(Contacts.ContactMethods.DATA, address);    
        ContentURI newAddressURI = getContentResolver().insert(newPersonURI.addPath(Contacts.ContactMethods.CONTENT_URI.getPath()), values);
    }
    
    public void displayInitialScreen() {
    	setContentView(R.layout.main);
        mainTextView = (TextView) findViewById(R.id.main_text);
        mainTextView.setAlignment(android.text.Layout.Alignment.ALIGN_CENTER);
        mainTextView.setText(R.string.intro_greeting);
    }
}