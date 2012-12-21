package org.aperteworkflow.util.vaadin.text;

import java.util.Locale;


public class CurrencyTextField extends NumberTextField {

	public CurrencyTextField() {
		super();
	}

	public CurrencyTextField(String caption) {
		super(caption);
	}

	public CurrencyTextField(Locale locale, String notDoubleErrorMessage) {
		super();
		setLocale(locale);
		if(notDoubleErrorMessage != null)
			addValidator(new LocalizedDoubleValidator(notDoubleErrorMessage));
	}

	public CurrencyTextField(String caption, Locale locale, String notDoubleErrorMessage) {
		this(caption, locale, notDoubleErrorMessage, false);
	}
	
	public CurrencyTextField(String caption, Locale locale, String notDoubleErrorMessage, boolean allowsNegative) {
		super(caption);
		setAllowsNegative(allowsNegative);
		setLocale(locale);
		if(notDoubleErrorMessage != null)
			addValidator(new LocalizedDoubleValidator(notDoubleErrorMessage));
	}
	
	@Override
	protected String getDecimalFormatString(){
		return "#0.00";
	}
}
