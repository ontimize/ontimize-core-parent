package com.ontimize.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.manager.IFormManager;

public class OKCancelFormDialog extends EJDialog implements Internationalization {

    private static final Logger logger = LoggerFactory.getLogger(OKCancelFormDialog.class);

    public static final int OK = 0;

    public static final int CANCEL = 1;

    protected Form form = null;

    protected IFormManager formManager = null;

    protected String formName = null;

    protected String returnValue = null;

    protected String emptyMessage = null;

    protected Object value = null;

    protected String titleKey = null;

    protected int result = OKCancelFormDialog.CANCEL;

    protected String[] returnValues = null;

    protected OKActionValidator validator = null;

    protected static OKCancelFormDialog v = null;

    public static interface OKActionValidator {

        public boolean validateOKAction(OKCancelFormDialog d);

    }

    protected OKCancelFormDialog(Frame frame, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage) {
        this(frame, formManager, title, formName, returnValue, emptyMessage, null);

    }

    @Override
    protected void setInitialFocus() {
        if (this.form != null) {
            this.form.requestDefaultFocus();
        }
    }

    protected OKCancelFormDialog(Frame frame, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage, OKActionValidator validator) {
        super(frame, title, true);
        this.validator = validator;
        String[] returnValues = { returnValue };
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected OKCancelFormDialog(Frame frame, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage) {
        super(frame, title, true);
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected OKCancelFormDialog(Frame frame, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, OKActionValidator validator) {
        super(frame, title, true);
        this.validator = validator;
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected OKCancelFormDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage) {
        super(dialog, title, true);
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected OKCancelFormDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, OKActionValidator validator) {
        super(dialog, title, true);
        this.validator = validator;
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected void setFixedValues(Map<?,?> fixedValues, String[] fixedValuesModifiables) {
        this.form.enableDataFields(true);
        List<?> vAttributes = this.form.getDataFieldAttributeList();
        for (int i = 0; i < vAttributes.size(); i++) {
            this.form.setModifiable(vAttributes.get(i).toString(), true);
        }
        if (fixedValues != null) {
            Iterator<?> enumKeys = fixedValues.keySet().iterator();
            while (enumKeys.hasNext()) {
                Object oKey = enumKeys.next();
                OKCancelFormDialog.v.form.setDataFieldValue(oKey, fixedValues.get(oKey));
                OKCancelFormDialog.v.form.setModifiable(oKey.toString(), false);
                boolean isModifiable = false;
                if (fixedValuesModifiables != null) {
                    for (int i = 0; i < fixedValuesModifiables.length; i++) {
                        if (oKey.equals(fixedValuesModifiables[i])) {
                            isModifiable = true;
                            break;
                        }
                    }
                }
                if (!isModifiable) {
                    if (ApplicationManager.DEBUG) {
                        OKCancelFormDialog.logger.debug("Disabling fields " + oKey + " of fixed values");
                    }
                    OKCancelFormDialog.v.form.disableDataField(oKey);
                } else {
                    if (ApplicationManager.DEBUG) {
                        OKCancelFormDialog.logger.debug("The field " + oKey + " of fixed values is not disabled");
                    }
                }
            }
        }
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.result = OKCancelFormDialog.CANCEL;
            this.setVisible(false);
        } else {
            super.processWindowEvent(e);
        }
    }

    protected void init(IFormManager formManager, String title, String formName, String[] returnValues,
            String emptyMessage) {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.formManager = formManager;
        this.titleKey = title;
        this.formName = formName;
        if (returnValues != null) {
            this.returnValue = returnValues[0];
            this.returnValues = returnValues;
        }

        this.emptyMessage = emptyMessage;
        this.createForm();
        this.installButtonListeners();
        this.pack();
        ApplicationManager.center(this);
    }

    public Form getForm() {
        return this.form;
    }

    public int getResult() {
        return this.result;
    }

    public String getFormName() {
        return this.formName;
    }

    @Override
    public String getTitle() {
        return this.titleKey;
    }

    public String getReturnValue() {
        return this.returnValue;
    }

    public Object getFormValue(String key) {
        return key == null ? null : this.form.getDataFieldValue(key);
    }

    public String getEmptyMessage() {
        return this.emptyMessage;
    }

    protected OKCancelFormDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage) {
        super(dialog, title, true);
        String[] returnValues = { returnValue };
        this.init(formManager, title, formName, returnValues, emptyMessage);
    }

    protected void createForm() {
        if (this.form == null) {
            this.form = this.formManager.getFormCopy(this.formName, this.getContentPane());
        }
    }

    protected void installButtonListeners() {
        Button bAccept = this.form.getButton("accept");
        if (bAccept != null) {
            bAccept.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (OKCancelFormDialog.this.returnValues != null) {
                        for (int i = 0; i < OKCancelFormDialog.this.returnValues.length; i++) {
                            if (OKCancelFormDialog.this.form.isEmpty(OKCancelFormDialog.this.returnValues[i])
                                    && (OKCancelFormDialog.this.emptyMessage != null)) {
                                OKCancelFormDialog.this.form.message(OKCancelFormDialog.this,
                                        OKCancelFormDialog.this.emptyMessage, Form.ERROR_MESSAGE);
                                OKCancelFormDialog.this.form.requestDefaultFocus();
                                return;
                            }
                        }
                    }
                    if (OKCancelFormDialog.this.validator != null) {
                        boolean val = OKCancelFormDialog.this.validator.validateOKAction(OKCancelFormDialog.this);
                        if (val) {
                            OKCancelFormDialog.this.result = OKCancelFormDialog.OK;
                            if (OKCancelFormDialog.this.returnValues != null) {
                                OKCancelFormDialog.this.value = OKCancelFormDialog.this.form
                                    .getDataFieldValue(OKCancelFormDialog.this.returnValues[0]);
                            }
                            OKCancelFormDialog.this.setVisible(false);
                        }
                    } else {
                        OKCancelFormDialog.this.result = OKCancelFormDialog.OK;
                        if (OKCancelFormDialog.this.returnValues != null) {
                            OKCancelFormDialog.this.value = OKCancelFormDialog.this.form
                                .getDataFieldValue(OKCancelFormDialog.this.returnValues[0]);
                        }
                        OKCancelFormDialog.this.setVisible(false);
                    }
                }
            });
        }

        Button bCancel = this.form.getButton("cancel");
        if (bCancel != null) {
            bCancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    OKCancelFormDialog.this.result = OKCancelFormDialog.CANCEL;
                    OKCancelFormDialog.this.setVisible(false);
                }
            });
        }
    }

    @Override
    public void setVisible(boolean vis) {
        if (vis) {
            if (this.form != null) {
                this.form.enableDataFields(false);
                this.form.enableButtons();
                this.form.requestDefaultFocus();
            }
        }
        super.setVisible(vis);
    }

    public static int showDialog(Frame frame, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage) {
        return OKCancelFormDialog.showDialog(frame, formManager, title, formName, returnValue, emptyMessage, null);
    }

    public static int showDialog(Frame frame, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage, OKActionValidator val) {
        if (OKCancelFormDialog.v == null) {
            OKCancelFormDialog.v = new OKCancelFormDialog(frame, formManager, title, formName, returnValue,
                    emptyMessage, val);
        } else if (!(frame == OKCancelFormDialog.v.getOwner())
                || !(OKCancelFormDialog.v.formName.equals(formName)) || !(OKCancelFormDialog.v.titleKey
                    .equals(title))
                || ((OKCancelFormDialog.v.returnValue != null) && !(OKCancelFormDialog.v.returnValue
                    .equals(returnValue)))
                || ((OKCancelFormDialog.v.emptyMessage != null) && !(OKCancelFormDialog.v.emptyMessage
                    .equals(emptyMessage)))
                || (val != OKCancelFormDialog.v.validator)) {
            try {
                OKCancelFormDialog.v.form.free();
            } catch (Exception e) {
                OKCancelFormDialog.logger.error(e.getMessage(), e);
                if (ApplicationManager.DEBUG) {
                    OKCancelFormDialog.logger.error(null, e);
                }
            }
            OKCancelFormDialog.v.dispose();
            OKCancelFormDialog.v = new OKCancelFormDialog(frame, formManager, title, formName, returnValue,
                    emptyMessage, val);
        }
        SwingUtilities.updateComponentTreeUI(OKCancelFormDialog.v);
        OKCancelFormDialog.v.form.deleteDataFields(true);
        OKCancelFormDialog.v.form.setAdvancedQueryModeAll(OKCancelFormDialog.advancedQueryMode);
        OKCancelFormDialog.v.form.requestDefaultFocus();
        OKCancelFormDialog.v.setResourceBundle(formManager.getResourceBundle());
        OKCancelFormDialog.v.setVisible(true);

        return OKCancelFormDialog.v.result;
    }

    public static int showDialog(Frame frame, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage) {
        return OKCancelFormDialog.showDialog(frame, formManager, title, formName, returnValues, emptyMessage, null);

    }

    public static int showDialog(Frame frame, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues) {
        return OKCancelFormDialog.showDialog(frame, formManager, title, formName, returnValues, emptyMessage,
                fixedValues, null);
    }

    public static int showDialog(Frame frame, IFormManager formManger, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues,
            OKActionValidator val) {
        return OKCancelFormDialog.showDialog(frame, formManger, title, formName, returnValues, emptyMessage,
                fixedValues, null, null);
    }

    public static int showDialog(Frame frame, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues,
            OKActionValidator val, String[] fixedValuesModifiables) {
        if (OKCancelFormDialog.v == null) {
            OKCancelFormDialog.v = new OKCancelFormDialog(frame, formManager, title, formName, returnValues,
                    emptyMessage, val);
        } else if (!(frame == OKCancelFormDialog.v.getOwner()) || !(OKCancelFormDialog.v.formName.equals(formName))
                || !(OKCancelFormDialog.v.titleKey.equals(
                        title))
                || ((OKCancelFormDialog.v.emptyMessage != null)
                        && !(OKCancelFormDialog.v.emptyMessage.equals(emptyMessage)))
                || (val != OKCancelFormDialog.v.validator)) {
            try {
                OKCancelFormDialog.v.form.free();
            } catch (Exception e) {
                OKCancelFormDialog.logger.error(e.getMessage(), e);
                if (ApplicationManager.DEBUG) {
                    OKCancelFormDialog.logger.error(null, e);
                }
            }
            OKCancelFormDialog.v.dispose();
            OKCancelFormDialog.v = new OKCancelFormDialog(frame, formManager, title, formName, returnValues,
                    emptyMessage, val);
        } else {
            // Now checks if the return values are the same
            boolean bValuesMatch = true;
            if ((returnValues == null) && (OKCancelFormDialog.v.returnValues == null)) {
            } else {
                if (returnValues == null) {
                    bValuesMatch = false;
                } else if (OKCancelFormDialog.v.returnValues == null) {
                    bValuesMatch = false;
                } else if (returnValues.length != OKCancelFormDialog.v.returnValues.length) {
                    bValuesMatch = false;
                } else {
                    for (int i = 0; i < returnValues.length; i++) {
                        if (!returnValues[i].equals(OKCancelFormDialog.v.returnValues[i])) {
                            bValuesMatch = false;
                            break;
                        }
                    }
                }
            }
            if (!bValuesMatch) {
                try {
                    OKCancelFormDialog.v.form.free();
                } catch (Exception e) {
                    OKCancelFormDialog.logger.error(e.getMessage(), e);
                    if (ApplicationManager.DEBUG) {
                        OKCancelFormDialog.logger.error(null, e);
                    }
                }
                OKCancelFormDialog.v.dispose();
                OKCancelFormDialog.v = new OKCancelFormDialog(frame, formManager, title, formName, returnValues,
                        emptyMessage, val);
            }
        }
        SwingUtilities.updateComponentTreeUI(OKCancelFormDialog.v);
        OKCancelFormDialog.v.form.deleteDataFields(true);
        OKCancelFormDialog.v.form.setAdvancedQueryModeAll(OKCancelFormDialog.advancedQueryMode);
        OKCancelFormDialog.v.setFixedValues(fixedValues, fixedValuesModifiables);
        OKCancelFormDialog.v.form.requestDefaultFocus();
        OKCancelFormDialog.v.setResourceBundle(formManager.getResourceBundle());
        OKCancelFormDialog.v.setVisible(true);

        return OKCancelFormDialog.v.result;
    }

    public static int showDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage) {
        return OKCancelFormDialog.showDialog(dialog, formManager, title, formName, returnValues, emptyMessage, null);

    }

    public static int showDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues) {
        return OKCancelFormDialog.showDialog(dialog, formManager, title, formName, returnValues, emptyMessage,
                fixedValues, null);
    }

    public static int showDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues,
            OKActionValidator val) {
        return OKCancelFormDialog.showDialog(dialog, formManager, title, formName, returnValues, emptyMessage,
                fixedValues, null, null);
    }

    public static int showDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String[] returnValues, String emptyMessage, Map<Object, Object> fixedValues,
            OKActionValidator val, String[] fixedValuesModifiables) {
        if (OKCancelFormDialog.v == null) {
            OKCancelFormDialog.v = new OKCancelFormDialog(dialog, formManager, title, formName, returnValues,
                    emptyMessage, val);
        } else if (!(dialog == OKCancelFormDialog.v.getOwner()) || !(OKCancelFormDialog.v.formName.equals(formName))
                || !(OKCancelFormDialog.v.titleKey.equals(
                        title))
                || ((OKCancelFormDialog.v.emptyMessage != null)
                        && !(OKCancelFormDialog.v.emptyMessage.equals(emptyMessage)))
                || (val != OKCancelFormDialog.v.validator)) {
            try {
                OKCancelFormDialog.v.form.free();
            } catch (Exception exc) {
                OKCancelFormDialog.logger.error(exc.getMessage(), exc);
                if (ApplicationManager.DEBUG) {
                    OKCancelFormDialog.logger.error(null, exc);
                }
            }
            OKCancelFormDialog.v.dispose();
            OKCancelFormDialog.v = new OKCancelFormDialog(dialog, formManager, title, formName, returnValues,
                    emptyMessage, val);
        } else {
            // Checks if the return values are the same
            boolean bValuesMatch = true;
            if ((returnValues == null) && (OKCancelFormDialog.v.returnValues == null)) {

            } else {
                if (returnValues == null) {
                    bValuesMatch = false;
                } else if (OKCancelFormDialog.v.returnValues == null) {
                    bValuesMatch = false;
                } else if (returnValues.length != OKCancelFormDialog.v.returnValues.length) {
                    bValuesMatch = false;
                } else {
                    for (int i = 0; i < returnValues.length; i++) {
                        if (!returnValues[i].equals(OKCancelFormDialog.v.returnValues[i])) {
                            bValuesMatch = false;
                            break;
                        }
                    }
                }
            }
            if (!bValuesMatch) {
                try {
                    OKCancelFormDialog.v.form.free();
                } catch (Exception e) {
                    OKCancelFormDialog.logger.error(e.getMessage(), e);
                    if (ApplicationManager.DEBUG) {
                        OKCancelFormDialog.logger.error(null, e);
                    }
                }
                OKCancelFormDialog.v.dispose();
                OKCancelFormDialog.v = new OKCancelFormDialog(dialog, formManager, title, formName, returnValues,
                        emptyMessage, val);
            }
        }
        SwingUtilities.updateComponentTreeUI(OKCancelFormDialog.v);
        OKCancelFormDialog.v.form.deleteDataFields(true);
        OKCancelFormDialog.v.form.setAdvancedQueryModeAll(OKCancelFormDialog.advancedQueryMode);
        OKCancelFormDialog.v.setFixedValues(fixedValues, fixedValuesModifiables);
        OKCancelFormDialog.v.form.requestDefaultFocus();
        OKCancelFormDialog.v.setResourceBundle(formManager.getResourceBundle());
        OKCancelFormDialog.v.setVisible(true);

        return OKCancelFormDialog.v.result;
    }

    public static int showDialog(Dialog dialog, IFormManager formManager, String title, String formName,
            String returnValue, String emptyMessage) {
        if (OKCancelFormDialog.v == null) {
            OKCancelFormDialog.v = new OKCancelFormDialog(dialog, formManager, title, formName, returnValue,
                    emptyMessage);
        } else if (!(dialog == OKCancelFormDialog.v.getOwner()) || !OKCancelFormDialog.v.formName.equals(formName)
                || !OKCancelFormDialog.v.titleKey
                    .equals(title)
                || !OKCancelFormDialog.v.returnValue.equals(returnValue)
                || !OKCancelFormDialog.v.emptyMessage.equals(emptyMessage)) {
            try {
                OKCancelFormDialog.v.form.free();
            } catch (Exception e) {
                OKCancelFormDialog.logger.error(e.getMessage(), e);
                if (ApplicationManager.DEBUG) {
                    OKCancelFormDialog.logger.error(null, e);
                }
            }
            OKCancelFormDialog.v.dispose();
            OKCancelFormDialog.v = new OKCancelFormDialog(dialog, formManager, title, formName, returnValue,
                    emptyMessage);
        }
        SwingUtilities.updateComponentTreeUI(OKCancelFormDialog.v);
        OKCancelFormDialog.v.form.deleteDataFields(true);
        OKCancelFormDialog.v.form.setAdvancedQueryModeAll(OKCancelFormDialog.advancedQueryMode);
        OKCancelFormDialog.v.form.requestDefaultFocus();
        OKCancelFormDialog.v.setResourceBundle(formManager.getResourceBundle());
        OKCancelFormDialog.v.setVisible(true);

        return OKCancelFormDialog.v.result;
    }

    public static Object getValue() {
        return OKCancelFormDialog.v == null ? null : OKCancelFormDialog.v.value;
    }

    public static Object[] getValues() {
        if (OKCancelFormDialog.v == null) {
            return null;
        } else {
            Object[] values = new Object[OKCancelFormDialog.v.returnValues.length];
            for (int i = 0; i < OKCancelFormDialog.v.returnValues.length; i++) {
                values[i] = OKCancelFormDialog.v.form.getDataFieldValue(OKCancelFormDialog.v.returnValues[i]);
            }
            return values;
        }
    }

    public static void setValue(String attr, Object value) {
        if (OKCancelFormDialog.v != null) {
            OKCancelFormDialog.v.form.setDataFieldValue(attr, value);
        }
        return;
    }

    public static Object getValue(String attr) {
        return OKCancelFormDialog.v == null ? null : OKCancelFormDialog.v.form.getDataFieldValue(attr);
    }

    @Override
    public List<String> getTextsToTranslate() {
        return new ArrayList<>();
    }

    @Override
    public void setResourceBundle(ResourceBundle res) {
        if (this.form != null) {
            this.form.setResourceBundle(res);
        }
        try {
            if (res != null) {
                this.setTitle(res.getString(this.titleKey));
            }
        } catch (Exception exc) {
            OKCancelFormDialog.logger.error(exc.getMessage(), exc);
        }
    }

    @Override
    public void setComponentLocale(Locale l) {
    }

    protected static boolean advancedQueryMode = false;

    public static void setAdvancedQueryModeOn(boolean on) {
        OKCancelFormDialog.advancedQueryMode = on;
    }

    public static boolean getAdvancedQueryModeOn() {
        return OKCancelFormDialog.advancedQueryMode;
    }

}
