package com.ontimize.gui.container;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.InteractionManager;
import com.ontimize.gui.OperationThread;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.security.ClientSecurityManager;

/**
 * This class implements a component that lets the user switch between a group of components by clicking on a tab. <p>
 *
 * @author Imatia Innovation
 */
public class TabPanel extends JTabbedPane implements FormComponent, ReferenceComponent, IdentifiedElement, AccessForm, ChangeListener, Freeable {

	private static final Logger			logger				= LoggerFactory.getLogger(TabPanel.class);

	/**
	 * The reference to attribute. By default, null.
	 */
	protected Object					attribute			= null;

	/**
	 * The instance of list with tabs.
	 */
	protected List<Object>					tabs				= new ArrayList<>();

	/**
	 * The reference to parent form for component. By default, null.
	 */
	protected Form						parentForm			= null;

	/**
	 * The reference to locator. By default, null.
	 */
	protected EntityReferenceLocator	locator				= null;

	/**
	 * The reference for visible permission. By default, null.
	 */
	protected FormPermission			visiblePermission	= null;

	/**
	 * The reference for enabled permission. By default, null.
	 */
	protected FormPermission			enabledPermission	= null;

	private final List<Object>			noQueriedTabs		= new ArrayList<>();

	protected ResourceBundle			resources			= null;

	@Override
	public void setParentForm(Form f) {
		this.parentForm = f;
	}

	/**
	 * The class constructor. Inits parameters and adds <code>ChangeListener</code>. <p>
	 *
	 * @param parameters
	 *            the map with parameters
	 */
	public TabPanel(Map<Object, Object> parameters) {
		this.init(parameters);
		this.addChangeListener(this);
	}

	/**
	 * Inits the parameters. <p>
	 *
	 * @param parameters
	 *            the map with parameters
	 *
	 *            <p>
	 *
	 *
	 *            <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td>
	 *            <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>attr</td> <td></td> <td></td> <td>yes</td> <td>The attribute for tab panel.</td> </tr> <tr> <td>opaque</td> <td>yes/no</td> <td>yes</td> <td>no</td>
	 *            <td>Specifies if the component must be opaque or not.</td> </tr> <tr> <td>bgcolor</td> <td>A color</td> <td></td> <td>no</td> <td>Specifies the component
	 *            background color.</td> </tr> </TABLE>
	 *
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		Object attr = parameters.get("attr");
		if (attr == null) {
			if (ApplicationManager.DEBUG) {
				TabPanel.logger.debug(this.getClass().toString() + " : Parameter 'attr' not found");
			}
		} else {
			this.attribute = attr;
		}

		Object oOpaque = parameters.get("opaque");
		if ((oOpaque != null) && !ApplicationManager.parseStringValue(parameters.get("opaque").toString())) {
			this.setOpaque(false);
		} else if ((oOpaque != null) && ApplicationManager.parseStringValue(parameters.get("opaque").toString())) {
			this.setOpaque(true);
		}

		Object bgcolor = parameters.get(DataField.BGCOLOR);
		if (bgcolor != null) {
			String bg = bgcolor.toString();
			if (bg.indexOf(";") > 0) {
				try {
					this.setBackground(ColorConstants.colorRGBToColor(bg));
				} catch (Exception exc) {
					TabPanel.logger.error(this.getClass().toString() + ": Error in parameter 'bgcolor': " + exc.getMessage(), exc);
				}
			} else {
				try {
					this.setBackground(ColorConstants.parseColor(bg));
				} catch (Exception exc) {
					TabPanel.logger.error(this.getClass().toString() + ": Error in parameter 'bgcolor': " + exc.getMessage(), exc);
				}
			}
		}
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (d.height < 75) {
			d.height = 75;
		}
		return d;
	}

	@Override
	public void setResourceBundle(ResourceBundle resource) {
		this.resources = resource;
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		return v;
	}

	@Override
	public void setComponentLocale(Locale l) {}

	/**
	 * Sets the tab enabled according to a index. <p>
	 *
	 * @param index
	 *            the tab index
	 * @param enabled
	 *            the condition to enable
	 */
	public void setTabEnabled(int index, boolean enabled) {
		if (index < this.getTabCount()) {
			this.setEnabledAt(index, enabled);
		}
	}

	/**
	 * Sets the tab enabled according to a title. <p>
	 *
	 * @param title
	 *            the title
	 * @param enabled
	 *            the condition to enable
	 */
	public void setTabEnabled(String title, boolean enabled) {
		for (int i = 0; i < this.getTabCount(); i++) {
			if (this.getComponentAt(i) instanceof Tab) {
				if (((Tab) this.getComponentAt(i)).getConstraints(null).equals(title)) {
					this.setEnabledAt(i, enabled);
					return;
				}
			}
		}
	}

