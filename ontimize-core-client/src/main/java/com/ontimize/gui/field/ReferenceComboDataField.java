package com.ontimize.gui.field;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.FormBuilder;
import com.ontimize.cache.CacheManager;
import com.ontimize.cache.CacheManager.DataCacheId;
import com.ontimize.cache.CachedComponent;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BorderManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.DataRecordEvent;
import com.ontimize.gui.ExtendedJPopupMenu;
import com.ontimize.gui.Form;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.ValueChangeListener;
import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.actions.CreateFormInDialog;
import com.ontimize.gui.field.ReferenceExtDataField.MultipleResultWindow;
import com.ontimize.gui.field.document.MaskDocument;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.NullValue;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.ConnectionManager;
import com.ontimize.jee.common.gui.ConnectionOptimizer;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.tools.Pair;
import com.ontimize.jee.common.util.ParseTools;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.templates.ITemplateField;

/**
 * The main class to create a classic Reference Data Field except that the data deployment is implemented in the combo. This component also implements a data cache. <p>
 *
 * @author Imatia Innovation
 */
public class ReferenceComboDataField extends ComboDataField
		implements AdvancedDataComponent, ReferenceComponent, CachedComponent, IFilterElement, ReferenceDataComponent, ITemplateField {

	private static final Logger		logger									= LoggerFactory.getLogger(ReferenceComboDataField.class);

	public static final String		PARENTKEY_LISTENER						= "parentkeylistener";

	public static final String		DISABLE_ON_PARENTKEY_NULL				= "disableonparentkeynull";

	/**
	 * Key used for xml parameter 'parentkeylistenerevent'
	 *
	 * @since Ontimize 5.2068-06EN
	 */
	public static final String		PARENTKEY_LISTENER_EVENT				= "parentkeylistenerevent";

	public static final String		PARENTKEY_LISTENER_EVENT_ALL			= "all";

	public static final String		PARENTKEY_LISTENER_EVENT_USER			= "user";

	public static final String		PARENTKEY_LISTENER_EVENT_PROGRAMMATIC	= "programmatic";

	public static String			defaultParentkeyListenerEvent			= ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_ALL;

	public static boolean			defaultParentkeyListener				= false;

	public static boolean			defaultDisableOnParentkeyNull			= false;

	/**
	 * Key used for xml parameter 'ignorenullonsetvalueset'
	 *
	 * @since Ontimize 5.2068-06EN
	 */
	public static final String		IGNORE_NULL_ONSETVALUESET				= "ignorenullonsetvalueset";

	public static boolean			defaultIgnoreNullOnSetValueSet			= false;

	/**
	 * The parent Frame. By default, null.
	 */
	protected Frame					parentFrame								= null;

	/**
	 * The reference to multiple result window. By default, null.
	 */
	protected MultipleResultWindow	multipleResultWindow					= null;

	/**
	 * The condition of code visibility of the code in the detail window. By default, false.
	 */
	protected boolean				visibleCodeSearch						= false;

	/**
	 * This object is used to store onsetvalueset attributes and equivalences (for these fields in entity) when structure of parameter <code>onsetvalueset</code> is:
	 * "fieldonset1:fieldentitypk1;fieldonset2:fieldentitypk2;...fieldonsetn:fieldentitypkn"
	 */
	protected Hashtable				hOnSetValueSetEquivalences				= new Hashtable();

	/**
	 * The key to update data.
	 */
	public static String			UPDATE_DATA								= "field.update_data";

	private static String			SPACE									= " ";

	/**
	 * The code query field key. By default, null.
	 */
	protected String				codeQueryField							= null;

	/**
	 * A reference to deployed list. By default, null.
	 */
	protected DeployedList			deployedList							= null;

	/**
	 * A vector for query values. By default, null.
	 */
	protected List<Object>			queryValues								= null;

	/**
	 * This object is used to store parentkeys and equivalences (for these fields in entity) when structure of parameter <code>parentkeys</code> is:
	 * "fieldpk1:fieldentitypk1;fieldpk2:fieldentitypk2;...fieldpkn:fieldentitypkn"
	 */
	protected Map<Object, Object>	hParentkeyEquivalences;

	/**
	 * The condition to show error messages. By default, true.
	 */
	protected boolean				showErrorMessages						= true;

	/**
	 * A reference to detail button. By default, null.
	 */
	protected JButton				detailButton							= null;

	/**
	 * The Field button for delete.
	 */
	protected JButton				deleteButton;

	protected static final String	DELETE_BUTTON_ATTR						= "deletebutton";

	public static boolean			defaultDeleteButton						= false;

	/**
	 * The form name. By default, null.
	 */
	protected String				formName								= null;

	/**
	 * True when DataRecordListener is registered in the detail form
	 */
	protected boolean				dataRecordListenerReady					= false;

	/**
	 * The vector with attributes to update when data field value changed. By default, null.
	 */
	protected List<Object>			onsetvaluesetAttributes					= null;

	/**
	 * Indicates if fields contained into 'onsetvalueset' have to be deleted on null value of the field.
	 *
	 * @since 5.2068EN-0.6
	 */
	protected boolean				ignorenullonsetvalueset					= ReferenceComboDataField.defaultIgnoreNullOnSetValueSet;

	/**
	 * The main class to design a deployed list.
	 *
	 * @author Imatia Innovation
	 */

	/**
	 * Indicates whether this field has registered a listener for each parentkey fields to reset it when one of the values of these parentkeys change (when parentkey field that
	 * changes is null it is not reset).
	 *
	 * @since 5.2057EN-1.4
	 */
	protected boolean				parentkeyListener						= ReferenceComboDataField.defaultParentkeyListener;

	/**
	 * Indicates the type of event that is taken into consideration on parentkey listener changes.
	 *
	 * @since 5.2068EN-0.6
	 */
	protected String				parentkeyListenerEvent					= ReferenceComboDataField.defaultParentkeyListenerEvent;

	/**
	 * Indicates whether this field has disabled when parentkey field is null.
	 *
	 * @since 5.2057EN-1.4
	 */
	protected boolean				disableonparentkeynull					= ReferenceComboDataField.defaultDisableOnParentkeyNull;

	protected class DeployedList extends JDialog implements Internationalization {

		/**
		 * The key for selecting query values.
		 */
		protected static final String		SELECT_QUERY_VALUES	= "datafield.choose_values_to_search";

		/**
		 * The key for cancel button.
		 */
		protected static final String		CANCEL				= "application.cancel";

		/**
		 * The key for accept button.
		 */
		protected static final String		ACCEPT				= "application.accept";

		/**
		 * The instance of a list.
		 */
		protected JList						list				= new JList();

		/**
		 * A reference for a reference combo data field. By default, null.
		 */
		protected ReferenceComboDataField	comboRef			= null;

		/**
		 * A reference for an accept button. By default, null.
		 */
		protected JButton					accept				= null;

		/**
		 * A reference for a cancel button. By default, null.
		 */
		protected JButton					cancel				= null;

		/**
		 * A instance of a info label.
		 */
		protected JLabel					info				= new JLabel();

		/**
		 * The constructor to create a deployed list in a Frame. Calls to {@link #init()} <p>
		 *
		 * @param f
		 *            the frame
		 * @param combo
		 *            the combo to insert into the frame
		 */
		public DeployedList(Frame f, ReferenceComboDataField combo) {
			super(f, ApplicationManager.getTranslation(AdvancedDataComponent.ADVANCED_QUERY), true);
			this.accept = new JButton(ApplicationManager.getTranslation(DeployedList.ACCEPT));
			this.cancel = new JButton(ApplicationManager.getTranslation(DeployedList.CANCEL));
			this.comboRef = combo;
			this.init();
		}

		/**
		 * The constructor to create a deployed list in a Dialog. Calls to {@link #init()}
		 *
		 * @param d
		 *            the dialog
		 * @param combo
		 *            the combo to insert into the Dialog
		 */
		public DeployedList(Dialog d, ReferenceComboDataField combo) {
			super(d, true);
			this.accept = new JButton(ApplicationManager.getTranslation(DeployedList.ACCEPT));
			this.cancel = new JButton(ApplicationManager.getTranslation(DeployedList.CANCEL));
			this.comboRef = combo;
			this.init();
		}

		/**
		 * The method to create the combo Panel, insert the accept and cancel buttons and performs a query for combo data.
		 */
		public void init() {
			MultiColumnComboRenderer listRenderer = new MultiColumnComboRenderer(this.comboRef.separator);
			listRenderer.setTranslate(ReferenceComboDataField.this.translate);
			this.list.setCellRenderer(listRenderer);
			this.updateData();
			this.list.setVisibleRowCount(8);
			this.list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			JPanel jpButtonsPanel = new JPanel();
			jpButtonsPanel.add(this.accept);
			jpButtonsPanel.add(this.cancel);
			this.info.setText(ApplicationManager.getTranslation(DeployedList.SELECT_QUERY_VALUES));
			this.getContentPane().add(this.info, BorderLayout.NORTH);
			this.getContentPane().add(jpButtonsPanel, BorderLayout.SOUTH);
			this.getContentPane().add(new JScrollPane(this.list));
			this.accept.addActionListener(e -> {
				if (DeployedList.this.list.getSelectedIndex() >= 0) {
					int[] indices = DeployedList.this.list.getSelectedIndices();
					List<Object> vValues = new ArrayList<>();
					for (int indice : indices) {
						Object oValue = DeployedList.this.list.getModel().getElementAt(indice);
						if (oValue.equals(CustomComboBoxModel.NULL_SELECTION)) {
							continue;
						}
						vValues.add(oValue);
					}
					DeployedList.this.comboRef.setQueryValues(vValues);
				} else {
					DeployedList.this.comboRef.setQueryValues(null);
				}
				DeployedList.this.setVisible(false);
			});
			this.cancel.addActionListener(e -> DeployedList.this.setVisible(false));
			this.pack();
			ApplicationManager.center(this);
		}

		/**
		 * Updates combo data list.
		 */
		public void updateData() {
			this.list.setListData((Vector) this.comboRef.dataCache.get(this.comboRef.code));
		}

		@Override
		public void processKeyEvent(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				e.consume();
				this.setVisible(false);
			} else {
				super.processKeyEvent(e);
			}

		}

		@Override
		public Vector getTextsToTranslate() {
			return null;
		}

		@Override
		public void setComponentLocale(Locale l) {}

		@Override
		public void setResourceBundle(ResourceBundle resources) {
			this.accept.setText(ApplicationManager.getTranslation(DeployedList.ACCEPT, resources));
			this.cancel.setText(ApplicationManager.getTranslation(DeployedList.CANCEL, resources));
			this.setTitle(ApplicationManager.getTranslation(AdvancedDataComponent.ADVANCED_QUERY, resources));
			this.info.setText(ApplicationManager.getTranslation(DeployedList.SELECT_QUERY_VALUES, resources));
			if (ReferenceComboDataField.this.translate) {
				if (this.list.getCellRenderer() instanceof Internationalization) {
					((Internationalization) this.list.getCellRenderer()).setResourceBundle(resources);
				}
			}
		}

	}

	/**
	 * The code field listener to manage the codeChange value in function of events. <p>
	 *
	 * @author Imatia Innovation
	 */
	protected class CodeFieldListener extends FocusAdapter implements DocumentListener, KeyListener {

		private boolean	codeChange	= false;

		private boolean	enabled		= true;

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.codeChange = true;
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.codeChange = true;
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (!this.enabled) {
				return;
			}
			this.codeChange = true;
		}

		@Override
		public void focusLost(FocusEvent event) {
			if (!event.isTemporary()) {
				if (this.codeChange) {
					this.codeChange = false;
					this.processFocus();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				this.codeChange = false;
				e.consume();
				this.processFocus();
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {}

		@Override
		public void keyTyped(KeyEvent e) {}

		/**
		 * Processes focus in function of code field value is deleted or is empty.
		 */
		protected void processFocus() {
			if (ReferenceComboDataField.this.codeField.getText().equals("") && (ReferenceComboDataField.this.codeQueryField == null)) {
				// Code field is empty. There are 2 possibilities, previous
				// value was empty too or not
				Object currentValue = ReferenceComboDataField.this.getValue();
				if (!ReferenceComboDataField.this.isInnerValueEqual(currentValue)) {
					try {
						ReferenceComboDataField.this.setInnerListenerEnabled(false);
						Object oldValue = ReferenceComboDataField.this.getInnerValue();
						((JTextField) ReferenceComboDataField.this.dataField).setText("");
						ReferenceComboDataField.this.setInnerValue(currentValue);
						ReferenceComboDataField.this.fireValueChanged(currentValue, oldValue, ValueEvent.USER_CHANGE);
					} finally {
						ReferenceComboDataField.this.setInnerListenerEnabled(true);
					}
				}
			} else if (ReferenceComboDataField.this.codeQueryField != null) {
				Object oCodeValue = null;
				oCodeValue = ReferenceComboDataField.this.getCodeFieldValue();
				if ((oCodeValue == null) || oCodeValue.equals("")) {
					oCodeValue = new SearchValue(SearchValue.NULL, null);
				}
				EntityResult res = ReferenceComboDataField.this.queryBy(ReferenceComboDataField.this.codeQueryField, oCodeValue);
				if (res.getCode() == EntityResult.OPERATION_WRONG) {
					ReferenceComboDataField.this.parentForm.message(res.getMessage(), Form.ERROR_MESSAGE);
				} else {
					if (res.isEmpty()) {
						ReferenceComboDataField.this.deleteData();
					} else if (res.calculateRecordNumber() == 1) {
						Map<Object, Object> hData = res.getRecordValues(0);
						Object oResultCodeValue = hData.get(ReferenceComboDataField.this.code);
						if (oResultCodeValue == null) {
							ReferenceComboDataField.logger.warn("Query result has not data for the specified code value {}", hData);
							ReferenceComboDataField.this.deleteData();
						} else {
							ReferenceComboDataField.this.setCode(oResultCodeValue, ValueEvent.USER_CHANGE);
						}
					} else {
						// More than one record
						Object o = ReferenceComboDataField.this.multipleResultWindow.showResultSelectionWindow(res, ReferenceComboDataField.this,
								ReferenceComboDataField.this.resources);
						if (o != null) {
							ReferenceComboDataField.this.setCode(o, ValueEvent.USER_CHANGE);
						} else {
							ReferenceComboDataField.this.setCode(ReferenceComboDataField.this.getValue(), ValueEvent.USER_CHANGE);
						}
					}
				}
			} else {
				Object codeValue = null;
				codeValue = ReferenceComboDataField.this.getCodeFieldValue();

				ReferenceComboDataField.this.setCode(codeValue, ValueEvent.USER_CHANGE);
			}
			this.codeChange = false;
		}

		public void setEnabled(boolean en) {
			this.enabled = en;
		}

	}

	/**
	 * A reference to code field listener.
	 */
	protected CodeFieldListener			codeFieldListener		= new CodeFieldListener();

	// /**
	// * A instance of focus adapter.
	// */
	// protected FocusAdapter codFieldListener = new FocusAdapter() {
	// public void focusLost(FocusEvent e) {
	// selectionCode();
	// }
	// };

	/**
	 * A multiple column combo renderer reference. By default, null.
	 */
	protected MultiColumnComboRenderer	renderer				= null;

	/**
	 * The condition of integer value. By default, false.
	 */
	protected boolean					integerValue			= false;

	/**
	 * The condition about existence of code number. By default, false.
	 */
	protected boolean					codeNumber				= false;

	/**
	 * The code number class. By default, it is referred to integer code.
	 */
	protected int						codeNumberClass			= ParseTools.INTEGER_;

	/**
	 * The string code. By default, null.
	 */
	protected String					code					= null;

	/**
	 * The locator reference. By default, null.
	 */
	// protected EntityReferenceLocator locator = null;
	protected EntityReferenceLocator	locator;

	/**
	 * A reference to code field. By default, null.
	 */
	protected JTextField				codeField				= new JTextField(4);

	/**
	 * The parent Keys vector. By default, null.
	 */
	protected Vector					parentKeys				= null;

	/**
	 * The entity name. By default, null.
	 */
	protected String					entityName				= null;

	/**
	 * A reference for a possible auxiliary attribute. By default, null.
	 */
	protected String					attrAux					= null;

	/**
	 * The columns vector. By default, null.
	 */
	protected List<Object>				cols					= null;

	/**
	 * The visible columns vector. By default, null.
	 */
	protected List<Object>				visibleColumns			= null;

	/**
	 * The condition about visible cod. By default, false.
	 */
	protected boolean					codVisible				= false;

	/**
	 * The mask to apply, by default, null.
	 */
	protected String					applyMask				= null;

	/**
	 * The cache time. By default, 600000.
	 */
	protected int						cacheTime				= 10 * 60 * 1000;

	protected boolean					parentkeyCache			= CacheManager.defaultParentKeyCache;

	/**
	 * The last cache time variable. By default, 0 to perform a query always the first time that combo is deployed.
	 */
	protected long						lastCacheTime			= 0;

	/**
	 * The dataCache implementation.
	 */
	protected EntityResult				dataCache				= new EntityResultMapImpl();

	/**
	 * The separator. By default, " ".
	 */
	protected String					separator				= " ";

	/**
	 * A menu with update data text.
	 */
	protected JMenuItem					updateData				= new JMenuItem(ReferenceComboDataField.UPDATE_DATA);

	/**
	 * A menu for an advanced query text.
	 */
	protected JMenuItem					advancedQuery			= new JMenuItem(ReferenceComboDataField.ADVANCED_QUERY);

	/**
	 * The condition about visible arrow button. By default, true.
	 */
	protected boolean					visibleArrowButton		= true;

	/**
	 * The reference to cache manager. By default, null.
	 */
	protected CacheManager				cacheManager			= null;

	/**
	 * The condition about cache manager use. By default, true.
	 */
	protected boolean					useCacheManager			= true;

	/**
	 * The condition about initialize cache on set value. By default, false.
	 */
	protected boolean					initCacheOnSetValue		= false;

	/**
	 * The condition about data cache initialization. By default, false.
	 */
	protected boolean					dataCacheInitialized	= false;

	public static final String			MULTILANGUAGE			= "multilanguage";

	/**
	 * The condition to disable cache
	 */
	protected boolean					multilanguage			= false;

	public static final String			TRANSLATE				= "translate";

	protected boolean					translate				= false;

	/**
	 * Class for a multicolumn combo renderer. <p>
	 *
	 * @author Imatia Innovation
	 */
	public class MultiColumnComboRenderer extends DefaultListCellRenderer implements Internationalization {

		public static final String COMBO_RENDERER_LABEL_NAME = "ComboBox.listRenderer";

		@Override
		public String getName() {
			return MultiColumnComboRenderer.COMBO_RENDERER_LABEL_NAME;
		}

		/**
		 * A reference for a separator. By default, " ".
		 */
		protected String			separator	= " ";

		/**
		 * A reference for text color. By default, null.
		 */
		protected Color				textColor	= null;

		/**
		 * An instance of String buffer.
		 */
		protected StringBuilder		text		= new StringBuilder();

		protected ResourceBundle	bundle;

		protected boolean			translate	= false;

		/**
		 * An instance of JLabel.
		 */
		protected JLabel			auxRenderer	= new JLabel() {

													@Override
													public void validate() {}

													@Override
													public void revalidate() {}

													@Override
													public void repaint(long tm, int x, int y, int width, int height) {}

													@Override
													public void repaint(Rectangle r) {}

													@Override
													protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
														// Strings get interned...
														if ("text".equals(propertyName)) {
															super.firePropertyChange(propertyName, oldValue, newValue);
														}
													}

													@Override
													public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
												};

		@Override
		public boolean isFocusTraversable() {
			return false;
		}

		// Used because the super.isOpaque() return false if the parent
		// component
		// is not opaque
		// Without this variable the required background color does not work
		protected boolean opaque;

		@Override
		public boolean isOpaque() {
			return super.isOpaque() || this.opaque;
		}

		@Override
		public void setOpaque(boolean isOpaque) {
			super.setOpaque(isOpaque);
			this.opaque = isOpaque;
		}

		/**
		 * The class constructor. Fixes the separator and text color. <p>
		 *
		 * @param separator
		 *            a separator for columns
		 */
		public MultiColumnComboRenderer(String separator) {
			this.separator = separator;
			this.auxRenderer.setOpaque(true);
			this.textColor = this.auxRenderer.getForeground();
		}

		@Override
		public void setFont(Font f) {
			super.setFont(f);
			if (this.auxRenderer != null) {
				this.auxRenderer.setFont(f);
			}
		}

		/**
		 * Gets the code description. <p>
		 *
		 * @param value
		 *            the object with value
		 * @param data
		 *            the hashtable with data
		 * @return the code description
		 */
		public String getCodeDescription(Object value, EntityResult data) {
			if ((value instanceof SearchValue) && (((SearchValue) value).getValue() instanceof java.util.List)) {
				List<?> list = (List<?>) ((SearchValue) value).getValue();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < list.size(); i++) {
					sb.append(this.getCodeDescription(list.get(i), data));
					if (i < (list.size() - 1)) {
						sb.append(" | ");
					}
				}
				return sb.toString();
			} else {
				if ((value == null) || value.equals(CustomComboBoxModel.NULL_SELECTION)) {
					return ReferenceComboDataField.SPACE;
				}
				List<?> vCodes = (List<?>) data.get(ReferenceComboDataField.this.code);
				if ((vCodes == null) || vCodes.isEmpty()) {
					return ReferenceComboDataField.SPACE;
				}
				int codeIndex = vCodes.indexOf(value);
				if (codeIndex < 0) {
					return ReferenceComboDataField.SPACE;
				}
				this.text.delete(0, this.text.length());

				if ((ReferenceComboDataField.this.formatPattern != null) && !ReferenceComboDataField.this.formatPattern.isEmpty()) {
					String s = ReferenceComboDataField.this.formatPattern.parse(codeIndex, ReferenceComboDataField.this.dataCache);
					if ((s != null) && (s.length() > 0)) {
						this.text.append(s);
					}
				} else {
					// Now get the column values
					List<Object> vColumnsToShow = ReferenceComboDataField.this.cols;
					if ((ReferenceComboDataField.this.visibleColumns != null) && !ReferenceComboDataField.this.visibleColumns.isEmpty()) {
						vColumnsToShow = ReferenceComboDataField.this.visibleColumns;
					}
					if (vColumnsToShow.isEmpty()) {
						int columnsCount = ReferenceComboDataField.this.dataCache.calculateRecordNumber();
						int i = 0;

						Enumeration<?> keys = ReferenceComboDataField.this.dataCache.keys();
						while(keys.hasMoreElements()) {
							i++;
							List<?> vColumnData = (List<?>) ReferenceComboDataField.this.dataCache.get(keys.nextElement());
							if (vColumnData != null) {
								Object oColumnValue = vColumnData.get(codeIndex);
								if (oColumnValue != null) {
									if ((this.bundle != null) && this.isTranslate()) {
										oColumnValue = ApplicationManager.getTranslation(oColumnValue.toString(), this.bundle);
									}
									this.text.append(oColumnValue.toString());
									if (i < (columnsCount - 1)) {
										this.text.append(this.separator);
									}
								}
							}
						}
						
					} else {
						for (int i = 0; i < vColumnsToShow.size(); i++) {
							List<?> vColumnData = (List<?>) ReferenceComboDataField.this.dataCache.get(vColumnsToShow.get(i));
							if (vColumnData != null) {
								Object oColumnValue = vColumnData.get(codeIndex);
								if (oColumnValue != null) {
									if ((this.bundle != null) && this.isTranslate()) {
										oColumnValue = ApplicationManager.getTranslation(oColumnValue.toString(), this.bundle);
									}
									this.text.append(oColumnValue.toString());
									if (i < (vColumnsToShow.size() - 1)) {
										this.text.append(this.separator);
									}
								}
							}
						}
					}
				}
				return this.text.toString();
			}
		}

		/**
		 * Gets a renderer component for the table. <p>
		 *
		 * @param value
		 *            the object for get renderer
		 * @param selection
		 *            the condition to manage
		 * @param focus
		 *            the condition to manage the focus
		 * @return the component
		 */
		public Component getRendererComponentForTable(Object oValue, boolean selection, boolean focus) {
			Object value = oValue;
			if ((value != null) && (value instanceof String)) {
				value = ReferenceComboDataField.this.getTypedInnerValue(oValue);
			}

			String sTtext = null;
			this.auxRenderer.setForeground(this.textColor);
			if ((value == null) || value.equals(CustomComboBoxModel.NULL_SELECTION)) {
				this.auxRenderer.setText(" ");
				return this.auxRenderer;
			}

			if (ReferenceComboDataField.this.cacheTime != 0) {
				if (!ReferenceComboDataField.this.dataCacheInitialized) {
					ReferenceComboDataField.this.initCache();
				}
				if (ReferenceComboDataField.this.dataCache == null) {
					this.auxRenderer.setForeground(Color.red);
					this.auxRenderer.setText(" Error ");
					return this.auxRenderer;
				} else if (ReferenceComboDataField.this.dataCache instanceof EntityResult) {
					if (((EntityResult) ReferenceComboDataField.this.dataCache).getCode() == EntityResult.OPERATION_WRONG) {
						this.auxRenderer.setForeground(Color.red);
						this.auxRenderer.setText(" Error ");
						return this.auxRenderer;
					}
				}
				Vector vCodes = (Vector) ReferenceComboDataField.this.dataCache.get(ReferenceComboDataField.this.code);
				if ((!ReferenceComboDataField.this.dataCache.isEmpty()) && (vCodes == null)) {
					this.auxRenderer.setForeground(Color.red);
					this.auxRenderer.setText(" Error : cache does not contains code " + ReferenceComboDataField.this.code);
					return this.auxRenderer;
				}
				int index = -1;
				if (vCodes != null) {
					index = vCodes.indexOf(value);
				}
				if (index >= 0) {
					// In this case value is in the cache. Nothing is doing
				} else {
					// If value is not in the cache make a query
					EntityResult res = ReferenceComboDataField.this.queryByCod(value);
					if (res.isEmpty()) {
						// There isn't a result
					} else {
						if (ReferenceComboDataField.this.dataCache.isEmpty()) {
							ReferenceComboDataField.this.dataCache = (EntityResultMapImpl) res;
						}
						if (ReferenceComboDataField.this.dataCache instanceof EntityResult) {
							if (((EntityResult) ReferenceComboDataField.this.dataCache).getCode() == EntityResult.OPERATION_WRONG) {
								this.auxRenderer.setForeground(Color.red);
								this.auxRenderer.setText(" Error " + res.getMessage());
								return this.auxRenderer;
							}
						}
						// Put in the cache
						List<?> codes = (List<?>) ReferenceComboDataField.this.dataCache.get(ReferenceComboDataField.this.code);
						if (codes == null) {
							if (((EntityResult) ReferenceComboDataField.this.dataCache).getCode() == EntityResult.OPERATION_WRONG) {
								this.auxRenderer.setForeground(Color.red);
								this.auxRenderer.setText(" Error : code not found");
								return this.auxRenderer;
							}
						}
						int codIndex = codes.indexOf(value);
						if (codIndex < 0) {
							// It is needed to put it in the cache
							this.fillCache(res);
						}
					}
				}
				sTtext = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
			} else {
				sTtext = this.getTextIfThereIsNoCache(value);
			}

			this.auxRenderer.setText(sTtext);
			return this.auxRenderer;
		}

		protected void fillCache(EntityResult res) {
			Enumeration<?> enumKeys = ReferenceComboDataField.this.dataCache.keys();
			while(enumKeys.hasMoreElements()) {
				Object oCacheKey = enumKeys.nextElement();
				List<Object> vCacheValues = (List<Object>) ReferenceComboDataField.this.dataCache.get(oCacheKey);
				List<?> vResValues = (List<?>) res.get(oCacheKey);
				if ((vResValues != null) && !vResValues.isEmpty()) {
					vCacheValues.add(vCacheValues.size(), vResValues.get(0));
				} else {
					vCacheValues.add(vCacheValues.size(), null);
				}
			}
		}

		/**
		 * Method used to reduce the complexity of {@link #getRendererComponentForTable(Object, boolean, boolean)}
		 *
		 * @param value
		 * @return
		 */
		protected String getTextIfThereIsNoCache(Object value) {
			String sTtext;
			// If there is no cache then value contains the data. It must be
			// a
			// Hashtable object
			if (value instanceof Map) {
				if (!((Map<?, ?>) value).isEmpty()) {
					// Value contains data: code and requested columns.
					ReferenceComboDataField.this.dataCache = new EntityResultMapImpl(new HashMap<>((Map<?, ?>) value));
					List<?> vCodes = (List<?>) ((Map<?, ?>) value).get(ReferenceComboDataField.this.code);
					// Select the index 0
					if ((vCodes == null) || vCodes.isEmpty() || (vCodes.get(0) == null)) {
						// Nothing to do, no data found.
						sTtext = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
					} else {
						sTtext = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
					}
				} else {
					sTtext = " ";
				}
			} else {
				ReferenceComboDataField.this.dataCache = ReferenceComboDataField.this.queryByCod(value);
				List<?> vCodes = (List<?>) ReferenceComboDataField.this.dataCache.get(ReferenceComboDataField.this.code);
				// Select the index 0
				if ((vCodes == null) || vCodes.isEmpty() || (vCodes.get(0) == null)) {
					// Nothing to do, no data found.
					sTtext = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
				} else {
					sTtext = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
				}

			}
			return sTtext;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean selection, boolean focus) {
			// Combo object contains the codes. Then value is the code.
			// Get the column values.

			Component c = null;
			if ((value == null) || value.equals(CustomComboBoxModel.NULL_SELECTION)) {
				c = super.getListCellRendererComponent(list, " ", index, selection, focus);
				StringBuilder sb = new StringBuilder();
				if (ReferenceComboDataField.this.advancedQueryMode && (ReferenceComboDataField.this.queryValues != null) && (ReferenceComboDataField.this.dataCache != null) && !ReferenceComboDataField.this.dataCache
						.isEmpty()) {
					for (int i = 0; i < ReferenceComboDataField.this.queryValues.size(); i++) {
						sb.append(this.getCodeDescription(ReferenceComboDataField.this.queryValues.get(i), ReferenceComboDataField.this.dataCache));
						if (i < (ReferenceComboDataField.this.queryValues.size() - 1)) {
							sb.append(" | ");
						}
					}
					if (c instanceof JLabel) {
						((JLabel) c).setText(sb.toString());
					}
				}
				return c;
			} else {
				c = super.getListCellRendererComponent(list, value, index, selection, focus);
			}
			Vector vCodes = (Vector) ReferenceComboDataField.this.dataCache.get(ReferenceComboDataField.this.code);
			if ((vCodes == null) || vCodes.isEmpty()) {
				if (c instanceof JLabel) {
					((JLabel) c).setText(" ");
				}
				return c;
			}
			int codeIndex = vCodes.indexOf(value);
			if (codeIndex < 0) {
				if (c instanceof JLabel) {
					((JLabel) c).setText("");
				}
				return c;
			}
			String sText = this.getCodeDescription(value, ReferenceComboDataField.this.dataCache);
			if (c instanceof JLabel) {
				((JLabel) c).setText(sText);
			}

			if (index == -1) {
				c.setEnabled(ReferenceComboDataField.this.isEnabled());
			}
			return c;
		}

		@Override
		public void setComponentLocale(Locale l) {

		}

		@Override
		public void setResourceBundle(ResourceBundle resourceBundle) {
			this.bundle = resourceBundle;

		}

		public boolean isTranslate() {
			return this.translate;
		}

		public void setTranslate(boolean translate) {
			this.translate = translate;
		}

		@Override
		public Vector getTextsToTranslate() {
			return null;
		}

	};

	/**
	 * A reference for detail button listener. By default, null.
	 */
	protected CreateFormInDialog detailButtonListener = null;

	static {
		// To refresh the modellist before show popup...
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			if ((event.getID() == MouseEvent.MOUSE_PRESSED) && SwingUtilities.isLeftMouseButton((MouseEvent) event)) {
				try {
					ExtCustomComboBox combo1 = null;
					if (event.getSource() instanceof ExtCustomComboBox) {
						combo1 = (ExtCustomComboBox) event.getSource();
					} else if (event.getSource() instanceof Component) {
						combo1 = (ExtCustomComboBox) SwingUtilities.getAncestorOfClass(ExtCustomComboBox.class, (Component) event.getSource());
					}
					if (combo1 != null) {
						combo1.popupWillShow();
					}
				} catch (Exception e1) {
					ReferenceComboDataField.logger.error(null, e1);
				}
			} else if ((event.getID() == KeyEvent.KEY_PRESSED) && (event instanceof KeyEvent)) {
				KeyEvent kEvent = (KeyEvent) event;
				if (!((KeyEvent.VK_DOWN == kEvent.getKeyCode()) || (KeyEvent.VK_UP == kEvent.getKeyCode()))) {
					return;
				}

				if (event.getSource() instanceof ExtCustomComboBox) {
					ExtCustomComboBox combo2 = (ExtCustomComboBox) event.getSource();
					combo2.popupWillShow();
				}
			}
		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	}

	protected static class ExtCustomComboBox extends CustomComboBox {

		protected ReferenceComboDataField	referenceComboDataField;

		protected boolean					keySelectionManagerSet;

		public ExtCustomComboBox(ReferenceComboDataField combo) {
			this.referenceComboDataField = combo;
		}

		public void setKeySelectionManager(KeySelectionManager aManager, boolean keySelectionManagerSet) {
			super.setKeySelectionManager(aManager);
			this.keySelectionManagerSet = keySelectionManagerSet;
		}

		@Override
		public void setKeySelectionManager(KeySelectionManager aManager) {
			if (!this.keySelectionManagerSet) {
				super.setKeySelectionManager(aManager);
			}
		}

		public void popupWillShow() {
			try {
				if (this.referenceComboDataField.isEnabled()) {
					this.referenceComboDataField.popupWillShow();
				}
			} catch (Exception e) {
				ReferenceComboDataField.logger.error(null, e);
			}
		}

	}

	/**
	 * The constructor for component. Calls to {@link #init(Map)}. <p>
	 *
	 * @param parameters
	 *            the hashtable with parameters
	 */
	public ReferenceComboDataField(Map<Object, Object> parameters) {
		super();

		this.dataField = new ExtCustomComboBox(this);

		// TODO Change to init();
		this.separator = ParseUtils.getString((String) parameters.get("separator"), this.separator);
		this.renderer = new MultiColumnComboRenderer(this.separator);
		this.init(parameters);
		this.renderer.setFont(this.getFont());

		this.dataField.setFont(this.getFont());
		if (this.codeField != null) {
			this.codeField.setFont(this.getFont());
		}

		if ((!this.parentkeyCache) && (this.parentKeys != null) && !this.parentKeys.isEmpty()) {
			this.useCacheManager = false;
			this.cacheTime = 0;
		}

		CustomComboBoxModel m = new CustomComboBoxModel(this.nullSelection);
		((JComboBox) this.dataField).setModel(m);
		((JComboBox) this.dataField).setRenderer(this.renderer);

		this.createPopupMenu();

		// If cache exists the attribute is the code. In other case it is an
		// ReferenceFieldAttribute
		if (this.cacheTime == 0) {
			if (this.attribute != null) {
				ReferenceComboDataField.logger.debug("Attribute isn't null: {}", this.attribute);
				this.attrAux = this.attribute.toString();
			} else {
				ReferenceComboDataField.logger.debug("Attribute is null, then the code is: {}", this.code);
				this.attrAux = this.code;
			}
			this.attribute = new ReferenceFieldAttribute(this.attrAux, this.entityName, this.code, this.getAttributes());
			this.labelComponent.setText(this.attrAux);
		} else {
			if (this.attribute == null) {
				this.attrAux = this.code;
				this.attribute = this.code;
				this.labelComponent.setText(this.labelText == null ? this.code : this.labelText);
			} else {
				this.attrAux = this.attribute.toString();
				this.labelComponent.setText(this.labelText == null ? this.attrAux : this.labelText);
			}
		}

		((ExtCustomComboBox) this.dataField).setKeySelectionManager(new ComboDataField.ExtKeySelectionManager() {

			@Override
			public int getComboIndex(String str, ComboBoxModel m) {
				long t = System.currentTimeMillis();
				int selectedIndex = ((CustomComboBox) ReferenceComboDataField.this.dataField).getSelectedIndex();
				if ((selectedIndex < 0) || (str.length() == 1)) {
					selectedIndex = 0;
				}
				int nCoincidences = 0;
				int maxIndex = -1;
				for (int i = selectedIndex; i < m.getSize(); i++) {
					int nEastCoincidences = 0;
					String sText = ReferenceComboDataField.this.getCodeDescription(m.getElementAt(i));
					sText = sText.replace('�', 'a');
					sText = sText.replace('�', 'e');
					sText = sText.replace('�', 'i');
					sText = sText.replace('�', 'o');
					sText = sText.replace('�', 'u');
					sText = sText.replace('�', 'a');
					sText = sText.replace('�', 'e');
					sText = sText.replace('�', 'i');
					sText = sText.replace('�', 'o');
					sText = sText.replace('�', 'u');
					for (int j = 0; (j < sText.length()) && (j < str.length()); j++) {
						if (Character.toLowerCase(sText.charAt(j)) != Character.toLowerCase(str.charAt(j))) {
							break;
						} else {
							nEastCoincidences++;
						}
					}
					if (nEastCoincidences > nCoincidences) {
						nCoincidences = nEastCoincidences;
						maxIndex = i;
					}
				}
				ReferenceComboDataField.logger.trace("Time getComboIndex: ", System.currentTimeMillis() - t);
				return maxIndex;
			}

			@Override
			public int selectionForKey(char keyChar, ComboBoxModel m) {
				return -1;
			}
		}, true);
		ToolTipManager.sharedInstance().registerComponent(this);
		this.deleteData();
	}

	public String getDebugInfo() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("<html>");
		buffer.append("<B>" + "entity" + "</B>" + ":  " + this.entityName);
		buffer.append("<br>");
		buffer.append("<B>" + "cod" + "</B>" + ":  " + this.code);
		buffer.append("<br>");
		buffer.append("<B>" + DataField.ATTR + "</B>" + ":  " + this.attribute);
		buffer.append("<br>");
		buffer.append("<B>" + "cols" + "</B>" + ":" + ApplicationManager.vectorToStringSeparateBy(this.cols, ";"));
		buffer.append("<br>");
		buffer.append("<B>" + "parentkeys" + "</B>" + ":  " + ApplicationManager.vectorToStringSeparateBy(this.parentKeys, ";"));
		buffer.append("<br>");
		buffer.append("<B>" + "cachetime" + "</B>" + ":  " + this.cacheTime);
		buffer.append("<br>");
		buffer.append("<B>" + "form" + "</B>" + ":  " + this.formName);
		buffer.append("</html>");
		return buffer.toString();
	}

	@Override
	public String getToolTipText(MouseEvent e) {
		if (e.isControlDown() && e.isAltDown() && e.isShiftDown()) {
			return this.getDebugInfo();
		} else {
			return super.getToolTipText(e);
		}
	}

	/**
	 * Parameters on this method are used to update the fields whose attributes are contained in {@link #onsetvaluesetAttributes}
	 *
	 * @param data
	 *            Values to update
	 */
	protected void updateOnSetValueSetAttributes(Map<Object, Object> data) {
		if ((this.parentForm != null) && (data != null)) {
			for (Object at : this.onsetvaluesetAttributes) {
				Object oValue = data.get(this.hOnSetValueSetEquivalences.get(at));
				this.parentForm.setDataFieldValue(at, oValue);
				ReferenceComboDataField.logger.debug(" Setting field value: {} -> {}", at, oValue);
			}
		}
	}

	public List<Object> getOnSetValueSetAttributes() {
		return this.onsetvaluesetAttributes;
	}

	public Map<Object, Object> getOnSetValueSetEquivalences() {
		return this.hOnSetValueSetEquivalences;
	}

	/**
	 * Adds a <code>DataRecordListener</code> listener to the detail form. This one checks when event is an update event and it is necessary to refresh the data cache and fields
	 * contained in {@link #onsetvaluesetAttributes}
	 *
	 * @param form
	 *            Form where event is fired
	 */
	protected void createDataRecordListener(Form form) {
		if (!this.dataRecordListenerReady) {
			if (form != null) {
				form.addDataRecordListener(event -> {
					if (event.getType() == DataRecordEvent.UPDATE) {
						// When data are updated in detail form it is
						// compulsory
						// updating the cache
						if ((ReferenceComboDataField.this.onsetvaluesetAttributes != null) && (ReferenceComboDataField.this.onsetvaluesetAttributes.size() > 0)) {
							Map<Object, Object> updateAttributes = event.getAttributesValues();
							Map<Object, Object> updateKeys = event.getKeysValues();
							ReferenceComboDataField.this.updateDataCache(updateAttributes, updateKeys);
							if (ReferenceComboDataField.this.existFieldsToUpdate(updateAttributes, updateKeys)) {
								Map<Object, Object> data = ReferenceComboDataField.this.getValuesToCode(ReferenceComboDataField.this.getValue());
								ReferenceComboDataField.this.updateOnSetValueSetAttributes(data);
							}
						}
					}
				});
			}
			this.dataRecordListenerReady = true;
		}
	}

	/**
	 * Checks the keys and the record values in a register to establish if fields in {@link #onsetvaluesetAttributes} must be updated
	 *
	 * @param changes
	 *            Changes to update
	 * @param keys
	 *            Keys for record to update
	 * @return
	 */
	protected boolean existFieldsToUpdate(Map<Object, Object> changes, Map<Object, Object> keys) {
		// Only is necessary to update fields in 'onsetvalueset' attributes if
		// current record has changed.
		// We must check if the changes are in one of the fields in the
		// 'onsetvalueset' attributes too.

		Object attr = this.getAttribute();
		if (attr instanceof ReferenceFieldAttribute) {
			attr = ((ReferenceFieldAttribute) this.getAttribute()).getAttr();
		}
		if (keys.containsKey(attr)) {
			Object currentValue = this.getValue();
			if (keys.get(attr).equals(currentValue)) {
				for (Entry<Object, Object> entry : changes.entrySet()) {
					Object key = entry.getKey();
					if (key instanceof ReferenceFieldAttribute) {
						key = ((ReferenceFieldAttribute) key).getAttr();
					}
					if (this.onsetvaluesetAttributes.contains(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Updates the data cache with specified values
	 *
	 * @param updateAttributes
	 *            Update values
	 * @param updateKeys
	 *            Keys for records to update
	 */
	protected void updateDataCache(Map<Object, Object> updateAttributes, Map<Object, Object> updateKeys) {
		// Maybe it is necessary to update the CacheManager too
		if ((this.dataCache != null) && !this.dataCache.isEmpty() && (this.dataCache instanceof EntityResult)) {
			int index = ((EntityResult) this.dataCache).getRecordIndex(updateKeys);
			if (index >= 0) {
				for (Entry<Object, Object> entry : updateAttributes.entrySet()) {
					Object key = entry.getValue();
					Object values = this.dataCache.get(key);
					if ((values == null) && (key instanceof ReferenceFieldAttribute)) {
						values = this.dataCache.get(((ReferenceFieldAttribute) key).getAttr());
					}
					if ((values != null) && (values instanceof Vector)) {
						((Vector) values).set(index, entry.getValue());
					}
				}
			}
		}
	}

	/**
	 * Initializes parameters. <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td>
	 * <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>attr</td> <td></td> <td><i>cod</td> <td>no</td> <td>The field attribute.</td> </tr> <tr> <td>entity</td> <td></td>
	 * <td></td> <td>yes</td> <td>Associated entity.</td> </tr> <tr> <td>dateformat</td> <td><i>A <a href=
	 * "http://java.sun.com/docs/books/tutorial/i18n/format/simpleDateFormat.html" >Java date pattern<a></i></td> <td></td> <td>no</td> <td>Java Date pattern to use in
	 * <code>format</code> parameter.</td> </tr> <tr> <td>format</td> <td>message;column_1;...;column_N</td> <td></td> <td>no</td> <td>The field shows the translation with the
	 * given message and columns. Example:<br> -User has a reference combo field that queries an entity with three fields ID;INITIALDATE;ENDDATE. ID is the code of field.<br> -User
	 * wants to format description of this field to show: Period starts: <i>INITIALDATE</i> and ends: <i>ENDDATE</i><br> -He only needs to store in bundle a key, e.g., refComboKey=
	 * Period starts: {0} and ends: {1}<br> -He must specify in parameter format="refComboKey;INITIALDATE;ENDDATE"</td> </tr> <tr> <td>cod</td> <td></td> <td></td> <td>yes</td>
	 * <td>The entity column name that matches with the attr.</td> </tr> <tr> <td>csize</td> <td></td> <td></td> <td>no</td> <td>The number of characters for code field.</td> </tr>
	 * <tr> <td>parentkeys</td> <td><i>fieldpk1:fieldentitypk1;fieldpk2:fieldentitypk2;...fieldpkn :fieldentitypkn (since version 5.2057EN-1.4)</i></td> <td></td> <td>yes</td>
	 * <td>The field that is parentkey and correspondent associated field in entity. It is accepted to indicate only the fieldpki when it is equal to fieldentitypki, e.g. :
	 * <i>fieldpk1;fieldpk2:fieldentitypk2 ;...fieldpkn:fieldentitypkn</i></td> </tr> <tr> <td>cols</td> <td><i>cols1;cols2;...;colsn</td> <td></td> <td>yes</td> <td>Columns
	 * associated to the code. It forms the description field.</td> </tr> <tr> <td>cachetime</td> <td></td> <td>10 minutes</td> <td>no</td> <td>The time data remains in cache.</td>
	 * </tr> <tr> <td>codnumber</td> <td>yes/no</td> <td>no</td> <td>no</td> <td>Allows to specify that code column is numerical but not an integer value.</td> </tr> <tr>
	 * <td>codnumberclass</td> <td>BigDecimal/Integer/Long...</td> <td>Integer</td> <td>no</td> <td>Only used when codnumber="yes". String value associated defining code number
	 * class (All values are mapped in ParseUtils class: ParseUtils.INTEGER/ParseUtils.BIG_INTEGER/...)</td> </tr> <tr> <td>deletebutton</td> <td>true/false</td> <td>false</td>
	 * <td>no</td> <td>A button to delete field</td> </tr> <tr> <td>separator</td> <td></td> <td>" "</td> <td>no</td> <td>The separator character for columns.</td> </tr> <tr>
	 * <td>usecachemanager</td> <td>yes/no</td> <td>yes</td> <td>no</td> <td>With "no" parameter, field will have its own cache.</td> </tr> <tr> <td>visiblecols</td>
	 * <td><i>vcols1,vcols2,...,vcolsn</td> <td></td> <td></td> <td>The visible cols from cols. With empty parameter all columns are visible.</td> </tr> <tr> <td>form</td>
	 * <td><i></td> <td></td> <td>no</td> <td>The form that is opened in detail. On init, update mode will be its state.</td> </tr> <tr> <td>parentkeylistener</td>
	 * <td><i>yes/no</td> <td>no</td> <td>no</td> <td>Register a listener for each parentkey field to delete data of this field when one of the parentkey values changes (when new
	 * value of the parentkey field is null, field value is not reset, excepts if 'disableonparentkeynull' is true). If you use this parameter then you need to use the
	 * setvalueorder for the Form @see {@link Form#init(Hashtable)}</td> </tr> <tr> <td>disableonparentkeynull</td> <td><i>yes/no</td> <td>no</td> <td>no</td> <td>Disable field
	 * when parentkey is null.</td> </tr> <tr> <td>onsetvalueset</td> <td><i>fieldonset1:fieldentitypk1;fieldonset2:fieldentitypk2;... fieldonsetn :fieldentitypkn (since version
	 * 5.2057EN-1.4)</td> <td></td> <td>no</td> <td>Field attributes whose value will be set when data on field change.</td> </tr> <tr> <td>codsearch</td> <td><i></td> <td></td>
	 * <td>no</td> <td>It specifies the field name for searching when user introduces a search value.</td> </tr> <tr> <td>parentkeylistenerevent</td> <td><i>user/programmatic</td>
	 * <td>both</td> <td>no</td> <td>The type of event that is taken into consideration on parentkey listener changes. E.g. if 'user' value is set, just the events generated by
	 * user changes are taken into consideration.</td> </tr> <tr> <td>ignorenullonsetvalueset</td> <td><i>yes/no</td> <td>no</td> <td>no</td> <td>It specifies whether the fields
	 * included into 'onsetvalueset' are deleted when null value is set in this field. If value is true the fields contained into 'onsetvalueset' are not deleted on null
	 * value.</td> </tr> <tr> <td>multilanguage</td> <td><i>yes/no</i></td> <td>no</td> <td>no</td> <td>If <i>yes</i>, invalidate the cache data.</td> </tr> </Table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		super.init(parameters);
		this.setCacheTimeParameter(parameters);
		// Same parameters as ReferenceExtDataField
		// Search for entity parameter
		this.setEntityParameter(parameters);
		// Parameter cod : Code column name
		this.setCodeParameter(parameters);
		// Parameter : parentkey or 'parentkeys'
		this.setParentKeysParameter(parameters);
		this.setParentKeyListenerParameter(parameters);
		this.setParentKeyListenerEventParameter(parameters);
		this.setDisableOnParentKeyNullParameter(parameters);
		// Parameter : codsearch
		this.setCodSearchParameter(parameters);
		this.setCodSearchVisibleParameters(parameters);
		Object codInteger = this.setCodIntegerParameter(parameters);
		this.integerValue = ParseUtils.getBoolean((String) codInteger, false);
		this.codeNumber = ParseUtils.getBoolean((String) parameters.get("codnumber"), false);
		this.setCodNumberClassParameter(parameters);
		this.codVisible = ParseUtils.getBoolean((String) parameters.get("codvisible"), false);
		this.codeField.setVisible(this.codVisible);
		this.setCodeVisibleParameter(parameters);
		this.setColsParameter(parameters);
		this.setVisibleColsParameters(parameters);
		this.setOnSetValueSetParameters(parameters);
		Object ignorenullonsetvalueset = parameters.get(ReferenceComboDataField.IGNORE_NULL_ONSETVALUESET);
		this.ignorenullonsetvalueset = ParseUtils.getBoolean((String) ignorenullonsetvalueset, ReferenceComboDataField.defaultIgnoreNullOnSetValueSet);
		this.useCacheManager = ParseUtils.getBoolean((String) parameters.get("usecachemanager"), this.useCacheManager);
		this.initCacheOnSetValue = ParseUtils.getBoolean((String) parameters.get("initcacheonsetvalue"), this.initCacheOnSetValue);
		this.setCodeQueryFieldParameter(parameters);
		ReferenceComboDataField.logger.debug(
				"Entity: {} Code: {} Attribute: {}, cols: {}, visiblecols: {}, parentkeys: {}, separator: {}, cachetime: {}, usecachemanager: {}, initcacheonsetvalue: {}",
				this.getEntity(), this.code, this.attribute, this.cols, this.visibleColumns, this.parentKeys, this.separator, this.cacheTime, this.useCacheManager,
				this.initCacheOnSetValue);
		this.setParentKeyCacheParameter(parameters);
		this.setFormParameter(parameters);
		this.setDetailButtonParameter();
		if (ParseUtils.getBoolean((String) parameters.get(ReferenceComboDataField.DELETE_BUTTON_ATTR), ReferenceComboDataField.defaultDeleteButton)) {
			this.createDeleteButton();
		}
		boolean borderbuttons = ParseUtils.getBoolean((String) parameters.get("borderbuttons"), true);
		boolean opaquebuttons = ParseUtils.getBoolean((String) parameters.get("opaquebuttons"), true);
		boolean highlightButtons = ParseUtils.getBoolean((String) parameters.get("highlightbuttons"), false);
		MouseListener listenerHighlightButtons = null;
		if (highlightButtons) {
			listenerHighlightButtons = new MouseAdapter() {

				@Override
				public void mouseEntered(MouseEvent e) {
					((AbstractButton) e.getSource()).setOpaque(true);
					((AbstractButton) e.getSource()).setContentAreaFilled(true);
				}

				@Override
				public void mouseExited(MouseEvent e) {
					((AbstractButton) e.getSource()).setOpaque(false);
					((AbstractButton) e.getSource()).setContentAreaFilled(false);
				}
			};
		}

		this.changeButton(this.detailButton, borderbuttons, opaquebuttons, listenerHighlightButtons);
		if (this.deleteButton != null) {
			this.changeButton(this.deleteButton, borderbuttons, opaquebuttons, listenerHighlightButtons);
		}

		this.setBackgroundColorInitParameter(parameters);

		this.translate = ParseUtils.getBoolean((String) parameters.get(ReferenceComboDataField.TRANSLATE), false);
		if (this.renderer != null) {
			this.renderer.setTranslate(this.translate);
		}

		this.setMultilanguageParameter(parameters);

		super.panel.add(this.codeField, new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(DataField.DEFAULT_TOP_MARGIN, DataField.DEFAULT_FIELD_LEFT_MARGIN, DataField.DEFAULT_BOTTOM_MARGIN, 0), 0, 0));

		this.codeField.addFocusListener(this.codeFieldListener);
		// Listener for changes in code
		this.codeField.getDocument().addDocumentListener(this.codeFieldListener);
		// Listener for VK_ENTER key
		this.codeField.addKeyListener(this.codeFieldListener);
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setMultilanguageParameter(Map<Object, Object> parameters) {
		Object oMultilanguage = parameters.get(ReferenceComboDataField.MULTILANGUAGE);
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
	protected void setBackgroundColorInitParameter(Map<Object, Object> parameters) {
		Object bgcolor = parameters.get(DataField.BGCOLOR);
		if (bgcolor != null) {
			try {
				this.getRenderer().setBackground(ColorConstants.parseColor(bgcolor.toString()));
			} catch (Exception exc) {
				ReferenceComboDataField.logger.error("Error 'bgcolor' parameter: ", exc);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 */
	protected void setDetailButtonParameter() {
		if (this.detailButton != null) {
			super.add(this.detailButton,
					new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			if (this.labelPosition != SwingConstants.LEFT) {
				this.validateComponentPositions();
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setFormParameter(Map<Object, Object> parameters) {
		Object form = parameters.get("form");
		if (form != null) {
			this.formName = form.toString();
			this.detailButton = new FieldButton();
			this.detailButton.setMargin(new Insets(0, 0, 0, 0));
			this.detailButton.setIcon(ImageManager.getIcon(ImageManager.MAGNIFYING_GLASS));

			Map<Object, Object> equivalentList = null;
			if ((this.attribute != null) && (this.code != null) && !this.attribute.toString().equals(this.code)) {
				equivalentList = new HashMap<>();
				equivalentList.put(this.code, this.attribute.toString());
			}

			this.detailButtonListener = new com.ontimize.gui.actions.CreateFormInDialog(null, this.formName, this.entityName, equivalentList) {

				@Override
				public void windowWillShow() {
					try {
						ReferenceComboDataField.this.createDataRecordListener(this.form);
						if (ReferenceComboDataField.this.parentKeys != null) {
							for (Object element : ReferenceComboDataField.this.parentKeys) {
								this.form.setDataFieldValue(element, ReferenceComboDataField.this.getParentKeyValue(element.toString()));
								this.form.setModifiable(element.toString(), false);
							}
						}

						if (!ReferenceComboDataField.this.isEmpty()) {
							this.goToSourceRecord();
						} else {
							// When there is no selected index then put the
							// detail form in insert mode
							this.form.deleteDataFields();
							this.form.getInteractionManager().setInsertMode();
						}
					} catch (Exception exc) {
						ReferenceComboDataField.this.parentForm.message(exc.getMessage(), Form.ERROR_MESSAGE);
						ReferenceComboDataField.logger.error(null, exc);
					}
				}

				@Override
				public boolean windowWillClose() {
					boolean res = super.windowWillClose();
					if (res) {
						Object oNewValue = this.getCurrentRecordValueField(ReferenceComboDataField.this.code);

						if ((oNewValue != null) && !oNewValue.equals(ReferenceComboDataField.this.getValue())) {
							boolean bQuestion = ReferenceComboDataField.this.parentForm.question("datafield.would_like_select_current_register");
							if (bQuestion) {
								ReferenceComboDataField.this.setValue(this.getCurrentRecordValueField(ReferenceComboDataField.this.code));
							}
						}
					}
					return res;
				}
			};
			this.detailButton.addActionListener(this.detailButtonListener);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setParentKeyCacheParameter(Map<Object, Object> parameters) {
		Object parentkeycache = parameters.get("parentkeycache");
		if ((parentkeycache != null) && parentkeycache.equals("yes")) {
			if (this.hasParentKeys()) {
				this.parentkeyCache = true;
				this.useCacheManager = true;
			} else {
				ReferenceComboDataField.logger.warn("Parameter 'parentkeycache' ignored because field hasn't any parentkey!!!!!");
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodeQueryFieldParameter(Map<Object, Object> parameters) {
		if (this.codeQueryField != null) {
			try {
				if (parameters.containsKey("form")) {
					parameters.remove("form");
				}
				Table tMultipleResults = new Table(parameters);
				this.createMultipleResultsWindow(tMultipleResults);
			} catch (Exception exc) {
				ReferenceComboDataField.logger.error("Error creating multiple results table.", exc);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setOnSetValueSetParameters(Map<Object, Object> parameters) {
		Object onsetvalueset = parameters.get("onsetvalueset");
		if (onsetvalueset != null) {

			this.hOnSetValueSetEquivalences = ApplicationManager.getTokensAt(onsetvalueset.toString(), ";", ":");
			this.onsetvaluesetAttributes = new ArrayList<>();

			// We can't use the keys of the hashtable to get the attribute names
			// because we have to use the same order that is in the xml
			List<String> valueNamesOrder = ApplicationManager.getTokensAt(onsetvalueset.toString(), ";");
			for (String element : valueNamesOrder) {
				int dotIndex = element.toString().indexOf(":");
				if (dotIndex > 0) {
					this.onsetvaluesetAttributes.add(element.toString().substring(0, dotIndex));
				} else {
					this.onsetvaluesetAttributes.add(element);
				}
			}
			if (!this.onsetvaluesetAttributes.isEmpty()) {
				this.addValueChangeListener(event -> {
					if (ReferenceComboDataField.this.isEmpty()) {
						if ((ReferenceComboDataField.this.parentForm != null) && (!ReferenceComboDataField.this.ignorenullonsetvalueset)) {
							for (Object element : ReferenceComboDataField.this.onsetvaluesetAttributes) {
								ReferenceComboDataField.this.parentForm.deleteDataField((String) element);
								ReferenceComboDataField.logger.debug("Deleting field value: {}", element);
							}
						}
					} else {
						Map<Object, Object> h = ReferenceComboDataField.this.getValuesToCode(ReferenceComboDataField.this.getValue());
						ReferenceComboDataField.this.updateOnSetValueSetAttributes(h);
					}
				});
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setVisibleColsParameters(Map<Object, Object> parameters) {
		Object visiblecols = parameters.get("visiblecols");
		if (visiblecols != null) {
			this.visibleColumns = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(visiblecols.toString(), ";");
			while (st.hasMoreTokens()) {
				String c = st.nextToken();
				if (this.cols.contains(c)) {
					this.visibleColumns.add(c);
				}
			}
		} else {
			this.visibleColumns = null;
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setColsParameter(Map<Object, Object> parameters) {
		Object cols = parameters.get("cols");
		if (cols != null) {
			this.cols = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(cols.toString(), ";");
			while (st.hasMoreTokens()) {
				this.cols.add(st.nextToken());
			}
		} else {
			this.cols = new ArrayList<>();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodNumberClassParameter(Map<Object, Object> parameters) {
		Object codnumberclass = parameters.get("codnumberclass");
		if (codnumberclass != null) {
			this.codeNumberClass = ParseUtils.getTypeForName(codnumberclass.toString(), ParseTools.INTEGER_);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 * @return
	 */
	protected Object setCodIntegerParameter(Map<Object, Object> parameters) {
		Object codInteger = parameters.get("codInteger");
		if (codInteger == null) {
			codInteger = parameters.get("codinteger");
		}
		return codInteger;
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodSearchVisibleParameters(Map<Object, Object> parameters) {
		Object codSearchVisible = parameters.get("codsearchvisible");
		if (codSearchVisible != null) {
			this.visibleCodeSearch = ApplicationManager.parseStringValue(codSearchVisible.toString());
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodSearchParameter(Map<Object, Object> parameters) {
		Object codsearch = parameters.get("codsearch");
		if (codsearch != null) {
			this.codeQueryField = codsearch.toString();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setDisableOnParentKeyNullParameter(Map<Object, Object> parameters) {
		Object disableonparentkeynull = parameters.get(ReferenceComboDataField.DISABLE_ON_PARENTKEY_NULL);
		if (disableonparentkeynull != null) {
			this.disableonparentkeynull = ParseUtils.getBoolean(disableonparentkeynull.toString(), ReferenceComboDataField.defaultDisableOnParentkeyNull);
		}

		if (!this.parentkeyListener) {
			this.disableonparentkeynull = false;
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodeVisibleParameter(Map<Object, Object> parameters) {
		if (this.codVisible) {
			// Parameter : mask
			Object mask = parameters.get("mask");
			if (mask != null) {
				this.applyMask = mask.toString();
				MaskDocument doc = new MaskDocument(this.applyMask);
				this.codeField.setDocument(doc);
			} else {
				this.applyMask = null;
			}
			// Parameter: csize
			Object csize = parameters.get("csize");
			if (csize != null) {
				try {
					this.codeField.setColumns(new Integer(csize.toString()).intValue());
				} catch (Exception e) {
					ReferenceComboDataField.logger.error("Error 'csize' parameter in ReferenceComboDataField: ", e);
				}
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setParentKeyListenerEventParameter(Map<Object, Object> parameters) {
		Object parentkeylistenerevent = parameters.get(ReferenceComboDataField.PARENTKEY_LISTENER_EVENT);
		if (parentkeylistenerevent != null) {
			if (parentkeylistenerevent.equals(ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_USER)) {
				this.parentkeyListenerEvent = ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_USER;
			} else if (parentkeylistenerevent.equals(ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_PROGRAMMATIC)) {
				this.parentkeyListenerEvent = ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_PROGRAMMATIC;
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setParentKeyListenerParameter(Map<Object, Object> parameters) {
		Object parentkeylistener = parameters.get(ReferenceComboDataField.PARENTKEY_LISTENER);
		if (parentkeylistener != null) {
			this.parentkeyListener = ParseUtils.getBoolean(parentkeylistener.toString(), ReferenceComboDataField.defaultParentkeyListener);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setParentKeysParameter(Map<Object, Object> parameters) {
		Object parentkeys = parameters.get("parentkey");
		if (parentkeys != null) {
			this.parentKeys = new Vector();
			StringTokenizer st = new StringTokenizer(parentkeys.toString(), ";");
			while (st.hasMoreTokens()) {
				this.parentKeys.add(ApplicationManager.getTokensAt(st.nextToken(), ":").get(0));
			}
		} else {
			parentkeys = parameters.get("parentkeys");
			if (parentkeys != null) {
				this.parentKeys = new Vector();
				StringTokenizer st = new StringTokenizer(parentkeys.toString(), ";");
				while (st.hasMoreTokens()) {
					this.parentKeys.add(ApplicationManager.getTokensAt(st.nextToken(), ":").get(0));
				}
			} else {
				this.parentKeys = null;
			}
		}
		if ((this.parentKeys != null) && (this.parentKeys.size() > 0)) {
			this.hParentkeyEquivalences = ApplicationManager.getTokensAt(parentkeys.toString(), ";", ":");
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCodeParameter(Map<Object, Object> parameters) {
		Object cod = parameters.get("cod");

		if (cod == null) {
			ReferenceComboDataField.logger.warn("'cod' parameter not found. Check parameters");
		} else {
			this.code = cod.toString();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setEntityParameter(Map<Object, Object> parameters) {
		Object entity = parameters.get("entity");
		if (entity == null) {
			ReferenceComboDataField.logger.warn("'entity' parameter not found. Check parameters.");
		} else {
			this.entityName = entity.toString();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Map)}
	 *
	 * @param parameters
	 */
	protected void setCacheTimeParameter(Map<Object, Object> parameters) {
		Object cache = parameters.get("cachetime");
		if (cache != null) {
			try {
				this.cacheTime = Integer.parseInt(cache.toString());
			} catch (Exception exc) {
				ReferenceComboDataField.logger.error("Error 'cachetime' parameter", exc);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #init(Hashtable)} Gets the last cache time. <p>
	 *
	 * @return the time from last access to cache data
	 */
	protected long getLastCacheTime() {
		if (this.useCacheManager && (this.cacheManager != null) && (this.parentkeyCache || this.cacheManager.existsCache(this.entityName, this.getAttributes(),
				this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences())))) {
			return this.cacheManager.getLastCacheTime(this.entityName, this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
		} else {
			return this.lastCacheTime;
		}
	}

	protected void createDeleteButton() {
		this.deleteButton = new FieldButton();
		this.deleteButton.setMargin(new Insets(0, 0, 0, 0));
		this.deleteButton.setIcon(ImageManager.getIcon(ImageManager.DELETE));
		this.deleteButton.setToolTipText(ApplicationManager.getTranslation("datafield.reset_field", this.resources));
		this.deleteButton.addActionListener(event -> ReferenceComboDataField.this.deleteUserData());

		super.add(this.deleteButton,
				new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	@Override
	public void setValue(Object value) {
		this.setValue(value, false);
	}

	/**
	 * Establishes the field value. An object that will be an Hashtable, whose keys are the code field name and description columns. Values may be vectors, in this case the first
	 * element is selected.
	 */
	public void setValue(Object value, boolean inner) {
		// Value must be the field code. If value is a Hashtable Object then
		// value
		// contains the record data, but it is checked at the cache
		if ((value == null) || (value instanceof NullValue)) {
			this.deleteData();
			return;
		}

		if ((value instanceof SearchValue) && this.isAdvancedQueryMode()) {
			this.initializeDeployedList();
			this.setQueryValues((List<Object>) ((SearchValue) value).getValue());
			this.fireValueChanged(this.getInnerValue(), this.getValue(), ValueEvent.PROGRAMMATIC_CHANGE);
			return;
		}

		this.updateDataIfDeployedListIsNotNull();
		this.setInnerListenerEnabled(false);
		try {

			this.setInnerListenerEnabled(false);
			Object oPreviousValue = this.getValue();

			ReferenceComboDataField.logger.debug("{}: setValue(): value: {}  : cachetime {} , usecachemanager: {} , initcacheonsetvalue: {}", this.getAttribute(), value,
					this.cacheTime, this.useCacheManager, this.initCacheOnSetValue);

			// There are cache data.
			if (this.cacheTime > 0) {
				if (!this.setCacheTimeValue(value, inner, oPreviousValue)) {
					return;
				}
			} else {
				// If there is not cache, value contains the data. It must be a
				// Hashtable object.
				// If value is not a Hashtable then this must be the code and
				// then
				// performs a query.
				if (value instanceof Map) {
					if (!((Map<?,?>) value).isEmpty()) {
						// Value contains the data: code and requested columns.
						this.dataCache = new EntityResultMapImpl(new HashMap<>((Map<?,?>)value));
						List<?> vCodes = (List<?>) ((Map<?,?>) value).get(this.code);
						((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).setDataVector(vCodes);

						// Select the index 0
						Object codV = vCodes.get(0);
						this.checkVCodesOnSetValue(vCodes, codV);
						if (codV != null) {
							this.setCodeFieldWhenCodesNotNull(codV);
						} else {
							this.deleteData();
							return;
						}
						this.saveValueifNotInnerOnSetValue(inner);
						this.setInnerValue(this.valueSave);
					} else {
						((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).removeAllElements();
						this.saveValueifNotInnerOnSetValue(inner);
						this.setInnerValue(this.valueSave);
					}
				} else {
					int index = ((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).getIndexOf(value);
					if (index >= 0) {
						this.setTextValue(value);

						((JComboBox) this.dataField).setSelectedIndex(index);
						this.saveValueifNotInnerOnSetValue(inner);
						this.setInnerValue(this.valueSave);
					} else {// Not found, then query it.
						EntityResult res = this.queryByCod(value);
						if (res.isEmpty()) {
							this.deleteUserData();
							return;
						} else {
							this.dataCache = (EntityResultMapImpl) res;
							Vector vCodes = (Vector) this.dataCache.get(this.code);
							// Update the combo
							((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).setDataVector(vCodes);
							// Select the index
							int codIndex = ((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).getIndexOf(value);

							if (codIndex < 0) {
								this.deleteData();
								return;
							} else {
								this.setTextValue(value);
							}
							((JComboBox) this.dataField).setSelectedIndex(codIndex);
							this.saveValueifNotInnerOnSetValue(inner);
							this.setInnerValue(this.valueSave);
						}
					}
				}
			}
			this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
		} catch (Exception e) {
			ReferenceComboDataField.logger.debug(null, e);
		} finally {
			this.setInnerListenerEnabled(true);
		}
	}

	protected boolean setCacheTimeValue(Object value, boolean inner, Object oPreviousValue) {
		Object oValue = this.getTypedInnerValue(value);
		long t = System.currentTimeMillis();
		long timeSinceLastQuery = t - this.getLastCacheTime();
		this.checkCacheTimeOnSetValue(timeSinceLastQuery);
		if ((!this.dataCacheInitialized) && (this.initCacheOnSetValue)) {
			try {
				ReferenceComboDataField.logger.debug("{} : setValue() : value: {}  : Initializing in cache", this.getAttribute(), oValue);
				this.initCache();
			} catch (Exception e) {
				ReferenceComboDataField.logger.debug(null, e);
				this.dataCache = null;
				this.deleteData();
				return false;
			}
		} else {
			this.setValueIfExistCache();

		}

		this.setInnerListenerEnabled(false);
		int index = ((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).getIndexOf(value);
		if (index >= 0) {
			this.setValueIfDataExist(value, inner, index);
		} else { // Not found. Makes a query.

			this.notFoundCacheComponentLog(value);

			EntityResult res = this.queryByCod(value);
			if (res.isEmpty() || (res.getCode() == EntityResult.OPERATION_WRONG)) {
				this.deleteData();
				return false;
			} else {
				if ((this.dataCache.isEmpty()) || !this.dataCache.containsKey(this.code)) {
					this.dataCache = (EntityResultMapImpl) res;
				}
				// Put it in the cache.
				Vector vCodes = (Vector) this.dataCache.get(this.code);
				int codIndex = vCodes.indexOf(value);
				if (codIndex >= 0) {
					this.setCodIndexValue(value, inner, oPreviousValue, vCodes, codIndex);
					return false;
				} else {
					ReferenceComboDataField.logger.debug("{} :setValue() : value: {} : Code is not stored in cache", this.getAttribute(), oValue);

					if (!((Vector) res.get(this.code)).contains(value)) {
						ReferenceComboDataField.logger.debug("{} :setValue() : value: {} : In codes of query result is not stored the code ", this.getAttribute(), value);
						this.deleteData();
						return false;
					}
					// TODO REVIEW Delete field because doesn't exist.
					// Put it in the cache because it is not in
					this.putCacheOnSetValue(res);
					vCodes = (Vector) this.dataCache.get(this.code);
					((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).setDataVector(vCodes);
					// Select the index
					codIndex = ((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).getIndexOf(value);
					((JComboBox) this.dataField).setSelectedIndex(codIndex);
					this.codeField.setText(value.toString());
					this.saveValueifNotInnerOnSetValue(inner);
					this.setInnerValue(this.valueSave);
				}
			}
		}

		return true;
	}

	protected void setCodIndexValue(Object value, boolean inner, Object oPreviousValue, Vector vCodes, int codIndex) {
		this.setTextCodeFieldOnSetValue(value, codIndex);
		// Update the combo field
		((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).setDataVector(vCodes);
		codIndex = ((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).getIndexOf(value);

		// Select the index
		((JComboBox) this.dataField).setSelectedIndex(codIndex);
		this.saveValueifNotInnerOnSetValue(inner);
		this.setInnerValue(this.valueSave);
		this.fireValueChanged(this.getInnerValue(), oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
		this.setInnerListenerEnabled(true);
	}

	protected void setTextValue(Object value) {
		if (this.codeQueryField == null) {
			this.codeField.setText(value.toString());
		} else {
			// Set the code field value when cachetime is 0 and
			// codsearch is not null
			List<?> vCodes = (List<?>) this.dataCache.get(this.code);
			int cacheIndex = vCodes.indexOf(value);
			List<?> v = (List<?>) this.dataCache.get(this.codeQueryField);
			Object v2 = v.get(cacheIndex);
			if (v2 != null) {
				this.codeField.setText(v2.toString());
			} else {
				this.codeField.setText("");
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param inner
	 */
	protected void saveValueifNotInnerOnSetValue(boolean inner) {
		if (!inner) {
			this.valueSave = this.getValue();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param vCodes
	 * @param codV
	 */
	protected void checkVCodesOnSetValue(List<?> vCodes, Object codV) {
		if ((vCodes == null) || vCodes.isEmpty() || (codV == null)) {
			((JComboBox) this.dataField).setSelectedIndex(0);
		} else {
			((JComboBox) this.dataField).setSelectedItem(codV);
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param codV
	 */
	protected void setCodeFieldWhenCodesNotNull(Object codV) {
		if (this.codeQueryField == null) {
			this.codeField.setText(codV.toString());
		} else {
			Object v2 = ((Vector) this.dataCache.get(this.codeQueryField)).get(0);
			if (v2 != null) {
				this.codeField.setText(v2.toString());
			} else {
				this.codeField.setText("");
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param res
	 */
	protected void putCacheOnSetValue(EntityResult res) {
		Enumeration<?> enumKeys = this.dataCache.keys();
		while(enumKeys.hasMoreElements()) {
			Object oCacheKey = enumKeys.nextElement();
			List<Object> vCacheValues = (List<Object>) this.dataCache.get(oCacheKey);
			List<?> resValues = (List<?>) res.get(oCacheKey);
			if ((resValues != null) && !resValues.isEmpty()) {
				vCacheValues.add(vCacheValues.size(), resValues.get(0));
			} else {
				vCacheValues.add(vCacheValues.size(), null);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param value
	 * @param inner
	 * @param index
	 */
	protected void setValueIfDataExist(Object value, boolean inner, int index) {
		this.setCodeFieldOnSetValueWithData(value);
		((JComboBox) this.dataField).setSelectedIndex(index);
		this.saveValueifNotInnerOnSetValue(inner);
		this.setInnerValue(this.valueSave);
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param value
	 */
	protected void setCodeFieldOnSetValueWithData(Object value) {
		if (this.codeQueryField == null) {
			this.codeField.setText(value.toString());
		} else {
			Vector v = (Vector) this.dataCache.get(this.code);
			int cacheIndex = v.indexOf(value);
			v = (Vector) this.dataCache.get(this.codeQueryField);
			Object v2 = v.get(cacheIndex);
			if (v2 != null) {
				this.codeField.setText(v2.toString());
			} else {
				this.codeField.setText("");
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 */
	protected void initializeDeployedList() {
		if (this.deployedList == null) {
			this.initDeployedList();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 */
	protected void updateDataIfDeployedListIsNotNull() {
		if (this.deployedList != null) {
			this.deployedList.updateData();
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param timeSinceLastQuery
	 */
	protected void checkCacheTimeOnSetValue(long timeSinceLastQuery) {
		if ((timeSinceLastQuery > this.cacheTime) && this.dataCacheInitialized) {
			try {
				this.fireValueEvents = false;
				this.invalidateCache();
				this.setInnerListenerEnabled(false);
			} catch (Exception e) {
				ReferenceComboDataField.logger.trace(null, e);
			} finally {
				this.fireValueEvents = true;
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param value
	 * @param codIndex
	 */
	protected void setTextCodeFieldOnSetValue(Object value, int codIndex) {
		if (this.codeQueryField == null) {
			this.codeField.setText(value.toString());
		} else {
			Vector v = (Vector) this.dataCache.get(this.codeQueryField);
			Object v2 = v.get(codIndex);
			if (v2 != null) {
				this.codeField.setText(v2.toString());
			} else {
				this.codeField.setText("");
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 * @param value
	 */
	protected void notFoundCacheComponentLog(Object value) {
		if (ReferenceComboDataField.logger.isDebugEnabled()) {
			if (this.dataCache.containsKey(this.code)) {
				ReferenceComboDataField.logger.debug("Code cannot be found {} between the values: {}", value, this.dataCache.get(this.code).toString());
			} else {
				ReferenceComboDataField.logger.debug("Or the cache hasn't been initialized or it doesn't contain data for the code: {} and value: {}", this.code, value);
			}
		}
	}

	/**
	 * Method used to reduce the complexity of {@link #setValue(Object, boolean)}
	 *
	 */
	protected void setValueIfExistCache() {
		// If cache exists check if it is valid and use the cache
		// manager.
		if (this.parentkeyCache && this.hasParentKeys()) {
			try {
				this.setInnerListenerEnabled(false);
				this.fireValueEvents = false;
				EntityResult res = this.cacheManager.getDataCache(this.entityName, this.getAttributes(), this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
				this.dataCache = (EntityResultMapImpl) res;
				this.setSelectionComboDataField(res);
			} catch (Exception ex) {
				ReferenceComboDataField.logger.error(null, ex);
			} finally {
				this.fireValueEvents = true;
				this.setInnerListenerEnabled(true);
			}
		}
	}

	protected void useCacheManager() {
		if (this.useCacheManager && (this.cacheManager != null)) {
			if (this.parentkeyCache && this.hasParentKeys()) {
				try {
					this.setInnerListenerEnabled(false);
					this.fireValueEvents = false;
					EntityResult res = this.cacheManager.getDataCache(this.entityName, this.getAttributes(), this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
					this.dataCache = (EntityResultMapImpl) res;
					this.setSelectionComboDataField(res);
				} catch (Exception ex) {
					ReferenceComboDataField.logger.error("{}", ex.getMessage(), ex);
				} finally {
					this.fireValueEvents = true;
					this.setInnerListenerEnabled(true);
				}
			} else if (this.dataCacheInitialized == false) {
				Pair<DataCacheId, EntityResult> pair = this.cacheManager.retrieveDataCache(this.entityName, this.getAttributes(), null, this.cacheTime);
				if (pair != null) {
					try {
						this.setInnerListenerEnabled(false);
						this.fireValueEvents = false;
						EntityResult res = pair.getSecond();
						this.dataCache = (EntityResultMapImpl) res;
						this.dataCacheInitialized = true;
						this.lastCacheTime = pair.getFirst().getTime();
						this.setSelectionComboDataField(res);
					} catch (Exception ex) {
						ReferenceComboDataField.logger.error("{}", ex.getMessage(), ex);
					} finally {
						this.fireValueEvents = true;
						this.setInnerListenerEnabled(true);
					}
				} else {
					// Retrieve cache for next time
					Thread th = new Thread("Initialize cache manager for : " + this.entityName) {
						@Override
						public void run() {
							super.run();
							EntityResult er = ReferenceComboDataField.this.cacheManager.getDataCache(ReferenceComboDataField.this.entityName,
									ReferenceComboDataField.this.getAttributes(), null);
						}
					};
					th.start();
				}
			}
		}
	}

	/**
	 * Selects the last value in the {@link JComboBox} if it is available in the combo after updating the values it will contain as an input parameter.
	 *
	 * @param res
	 *            {@link EntityResult} The new values that the combo will contain.
	 */
	protected void setSelectionComboDataField(EntityResult res) {
		if ((res != null) && (res.isEmpty() == false)) {
			Object value = ((JComboBox) this.dataField).getSelectedItem();
			((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).setDataVector((Vector) res.get(this.code));
			((JComboBox) this.dataField).setSelectedItem(value);
		} else {
			((CustomComboBoxModel) ((JComboBox) this.dataField).getModel()).removeAllElements();
		}
	}

	/**
	 * Sets the code in function of event value. <p>
	 *
	 * @param codeValue
	 *            the value to set
	 * @see #setValue(Object, boolean)
	 * @param valueEventType
	 *            the type of event
	 */
	protected void setCode(Object codeValue, int valueEventType) {
		// Query:
		try {
			if (this.cacheTime > 0) {
				ReferenceComboDataField.logger.debug("setCode(): Code Value: {} with cache", codeValue);
				boolean sameValue = false;
				Object oPreviousValue = this.getInnerValue();
				if ((oPreviousValue == null) && (codeValue == null)) {
					sameValue = true;
				} else if ((oPreviousValue != null) && (codeValue != null) && codeValue.equals(oPreviousValue)) {
					sameValue = true;
				}

				this.fireValueEvents = false;
				try {
					this.setValue(codeValue, true);
				} catch (Exception e) {
					ReferenceComboDataField.logger.trace(null, e);
				}
				this.fireValueEvents = true;
				this.setInnerValue(this.getValue());
				if (!sameValue) {
					this.fireValueChanged(this.getValue(), oPreviousValue, valueEventType);
				}
			} else {
				ReferenceComboDataField.logger.debug("setCode(): Code Value: {} without cache", codeValue);

				boolean sameValue = false;
				Object oPreviousValue = this.getInnerValue();
				if ((oPreviousValue == null) && (codeValue == null)) {
					sameValue = true;
				} else if ((oPreviousValue != null) && (codeValue != null) && codeValue.equals(oPreviousValue)) {
					sameValue = true;
				}

				this.fireValueEvents = false;
				try {
					this.setValue(codeValue, true);
				} catch (Exception e) {
					ReferenceComboDataField.logger.trace(null, e);
				}
				this.fireValueEvents = true;
				this.setInnerValue(this.getValue());
				if (!sameValue) {
					this.fireValueChanged(this.getValue(), oPreviousValue, valueEventType);
				}
			}
		} catch (Exception e) {
			ReferenceComboDataField.logger.debug("Error quering code", e);
		}
	}

	@Override
	public Object getValue() {
		if ((this.advancedQueryMode) && (this.queryValues != null) && !this.queryValues.isEmpty()) {
			return new SearchValue(SearchValue.OR, this.queryValues);
		}
		if (this.isEmpty()) {
			return null;
		}
		Object item = ((JComboBox) this.dataField).getSelectedItem();

		if (item == null) {
			return null;
		}
		return item;
	}

	/**
	 * Pops up the combo when pop-up button is pressed. Perfoms a database query only when cache is not present. <p>
	 *
	 * @throws Exception
	 *             when a Exception occurs
	 */
	protected void popupWillShow() throws Exception {

		long t = System.currentTimeMillis();
		long timeSinceLastQuery = t - this.getLastCacheTime();

		if ((this.advancedQueryMode == true) && (this.queryValues != null) && (this.queryValues.isEmpty() == false)) {
			Object oldValue = this.getValue();
			this.queryValues = null;
			this.fireValueChanged(null, oldValue, ValueEvent.PROGRAMMATIC_CHANGE);
		}

		this.codeField.setEnabled(true);
		this.queryValues = null;

		if ((!this.dataCacheInitialized) || (this.cacheTime == 0) || (timeSinceLastQuery > this.cacheTime)) {
			try {
				this.setInnerListenerEnabled(false);
				this.fireValueEvents = false;
				this.invalidateCache();
			} catch (Exception e) {
				ReferenceComboDataField.logger.trace(null, e);
			} finally {
				this.fireValueEvents = true;
				this.setInnerListenerEnabled(true);
			}
		} else {
			// If cache exists check if it is valid and use the cache manager.
			this.useCacheManager();
		}
	}

	/**
	 * Shows the pop-up combo. Calls to {@link #popupWillShow()} <p>
	 *
	 * @throws Exception
	 *             when Exception occurs
	 */
	protected void listWillShow() throws Exception {
		if (this.isEnabled()) {
			this.popupWillShow();
		}
	}

	/**
	 * Inits cache. It uses cacheManager.
	 */
	public void initCache() {

		if (this.parentkeyCache || (this.parentKeys == null) || this.parentKeys.isEmpty()) {
			// If cache exists then return.
			if ((this.cacheTime != 0) && this.dataCacheInitialized) {
				return;
			}
			if ((this.cacheManager != null) && this.useCacheManager) {
				try {
					long t = System.currentTimeMillis();
					this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// Refresh cache in the cache manager
					this.cacheManager.invalidateCache(this.entityName, this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
					EntityResult res = this.cacheManager.getDataCache(this.entityName, this.getAttributes(), this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
					if (res.getCode() == EntityResult.OPERATION_WRONG) {
						if ((this.parentForm != null) && this.showErrorMessages) {
							this.parentForm.message(res.getMessage(), Form.ERROR_MESSAGE);
						}
						ReferenceComboDataField.logger.warn("Error in petition to CacheManager: {}", res.getMessage());
						return;
					}
					this.dataCache = (EntityResultMapImpl) res;
					this.dataCacheInitialized = true;
					this.setSelectionComboDataField(res);
					this.lastCacheTime = System.currentTimeMillis();
					ReferenceComboDataField.logger.trace("Combo opening time: {}", this.lastCacheTime - t);
					return;
				} catch (Exception exc) {
					ReferenceComboDataField.logger.error("Error using CacheManager: ", exc);
				} finally {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			} else {
				// Continue with the following code(query)
			}
		}

		Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		long t = System.currentTimeMillis();
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Map<Object, Object> hKeysValues = this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences());
			if (this.locator == null) {
				throw new Exception(this.getClass().toString() + " A ReferenceLocator instance hasn't been received --> Use Table.prepareRenderer() o setReferenceLocator()");
			}

			Entity ent = this.locator.getEntityReference(this.entityName);
			if (ent == null) {
				throw new Exception(this.getClass().toString() + " Cannot get an entity reference" + this.entityName);
			}
			EntityResult result = ent.query(hKeysValues, this.getAttributes(), this.locator.getSessionId());
			if (result.getCode() == EntityResult.OPERATION_WRONG) {
				if ((this.parentForm != null) && this.showErrorMessages) {
					this.parentForm.message(result.getMessage(), Form.ERROR_MESSAGE);
				}
				return;
			} else {
				// Now check the net speed
				int iThreshold = ConnectionManager.getCompresionThreshold(result.getBytesNumber(), result.getStreamTime());
				if (iThreshold > 0) {
					ConnectionOptimizer opt = ConnectionManager.getConnectionOptimizer();
					if ((opt != null) && (this.locator instanceof ClientReferenceLocator)) {
						try {
							opt.setDataCompressionThreshold(((ClientReferenceLocator) this.locator).getUser(), this.locator.getSessionId(), iThreshold);
							ReferenceComboDataField.logger.debug("Compression threshold has been established for {} {} in : {}", ((ClientReferenceLocator) this.locator).getUser(),
									this.locator.getSessionId(), iThreshold);
						} catch (Exception exc) {
							ReferenceComboDataField.logger.error("ReferenceComboDataField: Error establishing compression threshold ", exc);
						}
					}
				}
			}
			// Show the window and set the value
			this.dataCache = (EntityResultMapImpl) result;
			this.dataCacheInitialized = true;
			this.setSelectionComboDataField(result);
			this.lastCacheTime = t;
			ReferenceComboDataField.logger.trace("Combo opening time: {}", this.lastCacheTime - t);
			if (ReferenceComboDataField.logger.isDebugEnabled()) {
				ReferenceComboDataField.logger.debug("Data cache Initialized.");
				int size = -1;
				ByteArrayOutputStream bOut = null;
				ObjectOutputStream out = null;
				try {
					bOut = new ByteArrayOutputStream();
					out = new ObjectOutputStream(bOut);
					out.writeObject(result);
					out.flush();
					size = bOut.size();

				} catch (Exception e) {
					ReferenceComboDataField.logger.error(null, e);
				} finally {
					if (bOut != null) {
						bOut.reset();
						bOut.close();
					}
					if (out != null) {
						out.close();
					}
				}
				ReferenceComboDataField.logger.debug("Cache size: {} bytes", size);
			}
		} catch (Exception e) {
			if ((this.parentForm != null) && this.showErrorMessages) {
				this.parentForm.message("interactionmanager.error_in_query", Form.ERROR_MESSAGE, e);
			}
			ReferenceComboDataField.logger.error(null, e);
			ReferenceComboDataField.logger.debug("Query Error. Cannot show results ");
		} finally {
			this.setCursor(cursor);
		}
	}

	/**
	 * Establishes the query <p>
	 *
	 * @param column
	 *            the col name
	 * @param value
	 *            the value to add to keyvalues hashtable, with the form (col,value)
	 * @return the result of query
	 */
	@Override
	public EntityResult queryBy(String column, Object value) {
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		long t = System.currentTimeMillis();
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Map<Object, Object> hKeysValues = this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences());
			hKeysValues.put(column, value);
			Entity ent = this.locator.getEntityReference(this.entityName);
			EntityResult result = ent.query(hKeysValues, this.getAttributes(), this.locator.getSessionId());

			ReferenceComboDataField.logger.trace("ReferenceComboDataField: Query time by code: {}", System.currentTimeMillis() - t);

			if (result.getCode() == EntityResult.OPERATION_WRONG) {
				ReferenceComboDataField.logger.debug("{}", result.getMessage());
				return new EntityResultMapImpl();
			} else {
				// Check net speed
				ConnectionManager.checkEntityResult(result, this.locator);
			}
			return result;
		} catch (Exception exc) {
			ReferenceComboDataField.logger.error(null, exc);
			ReferenceComboDataField.logger.debug("Query error.The results cannot be shown ");
			return new EntityResultMapImpl(EntityResult.OPERATION_WRONG, EntityResult.NODATA_RESULT);
		} finally {
			this.setCursor(cursor);
		}
	}

	/**
	 * Performs a query by cod. <p>
	 *
	 * @param code
	 *            the code to query
	 * @return the query result
	 */
	protected EntityResultMapImpl queryByCod(Object code) {
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		long t = System.currentTimeMillis();
		try {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Map<Object, Object> hKeysValues = this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences());
			hKeysValues.put(this.code, code);
			Entity ent = this.locator.getEntityReference(this.entityName);
			EntityResult result = ent.query(hKeysValues, this.getAttributes(), this.locator.getSessionId());

			ReferenceComboDataField.logger.trace("ReferenceComboDataField: Query time by code: {}", System.currentTimeMillis() - t);

			if (result.getCode() == EntityResult.OPERATION_WRONG) {
				ReferenceComboDataField.logger.debug("{}", result.getMessage());
				return new EntityResultMapImpl(EntityResult.OPERATION_WRONG, EntityResult.NODATA_RESULT);
			} else {
				// Check net speed
				int compresionThreshold = ConnectionManager.getCompresionThreshold(result.getBytesNumber(), result.getStreamTime());
				if (compresionThreshold > 0) {
					ConnectionOptimizer opt = ConnectionManager.getConnectionOptimizer();
					if ((opt != null) && (this.locator instanceof ClientReferenceLocator)) {
						try {
							opt.setDataCompressionThreshold(((ClientReferenceLocator) this.locator).getUser(), this.locator.getSessionId(), compresionThreshold);
							ReferenceComboDataField.logger.debug("Compression threshold has been established for {} {} in : {}", ((ClientReferenceLocator) this.locator).getUser(),
									this.locator.getSessionId(), compresionThreshold);
						} catch (Exception exc) {
							ReferenceComboDataField.logger.error("Error establishing compression threshold ", exc);
						}
					}
				}

			}
			return (EntityResultMapImpl) result;
		} catch (Exception exc) {
			ReferenceComboDataField.logger.error(null, exc);
			ReferenceComboDataField.logger.debug("Query error.The results cannot be shown ", exc);
			return new EntityResultMapImpl(EntityResult.OPERATION_WRONG, EntityResult.NODATA_RESULT);
		} finally {
			this.setCursor(cursor);
		}
	}

	/**
	 * Hides the pop-up combo. In this version is empty.
	 */
	protected void popupWillHide() {
		// ApplicationManager.printCurrentThreadMethods(20);
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator busc) {
		this.locator = busc;
	}

	@Override
	public int getSQLDataType() {
		if (this.codeNumber) {
			return ParseUtils.getSQLType(this.codeNumberClass, ParseTools.INTEGER_);
		}
		if (!this.integerValue) {
			return Types.VARCHAR;
		} else {
			return Types.INTEGER;
		}
	}

	@Override
	public void setEnabled(boolean en) {

		boolean enabled = en;

		if (en) {
			boolean permission = this.checkEnabledPermission();
			if (!permission) {
				this.setEnabled(false);
				return;
			}
		}

		if ((this.parentKeys != null) && (this.parentKeys.size() > 0) && this.disableonparentkeynull) {
			for (Object element : this.parentKeys) {
				Object dataFieldValue = this.parentForm.getDataFieldValue(element.toString());
				if (dataFieldValue == null) {
					enabled = false;
					break;
				}
			}
		}

		super.setEnabled(enabled);
		if (this.detailButton != null) {
			this.detailButton.setEnabled(enabled);
		}
		if (this.codeField != null) {
			this.codeField.setEnabled(enabled);
		}
		if (this.deleteButton != null) {
			this.deleteButton.setEnabled(enabled);
		}
		this.updateBackgroundColor();
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		super.setResourceBundle(resources);

		this.resources = resources;
		if ((this.multipleResultWindow != null) && (this.multipleResultWindow.multipleResultTable != null)) {
			this.multipleResultWindow.multipleResultTable.setResourceBundle(resources);
		}
		try {
			if (this.labelText != null) {
				if (resources != null) {
					this.labelComponent.setText(ApplicationManager.getTranslation(this.labelText, resources));
				} else {
					this.labelComponent.setText(this.labelText);
				}
			} else {
				if ((this.attribute != null) && (this.attribute instanceof String)) {
					if (resources != null) {
						this.labelComponent.setText(ApplicationManager.getTranslation(this.attribute.toString(), resources));
					} else {
						this.labelComponent.setText(this.attribute.toString());
					}
				} else {
					if (resources != null) {
						this.labelComponent.setText(ApplicationManager.getTranslation(this.attrAux, resources));
					} else {
						this.labelComponent.setText(this.attrAux);
					}
				}
			}
		} catch (Exception e) {
			ReferenceComboDataField.logger.debug(null, e);
		}
		try {
			if (resources != null) {
				this.updateData.setText(ApplicationManager.getTranslation(ReferenceComboDataField.UPDATE_DATA, resources));
			} else {
				this.updateData.setText(ReferenceComboDataField.UPDATE_DATA);
			}
		} catch (Exception e) {
			ReferenceComboDataField.logger.debug(null, e);
		}

		try {
			if (resources != null) {
				this.advancedQuery.setText(ApplicationManager.getTranslation(AdvancedDataComponent.ADVANCED_QUERY, resources));
			} else {
				this.advancedQuery.setText(AdvancedDataComponent.ADVANCED_QUERY);
			}
		} catch (Exception e) {
			ReferenceComboDataField.logger.debug(null, e);
		}

		if (this.deployedList != null) {
			this.deployedList.setTitle(ApplicationManager.getTranslation(AdvancedDataComponent.ADVANCED_QUERY, resources));
		}

		if (this.detailButtonListener != null) {
			Form form = this.detailButtonListener.getForm();
			if (form != null) {
				form.setResourceBundle(resources);
			}
		}
		if (this.deleteButton != null) {
			this.deleteButton.setToolTipText(ApplicationManager.getTranslation("datafield.reset_field", resources));
		}

		if (this.translate) {
			if (this.renderer != null) {
				this.renderer.setResourceBundle(resources);
			}
		}

		if (this.multilanguage) {
			// Check field in other uses
			try {
				ReferenceComboDataField.this.setInnerListenerEnabled(false);
				if (this.lastCacheTime > 0) {
					ReferenceComboDataField.this.invalidateCache();
				}
			} finally {
				ReferenceComboDataField.this.setInnerListenerEnabled(true);
			}
		}
	}

	/**
	 * Invalidates the data cache.
	 */
	public void invalidateCache() {
		try {
			this.fireValueEvents = false;
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.lastCacheTime = 0;
			Object oValue = this.getValue();
			this.dataCacheInitialized = false;
			this.dataCache = new EntityResultMapImpl();
			if ((this.cacheManager != null) && this.useCacheManager) {
				this.cacheManager.invalidateCache(this.entityName, this.replaceParentkeyByEquivalence(this.getParentkeyEquivalences()));
			}
			this.initCache();
			this.setValue(oValue);
			Object oNewValue = this.getValue();
			if ((oValue == null) && (oNewValue == null)) {
				this.fireValueEvents = true;
				// Event is not fired because the value has not changed
			} else if ((oValue != null) && (oNewValue != null) && (!oValue.equals(oNewValue)) && (!this.multilanguage)) {
				this.fireValueEvents = true;
				this.fireValueChanged(oNewValue, oValue, ValueEvent.PROGRAMMATIC_CHANGE);
			}
		} catch (Exception e) {
			ReferenceComboDataField.logger.error(null, e);
			ReferenceComboDataField.logger.debug("Error updating cache :", e);
		} finally {
			this.fireValueEvents = true;
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	protected void initDeployedList() {
		if (!this.advancedQueryMode) {
			return;
		}
		try {
			this.listWillShow();
		} catch (Exception e) {
			ReferenceComboDataField.logger.error(null, e);
		}
		if (this.deployedList == null) {
			Window w = SwingUtilities.getWindowAncestor(this);
			if (w instanceof Frame) {
				this.deployedList = new DeployedList((Frame) w, this);
			} else {
				this.deployedList = new DeployedList((Dialog) w, this);
			}
		}
		this.deployedList.setResourceBundle(this.resources);
	}

	/**
	 * Performs an advanced query. <p>
	 *
	 * @throws Exception
	 *             when Exception occurs
	 */
	protected void advancedQuery() throws Exception {
		if (!this.advancedQueryMode) {
			return;
		}
		this.listWillShow();
		if (this.deployedList == null) {
			Window w = SwingUtilities.getWindowAncestor(this);
			if (w instanceof Frame) {
				this.deployedList = new DeployedList((Frame) w, this);
			} else {
				this.deployedList = new DeployedList((Dialog) w, this);
			}
		}
		this.deployedList.setResourceBundle(this.resources);
		this.deployedList.setVisible(true);
	}

	@Override
	protected void createPopupMenu() {
		if (this.popupMenu == null) {
			this.popupMenu = new ExtendedJPopupMenu();
			this.addHelpMenuPopup(this.popupMenu);
			this.popupMenu.add(this.updateData);
			this.popupMenu.addSeparator();
			this.popupMenu.add(this.advancedQuery);

			ImageIcon icon = ImageManager.getIcon(ImageManager.REFRESH);
			if (icon != null) {
				this.updateData.setIcon(icon);
			}
			this.updateData.addActionListener(e -> {
				try {
					ReferenceComboDataField.this.setInnerListenerEnabled(false);
					ReferenceComboDataField.this.invalidateCache();
				} finally {
					ReferenceComboDataField.this.setInnerListenerEnabled(true);
				}
			});

			ImageIcon advanceSearchIcon = ImageManager.getIcon(ImageManager.ADVANCE_SEARCH);
			if (advanceSearchIcon != null) {
				this.advancedQuery.setIcon(advanceSearchIcon);
			}
			this.advancedQuery.addActionListener(e -> {
				try {
					ReferenceComboDataField.this.advancedQuery();
				} catch (Exception ex) {
					ReferenceComboDataField.logger.error(null, ex);
				}
			});
		}
	}

	@Override
	protected void installPopupMenuListener() {
		this.dataField.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					ReferenceComboDataField.this.showPopupMenu((Component) e.getSource(), e.getX(), e.getY());
				}
			}
		});

		// Fix problem with MAC Native Look & Feel...Button use all width
		try {
			if (this.dataField.getComponent(0) instanceof JButton) {
				((JButton) this.dataField.getComponent(0)).addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e)) {
							ReferenceComboDataField.this.showPopupMenu((Component) e.getSource(), e.getX(), e.getY());
						}
					}
				});
			}
		} catch (Exception ex) {
			ReferenceComboDataField.logger.trace(null, ex);
		}
	}

	@Override
	protected void showPopupMenu(Component c, int x, int y) {
		this.configurePopupMenuHelp();
		this.advancedQuery.setEnabled(this.advancedQueryMode);
		this.updateData.setVisible(this.cacheTime != 0);
		this.popupMenu.show(c, x, y);
	}

	/**
	 * Returns the associated combo values for a code. <p>
	 *
	 * @param code
	 *            the object where code are specified
	 * @return the key-value pairs
	 */
	public Map<Object, Object> getValuesToCode(Object code) {
		Map<Object, Object> h = new HashMap<>();
		List<?> lCodes = (List<?>) this.dataCache.get(this.code);
		if ((lCodes == null) || lCodes.isEmpty()) {
			return h;
		}
		int index = lCodes.indexOf(code);
		if (index < 0) {
			return h;
		}
		Enumeration keys = this.dataCache.keys();
		while(keys.hasMoreElements()) {
			Object oKey = keys.nextElement();
			List<?> vValues = (List<?>) dataCache.get(oKey);
			Object oValue = vValues.get(index);
			if (oValue != null) {
				h.put(oKey, oValue);
			}
		}
		return h;
	}

	/**
	 * Returns whether cache manager is used. <p>
	 *
	 * @return the useCacheManager condition
	 */
	public boolean isCacheManagerUsed() {
		return this.useCacheManager;
	}

	/**
	 * Sets the cache manager. <p>
	 *
	 * @param cacheManager
	 *            the cache manager condition
	 */
	public void setUseCacheManager(boolean cacheManager) {
		this.useCacheManager = cacheManager;
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> list = super.getTextsToTranslate();
		list.add(ReferenceComboDataField.UPDATE_DATA);
		return list;
	}

	@Override
	public boolean isEmpty() {
		if ((this.advancedQueryMode) && (this.queryValues != null) && !this.queryValues.isEmpty()) {
			return false;
		}
		if (((JComboBox) this.dataField).getSelectedIndex() >= 0) {
			Object oValue = ((JComboBox) this.dataField).getItemAt(((JComboBox) this.dataField).getSelectedIndex());
			if (oValue.equals(CustomComboBoxModel.NULL_SELECTION)) {
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void deleteData() {
		this.updateDataIfDeployedListIsNotNull();
		this.setInnerListenerEnabled(false);
		Object lastValue = this.getValue();
		ComboBoxModel model = ((JComboBox) this.dataField).getModel();
		if ((model instanceof CustomComboBoxModel) && ((CustomComboBoxModel) model).isNullSelection() && (((CustomComboBoxModel) model).getSize() > 0)) {
			((JComboBox) this.dataField).setSelectedIndex(0);
		} else {
			((JComboBox) this.dataField).setSelectedIndex(-1);
		}
		if (this.codeField != null) {
			this.codeField.setText("");
		}
		this.queryValues = null;
		this.valueSave = this.getValue();
		this.setInnerValue(this.valueSave);
		// since 5.3.8. Reset pop-up tip showed when user types in combo
		if (this.dataField instanceof CustomComboBox) {
			((CustomComboBox) this.dataField).resetTipScroll();
		}
		this.fireValueChanged(this.valueSave, lastValue, ValueEvent.PROGRAMMATIC_CHANGE);
		this.setInnerListenerEnabled(true);
	}

	public void deleteUserData() {
		this.updateDataIfDeployedListIsNotNull();
		this.setInnerListenerEnabled(false);
		Object lastValue = this.getInnerValue();
		ComboBoxModel model = ((JComboBox) this.dataField).getModel();
		if ((model instanceof CustomComboBoxModel) && ((CustomComboBoxModel) model).isNullSelection()) {
			((JComboBox) this.dataField).setSelectedIndex(0);
		} else {
			((JComboBox) this.dataField).setSelectedIndex(-1);
		}
		if (this.codeField != null) {
			this.codeField.setText("");
		}
		this.setInnerValue(this.getValue());
		this.fireValueChanged(this.getInnerValue(), lastValue, ValueEvent.USER_CHANGE);
		this.setInnerListenerEnabled(true);
	}

	@Override
	public void setAdvancedQueryMode(boolean enabled) {
		this.advancedQueryMode = enabled;
		this.queryValues = null;
		this.dataField.repaint();
		if ((!this.advancedQueryMode) && (this.deployedList != null)) {
			this.deployedList.setVisible(false);
		}
	}

	@Override
	public String getEntity() {
		return this.entityName;
	}

	@Override
	public List<Object> getAttributes() {
		List<Object> list = (List<Object>) ((ArrayList) this.cols).clone();
		if (!list.contains(this.code)) {
			list.add(this.code);
		}
		return list;
	}

	@Override
	public void setCacheManager(CacheManager cm) {
		this.cacheManager = cm;
	}

	public void setQueryValues(List<Object> values) {
		if (values == null) {
			this.deleteData();
			this.codeField.setEnabled(true);
			return;
		}
		// If values exist, show them and selected them
		this.queryValues = values;
		this.codeField.setEnabled(false);
		((JComboBox) this.dataField).setSelectedIndex(0);
		this.dataField.repaint();
	}

	/**
	 * Gets the code description. <p>
	 *
	 * @param c
	 *            the object to get the description
	 * @return the description
	 */
	public String getCodeDescription(Object c) {
		if ((!this.dataCacheInitialized) && !this.isCodeAvailable(c)) {
			this.initCache();
		}
		return this.renderer.getCodeDescription(c, this.dataCache);
	}

	protected boolean isCodeAvailable(Object value) {
		if ((this.dataCache == null) || this.dataCache.isEmpty()) {
			return false;
		}
		List vCodes = (List) this.dataCache.get(this.code);
		int codIndex = vCodes.indexOf(value);
		return codIndex >= 0;
	}

	@Override
	public String getText() {
		if (this.isEmpty()) {
			return "";
		}
		return this.renderer.getCodeDescription(this.getValue(), this.dataCache);
	}

	/**
	 * Gets the detail button listener. <p>
	 *
	 * @return the detail button listener
	 */
	public ActionListener getDetailButtonListener() {
		return this.detailButtonListener;
	}

	/**
	 * Gets the parent key value. <p>
	 *
	 * @param parentkey
	 *            the parent key name
	 * @return the parent key value
	 */
	protected Object getParentKeyValue(String parentkey) {
		return this.parentForm.getDataFieldValue(parentkey);
	}

	/**
	 * Gets the renderer. <p>
	 *
	 * @return the renderer
	 */
	public MultiColumnComboRenderer getRenderer() {
		return this.renderer;
	}

	/**
	 * Gets the data cache. <p>
	 *
	 * @return the data cache
	 */
	public EntityResult getDataCache() {
		return this.dataCache;
	}

	@Override
	public boolean hasParentKeys() {
		if ((this.parentKeys == null) || this.parentKeys.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public Hashtable getParentKeyValues() {
		if ((this.parentKeys != null) && (this.parentKeys.size() > 0)) {
			Hashtable hKeysValues = new Hashtable();
			for (Object oParentKey : this.parentKeys) {
				Object oParentValue = this.getParentKeyValue(oParentKey.toString());
				ReferenceComboDataField.logger.debug(" Filtering by: {} parentkey with value: {}", oParentKey, oParentValue);
				if (oParentValue != null) {
					hKeysValues.put(oParentKey, oParentValue);
				}
			}
			return hKeysValues;
		}
		return null;
	}

	@Override
	public Vector getParentKeyList() {
		return this.parentKeys;
	}

	/**
	 * Returns a Hashtable with key-value corresponding with result to apply two 'tokenizer' actions over parentkeys parameter. For example, <br> <br>
	 * <code>string="formfieldpk1:equivalententityfieldpk1;formfieldpk2:equivalententityfieldpk2;...;formfieldpkn:equivalententityfieldpkn"</code> <br> <br> returns
	 * <code>Hashtable</code>: <br> <br> { formfieldpk1 equivalententityfieldpk1} <br> { formfieldpk2 equivalententityfieldpk2} <br> { ... ... } <br> { formfieldpkn
	 * equivalententityfieldpkn} <br>
	 *
	 * @param parentkeys
	 *            the string with values
	 * @return <code>Hashtable</code> with key-value
	 */
	public Map<Object, Object> getParentkeyEquivalences() {
		return this.hParentkeyEquivalences;
	}

	public Map<Object, Object> replaceParentkeyByEquivalence(Map<Object, Object> hParentkeyEquivalences) {
		if ((hParentkeyEquivalences == null) || hParentkeyEquivalences.isEmpty()) {
			return new Hashtable();
		}
		Hashtable hParentkeyValues = this.getParentKeyValues();
		if (hParentkeyValues != null) {
			Map<Object, Object> hReplacedParentkeyValues = new HashMap<>();
			hReplacedParentkeyValues.putAll(hParentkeyValues);
			Set values = hParentkeyValues.keySet();
			Iterator itr = values.iterator();
			while (itr.hasNext()) {
				Object key = itr.next();
				Object value = hReplacedParentkeyValues.remove(key);
				hReplacedParentkeyValues.put(hParentkeyEquivalences.get(key), value);
			}
			return hReplacedParentkeyValues;
		}
		return new HashMap<>();
	}

	@Override
	protected void updateBackgroundColor() {
		super.updateBackgroundColor();

		if (this.codeField != null) {
			if (this.requiredBorder != null) {
				if (!this.enabled) {
					this.codeField.setForeground(this.fontColor);
					this.codeField.setBackground(DataComponent.VERY_LIGHT_GRAY);
				} else {
					this.codeField.setBorder(this.required ? BorderManager.getBorder(this.requiredBorder) : this.noRequiredBorder);
					this.codeField.setBackground(this.backgroundColor);
					this.codeField.setForeground(this.fontColor);
				}
			} else if (!DataField.ASTERISK_REQUIRED_STYLE) {
				if (!this.enabled) {
					this.codeField.setBackground(DataComponent.VERY_LIGHT_GRAY);
					this.codeField.setForeground(this.fontColor);
				} else {
					if (this.required) {
						this.codeField.setBackground(DataField.requiredFieldBackgroundColor);
						this.codeField.setForeground(DataField.requiredFieldForegroundColor);
					} else {
						this.codeField.setBackground(this.backgroundColor);
						this.codeField.setForeground(this.fontColor);
					}
				}
			}
		}
	}

	/**
	 * Gets the code field value. <p>
	 *
	 * @return the object with code field value
	 */
	protected Object getCodeFieldValue() {
		return this.getTypedInnerValue(this.codeField.getText());
	}

	/**
	 * Obtains the typed value from parameter. <p>
	 *
	 * @param s
	 *            the object to obtain the type
	 * @return the typed inner value
	 */
	protected Object getTypedInnerValue(Object s) {
		Object oCodeValue = null;
		if ((s == null) || (s.toString().trim().length() == 0)) {
			return null;
		}
		if (this.codeNumber) {
			return ParseUtils.getValueForClassType(s, this.codeNumberClass);
		} else {
			if (this.integerValue) {
				Object oIntValue = null;
				try {
					oIntValue = new Integer(s.toString());
				} catch (Exception e) {
					ReferenceComboDataField.logger.trace(null, e);
					return s;
				}
				oCodeValue = oIntValue;
			} else {
				oCodeValue = s;
			}
		}
		return oCodeValue;
	}

	@Override
	public JTextField getCodeField() {
		return this.codeField;
	}

	public String getCodeSearchField() {
		return this.codeQueryField;
	}

	@Override
	public boolean isCodeFieldVisible() {
		return this.codVisible;
	}

	@Override
	public boolean isCodeSearchVisible() {
		return this.visibleCodeSearch;
	}

	@Override
	public String getCodeFieldName() {
		return this.code;
	}

	@Override
	public String getCodeSearchFieldName() {
		return this.codeQueryField;
	}

	/**
	 * Creates the multiple result window. <p>
	 *
	 * @param t
	 *            the table
	 */
	protected void createMultipleResultsWindow(Table t) {
		if (this.multipleResultWindow == null) {
			this.multipleResultWindow = new MultipleResultWindow(t, this, this.resources);
		}
	}

	/**
	 * Gets the reference to table with multiple results.
	 */
	public Table getMultipleResultTable() {
		return this.multipleResultWindow != null ? this.multipleResultWindow.multipleResultTable : null;
	}

	@Override
	public void setFormBuilder(FormBuilder builder) {
		// t.setFormBuilder(builder);
		if (this.multipleResultWindow != null) {
			this.multipleResultWindow.multipleResultTable.setFormBuilder(builder);
		}
	}

	@Override
	public void setParentForm(Form f) {
		super.setParentForm(f);
		if (this.multipleResultWindow != null) {
			this.multipleResultWindow.multipleResultTable.setParentForm(f);
		}
		this.registerParentkeyValueChangeListeners();
	}

	public void registerParentkeyValueChangeListeners() {
		if (this.isParentkeyListener()) {
			if (this.getParentKeyList() != null) {
				for (Object element : this.getParentKeyList()) {
					DataField field = (DataField) this.parentForm.getDataFieldReference(element.toString());
					if (field != null) {

						field.addValueChangeListener(new ValueChangeListener() {

							@Override
							public void valueChanged(ValueEvent e) {
								// check event type
								if (ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_ALL.equals(ReferenceComboDataField.this.parentkeyListenerEvent)) {
									this.doAction(e);
								} else if ((e.getType() == ValueEvent.PROGRAMMATIC_CHANGE) && ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_PROGRAMMATIC
										.equals(ReferenceComboDataField.this.parentkeyListenerEvent)) {
									this.doAction(e);
								} else if ((e.getType() == ValueEvent.USER_CHANGE) && ReferenceComboDataField.PARENTKEY_LISTENER_EVENT_USER
										.equals(ReferenceComboDataField.this.parentkeyListenerEvent)) {
									this.doAction(e);
								}
							}

							protected void doAction(ValueEvent e) {
								if ((e.getOldValue() == null) || ((e.getOldValue() != null) && !(e.getOldValue().equals(e.getNewValue())))) {
									if (e.getNewValue() != null) {
										ReferenceComboDataField.this.deleteData();
										ReferenceComboDataField.this.setEnabled(true);
									} else {
										if ((e.getOldValue() != null) && ReferenceComboDataField.this.disableonparentkeynull) {
											// 5.2074EN - setEnabled(false)
											// disabled
											// advancedquerymode forever
											boolean keepAdvancedQueryMode = ReferenceComboDataField.this.isAdvancedQueryMode();
											ReferenceComboDataField.this.setEnabled(false);
											ReferenceComboDataField.this.setAdvancedQueryMode(keepAdvancedQueryMode);
											ReferenceComboDataField.this.deleteData();
										}
									}
								}
							}
						});
					}
				}
			}
		}
	}

	public boolean isParentkeyListener() {
		return this.parentkeyListener;
	}

	public void setParentkeyListener(boolean parentkeyListener) {
		this.parentkeyListener = parentkeyListener;
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
		if ((this.multipleResultWindow != null) && (this.multipleResultWindow.multipleResultTable != null)) {
			this.multipleResultWindow.multipleResultTable.setParentFrame(parentFrame);
		}
	}

	@Override
	protected void setInnerListenerEnabled(boolean enabled) {
		super.setInnerListenerEnabled(enabled);
		this.codeFieldListener.setEnabled(enabled);
	}

	@Override
	protected void installInnerListener() {
		this.innerListener = new InnerListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (this.innerListenerEnabled) {
					ReferenceComboDataField.this.setCode(ReferenceComboDataField.this.getValue(), ValueEvent.USER_CHANGE);
				}
			}
		};
		super.installInnerListener();
	}

	@Override
	public int getTemplateDataType() {
		return ITemplateField.DATA_TYPE_FIELD;
	}

	@Override
	public Object getTemplateDataValue() {
		return this.getText();
	}

}
