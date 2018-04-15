package com.shortn0tes.povdisplay;

import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;

/**
 * Created on 11/24/2017.
 */
public class NumericTextField extends TextField {

	private static final Logger LOGGER = Logger.getLogger(NumericTextField.class.getName());

	private long number;

	public NumericTextField() {
		textProperty().addListener(getNumericListenerForField());
	}

	public NumericTextField(String text) {
		super(text);
		textProperty().addListener(getNumericListenerForField());
	}

	public long getNumber() {
		return number;
	}

	private ChangeListener<String> getNumericListenerForField() {
		return (observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				setText(newValue.replaceAll("[^\\d]", ""));
			}
			String text = getText();
			if (text.length() > 0) {
				try {
					number = Long.parseLong(text);
				} catch (NumberFormatException nfe) {
					LOGGER.warning("Cannot parse number in numeric field " + getId());
					number = 0;
				}
			} else {
				number = 0;
			}
		};
	}
}
