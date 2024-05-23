package com.ontimize.gui.container;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.FormBuilder;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.CreateForms;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.TopWindow;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.field.ReferenceExtDataField;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.security.ClientSecurityManager;

/**
 * This class implements a {@link ReferenceExtDataField} similar component. It supports grouping many data components (like an entity) and fills in a query. The field has an
 * attribute, a code and columns and fields whose attributes are correspondent to these columns will be asked to associated entity. <p>
 *
 * @author Imatia Innovation
 */
public class ExtendedMultiReference extends JPanel
implements FormComponent, AccessForm, ReferenceComponent, OpenDialog, Internationalization, CreateForms, IdentifiedElement, Freeable {

	private static final Logger			logger				= LoggerFactory.getLogger(ExtendedMultiReference.class);

	/**
	 * The condition to show.
	 */
	protected boolean					show				= true;

	/**
	 * The reference to entity. By default, null.
	 */
	protected String					entity				= null;

	/**
	 * The reference to code. By default, null.
	 */
	protected String					code				= null;

	/**
	 * The reference to parentkey. By default, null.
	 */
	protected String					parentKey			= null;

	/**
	 * The reference to table window. By default, null.
	 */
	protected JDialog					tableWindow			= null;

	/**
	 * The reference to ok button. By default, null.
	 */
	protected JButton					okButton			= null;

	/**
	 * The reference to table. By default, null.
	 */
	protected Table						t					= null;

	/**
	 * The reference to locator. By default, null.
	 */
	protected EntityReferenceLocator	locator				= null;

	/**
	 * The reference to alignment. By default, north.
	 */
	protected int						alignment			= GridBagConstraints.NORTH;

	/**
	 * The reference to window title. By default, null.
	 */
	protected String					windowTitle			= null;

	/**
	 * The reference to text. By default, null.
	 */
	protected String					text				= null;

	/**
	 * The reference to query button. By default, null.
	 */
	protected JButton					queryButton			= new JButton();

	/**
	 * The reference to delete button. By default, null.
	 */
	protected JButton					deleteButton		= new JButton();

	protected List<Object>				fieldAttributes		= new ArrayList<>();

	/**
	 * The reference to asked attributes. By default, null.
	 */
	protected List<Object>				askedAttributes		= null;

	/**
	 * The reference to attributes. By default, null.
	 */
	protected String					attribute			= null;

	/**
	 * The reference to tip. By default, null.
	 */
	protected String					tip					= null;

	/**
	 * The reference to icon. By default, null.
	 */
	protected String					icon				= null;

	/**
	 * The reference to default icon path.
	 */
	protected String					defaultIcon			= ImageManager.MULTIREFQUERY;

	/**
	 * The reference to parent form. By default, null.
	 */
	protected Form						parentForm			= null;

	/**
	 * The reference to parent frame. By default, null.
	 */
	protected Frame						parentFrame			= null;

	/**
	 * The reference to bundle resources. By default, null.
	 */
	protected ResourceBundle			resources			= null;

	/**
	 * The reference to visible permission. By default, null.
	 */
	protected FormPermission			visiblePermission	= null;

	/**
	 * The reference to enabled permission. By default, null.
	 */
	protected FormPermission			enabledPermission	= null;

	/**
	 * An instance of a focus listener.
	 */
	protected FocusAdapter				focusListener		= new FocusAdapter() {

		@Override
		public void focusLost(FocusEvent e) {
			// When it loses the focus and key component is not empty
			// then make a query.
			DataComponent c = ExtendedMultiReference.this.parentForm
					.getDataFieldReference(ExtendedMultiReference.this.code);
			if (c != null) {
				if (!c.isEmpty()) {
					Object oKeyValue = c.getValue();
					ExtendedMultiReference.this.fillDataFields(ExtendedMultiReference.this.queryEntity(oKeyValue));
				} else {
					c.deleteData();
				}
			}
		}
	};

	/**
	 * The class constructor. Calls to <code>super()</code> and inits parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters
	 */
	public ExtendedMultiReference(Map<Object, Object> parameters) {
		super();
		this.init(parameters);
	}

	/**
	 * Inits parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>attr</td> <td></td> <td></td> <td>yes</td> <td>The field attribute.</td> </tr> <tr>
	 *            <td>align</td> <td><i>right/left</td> <td>north</td> <td>no</td> <td>The alignment for component.</td> </tr> <tr> <td>entity</td> <td></td> <td></td> <td>yes</td>
	 *            <td>Associated entity.</td> </tr> <tr> <td>cod</td> <td></td> <td></td> <td>no</td> <td>The code name associated to field.</td> </tr> <tr> <td>attrs</td>
	 *            <td><i>attr1;attr2;...;attrn</td> <td>cols</td> <td>no</td> <td>Attributes to fill after query operation.</td> </tr> <tr> <td>icon</td> <td></td> <td></td>
	 *            <td>no</td> <td>Icon for query button.</td> </tr> <tr> <td>text</td> <td></td> <td></td> <td>no</td> <td>The text for query button.</td> </tr> <tr> <td>tip</td>
	 *            <td></td> <td></td> <td>no</td> <td>The tip for query button.</td> </tr> <tr> <td>descriptioncols</td> <td><i>dcol1;dcol2;...;dcoln</td> <td>yes</td> <td>no</td>
	 *            <td>Visible table columns in text field. By default, it is the parameter value of visiblecols in table.</td> </tr> </Table> <p> Valid Table parameters: <p> <Table
	 *            BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr> <tr> <td>key</td> <td></td> <td><i>cod</td> <td>no</td> <td>The Table key.</td> </tr> <tr> <td>parentkey <i>or</i> parentkeys</td>
	 *            <td><i>pk1;pk2;...;pkn</td> <td></td> <td>yes</td> <td>The parentkey or parentkeys to filter.</td> </tr> <tr> <td>cols</td> <td><i>cols1;cols2;...;colsn</td>
	 *            <td></td> <td>yes</td> <td>Columns associated to the code. It forms the description field.</td> </tr> <tr> <td>form</td> <td><i></td> <td></td> <td>no</td>
	 *            <td>The form that is opened in detail. On init, update mode will be its state.</td> </tr> <tr> <td>controls</td> <td><i>yes/no</td> <td>yes</td> <td>no</td>
	 *            <td>Allows the Table to have some control buttons, in the top of it. This buttons, by default, can export to excel the data in the table, show charts, reports,
	 *            and so on. If the value is not, the controlsvisible attribute will not affect.</td> </tr> <tr> <td>otherkeys</td> <td><i>ok1;ok2;...;okn</td> <td></td>
	 *            <td>no</td> <td>The other keys.</td> </tr> </Table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		// parameter: Attribute.
		Object atrib = parameters.get("attr");
		if (atrib == null) {
			if (ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(this.getClass().toString() + ": Parameter 'attr' not found");
			}
		} else {
			this.attribute = atrib.toString();
		}

		Object ti = parameters.get("tip");
		if (ti != null) {
			this.tip = ti.toString();
		}

		// parameter: align
		Object align = parameters.get("align");
		if (align == null) {
		} else {
			if (align.equals("right")) {
				this.alignment = GridBagConstraints.NORTHEAST;
			} else {
				if (align.equals("left")) {
					this.alignment = GridBagConstraints.NORTHWEST;
				} else {
					this.alignment = GridBagConstraints.NORTH;
				}
			}
		}

		Object icon = parameters.get("icon");
		if (icon != null) {
			this.icon = icon.toString();
		}

		Object text = parameters.get("text");
		if (text != null) {
			this.text = text.toString();
		}

		Object entity = parameters.get("entity");
		if (entity == null) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug("Parameter 'entity' not found in ExtendedMultiReference. Check parameters.");
			}
		} else {
			this.entity = entity.toString();
		}

		// Parameter cod : The name of the cod column
		Object cod = parameters.get("cod");
		if (cod == null) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(this.getClass().toString() + ": " + "Error: 'cod' parameter not found in ExtededMultiReference. Check parameters");
			}
		} else {
			this.code = cod.toString();
		}

		Object title = parameters.get("title");
		if (title != null) {
			this.windowTitle = title.toString();
		} else {
			this.windowTitle = this.entity;
		}
		// Parameter : parentkey
		Object parentkey = parameters.get("parentkey");
		if (parentkey != null) {
			this.parentKey = parentkey.toString();
		} else {
			this.parentKey = null;
		}

		// Parameter key
		Object key = parameters.get("key");
		if (key == null) {
			parameters.put("key", this.code);
		}
		parameters.put("numrowscolumn", "no");
		parameters.put("rows", "15");
		// Create the table
		try {
			this.t = new Table(parameters);
			this.t.enableFiltering(false);
			this.t.getJTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.t.getJTable().getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					if (ExtendedMultiReference.this.t.getJTable().getSelectedRowCount() > 0) {
						if (ExtendedMultiReference.this.okButton != null) {
							ExtendedMultiReference.this.okButton.setEnabled(true);
						}
					} else {
						if (ExtendedMultiReference.this.okButton != null) {
							ExtendedMultiReference.this.okButton.setEnabled(false);
						}
					}
				}
			});
		} catch (Exception e) {
			ExtendedMultiReference.logger.error(this.getClass().toString() + ": The table has not been created." + e.getMessage(), e);
		}

		Object attrs = parameters.get("attrs");
		if (attrs == null) {
			if (ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(this.getClass().toString() + ": Parameter 'attrs' not found. Using cols");
			}
			this.fieldAttributes = this.t.getAttributeList();
		} else {
			StringTokenizer st = new StringTokenizer(attrs.toString(), ";");
			while (st.hasMoreTokens()) {
				this.fieldAttributes.add(st.nextToken());
			}
		}
		if (!this.fieldAttributes.contains(this.code)) {
			this.fieldAttributes.add(this.code);
		}

		this.queryButton.setMargin(new Insets(0, 0, 0, 0));
		ImageIcon queryIcon = null;
		if (this.icon != null) {
			queryIcon = ImageManager.getIcon(this.icon);
		} else {
			queryIcon = ImageManager.getIcon(this.defaultIcon);
		}
		if (queryIcon != null) {
			this.queryButton.setIcon(queryIcon);
		} else {
			this.queryButton.setText("?");
		}
		if (this.text != null) {
			this.queryButton.setText(this.text);
		}
		if (this.tip != null) {
			this.queryButton.setToolTipText(this.tip);
		}
		this.queryButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Cursor cursor = ExtendedMultiReference.this.getCursor();
				if (ExtendedMultiReference.this.parentKey != null) {
					if (ExtendedMultiReference.this.parentForm.isEmpty(ExtendedMultiReference.this.parentKey)) {
						ExtendedMultiReference.this.parentForm.message("M_NECESSARY_" + ExtendedMultiReference.this.parentKey, Form.ERROR_MESSAGE);
						return;
					}
				}
				TopWindow w = new TopWindow(ExtendedMultiReference.this.parentFrame, "performing_query",
						ExtendedMultiReference.this.resources,
						ImageManager.getIcon(ImageManager.SEARCHING), null);
				try {
					// Show a message dialog
					w.show(true);
					ExtendedMultiReference.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					if (ExtendedMultiReference.this.parentForm != null) {
						ExtendedMultiReference.this.parentForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					}
					Map<Object, Object> hKeysValues = new HashMap<>();
					if (ExtendedMultiReference.this.parentKey != null) {
						hKeysValues.put(ExtendedMultiReference.this.parentKey, ExtendedMultiReference.this.parentForm.getDataFieldValue(ExtendedMultiReference.this.parentKey));
					}
					// If the managed fields have some value use them as filter
					for (Object element : ExtendedMultiReference.this.fieldAttributes) {
						DataComponent c = ExtendedMultiReference.this.parentForm.getDataFieldReference(element.toString());
						if ((c != null) && (!c.isEmpty())) {
							hKeysValues.put(element, c.getValue());
						}
					}

					EntityResult result = ExtendedMultiReference.this.locator.getEntityReference(ExtendedMultiReference.this.entity).query(hKeysValues,
							ExtendedMultiReference.this.t.getAttributeList(), ExtendedMultiReference.this.locator.getSessionId());

					if (result.getCode() == EntityResult.OPERATION_WRONG) {
						if (ExtendedMultiReference.this.parentForm != null) {
							ExtendedMultiReference.this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
						}
						return;
					}
					// Now show the window and set the table value
					ExtendedMultiReference.this.t.setValue(result);
					if (ExtendedMultiReference.this.tableWindow == null) {
						ExtendedMultiReference.this.tableWindow = new JDialog(ExtendedMultiReference.this.parentFrame, true) {

							@Override
							protected void processKeyEvent(KeyEvent event) {
								if ((event.getKeyCode() == KeyEvent.VK_ESCAPE) && (e.getID() == KeyEvent.KEY_PRESSED)) {
									event.consume();
									this.setVisible(false);
									return;
								}
								super.processKeyEvent(event);
							}
						};

						try {
							if ((ExtendedMultiReference.this.windowTitle != null) && (ExtendedMultiReference.this.resources != null)) {
								ExtendedMultiReference.this.tableWindow.setTitle(ExtendedMultiReference.this.resources.getString(ExtendedMultiReference.this.windowTitle));
							} else if (ExtendedMultiReference.this.windowTitle != null) {
								ExtendedMultiReference.this.tableWindow.setTitle(ExtendedMultiReference.this.windowTitle);
							}
						} catch (Exception ex) {
							if (com.ontimize.gui.ApplicationManager.DEBUG) {
								ExtendedMultiReference.logger.debug(this.getClass().toString() + ": " + ex.getMessage(), ex);
							}
						}

						JPanel southPanel = new JPanel();
						ExtendedMultiReference.this.okButton = new JButton("datafield.select");
						ExtendedMultiReference.this.okButton.setEnabled(false);

						ExtendedMultiReference.this.okButton.setIcon(ImageManager.getIcon(ImageManager.OK));
						ExtendedMultiReference.this.okButton.setEnabled(false);
						southPanel.add(ExtendedMultiReference.this.okButton);
						ExtendedMultiReference.this.tableWindow.getContentPane().add(southPanel, BorderLayout.SOUTH);
						ExtendedMultiReference.this.tableWindow.getContentPane().add(ExtendedMultiReference.this.t);
						ExtendedMultiReference.this.okButton.addActionListener(e1 -> {
							if (ExtendedMultiReference.this.t.getSelectedRow() >= 0) {
								Map<?, ?> hFieldValue = ExtendedMultiReference.this.t.getRowData(ExtendedMultiReference.this.t.getSelectedRow());
								// Code value
								Object oValue = hFieldValue.get(ExtendedMultiReference.this.code);
								ExtendedMultiReference.this.fillDataFields(ExtendedMultiReference.this.queryEntity(oValue));
								ExtendedMultiReference.this.tableWindow.setVisible(false);
							}
						});
						ExtendedMultiReference.this.t.packTable();
						Object oValue = ExtendedMultiReference.this.parentForm.getDataFieldValue(ExtendedMultiReference.this.code);
						if (oValue != null) {
							int ind = ExtendedMultiReference.this.t.getPrimaryKeys().indexOf(oValue);
							if (ind >= 0) {
								ExtendedMultiReference.this.t.setSelectedRow(ind);
							}
						}
						ExtendedMultiReference.this.tableWindow.pack();
						w.hide();
						// Minimun size
						if (ExtendedMultiReference.this.tableWindow.getSize().getWidth() < 300) {
							ExtendedMultiReference.this.tableWindow.setSize(300, (int) ExtendedMultiReference.this.tableWindow.getSize().getHeight());
						}
						ExtendedMultiReference.this.tableWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
						ApplicationManager.center(ExtendedMultiReference.this.tableWindow);
						ExtendedMultiReference.this.tableWindow.setVisible(true);
					} else {
						w.hide();
						Object oValue = ExtendedMultiReference.this.parentForm.getDataFieldValue(ExtendedMultiReference.this.code);
						if (oValue != null) {
							int ind = ExtendedMultiReference.this.t.getPrimaryKeys().indexOf(oValue);
							if (ind >= 0) {
								ExtendedMultiReference.this.t.setSelectedRow(ind);
							}
						}
						ExtendedMultiReference.this.tableWindow.setVisible(true);
					}
				} catch (Exception ex) {
					ExtendedMultiReference.logger.error(null, ex);
					ExtendedMultiReference.this.parentForm.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
				} finally {
					if (w != null) {
						w.hide();
					}
					if (ExtendedMultiReference.this.parentForm != null) {
						ExtendedMultiReference.this.parentForm.setCursor(cursor);
					}
					ExtendedMultiReference.this.setCursor(cursor);
				}
			}
		});

		this.setLayout(new GridBagLayout());
		this.add(this.queryButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	/**
	 * Sets the value to component. <p>
	 *
	 * @param attribute
	 *            the attribute to set
	 * @param value
	 *            the value to set to the attribute
	 */
	protected void setComponentValue(Object attribute, Object value) {
		this.parentForm.setDataFieldValue(attribute, value);
		if (ApplicationManager.DEBUG) {
			ExtendedMultiReference.logger.debug(this.getClass().toString() + ": Setted field value " + attribute + " to " + value);
		}
	}

	/**
	 * The complementary show condition. <p>
	 *
	 * @return the boolean condition
	 */
	public boolean isHidden() {
		return !this.show;
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator locator) {
		this.locator = locator;
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
		this.t.setParentFrame(parentFrame);
	}

	@Override
	public void setParentForm(Form f) {
		this.parentForm = f;
		this.t.setParentForm(this.parentForm);
	}

	/**
	 * It is equals to insert a code in field. <p>
	 *
	 * @param codeValue
	 *            the code to set
	 */
	public void setCode(Object codeValue) {
		DataComponent c = this.parentForm.getDataFieldReference(this.code);
		c.setValue(codeValue);
		if (!c.isEmpty()) {
			Object oKeyValue = c.getValue();
			this.fillDataFields(this.queryEntity(oKeyValue));
		} else {
			c.deleteData();
		}
	}

	/**
	 * Fills the data fields with <code>Map</code> parameter. <p>
	 *
	 * @param result
	 *            the <code>EntituResult</code> with data.
	 */
	protected void fillDataFields(EntityResult result) {
		if (result == null) {
			return;
		}
		if (result instanceof EntityResult) {
			if (result.getCode() == EntityResult.OPERATION_WRONG) {
				if (this.parentForm != null) {
					this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
				}
				return;
			}
		}

		// Multiple results are not supported.
		Enumeration<?> enumKeys = result.keys();
		while (enumKeys.hasMoreElements()) {
			Object oKey = enumKeys.nextElement();
			Object oValues = result.get(oKey);
			Object oValue = oValues;
			if (oValues instanceof List) {
				oValue = ((List<?>) oValues).get(0);
			}
			if (this.askedAttributes.contains(oKey)) {
				this.setComponentValue(oKey, oValue);
				if (oKey instanceof ReferenceFieldAttribute) {
					this.parentForm.disableDataField(((ReferenceFieldAttribute) oKey).getAttr());
				} else {
					this.parentForm.disableDataField(oKey.toString());
				}
			}
		}
	}

	/**
	 * Creates a query by code for the defined entity. <p>
	 *
	 * @param codeValue
	 *            the code value
	 * @return the result
	 */
	protected EntityResult queryEntity(Object codeValue) {
		if (this.askedAttributes == null) {
			this.askedAttributes = this.createAskedAttributesList();
		}
		Cursor cursor = this.getCursor();
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			if (this.parentForm != null) {
				this.parentForm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
			Map<Object, Object> keysValues = new HashMap<>();
			keysValues.put(this.code, codeValue);
			if (this.parentKey != null) {
				keysValues.put(this.parentKey, this.parentForm.getDataFieldValue(this.parentKey));
			}
			EntityResult result = this.locator.getEntityReference(ExtendedMultiReference.this.entity).query(keysValues, this.askedAttributes, this.locator.getSessionId());
			return result;
		} catch (Exception ex) {
			ExtendedMultiReference.logger.error(null, ex);
			this.parentForm.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
			return null;
		} finally {
			if (this.parentForm != null) {
				this.parentForm.setCursor(cursor);
			}
			this.setCursor(cursor);
		}
	}

	private List<Object> createAskedAttributesList() {
		List<Object> attrib = new ArrayList<>();
		for (int i = 0; i < this.fieldAttributes.size(); i++) {
			Object attr = this.fieldAttributes.get(i);
			DataComponent c = this.parentForm.getDataFieldReference(attr.toString());
			if (c != null) {
				attrib.add(c.getAttribute());
			}
		}
		return attrib;
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		this.resources = resources;
		try {
			if ((resources != null) && (this.tip != null)) {
				this.queryButton.setToolTipText(resources.getString(this.tip));
			}
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(e.getMessage(), e);
			}
		}
		try {
			if ((resources != null) && (this.text != null)) {
				this.queryButton.setText(resources.getString(this.text));
			} else if (this.text != null) {
				this.queryButton.setText(this.text);
			}
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(e.getMessage(), e);
			}
		}
		try {
			if (this.tableWindow != null) {
				if ((this.windowTitle != null) && (resources != null)) {
					this.tableWindow.setTitle(resources.getString(this.windowTitle));
				} else if (this.windowTitle != null) {
					this.tableWindow.setTitle(this.windowTitle);
				}
			}
		} catch (Exception e) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				ExtendedMultiReference.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
			}
		}
		if (this.t != null) {
			this.t.setResourceBundle(resources);
		}
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(0, 0, 1, 1, 0.01, 0, this.alignment, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		if (this.tip != null) {
			v.add(this.tip);
		}
		return v;
	}

	@Override
	public void setComponentLocale(Locale l) {
		if (this.t != null) {
			this.t.setComponentLocale(l);
		}
	}

	@Override
	public void setFormBuilder(FormBuilder builder) {
		this.t.setFormBuilder(builder);
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
		this.queryButton.setVisible(enabled);
		this.setVisible(enabled);
		this.queryButton.setEnabled(enabled);
	}

	@Override
	public void setVisible(boolean vis) {
		if (vis) {
			boolean permission = this.checkVisiblePermission();
			if (!permission) {
				return;
			}
		}
		super.setVisible(vis);
	}

	@Override
	public void initPermissions() {
		if (ApplicationManager.getClientSecurityManager() != null) {
			ClientSecurityManager.registerSecuredElement(this);
		}
		boolean pVisible = this.checkVisiblePermission();
		if (!pVisible) {
			this.setVisible(false);
		}

		boolean pEnabled = this.checkEnabledPermission();
		if (!pEnabled) {
			this.setEnabled(false);
		}

	}

	/**
	 * Checks visible permission. <p>
	 *
	 * @return the visibility condition
	 */
	protected boolean checkVisiblePermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.visiblePermission == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.visiblePermission = new FormPermission(this.parentForm.getArchiveName(), "visible", this.attribute.toString(), true);
				}
			}
			try {
				// Check to show
				if (this.visiblePermission != null) {
					manager.checkPermission(this.visiblePermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					ExtendedMultiReference.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					ExtendedMultiReference.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Checks enabled permission. <p>
	 *
	 * @return the enabled condition
	 */
	protected boolean checkEnabledPermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.enabledPermission == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.enabledPermission = new FormPermission(this.parentForm.getArchiveName(), "enabled", this.attribute.toString(), true);
				}
			}
			try {
				// Check to show
				if (this.enabledPermission != null) {
					manager.checkPermission(this.enabledPermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					ExtendedMultiReference.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					ExtendedMultiReference.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * The restricted condition. By default, false.
	 */
	protected boolean restricted = false;

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
