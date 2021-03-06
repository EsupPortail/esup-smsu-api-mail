package org.esupportail.smsuapimail.domain.beans;

import java.util.LinkedList;
import java.util.List;

public class SmsMessage {

	private List<String> phoneNumbers;

	// useful in case of errors, esp. to get the email "From:"
	private RawMessage rawMessage;
	
	private String content;
	
	private String account;
	
	private String pwd;
	
	public String getAccount() {
		return account;
	}

	public void setAccount(final String account) {
		this.account = account;
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public String getPwd() {
		return pwd;
	}
	
	public void setPhoneNumbers(final List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	
	public void addPhoneNumber(final String phoneNumber) {
		if (this.phoneNumbers == null) {
			phoneNumbers = new LinkedList<String>();
		}
		
		phoneNumbers.add(phoneNumber);
		
	}

	public String getContent() {
		return content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

	public void setPwd(final String pwd) {
		this.pwd = pwd;
	}

	public void setRawMessage(final RawMessage rawMessage) {
		this.rawMessage = rawMessage;
	}

	public RawMessage getRawMessage() {
		return rawMessage;
	}
	
}
