package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.field.document.CIFDocument;
import com.ontimize.jee.common.db.NullValue;

/**
 * The class to create the CIF (Código de Identificación Fiscal). The control letter is
 * automatically calculated.
 *
 * @author Imatia Innovation
 */
public class CIFDataField extends TextFieldDataField {

	private static final Logger logger = LoggerFactory.getLogger(CIFDataField.class);

	/**
	 * The mask that contains a valid CIF format.
	 */
	static final String mask = "A0000000%";

	/**
	 * The class constructor. Initializes parameters and adds a <code>Document Listener<code> to the field. <p>
	 *
	 * &#64;param parameters the <code>Map</code> with parameters
	 */
	public CIFDataField(Map<Object, Object> parameters) {
		this.init(parameters);
		((JTextField) this.dataField).setDocument(new CIFDocument());
		Document doc = ((JTextField) this.dataField).getDocument();
		doc.addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent event) {
				CIFDataField.this.checkCIF();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				CIFDataField.this.checkCIF();
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				CIFDataField.this.checkCIF();
			}
		});

		((JTextField) this.dataField).addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent evento) {
				CIFDataField.this.checkCIF();
			}

			public void focusGain(FocusEvent event) {
				CIFDataField.this.checkCIF();
			}
		});
		((JTextField) this.dataField).addActionListener(event -> CIFDataField.this.transferFocus());
	}

	/**
	 * Initializes parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters.
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		super.init(parameters);
		if (parameters.get(DataField.SIZE) == null) {
			((JTextField) this.dataField).setColumns(CIFDataField.mask.length());
		}
	}

	/**
	 * Checks whether the letter is correct. In correct case sets color to black, and in other case
	 * changes to red.
	 *
	 * @see CIFDocument#isCIFWellFormed(String)
	 */
	protected void checkCIF() {
		CIFDocument doc = (CIFDocument) ((JTextField) this.dataField).getDocument();
		try {
			int length = doc.getLength();
			if (length > 8) {
				boolean correct = CIFDocument.isCIFWellFormed(doc.getText(0, length));
				if (!correct) {
					((JTextField) this.dataField).setForeground(Color.red);
				} else {
					if (this.isRequired()) {
						((JTextField) this.dataField).setForeground(DataField.requiredFieldForegroundColor);
					} else {
						((JTextField) this.dataField).setForeground(this.fontColor);
					}
				}
			} else {
				((JTextField) this.dataField).setForeground(Color.red);
			}
		} catch (Exception e) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				CIFDataField.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns a string with the CIF.
	 * <p>
	 * @return a string with values
	 */
	@Override
	public Object getValue() {
		if (this.isEmpty()) {
			return null;
		}

		CIFDocument document = (CIFDocument) ((JTextField) this.dataField).getDocument();
		String sValue = null;
		try {
			sValue = document.getText(0, document.getLength());
			return sValue;
		} catch (Exception e) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				CIFDataField.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
			}
			return null;
		}
	}

	/**
	 * Sets the CIF value to TextField.
	 * <p>
	 * @param value the string object with CIF value
	 */
	@Override
	public void setValue(Object value) {
		this.setInnerListenerEnabled(false);
		Object oldValue = this.getValue();
		if ((value == null) || (value instanceof NullValue)) {
			this.deleteData();
		} else {
			CIFDocument document = (CIFDocument) ((JTextField) this.dataField).getDocument();
			try {
				document.remove(0, document.getLength());
				document.insertString(0, (String) value, null);
			} catch (Exception e) {
				if (com.ontimize.gui.ApplicationManager.DEBUG) {
					CIFDataField.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
			}
			this.valueSave = this.getValue();
			this.fireValueChanged(this.valueSave, oldValue, ValueEvent.PROGRAMMATIC_CHANGE);
			this.setInnerListenerEnabled(true);
		}
		this.valueSave = this.getValue();
	}

	/**
	 * Calculates the CIF number.
	 * <p>
	 * @param number the CIF number
	 * @return the calculated letter
	 */
	protected char calculateLetter(int number) {
		return CIFDocument.calculateLetter(number);
	}

	/**
	 * Calculates the CIF number.
	 * <p>
	 *
	 * @see CIFDocument#isCIFWellFormed(String)
	 * @param number the CIF number
	 * @return the calculated number
	 */
	protected char calculateNumber(int number) {
		return CIFDocument.calculateNumber(number);
	}

	/**
	 * Gets the SQL data type.
	 * <p>
	 *
	 * @see java.sql.Types#VARCHAR
	 * @return the SQL Type for VARCHAR
	 */
	@Override
	public int getSQLDataType() {
		return java.sql.Types.VARCHAR;
	}

	/**
	 * Checks whether CIF is well formed.
	 * <p>
	 * @param CIF the CIf in a String
	 * @return the well-formed condition
	 */
	public static boolean isCIFWellFormed(String CIF) {
		return CIFDocument.isCIFWellFormed(CIF);
	}

}
