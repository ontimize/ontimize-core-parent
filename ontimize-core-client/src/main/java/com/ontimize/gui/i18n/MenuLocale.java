package com.ontimize.gui.i18n;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MainApplication;
import com.ontimize.gui.Menu;
import com.ontimize.locator.ClientReferenceLocator;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.InitialContext;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.util.ParseUtils;

/**
 * Menu component that implements a submenu to select the application locale <br>
 * Children elements in this menu must be ItemMenuLocale elements.<br>
 * When the locale changes a LocalEvent is fired
 */

public class MenuLocale extends Menu {

    private static final Logger logger = LoggerFactory.getLogger(MenuLocale.class);

    protected String bundle = null;

    protected ButtonGroup buttonGroup = new ButtonGroup();

    protected boolean autoconfigureItems;

    protected EventListenerList localeListenerList = new EventListenerList();

    ItemListener listener = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent ev) {
            // Event source
            Object oSource = ev.getSource();
            if (oSource instanceof LocaleMenuItem) {
                if (((LocaleMenuItem) oSource).isSelected()) {
                    MenuLocale.this.fireLocaleChange(((LocaleMenuItem) oSource).getLocale());
                }
            }
        }
    };

    public MenuLocale(Hashtable parameters) {
        super(parameters);
        Object resourceBundle = parameters.get("resourceBundle");
        if (resourceBundle != null) {
            this.bundle = resourceBundle.toString();
        } else {
            MenuLocale.logger.debug(this.getClass().toString() + " : Parameter 'resourceBundle' not found");
        }

        if (this.autoconfigureItems && ExtendedPropertiesBundle.isUsingDatabaseBundle()) {
            try {
                String dbBundleManagerName = ExtendedPropertiesBundle.getDbBundleManagerName();
                MainApplication mainApplication = (MainApplication) ApplicationManager.getApplication();
                EntityReferenceLocator locator = mainApplication.getReferenceLocator();

                InitialContext initial = ((ClientReferenceLocator) locator).getInitialContext();
                String[] locales = null;
                if ((initial != null) && initial.containsKey(InitialContext.AVAILABLE_LOCALES)) {
                    locales = (String[]) initial.get(InitialContext.AVAILABLE_LOCALES);
                }

                if (locales == null) {
                    IDatabaseBundleManager remoteReference = (IDatabaseBundleManager) ((UtilReferenceLocator) locator)
                        .getRemoteReference(dbBundleManagerName,
                                locator.getSessionId());
                    locales = remoteReference.getAvailableLocales(locator.getSessionId());
                }

                if (locales != null) {
                    for (int i = 0; i < locales.length; i++) {
                        Hashtable itemParams = new Hashtable();
                        itemParams.put("locale", locales[i]);
                        itemParams.put("attr", locales[i]);
                        LocaleMenuItem item = new LocaleMenuItem(itemParams);
                        this.add(item);
                    }
                }
            } catch (Exception e) {
                MenuLocale.logger.error(null, e);
            }
        }
    }

    @Override
    public void init(Hashtable parameters) {
        super.init(parameters);
        this.autoconfigureItems = ParseUtils.getBoolean((String) parameters.get("autoconfigureitems"), false);
    }

    @Override
    public JMenuItem add(JMenuItem itemMenu) {
        if (itemMenu instanceof LocaleMenuItem) {
            if (this.autoconfigureItems) {
                // Before insert a new item check if another exist with the same
                // locale
                Locale localeItem = ((LocaleMenuItem) itemMenu).getLocale();
                this.removeItemWithLocale(localeItem);
            }
            ((LocaleMenuItem) itemMenu).addItemListener(this.listener);
            this.buttonGroup.add(itemMenu);
        }
        return super.add(itemMenu);
    }

    @Override
    public Component add(Component itemMenu, int index) {
        if (itemMenu instanceof LocaleMenuItem) {
            if (this.autoconfigureItems) {
                // Before insert a new item check if another exist with the same
                // locale
                Locale localeItem = ((LocaleMenuItem) itemMenu).getLocale();
                this.removeItemWithLocale(localeItem);
            }
            ((LocaleMenuItem) itemMenu).addItemListener(this.listener);
            this.buttonGroup.add((LocaleMenuItem) itemMenu);
        }
        return super.add(itemMenu, index);
    }

    protected void removeItemWithLocale(Locale locale) {
        Enumeration elements = this.buttonGroup.getElements();

        while (elements.hasMoreElements()) {
            Object element = elements.nextElement();
            if (element instanceof LocaleMenuItem) {
                if (((LocaleMenuItem) element).getLocale().equals(locale)) {
                    ((LocaleMenuItem) element).removeItemListener(this.listener);
                    this.buttonGroup.remove((LocaleMenuItem) element);
                    super.remove((LocaleMenuItem) element);
                    break;
                }
            }
        }
    }

    /**
     * Adds an <code>LocaleListener</code> to the Menu.
     * @param l the <code>LocaleListener</code> to be added
     */
    public void addLocaleListener(LocaleListener listener) {
        this.localeListenerList.add(LocaleListener.class, listener);
    }

    /**
     * Removes an <code>LocaleListener</code> from the Menu.
     * @param l the listener to be removed
     */
    public void removeLocaleListener(LocaleListener listener) {
        this.localeListenerList.remove(LocaleListener.class, listener);
    }

    protected void fireLocaleChange(Locale l) {
        Object[] listeners = this.localeListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == LocaleListener.class) {
                ((LocaleListener) listeners[i + 1]).localeChange(new LocaleEvent(this, l, this.bundle));
            }
        }
    }

}
