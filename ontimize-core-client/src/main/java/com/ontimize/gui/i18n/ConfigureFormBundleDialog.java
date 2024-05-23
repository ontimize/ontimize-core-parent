package com.ontimize.gui.i18n;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MainApplication;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.container.Column;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.container.Row;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.Label;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.CellEditor;
import com.ontimize.gui.table.CellRenderer;
import com.ontimize.gui.table.ComboCellEditor;
import com.ontimize.gui.table.EditingVetoException;
import com.ontimize.gui.table.Table;
import com.ontimize.gui.table.TableButton;
import com.ontimize.gui.table.TableEditionEvent;
import com.ontimize.gui.table.TableEditorListener;
import com.ontimize.gui.table.TableSorter;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.gui.i18n.DatabaseBundleValues;
import com.ontimize.jee.common.gui.i18n.IDatabaseBundleManager;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.xml.DefaultXMLParametersManager;

public class ConfigureFormBundleDialog extends EJDialog implements IConfigureFormBundleGUI {

	private static final Logger logger = LoggerFactory.getLogger(ConfigureFormBundleDialog.class);

	private static final String MESSAGE_BUNDLE_SAVE_CHANGES = "bundle.save_changes";

	private static final String MESSAGE_BUNDLE_BUNDLE_CLASS_REQUIRED = "bundle.bundleClass_required";

	public static final String MESSAGE_BUNDLE_ALL_TRANSLATIONS_REQUIRED = "bundle.all_translations_required";

	public static final String MESSAGE_BUNDLE_NOT_REMOVE_TRANSLATION = "bundle.cant_remove_translation";

	public static final String M_WOULD_YOU_LIKE_TO_DELETE_TRANSLATION_ROWS = "bundle.do_you_really_want_to_delete_translation_selected_rows";

	public static final String M_BUNDLE_TRANSLATION_TOOLTIP_TEXT = "bundle.delete_translation";

	public static boolean requiredAllTranslations = false;

	protected Form form;

	protected EntityResult originalValues;

	protected Table confTable;

	protected Button acceptButton;

	protected Button cancelButton;

	protected Button applyButton;

	protected Button refreshButton;

	protected EntityResult tableChanges;

	protected String[] availableLocales;

	protected String remoteObjectName;

	protected ResourceBundle resourceBundle;

	protected JPanel panel;

	protected List<Object>		internationalizationComponents				= new ArrayList<>();

	protected Vector textsToTranslate = new Vector(Arrays.asList(
			new String[] { ConfigureFormBundleDialog.MESSAGE_BUNDLE_ALL_TRANSLATIONS_REQUIRED,
					ConfigureFormBundleDialog.MESSAGE_BUNDLE_BUNDLE_CLASS_REQUIRED,
					ConfigureFormBundleDialog.MESSAGE_BUNDLE_SAVE_CHANGES }));

	protected String originalTitle;

	public static String textKeyColumn = "textKey";

	public static String table_attr = "table_conf_bundle";

	public static String bundleClassColumn = "bundleClass";

	public ConfigureFormBundleDialog(Frame owner, String title, boolean modal, String remoteObjectName)
			throws Exception {
		super(owner, title, modal);
		this.remoteObjectName = remoteObjectName;
		this.originalTitle = title;
	}

	public ConfigureFormBundleDialog(Dialog owner, String title, boolean modal, String remoteObjectName)
			throws Exception {
		super(owner, title, modal);
		this.remoteObjectName = remoteObjectName;
		this.originalTitle = title;
	}

	protected void createGUI() throws Exception {
		this.panel = this.buildPanel();

		Component title = this.buildTitle();
		if (title != null) {
			if (title instanceof FormComponent) {
				this.panel.add(title, ((FormComponent) title).getConstraints(this.panel.getLayout()));
			} else {
				this.panel.add(title);
			}
		}

		Component tablePanel = this.buildTablePanel();

		if (tablePanel != null) {
			if (tablePanel instanceof FormComponent) {
				this.panel.add(tablePanel, ((FormComponent) tablePanel).getConstraints(this.panel.getLayout()));
			} else {
				this.panel.add(tablePanel);
			}
		}

		Component buttonsPanel = this.buildButtons(true, true, true, true);
		if (buttonsPanel != null) {
			if (buttonsPanel instanceof FormComponent) {
				this.panel.add(buttonsPanel, ((FormComponent) buttonsPanel).getConstraints(this.panel.getLayout()));
			} else {
				this.panel.add(buttonsPanel);
			}
		}

		this.add(this.panel);
		this.setSize(new Dimension(600, 500));
		this.pack();
	}

