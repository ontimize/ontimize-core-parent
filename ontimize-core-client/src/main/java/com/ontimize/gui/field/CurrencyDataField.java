package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.SearchValue;
import com.ontimize.gui.SelectCurrencyValues;
import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.field.document.AdvancedRealDocument;
import com.ontimize.gui.field.document.CurrencyDocument;
import com.ontimize.gui.field.document.RealDocument;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.help.HelpUtilities;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.calc.Calculator;

/**
 * This class implements a field to introduce coins. <br>
 * The base coin is EURO, so the <code>setValue()</code> and <code>getValue()</code> methods makes
 * operation in that unit. <br>
 * It is supported the change operation mode between Euro and Pesetas (1 Euro = 166.386 pta)
 * <p>
 *
 * @author Imatia Innovation
 */
public class CurrencyDataField extends TextFieldDataField
        implements OpenDialog, SelectCurrencyValues, AdvancedDataComponent {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyDataField.class);

    public static final String CURRENCY = "currency";

    /**
     * The default visibility condition for euro button. By default, false.
     * @deprecated
     */
    @Deprecated
    public static boolean DEFAULT_EURO_BUTTON_VISIBLE = false;

    // Icons
    /**
     * The euro icon. By default, null.
     * @deprecated
     */
    @Deprecated
    protected ImageIcon euroIcon = null;

    /**
     * The pta icon. By default, null.
     * @deprecated
     */
    @Deprecated
    protected ImageIcon pstIcon = null;

    /**
     * An instance of Euro.
     * @deprecated Symbol is defined in document
     */
    @Deprecated
    protected String euro = new String("Euro");

    /**
     * An instance of Pta.
     * @deprecated Symbol is defined in document
     */
    @Deprecated
    protected String pst = "Pta";

    /**
     * An instance of currency button. By default, null.
     * @deprecated
     */
    @Deprecated
    protected JButton currencyButton = null;

    /**
     * The condition to show euro coin. By default, true.
     * @deprecated
     */
    @Deprecated
    protected boolean showEuros = false;

    // private static JTextField textFieldAux = new EJTextField();

    /**
     * A reference for advanced help menu. By default, null.
     */
    protected JMenuItem advancedHelpBMenu = null;

    /**
     * A reference for the frame. By default, null.
     */
    protected Frame frame = null;

    /**
     * A reference for a calc button. By default, null.
     */
    protected JButton calcButton = null;

    /**
     * A calculator reference. By default, null.
     */
    protected Calculator calc = null;

    /**
     * The class constructor. Inits parameters and installs a validation document listener.
     * <p>
     * @param parameters the <code>Hashtable</code> with parameters.
     */
    public CurrencyDataField(Hashtable parameters) {
        this.init(parameters);
        try {
            this.euro = new String("�");
        } catch (Exception e) {
            if (com.ontimize.gui.ApplicationManager.DEBUG) {
                CurrencyDataField.logger.error(null, e);
            } else {
                CurrencyDataField.logger.trace(null, e);
            }
            this.euro = "Euro";
        }
        if (this.textAlignment == -1) {
            ((JTextField) this.dataField).setHorizontalAlignment(SwingConstants.RIGHT);
        }

        ((JTextField) this.dataField).addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent evento) {
                if ((!CurrencyDataField.this.isEmpty()) && (!evento.isTemporary())
                        && ((EJTextField) CurrencyDataField.this.dataField).setCaretPositionOnFocusLost) {
                    try {
                        CurrencyDataField.this.setInnerListenerEnabled(false);
                        Number numberValue = ((CurrencyDocument) ((JTextField) CurrencyDataField.this.dataField)
                            .getDocument()).getValue();
                        ((CurrencyDocument) ((JTextField) CurrencyDataField.this.dataField).getDocument()).format();
                        Number numberValue2 = ((CurrencyDocument) ((JTextField) CurrencyDataField.this.dataField)
                            .getDocument()).getValue();
                        if ((numberValue == null) && (numberValue2 == null)) {
                            // There is not change
                        } else if ((numberValue == null) || (numberValue2 == null)) {
                            // Change
                            CurrencyDataField.this.fireValueChanged(numberValue2, numberValue, ValueEvent.USER_CHANGE);
                        } else if (!numberValue.equals(numberValue2)) {
                            CurrencyDataField.this.fireValueChanged(numberValue2, numberValue, ValueEvent.USER_CHANGE);
                        }
                    } catch (Exception e) {
                        CurrencyDataField.logger.trace(null, e);
                    } finally {
                        CurrencyDataField.this.setInnerListenerEnabled(true);
                    }
                }
            }
        });
        this.installValidationDocumentListener();
    }

    /**
     * Initializes parameters.
     * <p>
     * @param parameters the <code>Hashtable</code> with parameters.
     *
     *        <p>
     *
     *
     *        <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
     *        <tr>
     *        <td><b>attribute</td>
     *        <td><b>values</td>
     *        <td><b>default</td>
     *        <td><b>required</td>
     *        <td><b>meaning</td>
     *        </tr>
     *
     *        <tr>
     *        <td>change</td>
     *        <td></i>yes/no</td>
     *        <td></td>
     *        <td>yes</td>
     *        <td>To support the change in the currency value.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>calc</td>
     *        <td>yes/no</td>
     *        <td>no</td>
     *        <td>no</td>
     *        <td>The presence of the calendar.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>minintegerdigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The minimum number of integer digits.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>maxintegerdigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The maximum number of integer digits.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>mindecimaldigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The minimum number of decimal digits.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>maxdecimaldigits</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The maximum number of decimal digits.</td>
     *        </tr>
     *
     *        <tr>
     *        <td>grouping</td>
     *        <td><i>yes/no</td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The condition to put the numbers in groups (1 000 000 -> 1000000)</td>
     *        </tr>
     *        </Table>
     */
    @Override
    public void init(Hashtable parameters) {
        super.init(parameters);

        String currencySymbol = ParseUtils.getString((String) parameters.get(CurrencyDataField.CURRENCY),
                CurrencyDocument.defaultCurrencySymbol);
        CurrencyDocument doc = this.createDocument(currencySymbol);
        ((JTextField) this.dataField).setDocument(doc);
        // this.showEuros = doc.getShownEuros();
        this.euro = doc.currencySymbol;
        this.pst = doc.currencySymbol;

        Object minintegerdigits = parameters.get("minintegerdigits");
        if (minintegerdigits != null) {
            try {
                int minimum = Integer.parseInt(minintegerdigits.toString());
                doc.setMinimumIntegerDigits(minimum);
            } catch (Exception e) {
                CurrencyDataField.logger.error("Error 'minintegerdigits' parameter" + e.getMessage(), e);
            }
        }

        Object maxintegerdigits = parameters.get("maxintegerdigits");
        if (maxintegerdigits != null) {
            try {
                int maximum = Integer.parseInt(maxintegerdigits.toString());
                doc.setMaximumIntegerDigits(maximum);
            } catch (Exception e) {
                CurrencyDataField.logger.error("Error 'maxintegerdigits' parameter " + e.getMessage(), e);
            }
        }

        Object mindecimaldigits = parameters.get("mindecimaldigits");
        if (mindecimaldigits != null) {
            try {
                int minimum = Integer.parseInt(mindecimaldigits.toString());
                doc.setMinimumFractionDigits(minimum);
            } catch (Exception e) {
                CurrencyDataField.logger.error("Error 'mindecimaldigits' parameter " + e.getMessage(), e);
            }
        }

        Object maxdecimaldigits = parameters.get("maxdecimaldigits");
        if (maxdecimaldigits != null) {
            try {
                int maximum = Integer.parseInt(maxdecimaldigits.toString());
                doc.setMaximumFractionDigits(maximum);
            } catch (Exception e) {
                CurrencyDataField.logger.error("Error 'maxdecimaldigits' parameter " + e.getMessage(), e);
            }
        }

        Object grouping = parameters.get("grouping");
        if (grouping != null) {
            if (grouping.toString().equals("no")) {
                doc.setGrouping(false);
            }
        }

        // By default the button to show the calculator is visible
        Object calcul = parameters.get("calc");

        if ((calcul != null) && calcul.toString().equalsIgnoreCase("yes")) {
            // Add the button to show
            ImageIcon iconoCalc = ImageManager.getIcon(ImageManager.CALC);
            if (iconoCalc == null) {
                if (ApplicationManager.DEBUG) {
                    CurrencyDataField.logger.debug("calc.png icon hasn't been found");
                }
                this.calcButton = new FieldButton("...");
            } else {
                this.calcButton = new FieldButton();
                this.calcButton.setIcon(iconoCalc);
                this.calcButton.setMargin(new Insets(0, 0, 0, 0));
            }
            super.add(this.calcButton,
                    new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
                            GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
            if (this.labelPosition != SwingConstants.LEFT) {
                this.validateComponentPositions();
            }
            // Process the button click
            this.calcButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    if (CurrencyDataField.this.calc == null) {
                        CurrencyDataField.this.calc = new Calculator(CurrencyDataField.this.frame);
                    }
                    Object oValue = CurrencyDataField.this.getDoubleValue();
                    double initialValue = 0.0;
                    if (oValue != null) {
                        initialValue = ((Double) oValue).doubleValue();
                    }
                    double result = CurrencyDataField.this.calc.showCalculator(initialValue,
                            CurrencyDataField.this.getLocationOnScreen().x,
                            CurrencyDataField.this.getLocationOnScreen().y + CurrencyDataField.this.getHeight());
                    CurrencyDataField.this.setValue(new Double(result));
                }
            });
        }

        Object change = parameters.get("change");
        if (change == null) {
            if (CurrencyDataField.DEFAULT_EURO_BUTTON_VISIBLE) {
                ImageIcon eurIcon = ImageManager.getIcon(ImageManager.EURO);
                ImageIcon pesetaIcon = ImageManager.getIcon(ImageManager.PESETA);
                if ((eurIcon == null) || (pesetaIcon == null)) {
                    // Is some icon is null then use text mode
                    this.currencyButton = new FieldButton(this.euro);
                    this.currencyButton.setMargin(new Insets(0, 0, 0, 0));
                } else {
                    this.euroIcon = eurIcon;
                    this.pstIcon = pesetaIcon;
                    this.currencyButton = new FieldButton(this.euroIcon);
                    this.currencyButton.setMargin(new Insets(0, 0, 0, 0));
                }
                this.add(this.currencyButton,
                        new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
                                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                if (this.labelPosition != SwingConstants.LEFT) {
                    this.validateComponentPositions();
                }
            }
        } else {
            if (change.toString().equalsIgnoreCase("no")) {
            } else {
                // Show the button;
                ImageIcon eurIcon = ImageManager.getIcon(ImageManager.EURO);
                ImageIcon ptaIcon = ImageManager.getIcon(ImageManager.PESETA);
                if ((eurIcon == null) || (ptaIcon == null)) {
                    // If some icon is null then use text mode
                    this.currencyButton = new FieldButton(this.euro);
                    this.currencyButton.setMargin(new Insets(0, 0, 0, 0));
                } else {
                    this.euroIcon = eurIcon;
                    this.pstIcon = ptaIcon;
                    this.currencyButton = new FieldButton(this.euroIcon);
                    this.currencyButton.setMargin(new Insets(0, 0, 0, 0));
                }
                this.add(this.currencyButton,
                        new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
                                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                if (this.labelPosition != SwingConstants.LEFT) {
                    this.validateComponentPositions();
                }
            }
        }
        this.setTip();
        if (this.currencyButton != null) {
            this.currencyButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent evento) {
                    // Change between euros and pesetas
                    try {
                        CurrencyDataField.this.showEuros = !CurrencyDataField.this.showEuros;
                        // since 5.3.15 this method was removed
                        // ((CurrencyDocument) ((JTextField)
                        // dataField).getDocument()).setShowEuros(showEuros);
                        // Change the icon
                        if (CurrencyDataField.this.showEuros) {
                            CurrencyDataField.this.currencyButton.setIcon(CurrencyDataField.this.euroIcon);
                        } else {
                            CurrencyDataField.this.currencyButton.setIcon(CurrencyDataField.this.pstIcon);
                        }
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            CurrencyDataField.logger
                                .debug("Field value (Euros):"
                                        + ((CurrencyDocument) ((JTextField) CurrencyDataField.this.dataField)
                                            .getDocument()).getValue().toString());
                        }
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            CurrencyDataField.logger
                                .debug("Field value (Euros):"
                                        + ((CurrencyDocument) ((JTextField) CurrencyDataField.this.dataField)
                                            .getDocument()).getValue().toString());
                        }
                    } catch (Exception e) {
                        if (com.ontimize.gui.ApplicationManager.DEBUG) {
                            CurrencyDataField.logger.debug(null, e);
                        } else {
                            CurrencyDataField.logger.trace(null, e);
                        }
                    }
                }
            });
        }
    }

    protected CurrencyDocument createDocument(String currencySymbol) {
        return new CurrencyDocument(currencySymbol);
    }

    @Override
    protected void createDataField() {
        this.dataField = new EJTextField() {

            @Override
            public void setDocument(Document doc) {
                try {
                    Document previousDocument = this.getDocument();
                    if ((CurrencyDataField.this.innerListener != null) && (previousDocument != null)) {
                        previousDocument.removeDocumentListener(CurrencyDataField.this.innerListener);
                    }
                } catch (Exception e) {
                    CurrencyDataField.logger.trace(null, e);
                }
                super.setDocument(doc);
                try {
                    CurrencyDataField.this.installInnerListener();
                } catch (Exception e) {
                    CurrencyDataField.logger.trace(null, e);
                    // The first time the UIManager creates a document and an
                    // Exception happens
                    // if (ApplicationManager.DEBUG) {
                    // logger.error(null,e);
                    // }
                }
            }
        };
        ((EJTextField) this.dataField).setReplaceDecimalSeparator(true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            boolean permission = this.checkEnabledPermission();
            if (!permission) {
                return;
            }
        }
        if (this.currencyButton != null) {
            this.currencyButton.setEnabled(enabled);
        }
        if (this.calcButton != null) {
            this.calcButton.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    public Object getValue() {
        if (this.isEmpty()) {
            return null;
        }
        if (this.advancedQueryMode) {
            return ((CurrencyDocument) ((JTextField) this.dataField).getDocument()).getQueryValue();
        } else {
            CurrencyDocument document = (CurrencyDocument) ((JTextField) this.dataField).getDocument();
            Double val = (Double) document.getValue();
            return val;
        }
    }

    @Override
    public void setValue(Object value) {
        this.setInnerListenerEnabled(false);
        Object oPreviousValue = this.getDoubleValue();
        if (value instanceof Number) {
            CurrencyDocument document = (CurrencyDocument) ((JTextField) this.dataField).getDocument();
            document.setValue(new Double(((Number) value).doubleValue()));
            this.valueSave = this.getDoubleValue();
            this.setInnerValue(this.valueSave);
            this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
            this.setInnerListenerEnabled(true);
        } else if ((value instanceof SearchValue)
                && (((JTextField) this.dataField).getDocument() instanceof AdvancedRealDocument)) {
            Number n = (Number) ((SearchValue) value).getValue();
            this.setValue(n);
        } else if ((value instanceof Double) || (value instanceof Float)) {
            Number n = (Number) value;
            this.setValue(n);
            // Here no event is fired because this method calls itself
        } else {
            // If value is not a number then delete data
            this.deleteData();
        }
    }

    @Override
    public int getSQLDataType() {
        return java.sql.Types.DOUBLE;
    }

    /**
     * Gets the double value.
     * <p>
     * @return the double value
     */
    public Object getDoubleValue() {
        if (this.isEmpty()) {
            return null;
        }
        RealDocument document = (RealDocument) ((JTextField) this.dataField).getDocument();
        Double val = (Double) document.getValue();
        if (val == null) {
            return null;
        } else {
            return val;
        }
    }

    /**
     * Sets the tip for field.
     */
    protected void setTip() {
        if (this.tipKey != null) {
            // since 5.2074EN
            // Allows to define custom tip according to 'tip' attribute in xml
            String tipKeyString = this.resources != null ? this.resources.getString(this.tipKey) : this.tipKey.toString();
            ((JTextField) this.dataField).setToolTipText(tipKeyString);
            return;

        }
        CurrencyDocument doc = (CurrencyDocument) ((JTextField) this.dataField).getDocument();
        Object oValue = this.getValue();
        if (!(oValue instanceof Double)) {
            this.dataField.setToolTipText(null);
            return;
        }
        Double dEurosValue = (Double) this.getValue();
        if (dEurosValue != null) {
            double ptas = dEurosValue.doubleValue() * CurrencyDocument.EURO;
            if (this.showEuros) {
                // Tooltip
                StringBuilder textoTip = new StringBuilder(doc.getFormat().format(dEurosValue.doubleValue()));
                textoTip.append(" ");
                textoTip.append(this.euro);
                // textoTip.append(" = ");
                // textoTip.append(doc.formatPst(ptas));
                // textoTip.append(pst);
                ((JTextField) this.dataField).setToolTipText(textoTip.toString());
                if (ApplicationManager.DEBUG) {
                    CurrencyDataField.logger.debug("Established tip: " + textoTip);
                }
            }
            // else {
            // // Tooltip
            // StringBuilder sbTooltipText = new
            // StringBuilder(doc.formatPst(ptas));
            // sbTooltipText.append(" ");
            // sbTooltipText.append(pst);
            // sbTooltipText.append(" = ");
            // sbTooltipText.append(doc.getFormat().format(dEurosValue.doubleValue()));
            // sbTooltipText.append(euro);
            // ((JTextField)
            // dataField).setToolTipText(sbTooltipText.toString());
            // if (ApplicationManager.DEBUG) {
            // }
            // }
        } else {
            ((JTextField) this.dataField).setToolTipText(null);
            if (ApplicationManager.DEBUG) {
                CurrencyDataField.logger.debug("Established tip: NULL");
            }
        }
    }

    @Override
    public void showCurrencyValue(String currencySymbol) {
        try {
            ((CurrencyDocument) ((JTextField) this.dataField).getDocument()).setCurrencySymbol(currencySymbol);
            // switch (currencyId) {
            // case SelectCurrencyValues.EURO:
            // showEuros = true;
            // ((CurrencyDocument) ((JTextField)
            // dataField).getDocument()).setShowEuros(showEuros);
            // break;
            // case SelectCurrencyValues.PST:
            // showEuros = false;
            // ((CurrencyDocument) ((JTextField)
            // dataField).getDocument()).setShowEuros(showEuros);
            // break;
            // default:
            // showEuros = true;
            // ((CurrencyDocument) ((JTextField)
            // dataField).getDocument()).setShowEuros(showEuros);
            // break;
            // }
        } catch (Exception e) {
            if (com.ontimize.gui.ApplicationManager.DEBUG) {
                CurrencyDataField.logger.debug(null, e);
            } else {
                CurrencyDataField.logger.trace(null, e);
            }
        }
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }

    @Override
    public void setFontSize(int size) {
        super.setFontSize(size);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public void setAdvancedQueryMode(boolean enable) {

        if (!(((JTextField) this.dataField).getDocument() instanceof AdvancedRealDocument)) {
            return;
        }

        this.valueSave = this.getValue();
        this.advancedQueryMode = enable;
        ((AdvancedRealDocument) ((JTextField) this.dataField).getDocument())
            .setAdvancedQueryMode(this.advancedQueryMode);
        Object oNewValue = this.getValue();
        if ((oNewValue == null) && (this.valueSave != null)) {
            this.fireValueChanged(this.getValue(), this.valueSave, ValueEvent.PROGRAMMATIC_CHANGE);
        } else if ((this.valueSave == null) && (oNewValue != null)) {
            this.fireValueChanged(this.getValue(), this.valueSave, ValueEvent.PROGRAMMATIC_CHANGE);
        } else if ((this.valueSave != null) && (oNewValue != null) && !this.valueSave.equals(oNewValue)) {
            this.fireValueChanged(this.getValue(), this.valueSave, ValueEvent.PROGRAMMATIC_CHANGE);
        }
        this.valueSave = oNewValue;
    }

    /**
     * Checks the consistency of currency document value, changing the color to red with an incorrect
     * value.
     * <p>
     * @param e the document event
     * @return the consistency condition
     */
    protected boolean colorSelection(DocumentEvent e) {
        boolean wasEnabled = this.getInnerListenerEnabled();
        try {

            this.setInnerListenerEnabled(false);
            CurrencyDocument doc = (CurrencyDocument) ((JTextField) this.dataField).getDocument();
            if (doc.isValid()) {
                if (this.isRequired()) {
                    ((JTextField) this.dataField).setForeground(DataField.requiredFieldForegroundColor);
                } else {
                    ((JTextField) this.dataField).setForeground(this.fontColor);
                }
                return false;
            } else {
                ((JTextField) this.dataField).setForeground(Color.red);
                return true;
            }
        } catch (Exception ex) {
            CurrencyDataField.logger.trace(null, ex);
            return true;
        } finally {
            if (wasEnabled) {
                this.setInnerListenerEnabled(true);
            }
        }
    }

    @Override
    protected void createPopupMenu() {
        if (this.popupMenu == null) {
            super.createPopupMenu();
            this.popupMenu.addSeparator();
            // this.popupMenu = new ExtendedJPopupMenu();
            this.advancedHelpBMenu = new JMenuItem();
            String sMenuText = HelpUtilities.ADVANCED_SEARCH_HELP;
            try {
                if (this.resources != null) {
                    sMenuText = this.resources.getString(HelpUtilities.ADVANCED_SEARCH_HELP);
                }
            } catch (Exception e) {
                CurrencyDataField.logger.trace(null, e);
            }
            ImageIcon helpIcon = ImageManager.getIcon(ImageManager.HELPBOOK);
            if (helpIcon != null) {
                this.advancedHelpBMenu.setIcon(helpIcon);
            }
            this.advancedHelpBMenu.setText(sMenuText);
            this.popupMenu.add(this.advancedHelpBMenu);
            this.advancedHelpBMenu.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        HelpUtilities.showDefaultAdvancedHelpDialog(
                                SwingUtilities.getWindowAncestor(CurrencyDataField.this),
                                CurrencyDataField.this.advancedHelpBMenu.getText(),
                                CurrencyDataField.this.locale);
                    } catch (Exception ex) {
                        CurrencyDataField.logger.trace(null, e);
                        CurrencyDataField.this.parentForm.message("datafield.help_files_cannot be displayed",
                                Form.ERROR_MESSAGE, ex);
                    }
                }
            });
        }
    }

    @Override
    public void setResourceBundle(ResourceBundle resource) {
        super.setResourceBundle(resource);
        if (this.advancedHelpBMenu != null) {
            this.advancedHelpBMenu
                .setText(ApplicationManager.getTranslation(HelpUtilities.ADVANCED_SEARCH_HELP, resource));
        }
    }

    @Override
    protected void showPopupMenu(Component c, int x, int y) {
        if (this.popupMenu == null) {
            this.createPopupMenu();
        }
        if (this.popupMenu != null) {
            this.configurePopupMenuHelp();
            this.advancedHelpBMenu.setVisible(this.advancedQueryMode);
            this.popupMenu.show(c, x, y);
        }
    }

    @Override
    public boolean isEmpty() {
        if (this.advancedQueryMode) {
            if (((AdvancedRealDocument) ((JTextField) this.dataField).getDocument()).getQueryValue() != null) {
                return false;
            } else {
                return true;
            }
        }
        return super.isEmpty();
    }

    /**
     * Installs a listener for validate document.
     */
    protected void installValidationDocumentListener() {
        ((JTextField) this.dataField).getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent ev) {
                boolean errors = CurrencyDataField.this.colorSelection(ev);
                if (!errors) {
                    CurrencyDataField.this.setTip();
                } else {
                    CurrencyDataField.this.dataField.setToolTipText(null);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent ev) {
                boolean errors = CurrencyDataField.this.colorSelection(ev);
                if (!errors) {
                    CurrencyDataField.this.setTip();
                } else {
                    CurrencyDataField.this.dataField.setToolTipText(null);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent ev) {
                boolean errors = CurrencyDataField.this.colorSelection(ev);
                if (!errors) {
                    CurrencyDataField.this.setTip();
                } else {
                    CurrencyDataField.this.dataField.setToolTipText(null);
                }
            }
        });
    }

    @Override
    public void setParentFrame(Frame parentFrame) {
        this.frame = parentFrame;
    }

    @Override
    protected void installInnerListener() {
        if (this.dataField != null) {
            Document d = ((JTextField) this.dataField).getDocument();
            if (d != null) {
                if (this.innerListener == null) {
                    this.innerListener = new InnerDocumentListener() {

                        @Override
                        protected Object getValueField() {
                            return CurrencyDataField.this.getDoubleValue();
                        }
                    };
                }
                d.addDocumentListener(this.innerListener);
            }
        }
    }

    @Override
    public void setComponentLocale(Locale loc) {
        Locale l = DateDataField.getSameCountryLocale(loc);
        Object value = this.getValue();
        super.setComponentLocale(l);
        boolean events = this.fireValueEvents;
        this.fireValueEvents = false;
        if (value != null) {
            this.setValue(value);
        }
        this.fireValueEvents = events;
    }

}
