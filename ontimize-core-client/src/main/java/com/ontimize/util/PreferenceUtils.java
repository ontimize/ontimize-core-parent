package com.ontimize.util;

import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public class PreferenceUtils {

    public static void deletePreference(String keyPreference, String user) {
        if (keyPreference == null) {
            return;
        }
        Application ap = ApplicationManager.getApplication();
        ApplicationPreferences prefs = ap.getPreferences();
        if (prefs != null) {
            String pref = prefs.getPreference(user, keyPreference);
            if (pref != null) {
                prefs.setPreference(user, keyPreference, null);
                prefs.savePreferences();
            }
        }
    }

    public static String loadPreference(String keyPreference) {
        EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
        String user = null;
        if (locator instanceof ClientReferenceLocator) {
            user = ((ClientReferenceLocator) locator).getUser();
        }
        return PreferenceUtils.loadPreference(keyPreference, user);
    }

    public static String loadPreference(String keyPreference, String user) {
        Application ap = ApplicationManager.getApplication();
        ApplicationPreferences prefs = ap.getPreferences();
        return PreferenceUtils.loadPreference(keyPreference, user, prefs);
    }

    public static String loadPreference(String keyPreference, String user, ApplicationPreferences prefs) {
        if (keyPreference == null) {
            return null;
        }
        if (prefs != null) {
            return prefs.getPreference(user, keyPreference);
        }
        return null;
    }

    public static String loadPreference(String keyPreference, ApplicationPreferences prefs) {
        EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
        String user = null;
        if ((locator != null) && (locator instanceof ClientReferenceLocator)) {
            user = ((ClientReferenceLocator) locator).getUser();
        }
        return PreferenceUtils.loadPreference(keyPreference, user, prefs);
    }

    public static void savePreference(String keyPreference, String preference, ApplicationPreferences prefs) {
        EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
        String user = null;
        if ((locator != null) && (locator instanceof ClientReferenceLocator)) {
            user = ((ClientReferenceLocator) locator).getUser();
        }
        PreferenceUtils.savePreference(keyPreference, preference, user);
    }

    public static void savePreference(String keyPreference, String preference, String user,
            ApplicationPreferences prefs) {
        if (keyPreference == null) {
            return;
        }
        if (prefs != null) {
            prefs.setPreference(user, keyPreference, preference);
            prefs.savePreferences();
        }
    }

    public static void savePreference(String keyPreference, String preference) {
        EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
        String user = null;
        if ((locator != null) && (locator instanceof ClientReferenceLocator)) {
            user = ((ClientReferenceLocator) locator).getUser();
        }
        PreferenceUtils.savePreference(keyPreference, preference, user);
    }

    public static void savePreference(String keyPreference, String preference, String user) {
        if (keyPreference == null) {
            return;
        }
        Application ap = ApplicationManager.getApplication();
        ApplicationPreferences prefs = ap.getPreferences();
        PreferenceUtils.savePreference(keyPreference, preference, user, prefs);
    }

}
