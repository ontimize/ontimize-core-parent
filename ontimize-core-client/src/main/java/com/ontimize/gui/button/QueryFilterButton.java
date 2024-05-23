package com.ontimize.gui.button;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ExtendedJPopupMenu;
import com.ontimize.gui.Form;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.InteractionManagerModeEvent;
import com.ontimize.gui.InteractionManagerModeListener;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.IFilterElement;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.jee.common.gui.field.ReferenceFieldAttribute;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.util.serializer.ISerializerManager;
import com.ontimize.jee.common.util.serializer.SerializerManagerFactory;
import com.ontimize.util.swing.MenuButton;

public class QueryFilterButton extends AbstractButtonSelection
implements InteractionManagerModeListener, ReferenceComponent {

	private static final Logger logger = LoggerFactory.getLogger(QueryFilterButton.class);

	protected static final String FORM_QUERY_FILTER_PREFERENCE_KEY = "form_query_filter";

	// Preferences format is: default~list~;name1;name2;name3;name4
	protected static final String PREFERENCE_DEFAULT_KEY = "default~";

	protected static final String PREFERENCE_LIST_KEY = "list~";

	protected static final String NO_RESULT_MESSAGE = "queryfilterbutton.no_result";

	protected static final String QUERY_FILTER_EXIST_QUESTION = "queryfilterbutton.query_filter_name_exists";

	protected static final String INSERT_FILTER_NAME_MESSAGE = "queryfilterbutton.insert_filter_name";

	protected static final String NO_DATA_FOR_FILTER_MESSAGE = "queryfilterbutton.no_data_for_filter_message";

	protected static final String DELETE_KEY = "queryfilterbutton.delete_key";

	protected JPopupMenu filterMenu = null;

	protected ItemListener itemListener = null;

	protected DefaultItemListener defaultItemListener = null;

	protected ItemDeleteListener itemDeleteListener = null;

	protected ItemSaveListener itemSaveListener = null;

	protected EntityReferenceLocator locator = null;

	protected JMenuItem insertMenuItem = null;

	protected ISerializerManager serializerManager = SerializerManagerFactory.getSerializerManager();

	public QueryFilterButton(Map<Object, Object> parameter) {
		super(parameter);
		this.jInit();
	}

	@Override
	public void init(Map<Object, Object> parameter) {
		super.init(parameter);
		if (this.icon == null) {
			this.icon = ImageManager.FUNNEL_NEW;
		}
		this.button.setIcon(ImageManager.getIcon(this.icon));
	}

	protected void jInit() {
		this.menuButton.addActionListener(event -> {
			QueryFilterButton.this.createFilterMenu();
			QueryFilterButton.this.filterMenu.show(QueryFilterButton.this.menuButton, 0,
					QueryFilterButton.this.menuButton.getHeight());
		});
		this.itemSaveListener = new ItemSaveListener(this.bundle);
		this.addActionListener(this.itemSaveListener);
		this.setMargin(new Insets(3, 3, 4, 3));
	}

	@Override
	public void interactionManagerModeChanged(InteractionManagerModeEvent event) {
		if (InteractionManager.QUERY == event.getInteractionManagerMode()) {
			String defaultFilter = this.getDefaultQueryFilter();
			if (defaultFilter != null) {
				this.performFilter(defaultFilter);
			}
			this.setEnabled(true);
		} else {
			this.setEnabled(false);
		}

	}

	protected class ItemListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			Object o = event.getSource();
			if (o instanceof AbstractButton) {
				String command = ((AbstractButton) o).getActionCommand();
				QueryFilterButton.this.performFilter(command);
			}
			QueryFilterButton.this.filterMenu.setVisible(false);
		}

	}

	protected class ItemSaveListener implements ActionListener {

		protected ResourceBundle bundle = null;

		public ItemSaveListener(ResourceBundle resource) {
			this.bundle = resource;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			Object s = MessageDialog.showInputMessage(SwingUtilities.getWindowAncestor(QueryFilterButton.this),
					QueryFilterButton.INSERT_FILTER_NAME_MESSAGE, this.bundle);
			if (s != null) {
				QueryFilterButton.this.storeCurrentFilter(s.toString());
			}
			if (QueryFilterButton.this.filterMenu != null) {
				QueryFilterButton.this.filterMenu.setVisible(false);
			}
		}

		public void setResourceBundle(ResourceBundle recursos) {
			this.bundle = recursos;
		}

	}

	protected class DefaultItemListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			Object o = event.getSource();
			if (o instanceof JCheckBox) {
				JCheckBox checkBox = (JCheckBox) o;
				if (!checkBox.isSelected()) {
					// Uncheck
					QueryFilterButton.this.setDefaultQueryFilter(null);
				} else {
					String name = checkBox.getActionCommand();
					QueryFilterButton.this.setDefaultQueryFilter(name);
				}
			}
			QueryFilterButton.this.filterMenu.setVisible(false);
		}

	}

	/**
	 * Listener that is invoked when a filter configuration is deleted
	 *
	 * @author Imatia Innovation
	 */
	protected class ItemDeleteListener implements ActionListener {

		protected ResourceBundle bundle = null;

		public ItemDeleteListener(ResourceBundle resource) {
			this.bundle = resource;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object o = e.getSource();
			if (o instanceof AbstractButton) {
				int i = JOptionPane.showConfirmDialog((Component) o,
						ApplicationManager.getTranslation(QueryFilterButton.DELETE_KEY, this.bundle), "",
						JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.OK_OPTION) {
					String command = ((AbstractButton) o).getActionCommand();
					QueryFilterButton.this.deleteFilterConfiguration(command);
				}
			}
			QueryFilterButton.this.filterMenu.setVisible(false);
		}

		public void setResourceBundle(ResourceBundle bundle) {
			this.bundle = bundle;
		}

	}

	protected void createFilterMenu() {
		if (this.filterMenu == null) {
			this.filterMenu = new ExtendedJPopupMenu();
			this.itemListener = new ItemListener();
			this.defaultItemListener = new DefaultItemListener();
			this.itemDeleteListener = new ItemDeleteListener(this.bundle);
		}

		this.filterMenu.removeAll();

		java.util.List lList = this.getFilterConfigurations();
		int originalSize = lList.size();

		if (originalSize != 0) {
			String defaultQueryFilter = this.getDefaultQueryFilter();
			for (Object element : lList) {
				JPanel panel = new JPanel(new GridBagLayout());
				String currentName = (String) element;

				JCheckBox defaultValue = new JCheckBox();
				defaultValue.setActionCommand(currentName);
				if (currentName.equals(defaultQueryFilter)) {
					defaultValue.setSelected(true);
				}
				defaultValue.setBorderPainted(false);
				defaultValue.addActionListener(this.defaultItemListener);
				panel.add(defaultValue, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
						GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
				JButton item = new MenuButton(currentName);
				item.addActionListener(this.itemListener);
				panel.add(item, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				ImageIcon icon = ImageManager.getIcon(ImageManager.RECYCLER);
				item.setMargin(new Insets(0, 0, 0, 0));
				JButton delete = new MenuButton(icon);
				delete.setActionCommand((String) element);
				delete.addActionListener(this.itemDeleteListener);
				delete.setMargin(new Insets(0, 0, 0, 0));
				panel.add(delete,
						new GridBagConstraints(2, 0, GridBagConstraints.REMAINDER, 1, 0, 0, GridBagConstraints.EAST,
								GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
				this.filterMenu.add(panel);
			}
		} else {
			JLabel label = new JLabel(
					ApplicationManager.getTranslation(QueryFilterButton.NO_RESULT_MESSAGE, this.bundle));
			this.filterMenu.add(label);
		}
	}

	private java.util.List<String> getFilterConfigurations() {
		List<String> list = new ArrayList<>();
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);
				if (p != null) {
					StringTokenizer st = new StringTokenizer(p, ";");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (!(token.indexOf(QueryFilterButton.PREFERENCE_DEFAULT_KEY) >= 0)) {
							list.add(token);
						}
					}
				}
			}
		} catch (Exception exc) {
			QueryFilterButton.logger.error(null, exc);
		}
		return list;
	}

	protected String getFilterListPreferenceKey() {
		Form f = this.parentForm;
		return QueryFilterButton.FORM_QUERY_FILTER_PREFERENCE_KEY + "_" + f.getArchiveName();
	}

	protected String getFilterPreferenceKey(String name) {
		Form f = this.parentForm;
		return name != null ? QueryFilterButton.FORM_QUERY_FILTER_PREFERENCE_KEY + "_" + f.getArchiveName() + "_" + name
				: QueryFilterButton.FORM_QUERY_FILTER_PREFERENCE_KEY + "_" + f.getArchiveName();
	}

	protected String getUser() {
		if (this.locator instanceof ClientReferenceLocator) {
			return ((ClientReferenceLocator) this.locator).getUser();
		}
		return null;
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator locator) {
		this.locator = locator;
	}

	private void deleteFilterConfiguration(String conf) {
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);

				String pout = this.deleteQueryFilterToList(conf, p);
				if (pout != null) {
					prefs.setPreference(this.getUser(), this.getFilterPreferenceKey(conf), null);
					prefs.setPreference(this.getUser(), preferenceKey, pout);
					prefs.savePreferences();
				}
			}
		} catch (Exception exc) {
			QueryFilterButton.logger.error(null, exc);
		}
	}

	protected boolean performFilter(String filtername) {
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);
				if (p != null) {
					StringTokenizer st = new StringTokenizer(p, ";");
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						if (token.equalsIgnoreCase(filtername)) {
							String currentPreferences = prefs.getPreference(this.getUser(),
									this.getFilterPreferenceKey(token));
							this.retrieveFilter(currentPreferences);
							return true;
						}
					}
				}
			}
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
		}
		return false;
	}

	protected boolean existCurrentFilter(String preferences, String name) {
		StringTokenizer tokens = new StringTokenizer(preferences, ";");
		while (tokens.hasMoreElements()) {
			String token = tokens.nextToken();
			if (name.equals(token)) {
				return true;
			}
		}
		return false;
	}

	protected boolean storeCurrentFilter(String filtername) {
		Map<String, Object> currentData = this.retrieveFilterFormData();
		if (currentData.isEmpty()) {
			this.parentForm.message(QueryFilterButton.NO_DATA_FOR_FILTER_MESSAGE, JOptionPane.INFORMATION_MESSAGE);
			// Message to indicate that nothing is stored
			return true;
		}
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);
				if (p == null) {
					p = QueryFilterButton.PREFERENCE_DEFAULT_KEY;
				}
				// Checks if other preference with the same name exists
				if (this.existCurrentFilter(p, filtername)) {
					int op = this.parentForm.message(QueryFilterButton.QUERY_FILTER_EXIST_QUESTION,
							JOptionPane.QUESTION_MESSAGE);
					if (JOptionPane.YES_OPTION != op) {
						return false;
					}
				}

				p = this.insertQueryFilterToList(filtername, p);
				String currentPreference = this.convertFilterDataToString(currentData);
				prefs.setPreference(this.getUser(), this.getFilterPreferenceKey(filtername), currentPreference);
				prefs.setPreference(this.getUser(), preferenceKey, p);
			}
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
		}
		return false;

	}

	protected Map<String, Object> retrieveFilterFormData() {
		Map<String, Object> data = new HashMap<>();
		List<?> fieldList = this.parentForm.getDataComponents();
		for (int i = 0; i < fieldList.size(); i++) {
			Object currentValue = null;
			Object currentField = fieldList.get(i);
			if (currentField instanceof IdentifiedElement) {
				Object currentAttr = ((IdentifiedElement) currentField).getAttribute();
				if (currentAttr instanceof String) {
					currentValue = this.parentForm.getDataFieldValue(currentAttr.toString());
				} else if (currentAttr instanceof ReferenceFieldAttribute) {
					currentValue = this.parentForm.getDataFieldValue(((ReferenceFieldAttribute) currentAttr).getAttr());
				}
				if (currentValue != null) {
					data.put((String) currentAttr, currentValue);
				}
			}
		}
		return data;
	}

	protected void retrieveFilter(String data) throws Exception {
		Map<String, Object> values = this.convertFilterDataToHashtable(data);
		Enumeration<String> keys = Collections.enumeration(values.keySet());
		this.parentForm.deleteDataFields();
		List<?> vSetValueOrder = this.parentForm.getSetValuesOrder();
		List<String> vsetDataFields = Collections.list(keys);
		if ((vSetValueOrder != null) && !vSetValueOrder.isEmpty()) {
			for (int j = vSetValueOrder.size() - 1; j >= 0; j--) {
				Object currentSetValueOrder = vSetValueOrder.get(j);
				if (vsetDataFields.contains(currentSetValueOrder)) {
					String dataField = vsetDataFields.remove(vsetDataFields.indexOf(currentSetValueOrder));
					vsetDataFields.add(0, dataField);
				}
			}
		}

		// Until the comment with slashes, performs the checks of parent keys
		// and sets it the field in correct load order

		List<String> orderedElements = new ArrayList<>();
		List<Object> filterComponentList = new ArrayList<>();
		for (int i = 0; i < vsetDataFields.size(); i++) {
			String attr = vsetDataFields.get(i);
			Object comp = this.parentForm.getDataFieldReference(attr);
			if (comp instanceof IFilterElement) {
				filterComponentList.add(comp);
			} else {
				orderedElements.add(vsetDataFields.get(i));
			}
		}

		for (Object component : filterComponentList) {
			IFilterElement comp = (IFilterElement) component;
			if (comp.hasParentKeys()) {
				for (Object parentKey : comp.getParentKeyList()) {
					this.addFilterElementToOrderedFields(orderedElements, (String) parentKey);
				}
			}

			String attr = (String) ((DataField) comp).getAttribute();
			if (!orderedElements.contains(attr)) {
				orderedElements.add(attr);
			}

		}

		vsetDataFields.clear();
		vsetDataFields.addAll(orderedElements);

		/////////////////////////////////////////////////////////////////////////////////////////////

		for (String currentKey : vsetDataFields) {
			Object currentValue = values.get(currentKey);
			this.parentForm.setDataFieldValue(currentKey, currentValue);
		}
	}

	/**
	 * Checks if the attribute of an {@link IFilter} element field has parent keys, to add them to the
	 * list of elements to load before the field itself. Recursively checks that this is the case,
	 * avoiding adding the attribute if it already exists in the List
	 * @param orderedList A {@link List} wich contains the ordered elements
	 * @param attr A {@link String} with the attr to perform the check
	 */
	protected void addFilterElementToOrderedFields(List<String> orderedList, String attr) {
		if (!orderedList.contains(attr)) {
			IFilterElement comp = (IFilterElement) this.parentForm.getDataFieldReference(attr);
			if (comp.hasParentKeys()) {
				for (Object parentKey : comp.getParentKeyList()) {
					this.addFilterElementToOrderedFields(orderedList, (String) parentKey);
				}
			} else {
				orderedList.add(attr);
			}
		}

	}

	protected String convertFilterDataToString(Map<String, Object> data) {
		try {
			return this.serializerManager.serializeMapToString(data);
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
		}
		return null;
	}

	protected Map<String, Object> convertFilterDataToHashtable(String buffer) {
		try {
			return this.serializerManager.deserializeStringToMap(buffer);
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
			try {
				return SerializerManagerFactory.getDefaultSerializerManager()
						.deserializeStringToMap(buffer);
			} catch (Exception e) {
				QueryFilterButton.logger.error(null, e);
			}
		}
		return null;
	}

	protected String getDefaultQueryFilter(String preference) {
		if (preference != null) {
			StringTokenizer st = new StringTokenizer(preference, ";");
			if (st.hasMoreTokens()) {
				String token = st.nextToken();
				int index = token.indexOf(QueryFilterButton.PREFERENCE_DEFAULT_KEY);
				if (index >= 0) {
					token = token.substring(index + QueryFilterButton.PREFERENCE_DEFAULT_KEY.length()).trim();
					if ((token != null) && (token.length() > 0)) {
						return token;
					}
					return null;
				}
			}
		}
		return null;
	}

	protected String getDefaultQueryFilter() {
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);
				return this.getDefaultQueryFilter(p);
			}
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
		}
		return null;
	}

	protected String setDefaultQueryFilter(String filterName, String preferences) {
		if (preferences != null) {
			StringTokenizer st = new StringTokenizer(preferences, ";");
			StringBuilder result = new StringBuilder();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int index = token.indexOf(QueryFilterButton.PREFERENCE_DEFAULT_KEY);
				if (index >= 0) {
					result.append(QueryFilterButton.PREFERENCE_DEFAULT_KEY);
					result.append(filterName == null ? "" : filterName);
				} else {
					result.append(";");
					result.append(token);
				}
			}
			return result.toString();
		}
		return preferences;
	}

	protected void setDefaultQueryFilter(String filterName) {
		try {
			Application ap = this.parentForm.getFormManager().getApplication();
			String preferenceKey = this.getFilterListPreferenceKey();
			ApplicationPreferences prefs = ap.getPreferences();
			if ((preferenceKey != null) && (prefs != null)) {
				String p = prefs.getPreference(this.getUser(), preferenceKey);
				String result = this.setDefaultQueryFilter(filterName, p);
				if (result != null) {
					prefs.setPreference(this.getUser(), preferenceKey, result);
					prefs.savePreferences();
				}
			}
		} catch (Exception ex) {
			QueryFilterButton.logger.error(null, ex);
		}
	}

	protected String deleteQueryFilterToList(String filterName, String preferences) {
		if (preferences != null) {
			StringTokenizer st = new StringTokenizer(preferences, ";");
			StringBuilder result = new StringBuilder();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				int index = token.indexOf(QueryFilterButton.PREFERENCE_DEFAULT_KEY);
				if (index >= 0) {
					if (!filterName
							.equals(token.substring(index + QueryFilterButton.PREFERENCE_DEFAULT_KEY.length()))) {
						result.append(token);
					} else {
						result.append(QueryFilterButton.PREFERENCE_DEFAULT_KEY);
					}
				} else {
					if (!filterName.equals(token)) {
						result.append(";");
						result.append(token);
					}
				}
			}
			return result.toString();
		}
		return preferences;
	}

	protected String insertQueryFilterToList(String filterName, String preferences) {
		if (preferences != null) {
			StringTokenizer st = new StringTokenizer(preferences, ";");
			StringBuilder result = new StringBuilder();
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (filterName.equals(token)) {
					return preferences;
				}
				result.append(token);
				result.append(";");
			}
			result.append(filterName);
			return result.toString();
		}
		return null;
	}

	@Override
	public void setResourceBundle(ResourceBundle recursos) {
		super.setResourceBundle(recursos);
		if (this.itemSaveListener != null) {
			this.itemSaveListener.setResourceBundle(recursos);
		}
		if (this.itemDeleteListener != null) {
			this.itemDeleteListener.setResourceBundle(recursos);
		}
	}

}
