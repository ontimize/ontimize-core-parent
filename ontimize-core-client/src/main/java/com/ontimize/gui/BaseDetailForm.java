package com.ontimize.gui;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.db.OEntityResultImpl;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public abstract class BaseDetailForm extends JPanel implements IDetailForm {

	private static final Logger		logger				= LoggerFactory.getLogger(BaseDetailForm.class);

	protected Map<Object, Object>	tableKeys			= null;

	protected List<Object>			fieldsKey			= null;

	protected int					vectorIndex			= 0;

	protected Form					form				= null;

	protected Map<Object, Object>	parentkeys			= null;

	protected Table					table				= null;

	protected String				title				= null;

	protected EntityResult		data;

	protected Map<Object, Object>	codValues			= null;

	protected Map<Object, Object>	reverseCodValues	= null;

	@Override
	public void setComponentLocale(Locale l) {
		this.form.setComponentLocale(l);
	}

	@Override
	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.form.setResourceBundle(resourceBundle);
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = this.form.getTextsToTranslate();
		return v;
	}

	@Override
	public Form getForm() {
		return this.form;
	}

	@Override
	public Table getTable() {
		return this.table;
	}

	protected void initCodValues(Map<Object, Object> codValues) {
		if (codValues == null) {
			return;
		}
		this.codValues = codValues;
		this.reverseCodValues = new HashMap<>();
		for (Entry<Object, Object> entry : codValues.entrySet()) {
			this.reverseCodValues.put(entry.getValue(), entry.getKey());
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

	public String getTableFieldName(String name) {
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
		return this.valuesToForm(entityResult.getRecordValues(index));
	}
	@Override
	public Map<Object, Object> valuesToForm(Map<?, ?> values) {
		if (values != null) {
			Map<Object, Object> clone = new HashMap<>();
			for (Entry<?, ?> entry : values.entrySet()) {
				Object current = entry.getKey();
				clone.put(this.getFormFieldName(current.toString()), entry.getValue());
			}
			return clone;
		}
		return null;
	}

	@Override
	public Map<Object, Object> valuesToTable(Map<?, ?> values) {
		Map<Object, Object> clone = new HashMap<>();
		for (Entry<?, ?> entry : values.entrySet()) {
			Object current = entry.getKey();
			clone.put(this.getTableFieldName(current.toString()), entry.getValue());
		}
		return clone;
	}

	protected List<Object> listToForm(List<?> list) {
		List<Object> current = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			current.add(this.getFormFieldName(list.get(i).toString()));
		}
		return current;
	}

	protected void updateFieldsParentkeys() {
		// Fill form fields used as parent keys
		if ((this.parentkeys != null) && !this.parentkeys.isEmpty()) {
			Enumeration<?> enumOtherParentKeys = Collections.enumeration(this.parentkeys.keySet());
			while (enumOtherParentKeys.hasMoreElements()) {
				Object oParentkey = enumOtherParentKeys.nextElement();
				this.form.setDataFieldValue(oParentkey, this.parentkeys.get(oParentkey));
				DataComponent comp = this.form.getDataFieldReference(oParentkey.toString());
				if (comp != null) {
					comp.setModifiable(false);
				}
			}
		}
	}

	@Override
	public void setQueryInsertMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setQueryInsertMode();
	}

	@Override
	public void setUpdateMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setUpdateMode();
	}

	@Override
	public void setInsertMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setInsertMode();
	}

	@Override
	public void setQueryMode() {
		this.updateFieldsParentkeys();
		this.form.getInteractionManager().setQueryMode();
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

	@Override
	public void resetParentkeys(List<?> parentKeys) {
		if (parentKeys != null) {
			for (Object parentKey : parentKeys) {
				String formAttr = this.getFormFieldName(parentKey);
				DataComponent comp = this.form.getDataFieldReference(formAttr);
				if (comp != null) {
					comp.setModifiable(true);
					comp.deleteData();
				}
			}
		}
	}

	@Override
	public void setParentKeyValues(Map<?, ?> parentKeyValues) {
		this.parentkeys = this.valuesToForm(parentKeyValues);
		this.updateFieldsParentkeys();
	}

	/**
	 * This method sets the keys in the table records.<br> This keys are used to query the record values
	 *
	 * @param tableKeys
	 * @param index
	 */
	@Override
	public void setKeys(EntityResult tableKeys, int index) {
		this.tableKeys = this.valuesToForm(tableKeys, index);
		// Reset the index of the selected element
		this.vectorIndex = 0;

		// If there are more than one record
		int recordNumber = 0;
		if (tableKeys.isEmpty()) {
			this.form.disableButtons();
			this.form.disableDataFields();
		} else {
			Enumeration<?> enumTableKeys = Collections.enumeration(this.tableKeys.keySet());
			List<?> vKeys = (List<?>) this.tableKeys.get(enumTableKeys.nextElement());
			recordNumber = vKeys.size();
		}

		if (index < recordNumber) {
			this.vectorIndex = index;
		}
		if (!tableKeys.isEmpty()) {
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
				((FormExt) this.form).updateDataFields(this.tableKeys, this.vectorIndex);
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
					Enumeration enumOtherKeys = Collections.enumeration(this.parentkeys.keySet());
					while (enumOtherKeys.hasMoreElements()) {
						Object oParentkeyElement = enumOtherKeys.nextElement();
						if (this.parentkeys.get(oParentkeyElement) == null) {

							if (BaseDetailForm.logger.isDebugEnabled()) {
								Window w = SwingUtilities.getWindowAncestor(this);
								MessageDialog.showErrorMessage(w,
										"DEBUG: DetailForm: parentkey " + oParentkeyElement + " is NULL. It won't be included in the query. Check the xml that contains the table configuration and ensure that the parentkey has value there.");
							}
						} else {
							hKeysValues.put(oParentkeyElement, this.parentkeys.get(oParentkeyElement));
						}
					}
				}
				List<?> vTableKeys = this.table.getKeys();
				for (Object oKeyField : vTableKeys) {
					List<?> vKeyValues = (List<?>) this.tableKeys.get(oKeyField);
					if (vKeyValues.size() <= index) {
						if (BaseDetailForm.logger.isDebugEnabled()) {
							Window window = SwingUtilities.getWindowAncestor(this);
							MessageDialog.showErrorMessage(window,
									"DEBUG: DetailForm: Hashtable with the detail form keys contains less elements for the key " + oKeyField + " than the selected index " + index);
						}
						return new EntityResultMapImpl();
					}

					if (vKeyValues.get(index) == null) {
						if (BaseDetailForm.logger.isDebugEnabled()) {
							Window window = SwingUtilities.getWindowAncestor(this);
							MessageDialog.showErrorMessage(window,
									"DEBUG: DetailForm:  Hashtable with the detail form keys contains a NULL value for the key: " + oKeyField + " in the selected index: " + index);
						}
					}
					hKeysValues.put(oKeyField, vKeyValues.get(index));
				}

			} else {
				return new OEntityResultImpl();
			}
			EntityReferenceLocator referenceLocator = this.form.getFormManager().getReferenceLocator();
			Entity entity = referenceLocator.getEntityReference(this.form.getEntityName());
			List<?> vAttributeList = (List<?>) this.form.getDataFieldAttributeList().clone();
			// If key is not include then add it to the query fields, but it can
			// be
			// an ReferenceFieldAttribute
			for (Object element : this.fieldsKey) {
				boolean containsKey = false;
				for (Object oAttribute : vAttributeList) {
					if (oAttribute.equals(element)) {
						containsKey = true;
						break;
					} else if (oAttribute instanceof ReferenceFieldAttribute) {
						if (((ReferenceFieldAttribute) oAttribute).getAttr() != null) {
							if (((ReferenceFieldAttribute) oAttribute).getAttr().equals(element)) {
								containsKey = true;
								break;
							}
						}
					}
				}
				if (!containsKey) {
					vAttributeList.add(element);
				}
			}
			res = entity.query(hKeysValues, vAttributeList, referenceLocator.getSessionId());
			// For each key get the value and add it to the data
			return res;
		} catch (Exception exc) {
			BaseDetailForm.logger.error("DetailForm: Error in query. Check the parameters, the xml and the entity configuration", exc);
			if (ApplicationManager.DEBUG) {
				BaseDetailForm.logger.error(null, exc);
			}
			return new EntityResultMapImpl();
		}
	}

	@Override
	public void free() {
		this.tableKeys = null;
		this.fieldsKey = null;
		this.parentkeys = null;
		this.table = null;
		this.data = null;
		this.codValues = null;
		this.reverseCodValues = null;
		FreeableUtils.freeComponent(this.form);
		this.form = null;
	}

}
