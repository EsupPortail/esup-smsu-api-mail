package org.esupportail.smsuapimail.services.messageRetriever.pop;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.smsuapimail.domain.beans.SmsMessage;
import org.esupportail.smsuapimail.exceptions.ParsingMessageBodyException;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class is used to convert a email body into a valid mailtoSmsMessage.
 *
 */
public class MessageBodyToMailToSmsMessageConverter implements InitializingBean {

	/**
	 * A logger.
	 */
	private final Logger logger = new LoggerImpl(getClass());
	
	/**
	 * the regular expression used to find tags
	 */
	private static final String TAG_PATTERN = "\\s*(\\w+)\\s*=\\s*";
	
	/**
	 * the separator comma used to separate phone number.
	 */
	private static final String COMMA_SEPARATOR_REGULAR_EXPR = ",";
	
	/**
	 * Tag used in email to defined pwd.
	 */
	private String pwdTag;
	
	/**
	 * Tag used in email to defined recipients.
	 */
	private String recipientsTag;
	
	/**
	 * Tag used email to defined account.
	 */
	private String accountTag;
	
	/**
	 * tag used in email to defined content.
	 */
	private String contentTag;
	
	/**
	 * the valid phone number pattern.
	 */
	private String phoneNumberPattern;
	
	/**
	 * message end tag
	 */
	private String endTag;
	
	public void afterPropertiesSet() {
		//tests();
	}
	
	/**
	 * convert the mail body given as a String into a MailToSmsMessage.
	 * @param messageBody
	 * @return
	 */
	public SmsMessage convertMessageBodyToMailToSmsMessage(String messageBody) throws ParsingMessageBodyException {
		messageBody = mayRemoveSignature(messageBody).trim();

		List<String> weirdTags = new LinkedList<String>();	
		Map<String, String> tag2value = parseTagAndGetValues(messageBody, weirdTags);

		checkValues(tag2value, weirdTags);

		final SmsMessage smsMessage = createSmsMessageFromValues(tag2value);
		
		if (logger.isDebugEnabled()) {
			logger.debug("convertMessageBodyToMailToSmsMessage return value : \n" + 
				     " - recipient list : " + smsMessage.getPhoneNumbers() + "\n" + 
				     " - account : " + smsMessage.getAccount() + "\n" + 
				     " - content : " + smsMessage.getContent() + "\n");
		}	
		return smsMessage;
		
	}

	private SmsMessage createSmsMessageFromValues(Map<String, String> tag2value) {
		SmsMessage msg = new SmsMessage();
		msg.setPwd(getSimplifiedString(tag2value, pwdTag));
		msg.setPhoneNumbers(getRecipientsFromString(getSimplifiedString(tag2value, recipientsTag)));
		msg.setAccount(getSimplifiedString(tag2value, accountTag));
		msg.setContent(getSimplifiedString(tag2value, contentTag));
		return msg;
	}

	private void checkValues(Map<String, String> tag2value, List<String> weirdTags) throws ParsingMessageBodyException {
		Set<String> nonEmptyTags = nonEmptyTags();
		Set<String> mandatoryTags = nonEmptyTags;

		for (Entry<String, String> e : tag2value.entrySet()) {
			String tag = e.getKey();
			String val = e.getValue();
			if (val == null) {
				if (mandatoryTags.contains(tag)) {
					throw new ParsingMessageBodyException.MissingMandatoryTag(tag, weirdTags, tag2value.get(""));
				}
			} else if (val.trim().equals("")) {
				if (nonEmptyTags.contains(tag))
					throw new ParsingMessageBodyException("ERROR.PARSING.EMPTY.TAG", tag);
			}
		}
	}

	private String[] knownTags() {
		String[] l = { pwdTag, accountTag, recipientsTag, contentTag };
		return l;
	}

	private Set<String> nonEmptyTags() {
		String[] l = { pwdTag, recipientsTag, contentTag };
		return new HashSet<String>(Arrays.asList(l));
	}

	private Map<String, String> tag2nullValue() {
		Map<String, String> r = new HashMap<String, String>();
		for (String tag : knownTags()) r.put(tag, null);
		return r;
	}

