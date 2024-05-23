package com.ontimize.gui.container;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Types;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/**
 * Implements a data component group where data are used like an entity for a component. To perform a query when focus is lost, component should be a field. <p>
 *
 * @author Imatia Innovation
 */
public class MultiReference extends Column implements OpenDialog, ReferenceComponent, DataComponent, AccessForm {

	private static final Logger			logger			= LoggerFactory.getLogger(MultiReference.class);

	/**
	 * An object to store data. By default, null.
	 */
	protected Object					storedValue		= null;

	/**
	 * The required condition. By default, false.
	 */
	protected boolean					required		= false;

	/**
	 * The modifiable condition. By default, true.
	 */
	protected boolean					modificable		= true;

	/**
	 * The condition to show. By default, true.
	 */
	protected boolean					show			= true;

	/**
	 * The entity reference. By default, null.
	 */
	protected String					entity			= null;

	/**
	 * The key reference. By default, null.
	 */
	protected String					key				= null;

	/**
	 * The parentkey reference. By default, null.
	 */
	protected String					parentKey		= null;

	/**
	 * A reference to a table window. By default, null.
	 */
	protected JDialog					tableWindow		= null;

	/**
	 * A reference to a table. By default, null.
	 */
	protected Table						t				= null;

	/**
	 * A reference to locator. By default, null.
	 */
	protected EntityReferenceLocator	locator			= null;

	/**
	 * A reference to a parent frame. By default, null.
	 */
	protected Frame						parentFrame		= null;

	/**
	 * A reference to a data components. By default, null.
	 */
	protected Map<Object, Object>		dataComponents	= null;

	/**
	 * An instance of a query button.
	 */
	protected JButton					queryButton		= new JButton();

	/**
	 * An instance of a delete button.
	 */
	protected JButton					deleteButton	= new JButton();

	/**
	 * An instance of a accept button.
	 */
	protected JButton					okButton		= null;

	/**
	 * An instance of a button panel with alignment to left. <p>
	 *
	 * @see FlowLayout#LEFT
	 */
	protected JPanel					buttonPanel		= new JPanel(new FlowLayout(FlowLayout.LEFT));

	/**
	 * An instance of focus adapter to manage the loss focus.
	 */
	protected FocusAdapter				focusListener	= new FocusAdapter() {

		@Override
		public void focusLost(FocusEvent e) {
			// When it loses the focus and the key field is not empty,
			// then execute a query
			Object c = MultiReference.this.dataComponents.get(MultiReference.this.key);
			if ((c != null) && (c instanceof DataComponent)) {
				if (!((DataComponent) c).isEmpty()) {
					Object oKeyValue = ((DataComponent) c).getValue();
					Cursor cursor = MultiReference.this.getCursor();
					try {
						MultiReference.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						if (MultiReference.this.parentForm != null) {
							MultiReference.this.parentForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						}
						Map<Object, Object> keysValues = new HashMap<>();
						if (MultiReference.this.parentKey != null) {
							keysValues.put(MultiReference.this.parentKey,
									MultiReference.this.parentForm.getDataFieldValue(MultiReference.this.parentKey));
						}
						keysValues.put(MultiReference.this.key, oKeyValue);
						EntityResult result = MultiReference.this.locator.getEntityReference(MultiReference.this.entity).query(
								keysValues, MultiReference.this.t.getAttributeList(),
								MultiReference.this.locator.getSessionId());
						if (result.getCode() == EntityResult.OPERATION_WRONG) {
							if (MultiReference.this.parentForm != null) {
								MultiReference.this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
							}
							return;
						} else {
							Map<Object, Object> fieldValue = new HashMap<>();
							Enumeration<?> en = result.keys();
							while (en.hasMoreElements()) {
								Object oKey = en.nextElement();
								Object v = result.get(oKey);
								Object val = ((List<?>) v).get(0);
								if (val != null) {
									fieldValue.put(oKey, val);
								}
							}
							MultiReference.this.setValue(fieldValue);
						}
					} catch (Exception ex) {
						MultiReference.logger.error(null, ex);
						MultiReference.this.parentForm.message(
								ex.getMessage(), Form.ERROR_MESSAGE, ex);
					} finally {
						if (MultiReference.this.parentForm != null) {
							MultiReference.this.parentForm
							.setCursor(cursor);
						}
						MultiReference.this.setCursor(cursor);
					}
				} else {
					MultiReference.this.deleteData();
				}
			}
		}
	};

