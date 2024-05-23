package com.ontimize.gui;

import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.xml.XMLApplicationBuilder;
import com.ontimize.gui.container.RadioItemGroup;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.i18n.LocaleListener;
import com.ontimize.gui.i18n.MenuLocale;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.preferences.HasPreferenceComponent;
import com.ontimize.gui.preferences.ShortcutDialogConfiguration;
import com.ontimize.help.HelpUtilities;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.xml.XMLClientProvider;

/**
 * Basic implementation of the application menu bar.
 *
 * @version 1.0
 */
public class ApplicationMenuBar extends JMenuBar
implements FormComponent, Freeable, HasHelpIdComponent, HasPreferenceComponent {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationMenuBar.class);

	protected Map<Object, Object>		menuItemList				= new HashMap<>();

	private MenuElement element = null;

	protected boolean dynamicloaded = false;

	private boolean listBuild = false;

	private List<Object>				buttonGroup					= new ArrayList<>();

	// TODO check the use of an empty image for the menu items
	private static ImageIcon emptyIcon_16 = null;

	protected ResourceBundle resources;

	private ShortcutDialogConfiguration menuShortcutConfiguration = null;

	public ApplicationMenuBar(Map<Object, Object> parameters) {
		this.init(parameters);
		if (ApplicationMenuBar.emptyIcon_16 == null) {
			ApplicationMenuBar.emptyIcon_16 = ImageManager.getIcon(ImageManager.EMPTY_16);
		}
	}

	@Override
	public void init(Map<Object, Object> parameters) {
		this.installHelpId();
	}

	/**
	 * Return always null
	 */
	@Override
	public Object getConstraints(LayoutManager layout) {
		return null;
	}

	/**
	 * Method used to store a reference to the RadioItemGroup elements
	 * @param buttonsGroup
	 */
	public void add(RadioItemGroup buttonsGroup) {
		this.buttonGroup.add(buttonsGroup);
	}

	/**
	 * Get a reference to the component with the specified attribute. The element can be a Menu or a
	 * Menu Item
	 * @param attribute
	 * @return Element reference or null if there are not elements with the specified attribute
	 */
	public JMenuItem getMenuItem(String attribute) {
		if (!this.listBuild) {
			this.buildList(this);
			this.listBuild = true;
		}
		if (this.menuItemList.containsKey(attribute)) {
			return (JMenuItem) this.menuItemList.get(attribute);
		} else {
			return null;
		}
	}

	/**
	 * Enables or disables the element with the specified attribute. If there are no components with
	 * this attribute nothing is done
	 * @param attribute
	 * @param enabled
	 */
	public void setItemMenuEnabled(String attribute, boolean enabled) {
		JMenuItem item = this.getMenuItem(attribute);
		if (item != null) {
			item.setEnabled(enabled);
		}
	}

	public void clearItemList() {
		this.menuItemList.clear();
		this.listBuild = false;
	}

	/**
	 * Create a list with all the menu elements
	 * 
	 * @return A List with all the menu elements
	 */
	public List<Object> getAllItems() {
		if (!this.listBuild) {
			this.buildList(this);
			this.listBuild = true;
		}
		List<Object> items = new ArrayList<>();
		for (Entry<Object, Object> entry : this.menuItemList.entrySet()) {
			items.add(entry.getValue());
		}
		return items;
	}

	/**
	 * Creates the list with the menu elements including the identificator
	 * @param element
	 */
	private void buildList(MenuElement element) {
		if (element instanceof IdentifiedElement) {
			if (this.menuItemList.containsKey(((IdentifiedElement) element).getAttribute())) {
				ApplicationMenuBar.logger
				.debug("ApplicationMenuBar: '" + ((IdentifiedElement) element).getAttribute()
						+ "' attribute has multiple instances. The xml file must be checked.");
			}
			this.menuItemList.put(((IdentifiedElement) element).getAttribute(), element);
		}
		// Now children
		MenuElement[] childElements = element.getSubElements();
		// If it is a menu and some of the children has an icon use an empty
		// icon to align all items
		if (element instanceof JPopupMenu) {
			boolean bSomeIcon = false;
			for (MenuElement childElement : childElements) {
				if (childElement instanceof AbstractButton) {
					if (((AbstractButton) childElement).getIcon() != null) {
						bSomeIcon = true;
						break;
					}
				}
			}
			if (bSomeIcon && !ApplicationManager.jvmVersionHigherThan_1_6_0()) {
				for (MenuElement childElement : childElements) {
					if (childElement instanceof AbstractButton) {
						if (((AbstractButton) childElement).getIcon() == null) {
							((AbstractButton) childElement).setIcon(ApplicationMenuBar.emptyIcon_16);
						}
					}
				}
			}
		}
		for (MenuElement childElement : childElements) {
			this.buildList(childElement);
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		this.getChildsTexts(this, v);
		return v;
	}

	private void getChildsTexts(MenuElement menuElement, List<String> v) {
		MenuElement[] childElements = menuElement.getSubElements();
		for (MenuElement childElement : childElements) {
			if (childElement instanceof Internationalization) {
				v.addAll(((Internationalization) childElement).getTextsToTranslate());
			}
			this.getChildsTexts(childElement, v);
		}
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		this.resources = resources;
		this.setChildsResourceBundle(this, resources);
	}

	private void setChildsResourceBundle(MenuElement menuElement, ResourceBundle resoruces) {
		MenuElement[] childElements = menuElement.getSubElements();
		for (MenuElement childElement : childElements) {
			if (childElement instanceof Internationalization) {
				((Internationalization) childElement).setResourceBundle(resoruces);
			}
			this.setChildsResourceBundle(childElement, resoruces);
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public void free() {
		this.menuItemList.clear();
		this.menuItemList = null;
		this.buttonGroup.clear();
		this.buttonGroup = null;
		this.element = null;
		MenuElement[] children = this.getSubElements();
		// Free the menu components
		for (MenuElement element2 : children) {
			if (element2 instanceof Freeable) {
				try {
					((Freeable) element2).free();
				} catch (Exception e) {
					if (com.ontimize.gui.ApplicationManager.DEBUG) {
						ApplicationMenuBar.logger.debug(this.getClass().toString() + ": " + "Exception in free() "
								+ element2.getClass().toString() + " : " + e.getMessage(), e);
					}
				}
			}
		}
		// Remove the children
		this.removeAll();
		if (com.ontimize.gui.ApplicationManager.DEBUG) {
			ApplicationMenuBar.logger.debug(this.getClass() + " Liberado");
		}
	}

	/**
	 * Gets an list with the reference to the RadioItemGroup elements
	 *
	 * @return
	 */
	public List<Object> getButtonGroup() {
		return this.buttonGroup;
	}

	@Override
	public String getHelpIdString() {
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf(".") + 1);
		return className + "HelpId";
	}

	@Override
	public void installHelpId() {
		try {
			String helpId = this.getHelpIdString();
			HelpUtilities.setHelpIdString(this, helpId);
		} catch (Exception e) {
			ApplicationMenuBar.logger.error(e.getMessage(), e);
			return;
		}
	}

	protected void createMenuShortcutsConfigurationDialog() {
		if (this.menuShortcutConfiguration == null) {
			this.menuShortcutConfiguration = new ShortcutDialogConfiguration(
					ApplicationManager.getApplication().getFrame(), this);
			this.menuShortcutConfiguration.pack();
			ApplicationManager.center(this.menuShortcutConfiguration);
		}
	}

	public void showMenuShortcutsConfigurationDialog() {
		// Window
		this.createMenuShortcutsConfigurationDialog();
		this.menuShortcutConfiguration.setResourceBundle(this.resources);
		this.menuShortcutConfiguration.setVisible(true);
	}

	public void addConfigurableKeyStrokeGroup(String groupName, List keyBindings) {
		this.createMenuShortcutsConfigurationDialog();
		this.menuShortcutConfiguration.addConfigurableKeyStrokeGroup(groupName, keyBindings);
	}

	/**
	 * This method convert a keystroke in a String with this structure: 'modifiers keycode'. <br>
	 * For example:<br>
	 *
	 * <br>
	 * For <b>keystroke</b> correspondent to: <b>'Ctrl + Alt + a'</b> , method will return:<br>
	 * '<b>650 65</b>'. '650' is the code for modifiers 'Ctrl + Alt' and '65' is the code for 'a'. <br>
	 * Shorcuts will be stored in preferences in this way.
	 *
	 * Note: Method that makes the opposite is
	 * @param accelerator keystroke to convert
	 * @return A <code>String</code> with numeric values of keystroke.
	 */
	public static String acceleratorToString(KeyStroke accelerator) {
		if (accelerator == null) {
			return null;
		}
		String acceleratorText = "";
		if (accelerator != null) {
			int modifiers = accelerator.getModifiers();
			if (modifiers > 0) {
				acceleratorText += modifiers;
				acceleratorText += " ";
			}
			int keyCode = accelerator.getKeyCode();
			if (keyCode != 0) {
				acceleratorText += keyCode;
			}
		}
		return acceleratorText;
	}

	/**
	 * This method convert a keystroke in a String with this structure: i.e. <b>'Ctrl + Alt + a'</b>.
	 * Modifiers (Ctrl + Alt ) are also internazionalized for avoiding problems with jvm
	 * locale-dependent method: <code>getKeyStroke</code>.
	 * @param accelerator keystroke to convert
	 * @return A <code>String</code> with text for keystroke or directly the codes for keystroke when it
	 *         does not exist in bundle.
	 */
	public static String acceleratorMessageFromKeystroke(KeyStroke accelerator) {
		String ksmessage = new String("");

		// Modifiers are translated because KeyEvent.getKeyModifiersText is
		// dependent of jvm location.
		int modifiers = accelerator.getModifiers();
		if (modifiers > 0) {
			ksmessage += ApplicationManager.getTranslation(KeyEvent.getKeyModifiersText(modifiers));
			ksmessage += " ";
		}
		int keyCode = accelerator.getKeyCode();
		if ((keyCode != 0) && (KeyEvent.VK_CONTROL != keyCode) && (KeyEvent.VK_ALT != keyCode)
				&& (KeyEvent.VK_SHIFT != keyCode)) {
			ksmessage += KeyEvent.getKeyText(keyCode);
		}
		return ksmessage;
	}

	@Override
	public void initPreferences(ApplicationPreferences prefs, String user) {
		this.initChildPreferences(this, prefs, user);
	}

	private void initChildPreferences(MenuElement elementoMenu, ApplicationPreferences prefs, String user) {
		MenuElement[] childElements = elementoMenu.getSubElements();
		for (MenuElement childElement : childElements) {
			if (childElement instanceof HasPreferenceComponent) {
				((HasPreferenceComponent) childElement).initPreferences(prefs, user);
			}
			this.initChildPreferences(childElement, prefs, user);
		}
	}

	public void loadDynamicItems() {
		if (!this.dynamicloaded) {
			try {
				EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
				if (locator instanceof XMLClientProvider) {
					XMLClientProvider clientProvider = (XMLClientProvider) locator;
					String xmlMenu = clientProvider.getXMLMenu(locator.getSessionId());
					if (xmlMenu != null) {
						XMLApplicationBuilder.getXMLApplicationBuilder().getMenuBuilder().appendMenu(this, xmlMenu);

						this.clearItemList();
						List<Object> allItems = this.getAllItems();
						for (Object item : allItems) {
							if ((item instanceof IDynamicItem) && ((IDynamicItem) item).isDynamic()) {
								if (item instanceof MenuLocale) {
									((MenuLocale) item).addLocaleListener(
											(LocaleListener) ApplicationManager.getApplication().getMenuListener());
								} else {
									((JMenuItem) item).addActionListener(
											(ActionListener) ApplicationManager.getApplication().getMenuListener());
								}

								if (item instanceof Internationalization) {
									((Internationalization) item).setResourceBundle(this.resources);
								}
							}
						}
					}
				}
				this.dynamicloaded = true;
			} catch (Exception e) {
				ApplicationMenuBar.logger.error(null, e);
			}
			this.revalidate();
			this.repaint();
		}
	}

	public void removeDynamicItems() {
		if (this.dynamicloaded) {
			try {
				// deleteDynamicItems
				List<Object> allItems = this.getAllItems();
				for (Object allItem : allItems) {
					JComponent currenComponent = (JComponent) allItem;
					if ((currenComponent instanceof IDynamicItem) && ((IDynamicItem) currenComponent).isDynamic()) {
						Container parent = currenComponent.getParent();
						parent.remove(currenComponent);

						if (currenComponent instanceof MenuLocale) {
							((MenuLocale) currenComponent).removeLocaleListener(
									(LocaleListener) ApplicationManager.getApplication().getMenuListener());
						} else {
							((JMenuItem) currenComponent).removeActionListener(
									(ActionListener) ApplicationManager.getApplication().getMenuListener());
						}
					}
				}

				this.clearItemList();
				this.revalidate();
				this.repaint();
				this.dynamicloaded = false;
			} catch (Exception e) {
				ApplicationMenuBar.logger.error(null, e);
			}
		}
	}

}
