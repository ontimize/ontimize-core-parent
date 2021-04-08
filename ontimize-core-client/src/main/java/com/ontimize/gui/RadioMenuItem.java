package com.ontimize.gui;

import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.preferences.HasPreferenceComponent;
import com.ontimize.locator.ClientReferenceLocator;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.security.MenuPermission;
import com.ontimize.util.ParseUtils;

/**
 * This class implements a radio menu item.
 * <p>
 *
 * @author Imatia Innovation
 */
public class RadioMenuItem extends JRadioButtonMenuItem implements FormComponent, IdentifiedElement, SecureElement,
        StatusComponent, HasPreferenceComponent, IDynamicItem, Freeable {

    private static final Logger logger = LoggerFactory.getLogger(RadioMenuItem.class);

    /**
     * The menu accelerator key.
     */
    public static final String MENU_ACCELERATOR = "menu_accelerator";

    /**
     * The status text reference. By default, null.
     */
    protected String statusText = null;

    String attribute = null;

    String shortcut = "";

    private MenuPermission visiblePermission = null;

    private MenuPermission enabledPermission = null;

    protected boolean dynamic = false;

    /**
     * The class constructor. Calls to super, initializes parameters and permissions and sets margin.
     * <p>
     * @param parameters the hashtable with parameters
     */
    public RadioMenuItem(Hashtable parameters) {
        super();
        this.init(parameters);
        Insets margins = this.getMargin();
        int leftmargin = margins.left;
        int newMargin = Math.max(0, leftmargin - 5);
        this.setMargin(new Insets(margins.top, newMargin, margins.bottom, margins.right));
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
     * @param parameters the <code>Hashtable</code> with parameters. The next parameters are added:
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
     *        <td>The attribute values.</td>
     *        </tr>
     *        <tr>
     *        <td>status</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>Indicates the status of component.</td>
     *        </tr>
     *        <tr>
     *        <td>checked</td>
     *        <td>yes/no</td>
     *        <td>no</td>
     *        <td>no</td>
     *        <td>Indicates the type of separator.</td>
     *        </tr>
     *        <tr>
     *        <td>mnemonic</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>Indicates the mnemonic of field.</td>
     *        </tr>
     *        <tr>
     *        <td>shortcut</td>
     *        <td></td>
     *        <td></td>
     *        <td>no</td>
     *        <td>Indicates a shortcut to radio menu.</td>
     *        </tr>
     *        </Table>
     */
    @Override
    public void init(Hashtable parameters) {
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
                RadioMenuItem.logger.trace(null, e);
            }
        }
    }

    @Override
    public Object getAttribute() {
        return this.attribute;
    }

    @Override
    public Vector getTextsToTranslate() {
        Vector v = new Vector();
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
        } catch (Exception e) {
            if (com.ontimize.gui.ApplicationManager.DEBUG) {
                RadioMenuItem.logger.debug(null, e);
            } else {
                RadioMenuItem.logger.trace(null, e);
            }
        }
    }

    @Override
    public void setComponentLocale(Locale l) {
    }

    /**
     * Releases all listeners.
     */
    @Override
    public void free() {
        EventListener[] listeners = this.getListeners(Action.class);
        for (int i = 0; i < listeners.length; i++) {
            this.removeActionListener((ActionListener) listeners[i]);
        }
        MenuElement[] menuChildren = this.getSubElements();
        // Free the menu components
        for (int i = 0; i < menuChildren.length; i++) {
            if (menuChildren[i] instanceof Freeable) {
                try {
                    ((Freeable) menuChildren[i]).free();
                } catch (Exception e) {
                    if (com.ontimize.gui.ApplicationManager.DEBUG) {
                        RadioMenuItem.logger
                            .debug("Exception in free() method " + menuChildren[i].getClass().toString(), e);
                    } else {
                        RadioMenuItem.logger
                            .trace("Exception in free() method " + menuChildren[i].getClass().toString(), e);
                    }
                }
            }
        }
        // Remove the children
        this.removeAll();
        if (com.ontimize.gui.ApplicationManager.DEBUG) {
            RadioMenuItem.logger.debug(this.getClass().toString() + " Free");
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
                if (vis) {
                    manager.checkPermission(this.visiblePermission);
                }
                super.setVisible(vis);
            } catch (Exception e) {
                if (ApplicationManager.DEBUG_SECURITY) {
                    RadioMenuItem.logger.debug(null, e);
                } else {
                    RadioMenuItem.logger.trace(null, e);
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
                if ((enabled) && (manager != null)) {
                    manager.checkPermission(this.enabledPermission);
                }
                super.setEnabled(enabled);
            } catch (Exception e) {
                if (ApplicationManager.DEBUG_SECURITY) {
                    RadioMenuItem.logger.debug(null, e);
                } else {
                    RadioMenuItem.logger.trace(null, e);
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
                    RadioMenuItem.logger.debug(null, e);
                } else {
                    RadioMenuItem.logger.trace(null, e);
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
                    RadioMenuItem.logger.debug(null, e);
                } else {
                    RadioMenuItem.logger.trace(null, e);
                }
            }
        }
    }

    @Override
    public String getStatusText() {
        return this.statusText;
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
                KeyStroke ks = null;
                String prefs[] = pref.split(" ");
                try {
                    ks = KeyStroke.getKeyStroke(Integer.parseInt(prefs[1]), Integer.parseInt(prefs[0]));
                } catch (Exception e) {
                    RadioMenuItem.logger.trace(null, e);
                }
                if (ks != null) {
                    super.setAccelerator(ks);
                }
            }
        }
    }

    /**
     * Gets the accelerator preference key.
     * <p>
     * @return the concatenation: menu_accelerator + attribute.
     */
    protected String getAcceleratorPreferenceKey() {
        return RadioMenuItem.MENU_ACCELERATOR + "_" + this.attribute;
    }

    @Override
    public boolean isDynamic() {
        return this.dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

}
