package com.ontimize.gui.field.document;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.field.DateDataField;
import com.ontimize.gui.i18n.Internationalization;

/**
 * This document implements the model for managing real numbers in a JTextField
 *
 * @version 1.0 01/04/2001
 */
public class RealDocument extends PlainDocument implements Internationalization {

    private static final Logger logger = LoggerFactory.getLogger(RealDocument.class);

    protected DecimalFormatSymbols symbology = new DecimalFormatSymbols();

    protected NumberFormat formatter = NumberFormat.getInstance();

    protected Double floatValue = null;

    public RealDocument() {
        super();
        this.formatter.setMaximumFractionDigits(10);
    }

    public void setValue(Number value) {
        try {
            this.remove(0, this.getLength());
            // Format
            String stringValue = this.formatter.format(value);
            this.insertString(0, stringValue, null);
        } catch (Exception e) {
            if (ApplicationManager.DEBUG) {
                RealDocument.logger.debug(null, e);
            }
        }
    }

    public void format() {
        try {
            String currentText = this.getText(0, this.getLength());
            if (currentText.length() == 0) {
                return;
            }
            if ("-".equalsIgnoreCase(currentText)) {
                currentText = "0";
            }
            Number number = this.formatter.parse(currentText);
            this.remove(0, this.getLength());
            this.insertString(0, this.formatter.format(number).toString(), null);
        } catch (Exception e) {
            if (ApplicationManager.DEBUG) {
                RealDocument.logger.debug(null, e);
            }
        }
    }

    public Number getValue() {
        this.updateValue();
        return this.floatValue;
    }

    public Double getDoubleValue(String s) {
        try {
            Number number = this.formatter.parse(s);
            return new Double(number.doubleValue());
        } catch (Exception e) {
            RealDocument.logger.trace(null, e);
            return null;
        }
    }

    protected void updateValue() {

        Double previousValue = this.floatValue;
        try {
            String currentText = this.getText(0, this.getLength());
            if (currentText.length() == 0) {
                this.floatValue = new Double(0);
            } else {
                Number number = this.formatter.parse(currentText);
                this.floatValue = new Double(number.doubleValue());
            }
        } catch (Exception e) {
            RealDocument.logger.trace(null, e);
            this.floatValue = previousValue;
        }
    }

    public boolean isValid() {
        try {
            if (this.getLength() == 0) {
                return true;
            }
            String currentText = this.getText(0, this.getLength());
            this.formatter.parse(currentText);
            return true;
        } catch (Exception e) {
            if (ApplicationManager.DEBUG) {
                RealDocument.logger.debug(null, e);
            }
            return false;
        }
    }

    public void setFractionDigits(int number) {
        this.formatter.setMaximumFractionDigits(number);
    }

