package com.ontimize.gui.field;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Types;
import java.text.Format;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.FormBuilder;
import com.ontimize.cache.CacheManager;
import com.ontimize.cache.CachedComponent;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.CreateForms;
import com.ontimize.gui.Form;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.ValueChangeListener;
import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.field.TextFieldDataField.EJTextField;
import com.ontimize.gui.field.document.MaskDocument;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.ConnectionManager;
import com.ontimize.jee.common.gui.MultipleValue;
import com.ontimize.jee.common.gui.field.MultipleReferenceDataFieldAttribute;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/**
 * Abstract class that implements a Multiple Reference Data Field. <p>
 *
 * @author Imatia Innovation
 */

public abstract class AbstractMultipleReferenceDataField extends DataField implements DataComponent, ReferenceComponent, OpenDialog, CreateForms, CachedComponent {

	private static final Logger			logger					= LoggerFactory.getLogger(AbstractMultipleReferenceDataField.class);

	/** Cods property. */
	public static final String			CODS					= "cods";

	/** Typecods property. */
	public static final String			TYPECODS				= "typecods";

	/** Typecods property. */
	public static final String			ONSETVALUESET			= "onsetvalueset";

	/** Parentcods property. */
	public static final String			PARENT_CODS				= "parentcods";

	/** Visiblecods property. */
	public static final String			VISIBLECODS				= "visiblecods";

	/** Keys property. */
	public static final String			KEYS					= "keys";

	/** Parentkeys property. */
	public static final String			PARENT_KEYS				= "parentkeys";

	/** Cols property. */
	public static final String			COLS					= "cols";

	/** Entity property */
	public static final String			ENTITY					= "entity";

	/** Cachetime property. */
	public static final String			CACHETIME				= "cachetime";

	public static final String			PARENTKEYCACHE			= "parentkeycache";

	public static final String			MULTILANGUAGE			= "multilanguage";

	protected boolean					parentkeyCache			= CacheManager.defaultParentKeyCache;

	/**
	 * The Entity reference Locator that provides a locator to this entity
	 */
	protected EntityReferenceLocator	locator					= null;

	private Frame						parentFrame				= null;

	/**
	 * The vector with attributes to update when data field value changed. By default, null.
	 */
	protected List<String>				onsetvaluesetAttributes	= null;

	/**
	 * An ArrayList where will be inserted the data field names of codes
	 */
	protected List<String>				cods					= null;

	/**
	 * An ArrayList where the possible types of codes will be inserted. If no other specification(String,Float,Double) exists, the type will be Integer by default. The order must
	 * be the same that cods have in <code>Arraylist cods</codes>.
	 */
	protected List<Object>				typecods				= null;

	/**
	 * An ArrayList where the visible column codes will be inserted . If it is empty all codes will be hidden and by default all codes will be visible.
	 */
	protected List<Object>				visibleCods;

	/**
	 * An map to put the visible key-component pairs.
	 */
	protected Map<Object, Object>		jVisibleCods;

	/**
	 * An map to put the visible parameters-position in multiple data field
	 */
	protected Map<Object, Object>		visiblesize;

	/**
	 * The reference to parent cods. By default, null.
	 */
	protected List<Object>				parentCods				= null;

	/**
	 * The reference to keys. By default, null.
	 */
	protected List<String>				keys					= null;

	/**
	 * The reference to parent keys. By default, null.
	 */
	protected List<Object>				parentkeys				= null;

	protected List<Object>				cols					= null;

	/**
	 * A separator reference. By default, ' '.
	 */
	protected String					separator				= " ";

	/**
	 * A reference to a value object. By default, null.
	 */
	protected Object					value					= null;

	protected String					entity;

	/**
	 * Defines the cache time. By default, the {@link Integer#MAX_VALUE}
	 */
	protected int						cacheTime				= Integer.MAX_VALUE;

	/**
	 * The last cache time. By default, zero to query always the first time
	 */
	protected long						lastCacheTime			= 0;

	/**
	 * The condition about data cache initialization. By default, false.
	 */
	protected boolean					dataCacheInitialized	= false;

	/**
	 * A data cache.
	 */
	protected EntityResult				dataCache				= new EntityResultMapImpl();

	/**
	 * The condition to initialize cache on setValue. By default, false.
	 */
	protected boolean					initCacheOnSetValue		= false;

	/**
	 * The condition to use cache manager. By default, true.
	 */
	protected boolean					useCacheManager			= true;

	/**
	 * A reference to cache manager. By default, null.
	 */
	protected CacheManager				cacheManager			= null;

	/**
	 * The format column.
	 */
	protected Map<Object, Object>					formatColumn			= new HashMap<>();

	/**
	 * The condition to disable events. By default, false.
	 */
	protected boolean					valueEventDisabled		= false;

	/**
	 * The condition to disable cache
	 */
	protected boolean					multilanguage			= false;

	/**
	 * The main class to create the EJTextField <p>
	 *
	 * @author Imatia Innovation
	 */
	protected class CEJTextField extends EJTextField {

		/**
		 * The class constructor. Calls to EJTextField <code>constructor</code> with four columns.
		 */
		public CEJTextField() {
			super(4);
		}

		/**
		 * The class constructor to create a EJTextField with a specified number of columns. <p>
		 * 
		 * @param col
		 *            the number of columns
		 */
		public CEJTextField(int col) {
			super(col);
		}

