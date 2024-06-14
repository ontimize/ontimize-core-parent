package com.ontimize.gui.button;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.ConnectionManager;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/**
 * <p> This button allows to query an entity and fill some form data fields with the query result. The form values in the query moment are used as filter values. <BR> This class
 * uses Button parameters and Table parameters. the 'table_key' parameter is used as key for the table because there is a conflict with the 'key' parameter'.<BR> </p> <p> Company:
 * IMATIA </p>
 */

public class QueryButton extends Button implements ReferenceComponent {

	private static final Logger			logger						= LoggerFactory.getLogger(QueryButton.class);

	private static String				MISSING_DATA_MESSAGE_KEY	= "value_must_be_entered_message";

	private static final int			MIN_WIDTH_TABLE_WINDOW		= 300;

	protected String					entity						= null;

	protected ArrayList<String>			formAttributes				= new ArrayList<>(5);

	protected ArrayList<String>			queryAttributes				= new ArrayList<>(5);

	protected ArrayList<String>			requiredQueryAttributes		= new ArrayList<>(5);

	protected ArrayList<String>			listenedAttributeList		= new ArrayList<>(5);

	protected EntityReferenceLocator	locator						= null;

	protected Table						table						= null;

	protected JDialog					tableWindow					= null;

	private static String				okKey						= "application.accept";

	protected JButton					oKButton					= null;

	protected final String				ASTERISK					= "*";

	protected boolean					addAsterisk					= false;

	protected boolean					someFieldChangeByUser		= false;