    @Override
    public void insertString(int offset, String sringValue, AttributeSet attributes) throws BadLocationException {
        if (sringValue.length() == 0) {
            return;
        }
        // Use the system separator (. or ,)
        char decimalSeparator = this.symbology.getDecimalSeparator();
        // First comprobation:
        if (sringValue.length() == 1) {
            // Checks that it is a numeric character
            if (!Character.isDigit(sringValue.charAt(0)) && (sringValue.charAt(0) != decimalSeparator)) {
                if ((sringValue.charAt(0) == '-') && (offset == 0)) {
                    try {
                        super.insertString(offset, sringValue, attributes);
                    } catch (Exception e) {
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            RealDocument.logger.error(null, e);
                        }
                    }
                } else {
                    return;
                }
            } else {
                // Only one decimal separator is allowed
                try {
                    int length = this.getLength();
                    boolean separatorExist = false;
                    String text = this.getText(0, length);
                    for (int i = 0; i < length; i++) {
                        if (text.charAt(i) == decimalSeparator) {
                            separatorExist = true;
                            break;
                        }
                    }
                    if (((sringValue.charAt(0) == decimalSeparator) && (offset == 0))
                            || ((sringValue.charAt(0) == decimalSeparator) && separatorExist)) {
                        return;
                    }
                    StringBuilder currentText = new StringBuilder(this.getText(0, this.getLength()));
                    currentText.insert(offset, sringValue);
                    Number number = this.formatter.parse(currentText.toString());
                    Double previousValue = this.floatValue;
                    this.floatValue = new Double(number.doubleValue());

                    try {
                        super.insertString(offset, sringValue, attributes);
                    } catch (Exception ex) {
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            RealDocument.logger.debug(this.getClass().toString() + ": " + ex.getMessage(), ex);
                        }
                        this.floatValue = previousValue;
                        return;
                    }
                } catch (Exception e) {
                    if (com.ontimize.gui.ApplicationManager.DEBUG) {
                        RealDocument.logger.debug(null, e);
                    }
                }
            }
        } else {
            // Check that the result string is a valid number
            try {
                StringBuilder currentText = new StringBuilder(this.getText(0, this.getLength()));
                currentText.insert(offset, sringValue);
                try {
                    Number number = this.formatter.parse(currentText.toString());
                    Double previousValue = this.floatValue;
                    this.floatValue = new Double(number.doubleValue());
                    try {
                        super.insertString(offset, sringValue, attributes);
                    } catch (BadLocationException e) {
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            RealDocument.logger.debug(null, e);
                        }
                        this.floatValue = previousValue;
                    }
                } catch (ParseException ex) {
                    if (com.ontimize.gui.ApplicationManager.DEBUG) {
                        RealDocument.logger.debug(this.getClass().toString() + ": " + ex.getMessage(), ex);
                    }
                    return;
                }
            } catch (Exception e) {
                if (com.ontimize.gui.ApplicationManager.DEBUG) {
                    RealDocument.logger.debug(null, e);
                }
                return;
            }
        }
    }

    @Override
    public void remove(int offset, int len) throws BadLocationException {
        // Delete. First in the buffer because of the events
        try {
            Double previousValue = this.floatValue;
            StringBuilder currentText = new StringBuilder(this.getText(0, this.getLength()));
            currentText.delete(offset, offset + len);
            if ((currentText.length() == 0) || currentText.toString().equals("-")) {
                this.floatValue = new Double(0);
            } else {
                Number number = this.formatter.parse(currentText.toString());
                this.floatValue = new Double(number.doubleValue());
            }
            try {
                super.remove(offset, len);
            } catch (Exception e) {
                if (ApplicationManager.DEBUG) {
                    RealDocument.logger.debug(null, e);
                }
                this.floatValue = previousValue;
            }
        } catch (Exception e) {
            if (ApplicationManager.DEBUG) {
                RealDocument.logger.debug(null, e);
            }
        }
    }

    public void setMinimumIntegerDigits(int number) {
        this.formatter.setMinimumIntegerDigits(number);
    }

    public void setMaximumIntegerDigits(int number) {
        this.formatter.setMaximumIntegerDigits(number);
    }

    public void setGrouping(boolean group) {
        this.formatter.setGroupingUsed(group);
    }

    public void setMinimumFractionDigits(int number) {
        this.formatter.setMinimumFractionDigits(number);
    }

    public void setMaximumFractionDigits(int number) {
        this.formatter.setMaximumFractionDigits(number);
    }

    protected void insertStringWithoutCheck(int offset, String stringValue, AttributeSet attributes)
            throws BadLocationException {
        super.insertString(offset, stringValue, attributes);
    }

    protected void removeWithoutCheck(int offset, int length) throws BadLocationException {
        super.remove(offset, length);
    }

    public NumberFormat getFormat() {
        return this.formatter;
    }

    public char getDecimalSeparator() {
        return this.symbology.getDecimalSeparator();
    }

    @Override
    public void setComponentLocale(Locale loc) {
        Locale l = DateDataField.getSameCountryLocale(loc);
        this.symbology = new DecimalFormatSymbols(l);
        int minimunFractionDigits = this.formatter.getMinimumFractionDigits();
        int maximumFractionDigits = this.formatter.getMaximumFractionDigits();

        int minimunIntegerDigits = this.formatter.getMinimumIntegerDigits();
        int maximumIntegerDigits = this.formatter.getMaximumIntegerDigits();

        boolean g = this.formatter.isGroupingUsed();
        this.formatter = NumberFormat.getInstance(l);
        this.formatter.setMaximumFractionDigits(maximumFractionDigits);
        this.formatter.setMinimumFractionDigits(minimunFractionDigits);
        this.formatter.setMaximumIntegerDigits(maximumIntegerDigits);
        this.formatter.setMinimumIntegerDigits(minimunIntegerDigits);
        this.formatter.setGroupingUsed(g);
    }

    @Override
    public Vector getTextsToTranslate() {
        return new Vector();
    }

    @Override
    public void setResourceBundle(ResourceBundle res) {
    }

}
