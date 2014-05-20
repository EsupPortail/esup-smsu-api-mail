package org.esupportail.smsuapimail.exceptions;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.esupportail.commons.services.i18n.I18nService;

@SuppressWarnings("serial")
public class ParsingMessageBodyException extends I18nException {

	private String i18nKey;
	private String[] params;

	public ParsingMessageBodyException(final String i18nKey, final String[] params) {
		super(i18nKey);
		this.i18nKey = i18nKey;
		this.params = params;
	}
	public ParsingMessageBodyException(final String i18nKey, final String param1) {
		this(i18nKey, new String[] { param1 });
	}
	public ParsingMessageBodyException(final String i18nKey, final String param1, final String param2) {
		this(i18nKey, new String[] { param1, param2 });
	}

	public String toString() {
		return "ParsingMessageBodyException" + "_" + i18nKey + "_" + join(params, "_");
	}

	public String toI18nString(I18nService i18nService) {
		return i18nService.getString(i18nKey, (Object[]) params);
	}

	static public class MissingMandatoryTag extends ParsingMessageBodyException {

		private List<String> weirdTags;
		private String textBeforeAnyTag;
		
		public MissingMandatoryTag(String tag, List<String> weirdTags, String textBeforeAnyTag) {
			super("ERROR.PARSING.MISSING.MANDATORY.TAG", new String[] { tag });
			this.weirdTags = weirdTags;
			this.textBeforeAnyTag = textBeforeAnyTag;
		}

		public String toString() {
			return "ParsingMessageBodyException.MissingMandatoryTag(weirdTags=\"" + weirdTags + "\", textBeforeAnyTag=\"" + textBeforeAnyTag + "\")";
		}

		public String toI18nString(I18nService i18nService) {
			String s = super.toI18nString(i18nService);
			if (!weirdTags.isEmpty())
				s += i18nService.getString(
					weirdTags.size() > 1 ?
					"ERROR.PARSING.MISSING.MANDATORY.TAG.SUGGESTION.WEIRD_TAGS" :
					"ERROR.PARSING.MISSING.MANDATORY.TAG.SUGGESTION.WEIRD_TAG", join(weirdTags, ", "));
			if (!StringUtils.isBlank(textBeforeAnyTag))
				s += i18nService.getString("ERROR.PARSING.MISSING.MANDATORY.TAG.SUGGESTION.DROPPED_TEXT", textBeforeAnyTag);
			return s;
		}

	}

	public static String join(Object[] elements, CharSequence separator) {
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

}
