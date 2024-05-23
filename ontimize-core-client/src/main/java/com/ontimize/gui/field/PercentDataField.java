package com.ontimize.gui.field;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.field.document.PercentDocument;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.util.ParseUtils;

/**
 * The main class for creating a percentage data field.
 * <p>
 *
 * @author Imatia Innovation
 */

public class PercentDataField extends TextFieldDataField {

    private static final Logger logger = LoggerFactory.getLogger(PercentDataField.class);

    /**
     * The class constructor. It initializes the parameters. By default, the right alignment and if
     * parameter 'size' exists, its values is fixed to 3 in <code>Map</code> .
     * <p>
     * @param parameters the <code>Map</code> with parameters from XML definition.
     */
    public PercentDataField(Map<Object, Object> parameters) {
        super();
        if (!parameters.containsKey("size")) {
            parameters.put("size", "3");
        }
        this.init(parameters);
        if (this.textAlignment != -1) {
            ((JTextField) this.dataField).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        ((JTextField) this.dataField).addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                if (!PercentDataField.this.isEmpty()) {
                    PercentDataField.this.format();
                }
            }
        });

        if (this.dataField instanceof EJTextField) {
            ((EJTextField) this.dataField).setReplaceDecimalSeparator(true);

        }
    }

    /**
     * Calls to <code>super()</code> to initialize parameters.
     * <p>
     * @param parameters <code>Map</code> for initialization parameters.
     *        <p>
     *        <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
     *        <tr>
     *        <td><b>attribute</td>
     *        <td><b>values</td>
     *        <td><b>default</td>
     *        <td><b>required</td>
     *        <td><b>meaning</td>
     *        </tr>
     *        <tr>
     *        <td>minintegerdigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The minimum number of integer digits.</td>
     *        </tr>
     *        <tr>
     *        <td>maxintegerdigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The maximum number of integer digits.</td>
     *        </tr>
     *        <tr>
     *        <td>mindecimaldigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The minimum number of decimal digits.</td>
     *        </tr>
     *        <tr>
     *        <td>maxdecimaldigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The maximum number of decimal digits.</td>
     *        </tr>
     *        <tr>
     *        <td>limit100</td>
     *        <td>yes/no</td>
     *        <td>yes</td>
     *        <td>no</td>
     *        <td>Allow to insert higher amounts to 100%</td>
     *        </tr>
     *        </table>
     */
    @Override
    public void init(Map<Object, Object> parameters) {
        super.init(parameters);

        ((JTextField) this.dataField).setDocument(new PercentDocument());
        Object minintegerdigits = parameters.get("minintegerdigits");
        if (minintegerdigits != null) {
            try {
                int minimum = Integer.parseInt(minintegerdigits.toString());
                PercentDocument doc = (PercentDocument) ((JTextField) this.dataField).getDocument();
                doc.setMinimumIntegerDigits(minimum);
            } catch (Exception e) {
                PercentDataField.logger.error("Error in parameter 'minintegerdigits' " + e.getMessage(), e);
            }
        }

        Object maxintegerdigits = parameters.get("maxintegerdigits");
        if (maxintegerdigits != null) {
            try {
                int maximum = Integer.parseInt(maxintegerdigits.toString());
                PercentDocument doc = (PercentDocument) ((JTextField) this.dataField).getDocument();
                doc.setMaximumIntegerDigits(maximum);
            } catch (Exception exc) {
                PercentDataField.logger.error("Error in parameter 'maxintegerdigits' ", exc);
            }
        }

        Object mindecimaldigits = parameters.get("mindecimaldigits");
        if (mindecimaldigits != null) {
            try {
                int minimum = Integer.parseInt(mindecimaldigits.toString());
                PercentDocument doc = (PercentDocument) ((JTextField) this.dataField).getDocument();
                doc.setMinimumFractionDigits(minimum);
            } catch (Exception exc) {
                PercentDataField.logger.error("Error in parameter 'mindecimaldigits' ", exc);
            }
        }

        Object maxdecimaldigits = parameters.get("maxdecimaldigits");
        if (maxdecimaldigits != null) {
            try {
                int maximum = Integer.parseInt(maxdecimaldigits.toString());
                PercentDocument doc = (PercentDocument) ((JTextField) this.dataField).getDocument();
                doc.setMaximumFractionDigits(maximum);
            } catch (Exception e) {
                PercentDataField.logger.error("Error in parameter 'maxdecimaldigits' ", e);
            }
        }

        try {
            boolean limit100 = ParseUtils.getBoolean((String) parameters.get("limit100"), true);
            PercentDocument doc = (PercentDocument) ((JTextField) this.dataField).getDocument();
            doc.setLimit100(limit100);
        } catch (Exception exc) {
            PercentDataField.logger.error("Error in parameter 'limit100' ", exc);
        }

    }

    /**
     * The format to field.
     * <p>
     *
     * @see PercentDocument#format()
     */
    protected void format() {
        boolean selectAll = this.isSelectedAll();
        try {
            Object oNewValue = this.getValue();
            this.setInnerListenerEnabled(false);
            PercentDocument document = (PercentDocument) ((JTextField) this.dataField).getDocument();
            document.format();
            this.setInnerListenerEnabled(true);
            if (!this.isInnerValueEqual(oNewValue)) {
                this.fireValueChanged(oNewValue, this.getInnerValue(), ValueEvent.USER_CHANGE);
                this.setInnerValue(oNewValue);
            }
        } catch (Exception ex) {
            PercentDataField.logger.trace(null, ex);
        } finally {
            if (selectAll) {
                ((JTextField) this.dataField).selectAll();
            }
            this.setInnerListenerEnabled(true);
        }
    }

    /**
     * The method to get the number value.
     * <p>
     *
     * @see JTextComponent#getDocument()
     * @return a numerical value
     */
    public Number getNumericalValue() {
        if (this.isEmpty()) {
            return null;
        }
        PercentDocument document = (PercentDocument) ((JTextField) this.dataField).getDocument();
        return document.getValue();
    }

    /**
     * If value is a number, its value is set to 'value'. It must be between 0 and 1. In other case,
     * deletes the field value.
     * <p>
     * @param value the object to set values
     */
    @Override
    public void setValue(Object value) {
        if ((value == null) || (value instanceof NullValue)) {
            this.deleteData();
            return;
        }
        this.setInnerListenerEnabled(false);
        Object oPreviousValue = this.getNumericalValue();
        if (value instanceof Number) {

            PercentDocument document = (PercentDocument) ((JTextField) this.dataField).getDocument();
            if (document.isLimit100()) {
                if (((Number) value).doubleValue() > 1.0) {
                    value = new Double(0.0);
                }
            }
            document.setValue((Number) value);
            this.valueSave = this.getNumericalValue();
            this.setInnerValue(this.valueSave);
            this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
            this.setInnerListenerEnabled(true);
        } else {
            this.deleteData();
        }
    }

    /**
     * The method to get the value in a object.
     * <p>
     *
     * @see JTextComponent#getDocument()
     * @return a numerical value
     */
    @Override
    public Object getValue() {
        if (this.isEmpty()) {
            return null;
        }
        PercentDocument document = (PercentDocument) ((JTextField) this.dataField).getDocument();
        return document.getValue();
    }

    /**
     * The method to get the integer SQL types for double type.
     * <p>
     *
     * @returns the return type according to double SQL type
     */
    @Override
    public int getSQLDataType() {
        return java.sql.Types.DOUBLE;
    }

}
