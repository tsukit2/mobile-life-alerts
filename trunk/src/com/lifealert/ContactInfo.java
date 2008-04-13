package com.lifealert;

/**
 * Bean object for storing the contacts information
 * selected from the Contacts app. 
 * @author Chate Luu, Sukit Tretriluxana
 */
public class ContactInfo {

	private String name;
	private String phoneNumber;
	private String phoneNumberType;
	private String email;
	private String emailType;
	private String address;
	private String addressType;
	private Long personId;
	
	/**
	 * Constructor
	 * @param name
	 * @param phoneNumber
	 */
	public ContactInfo(Long personId, String name, String phoneNumber, String phoneNumberType) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.phoneNumberType = phoneNumberType;
		this.personId = personId;
	}
	
	/**
	 * Constructor
	 * @param personId
	 * @param name
	 * @param phoneNumber
	 * @param email
	 */
	public ContactInfo(Long personId, String name, String phoneNumber, String phoneNumberType
			, String email) {
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.phoneNumberType = phoneNumberType;
		this.email = email;
		this.personId = personId;
	}

	/**
	 * Constructor
	 * @param name
	 * @param phoneNumber
	 * @param phoneNumberType
	 * @param email
	 * @param emailType
	 */
	public ContactInfo(Long personId, String name, String phoneNumber,
			String phoneNumberType, String email, String emailType) {
		this.personId = personId;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.phoneNumberType = phoneNumberType;
		this.email = email;
		this.emailType = emailType;
	}

	/**
	 * Constructor
	 * @param personId
	 * @param name
	 * @param phoneNumber
	 * @param phoneNumberType
	 * @param email
	 * @param emailType
	 * @param address
	 * @param addressType
	 */
	public ContactInfo(Long personId, String name, String phoneNumber,
			String phoneNumberType, String email, String emailType,
			String address, String addressType) {
		this.personId = personId;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.phoneNumberType = phoneNumberType;
		this.email = email;
		this.emailType = emailType;
		this.address = address;
		this.addressType = addressType;
	}
	
	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumberType() {
		return phoneNumberType;
	}

	public void setPhoneNumberType(String phoneNumberType) {
		this.phoneNumberType = phoneNumberType;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmailType() {
		return emailType;
	}

	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public String toString() {
      return formatContact(name, phoneNumber, null, null);
	}
	
	/**
	 * Return the contact info stored in this object into more readable
	 * format
	 * @param inName
	 * @param inPhone
	 * @param inEmail
	 * @param inAddress
	 * @return
	 */
	public static String formatContact (String inName, String inPhone, String inEmail, String inAddress) {
		inPhone = (inPhone != null && !"".equals(inPhone)) ? "\n\t" + inPhone : "";
		inEmail = (inEmail != null && !"".equals(inEmail)) ? "\n\t" + inEmail : "";
		inAddress = (inAddress != null && !"".equals(inAddress)) ? "\n\t" + inAddress : "";
		
		return inName 
	     		+ inPhone
	     		+ inEmail
	     		+ inAddress;		
	}
}