	protected Component buildTablePanel() throws Exception {
		this.confTable = this.buildTable();
		Map<Object, Object> hParams = DefaultXMLParametersManager.getParameters(Row.class.getName());
		hParams.put("margin", "5;5;5;5");
		hParams.put("expand", "yes");
		Row row = new Row(hParams);
		row.add(this.confTable, this.confTable.getConstraints(row.getLayout()));
		return row;

	}

	protected Component buildTitle() {
		Map<Object, Object> hParams = DefaultXMLParametersManager.getParameters(Row.class.getName());

		hParams.put("margin", "5;5;5;5");
		hParams.put("bgcolor", "161;173;187");
		hParams.put("opaque", "yes");
		hParams.put("expand", "no");
		Row row = new Row(hParams);

		hParams.clear();
		hParams = DefaultXMLParametersManager.getParameters(Label.class.getName());
		hParams.put(Label.ATTR, "bundle.title_label");
		hParams.put(Label.TEXT, "bundle.title_label");
		hParams.put(Label.ALIGN, Label.CENTER);
		Label label = new Label(hParams);

		row.add(label);
		this.internationalizationComponents.add(row);
		this.internationalizationComponents.add(label);
		return row;
	}

	protected Component buildButtons(boolean accept, boolean cancel, boolean refresh, boolean reload) {
		Map<Object, Object> hParams = DefaultXMLParametersManager.getParameters(Row.class.getName());
		hParams.put("margin", "5;5;5;5");
		hParams.put("expand", "no");
		Row row = new Row(hParams);

		hParams.clear();
		hParams = DefaultXMLParametersManager.getParameters(Button.class.getName());

		if (reload) {
			hParams.put(Button.KEY, "bundle.reload_button");
			hParams.put(Button.TEXT, "application.apply_reload");
			hParams.put("icon", ImageManager.BUNDLE_ICON);
			this.applyButton = new Button(hParams);
			row.add(this.applyButton);
			this.applyButton.setEnabled(false);
			this.internationalizationComponents.add(this.applyButton);
		}

		if (accept) {
			hParams.put(Button.KEY, "bundle.accept_button");
			hParams.put(Button.TEXT, "application.accept");
			hParams.put("icon", ImageManager.OK);
			this.acceptButton = new Button(hParams);
			this.acceptButton.setEnabled(false);
			row.add(this.acceptButton);
			this.internationalizationComponents.add(this.acceptButton);
		}

		if (cancel) {
			hParams.put(Button.KEY, "bundle.cancel_button");
			hParams.put(Button.TEXT, "application.cancel");
			hParams.put("icon", ImageManager.CANCEL);
			this.cancelButton = new Button(hParams);
			row.add(this.cancelButton);
			this.internationalizationComponents.add(this.cancelButton);
		}

		if (refresh) {
			hParams.put(Button.KEY, "bundle.refresh_button");
			hParams.put(Button.TEXT, "application.refresh");
			hParams.put("icon", ImageManager.REFRESH);
			this.refreshButton = new Button(hParams);
			row.add(this.refreshButton);
			this.internationalizationComponents.add(this.refreshButton);
		}

		this.internationalizationComponents.add(row);
		return row;
	}

	protected JPanel buildPanel() {
		Map<Object, Object> hParams = DefaultXMLParametersManager.getParameters(Column.class.getName());
		hParams.put("margin", "5;5;5;5");
		hParams.put("expandlast", "no");
		Column column = new Column(hParams);
		this.internationalizationComponents.add(column);
		return column;
	}

