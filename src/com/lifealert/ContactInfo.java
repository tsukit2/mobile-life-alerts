package com.lifealert;

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
	public ContactInfo(Long personId, String name, String phoneNumber) {
		super();
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.personId = personId;
	}

	public ContactInfo(Long personId, String name, String phoneNumber
			, String email) {
		super();
		this.name = name;
		this.phoneNumber = phoneNumber;
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
		super();
		this.personId = personId;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.phoneNumberType = phoneNumberType;
		this.email = email;
		this.emailType = emailType;
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

	
}
