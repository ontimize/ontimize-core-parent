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
import javax.swing.JCheckBoxMenuItem;
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
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.security.MenuPermission;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.util.ParseUtils;

/**
 * This class implements a menu item that can be selected or deselected. If selected, the menu item
 * typically appears with a checkmark next to it. If unselected or deselected, the menu item appears
 * without a checkmark.
 * <p>
 *
 * @author Imatia Innovation
 */
public class CheckMenuItem extends JCheckBoxMenuItem implements FormComponent, IdentifiedElement, SecureElement,
        StatusComponent, HasPreferenceComponent, IDynamicItem {

    private static final Logger logger = LoggerFactory.getLogger(CheckMenuItem.class);

    /**
     * The key for menu accelerator.
     */
    public static final String MENU_ACCELERATOR = "menu_accelerator";

    /**
     * The reference for status text. By default, null.
     */
    protected String statusText = null;

    String attribute = null;

    String shortcut = "";

    private MenuPermission visiblePermission = null;

    private MenuPermission enabledPermission = null;

    protected boolean dynamic = false;

    /**
     * The class constructor. Inits parameters, permissions and registers status component.
     * <p>
     * @param parameters the <code>Map</code> with parameters.
     */

    public CheckMenuItem(Map<Object, Object> parameters) {
        this.init(parameters);

        // Icon parameter
        Object icon = parameters.get("icon");

        if (icon != null) {
            this.setHorizontalAlignment(SwingConstants.LEFT);
            String sIconPath = icon.toString();
            ImageIcon iconCurrent = ImageManager.getIcon(sIconPath);
            if (iconCurrent != null) {
                this.setIcon(iconCurrent);
            }
        }

        Insets iMargins = this.getMargin();
        int iLeftMargin = iMargins.left;
        int iNewMargin = Math.max(0, iLeftMargin - 5);
        this.setMargin(new Insets(iMargins.top, iNewMargin, iMargins.bottom, iMargins.right));
        this.initPermissions();
        ApplicationManager.registerStatusComponent(this);
        this.dynamic = ParseUtils.getBoolean((String) parameters.get("dynamic"), false);
    }

    @Override
    public Object getConstraints(LayoutManager layout) {
        return null;
    }

    /**
     * Initializes parameters.
     * <p>
     * @param parameters the map with parameters
     *        <p>
     *        <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
     *        <tr>
     *        <td><b>attribute</td>
     *        <td><b>values</td>
     *        <td><b>default</td>
     *        <td><b>required</td>
     *        <td><b>meaning</td>
     *        </tr>
     *        <tr>
     *        <td>attr</td>
     *        <td></td>
     *        <td></td>
     *        <td>yes</td>
     *        <td>The attribute for tab panel.</td>
     *        </tr>
     *        <tr>
     *        <td>status</td>
     *        <td></td>
     *        <td>attr</td>
     *        <td>no</td>
     *        <td>The status text.</td>
     *        </tr>
     *        <tr>
     *        <td>checked</td>
     *        <td><i>yes/no or true/false</td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The checked condition.</td>
     *        </tr>
     *        <tr>
     *        <td>mnemonic</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The mnemonic for component.</td>
     *        </tr>
     *        <tr>
     *        <td>shorcut</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>The shorcut to access the component.</td>
     *        </tr>
     *        </TABLE>
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

        Object checked = parameters.get("checked");
        if (checked != null) {
            if (checked.toString().equalsIgnoreCase("yes") || checked.toString().equalsIgnoreCase("true")) {
                this.setSelected(true);
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
                CheckMenuItem.logger.trace(null, e);
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
    public void setResourceBundle(ResourceBundle resource) {
        try {
            if (resource != null) {
                this.setText(resource.getString(this.attribute));
            } else {
                this.setText(this.attribute);
            }
        } catch (Exception e) {
            if (com.ontimize.gui.ApplicationManager.DEBUG) {
                CheckMenuItem.logger.debug(null, e);
            } else {
                CheckMenuItem.logger.trace(null, e);
            }
        }
    }

    @Override
    public void setComponentLocale(Locale l) {
    }

    /**
     * Releases all listeners.
     */
    public void free() {
        EventListener[] listeners = this.getListeners(Action.class);
        for (int i = 0; i < listeners.length; i++) {
            this.removeActionListener((ActionListener) listeners[i]);
        }
        MenuElement[] children = this.getSubElements();
        // Free the menu components
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Freeable) {
                try {
                    ((Freeable) children[i]).free();
                } catch (Exception e) {
                    if (com.ontimize.gui.ApplicationManager.DEBUG) {
                        CheckMenuItem.logger.debug(
                                "Exception while free " + children[i].getClass().toString() + " : " + e.getMessage(),
                                e);
                    } else {
                        CheckMenuItem.logger.trace(null, e);
                    }
                }
            }
        }
        // Now remove the children
        this.removeAll();
        if (com.ontimize.gui.ApplicationManager.DEBUG) {
            CheckMenuItem.logger.debug(this.getClass().toString() + " free");
        }
    }

    @Override
    public void setVisible(boolean visible) {
        ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
        if (manager != null) {
            if (this.visiblePermission == null) {
                this.visiblePermission = new MenuPermission("visible", this.attribute, true);
            }
            try {
                // Checks to show
                if (visible) {
                    manager.checkPermission(this.visiblePermission);
                }
                super.setVisible(visible);
            } catch (Exception e) {
                if (ApplicationManager.DEBUG_SECURITY) {
                    CheckMenuItem.logger.debug(null, e);
                } else {
                    CheckMenuItem.logger.trace(null, e);
                }
            }
        } else {
            super.setVisible(visible);
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
                if (enabled) {
                    manager.checkPermission(this.enabledPermission);
                }
                this.restricted = false;
                super.setEnabled(enabled);
            } catch (Exception e) {
                this.restricted = true;
                if (ApplicationManager.DEBUG_SECURITY) {
                    CheckMenuItem.logger.debug(null, e);
                } else {
                    CheckMenuItem.logger.trace(null, e);
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
            } catch (Exception e) {
                this.restricted = true;
                super.setEnabled(false);
                if (ApplicationManager.DEBUG_SECURITY) {
                    CheckMenuItem.logger.debug(null, e);
                } else {
                    CheckMenuItem.logger.trace(null, e);
                }
            }
            if (this.visiblePermission == null) {
                this.visiblePermission = new MenuPermission("visible", this.attribute, true);
            }
            try {
                manager.checkPermission(this.visiblePermission);
            } catch (Exception e) {
                super.setVisible(false);
                if (ApplicationManager.DEBUG_SECURITY) {
                    CheckMenuItem.logger.debug(null, e);
                } else {
                    CheckMenuItem.logger.trace(null, e);
                }
            }
        }
    }

    @Override
    public String getStatusText() {
        return this.statusText;
    }

    /**
     * The restricted condition.
     */
    protected boolean restricted = false;

    @Override
    public boolean isRestricted() {
        return this.restricted;
    }

    @Override
    public void setAccelerator(KeyStroke ks) {
        super.setAccelerator(ks);
        // preferences
        ApplicationPreferences prefs = ApplicationManager.getApplication().getPreferences();
        if (prefs != null) {
            String user = null;
            if (ApplicationManager.getApplication().getReferenceLocator() instanceof ClientReferenceLocator) {
                user = ((ClientReferenceLocator) ApplicationManager.getApplication().getReferenceLocator()).getUser();
            }
            prefs.setPreference(user, this.getAcceleratorPreferenceKey(), ApplicationMenuBar.acceleratorToString(ks));
        }
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

    /**
     * Gets the accelerator preference key.
     * <p>
     * @return the accelerator preference key
     */
    protected String getAcceleratorPreferenceKey() {
        return CheckMenuItem.MENU_ACCELERATOR + "_" + this.attribute;
    }

    @Override
    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

}