	protected Table buildTable() throws Exception {
		List<Object> cols = new ArrayList<>();
		cols.add(ConfigureFormBundleDialog.textKeyColumn);
		cols.add(ConfigureFormBundleDialog.bundleClassColumn);

		List<Object> locales = new ArrayList<>(Arrays.asList(this.getAvailableLocales()));
		cols.addAll(locales);
		Map<Object, Object> hParams = DefaultXMLParametersManager.getParameters(Table.class.getName());

		String stringCols = ApplicationManager.vectorToStringSeparateBy(cols, ";");
		hParams.put(Table.COLS, stringCols);
		hParams.put(Table.VISIBLE_COLS, stringCols);
		hParams.put(Table.ENTITY, ConfigureFormBundleDialog.table_attr);
		List<Object> editableColumns = new ArrayList<>(locales);
		editableColumns.add(ConfigureFormBundleDialog.bundleClassColumn);
		hParams.put(Table.EDITABLE_COLUMNS, ApplicationManager.vectorToStringSeparateBy(editableColumns, ";"));
		hParams.put(Table.INSERT_TABLE, "yes");
		hParams.put(Table.DATABASE_INSERT, "no");
		hParams.put(Table.DATABASE_REMOVE, "yes");
		hParams.put(Table.REQUIRED_COLS, stringCols);
		hParams.put(Table.DELETE_BUTTON, "yes");

		Table table = new Table(hParams);
		table.setParentForm(this.form);

		TableButton deletebutton = (TableButton) table.getTableComponentReference(Table.BUTTON_DELETE);
		for (ActionListener l : deletebutton.getActionListeners()) {
			deletebutton.removeActionListener(l);
		}
		deletebutton.setToolTipText(ConfigureFormBundleDialog.M_BUNDLE_TRANSLATION_TOOLTIP_TEXT);
		deletebutton.addActionListener(e -> {
			Table tableBundle = (Table) SwingUtilities.getAncestorOfClass(Table.class, (TableButton) e.getSource());
			int option = tableBundle.getParentForm()
					.message(ConfigureFormBundleDialog.M_WOULD_YOU_LIKE_TO_DELETE_TRANSLATION_ROWS,
							Form.QUESTION_MESSAGE);
			if (option == Form.YES) {

				for (int i = 0; i < tableBundle.getSelectedRowsNumber(); i++) {
					String key = (String) ((List<?>) tableBundle.getSelectedRowData()
							.get(ConfigureFormBundleDialog.textKeyColumn)).get(i);
					String bundleClassName = (String) ((List<?>) tableBundle
							.getSelectedRowData()
							.get(ConfigureFormBundleDialog.bundleClassColumn)).get(i);

					try {
						ConfigureFormBundleDialog.this.deleteTranslationForKey(key, bundleClassName);

					} catch (Exception e1) {
						ConfigureFormBundleDialog.logger.error(null, e1);
						MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor(tableBundle),
								ApplicationManager.getTranslation(
										ConfigureFormBundleDialog.MESSAGE_BUNDLE_NOT_REMOVE_TRANSLATION));
					}
				}

				ConfigureFormBundleDialog.this.refresh();
			}
		});

		String formResourceFileName = this.form.getResourceFileName();
		String formManagerResourceFileName = this.form.getFormManager().getResourceFileName();
		String applicationResourceFileName = ((MainApplication) this.form.getFormManager().getApplication())
				.getResourcesFileName();

		List<Object> values = new ArrayList<>();
		if (formResourceFileName != null) {
			values.add(formResourceFileName);
		}
		if (formManagerResourceFileName != null) {
			values.add(formManagerResourceFileName);
		}
		if (applicationResourceFileName != null) {
			values.add(applicationResourceFileName);
		}
		Map<Object, Object> h = new HashMap<>();
		h.put(CellEditor.COLUMN_PARAMETER, ConfigureFormBundleDialog.bundleClassColumn);
		ComboCellEditor cce = new CustomComboCellEditor(h, this.confTable) {

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
					int column) {
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
		};
		cce.setValues(values);

		table.setRendererForColumn(ConfigureFormBundleDialog.bundleClassColumn, new CustomStringCellRenderer());

		table.setColumnEditor(ConfigureFormBundleDialog.bundleClassColumn, cce);

		this.addComponentsToInternationalizeList(table);

