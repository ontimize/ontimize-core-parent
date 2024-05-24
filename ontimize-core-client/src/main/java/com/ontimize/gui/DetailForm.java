package com.ontimize.gui;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.EntityResultTools;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.tools.StringTools;

/**
 * Form that is shown when user makes double click in a table component
 *
 * @version 1.2 15/06/2001
 */
public class DetailForm extends EJDialog implements Internationalization, DataNavigationListener, IDetailForm, Freeable {

	private static final Logger		logger									= LoggerFactory.getLogger(DetailForm.class);

	public static final String		M_MODIFIED_DATA_CLOSE_AND_LOST_CHANGES	= "detailform.modified_data_discard_changes_and_close";

	protected Map<Object, Object>	tableKeys								= null;

	protected List<Object>			fieldsKey								= null;

	protected int					vectorIndex								= 0;

	protected Form					form									= null;

	protected EntityResult			data									= new EntityResultMapImpl();

	/**
	 * @deprecated
	 */
	@Deprecated
	protected Object				parentKeyValue							= null;

	/**
	 * @deprecated
	 */
	@Deprecated
	protected String				parentKeyName							= null;

	/**
	 * @deprecated
	 */
	@Deprecated
	protected Map<Object, Object>	otherParentKeys							= null;

	protected Map<Object, Object>	parentkeys								= null;

	protected Table					table									= null;

	protected String				title									= null;

	protected CardLayout			cardLayout								= new CardLayout();

	protected Map<Object, Object>	cacheForms								= new HashMap<>(1);

	protected boolean				dataChangeEventProcessing				= true;

	public boolean					checkDataBeforeClose					= true;

	protected class ActivationFormListener extends WindowAdapter {

		Form currentForm = null;

		public ActivationFormListener(Form f) {
			this.currentForm = f;
		}

		@Override
		public void windowActivated(WindowEvent event) {
			if (this.currentForm.getFormManager() != null) {
				this.currentForm.getFormManager().setActiveForm(this.currentForm);
			}
		}

		@Override
		public void windowDeactivated(WindowEvent event) {
			if (this.currentForm.getFormManager() != null) {
				this.currentForm.getFormManager().setActiveForm(null);
			}
		}

	};

	protected ActivationFormListener	activedFormListener	= null;

	protected boolean					packed				= false;

	protected Map<Object, Object>		codValues			= null;

	protected Map<Object, Object>		reverseCodValues	= null;

	public DetailForm(Frame f, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeys) {
		this(f, title, modal, form, tableKeys, keyFields, sourceTable, parentkeys, null);
	}

	public DetailForm(Frame f, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeys,
			Map<Object, Object> codValues) {
		super(f, title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, sourceTable, parentkeys);
	}

	public DetailForm(Dialog d, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeys) {
		this(d, title, modal, form, tableKeys, keyFields, sourceTable, parentkeys, null);
	}

	public DetailForm(Dialog d, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeys,
			Map<Object, Object> codValues) {
		super(d, title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, sourceTable, parentkeys);
	}

	public DetailForm(String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentKeys) {
		this(title, modal, form, tableKeys, keyFields, sourceTable, parentKeys, null);
	}