	/**
	 * Class constructor, calls to {@link Column} constructor. <p>
	 *
	 * @param params
	 *            the map with parameters.
	 *
	 *            <p>
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>entity</td> <td></td> <td></td> <td>yes</td> <td>Associated entity.</td> </tr>
	 *
	 *            <tr> <td>title</td> <td></td> <td><i>entity</td> <td>no</td> <td>The title.</td> </tr>
	 *
	 *            <tr> <td>key</td> <td></td> <td></td> <td>yes</td> <td>Attribute for data component. It is the primary key for entity.</td> </tr> </Table>
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>Table parameters supported:</td> <td></td> <td></td> <td></td> <td></td> </tr>
	 *
	 *            <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>parentkey</td> <td></td> <td></td> <td>no</td> <td>To establish a filter. It will be a constraint for all queries in entity.</td> </tr>
	 *
	 *            <tr> <td>form</td> <td></td> <td></td> <td>no</td> <td>Form associated to the table.</td> </tr>
	 *
	 *            <tr> <td>otherkeys</td> <td><I>field1;...;fieldN</I></td> <td></td> <td></td> <td>Name of the other fields that are parentkeys, that are passed to the detail form
	 *            when it is open. These values will be get from the parent form, in which the <code>Table</code> is placed.</td>
	 *
	 *            <tr> <td>controls</td> <td>yes/no</td> <td>yes</td> <td></td> <td>Allows the Table to have some control buttons, in the top of it. This buttons, by default, can
	 *            export to excel the data in the table, show charts, reports, and so on. If the value is not, the controlsvisible attribute will not affect.</td> </tr>
	 *
	 *            </Table>
	 */
	public MultiReference(Map<Object, Object> params) throws Exception {
		super(params);
		this.attribute = null;
		Object entity = params.get("entity");
		if (entity == null) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				MultiReference.logger.debug("Parameter 'entity' not found in MultiReference. Check parameters.");
			}
		} else {
			if ((this.title == null) || this.title.equals("")) {
				this.title = entity.toString();
			}
			this.entity = entity.toString();
		}

		// Parameter key
		Object key = params.get("key");
		if (key == null) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				MultiReference.logger.debug("Error: Paramter 'key' not found in MultiReference. Check parameters");
			}
		} else {
			this.key = key.toString();
		}

		// Parameter : parentkey
		Object parentkey = params.get("parentkey");
		if (parentkey != null) {
			this.parentKey = parentkey.toString();
		} else {
			this.parentKey = null;
		}
		// Create the table
		this.t = new Table(params);
		this.t.enableFiltering(false);
		this.t.getJTable().getSelectionModel().addListSelectionListener(e -> {
			if (MultiReference.this.t.getSelectedRowsNumber() > 0) {
				MultiReference.this.okButton.setEnabled(true);
			} else {
				MultiReference.this.okButton.setEnabled(false);
			}
		});
		this.queryButton.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon queryIcon = ImageManager.getIcon(ImageManager.MULTIREFQUERY);
		if (queryIcon != null) {
			this.queryButton.setIcon(queryIcon);
		} else {
			this.queryButton.setText("?");
		}
		this.queryButton.addActionListener(e -> {
			Cursor cursor = MultiReference.this.getCursor();
			try {
				MultiReference.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (MultiReference.this.parentForm != null) {
					MultiReference.this.parentForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
				Map<Object, Object> keysValues = new HashMap<>();
				if (MultiReference.this.parentKey != null) {
					keysValues.put(MultiReference.this.parentKey, MultiReference.this.parentForm.getDataFieldValue(MultiReference.this.parentKey));
				}
				EntityResult result = MultiReference.this.locator.getEntityReference(MultiReference.this.entity).query(keysValues, MultiReference.this.t.getAttributeList(),
						MultiReference.this.locator.getSessionId());
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					if (MultiReference.this.parentForm != null) {
						MultiReference.this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
					}
					return;
				}
				// Now show the window and set the table value
				MultiReference.this.t.setValue(result);
				if (MultiReference.this.tableWindow == null) {
					MultiReference.this.tableWindow = new JDialog(MultiReference.this.parentFrame, true);
					JPanel southPanel = new JPanel();
					MultiReference.this.okButton = new JButton("datafield.select");
					MultiReference.this.okButton.setEnabled(false);
					ImageIcon okIcon = ImageManager.getIcon(ImageManager.OK);
					if (okIcon != null) {
						MultiReference.this.okButton.setIcon(okIcon);
					}
					southPanel.add(MultiReference.this.okButton);
					MultiReference.this.tableWindow.getContentPane().add(southPanel, BorderLayout.SOUTH);
					MultiReference.this.tableWindow.getContentPane().add(MultiReference.this.t);
					MultiReference.this.okButton.addActionListener(e1 -> {
						if (MultiReference.this.t.getSelectedRow() >= 0) {
							Map<Object, Object> hFieldValue = MultiReference.this.t.getRowData(MultiReference.this.t.getSelectedRow());
							MultiReference.this.setValue(hFieldValue);
							MultiReference.this.tableWindow.setVisible(false);
						}
					});
					MultiReference.this.t.packTable();
					Object oValue1 = MultiReference.this.getValue();
					if (oValue1 != null) {
						int ind1 = MultiReference.this.t.getPrimaryKeys().indexOf(oValue1);
						if (ind1 >= 0) {
							MultiReference.this.t.setSelectedRow(ind1);
						}
					}
					MultiReference.this.tableWindow.pack();
					// Minimun size
					if (MultiReference.this.tableWindow.getSize().getWidth() < 300) {
						MultiReference.this.tableWindow.setSize(300, (int) MultiReference.this.tableWindow.getSize().getHeight());
					}
					MultiReference.this.tableWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
					MultiReference.this.tableWindow.setLocation((d.width - MultiReference.this.tableWindow
							.getSize().width) / 2,
							(d.height - MultiReference.this.tableWindow.getSize().height) / 2);
					MultiReference.this.tableWindow.setVisible(true);
				} else {
					Object oValue2 = MultiReference.this.getValue();
					if (oValue2 != null) {
						int ind2 = MultiReference.this.t.getPrimaryKeys().indexOf(oValue2);
						if (ind2 >= 0) {
							MultiReference.this.t.setSelectedRow(ind2);
						}
					}
					MultiReference.this.tableWindow.setVisible(true);
				}
			} catch (Exception ex) {
				MultiReference.logger.error(null, ex);
				MultiReference.this.parentForm.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
			} finally {
				if (MultiReference.this.parentForm != null) {
					MultiReference.this.parentForm.setCursor(cursor);
				}
				MultiReference.this.setCursor(cursor);
			}
		});

		ImageIcon deleteIcon = ImageManager.getIcon(ImageManager.DELETE);
		if (deleteIcon != null) {
			this.deleteButton.setIcon(deleteIcon);
		} else {
			this.deleteButton.setText("..");
		}
		this.deleteButton.setMargin(new Insets(0, 0, 0, 0));
		this.deleteButton.addActionListener(evento -> MultiReference.this.deleteData());
		this.add(this.buttonPanel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.buttonPanel.add(this.queryButton);
		this.buttonPanel.add(this.deleteButton);
	}

	@Override
	public Object getValue() {
		this.checkComponentListOK();
		DataComponent c = (DataComponent) this.dataComponents.get(this.key);
		if (c != null) {
			return c.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Returns the not null field values associated to this object. Returns null when {@link #getValue()} returns null. <p>
	 *
	 * @return a <code>Map</code> with attributes and values
	 */
	public Map<Object, Object> getValues() {
		if (this.getValue() == null) {
			return null;
		}
		Map<Object, Object> values = new HashMap<>();
		for (Entry<Object, Object> entry : this.dataComponents.entrySet()) {
			Object attribute = entry.getKey();
			DataComponent c = (DataComponent) entry.getValue();
			if (!c.isEmpty()) {
				values.put(attribute, c.getValue());
			}
		}
		return values;
	}

	/**
	 * Sets the component value. Calls to {@link #setValue(Object)} <p>
	 *
	 * @param attribute
	 *            the attribute to set value
	 * @param value
	 *            the object to set to attribute
	 */
	protected void setComponentValue(Object attribute, Object value) {
		DataComponent c = (DataComponent) this.dataComponents.get(attribute);
		if (c != null) {
			c.setValue(value);
		}
	}

	@Override
	public void setValue(Object value) {
		this.checkComponentListOK();
		this.deleteData();
		if ((value != null) && (value instanceof Map)) {
			if (((Map<?, ?>) value).isEmpty()) {
				this.deleteData();
				return;
			}
			Enumeration<?> enumKeys = Collections.enumeration(((Map<?, ?>) value).keySet());
			while (enumKeys.hasMoreElements()) {
				Object oKey = enumKeys.nextElement();
				Object oFieldValue = ((Map<?, ?>) value).get(oKey);
				if (oFieldValue instanceof List) {
					this.setComponentValue(oKey, ((List<?>) oFieldValue).get(0));
				} else {
					this.setComponentValue(oKey, oFieldValue);
				}
			}
		} else {
			this.deleteData();
		}
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public boolean isModified() {
		Object oValue = this.getValue();
		if ((oValue == null) && (this.storedValue == null)) {
			return false;
		}
		if ((oValue == null) && (this.storedValue != null)) {
			if (ApplicationManager.DEBUG) {
				MultiReference.logger.debug("Component: " + this.attribute + " Modified: Previous value = " + this.storedValue + " .New value = " + oValue);
			}
			return true;
		}
		if ((oValue != null) && (this.storedValue == null)) {
			if (ApplicationManager.DEBUG) {
				MultiReference.logger.debug("Component: " + this.attribute + " Modified: Previous value = " + this.storedValue + " .New value = " + oValue);
			}
			return true;
		}
		if (!oValue.equals(this.storedValue)) {
			if (ApplicationManager.DEBUG) {
				MultiReference.logger.debug("Component: " + this.attribute + " Modified: Previous value = " + this.storedValue + " .New value = " + oValue);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isHidden() {
		return !this.show;
	}

	@Override
	public void deleteData() {
		this.checkComponentListOK();
		for (Entry<Object, Object> entry : this.dataComponents.entrySet()) {
			((DataComponent) entry.getValue()).deleteData();
		}
	}

	@Override
	public boolean isEmpty() {
		DataComponent c = (DataComponent) this.dataComponents.get(this.key);
		if (c == null) {
			return true;
		} else {
			return c.isEmpty();
		}
	}

	@Override
	public void setModifiable(boolean modif) {
		this.modificable = modif;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			boolean permission = this.checkEnabledPermission();
			if (!permission) {
				return;
			}
		}
		super.setEnabled(enabled);
		this.queryButton.setEnabled(enabled);
		this.deleteButton.setEnabled(enabled);
		this.checkComponentListOK();
		for (Entry<Object, Object> entry : this.dataComponents.entrySet()) {
			Object oKey = entry.getKey();
			DataComponent c = this.parentForm.getDataFieldReference(oKey.toString());
			if (c != null) {
				if (this.key.equals(c.getAttribute().toString())) {
					c.setEnabled(enabled);
				} else {
					c.setEnabled(false);
				}
			}
		}
	}

	@Override
	public boolean isModifiable() {
		return this.modificable;
	}

	@Override
	public int getSQLDataType() {
		DataComponent keyComponent = (DataComponent) this.dataComponents.get(this.key);
		if (keyComponent == null) {
			return Types.VARCHAR;
		} else {
			return keyComponent.getSQLDataType();
		}
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator locator) {
		this.locator = locator;
	}

	@Override
	public Object getAttribute() {
		if (this.attribute == null) {
			this.attribute = new HashMap<>();
			((Map<Object, Object>) this.attribute).put(this.entity, this.t.getAttributeList());
		}
		return ((HashMap<?, ?>) this.attribute).clone();
	}

	@Override
	public void setParentFrame(Frame marco) {
		this.parentFrame = marco;
		this.t.setParentFrame(this.parentFrame);
	}

	@Override
	public String getLabelComponentText() {
		if (this.getBorder() != null) {
			return ((TitledBorder) this.getBorder()).getTitle();
		} else {
			return "";
		}
	}

	@Override
	public void setParentForm(Form f) {
		this.parentForm = f;
		this.t.setParentForm(this.parentForm);
	}

	/**
	 * Creates the form component list manages by this object. All list component are set no modifiables. So, form is not allowed to delete/activate it.
	 *
	 */
	protected void checkComponentListOK() {
		// This method is called after the form creation.
		if (this.dataComponents == null) {
			this.dataComponents = new HashMap<>();
			List<?> attributes = this.t.getAttributeList();
			for (int i = 0; i < attributes.size(); i++) {
				Object oAttribute = attributes.get(i);
				Object c = this.parentForm.getDataFieldReference(oAttribute.toString());
				if (oAttribute.equals(this.key)) {
					if (c != null) {
						((DataComponent) c).setModifiable(false);
					}
				} else {
					if (c instanceof DataField) {
						((DataField) c).getDataField().addFocusListener(this.focusListener);
					}
				}
				if (c != null) {
					this.dataComponents.put(oAttribute, c);
				}
			}
		}
	}

	/**
	 * This method is similar to introduce the code into the field from user interface. It queries to entity and fills the fields. <p>
	 *
	 * @param code
	 *            the object to set code
	 */
	public void setCode(Object code) {
		DataComponent c = (DataComponent) this.dataComponents.get(this.key);
		c.setValue(code);
		if (c.isEmpty()) {
			Cursor cursor = this.getCursor();
			try {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (this.parentForm != null) {
					this.parentForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				}
				Map<Object, Object> keysValues = new HashMap<>();
				keysValues.put(this.key, code);
				if (this.parentKey != null) {
					keysValues.put(this.parentKey, this.parentForm.getDataFieldValue(this.parentKey));
				}
				EntityResult result = this.locator.getEntityReference(this.entity).query(keysValues, this.t.getAttributeList(), this.locator.getSessionId());
				if (result.getCode() == EntityResult.OPERATION_WRONG) {
					if (this.parentForm != null) {
						this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
					}
					return;
				} else {
					Map<Object, Object> fieldValue = new HashMap<>();
					Enumeration<?> en = result.keys();
					while (en.hasMoreElements()) {
						Object oKey = en.nextElement();
						Object v = result.get(oKey);
						Object val = ((List<?>) v).get(0);
						if (val != null) {
							fieldValue.put(oKey, val);
						}
					}
					this.setValue(fieldValue);
				}
			} catch (Exception ex) {
				MultiReference.logger.error(null, ex);
				this.parentForm.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
			} finally {
				if (this.parentForm != null) {
					this.parentForm.setCursor(cursor);
				}
				this.setCursor(cursor);
			}
		} else {
			this.deleteData();
		}
	}

}
