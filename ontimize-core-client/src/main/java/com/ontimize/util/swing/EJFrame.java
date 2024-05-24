package com.ontimize.util.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.util.incidences.FormCreateIncidences;

public class EJFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(EJFrame.class);

    public static boolean defaultValueAskQuestionOnClose = false;

    protected boolean askQuestionEverOnClose = EJFrame.defaultValueAskQuestionOnClose;

    public static final String closeQuestion = "ejframe.close_dialog";

    protected String sizePositionPreference = null;

    protected static Action[] actions = new Action[0];

    protected static KeyStroke[] keyStrokes = new KeyStroke[0];

    protected static String[] keys = new String[0];

    static {
        class EAction extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                if (w instanceof JFrame) {
                    ((JFrame) w).setVisible(false);
                }
            }

        }

        class EMaximized extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
                if (w instanceof JFrame) {
                    JFrame mainFrame = (JFrame) w;
                    mainFrame.setExtendedState(mainFrame.getExtendedState() | Frame.MAXIMIZED_BOTH);
                }
                ApplicationManager.maximize(SwingUtilities.getWindowAncestor((Component) e.getSource()));
            }

        }

        class ECreateIncidence extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                FormCreateIncidences incidence = new FormCreateIncidences(e.getSource());
                ApplicationManager.center(incidence);
                incidence.setVisible(true);
            }

        }

        EJFrame.setActionForKey(KeyEvent.VK_ESCAPE, 0, new EAction(), "Close window");
        EJFrame.setActionForKey(KeyEvent.VK_ADD, InputEvent.CTRL_MASK, new EMaximized(), "Maximized window");
        EJFrame.setActionForKey(KeyEvent.VK_I, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK, new ECreateIncidence(),
                "Create incidence");
    }

    public EJFrame() throws HeadlessException {
        super();
        this.registerKeyBindings();
        this.registerListeners();
    }

    public EJFrame(GraphicsConfiguration gc) {
        super(gc);
        this.registerKeyBindings();
        this.registerListeners();
    }

    public EJFrame(String title, GraphicsConfiguration gc) {
        super(title, gc);
        this.registerKeyBindings();
        this.registerListeners();
    }

    public EJFrame(String title) throws HeadlessException {
        super(title);
        this.registerKeyBindings();
        this.registerListeners();
    }

    protected void registerListeners() {
        this.addComponentListener(new WindowSaveSizePositionPreference());
    }

    protected void registerKeyBindings() {
        try {
            InputMap inMap = ((JComponent) this.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actMap = ((JComponent) this.getContentPane()).getActionMap();
            for (int i = 0; i < EJFrame.actions.length; i++) {
                inMap.put(EJFrame.keyStrokes[i], EJFrame.keys[i]);
                actMap.put(EJFrame.keys[i], EJFrame.actions[i]);
            }
        } catch (Exception e) {
            EJFrame.logger.error("Error registering keybindings : {}", e.getMessage(), e);
        }
    }

    public static void setActionForKey(int keyCode, int modifiers, Action action, String key) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers, false);
        Action[] a = new Action[EJFrame.actions.length + 1];
        for (int i = 0; i < EJFrame.actions.length; i++) {
            a[i] = EJFrame.actions[i];
        }
        a[a.length - 1] = action;
        KeyStroke[] k = new KeyStroke[EJFrame.keyStrokes.length + 1];
        for (int i = 0; i < EJFrame.keyStrokes.length; i++) {
            k[i] = EJFrame.keyStrokes[i];
        }
        k[k.length - 1] = ks;

        String[] ke = new String[EJFrame.keys.length + 1];
        for (int i = 0; i < EJFrame.keys.length; i++) {
            ke[i] = EJFrame.keys[i];
        }
        ke[ke.length - 1] = key;

        EJFrame.keys = ke;
        EJFrame.actions = a;
        EJFrame.keyStrokes = k;
    }

    public void setAction(int keyCode, int modifiers, Action action, String key) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers, true);
        try {
            InputMap inMap = ((JComponent) this.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actMap = ((JComponent) this.getContentPane()).getActionMap();
            inMap.put(ks, key);
            actMap.put(key, action);
        } catch (Exception e) {
            EJFrame.logger.error("Error registering keybindings.", e);
        }
    }

    protected boolean askCloseQuestion() {
        int result = MessageDialog.showMessage(this, EJFrame.closeQuestion, JOptionPane.QUESTION_MESSAGE,
                ApplicationManager.getApplicationBundle());
        return result == JOptionPane.YES_OPTION;
    }

    public boolean isAskOnClose() {
        return this.askQuestionEverOnClose;
    }

    /**
     * Sets the condition to ask a question before closing the dialog in any situation
     * @param askQuestionOnClose
     */
    public void setAskOnClose(boolean askQuestionOnClose) {
        this.askQuestionEverOnClose = askQuestionOnClose;
    }

    public void setSizePositionPreference(String s) {
        this.sizePositionPreference = s;
    }

    public String getSizePositionPreference() {
        return this.sizePositionPreference;
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if ((e.getID() == WindowEvent.WINDOW_CLOSING) && this.isAskOnClose()) {
            boolean close = this.askCloseQuestion();
            if (!close) {
                return;
            }
        }

        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            this.checkToSaveSizePrositionPreference();
        }
    }

    public void checkToSaveSizePrositionPreference() {
        try {
            if (this.sizePositionPreference != null) {
                ApplicationPreferences prefs = ApplicationManager.getApplication().getPreferences();
                if (prefs != null) {
                    String user = null;
                    try {
                        EntityReferenceLocator b = ApplicationManager.getApplication().getReferenceLocator();
                        if (b instanceof ClientReferenceLocator) {
                            user = ((ClientReferenceLocator) b).getUser();
                        }
                    } catch (Exception ex) {
                        EJFrame.logger.error("Error obtaining user to save the preferences", ex);
                    }
                    prefs.setPreference(user, this.sizePositionPreference,
                            this.getWidth() + ";" + this.getHeight() + ";" + this.getX() + ";" + this.getY() + ";"
                                    + this.getExtendedState());
                    // prefs.savePreferences();
                }
            }
        } catch (Exception ex) {
            EJFrame.logger.error("Error saving the preferences.", ex);
        }
    }

    @Override
    public void pack() {
        if (this.sizePositionPreference != null) {
            try {
                ApplicationPreferences prefs = ApplicationManager.getApplication().getPreferences();
                if (prefs != null) {
                    String user = null;
                    try {
                        EntityReferenceLocator b = ApplicationManager.getApplication().getReferenceLocator();
                        if (b instanceof ClientReferenceLocator) {
                            user = ((ClientReferenceLocator) b).getUser();
                        }
                    } catch (Exception ex) {
                        EJFrame.logger.error(null, ex);
                    }
                    String s = prefs.getPreference(user, this.sizePositionPreference);
                    if (s != null) {
                        String[] values = s.split(";");
                        if (values.length != 5) {
                            EJFrame.logger.debug("Invalid preference: " + this.sizePositionPreference + " : " + s);
                            super.pack();
                            return;
                        }
                        Dimension d = new Dimension(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                        Point p = new Point(Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                        if ((Double.compare(d.getWidth(), 0) != 0) && (Double.compare(d.getHeight(), 0) != 0)) {
                            this.setSize(d);
                        }

                        if (Integer.parseInt(values[4]) == 6) {
                            Point middlePoint = new Point(p.x + (d.width / 2), p.y + (d.height / 2));
                            Point middlePointChecked = ApplicationManager.checkAvailablePoint(middlePoint);
                            if (middlePoint.equals(middlePointChecked)) {
                                this.setLocation(p);
                                this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
                            } else {
                                this.setLocation(new Point(0, 0));
                                this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
                            }

                        } else {
                            Point leftCornerChecked = ApplicationManager.checkAvailablePoint(p);
                            if (p.equals(leftCornerChecked)) {
                                this.setLocation(p);
                            } else {
                                Point rightCorner = new Point(p.x + d.width, p.y);
                                Point rightCornerChecked = ApplicationManager.checkAvailablePoint(rightCorner);
                                if (rightCorner.equals(rightCornerChecked)) {
                                    this.setLocation(p);
                                } else {
                                    this.setLocation(new Point(0, 0));
                                }
                            }
                        }

                        // if (Integer.parseInt(values[4]) == 6) {
                        // Point middlePoint = new Point(p.x + (d.width / 2),
                        // p.y + (d.height / 2));
                        // middlePoint =
                        // ApplicationManager.checkAvailablePoint(middlePoint);
                        // this.setLocation(p);
                        // this.setExtendedState(this.getExtendedState() |
                        // Frame.MAXIMIZED_BOTH);
                        // } else {
                        // p = ApplicationManager.checkAvailablePoint(p);
                        // this.setLocation(p);
                        // }
                    } else {
                        super.pack();
                        ApplicationManager.center(this);
                    }
                }
            } catch (Exception ex1) {
                EJFrame.logger.trace(null, ex1);
            }
        } else {
            super.pack();
        }
    }

    public static class WindowSaveSizePositionPreference extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            if (e.getSource() instanceof EJFrame) {
                ((EJFrame) e.getSource()).checkToSaveSizePrositionPreference();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            super.componentMoved(e);
            if (e.getSource() instanceof EJFrame) {
                ((EJFrame) e.getSource()).checkToSaveSizePrositionPreference();
            }
        }

    }

}