	/**
	 * Sets the tab visible. <p>
	 *
	 * @param title
	 *            the title to find the tab
	 */
	public void setTabVisible(String title) {
		for (int i = 0; i < this.getTabCount(); i++) {
			if (this.getComponentAt(i) instanceof Tab) {
				if (title.equals(((Tab) this.getComponentAt(i)).getTitleKey())) {
					if (this.isEnabledAt(i)) {
						this.setSelectedIndex(i);
					}
					return;
				} else if (title.equals(((Tab) this.getComponentAt(i)).getAttribute())) {
					if (this.isEnabledAt(i)) {
						this.setSelectedIndex(i);
					}
					return;
				}
			}
		}
	}

	/**
	 * Gets the tab visible. <p>
	 *
	 * @return the tab visible
	 */
	public String getTabVisible() {
		for (int i = 0; i < this.getTabCount(); i++) {
			if (this.getComponentAt(i) instanceof Tab) {
				if (this.getSelectedIndex() == i) {
					return (String) ((Tab) this.getComponentAt(i)).getConstraints(null);
				}
			}
		}
		return null;
	}

	/**
	 * Sets the tab visible. <p>
	 *
	 * @param index
	 *            the tab index
	 */
	public void setTabVisible(int index) {
		if (this.isEnabledAt(index)) {
			this.setSelectedIndex(index);
		}
	}

	/**
	 * Shows all tabs.
	 */
	public void showAllTabs() {
		for (Object element : this.tabs) {
			if (element instanceof Tab) {
				String title = ((Tab) element).getConstraints(null).toString();
				this.showTab(title);
			}
		}
	}

	/**
	 * Hides all tabs.
	 */
	public void hideAllTabs() {
		for (Object element : this.tabs) {
			if (element instanceof Tab) {
				String title = ((Tab) element).getConstraints(null).toString();
				if (title != null) {
					this.hideTabs(title);
				}
			}
		}
	}

	/**
	 * Hides the tab according to the title. <p>
	 *
	 * @param title
	 *            the title
	 */
	public void hideTabs(String title) {
		for (int i = 0; i < this.getTabCount(); i++) {
			if (((this.getComponentAt(i) instanceof Tab) && title.equals(((Tab) this.getComponentAt(i)).getConstraints(null))) || title
					.equals(((Tab) this.getComponentAt(i)).getAttribute())) {
				this.removeTabAt(i);
			}
		}
	}

	/**
	 * Shows the tab according to the title. <p>
	 *
	 * @param title
	 *            the title
	 */
	public void showTab(String title) {
		// Checks that this tab is not added yet
		for (int i = 0; i < this.getTabCount(); i++) {
			if (((this.getComponentAt(i) instanceof Tab) && (((Tab) this.getComponentAt(i)).getConstraints(null).equals(title))) || title
					.equals(((Tab) this.getComponentAt(i)).getAttribute())) {
				return;
			}
		}

		int notVisibles = 0;
		for (int i = 0; i < this.tabs.size(); i++) {
			if (this.tabs.get(i) instanceof Tab) {
				Tab tab = (Tab) this.tabs.get(i);
				Object attr = tab.getAttribute();
				String innerTitle = tab.getConstraints(null).toString();
				if ((attr != null) && (attr.equals(title) || innerTitle.equals(title))) {
					this.insertTab(ApplicationManager.getTranslation(tab.getConstraints(null).toString(), this.resources), tab.getIcon(), tab,
							ApplicationManager.getTranslation(tab.getTip(), this.resources), Math.min(i - notVisibles, this.getTabCount()));
					return;
				}
				if (this.indexOfComponent(tab) < 0) {
					notVisibles++;
				}
			}
		}
	}

	@Override
	public void add(Component c, Object constraints) {
		if (!(c instanceof Tab)) {
			super.add(c, constraints);
			TabPanel.logger.debug("WARNING: Component different of Tab added in a TabPanel component");
		} else {
			if (((Tab) c).getIcon() == null) {
				super.add(c, constraints);
			} else {
				super.addTab((String) constraints, ((Tab) c).getIcon(), c);
			}
			this.tabs.add(this.tabs.size(), c);
		}
	}

	@Override
	public void remove(Component component) {
		super.remove(component);
		if (this.tabs.contains(component)) {
			this.tabs.remove(component);
		}
	}