	private Map<String, String> parseTagAndGetValues(String messageBody, List<String> weirdTags) throws ParsingMessageBodyException {
		Pattern tag_pattern = Pattern.compile(TAG_PATTERN);

		Map<String, String> tag2value = tag2nullValue();
		tag2value.put("", null);
		String currentTag = "";
		while (!messageBody.equals("")) {
			String value;
			String nextTag = null;
			Matcher matcher = tag_pattern.matcher(messageBody);
			if (matcher.find()) {
				String before = messageBody.substring(0, matcher.start());
				String tag = matcher.group(1).toLowerCase();

				if (tag2value.containsKey(tag)) {
					if (!tag.equals(currentTag)) {
						nextTag = tag;
					}
					value = before;
				} else {
					weirdTags.add(tag);
					// ignore this tag:
					value = before + matcher.group();
				}
				messageBody = messageBody.substring(matcher.end());
			} else {
				// no more tags
				value = messageBody;
				messageBody = "";
			}
			value = replaceCarriageReturn(value);

			String prev = tag2value.get(currentTag);
			tag2value.put(currentTag, (prev == null ? "" : prev + " ") + value);

			if (nextTag != null) currentTag = nextTag;
		}
		return tag2value;
	}

	private String mayRemoveSignature(String messageBody) {
		int pos = messageBody.indexOf(endTag);
		if (pos != -1) {
			if (logger.isDebugEnabled())
				logger.debug("Signature removed : " + messageBody.substring(pos));
			return messageBody.substring(0, pos);
		} else {
			return messageBody;
		}
	}
	
	/**
	 * Extract a recipients list from a String.
	 * @param recipientsAsStringWithTag
	 * @return
	 */
	private List<String> getRecipientsFromString(final String recipientsAsString) {	
		final String[] phoneNumbers = recipientsAsString.split(COMMA_SEPARATOR_REGULAR_EXPR);

		List<String> retVal  = new LinkedList<String>();
		for (String phoneNumber : phoneNumbers) {
			phoneNumber = phoneNumber.trim();

			// if phone number has a valid format, add it to the return value 
			if (Pattern.matches(phoneNumberPattern, phoneNumber)) {
				retVal.add(phoneNumber);
			} else {
				logger.warn("Invalid recipient phone number found : " + phoneNumber + "\n" + 
					    "The phone number is rejected");
			}

		}

		return retVal;
	}
		
	/**
	 * replace carriage return in the specified string.
	 * @param str
	 * @return
	 */
	private String replaceCarriageReturn(final String str) {
		return str.replaceAll("\r?\n", " ");
	}

	/**
	 * extract a simplified value.
	 * @param s
	 * @return the value or null.
	 */
	private String getSimplifiedString(Map<String,String> tag2value, String tag) {
		String r = tag2value.get(tag);
		return r != null && r.length() > 0 ? r : null;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param recipientsTag
	 */
	public void setRecipientsTag(final String recipientsTag) {
		this.recipientsTag = recipientsTag;
	}

	/**
	 * Standard setter used by spring.
	 * @param accountTag
	 */
	public void setAccountTag(final String accountTag) {
		this.accountTag = accountTag;
	}

	/**
	 * Standard setter used by spring.
	 * @param contentTag
	 */
	public void setContentTag(final String contentTag) {
		this.contentTag = contentTag;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param phoneNumberPattern
	 */
	public void setPhoneNumberPattern(final String phoneNumberPattern) {
		this.phoneNumberPattern = phoneNumberPattern;
	}
	
	/**
	 * Standard setter used by spring.
	 * @param pwdTag
	 */
	public void setPwdTag(final String pwdTag) {
		this.pwdTag = pwdTag;
	}

	/**
	 * Standard setter used by spring.
	 * @param endTag
	 */
	public void setEndTag(final String endTag) {
		this.endTag = endTag;
	}

	public static String join(Iterable<?> elements, CharSequence separator) {
		if (elements == null) return "";

		StringBuilder sb = null;

		for (Object s : elements) {
			if (sb == null)
				sb = new StringBuilder();
			else
				sb.append(separator);
			sb.append(s);			
		}
		return sb == null ? "" : sb.toString();
	}

	@SuppressWarnings("unused")
	private void tests() {
		String test1 =
			"paSSwd=xxx\n" +
			" destinataires=0652767674\n" +
			"compte = mail2sms.crir.paris1\n" +
			"message =  test 13h38 (blacklisted)\n" +
			"citron  d'\u00E9t\u00E9 \r\n" +
			"!!!!! \n";

		String test2 =
			"destinataires=0652767674 passwd=xxx\n" +
			"message=a\n" +
			"message=b\n";

		String test3 =
			"foobar zzzz=0652767674 passwd=xxx\n" +
			"message=a\n" +
			"message=b\n";

		String test4 =
			"presque vide";

		String test_vide =
			"";

		String[] tests = { test1, test2, test3, test4, test_vide };

		for (String test : tests) {
			try {
				convertMessageBodyToMailToSmsMessage(test);
			} catch (ParsingMessageBodyException e) {
				logger.error(e);
			}
		}
	}

}
