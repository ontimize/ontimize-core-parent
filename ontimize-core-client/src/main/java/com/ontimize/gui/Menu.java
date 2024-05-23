package com.ontimize.gui;

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.preferences.HasPreferenceComponent;
import com.ontimize.jee.common.security.MenuPermission;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.util.ParseUtils;

/**
 * This class implements a deployed list to use in the menu bar. It uses swing components.
 *
 * @version 1.0 01/05/2001
 */

public class Menu extends JMenu implements FormComponent, IdentifiedElement, Freeable, SecureElement, StatusComponent,
HasPreferenceComponent, IDynamicItem {

	private static final Logger logger = LoggerFactory.getLogger(Menu.class);

	public static final String MENU_ACCELERATOR = "menu_accelerator";

	protected String statusText = null;

	protected String attribute = null;

	protected String shortcut = "";

	private MenuPermission visiblePermission = null;

	private MenuPermission enabledPermission = null;

	protected boolean dynamic = false;

	public Menu(Map<Object, Object> parameters) {
		this.init(parameters);
		Insets margin = this.getMargin();
		int leftMargin = margin.left;
		int newMargin = Math.max(0, leftMargin - 5);
		this.setMargin(new Insets(margin.top, newMargin, margin.bottom, margin.right));
		this.initPermissions();
		ApplicationManager.registerStatusComponent(this);
		this.dynamic = ParseUtils.getBoolean((String) parameters.get("dynamic"), false);
	}

	@Override
	public Object getConstraints(LayoutManager layout) {
		return null;
	}

    /**
	 * This method initializes the menu parameters <p>
	 * 
	 * @param parameters
	 *            the <code>Map</code> with parameters <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS * FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>attr</td> <td><i></td> <td></td> <td>yes</td> <td>Attribute to identify the
	 *            component.</td> </tr> <tr> <td>mnemonic</td> <td><i></td> <td></td> <td>no</td> <td>string specifying the mnemonic value</td> </tr> <tr> <td>shortcut</td>
	 *            <td><i></td> <td></td> <td>no</td> <td>Key stroke to open the menu</td> </tr> </table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		// Attribute
		Object attr = parameters.get("attr");
		if (attr != null) {
			this.attribute = attr.toString();
			this.setText(this.attribute);
			this.setActionCommand(this.attribute);
		}

		Object status = parameters.get("status");
		if (status == null) {
			this.statusText = this.attribute;
		} else {
			this.statusText = status.toString();
		}

		// Icon parameter
		Object oIcon = parameters.get("icon");
		if (oIcon != null) {
			this.setHorizontalAlignment(SwingConstants.LEFT);
			String sIconFile = oIcon.toString();
			ImageIcon icon = ImageManager.getIcon(sIconFile);
			if (icon != null) {
				this.setIcon(icon);
			}
		}

		Object mnemonic = parameters.get("mnemonic");
		if ((mnemonic != null) && !mnemonic.equals("")) {
			this.setMnemonic(mnemonic.toString().charAt(0));
		}

		Object shortcut = parameters.get("shortcut");
		if ((shortcut != null) && !shortcut.equals("")) {
			try {
				KeyStroke ks = KeyStroke.getKeyStroke(shortcut.toString());
				super.setAccelerator(ks);
				this.shortcut = ks.toString();
			} catch (Exception e) {
				Menu.logger.trace(null, e);
			}
		}
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>();
		v.add(this.attribute);
		return v;
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		try {
			if (resources != null) {
				this.setText(resources.getString(this.attribute));
			} else {
				this.setText(this.attribute);
			}
		} catch (Exception exc) {
			if (ApplicationManager.DEBUG) {
				Menu.logger.debug(null, exc);
			} else {
				Menu.logger.trace(null, exc);
			}
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public void free() {
		EventListener[] listeners = this.getListeners(Action.class);
		for (EventListener listener : listeners) {
			this.removeActionListener((ActionListener) listener);
		}
		MenuElement[] menuChildren = this.getSubElements();
		// Free the menu components
		for (MenuElement element : menuChildren) {
			if (element instanceof Freeable) {
				try {
					((Freeable) element).free();
				} catch (Exception exc) {
					if (ApplicationManager.DEBUG) {
						Menu.logger.debug("Exception in free() method " + element.getClass().toString(), exc);
					} else {
						Menu.logger.trace("Exception in free() method " + element.getClass().toString(), exc);
					}
				}
			}
		}
		// Now removes the children
		this.removeAll();
		if (ApplicationManager.DEBUG) {
			Menu.logger.debug(this.getClass().toString() + " Free");
		}
	}

	@Override
	public void setVisible(boolean vis) {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.visiblePermission == null) {
				this.visiblePermission = new MenuPermission("visible", this.attribute, true);
			}
			try {
				// Checks to show
				if (vis) {
					manager.checkPermission(this.visiblePermission);
				}
				super.setVisible(vis);
			} catch (Exception e) {
				if (ApplicationManager.DEBUG_SECURITY) {
					Menu.logger.debug(null, e);
				} else {
					Menu.logger.trace(null, e);
				}
			}
		} else {
			super.setVisible(vis);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.enabledPermission == null) {
				this.enabledPermission = new MenuPermission("enabled", this.attribute, true);
			}
			try {
				// checks to enable
				if ((enabled) && (manager != null)) {
					manager.checkPermission(this.enabledPermission);
				}
				this.restricted = false;
				super.setEnabled(enabled);
			} catch (Exception exc) {
				this.restricted = true;
				if (ApplicationManager.DEBUG_SECURITY) {
					Menu.logger.debug(null, exc);
				} else {
					Menu.logger.trace(null, exc);
				}
			}
		} else {
			super.setEnabled(enabled);
		}
	}

	@Override
	public void initPermissions() {
		if (ApplicationManager.getClientSecurityManager() != null) {
			ClientSecurityManager.registerSecuredElement(this);
		}
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.enabledPermission == null) {
				this.enabledPermission = new MenuPermission("enabled", this.attribute, true);
			}
			try {
				manager.checkPermission(this.enabledPermission);
				this.restricted = false;
			} catch (Exception exc) {
				this.restricted = true;
				super.setEnabled(false);
				if (ApplicationManager.DEBUG_SECURITY) {
					Menu.logger.debug(null, exc);
				} else {
					Menu.logger.trace(null, exc);
				}
			}
			if (this.visiblePermission == null) {
				this.visiblePermission = new MenuPermission("visible", this.attribute, true);
			}
			try {
				manager.checkPermission(this.visiblePermission);
			} catch (Exception exc) {
				super.setVisible(false);
				if (ApplicationManager.DEBUG_SECURITY) {
					Menu.logger.debug(null, exc);
				} else {
					Menu.logger.trace(null, exc);
				}
			}
		}
	}

	@Override
	public String getStatusText() {
		return this.statusText;
	}

	protected boolean restricted = false;

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	@Override
	public void initPreferences(ApplicationPreferences aPrefs, String user) {
		// KeyStroke
		if (aPrefs != null) {
			String pref = aPrefs.getPreference(user, this.getAcceleratorPreferenceKey());
			if (pref != null) {
				String prefs[] = pref.split(" ");
				KeyStroke ks = KeyStroke.getKeyStroke(Integer.parseInt(prefs[1]), Integer.parseInt(prefs[0]));
				// KeyStroke ks = KeyStroke.getKeyStroke(pref);
				if (ks != null) {
					super.setAccelerator(ks);
				}
			}
		}
	}

	protected String getAcceleratorPreferenceKey() {
		return Menu.MENU_ACCELERATOR + "_" + this.attribute;
	}

	@Override
	public boolean isDynamic() {
		return this.dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}

}