	public DetailForm(String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeys,
			Map<Object, Object> codValues) {
		super(form.getParentFrame(), title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, sourceTable, parentkeys);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(Frame f, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName,
			Table sourceTable, Map<Object, Object> otherParentkeys) {
		this(f, title, modal, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentkeys, null);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(Frame f, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName,
			Table sourceTable, Map<Object, Object> otherParentkeys, Map<Object, Object> codValues) {
		super(f, title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentkeys);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(Dialog d, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName,
			Table sourceTable, Map<Object, Object> otherParentkeys) {
		this(d, title, modal, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentkeys, null);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(Dialog d, String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName,
			Table sourceTable, Map<Object, Object> otherParentkeys, Map<Object, Object> codValues) {
		super(d, title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentkeys);

	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName, Table sourceTable,
			Map<Object, Object> otherParentKeys) {
		this(title, modal, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentKeys, null);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public DetailForm(String title, boolean modal, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName, Table sourceTable,
			Map<Object, Object> otherParentkeys, Map<Object, Object> codValues) {
		super(form.getParentFrame(), title, modal);
		this.initCodValues(codValues);
		this.init(title, form, tableKeys, keyFields, parentkeyValue, parentkeyName, sourceTable, otherParentkeys);
	}

	protected void initCodValues(Map<Object, Object> codValues) {
		if (codValues == null) {
			return;
		}
		this.codValues = codValues;
		this.reverseCodValues = new HashMap<>();
		for (Entry<Object, Object> entry : this.codValues.entrySet()) {
			this.reverseCodValues.put(entry.getValue(), entry.getValue());

		}
	}

	@Override
	public String getFormFieldName(Object name) {
		if (name == null) {
			return null;
		}
		return this.getFormFieldName(name.toString());
	}

	protected String getFormFieldName(String name) {
		if (this.codValues == null) {
			return name;
		}
		if (this.codValues.containsKey(name)) {
			return (String) this.codValues.get(name);
		}
		return name;
	}

	protected String getTableFieldName(String name) {
		if (this.reverseCodValues == null) {
			return name;
		}
		if (this.reverseCodValues.containsKey(name)) {
			return (String) this.reverseCodValues.get(name);
		}
		return name;
	}

	@Override
	public String getTableFieldName(Object name) {
		if (name == null) {
			return null;
		}
		return this.getTableFieldName(name.toString());
	}

	@Override
	public Map<Object, Object> valuesToForm(EntityResult entityResult, int index) {
		Map<?,?> values = entityResult.getRecordValues(index);
		return this.valuesToForm(values);
	}

	@Override
	public Map<Object, Object> valuesToForm(Map<?, ?> values) {
		if (values != null) {
			Map<Object, Object> clone = new HashMap<>();
			for (Entry<?, ?> entry : values.entrySet()) {
				Object current = StringTools.toString(entry.getKey());
				clone.put(this.getFormFieldName(current), entry.getValue());
			}
			return clone;
		}
		return null;
	}

	@Override
	public Map<Object, Object> valuesToTable(Map<?, ?> values) {
		Map<Object, Object> clone = new HashMap<>();
		for (Entry<?, ?> entry : values.entrySet()) {
			Object current = StringTools.toString(entry.getKey());
			clone.put(this.getTableFieldName(current.toString()), entry.getValue());
		}
		return clone;
	}

	protected List<Object> listToForm(List<Object> list) {
		List<Object> current = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			current.add(this.getFormFieldName(list.get(i).toString()));
		}
		return current;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	protected void init(String title, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Object parentkeyValue, String parentkeyName, Table sourceTable,
			Map<Object, Object> otherParentkeys) {
		Map<Object, Object> parentkeysvalues = new HashMap<>();
		if ((parentkeyName != null) && (parentkeyValue != null)) {
			parentkeysvalues.put(parentkeyName, parentkeyValue);
		}
		if (otherParentkeys != null) {
			parentkeysvalues.putAll(otherParentkeys);
		}
		this.init(title, form, tableKeys, keyFields, sourceTable, parentkeysvalues);
	}

	/**
	 * This method is used only to preserver compatibility with previous versions
	 * 
	 * @param parentkeys
	 */
	private void assignParentkeyOtherParentkeys(Map<Object, Object> parentkeys) {
		if ((parentkeys != null) && (parentkeys.size() > 0)) {
			Map<Object, Object> pClone = new HashMap<>(parentkeys);
			Iterator<?> pkeyNames = pClone.keySet().iterator();
			Object pName = pkeyNames.next();
			this.parentKeyName = this.getFormFieldName(pName);
			this.parentKeyValue = pClone.remove(pName);
			if (pClone.size() > 0) {
				this.otherParentKeys = this.valuesToForm(pClone);
			}
		}
	}

	protected void init(String title, Form form, Map<Object, Object> tableKeys, List<Object> keyFields, Table sourceTable, Map<Object, Object> parentkeyValues) {
		this.autoPackOnOpen = false;
		this.title = title;
		this.tableKeys = this.valuesToForm(tableKeys);
		this.form = form;
		this.activedFormListener = new ActivationFormListener(form);
		this.addWindowListener(this.activedFormListener);
		this.table = sourceTable;

		this.parentkeys = this.valuesToForm(parentkeyValues);
		// Call this method only to preserver compatibility
		this.assignParentkeyOtherParentkeys(parentkeyValues);

		this.form.setDetailForm(this);
		this.form.disableDataFields();
		this.fieldsKey = this.listToForm(keyFields);

		if (form.getInteractionManager() instanceof BasicInteractionManager) {
			((BasicInteractionManager) form.getInteractionManager()).setDetailForm(true);
		}

		for (Entry<Object, Object> entry : this.parentkeys.entrySet()) {
			form.setModifiable(entry.getKey().toString(), false);
		}
		this.vectorIndex = 0;

		this.installButtonsListeners();

		this.getContentPane().setLayout(this.cardLayout);
		this.getContentPane().add(form, this.form.getArchiveName());
		if (this.form.getArchiveName() != null) {
			this.cacheForms.put(form.getArchiveName(), form);
		}
		this.pack();
		// Center in the screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		// If tableKeys is empty then no query is executed
		if (tableKeys.isEmpty()) {
			this.form.disableButtons();
			this.form.disableDataFields();
			this.form.deleteDataFields();
			this.form.addDataNavigationListener(this);
			return;
		} else {
			// Save data in a hashtable with the vector index
			this.vectorIndex = this.table.getSelectedRow();
			if (this.form instanceof FormExt) {
				// ((FormExt) this.form).updateDataFields(tableKeys, -1);
				((FormExt) this.form).updateDataFields(tableKeys);
			} else {
				this.data = this.query(this.vectorIndex);
				this.form.updateDataFields(this.data);
			}

			// If there are more than one record
			int recordNumber = 0;

			Iterator<?> enumTableKeys = this.tableKeys.keySet().iterator();
			List<?> vKeys = (List<?>) this.tableKeys.get(enumTableKeys.next());
			recordNumber = vKeys.size();
			if (recordNumber > 1) {
				// If all buttons are not null and keys is greater than 1
				this.form.startButton.setEnabled(true);
				this.form.previousButton.setEnabled(true);
				this.form.nextButton.setEnabled(true);
				this.form.endButton.setEnabled(true);
			}
			this.form.addDataNavigationListener(this);
		}
	}

	@Override
	public void showDetailForm() {
		if (!this.packed) {
			this.pack();
			this.packed = true;
		}
		this.setVisible(true);
	}

	@Override
	public void hideDetailForm() {
		this.setVisible(false);
	}

	@Override
	public Table getTable() {
		return this.table;
	}

	/**
	 * This method sets the keys in the table records.<br> This keys are used to query the record values
	 * 
	 * @param tableValues
	 * @param index
	 */
	@Override
	public void setKeys(EntityResult tableValues, int index) {
		this.tableKeys = new HashMap<>();
		Map<Object, Object> tempKeys = this.valuesToForm(tableValues, index);
		List<String> formKeys = this.form.getKeys();
		for (Entry<Object, Object> entry : tempKeys.entrySet()) {
			Object key = entry.getKey();
			if (formKeys.contains(key)) {
				this.tableKeys.put(key, entry.getValue());
			}

		}

		// Reset the index of the selected element
		this.vectorIndex = 0;

		// If there are more than one record
		int recordNumber = 0;
		if (this.tableKeys.isEmpty()) {
			this.form.disableButtons();
			this.form.disableDataFields();
		} else {
			Iterator<?> enumTableKeys = this.tableKeys.keySet().iterator();
			List<?> vKeys = (List<?>) this.tableKeys.get(enumTableKeys.next());
			recordNumber = vKeys.size();
		}

		if (index < 0) {
			index = 0;
		}

		if (index < recordNumber) {
			this.vectorIndex = index;
		}
		if (!this.tableKeys.isEmpty()) {
			if (!(this.form instanceof FormExt)) {
				if (this.vectorIndex >= 0) {
					this.data = this.query(this.vectorIndex);
					this.form.updateDataFields(this.data);
					if (recordNumber > 1) {
						this.form.startButton.setEnabled(true);
						this.form.previousButton.setEnabled(true);
						this.form.nextButton.setEnabled(true);
						this.form.endButton.setEnabled(true);
						if (this.vectorIndex == 0) {
							this.form.startButton.setEnabled(false);
							this.form.previousButton.setEnabled(false);
						} else if (this.vectorIndex >= (recordNumber - 1)) {
							this.form.nextButton.setEnabled(false);
							this.form.endButton.setEnabled(false);
						}
					}
				} else {
					this.form.updateDataFields(new HashMap<>());
				}
			} else {
				((FormExt) this.form).updateDataFields(EntityResultTools.mapToEntityResult(this.tableKeys), this.vectorIndex);
			}
		} else {
			this.form.updateDataFields(new HashMap<>());
		}
		if (recordNumber == 0) {
			this.setQueryInsertMode();
		}
	}

	protected EntityResult query(int index) {
		EntityResult res = null;
		try {
			Map<Object, Object> hKeysValues = new HashMap<>();
			if (index >= 0) {
				// parent key values are used in the query too
				// Parentkey;
				if (this.parentkeys != null) {
					// Other parent keys
					Iterator<?> enumOtherKeys = this.parentkeys.keySet().iterator();
					while (enumOtherKeys.hasNext()) {
						Object oParentkeyElement = enumOtherKeys.next();
						if (this.parentkeys.get(oParentkeyElement) == null) {
							if (ApplicationManager.DEBUG) {
								MessageDialog.showErrorMessage(this,
										"DEBUG: DetailForm: parentkey " + oParentkeyElement + " is NULL. It won't be included in the query. Check the xml that contains the table configuration and ensure that the parentkey has value there.");
							}
						} else {
							hKeysValues.put(oParentkeyElement, this.parentkeys.get(oParentkeyElement));
						}
					}
				}
				List<Object> vTableKeys = this.table.getKeys();
				for (int i = 0; i < vTableKeys.size(); i++) {
					Object oKeyField = vTableKeys.get(i);
					List<?> vKeyValues = (List<?>) this.tableKeys.get(oKeyField);
					if (vKeyValues.size() <= index) {
						if (ApplicationManager.DEBUG) {
							MessageDialog.showErrorMessage(this,
									"DEBUG: DetailForm: Hashtable with the detail form keys contains less elements for the key " + oKeyField + " than the selected index " + index);
						}
						return new EntityResultMapImpl();
					}
					if (vKeyValues.get(index) == null) {
						if (ApplicationManager.DEBUG) {
							MessageDialog.showErrorMessage(this,
									"DEBUG: DetailForm:  Hashtable with the detail form keys contains a NULL value for the key: " + oKeyField + " in the selected index: " + index);
						}
					}
					hKeysValues.put(oKeyField, vKeyValues.get(index));
				}

			} else {
				return new EntityResultMapImpl();
			}
			EntityReferenceLocator referenceLocator = this.form.getFormManager().getReferenceLocator();
			Entity entity = referenceLocator.getEntityReference(this.form.getEntityName());
			List<Object> vAttributeList = new ArrayList<>(this.form.getDataFieldAttributeList());
			// If key is not include then add it to the query fields, but it can
			// be
			// an ReferenceFieldAttribute
			for (int i = 0; i < this.fieldsKey.size(); i++) {
				boolean containsKey = false;
				for (int j = 0; j < vAttributeList.size(); j++) {
					Object oAttribute = vAttributeList.get(j);
					if (oAttribute.equals(this.fieldsKey.get(i))) {
						containsKey = true;
						break;
					} else if (oAttribute instanceof ReferenceFieldAttribute) {
						if (((ReferenceFieldAttribute) oAttribute).getAttr() != null) {
							if (((ReferenceFieldAttribute) oAttribute).getAttr().equals(this.fieldsKey.get(i))) {
								containsKey = true;
								break;
							}
						}
					}
				}
				if (!containsKey) {
					vAttributeList.add(this.fieldsKey.get(i));
				}
			}
			res = entity.query(hKeysValues, vAttributeList, referenceLocator.getSessionId());
			// For each key get the value and add it to the data
			return res;
		} catch (Exception e) {
			DetailForm.logger.error("DetailForm: Error in query. Check the parameters, the xml and the entity configuration", e);
			if (ApplicationManager.DEBUG) {
				DetailForm.logger.error(null, e);
			}
			return new EntityResultMapImpl();
		}
	}

	@Override
	public void setInsertMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setInsertMode();
	}

	@Override
	public void setUpdateMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setUpdateMode();
	}

	public void forceUpdateMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setUpdateMode();

	}

	@Override
	public void setQueryInsertMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setQueryInsertMode();
	}

	@Override
	public void setQueryMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setQueryMode();

	}

