package com.ontimize.ols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.jee.common.ols.ILCheck;

public class CheckLComponent {

    private static final Logger logger = LoggerFactory.getLogger(CheckLComponent.class);

    private static Object obj = null;

    private static void init() {
        try {
            CheckLComponent.obj = ApplicationManager.getApplication().getReferenceLocator();
        } catch (Exception e) {
            CheckLComponent.logger.trace(null, e);
            try {
                Class<?> c = Class.forName("com.ontimize.ols.LCheck");
                CheckLComponent.obj = c.newInstance();
            } catch (Exception ex) {
                CheckLComponent.logger.trace(null, ex);
                CheckLComponent.obj = null;
            }
        }
    }

    public static boolean checkOk(String number) {
        if (CheckLComponent.obj == null) {
            CheckLComponent.init();
        }
        if (CheckLComponent.obj == null) {
            return false;
        }
        if (CheckLComponent.obj instanceof LOk) {
            try {
                return ((LOk) CheckLComponent.obj).ok(number);
            } catch (Exception ex) {
                CheckLComponent.logger.trace(null, ex);
            }
        }
        if (CheckLComponent.obj instanceof ILCheck) {
            try {
                return ((ILCheck) CheckLComponent.obj).ok(number);
            } catch (Exception ex) {
                CheckLComponent.logger.trace(null, ex);
            }
        }
        return false;
    }

}