		return table;
	}

	@Override
	public void setForm(Form form) throws Exception {
		this.form = form;

		this.createGUI();

		this.setSizePositionPreference(
				("bundledialog." + form.getArchiveName()) != null ? form.getArchiveName() : form.getFormTitle());

		// Register table listener
		this.tableChanges = new EntityResultMapImpl();
		List<Object> columns = this.confTable.getAttributeList();
		this.originalValues = new EntityResultMapImpl();
		for (Object column : columns) {
			this.tableChanges.put(column, new ArrayList<>());
		}

		this.confTable.addTableEditorListener(new TableEditorListener() {

			@Override
			public void editingCanceled(TableEditionEvent e) {
			}

			@Override
			public void editingStopped(TableEditionEvent e) {
				Hashtable rowValues = ConfigureFormBundleDialog.this.confTable.getRowData(e.getRow());
				if (rowValues != null) {
					Hashtable keys = new Hashtable();
					keys.put(ConfigureFormBundleDialog.textKeyColumn,
							rowValues.get(ConfigureFormBundleDialog.textKeyColumn));
					keys.put(ConfigureFormBundleDialog.bundleClassColumn,
							rowValues.get(ConfigureFormBundleDialog.bundleClassColumn));
					int recordIndex = ConfigureFormBundleDialog.this.tableChanges.getRecordIndex(keys);
					if (recordIndex >= 0) {
						ConfigureFormBundleDialog.this.tableChanges.deleteRecord(recordIndex);
					}
					ConfigureFormBundleDialog.this.tableChanges.addRecord(rowValues);
					if (ConfigureFormBundleDialog.this.acceptButton != null) {
						ConfigureFormBundleDialog.this.acceptButton.setEnabled(true);
					}
					if (ConfigureFormBundleDialog.this.applyButton != null) {
						ConfigureFormBundleDialog.this.applyButton.setEnabled(true);
					}
				}

			}

			@Override
			public void editingWillStop(TableEditionEvent e) throws EditingVetoException {
			}
		});

		this.confTable.addInsertTableInsertRowListener(e -> {
			Hashtable rowData = e.getRowData();
			if (rowData != null) {
				ConfigureFormBundleDialog.this.tableChanges.addRecord(rowData);
				if (ConfigureFormBundleDialog.this.acceptButton != null) {
					ConfigureFormBundleDialog.this.acceptButton.setEnabled(true);
				}
				if (ConfigureFormBundleDialog.this.applyButton != null) {
					ConfigureFormBundleDialog.this.applyButton.setEnabled(true);
				}
			}
		});

		// Register buttons listeners
		if (this.acceptButton != null) {
			this.acceptButton.addActionListener(e -> ConfigureFormBundleDialog.this.accept(true, true));
		}

		if (this.cancelButton != null) {
			this.cancelButton.addActionListener(e -> ConfigureFormBundleDialog.this.cancel());
		}

		if (this.applyButton != null) {
			this.applyButton.addActionListener(e -> ConfigureFormBundleDialog.this.applyChangesReloadBundle());
		}

		if (this.refreshButton != null) {
			this.refreshButton.addActionListener(e -> ConfigureFormBundleDialog.this.refresh());
		}

		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				ConfigureFormBundleDialog.this.cancel();
			}
		});

	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			this.configureValues();
		}
		super.setVisible(visible);
	}

	protected void cancel() {
		if (this.tableChanges.calculateRecordNumber() > 0) {
			boolean saveChanges = this.form.question(ConfigureFormBundleDialog.MESSAGE_BUNDLE_SAVE_CHANGES);
			if (saveChanges) {
				try {
					if (!this.saveChanges()) {
						return;
					}
				} catch (Exception e) {
					ConfigureFormBundleDialog.logger.error(null, e);
				}
			}
		}
		this.clearChanges();
		this.confTable.setValue(this.originalValues);
		this.setVisible(false);
	}

	protected void applyChangesReloadBundle() {
		this.accept(false, true);
	}

	protected void clearChanges() {
		// The entity result with the changes needs always the column names.
		Vector columns = this.confTable.getAttributeList();
		this.tableChanges.clear();
		for (Object column : columns) {
			this.tableChanges.put(column, new Vector());
		}
	}

	protected void refresh() {
		try {
			if (this.tableChanges.calculateRecordNumber() > 0) {
				boolean saveChanges = this.form.question(ConfigureFormBundleDialog.MESSAGE_BUNDLE_SAVE_CHANGES);
				if (saveChanges) {
					if (!this.saveChanges()) {
						return;
					}
				}
			}
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.reload();
			this.configureValues();
		} catch (Exception e) {
			ConfigureFormBundleDialog.logger.error(null, e);
		} finally {
			this.setCursor(Cursor.getDefaultCursor());
		}
	}

	protected void accept(boolean close, boolean reload) {
		try {
			if (this.saveChanges()) {
				if (reload) {
					this.reload();
				}
				if (close) {
					this.setVisible(false);
				}
			}

		} catch (Exception e) {
			ConfigureFormBundleDialog.logger.error(null, e);
			if (this.form != null) {
				this.form.message(e.getMessage(), Form.ERROR_MESSAGE, e);
			}
		}
	}

	protected boolean deleteTranslationForKey(String key, String bundleClassColumn) throws Exception {
		Object remoteReference = ((UtilReferenceLocator) this.form.getFormManager().getReferenceLocator())
				.getRemoteReference(this.remoteObjectName,
						this.form.getFormManager().getReferenceLocator().getSessionId());
		if ((remoteReference != null) && (remoteReference instanceof IDatabaseBundleManager)) {
			DatabaseBundleValues dbbv = new DatabaseBundleValues(this.availableLocales);

			dbbv.addBundleValue(key, bundleClassColumn, new Hashtable<String, Object>());

			((IDatabaseBundleManager) remoteReference).deleteBundleValues(dbbv,
					this.form.getFormManager().getReferenceLocator().getSessionId());
		}
		if (this.acceptButton != null) {
			this.acceptButton.setEnabled(false);
		}
		if (this.applyButton != null) {
			this.applyButton.setEnabled(false);
		}
		this.clearChanges();

		// Update original values
		this.originalValues = (EntityResult) this.confTable.getValue();

		return true;
	}

	protected boolean saveChanges() throws Exception {
		if (this.tableChanges.size() > 0) {
			Object remoteReference = ((UtilReferenceLocator) this.form.getFormManager().getReferenceLocator())
					.getRemoteReference(this.remoteObjectName,
							this.form.getFormManager().getReferenceLocator().getSessionId());
			if ((remoteReference != null) && (remoteReference instanceof IDatabaseBundleManager)) {
				DatabaseBundleValues dbbv = new DatabaseBundleValues(this.availableLocales);

				for (int i = 0; i < this.tableChanges.calculateRecordNumber(); i++) {
					Hashtable h = this.tableChanges.getRecordValues(i);
					Hashtable trans = new Hashtable();
					for (String availableLocale : this.availableLocales) {
						if (h.get(availableLocale) != null) {
							trans.put(availableLocale, h.get(availableLocale));
						} else if (ConfigureFormBundleDialog.requiredAllTranslations) {
							this.form.message(ConfigureFormBundleDialog.MESSAGE_BUNDLE_ALL_TRANSLATIONS_REQUIRED,
									Form.WARNING_MESSAGE);
							return false;
						}
					}

					if (h.get(ConfigureFormBundleDialog.bundleClassColumn) == null) {
						this.form.message(ConfigureFormBundleDialog.MESSAGE_BUNDLE_BUNDLE_CLASS_REQUIRED,
								Form.WARNING_MESSAGE);
						return false;
					}

					dbbv.addBundleValue((String) h.get(ConfigureFormBundleDialog.textKeyColumn),
							(String) h.get(ConfigureFormBundleDialog.bundleClassColumn), trans);
				}

				((IDatabaseBundleManager) remoteReference).updateBundleValues(dbbv,
						this.form.getFormManager().getReferenceLocator().getSessionId());
			}
			if (this.acceptButton != null) {
				this.acceptButton.setEnabled(false);
			}
			if (this.applyButton != null) {
				this.applyButton.setEnabled(false);
			}
			this.clearChanges();

			// Update original values
			this.originalValues = (EntityResult) this.confTable.getValue();
		}
		return true;
	}

	protected void reload() {
		ExtendedPropertiesBundle.reloadBundle();
	}

	protected String[] getAvailableLocales() {
		if (this.form != null) {
			if (this.availableLocales == null) {
				try {
					Object remoteReference = ((UtilReferenceLocator) this.form.getFormManager().getReferenceLocator())
							.getRemoteReference(this.remoteObjectName,
									this.form.getFormManager().getReferenceLocator().getSessionId());
					if ((remoteReference != null) && (remoteReference instanceof IDatabaseBundleManager)) {
						this.availableLocales = ((IDatabaseBundleManager) remoteReference)
								.getAvailableLocales(this.form.getFormManager().getReferenceLocator().getSessionId());
					}
				} catch (Exception e) {
					ConfigureFormBundleDialog.logger.error(null, e);
				}
			}
		}
		return this.availableLocales;
	}

	protected void configureValues() {
		String formResourceFileName = this.form.getResourceFileName();
		String formManagerResourceFileName = this.form.getFormManager().getResourceFileName();
		String applicationResourceFileName = ((MainApplication) this.form.getFormManager().getApplication())
				.getResourcesFileName();

		List textsToTranslate = this.form.getTextsToTranslate();
		String[] locales = this.getAvailableLocales();

		Vector columns = this.confTable.getAttributeList();
		this.originalValues = new EntityResult();
		for (Object column : columns) {
			this.originalValues.put(column, new Vector());
		}

		LanguageResourcesManager lrm = new LanguageResourcesManager(locales, formResourceFileName,
				formManagerResourceFileName, applicationResourceFileName);
		Collections.sort(textsToTranslate);
		// For each text key we must know the resources where it appears and the
		// translations in each available language
		Hashtable lastRecord = null;
		for (int i = 0; i < textsToTranslate.size(); i++) {
			if (textsToTranslate.get(i) instanceof String) {
				String str = (String) textsToTranslate.get(i);
				if ((str != null) && (str.toString().trim().length() > 0)) {
					Hashtable record = lrm.getValuesForKey(str.toString());
					if (record == null) {
						record = new Hashtable();
						record.put(ConfigureFormBundleDialog.textKeyColumn, str);
						if (formResourceFileName != null) {
							record.put(ConfigureFormBundleDialog.bundleClassColumn, formResourceFileName);
						} else if (formManagerResourceFileName != null) {
							record.put(ConfigureFormBundleDialog.bundleClassColumn, formManagerResourceFileName);
						} else {
							record.put(ConfigureFormBundleDialog.bundleClassColumn, applicationResourceFileName);
						}
					}
					// confTable.addRow(record);

					if (!this.compareRecordBundleKeys(lastRecord, record)) {
						this.originalValues.addRecord((Hashtable) record.clone(), i);
						lastRecord = record;
					}
				}
			} else {
				ConfigureFormBundleDialog.logger
				.debug(this.getClass().getName() + " there is an object "
						+ textsToTranslate.get(i).getClass().getName() + " in the list of texts to translate");
			}
		}
		this.confTable.setValue(this.originalValues);
	}

	protected boolean compareRecordBundleKeys(Hashtable record1, Hashtable record2) {
		if ((record1 != null) && (record2 != null)) {
			Object textKey1 = record1.get(ConfigureFormBundleDialog.textKeyColumn);
			Object textKey2 = record2.get(ConfigureFormBundleDialog.textKeyColumn);
			if ((textKey1 != null) && (textKey2 != null) && textKey1.equals(textKey2)) {
				Object bundleClass1 = record1.get(ConfigureFormBundleDialog.bundleClassColumn);
				Object bundleClass2 = record2.get(ConfigureFormBundleDialog.bundleClassColumn);
				if (((bundleClass1 == null) && (bundleClass2 == null))
						|| ((bundleClass1 != null) && (bundleClass2 != null) && bundleClass1.equals(bundleClass2))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Vector getTextsToTranslate() {
		if (this.internationalizationComponents != null) {
			Vector v = new Vector();
			for (Object element : this.internationalizationComponents) {
				Vector textsToTranslate = ((Internationalization) element)
						.getTextsToTranslate();
				if (textsToTranslate != null) {
					v.addAll(textsToTranslate);
				}
			}
			v.addAll(this.textsToTranslate);
			return v;
		}
		return this.textsToTranslate;
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
		this.setTitle(ApplicationManager.getTranslation(this.originalTitle, resourceBundle));
		Component[] components = this.getComponents();
		for (Component component : components) {
			if (component instanceof Internationalization) {
				((Internationalization) component).setResourceBundle(resourceBundle);
			}
			if (component instanceof Container) {
				this.setResourceBundle((Container) component, resourceBundle);
			}
		}

		for (Object element : this.internationalizationComponents) {
			if (element instanceof Internationalization) {
				((Internationalization) element).setResourceBundle(resourceBundle);
			}
		}
	}

	protected void setResourceBundle(Container cont, ResourceBundle res) {
		Component[] components = cont.getComponents();
		for (Component component : components) {
			if (component instanceof Internationalization) {
				((Internationalization) component).setResourceBundle(this.resourceBundle);
			} else if (component instanceof Container) {
				this.setResourceBundle((Container) component, this.resourceBundle);
			}
		}
	}

	public static class LanguageResourcesManager {

		protected LanguageResources formResources;

		protected LanguageResources managerResources;

		protected LanguageResources applicationResources;

		public LanguageResourcesManager(String[] locales, String formResources, String managerResources,
				String applicationResources) {
			if (formResources != null) {
				this.formResources = new LanguageResources(locales, formResources);
			}
			if (managerResources != null) {
				this.managerResources = new LanguageResources(locales, managerResources);
			}
			if (applicationResources != null) {
				this.applicationResources = new LanguageResources(locales, applicationResources);
			}
		}

		public Hashtable getValuesForKey(String textKey) {
			Hashtable result = null;
			if (this.formResources != null) {
				result = this.formResources.getTranslationForKey(textKey);
			}
			if ((result == null) && (this.managerResources != null)) {
				result = this.managerResources.getTranslationForKey(textKey);
			}
			if ((result == null) && (this.applicationResources != null)) {
				result = this.applicationResources.getTranslationForKey(textKey);
			}
			return result;
		}

	}

	protected static class LanguageResources {

		protected String[] locales;

		protected ResourceBundle[] bundles;

		protected String resourcesFileName;

		public LanguageResources(String[] locales, String resourcesFileName) {
			this.locales = locales;
			this.resourcesFileName = resourcesFileName;
			this.build();
		}

		protected void build() {
			this.bundles = new ResourceBundle[this.locales.length];
			for (int i = 0; i < this.locales.length; i++) {
				this.bundles[i] = ExtendedPropertiesBundle.getExtendedBundle(this.resourcesFileName,
						LanguageResources.getLocale(this.locales[i]));
			}
		}

		public static Locale getLocale(String loc) {
			StringTokenizer st = new StringTokenizer(loc.toString(), "_");
			String sCountry = null;
			String language = null;
			String variant = "";// Always initialized

			if (st.hasMoreTokens()) {
				language = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				sCountry = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				variant = st.nextToken();
			}
			if ((language != null) && (sCountry != null)) {
				Locale l = new Locale(language, sCountry, variant);
				return l;
			}
			return null;
		}

		public Hashtable getTranslationForKey(String textKey) {
			Hashtable h = new Hashtable();
			for (int i = 0; i < this.bundles.length; i++) {
				if (ConfigureFormBundleDialog.bundleContainsKey(this.bundles[i], textKey)) {
					h.put(this.locales[i], this.bundles[i].getString(textKey));
				}
			}
			if (!h.isEmpty()) {
				h.put(ConfigureFormBundleDialog.textKeyColumn, textKey);
				h.put(ConfigureFormBundleDialog.bundleClassColumn, this.resourcesFileName);
				return h;
			}
			return null;
		}

	}

	protected static boolean bundleContainsKey(ResourceBundle bundle, String key) {
		// TODO in jvm 1.6 there is a method containskey in ResourceBundle class
		if (key != null) {
			try {
				String result = bundle.getString(key);
				if (result != null) {
					return true;
				}
			} catch (Exception e) {
				ConfigureFormBundleDialog.logger.error(null, e);
			}
		}
		return false;
	}

	protected static class CustomComboCellEditor extends ComboCellEditor {

		protected Table table;

		public CustomComboCellEditor(Map<Object, Object> parameters, Table table) {
			super(parameters);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
				int column) {
			if (((TableSorter) table.getModel()).isInsertingRow(row) || (value == null)) {
				return super.getTableCellEditorComponent(table, value, isSelected, row, column);
			}
			return null;

		}

	}

	protected static class CustomStringCellRenderer extends CellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, selected, hasFocus, row, column);
			((JLabel) c).setText((String) value);
			if (!CellRenderer.isInsertingRow(row, table)) {
				if (value != null) {
					if (selected) {
						c.setBackground(CellRenderer.selectedBackgroundColor);
					} else if ((row % 2) == 0) { // odd row
						c.setBackground(CellRenderer.oddRowBackgroundColor);
					} else {
						c.setBackground(CellRenderer.evenRowBackgroundColor);
					}
				}
			}
			return c;
		}

	}

	protected void addComponentsToInternationalizeList(Component component) {
		if (component instanceof Internationalization) {
			this.internationalizationComponents.add(component);
		}
		if (component instanceof Container) {
			Component[] components = ((Container) component).getComponents();
			for (Component component2 : components) {
				if (component2 instanceof Container) {
					this.addComponentsToInternationalizeList(component2);
				} else if (component2 instanceof Internationalization) {
					this.internationalizationComponents.add(component2);
				}
			}
		}
	}

}