	/**
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public String getParentKeyName() {
		return this.parentKeyName;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public Object getParentKeyValue() {
		return this.form.getParentKeyValue();
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	protected void updateFieldsOthersParentKeys() {
		// Fill form fields used as parent keys
		if ((this.otherParentKeys != null) && !this.otherParentKeys.isEmpty()) {
			Iterator<?> enumOtherParentKeys = this.otherParentKeys.keySet().iterator();
			while (enumOtherParentKeys.hasNext()) {
				Object oParentkey = enumOtherParentKeys.next();
				this.form.setDataFieldValue(oParentkey, this.otherParentKeys.get(oParentkey));
				DataComponent comp = this.form.getDataFieldReference(oParentkey.toString());
				if (comp != null) {
					comp.setModifiable(false);
				}
			}
		}
	}

	/**
	 * @deprecated
	 * @param otherParentKeys
	 */
	@Deprecated
	public void resetOthersParentKeys(List<?> otherParentKeys) {
		if (otherParentKeys != null) {
			for (int i = 0; i < otherParentKeys.size(); i++) {
				String formAttr = this.getFormFieldName(otherParentKeys.get(i));
				DataComponent comp = this.form.getDataFieldReference(formAttr);
				if (comp != null) {
					comp.setModifiable(true);
					comp.deleteData();
				}
			}
		}
	}

	@Override
	public void resetParentkeys(List<?> parentKeys) {
		if (parentKeys != null) {
			for (int i = 0; i < parentKeys.size(); i++) {
				String formAttr = this.getFormFieldName(parentKeys.get(i));
				DataComponent comp = this.form.getDataFieldReference(formAttr);
				if (comp != null) {
					comp.setModifiable(true);
					comp.deleteData();
				}
			}
		}
	}