	/**
	 * @param parameters
	 *            This class uses Button parameters and Table parameters. the 'table_key' parameter is used as key for the table because there is a conflict with the 'key'
	 *            parameter'. Additional parameters: <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>entity</td> <td></td> <td></td> <td>yes</td> <td>Associated entity.</td> </tr> <tr>
	 *            <td>source</td> <td>attr1;...;attrN</td> <td></td> <td>yes</td> <td>Data field attributes separated by ';'. This fields will be filled with the query result</td>
	 *            </tr> <tr> <td>search</td> <td><i></td> <td></td> <td>yes</td> <td>Field attributes used in the query to show the table.</td> </tr> <tr> <td>searchrequired</td>
	 *            <td></td> <td></td> <td>no</td> <td>Field attributes (separated by ';'), which are specified in 'search' and are necessary to make the query of data that will be
	 *            shown in the table.It allows to force to intoduce search criterion</td> </tr> <tr> <td>addasterisk</td> <td>yes,no</td> <td>no</td> <td>no</td> <td>Specifies if
	 *            it automatically includes an * at the end of the string value when it searches for the query attributes</td> </tr> <tr> <td>listen</td> <td></td> <td>The same as
	 *            'search'</td> <td>no</td> <td>Field attributes (subset as 'source') to listen for the button. If some of this fields changes the record select window will be
	 *            shown</td> </tr> <tr> <td>table_key</td> <td></td> <td></td> <td>yes</td> <td>Name of the table primary key</td> </tr>
	 *
	 *
	 *            </table> <br> </tr> <![if supportMisalignedColumns]> </table>
	 */
	public QueryButton(Map<Object, Object> parameters) {
		super(parameters);
		this.setMargin(new Insets(0, 0, 0, 0));
		Object entity = parameters.get("entity");
		if (entity == null) {
			throw new IllegalArgumentException(this.getClass().toString() + " 'entity' parameter is mandatory");
		}
		this.entity = entity.toString();
		Object source = parameters.get("source");
		if (source == null) {
			throw new IllegalArgumentException(this.getClass().toString() + " 'source' parameter is mandatory");
		}
		StringTokenizer st = new StringTokenizer(source.toString(), ";");
		while (st.hasMoreTokens()) {
			this.formAttributes.add(st.nextToken());
		}
		Object search = parameters.get("search");
		if (search == null) {
			throw new IllegalArgumentException(this.getClass().toString() + " 'search' parameter is mandatory");
		}
		st = new StringTokenizer(search.toString(), ";");
		while (st.hasMoreTokens()) {
			this.queryAttributes.add(st.nextToken());
		}

		Object useasterisk = parameters.get("addasterisk");
		if (useasterisk != null) {
			if (useasterisk.equals("yes")) {
				this.addAsterisk = true;
			}
		}

		// Filter.
		Object searchrequired = parameters.get("searchrequired");
		if (searchrequired != null) {
			st = new StringTokenizer(searchrequired.toString(), ";");
			while (st.hasMoreTokens()) {
				String at = st.nextToken();
				if (this.queryAttributes.contains(at)) {
					this.requiredQueryAttributes.add(at);
				}
			}
		}

		// Listen
		Object listen = parameters.get("listen");
		if (listen != null) {
			st = new StringTokenizer(listen.toString(), ";");
			while (st.hasMoreTokens()) {
				String at = st.nextToken();
				if (this.formAttributes.contains(at)) {
					this.listenedAttributeList.add(at);
				}
			}
		} else {
			this.listenedAttributeList = new ArrayList<>(this.queryAttributes);
		}

		Object tablekey = parameters.get("table_key");
		if (tablekey == null) {
			throw new IllegalArgumentException(this.getClass().toString() + " 'table_key' parameter is mandatory");
		}
		parameters.put("key", tablekey);
		parameters.put("numrowscolumn", "no");
		if (!parameters.containsKey("autoadjustheader")) {
			parameters.put("autoadjustheader", "no");
		}
		if (!parameters.containsKey("rows")) {
			parameters.put("rows", "15");
		}
		try {
			long tIni = System.currentTimeMillis();
			this.table = new Table(parameters);
			this.table.getJTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.table.getJTable().getSelectionModel().addListSelectionListener(e -> {
				if (!e.getValueIsAdjusting()) {
					if (QueryButton.this.table.getJTable().getSelectedRowCount() > 0) {
						if (QueryButton.this.oKButton != null) {
							QueryButton.this.oKButton.setEnabled(true);
						}
					} else {
						if (QueryButton.this.oKButton != null) {
							QueryButton.this.oKButton.setEnabled(false);
						}
					}
				}
			});
			if (!this.table.hasForm()) {
				this.table.getJTable().addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							e.consume();
							if (QueryButton.this.table.getSelectedRow() >= 0) {
								QueryButton.this.oKButton.doClick(10);
							}
						}
					}
				});
			}
			this.table.packTable();
			if (ApplicationManager.DEBUG_TIMES) {
				QueryButton.logger.debug("QueryButton: Table creation time: " + (System.currentTimeMillis() - tIni));
			}
		} catch (Exception e) {
			QueryButton.logger.error("Table has not been created.", e);
		}
		super.addActionListener(e -> QueryButton.this.processingAction());
	}

	protected void processingAction() {
		// Evaluates if filter is necessary.
		for (Object element : this.requiredQueryAttributes) {
			String atr = element.toString();

			if (this.parentForm.isEmpty(atr)) {
				DataComponent cData = this.parentForm.getDataFieldReference(atr);
				String sText = atr;
				if ((cData != null) && (cData instanceof DataField)) {
					if (((DataField) cData).getLabelText() != null) {
						sText = ((DataField) cData).getLabelText();
					}
				}
				Object[] args = { ApplicationManager.getTranslation(sText, QueryButton.this.resourcesFileName) };
				String sTranslation = ApplicationManager.getTranslation(QueryButton.MISSING_DATA_MESSAGE_KEY, QueryButton.this.resourcesFileName, args);
				this.parentForm.message(SwingUtilities.getWindowAncestor(QueryButton.this), sTranslation, Form.WARNING_MESSAGE);
				this.deleteDataFields();
				this.requestFocus();
				return;
			}
		}
		if (this.tableWindow == null) {
			this.createTableWindow();
		}
		// Now executes the query
		try {
			Entity ent = this.locator.getEntityReference(this.entity);
			Map<Object, Object> kv = new HashMap<>();
			for (String element : this.queryAttributes) {
				Object v = this.parentForm.getDataFieldValue(element);
				if (v != null) {
					if ((v instanceof String) && (this.addAsterisk) && (((String) v).indexOf(this.ASTERISK) < 0)) {
						v = v + this.ASTERISK;
					}
					kv.put(element, v);
				}
			}

			EntityResult res = ent.query(kv, this.table.getAttributeList(), this.locator.getSessionId());
			if (res.getCode() == EntityResult.OPERATION_WRONG) {
				this.parentForm.message(SwingUtilities.getWindowAncestor(QueryButton.this), res.getMessage(), Form.ERROR_MESSAGE);
			} else {
				ConnectionManager.checkEntityResult(res, this.locator);
				// Now look in the form the values of the keys. If these values
				// exist then select the row
				Map<Object, Object> hKeys = new HashMap<>();
				List<String> vKeys = this.table.getKeys();
				boolean all = true;
				for (String vKey : vKeys) {
					Object v = this.parentForm.getDataFieldValue(vKey);
					if (v == null) {
						all = false;
						break;
					} else {
						hKeys.put(vKey, v);
					}
				}
				this.table.setValue(res);
				if (all) {
					this.table.setSelectedRow(this.table.getRowForKeys(hKeys));
				}
				this.showTableWindow();
			}
		} catch (Exception ex) {
			if (ApplicationManager.DEBUG) {
				QueryButton.logger.error(null, ex);
			} else {
				QueryButton.logger.trace(null, ex);
			}
			this.parentForm.message(SwingUtilities.getWindowAncestor(QueryButton.this), ex.getMessage(), Form.ERROR_MESSAGE);
		}
	}

	@Override
	public void setParentForm(Form f) {
		super.setParentForm(f);

		List<String> vListenedAttributes = this.listenedAttributeList;
		for (final String at : vListenedAttributes) {
			DataComponent c = this.parentForm.getDataFieldReference(at);
			if ((c != null) && (c instanceof DataField)) {
				DataField dfField = (DataField) c;
				dfField.addValueChangeListener(e -> {
					// ONLY USER
					if (e.getType() != ValueEvent.USER_CHANGE) {
						return;
					}
					if ((e.getNewValue() == null) && (e.getOldValue() == null)) {
						// The value has not changed
					} else if (e.getNewValue() == null) {
						QueryButton.this.someFieldChangeByUser = true;
					} else if (e.getOldValue() == null) {
						QueryButton.this.someFieldChangeByUser = true;
					} else if (!e.getNewValue().equals(e.getOldValue())) {
						QueryButton.this.someFieldChangeByUser = true;
					}
				});
				dfField.addFocusListener(new FocusAdapter() {

					@Override
					public void focusLost(FocusEvent e) {
						if (ApplicationManager.DEBUG) {
							QueryButton.logger.debug(this.getClass() + " focusLost en campo " + at + " temporary: " + e.isTemporary());
						}
						if (!QueryButton.this.someFieldChangeByUser && (e.isTemporary())) {
							QueryButton.this.processingAction();
						}
					}
				});
			}
		}

	}

	protected void showTableWindow() {
		if (this.tableWindow == null) {
			this.createTableWindow();
		}
		this.tableWindow.pack();
		if (this.tableWindow.getWidth() < QueryButton.MIN_WIDTH_TABLE_WINDOW) {
			this.tableWindow.setSize(QueryButton.MIN_WIDTH_TABLE_WINDOW, this.tableWindow.getHeight());
		}
		this.tableWindow.setVisible(true);
	}

	protected void createTableWindow() {
		Window w = SwingUtilities.getWindowAncestor(this);
		if ((w instanceof Frame) || (w == null)) {
			this.tableWindow = new EJDialog((Frame) w, ApplicationManager.getTranslation(this.entity, this.resourcesFileName), true) {

				@Override
				public void processWindowEvent(WindowEvent evt) {
					super.processWindowEvent(evt);
					if (evt.getID() == WindowEvent.WINDOW_CLOSING) {
						try {
							QueryButton.this.someFieldChangeByUser = false;
							// Set the value for the selected index
							Map<Object, Object> hKeysValues = new HashMap<>();
							List<String> vTableKeys = QueryButton.this.table.getKeys();
							for (String element : QueryButton.this.formAttributes) {
								if (vTableKeys.contains(element) && (QueryButton.this.parentForm.getDataFieldValueFromFormCache(element) != null)) {
									hKeysValues.put(element, QueryButton.this.parentForm.getDataFieldValueFromFormCache(element));
								}
							}
							// If hKeysValues is empty then not data found
							if (hKeysValues.isEmpty()) {
								for (String element : QueryButton.this.formAttributes) {
									QueryButton.this.parentForm.deleteDataField(element);
								}
								return;
							}
							EntityResult res = QueryButton.this.query(hKeysValues);
							if (!res.isEmpty()) {
								Map<?, ?> hData = res.getRecordValues(0);
								QueryButton.this.parentForm.setDataFieldValues(hData);
							} else {
								for (String element : QueryButton.this.formAttributes) {
									QueryButton.this.parentForm.deleteDataField(element);
								}
								return;
							}
						} catch (Exception ex) {
							QueryButton.logger.error(null, ex);
						}
					}
				}
			};
		} else {
			this.tableWindow = new EJDialog((Dialog) w, ApplicationManager.getTranslation(this.entity, this.resourcesFileName), true) {

				@Override
				public void processWindowEvent(WindowEvent e) {
					super.processWindowEvent(e);
					if (e.getID() == WindowEvent.WINDOW_CLOSING) {
						try {
							QueryButton.this.someFieldChangeByUser = false;
							// Set the value for the current selection
							Map<Object, Object> hKVs = new HashMap<>();
							List<String> vTableKeys = QueryButton.this.table.getKeys();
							for (String element : QueryButton.this.formAttributes) {
								if (vTableKeys.contains(element) && (QueryButton.this.parentForm.getDataFieldValueFromFormCache(element) != null)) {
									hKVs.put(element, QueryButton.this.parentForm.getDataFieldValueFromFormCache(element));
								}
							}
							// If hKVs is empty then not data found
							if (hKVs.isEmpty() || (vTableKeys.size() != hKVs.size())) {
								for (String element : QueryButton.this.formAttributes) {
									QueryButton.this.parentForm.deleteDataField(element);
								}
								return;
							}
							EntityResult res = QueryButton.this.query(hKVs);
							if (!res.isEmpty()) {
								Map<?, ?> hData = res.getRecordValues(0);
								QueryButton.this.parentForm.setDataFieldValues(hData);
							} else {
								for (String element : QueryButton.this.formAttributes) {
									QueryButton.this.parentForm.deleteDataField(element);
								}
								return;
							}
						} catch (Exception ex) {
							QueryButton.logger.error(null, ex);
						}
					}
				}
			};
		}
		JPanel jbButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		this.tableWindow.getContentPane().add(jbButtonsPanel, BorderLayout.SOUTH);
		this.tableWindow.getContentPane().add(this.table);
		this.oKButton = new JButton(ApplicationManager.getTranslation(QueryButton.okKey, this.resourcesFileName));
		this.oKButton.setIcon(ImageManager.getIcon(ImageManager.OK));
		jbButtonsPanel.add(this.oKButton);
		this.tableWindow.pack();
		ApplicationManager.center(this.tableWindow);
		this.oKButton.addActionListener(e -> {
			// Selected row
			int iSelectedRow = QueryButton.this.table.getSelectedRow();
			if (iSelectedRow >= 0) {
				// Get all data related with this button and clear their
				// values
				QueryButton.this.deleteDataFields();
				for (Object element : QueryButton.this.formAttributes) {
					QueryButton.this.parentForm.deleteDataField(element.toString());
				}
				// Now query in function of the table
				Map<Object, Object> hKv = QueryButton.this.table.getRowData(iSelectedRow);
				Iterator<?> enumKeys = hKv.keySet().iterator();
				// Set the values
				while (enumKeys.hasNext()) {
					Object oKey = enumKeys.next();
					if (QueryButton.this.formAttributes.contains(oKey)) {
						QueryButton.this.parentForm.setDataFieldValue(oKey, hKv.get(oKey));
					}
				}
				QueryButton.this.someFieldChangeByUser = false;
				QueryButton.this.tableWindow.setVisible(false);
			}
		});
	}

	private void deleteDataFields() {
		for (Object element : this.formAttributes) {
			this.parentForm.deleteDataField(element.toString());
		}
	}

	public void addActionListener(ActionEvent e) {

	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator locator) {
		this.locator = locator;
	}

	@Override
	public void setResourceBundle(ResourceBundle res) {
		super.setResourceBundle(res);
		try {
			if (this.table != null) {
				this.table.setResourceBundle(res);
			}
			this.oKButton.setText(ApplicationManager.getTranslation(QueryButton.okKey, res));
		} catch (Exception e) {
			QueryButton.logger.trace(null, e);
		}
	}

	protected EntityResult query(Map<?, ?> kv) throws Exception {
		Entity ent = this.locator.getEntityReference(this.entity);
		EntityResult res = ent.query(kv, this.table.getAttributeList(), this.locator.getSessionId());
		if (EntityResult.OPERATION_WRONG != res.getCode()) {
			ConnectionManager.checkEntityResult(res, this.locator);
		}
		return res;
	}

}