		@Override
		public void setText(String text) {
			Document d = this.getDocument();
			if (d instanceof MaskDocument) {
				try {
					((MaskDocument) d).setValue(text, true);
				} catch (Exception e) {
					AbstractMultipleReferenceDataField.logger.trace(null, e);
					super.setText(text);
				}
			} else {
				super.setText(text);
			}
		}

	};

	/**
	 * Creates component. Empty method.
	 */
	protected void createComponent() {

	}

	/**
	 * Creates a code component. Uses {@link #visibleCods},{@link #visiblesize} and adds listeners.
	 */
	protected void createCodeComponents() {
		this.jVisibleCods = new HashMap<>();
		for (int i = 0; i < this.visibleCods.size(); i++) {
			Object oKey = this.visibleCods.get(i);
			Object oWidth = this.visiblesize.get(oKey);
			JComponent comp = null;
			if ((oWidth != null) && (oWidth instanceof Integer)) {
				comp = new CEJTextField(((Integer) oWidth).intValue());
			} else {
				comp = new CEJTextField();
			}

			comp.addKeyListener(this.codListener);
			comp.addFocusListener(this.codListener);
			((CEJTextField) comp).getDocument().addDocumentListener(this.codListener);

			this.jVisibleCods.put(oKey, comp);
			super.panel.add(comp, new GridBagConstraints(i, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(DataField.DEFAULT_TOP_MARGIN, DataField.DEFAULT_FIELD_LEFT_MARGIN, DataField.DEFAULT_BOTTOM_MARGIN, 0), 0, 0));
		}
	}