	protected void updateFieldsParentkeys() {
		// Fill form fields used as parent keys
		if ((this.parentkeys != null) && !this.parentkeys.isEmpty()) {
			for (Entry<Object, Object> entry : this.parentkeys.entrySet()) {
				Object oParentkey = entry.getKey();
				this.form.setDataFieldValue(oParentkey, entry.getValue());
				DataComponent comp = this.form.getDataFieldReference(oParentkey.toString());
				if (comp != null) {
					comp.setModifiable(false);
				}
			}
		}
	}

	@Override
	public void setAttributeToFix(Object attribute, Object value) {
		if (attribute == null) {
			return;
		}
		String formAttr = this.getFormFieldName(attribute.toString());
		this.form.setDataFieldValue(formAttr, value);
		DataComponent comp = this.form.getDataFieldReference(formAttr);
		if (comp != null) {
			comp.setModifiable(false);
		}
	}

	/**
	 * @deprecated
	 * @param otherKeys
	 */
	@Deprecated
	public void setOthersParentKeys(Map<?, ?> otherKeys) {
		// For each key set the form data fields associated as no modifiable
		this.otherParentKeys = this.valuesToForm(otherKeys);
		this.updateFieldsOthersParentKeys();
	}

	/**
	 * @deprecated
	 * @param value
	 */
	@Deprecated
	public void setParentKeyValue(Object value) {
		this.form.setParentKeyValue(this.parentKeyName, value);
		this.parentKeyValue = value;
	}

	@Override
	public void setParentKeyValues(Map<?, ?> parentKeyValues) {
		this.parentkeys = this.valuesToForm(parentKeyValues);
		this.updateFieldsParentkeys();
	}

	/**
	 * Clear the detail form data cache and set the primary keys of the table again.<br> In this way all queries are executed when user uses the form navigation buttons.
	 */
	public void synchronize() {
		Object keyField = this.fieldsKey.get(0);
		int index = 0;
		if (keyField != null) {
			List<?> vKeys = (List<?>) this.table.getAllPrimaryKeys().get(keyField);
			if (vKeys != null) {
				index = vKeys.indexOf(this.form.getDataFieldValue(keyField.toString()));
			}
		}
		if (index < 0) {
			index = 0;
		}
		this.setKeys(this.table.getAllPrimaryKeys(), index);
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = this.form.getTextsToTranslate();
		if (this.title != null) {
			v.add(this.title);
		}
		return v;
	}