	public Component getTabAt(int index) {
		return (Component) this.tabs.get(index);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			boolean permission = this.checkVisiblePermission();
			if (!permission) {
				return;
			}
		}
		super.setVisible(visible);
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
	 * Checks the visible permission. <p>
	 *
	 * @return the condition about visibility permission
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
					TabPanel.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					TabPanel.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
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
	 * @return the enabled permission condition
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
					TabPanel.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					TabPanel.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	/**
	 * Inits no queried data field attributes. <p>
	 *
	 * @return the list with no queried data field attributes
	 */
	public List<Object> initNotQueriedDataFieldAttributes() {
		List<Object> v = new ArrayList<>();
		this.noQueriedTabs.clear();
		for (Object element : this.tabs) {
			if (element instanceof Tab) {
				if (((Tab) element).isQueryIfVisible()) {
					// Check if the component is visible
					if (!element.equals(this.getSelectedComponent())) {
						this.noQueriedTabs.add(element);
						v.addAll(((Tab) element).getNotRequiredTabFieldAttributes());
					}
				}
			}
		}
		return v;
	}

	/**
	 * Gets no queried data field attributes. <p>
	 *
	 * @return the list with no queried data field attributes
	 */
	public List<Object> getNotQueriedDataFieldAttributes() {
		List<Object> v = new ArrayList<>();
		for (int i = 0; i < this.noQueriedTabs.size(); i++) {
			v.addAll(((Tab) this.noQueriedTabs.get(i)).getNotRequiredTabFieldAttributes());
		}
		return v;
	}

	public void clearNotQueriedTabs() {
		this.noQueriedTabs.clear();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		try {
			int index = this.getSelectedIndex();
			if (index < 0) {
				return;
			}
			final Component c = this.getComponentAt(index);
			if ((c instanceof Tab) && ((Tab) c).isQueryIfVisible()) {
				if (this.noQueriedTabs.contains(c) && (this.parentForm.getInteractionManager().currentMode == InteractionManager.UPDATE)) {
					this.noQueriedTabs.remove(c);
					OperationThread t = new OperationThread() {

						@Override
						public void run() {
							try {
								this.hasStarted = true;
								this.status = "tabpanel.updating_data";
								// try {
								// if ((TabPanel.this.parentForm != null) &&
								// (TabPanel.this.parentForm.getResourceBundle()
								// != null)) {
								// this.status =
								// TabPanel.this.parentForm.getResourceBundle().getString(this.status);
								// }
								// } catch (Exception e) {
								// if (ApplicationManager.DEBUG) {
								// logger.debug(e.getMessage(),e);
								// }
								// }
								List<?> vTab = (List<?>) ((ArrayList<Object>) ((Tab) c).getNotRequiredTabFieldAttributes()).clone();
								EntityResult res = TabPanel.this.parentForm.query(TabPanel.this.parentForm.getCurrentIndex(), vTab);
								if (res.getCode() == EntityResult.OPERATION_WRONG) {
									TabPanel.this.parentForm.message(res.getMessage(), Form.ERROR_MESSAGE);
									this.hasFinished = true;
									return;
								}
								if (res.isEmpty()) {
									return;
								}

								boolean oldValueChangeListener = TabPanel.this.parentForm.getInteractionManager().isValueChangeListenerEnabled();
								try {
									TabPanel.this.parentForm.getInteractionManager().setValueChangeEventListenerEnabled(false);
									Enumeration<?> enumKeys = res.keys();
									while (enumKeys.hasMoreElements()) {
										Object oKey = enumKeys.nextElement();
										List<?> list = (List<?>) res.get(oKey);
										if ((list == null) || list.isEmpty()) {
											continue;
										}
										if (vTab.contains(oKey)) {
											TabPanel.this.parentForm.setDataFieldValue(oKey, list.get(0));
											TabPanel.this.parentForm.setDataFieldValueToFormCache(oKey, list.get(0));
										}
									}
								} finally {
									TabPanel.this.parentForm.getInteractionManager().setValueChangeEventListenerEnabled(oldValueChangeListener);
								}

								this.hasFinished = true;
							} catch (Exception e2) {
								TabPanel.logger.trace(null, e2);
								this.hasFinished = true;
							}
						}
					};

					Window w = SwingUtilities.getWindowAncestor(this.parentForm);
					if (w instanceof Frame) {
						ApplicationManager.proccessNotCancelableOperation((Frame) w, t, 600);
					} else if (w instanceof Dialog) {
						ApplicationManager.proccessNotCancelableOperation((Dialog) w, t, 600);
					}
				}
			}
		} catch (Exception ex) {
			TabPanel.logger.error(null, ex);
		}
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator b) {
		this.locator = b;
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