	/**
	 * Initializes parameters and throws {@link IllegalArgumentException} when required parameters are not present. <p>
	 * 
	 * @param parameters
	 *            the <code>Map</code> with parameters
	 *
	 *            <p>
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>attr</td> <td></td> <td></td> <td>yes</td> <td>An {@link AbstractMultipleReferenceDataField} object. It is used to get a reference for field</td> </tr>
	 *
	 *
	 *            <tr> <td>keys</td> <td><i>key1;key2;...;keyn</td> <td></td> <td>yes</td> <td>This attribute contains columns that are keys in entity of current form.</td> </tr>
	 *
	 *            <tr> <td>cods</td> <td><i>cod1;cod2;...;codn</td> <td>keys</td> <td>yes</td> <td>This parameter refers to the keys of entity that is queried. We need keys and
	 *            cods because, keys in entity and form accept different names.</td> </tr>
	 *
	 *            <tr> <td>visiblecods</td> <td><i>vcod1;vcod2;...;vcodn</td> <td></td> <td>no</td> <td>The number of boxes showed on the left of description field. Each visible
	 *            cods is used to select an individual cod.</td> </tr>
	 *
	 *            <tr> <td>typecods</td> <td><i>vtcod1;vtcod2;...;vtcodn</td> <td>Integer</td> <td>no</td> <td>The class type of cods. To indicate other types for cods, they should
	 *            be ordered like <CODE>cods<CODE></td> </tr>
	 *
	 *            <tr> <td>parentkeys</td> <td><i>pk1;pk2;...;pkn</td> <td>parentcods</td> <td>no</td> <td>Attribute used to filter MultipleReferenceDataField. It will contain all
	 *            attributes whose values will be extracted of current form to filter the field.</td> </tr>
	 *
	 *            <tr> <td>parentcods</td> <td><i>pcod1;pcod2;...;pcodn</td> <td>parentkeys</td> <td>no</td> <td>This parameter refers to the parentkeys of entity that is queried.
	 *            Sometimes, we need parentkeys and parentcods because columns in entity and form accept different names. This parameter must be ordered in same manner that
	 *            parentkeys, to establish the correspondence position by position.</td> </tr>
	 *
	 *            <tr> <td>entity</td> <td></td> <td></td> <td>yes</td> <td>The entity to obtain the data</td> </tr>
	 *
	 *
	 *            <tr> <td>cols</td> <td><i>col1;col2;...;coln</td> <td></td> <td>yes</td> <td>The cols to show both in description of field and in table to select records.</td>
	 *            </tr>
	 *
	 *            <tr> <td>onsetvalueset</td> <td><i></td> <td></td> <td></td> <td>Field attributes whose value will be set when field data change.</td> </tr>
	 *
	 *            <tr> <td>multilanguage</td> <td><i>yes/no</i></td> <td>no</td> <td>no</td> <td>If <i>yes</i>, invalidate the cache data.</td> </tr>
	 *
	 *            </Table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		super.init(parameters);
		if (this.attribute == null) {
			throw new IllegalArgumentException(this.getClass().getName() + ": attr parameter not found");
		}
		this.setKeysParameter(parameters);
		this.setOnSetValueSetParameter(parameters);
		this.setCodsAndTypeCodsParameter(parameters);
		this.visibleCods = new ArrayList<>();
		this.visiblesize = new HashMap<>();
		this.setVisibleCodsParameter(parameters);
		this.setColsParameter(parameters);
		this.setParentKeysParameter(parameters);
		this.setParentCodsParameter(parameters);
		this.setEntityParameter(parameters);
		this.setCodsParameter(parameters);
		this.setParentKeyCacheParameter(parameters);
		this.setMultilanguageParameter(parameters);
		this.attribute = new MultipleReferenceDataFieldAttribute(this.attribute.toString(), this.entity.toString(), this.cods, this.typecods, this.keys, this.cols, this.parentCods,
				this.parentkeys);
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setMultilanguageParameter(Map<Object, Object> parameters) {
		Object oMultilanguage = parameters.get(AbstractMultipleReferenceDataField.MULTILANGUAGE);
		if (oMultilanguage != null) {
			if (oMultilanguage.toString().equalsIgnoreCase("yes")) {
				this.multilanguage = true;
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setParentKeyCacheParameter(Map<Object, Object> parameters) {
		Object parentkeycache = parameters.get(AbstractMultipleReferenceDataField.PARENTKEYCACHE);
		if ((parentkeycache != null) && parentkeycache.equals("yes")) {
			if (this.hasParentKeys()) {
				this.parentkeyCache = true;
				this.useCacheManager = true;
			} else {
				AbstractMultipleReferenceDataField.logger.debug("WARNING: 'parentkeycache' parameter will not be established if the parentkey isn't defined!");
			}
		} else {
			if (this.hasParentKeys()) {
				this.useCacheManager = false;
				this.cacheTime = 0;
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setCodsParameter(Map<Object, Object> parameters) {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.CODS)) {
			this.cods = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.CODS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.cods.add(st.nextToken());
			}
		} else {
			this.cods = new ArrayList<>(this.keys);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 * @throws IllegalArgumentException
	 */
	protected void setEntityParameter(Map<Object, Object> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.ENTITY)) {
			this.entity = parameters.get(AbstractMultipleReferenceDataField.ENTITY).toString();
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + ": 'entity' parameter not found");
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setParentCodsParameter(Map<Object, Object> parameters) {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.PARENT_CODS)) {
			this.parentCods = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.PARENT_CODS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.parentCods.add(st.nextToken());
			}
		} else if (this.parentkeys != null) {
			this.parentCods = new ArrayList<>(this.parentkeys);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setParentKeysParameter(Map<Object, Object> parameters) {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.PARENT_KEYS)) {
			this.parentkeys = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.PARENT_KEYS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.parentkeys.add(st.nextToken());
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 * @throws IllegalArgumentException
	 */
	protected void setColsParameter(Map<Object, Object> parameters) throws IllegalArgumentException {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.COLS)) {
			this.cols = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.COLS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.cols.add(st.nextToken());
			}
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + ": 'cols' paramter not found");
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setVisibleCodsParameter(Map<Object, Object> parameters) {
		if (parameters.containsKey(AbstractMultipleReferenceDataField.VISIBLECODS)) {
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.VISIBLECODS).toString(), ";");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int pos = token.indexOf(":");
				if (pos != -1) {
					this.visibleCods.add(token.substring(0, pos));
					try {
						Integer integer = new Integer(token.substring(pos + 1));
						this.visiblesize.put(token.substring(0, pos), integer);
					} catch (Exception e) {
						AbstractMultipleReferenceDataField.logger.trace(null, e);
					}
				} else {
					this.visibleCods.add(token);
				}
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setCodsAndTypeCodsParameter(Map<Object, Object> parameters) {
		this.typecods = new ArrayList<>();
		if (parameters.containsKey(AbstractMultipleReferenceDataField.TYPECODS)) {
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.TYPECODS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.typecods.add(this.getSQLType(st.nextToken()));
			}
		}

		if (this.cods != null) {
			if (this.typecods.size() != this.cods.size()) {
				this.typecods.clear();
				for (int i = 0; i < this.cods.size(); i++) {
					this.typecods.add(new Integer(java.sql.Types.INTEGER));
				}
			}
		} else {
			if (this.typecods.size() != this.keys.size()) {
				this.typecods.clear();
				for (int i = 0; i < this.keys.size(); i++) {
					this.typecods.add(new Integer(java.sql.Types.INTEGER));
				}
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 */
	protected void setOnSetValueSetParameter(Map<Object, Object> parameters) {
		Object onsetvalueset = parameters.get(AbstractMultipleReferenceDataField.ONSETVALUESET);
		if (onsetvalueset != null) {
			StringTokenizer st = new StringTokenizer(onsetvalueset.toString(), ";");
			this.onsetvaluesetAttributes = new ArrayList<>();
			while (st.hasMoreTokens()) {
				this.onsetvaluesetAttributes.add(st.nextToken());
			}
			if (!this.onsetvaluesetAttributes.isEmpty()) {
				this.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChanged(ValueEvent e) {
						if (AbstractMultipleReferenceDataField.this.isEmpty()) {
							if (AbstractMultipleReferenceDataField.this.parentForm != null) {
								for (int i = 0; i < AbstractMultipleReferenceDataField.this.onsetvaluesetAttributes.size(); i++) {
									AbstractMultipleReferenceDataField.this.parentForm
											.deleteDataField((String) AbstractMultipleReferenceDataField.this.onsetvaluesetAttributes.get(i));
									if (ApplicationManager.DEBUG) {
										AbstractMultipleReferenceDataField.logger
												.debug("Deleting field value: " + AbstractMultipleReferenceDataField.this.onsetvaluesetAttributes.get(i));
									}
								}
							}
						} else {
							Map<?, ?> h = AbstractMultipleReferenceDataField.this.getValuesToCode((MultipleValue) AbstractMultipleReferenceDataField.this.getValue());
							AbstractMultipleReferenceDataField.this.updateOnSetValueSetAttributes(h);
						}
					}
				});
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 * 
	 * @param parameters
	 * @throws IllegalArgumentException
	 */
	protected void setKeysParameter(Map<Object, Object> parameters) throws IllegalArgumentException {
		this.keys = new ArrayList<>();
		if (parameters.containsKey(AbstractMultipleReferenceDataField.KEYS)) {
			StringTokenizer st = new StringTokenizer(parameters.get(AbstractMultipleReferenceDataField.KEYS).toString(), ";");
			while (st.hasMoreTokens()) {
				this.keys.add(st.nextToken());
			}
		} else {
			throw new IllegalArgumentException(this.getClass().getName() + " Parameter 'keys' not found");
		}
	}

	/**
	 * Checks whether data are integer, string or float, in other case returns an integer. <p>
	 * 
	 * @param s
	 *            the data type
	 * @return the SQL type
	 */
	protected Integer getSQLType(String s) {
		if ("INTEGER".equalsIgnoreCase(s)) {
			return new Integer(java.sql.Types.INTEGER);
		}
		if ("STRING".equalsIgnoreCase(s)) {
			return new Integer(java.sql.Types.VARCHAR);
		}
		if ("DOUBLE".equalsIgnoreCase(s)) {
			return new Integer(java.sql.Types.DOUBLE);
		}
		if ("FLOAT".equalsIgnoreCase(s)) {
			return new Integer(java.sql.Types.FLOAT);
		}
		if ("SHORT".equalsIgnoreCase(s) || "SMALLINT".equalsIgnoreCase(s)) {
			return new Integer(java.sql.Types.SMALLINT);
		}
		return new Integer(java.sql.Types.INTEGER);
	}

	/**
	 * Parameters on this method are used to update the fields whose attributes are contained in {@link #onsetvaluesetAttributes}
	 * 
	 * @param data
	 *            Values to update
	 */
	protected void updateOnSetValueSetAttributes(Map<?, ?> data) {
		if ((this.parentForm != null) && (data != null)) {
			for (int i = 0; i < this.onsetvaluesetAttributes.size(); i++) {
				Object at = this.onsetvaluesetAttributes.get(i);
				Object oValue = data.get(at);
				this.parentForm.setDataFieldValue(at, oValue);
				if (ApplicationManager.DEBUG) {
					AbstractMultipleReferenceDataField.logger.debug("Setting field value: " + at + " -> " + oValue);
				}
			}
		}
	}

	/**
	 * Returns the associated values for a code to set in onsetvalueset attributes. <p>
	 * 
	 * @param code
	 *            the object where code are specified
	 * @return the key-value pairs
	 */
	public Map<?, ?> getValuesToCode(MultipleValue code) {
		Map<?, ?> h = new HashMap<>();
		List<Object> vCodeKeys = new ArrayList<>();
		Enumeration<?> eCode = code.keys();
		while (eCode.hasMoreElements()) {
			vCodeKeys.add(eCode.nextElement());
		}
		for (int i = 0; i < ((EntityResult) this.dataCache).calculateRecordNumber(); i++) {
			if (AbstractMultipleReferenceDataField.compareMultipleValue(new MultipleValue(((EntityResult) this.dataCache).getRecordValues(i)), code, vCodeKeys)) {
				h = ((EntityResult) this.dataCache).getRecordValues(i);
				break;
			}
		}
		return h;
	}

	/**
	 * Interface to implement inner listener methods. <p>
	 *
	 * @author Imatia Innovation
	 */
	protected interface InnerListener {

		/**
		 * Sets a inner listener. <p>
		 * 
		 * @param enabled
		 *            the condition to listener
		 */
		public void setInnerListenerEnabled(boolean enabled);

		/**
		 * Gets the inner value. <p>
		 * 
		 * @return the inner value
		 */
		public Object getInnerValue();

		/**
		 * Sets an inner value to object. <p>
		 * 
		 * @param o
		 *            the object to set inner
		 */
		public void setInnerValue(Object o);

	};

	/**
	 * A reference to inner Listener. By default, null.
	 */
	protected InnerListener		innerListener	= null;

	/**
	 * An instance of cod field listener.
	 */
	protected CodFieldListener	codListener		= new CodFieldListener();

	/**
	 * The main class to create a listener in a cod. <p>
	 *
	 * @author Imatia Innovation
	 */
	protected class CodFieldListener extends FocusAdapter implements DocumentListener, KeyListener {

		private boolean	keyChange	= false;

		private boolean	enabled		= true;

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.keyChange = true;
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.keyChange = true;
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.keyChange = true;
		}

		@Override
		public void focusLost(FocusEvent event) {
			if (!event.isTemporary()) {
				if (this.keyChange) {
					this.keyChange = false;
					this.processFocus();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				this.keyChange = false;
				e.consume();
				this.processFocus();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}

		/**
		 * Processes the focus.
		 */
		protected void processFocus() {
			// If some of the fields are empty then clear the field and return.
			Iterator<?> enu = AbstractMultipleReferenceDataField.this.jVisibleCods.keySet().iterator();
			while (enu.hasNext()) {
				Object k = enu.next();
				if (((CEJTextField) AbstractMultipleReferenceDataField.this.jVisibleCods.get(k)).getText().equals("")) {
					if (!AbstractMultipleReferenceDataField.this.isEmpty()) {
						AbstractMultipleReferenceDataField.this.deleteUserData(false);
					}
					this.keyChange = false;
					return;
				}
			}

			// If all fields are fill then creates the appropriate MultipleValue

			enu = AbstractMultipleReferenceDataField.this.jVisibleCods.keySet().iterator();
			Map<Object, Object> values = new HashMap<>();

			while (enu.hasNext()) {
				Object k = enu.next();
				String text = ((CEJTextField) AbstractMultipleReferenceDataField.this.jVisibleCods.get(k)).getText();
				Integer type = ((MultipleReferenceDataFieldAttribute) AbstractMultipleReferenceDataField.this.attribute).getTypeData(k);
				try {
					Object value = AbstractMultipleReferenceDataField.this.getCodData(type, text);
					values.put(k, value);
				} catch (Exception e) {
					AbstractMultipleReferenceDataField.logger.trace(null, e);
					AbstractMultipleReferenceDataField.this.deleteUserData(false);
					this.keyChange = false;
					return;
				}
			}
			AbstractMultipleReferenceDataField.this.setCode(values, ValueEvent.USER_CHANGE);
			this.keyChange = false;
		}

		/**
		 * Enables the field. <p>
		 * 
		 * @param en
		 *            the condition to set or no the field
		 */
		public void setEnabled(boolean en) {
			this.enabled = en;
		}

	}

	/**
	 * Gets the cod data to check type. <p>
	 * 
	 * @param type
	 *            the cod data type.
	 * @param value
	 *            the string to check cod data.
	 * @return the type in function of {@link Types}
	 * @throws Exception
	 *             when Exception occurs.
	 */
	protected Object getCodData(Integer type, String value) throws Exception {
		switch (type.intValue()) {
			case java.sql.Types.INTEGER:
				return new Integer(value.toString());
			case java.sql.Types.VARCHAR:
				return value;
			case java.sql.Types.DOUBLE:
				return new Double(value.toString());
			case java.sql.Types.FLOAT:
				return new Float(value.toString());
		}
		return value;
	}

	@Override
	public int getSQLDataType() {
		return java.sql.Types.OTHER;
	}

	protected long getLastCacheTime() {
		if (this.useCacheManager && (this.cacheManager != null) && this.cacheManager.existsCache(this.entity, this.getAttributes(), this.getParentKeyValues())) {
			return this.cacheManager.getLastCacheTime(this.entity, this.getParentKeyValues());
		} else {
			return this.lastCacheTime;
		}
	}

	@Override
	public Object getValue() {
		return this.value;
	}

	@Override
	public void setValue(Object originalValue) {
		this.setValue(originalValue, false);
	}

	/**
	 * Sets the value to object. Checks whether object is a multiple value. <p>
	 * 
	 * @param originalValue
	 *            the object to set value
	 * @param intern
	 *            the condition about intern listener state.
	 */
	public void setValue(Object originalValue, boolean intern) {
		if ((originalValue == null) || (originalValue instanceof NullValue)) {
			this.deleteData();
			return;
		}
		try {
			this.enableInnerListener(false);
			Object previousValue = this.getValue();
			if (originalValue instanceof Map) {
				this.setCode(originalValue, ValueEvent.PROGRAMMATIC_CHANGE);
			} else if (originalValue instanceof MultipleValue) {
				this.setFormatValue(originalValue);
				this.setFormatCods(originalValue);
				this.value = originalValue;
				this.setInnerValue(this.getValue());
				if (!intern) {
					this.valueSave = this.getInnerValue();
				}
				this.fireValueChanged(this.getInnerValue(), previousValue, ValueEvent.PROGRAMMATIC_CHANGE);
			} else {
				// If data type is not MultipleValue
				AbstractMultipleReferenceDataField.logger.debug("Wrong data type in the AbstractMultipleReferenceDataField");
			}
		} catch (Exception ex) {
			AbstractMultipleReferenceDataField.logger.error(null, ex);
			this.deleteData();
		} finally {
			this.enableInnerListener(true);
		}
	}

	protected abstract void setFormatValue(Object originalValue);

	/**
	 * Sets the format to visible cods. <p>
	 * 
	 * @param value
	 *            the object to set the cods.
	 */
	public void setFormatCods(Object value) {
		for (int i = 0; i < this.visibleCods.size(); i++) {
			Object oKey = this.visibleCods.get(i);
			JTextField textField = (JTextField) this.jVisibleCods.get(oKey);
			if (value == null) {
				textField.setText("");
			} else if (value instanceof MultipleValue) {
				String t = ((MultipleValue) value).get(oKey).toString();
				textField.setText(t);
			}
		}
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator referenceLocator) {
		this.locator = referenceLocator;

	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
	}

	public void free() {
		super.free();
	}

	@Override
	public void setFormBuilder(FormBuilder constructor) {}

	@Override
	public String getEntity() {
		return this.entity;
	}

	@Override
	public void setCacheManager(CacheManager c) {
		this.cacheManager = c;
	}

	@Override
	public boolean isEmpty() {
		if (this.getValue() != null) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the vector with all columns and keys. <p>
	 * 
	 * @return the vector with all columns and keys.
	 */
	@Override
	public List<Object> getAttributes() {
		List<Object> v = new ArrayList<>();
		v.addAll(this.cols);
		v.addAll(this.keys);
		return v;
	}

	/**
	 * Initializes cache. It uses the cachemanager when parentkeys are not present.
	 */
	public void initCache() {
		// If there are not parent keys, uses the cachemanager.
		if ((this.parentCods == null) || this.parentCods.isEmpty()) {
			if ((this.cacheTime != 0) && this.dataCacheInitialized) {
				return;
			}
			if ((this.cacheManager != null) && this.useCacheManager) {
				try {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					EntityResult res = this.cacheManager.getDataCache(this.entity, this.getAttributes(), this.getParentKeyValues());
					if (res.getCode() == EntityResult.OPERATION_WRONG) {
						if (this.parentForm != null) {
							this.parentForm.message(res.getMessage(), Form.ERROR_MESSAGE);
						}
						return;
					}

					this.dataCache = res;
					this.dataCacheInitialized = true;
					this.lastCacheTime = System.currentTimeMillis();
					return;
				} catch (Exception e) {
					if (ApplicationManager.DEBUG) {
						AbstractMultipleReferenceDataField.logger.debug("CacheManager cannot be used: " + e.getMessage(), e);
					} else {
						AbstractMultipleReferenceDataField.logger.trace(null, e);
					}
				} finally {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}

		Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		long t = System.currentTimeMillis();
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Map<Object, Object> hKeysValues = new HashMap<>();
			if (this.parentCods != null) {
				for (int i = 0; i < this.parentCods.size(); i++) {
					Object oParentKey = this.parentCods.get(i);
					Object oParentKeyValue = this.parentForm.getDataFieldValue(oParentKey.toString());
					if (ApplicationManager.DEBUG) {
						AbstractMultipleReferenceDataField.logger.debug("Filtering by parent key: " + oParentKey + " with value: " + oParentKeyValue);
					}
					if (oParentKeyValue != null) {
						hKeysValues.put(this.parentkeys.get(i), oParentKeyValue);
					}
				}
			}
			EntityResult entityResult = this.locator.getEntityReference(this.entity).query(hKeysValues, this.getAttributes(), this.locator.getSessionId());
			if (ApplicationManager.DEBUG_TIMES) {
				AbstractMultipleReferenceDataField.logger.debug("AbstractMultipleReferenceDataField: init cache time: " + (this.lastCacheTime - t));
			}
			if (entityResult.getCode() == EntityResult.OPERATION_WRONG) {
				if (this.parentForm != null) {
					this.parentForm.message(entityResult.getMessage(), Form.ERROR_MESSAGE);
				}
				return;
			}
			ConnectionManager.checkEntityResult(entityResult, this.locator);
			this.dataCache = entityResult;
			this.dataCacheInitialized = true;
			this.lastCacheTime = t;
			if (ApplicationManager.DEBUG) {
				AbstractMultipleReferenceDataField.logger.debug("Data cache initialized.");
				int size = -1;
				ByteArrayOutputStream bOut = null;
				ObjectOutputStream out = null;
				try {
					bOut = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bOut);
					out.writeObject(entityResult);
					out.flush();
					size = bOut.size();

				} catch (Exception e) {
					AbstractMultipleReferenceDataField.logger.error(null, e);
				} finally {
					if (bOut != null) {
						bOut.reset();
						bOut.close();
					}
					if (out != null) {
						out.close();
					}
				}
				AbstractMultipleReferenceDataField.logger.debug("Cache size is " + size + " bytes");
			}
		} catch (Exception e) {
			this.parentForm.message("interactionmanager.error_in_query", Form.ERROR_MESSAGE, e);
			if (ApplicationManager.DEBUG) {
				AbstractMultipleReferenceDataField.logger.debug("Query Error. Cannot show results" + e.getMessage(), e);
			} else {
				AbstractMultipleReferenceDataField.logger.error(null, e);
			}
		} finally {
			this.setCursor(cursor);
		}

	}

	/**
	 * Invalidates the cache. Sets {@link #dataCacheInitialized} to false.
	 */
	public void invalidateCache() {
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.lastCacheTime = 0;
			Object oValue = this.getValue();
			this.dataCacheInitialized = false;
			this.dataCache = new EntityResultMapImpl();
			if ((this.cacheManager != null) && this.useCacheManager) {
				this.cacheManager.invalidateCache(this.entity, this.getParentKeyValues());
			}
			this.initCache();
			this.setValue(oValue);
		} catch (Exception exc) {
			AbstractMultipleReferenceDataField.logger.error("Cache update error", exc);
			if (ApplicationManager.DEBUG) {
				AbstractMultipleReferenceDataField.logger.error(null, exc);
			} else {
				AbstractMultipleReferenceDataField.logger.trace(null, exc);
			}
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Prepares the object value to perform a query by code. <p>
	 * 
	 * @param value
	 *            the object to query
	 * @return the query result
	 */
	protected EntityResult queryByCode(Object value) {

		Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Map<Object, Object> hFilterKeys = new HashMap<>();
			if (value instanceof Map) {

				if (this.cods != null) {
					for (int i = 0; i < this.cods.size(); i++) {
						Object oEntityKey = this.cods.get(i);
						if (((Map<?,?>) value).containsKey(oEntityKey)) {
							hFilterKeys.put(oEntityKey, ((Map<?,?>) value).get(oEntityKey));
						}
					}
					if (hFilterKeys.size() == 0) {
						AbstractMultipleReferenceDataField.logger.debug("AbstractMultipleReferenceDataField: COD values missing");
						return new EntityResultMapImpl();
					}
				} else {
					hFilterKeys.putAll((Map<?,?>) value);
				}
				if (this.parentCods != null) {
					for (int i = 0; i < this.parentCods.size(); i++) {
						Object oParentCod = this.parentCods.get(i);
						Object oParentCodValue = this.parentForm.getDataFieldValue(this.parentkeys.get(i).toString());
						if (ApplicationManager.DEBUG) {
							AbstractMultipleReferenceDataField.logger.debug("Filtering by parent key: " + oParentCod + " with value: " + oParentCodValue);
						}
						if (oParentCodValue != null) {
							hFilterKeys.put(oParentCod, oParentCodValue);
						}
					}
				}
			}

			EntityResult entityResult = this.locator.getEntityReference(this.entity).query(hFilterKeys, this.getAttributes(), this.locator.getSessionId());
			if (entityResult.getCode() == EntityResult.OPERATION_WRONG) {
				if (ApplicationManager.DEBUG) {
					AbstractMultipleReferenceDataField.logger.debug(entityResult.getMessage());
				}
				return new EntityResultMapImpl();
			}
			ConnectionManager.checkEntityResult(entityResult, this.locator);
			return entityResult;
		} catch (Exception exc) {
			if (ApplicationManager.DEBUG) {
				AbstractMultipleReferenceDataField.logger.debug("Query error. Result cannot be shown" + exc.getMessage(), exc);
			} else {
				AbstractMultipleReferenceDataField.logger.error(null, exc);
			}
			return new EntityResultMapImpl();
		} finally {
			this.setCursor(cursor);
		}
	}

	/**
	 * Deletes data. It sets <code>null<code> in all formats (value and cods).
	 */
	@Override
	public void deleteData() {
		this.enableInnerListener(false);
		Object previousValue = this.getValue();
		this.value = null;
		this.setFormatValue(null);
		this.setFormatCods(null);
		this.valueSave = this.getValue();
		this.setInnerValue(this.getValue());
		this.fireValueChanged(this.valueSave, previousValue, ValueEvent.PROGRAMMATIC_CHANGE);
		this.enableInnerListener(true);
	}

	/**
	 * Deletes the user data. Calls to {@link #deleteUserData(boolean)} with true parameter.
	 */
	public void deleteUserData() {
		this.deleteUserData(true);
	}

	/**
	 * Deletes the user data. <p>
	 * 
	 * @param withcods
	 *            the cods or no cods presence
	 */
	protected void deleteUserData(boolean withcods) {
		this.enableInnerListener(false);
		Object previousValue = this.getValue();
		this.value = null;
		this.setFormatValue(null);
		if (withcods) {
			this.setFormatCods(null);
		}
		this.setInnerValue(this.getValue());
		this.fireValueChanged(this.getInnerValue(), previousValue, ValueEvent.USER_CHANGE);
		this.enableInnerListener(true);
	}

	/**
	 * Gets the description for all columns in multiple value object. <p>
	 * 
	 * @param value
	 *            the multiple value object to get description
	 * @return the description
	 */
	protected String getDescription(Object value) {

		// For each column get the value to show
		if (value instanceof MultipleValue) {
			StringBuilder descriptionString = new StringBuilder();
			for (int i = 0; i < this.cols.size(); i++) {
				Object oColumn = this.cols.get(i);
				Format format = this.getFormat(oColumn.toString());
				Object vColumn = ((MultipleValue) value).get(oColumn);
				if (vColumn != null) {
					if (format != null) {
						descriptionString.append(format.format(vColumn));
					} else {
						descriptionString.append(vColumn.toString());
					}

					if (i < (this.cols.size() - 1)) {
						descriptionString.append(this.separator);
					}
				}
			}
			return descriptionString.toString();
		}
		return "";
	}

	/**
	 * Gets the format to column. <p>
	 * 
	 * @param column
	 *            the name of column
	 * @return the current column format
	 */
	protected Format getFormat(String column) {
		if (this.formatColumn.containsKey(column)) {
			return (Format) this.formatColumn.get(column);
		} else {
			return null;
		}
	}

	/**
	 * Adds the format to columns. <p>
	 * 
	 * @param column
	 *            the column
	 * @param format
	 *            the format to apply
	 */
	protected void addFormat(String column, Format format) {
		this.formatColumn.put(column, format);
	}

	/**
	 * Enables the inner listener. <p>
	 * 
	 * @param enable
	 *            the condition to enable the listener
	 */
	protected void enableInnerListener(boolean enable) {
		this.innerListener.setInnerListenerEnabled(enable);
	}

	/**
	 * Gets the inner value. <p>
	 * 
	 * @return the inner value
	 */
	protected Object getInnerValue() {
		return this.innerListener.getInnerValue();
	}

	/**
	 * Sets the inner value. <p>
	 * 
	 * @param o
	 *            the object to set the inner value.
	 */
	protected void setInnerValue(Object o) {
		this.innerListener.setInnerValue(o);
	}

	/**
	 * Sets a code to object. It calls to {@link #setValue(Object)} after looking the data cache or the {@link #queryByCode(Object)}. <p>
	 * 
	 * @param codeValue
	 *            the hashtable to set the multiple value
	 * @param valueEventType
	 *            the value event type
	 */
	protected void setCode(Object codeValue, int valueEventType) {
		// Query:
		try {
			if (this.cacheTime > 0) {
				long t = System.currentTimeMillis();
				long timeFromLastQuery = t - this.getLastCacheTime();
				if (timeFromLastQuery > this.cacheTime) {
					try {
						this.fireValueEvents = false;
						this.invalidateCache();
						this.enableInnerListener(false);
					} catch (Exception e) {
						AbstractMultipleReferenceDataField.logger.trace(null, e);
					} finally {
						this.fireValueEvents = true;
					}
				}
				if (ApplicationManager.DEBUG) {
					AbstractMultipleReferenceDataField.logger.debug("setCode(): Code value: " + codeValue + " with cache");
				}
				// If codeValue is an hastable, must check the cache and search
				// for the value to this record
				if (codeValue instanceof Map) {
					if (this.dataCacheInitialized) {
						int record = this.dataCache.getRecordIndex((Map<?, ?>) codeValue);
						if (record >= 0) {
							Map<?, ?> dataRecord = ((EntityResult) this.dataCache).getRecordValues(record);
							codeValue = new MultipleValue(dataRecord);
						} else {
							this.deleteUserData(false);
							return;
						}
					} else {
						EntityResult res = this.queryByCode(codeValue);
						if (res.isEmpty()) {
							this.deleteUserData(false);
							return;
						}
						codeValue = new MultipleValue(res.getRecordValues(0));
					}
				}

				boolean sameValue = false;
				Object oPreviousValue = this.getInnerValue();
				sameValue = AbstractMultipleReferenceDataField.compareMultipleValue(codeValue, oPreviousValue, this.cods);
				this.valueEventDisabled = true;
				try {
					this.setValue(codeValue, true);
				} catch (Exception e) {
					AbstractMultipleReferenceDataField.logger.trace(null, e);
				}
				this.valueEventDisabled = false;
				this.setInnerValue(this.getValue());
				if (!sameValue) {
					this.fireValueChanged(this.getValue(), oPreviousValue, valueEventType);
				}
			} else {
				// If there is not cache
				if (codeValue instanceof Map) {
					EntityResult res = this.queryByCode(codeValue);
					if (res.isEmpty()) {
						this.deleteUserData(false);
						return;
					}
					Map<?,?> data = res.getRecordValues(0);
					Map<Object, Object> hEntityData = new HashMap<>();
					hEntityData.putAll(data);
					if (this.cods != null) {
						for(Entry<?, ?> entry:data.entrySet()) {
							Object k = entry.getKey();
							if (this.cods.contains(k)) {
								hEntityData.put(this.keys.get(this.cods.indexOf(k)), entry.getValue());
							}
						}
					}
					codeValue = new MultipleValue(hEntityData);
				}

				if (ApplicationManager.DEBUG) {
					AbstractMultipleReferenceDataField.logger.debug("setCode(): Code value: " + codeValue + " without cache");
				}

				boolean sameValue = false;
				Object oPreviousValue = this.getInnerValue();
				if ((oPreviousValue == null) && (codeValue == null)) {
					sameValue = true;
				} else if ((oPreviousValue != null) && (codeValue != null) && codeValue.equals(oPreviousValue)) {
					sameValue = true;
				}

				this.valueEventDisabled = true;
				try {
					this.setValue(codeValue, true);
				} catch (Exception e) {
					AbstractMultipleReferenceDataField.logger.trace(null, e);
				}
				this.valueEventDisabled = false;
				this.setInnerValue(this.getValue());
				if (!sameValue) {
					this.fireValueChanged(this.getValue(), oPreviousValue, valueEventType);
				}
			}
		} catch (Exception e) {
			if (com.ontimize.gui.ApplicationManager.DEBUG) {
				AbstractMultipleReferenceDataField.logger.debug("Error querying code", e);
			} else {
				AbstractMultipleReferenceDataField.logger.trace(null, e);
			}
		}
	}

	/**
	 * Compares multiple values from key list. <p>
	 * 
	 * @param v1
	 *            the object 1
	 * @param v2
	 *            the object 2
	 * @param keys
	 *            the list of keys
	 * @return true when two multiple value objects are equals
	 */
	public static boolean compareMultipleValue(Object v1, Object v2, List<?> keys) {
		if ((v1 == null) && (v2 == null)) {
			return true;
		}
		if (v1 == null) {
			return false;
		}
		if (v2 == null) {
			return false;
		}
		if ((v1 instanceof MultipleValue) && (v2 instanceof MultipleValue)) {
			for (int i = 0; i < keys.size(); i++) {
				Object c = keys.get(i);
				Object c1 = ((MultipleValue) v1).get(c);
				Object c2 = ((MultipleValue) v2).get(c);
				if (!((c1 == null) && (c2 == null))) {
					if (c1 == null) {
						return false;
					}
					if (c2 == null) {
						return false;
					}
					if (!c1.equals(c2)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public boolean hasParentKeys() {
		if ((this.parentkeys == null) || this.parentkeys.isEmpty()) {
			return false;
		}
		return true;
	}

	public Map<Object, Object> getParentKeyValues() {
		if (this.hasParentKeys()) {
			Map<Object, Object> hKeysValues = new HashMap<>();
			for (int i = 0; i < this.parentkeys.size(); i++) {
				Object oParentKey = this.parentkeys.get(i);
				Object oParentKeyValue = this.parentForm.getDataFieldValue(oParentKey.toString());
				if (ApplicationManager.DEBUG) {
					AbstractMultipleReferenceDataField.logger.debug("Filtering by " + oParentKey + " parentkey with value: " + oParentKeyValue);
				}
				if (oParentKeyValue != null) {
					hKeysValues.put(this.parentkeys.get(i), oParentKeyValue);
				}
			}
			return hKeysValues;
		}
		return null;
	}

	@Override
	public void setResourceBundle(ResourceBundle resource) {
		super.setResourceBundle(resource);
		if (this.multilanguage) {

			this.invalidateCache();

		}
	}

}