	@Override
	public void setResourceBundle(ResourceBundle res) {
		ResourceBundle formBundle = this.form.getResourceBundle();
		if (!res.equals(formBundle)) {
			this.form.setResourceBundle(res);
		}

		try {
			if (res != null) {
				this.setTitle(res.getString(this.title));
			} else {
				this.setTitle(this.title);
			}
		} catch (Exception e) {
			this.setTitle(this.title);
			DetailForm.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
		this.form.setComponentLocale(l);
	}

	public void updateUI() {
		this.getRootPane().updateUI();
	}

	@Override
	public Form getForm() {
		return this.form;
	}

	protected int getRecordCount() {
		if (this.tableKeys.isEmpty()) {
			return 0;
		} else {
			Iterator<?> enumTableKeys = this.tableKeys.keySet().iterator();
			List<?> vKeys = (List<?>) this.tableKeys.get(enumTableKeys.next());
			return vKeys.size();
		}
	}

	protected void updateNavigationButtonState() {
		int recordNumber = this.getRecordCount();
		if (recordNumber <= 1) {
			this.form.previousButton.setEnabled(false);
			this.form.startButton.setEnabled(false);
			this.form.endButton.setEnabled(false);
			this.form.nextButton.setEnabled(false);
			return;
		} else {
			this.form.previousButton.setEnabled(true);
			this.form.startButton.setEnabled(true);
			this.form.endButton.setEnabled(true);
			this.form.nextButton.setEnabled(true);
		}
		if (this.vectorIndex == 0) {
			this.form.previousButton.setEnabled(false);
			this.form.startButton.setEnabled(false);
			this.form.endButton.setEnabled(true);
			this.form.nextButton.setEnabled(true);
		} else if (this.vectorIndex >= (recordNumber - 1)) {
			this.form.nextButton.setEnabled(false);
			this.form.endButton.setEnabled(false);
			this.form.startButton.setEnabled(true);
			this.form.previousButton.setEnabled(true);
		}
	}

	protected void installButtonsListeners() {
		// If form is a FormExt, then it is not necessary to install the buttons
		// listeners. The FormExt uses the keys to query the other data.
		if (this.form instanceof FormExt) {
			return;
		}
		// remove the buttons event managers
		if (this.form.previousButton != null) {
			this.form.resultCountLabel.setVisible(false);
			this.form.previousButton.removeActionListener(this.form.previousButtonListener);
			this.form.nextButton.removeActionListener(this.form.nextButtonListener);
			this.form.endButton.removeActionListener(this.form.endButtonListener);
			this.form.startButton.removeActionListener(this.form.startButtonListener);
		}
		if ((this.form.previousButton == null) || (this.form.endButton == null) || (this.form.startButton == null) || (this.form.nextButton == null)) {
			if (this.form.clearDataFieldButton == null) {
				this.form.clearDataFieldButton = new JButton(this.form.deleteButtonText);
			}
			try {
				this.form.nextIcon = ImageManager.getIcon(ImageManager.NEXT_2);
				this.form.previousIcon = ImageManager.getIcon(ImageManager.PREVIOUS_2);
				this.form.endIcon = ImageManager.getIcon(ImageManager.END_2);
				this.form.startIcon = ImageManager.getIcon(ImageManager.START_2);
				this.form.nextButton = new JButton(this.form.nextIcon);
				this.form.previousButton = new JButton(this.form.previousIcon);
				this.form.endButton = new JButton(this.form.endIcon);
				this.form.startButton = new JButton(this.form.startIcon);
				this.form.previousButton.setMnemonic(KeyEvent.VK_LEFT);
				this.form.nextButton.setMnemonic(KeyEvent.VK_RIGHT);
				this.form.nextButton.setMargin(new Insets(0, 0, 0, 0));
				this.form.previousButton.setMargin(new Insets(0, 0, 0, 0));
				this.form.endButton.setMargin(new Insets(0, 0, 0, 0));
				this.form.startButton.setMargin(new Insets(0, 0, 0, 0));
			} catch (Exception e) {
				DetailForm.logger.debug(this.getClass().toString() + ": " + "Detail form. Icons error. " + e.getMessage(), e);
				this.form.nextButton = new JButton("->");
				this.form.previousButton = new JButton("<-");
				this.form.endButton = new JButton(">>");
				this.form.startButton = new JButton("<<");
				this.form.previousButton.setMnemonic(KeyEvent.VK_LEFT);
				this.form.nextButton.setMnemonic(KeyEvent.VK_RIGHT);
			}
			// Add buttons to the north
			this.form.buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			this.form.buttonPanel.add(this.form.startButton);
			this.form.buttonPanel.add(this.form.previousButton);
			this.form.buttonPanel.add(this.form.nextButton);
			this.form.buttonPanel.add(this.form.endButton);
			this.form.buttonPanel.add(this.form.clearDataFieldButton);
		}

		// Event listeners
		// Get the form buttons
		this.form.previousButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evento) {
				if (DetailForm.this.vectorIndex > 0) {
					DetailForm.this.vectorIndex--;
				}
				DetailForm.this.form.endButton.setEnabled(true);
				DetailForm.this.form.nextButton.setEnabled(true);
				// Checks if the record has yet been queried. If the record has
				// been queried then the key exist in the data.
				// If record has more than one key all keys have to been
				// checked.
				boolean dataExist = false;
				int index = -1;
				// Get each key and check if it exist in key vector
				Enumeration<?> enumKeys = DetailForm.this.data.keys();
				List<?> vAux = (List<?>) DetailForm.this.data.get(enumKeys.nextElement());
				int numberOfRecordsInData = vAux.size();
				int keysNumber = DetailForm.this.fieldsKey.size();
				for (int i = 0; i < numberOfRecordsInData; i++) {
					boolean bAllMatch = true;
					// For each key field check if the value matchs with the
					// record -i.
					for (int j = 0; j < keysNumber; j++) {
						Object oKeyField = DetailForm.this.fieldsKey.get(j);
						List<?> vDataKeyValues = (List<?>) DetailForm.this.data.get(oKeyField);
						List<?> vKeyValues = (List<?>) DetailForm.this.tableKeys.get(oKeyField);
						Object oKey = vKeyValues.get(DetailForm.this.vectorIndex);
						if (!vDataKeyValues.get(i).equals(oKey)) {
							bAllMatch = false;
							break;
						}
					}
					// If all keys match then stop
					if (bAllMatch) {
						dataExist = true;
						index = i;
						break;
					}
				}
				if (dataExist) {
					DetailForm.this.form.updateDataFields(DetailForm.this.data);
					DetailForm.this.form.updateDataFieldNavegationButton(index);
					DetailForm.this.updateNavigationButtonState();
					return;
				}
				EntityResult result = DetailForm.this.query(DetailForm.this.vectorIndex);
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					DetailForm.this.form.message(result.getMessage(), Form.ERROR_MESSAGE);
					return;
				}
				int newDataIndex = 0;
				Enumeration<?> keys = result.keys();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object oValue = result.get(key);
					if (oValue instanceof List<?>) {
						List<Object> vValues = (List<Object>) DetailForm.this.data.get(key);
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						vValues.add(newDataIndex, ((List<?>) oValue).get(0));
					} else {
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						((List<Object>) DetailForm.this.data.get(key)).add(newDataIndex, oValue);
					}
				}
				DetailForm.this.form.updateDataFields(DetailForm.this.data);
				// Set the current index in the form. Search in the data
				DetailForm.this.form.updateDataFieldNavegationButton(newDataIndex);
				DetailForm.this.updateNavigationButtonState();
			}
		});

		this.form.nextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evento) {
				if (DetailForm.this.vectorIndex < (DetailForm.this.getRecordCount() - 1)) {
					DetailForm.this.vectorIndex++;
				}
				boolean dateAlreadyExist = false;
				int existentDataIndex = -1;
				// Get each key and check in the key vector if match with the
				// record keys
				Enumeration<?> enumKeys = DetailForm.this.data.keys();
				List<?> vAux = (List<?>) DetailForm.this.data.get(enumKeys.nextElement());
				int numberOfRecordsInData = vAux.size();
				int keysNumber = DetailForm.this.fieldsKey.size();
				for (int i = 0; i < numberOfRecordsInData; i++) {
					boolean bAllMatch = true;
					// Checks for each key field if the data in index i match
					// with the record key
					// If record does not exist then query
					for (int j = 0; j < keysNumber; j++) {
						Object oKeyField = DetailForm.this.fieldsKey.get(j);
						List<?> vDataKeysValues = (List<?>) DetailForm.this.data.get(oKeyField);
						List<?> vKeyValues = (List<?>) DetailForm.this.tableKeys.get(oKeyField);
						Object oKey = vKeyValues.get(DetailForm.this.vectorIndex);
						if (!vDataKeysValues.get(i).equals(oKey)) {
							bAllMatch = false;
							break;
						}
					}
					// If all match then stop
					if (bAllMatch) {
						dateAlreadyExist = true;
						existentDataIndex = i;
						break;
					}
				}
				if (dateAlreadyExist) {
					DetailForm.this.form.updateDataFields(DetailForm.this.data);
					DetailForm.this.form.updateDataFieldNavegationButton(existentDataIndex);
					DetailForm.this.updateNavigationButtonState();
					return;
				}
				EntityResult result = DetailForm.this.query(DetailForm.this.vectorIndex);
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					DetailForm.this.form.message(result.getMessage(), Form.ERROR_MESSAGE);
					DetailForm.this.updateNavigationButtonState();
					return;
				}
				int newDataIndex = 0;
				Enumeration<?> keys = result.keys();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object oValue = result.get(key);
					if (oValue instanceof List) {
						List<Object> vValues = (List<Object>) DetailForm.this.data.get(key);
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						vValues.add(newDataIndex, ((List<?>) oValue).get(0));
					} else {
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						((List<Object>) DetailForm.this.data.get(key)).add(newDataIndex, oValue);
					}
				}
				DetailForm.this.form.updateDataFields(DetailForm.this.data);
				// Set the current index in the form. Search in the data
				DetailForm.this.form.updateDataFieldNavegationButton(newDataIndex);
				DetailForm.this.updateNavigationButtonState();
			}
		});

		this.form.startButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				DetailForm.this.vectorIndex = 0;
				boolean dataAlreadyExist = false;
				int existentDataIndex = -1;
				Enumeration<?> enumKeys = DetailForm.this.data.keys();
				List<?> vAux = (List<?>) DetailForm.this.data.get(enumKeys.nextElement());
				int dataRecordNumber = vAux.size();
				int keysNumber = DetailForm.this.fieldsKey.size();
				for (int i = 0; i < dataRecordNumber; i++) {
					boolean allMatch = true;
					// Check for each key field if the data matches with the
					// record key. If keys does not exist then query
					for (int j = 0; j < keysNumber; j++) {
						Object oKeyField = DetailForm.this.fieldsKey.get(j);
						List<?> oDataKeysValues = (List<?>) DetailForm.this.data.get(oKeyField);
						List<?> vParentKeysValues = (List<?>) DetailForm.this.tableKeys.get(oKeyField);
						Object oKey = vParentKeysValues.get(DetailForm.this.vectorIndex);
						if (!oDataKeysValues.get(i).equals(oKey)) {
							allMatch = false;
							break;
						}
					}
					// If all coincide then stop
					if (allMatch) {
						dataAlreadyExist = true;
						existentDataIndex = i;
						break;
					}
				}
				if (dataAlreadyExist) {
					DetailForm.this.form.updateDataFields(DetailForm.this.data);
					DetailForm.this.form.updateDataFieldNavegationButton(existentDataIndex);
					DetailForm.this.updateNavigationButtonState();
					return;
				}
				EntityResult result = DetailForm.this.query(DetailForm.this.vectorIndex);
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					DetailForm.this.form.message(result.getMessage(), Form.ERROR_MESSAGE);
					return;
				}
				int newDataIndex = 0;
				Enumeration<?> keys = result.keys();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object oValue = result.get(key);
					if (oValue instanceof List) {
						List<Object> vValues = (List<Object>) DetailForm.this.data.get(key);
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						vValues.add(newDataIndex, ((List<?>) oValue).get(0));
					} else {
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						((List<Object>) DetailForm.this.data.get(key)).add(newDataIndex, oValue);
					}
				}
				DetailForm.this.form.updateDataFields(DetailForm.this.data);
				// Set the current index in the form. Search in the data
				DetailForm.this.form.updateDataFieldNavegationButton(newDataIndex);
				DetailForm.this.updateNavigationButtonState();
			}
		});

		this.form.endButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				DetailForm.this.vectorIndex = DetailForm.this.getRecordCount() - 1;
				DetailForm.this.vectorIndex = Math.max(DetailForm.this.vectorIndex, 0);
				// Checks if the record has already been queried.
				// This is when all the record keys exist in the data
				boolean alreadyExist = false;
				int existDataIndex = -1;
				// Get each key and check it
				Enumeration<?> enumKeys = DetailForm.this.data.keys();
				List<?> vAux = (List<?>) DetailForm.this.data.get(enumKeys.nextElement());
				int recordNumberInData = vAux.size();
				int keysNumber = DetailForm.this.fieldsKey.size();
				for (int i = 0; i < recordNumberInData; i++) {
					boolean bAllMatch = true;
					// Checks for each key field if the data in index i matchs
					// with the record keys.
					// When record does not exist then query it
					for (int j = 0; j < keysNumber; j++) {
						Object oKeyField = DetailForm.this.fieldsKey.get(j);
						List<?> vDataKeyValues = (List<?>) DetailForm.this.data.get(oKeyField);
						List<?> vKeyvalues = (List<?>) DetailForm.this.tableKeys.get(oKeyField);
						Object oKey = vKeyvalues.get(DetailForm.this.vectorIndex);
						if (!vDataKeyValues.get(i).equals(oKey)) {
							bAllMatch = false;
							break;
						}
					}
					// If all match then stop
					if (bAllMatch) {
						alreadyExist = true;
						existDataIndex = i;
						break;
					}
				}
				if (alreadyExist) {
					DetailForm.this.form.updateDataFields(DetailForm.this.data);
					DetailForm.this.form.updateDataFieldNavegationButton(existDataIndex);
					DetailForm.this.updateNavigationButtonState();
					return;
				}
				EntityResult result = DetailForm.this.query(DetailForm.this.vectorIndex);
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					DetailForm.this.form.message(result.getMessage(), Form.ERROR_MESSAGE);
					return;
				}
				int newDataIndex = 0;
				Enumeration<?> keys = result.keys();
				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object oValue = result.get(key);
					if (oValue instanceof List) {
						List<Object> vValues = (List<Object>) DetailForm.this.data.get(key);
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						vValues.add(newDataIndex, ((List<?>) oValue).get(0));
					} else {
						newDataIndex = ((List<?>) DetailForm.this.data.get(key)).size();
						((List<Object>) DetailForm.this.data.get(key)).add(newDataIndex, oValue);
					}
				}
				DetailForm.this.form.updateDataFields(DetailForm.this.data);
				// Set the current index in the form
				DetailForm.this.form.updateDataFieldNavegationButton(newDataIndex);
				DetailForm.this.updateNavigationButtonState();
			}
		});
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (this.getForm().getInteractionManager() != null) {
				if (this.getCheckDataBeforeClose()) {
					if ((!this.getForm().getInteractionManager().getModifiedFieldAttributes().isEmpty()) && this.getForm().getInteractionManager()
							.getCheckModifiedDataChangeEvent()) {
						boolean bClose = this.getForm().question(DetailForm.M_MODIFIED_DATA_CLOSE_AND_LOST_CHANGES);
						if (!bClose) {
							return;
						} else {
							// To avoid two questions to close the window
							boolean previousValueAskOnClose = this.isAskOnClose();
							boolean previousValueAskOnEsc = this.isAskOnEsc();
							this.setAskOnClose(false);
							this.setAskOnEsc(false);
							super.processWindowEvent(e);
							this.setAskOnClose(previousValueAskOnClose);
							this.setAskOnEsc(previousValueAskOnEsc);
							return;
						}
					}
				}
			}
		}
		super.processWindowEvent(e);
	}

	@Override
	protected void processKeyEvent(KeyEvent event) {
		super.processKeyEvent(event);
	}

	public void changeDynamicForm(Map<?, ?> dynamicFormData) {
		Form previousForm = this.form;
		DynamicFormManager dynamicFormManager = previousForm.getDynamicFormManager();
		if (dynamicFormManager != null) {
			String formToShow = dynamicFormManager.getForm(dynamicFormData);
			if (!this.cacheForms.containsKey(formToShow)) {
				IFormManager formManager = previousForm.getFormManager();
				if (formManager.getInteractionManager(formToShow) == null) {
					formManager.setInteractionManager(dynamicFormManager.getFormInteractionManagerClass(formToShow), formToShow);
				}
				Form f = formManager.getFormCopy(formToShow);
				this.form = f;
				f.setDynamicFormManager(dynamicFormManager);
				f.setDetailForm(this);
				this.cacheForms.put(formToShow, f);
				this.getContentPane().add(f, formToShow);
				// Registers as form listener
				f.addDataNavigationListener(this);
				// Install the navigation buttons listeners
				// Uninstall the old form listeners and install a new ones for
				// the new form
				this.installButtonsListeners();

				if (ApplicationManager.DEBUG) {
					DetailForm.logger.debug("Registered DetailForm as DataChanged listener to manage the dynamic form: " + formToShow);
				}
			}
			this.showForm(formToShow);
			if (ApplicationManager.DEBUG) {
				DetailForm.logger.debug("DetailForm: current form is : " + this.form.getArchiveName());
			}
			if ((this.form != null) && (this.form != previousForm)) {
				// WARNING: Configure the no modifiable fields and the values
				// for the fields that are not in the data list
				Map<Object, Object> hFieldValues = previousForm.getDataFieldValues(false);
				this.form.setDataFieldValues(hFieldValues);
				List<Object> v = previousForm.getDataFieldAttributeList();
				for (int i = 0; i < v.size(); i++) {
					DataComponent component = previousForm.getDataFieldReference(v.get(i).toString());
					if (component != null) {
						if (!component.isModifiable()) {
							this.form.setModifiable(v.get(i).toString(), false);
						}
					}
				}

				// Data list. Shared

				this.dataChangeEventProcessing = false;
				try {
					EntityResult hDataList = previousForm.totalDataList;
					if (this.form.getInteractionManager() != null) {
						this.form.getInteractionManager().setDataChangedEventProcessing(false);
					}
					if (this.form instanceof FormExt) {
						((FormExt) this.form).updateDataFields(hDataList, -1);
					} else {
						this.form.updateDataFields(hDataList);
					}
					if ((previousForm instanceof FormExt) && (this.form instanceof FormExt)) {
						((FormExt) this.form).queryRecordIndex = new ArrayList<>(((FormExt) previousForm).queryRecordIndex);
					}

				} catch (Exception ex) {
					DetailForm.logger.error(null, ex);
				} finally {
					if (this.form.getInteractionManager() != null) {
						this.form.getInteractionManager().setDataChangedEventProcessing(true);
					}
				}
				this.dataChangeEventProcessing = true;

				// Now the interaction manager status
				InteractionManager formInteractionManager = this.form.getInteractionManager();
				if (formInteractionManager != null) {
					int currentMode = previousForm.getInteractionManager().currentMode;
					switch (currentMode) {
						case InteractionManager.INSERT:
							formInteractionManager.setInsertMode();
							break;
						case InteractionManager.QUERY:
							formInteractionManager.setQueryMode();
							break;
						case InteractionManager.QUERYINSERT:
							formInteractionManager.setQueryInsertMode();
							break;
						case InteractionManager.UPDATE:
							formInteractionManager.setUpdateMode();
							break;
						default:
							formInteractionManager.setUpdateMode();
							break;
					}
				}

			} else {
				// If the form is the same then nothing is done
				if (ApplicationManager.DEBUG) {
					DetailForm.logger.debug("New form to show is the same that the previous one");
				}
			}
		} else if (ApplicationManager.DEBUG) {
			DetailForm.logger.debug("changeDynamicForm: Dynamico form manager has not been established");
		}
	}

	@Override
	public boolean dataWillChange(DataNavigationEvent e) {
		return true;
	}

	@Override
	public void dataChanged(DataNavigationEvent e) {
		if (!this.dataChangeEventProcessing) {
			return;
		}
		Map<?, ?> hFormData = e.getData();
		int viewIndex = this.table.getRowForKeys(this.valuesToTable(hFormData));
		if ((!hFormData.isEmpty()) && (viewIndex >= 0)) {
			this.table.setSelectedRow(viewIndex);
		}
		// Show the appropriate form if dynamic form manager exists
		Form previousForm = this.form;

		if (ApplicationManager.DEBUG) {
			DetailForm.logger.debug("Previous form: " + previousForm.getArchiveName());
		}
		DynamicFormManager dynamicFormManager = previousForm.getDynamicFormManager();
		if (dynamicFormManager != null) {
			String formToShow = dynamicFormManager.getForm(hFormData);
			if (!this.cacheForms.containsKey(formToShow)) {
				IFormManager formManager = previousForm.getFormManager();
				if (formManager.getInteractionManager(formToShow) == null) {
					formManager.setInteractionManager(dynamicFormManager.getFormInteractionManagerClass(formToShow), formToShow);
				}
				Form f = formManager.getFormCopy(formToShow);
				this.form = f;
				f.setDynamicFormManager(dynamicFormManager);
				f.setDetailForm(this);
				this.cacheForms.put(formToShow, f);
				this.getContentPane().add(f, formToShow);
				// Register as listeners of the new form
				f.addDataNavigationListener(this);
				// Install the navigation buttons listeners
				// Uninstall the form listeners and install a new ones for the
				// new form
				this.installButtonsListeners();
				if (ApplicationManager.DEBUG) {
					DetailForm.logger.debug("Registering DeatailForm as DataChanged listener to manage dynamic form: " + formToShow);
				}
			}
			this.showForm(formToShow);
			if (ApplicationManager.DEBUG) {
				DetailForm.logger.debug("Current form: " + this.form.getArchiveName());
			}
			if ((this.form != null) && (this.form != previousForm)) {
				// WARNING: Configure the no modifiable fields and values for
				// fields that are not in the data list
				Map<?, ?> hFieldsValues = previousForm.getDataFieldValues(false);
				this.form.setDataFieldValues(hFieldsValues);
				List<?> v = previousForm.getDataFieldAttributeList();
				for (int i = 0; i < v.size(); i++) {
					DataComponent component = previousForm.getDataFieldReference(v.get(i).toString());
					if (component != null) {
						if (!component.isModifiable()) {
							this.form.setModifiable(v.get(i).toString(), false);
						}
					}
				}
				// Data list. Shared

				this.dataChangeEventProcessing = false;
				try {
					EntityResult hDataList = previousForm.totalDataList;
					if (this.form.getInteractionManager() != null) {
						this.form.getInteractionManager().setDataChangedEventProcessing(false);
					}

					if (this.form instanceof FormExt) {
						((FormExt) this.form).updateDataFields(hDataList, -1);
					} else {
						this.form.updateDataFields(hDataList);
					}
					if ((previousForm instanceof FormExt) && (this.form instanceof FormExt)) {
						((FormExt) this.form).queryRecordIndex = new ArrayList<>(((FormExt) previousForm).queryRecordIndex);
					}

					// Select the record
					if (ApplicationManager.DEBUG) {
						DetailForm.logger.debug(previousForm.totalDataList + " : Index: " + e.getIndex());
					}
					this.form.updateDataFieldNavegationButton(e.getIndex());
				} catch (Exception ex) {
					DetailForm.logger.error(null, ex);
				} finally {
					if (this.form.getInteractionManager() != null) {
						this.form.getInteractionManager().setDataChangedEventProcessing(true);
					}
				}
				this.dataChangeEventProcessing = true;

				// Now the interaction manager status
				InteractionManager interactionManager = this.form.getInteractionManager();
				if (interactionManager != null) {
					int currentMode = previousForm.getInteractionManager().currentMode;
					switch (currentMode) {
						case InteractionManager.INSERT:
							interactionManager.setInsertMode();
							break;
						case InteractionManager.QUERY:
							interactionManager.setQueryMode();
							break;
						case InteractionManager.QUERYINSERT:
							interactionManager.setQueryInsertMode();
							break;
						case InteractionManager.UPDATE:
							interactionManager.setUpdateMode();
							break;
						default:
							interactionManager.setUpdateMode();
							break;
					}
				}

			} else {
				// If the new form is the save as the previous one then nothing
				// is done
				if (ApplicationManager.DEBUG) {
					DetailForm.logger.debug("New form to show is the same as the previous one");
				}
			}

		}
	}

	public void updateDataCacheActualRecord() {
		// Query the current data record values
		EntityResult result = this.query(this.vectorIndex);
		if (result.getCode() == EntityResult.OPERATION_WRONG) {
			this.form.message(result.getMessage(), Form.ERROR_MESSAGE);
			return;
		}
		Enumeration keys = result.keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object oValue = result.get(key);
			if (oValue instanceof List) {
				List<Object> vValues = (List<Object>) this.data.get(key);
				if (this.vectorIndex < vValues.size()) {
					vValues.remove(this.vectorIndex);
					vValues.add(this.vectorIndex, ((List<?>) oValue).get(0));
				}
			} else {
				List<Object> vValues = (List<Object>) this.data.get(key);
				if (this.vectorIndex < vValues.size()) {
					vValues.remove(this.vectorIndex);
					vValues.add(this.vectorIndex, oValue);
				}
			}
		}
		this.form.updateDataFields(this.vectorIndex);
	}

	public void centerOnScreen() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width / 2) - (this.getWidth() / 2), (d.height / 2) - (this.getHeight() / 2));
	}

	public void showForm(String formName) {
		// If the form is in cache then show it, in other case load it first
		if (this.cacheForms.containsKey(formName)) {
			this.cardLayout.show(this.getContentPane(), formName);
			this.form = (Form) this.cacheForms.get(formName);
		}

	}

	protected void reload(Form f) {
		if (f == null) {
			return;
		}
		try {
			if (this.cacheForms.containsValue(f)) {
				IFormManager formManager = f.getFormManager();
				String fileName = f.getArchiveName();
				this.cacheForms.remove(fileName);
				if (f.getInteractionManager() != null) {
					f.getInteractionManager().free();
					f.free();
				}
				Container container = f.getParent();
				if (container != null) {
					Object constraints = null;
					LayoutManager l = container.getLayout();
					if (l instanceof CardLayout) {
						constraints = f.getArchiveName();
					}
					container.remove(f);
					Form newForm = formManager.getFormCopy(fileName);
					container.add(newForm, constraints);
					if (this.form == f) {
						this.form = newForm;
					}
					container.validate();
					container.repaint();
				}
			}
		} catch (Exception e) {
			DetailForm.logger.error(null, e);
		}
	}

	public void setEntityName(String name) {
		this.form.setEntityName(name);
	}

	public void resetEntityName() {
		this.form.resetEntityName();
	}

	public void setCheckDataWhenClose(boolean v) {
		this.checkDataBeforeClose = v;
	}

	public boolean getCheckDataBeforeClose() {
		return this.checkDataBeforeClose;
	}

	@Override
	public void free() {
		FreeableUtils.clearMap(this.tableKeys);
		// FreeableUtils.clearMap(this.data);
		this.data.clear();
		FreeableUtils.clearMap(this.otherParentKeys);
		FreeableUtils.clearMap(this.parentkeys);
		FreeableUtils.clearMap(this.cacheForms);
		FreeableUtils.clearMap(this.codValues);
		FreeableUtils.clearMap(this.reverseCodValues);
		FreeableUtils.clearCollection(this.fieldsKey);
		if (this.form != null) {
			this.form.free();
		}
		this.parentKeyValue = null;
		this.cardLayout = null;
		this.activedFormListener = null;
		FreeableUtils.freeComponent(this.table);
		FreeableUtils.freeComponent(this.getComponents());
		this.table = null;
	}

}
