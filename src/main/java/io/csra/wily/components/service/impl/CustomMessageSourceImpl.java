package io.csra.wily.components.service.impl;

import java.util.Locale;

import io.csra.wily.components.service.CustomMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component("messageSource")
public class CustomMessageSourceImpl extends ResourceBundleMessageSource implements CustomMessageSource {

	protected static final String MESSAGE_BASENAME = "validationMessages";
	protected static final String DEFAULT_ENCODING = "UTF-8";
	protected static final Locale DEFAULT_LOCALE = Locale.US;

	public CustomMessageSourceImpl() {
		this.setBasename(MESSAGE_BASENAME);
		this.setDefaultEncoding(DEFAULT_ENCODING);
	}

	public String getMessage(String msg) {
		return super.getMessage(msg, null, DEFAULT_LOCALE);
	}

	public String getMessage(String msg, String[] replacementValues) {
		return super.getMessage(msg, replacementValues, DEFAULT_LOCALE);
	}

}
