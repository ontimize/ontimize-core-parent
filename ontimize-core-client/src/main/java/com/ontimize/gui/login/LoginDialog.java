package com.ontimize.gui.login;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.field.CheckDataField;
import com.ontimize.gui.field.PasswordDataField;
import com.ontimize.gui.field.TextDataField;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.util.ParseUtils;
import com.ontimize.xml.DefaultXMLParametersManager;

public class LoginDialog extends AbstractLoginDialog implements MouseInputListener {

    private static final Logger logger = LoggerFactory.getLogger(LoginDialog.class);

    public static int topMargin = 123;

    public static int bottomMargin = 56;

    public static int leftMargin = 20;

    public static int rightMargin = 20;

    public static int statusUserGap = 10;

    public static int userPasswordGap = 4;

    public static int passwordCheckGap = 22;

    public static int comboCheckGap = 24;

    public static int buttonComboGap = 4;

    public static final Integer BACKGROUND_LAYER = new Integer(-60000);

    protected BackgroundPanel background = null;

    private Cursor lastCursor = null;

    protected static class BackgroundPanel extends JPanel implements ComponentListener {

        protected JLabel label = null;

        public BackgroundPanel(ImageIcon icon) {
            this.setOpaque(false);
            this.label = new JLabel(icon);
            this.setLayout(new BorderLayout());
            this.add(this.label, BorderLayout.CENTER);
        }

        @Override
        public void setOpaque(boolean arg0) {
            super.setOpaque(arg0);
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            this.setBounds(((JComponent) e.getSource()).getBounds());
            LoginDialog.logger.debug(e.getSource() + ":" + this.getPreferredSize());
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

    }

    public LoginDialog(Application mainApplication, Map<Object, Object> parameters, EntityReferenceLocator locator) {
        super(mainApplication, parameters, locator);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        this.setResizable(false);

        this.layoutComponents(parameters);
        ApplicationManager.center(this);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension backgroundDimension = this.background.getPreferredSize();
        return backgroundDimension;
    }

    protected void layoutComponents(Map<Object, Object> parameters) {
        ImageIcon icon = this.createImage(parameters);
        if (icon != null) {
            this.background = new BackgroundPanel(icon);
        }

        this.getLayeredPane().add(this.background, LoginDialog.BACKGROUND_LAYER);
        ((JComponent) this.getContentPane()).setOpaque(false);

        this.getContentPane().addComponentListener(this.background);

        this.getContentPane().setLayout(new GridBagLayout());
        JPanel panel = this.createCenterPanel(parameters);
        this.getContentPane()
            .add(panel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(LoginDialog.topMargin, LoginDialog.leftMargin, LoginDialog.bottomMargin,
                            LoginDialog.rightMargin),
                    0, 0));
        this.installListener();
        this.init(parameters);
        this.pack();
    }

    protected JPanel createCenterPanel(Map<Object, Object> parameters) {
        JPanel centerPanel = new JPanel(new GridBagLayout()) {

            @Override
            public Dimension getPreferredSize() {
                return super.getPreferredSize();
            }
        };
        centerPanel.setOpaque(false);

        this.status = this.createStatusLabel(parameters);

        centerPanel.add(this.status,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.SOUTH,
                        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        this.user = this.createLogin(parameters);
        centerPanel.add(this.user,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.NORTH,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(LoginDialog.statusUserGap, 0, 0, 0), 0, 0));

        this.password = this.createPassword(parameters);
        centerPanel.add(this.password,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.NORTH,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(LoginDialog.userPasswordGap, 0, 0, 0), 0, 0));

        this.rememberLogin = this.createRememberLogin(parameters);
        this.rememberPassword = this.createRememberPassword(parameters);

        JPanel checkRow = new JPanel(new GridBagLayout());
        checkRow.setOpaque(false);
        checkRow.add(this.rememberLogin, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        checkRow.add(this.rememberPassword, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        centerPanel.add(checkRow,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.CENTER,
                        GridBagConstraints.NONE,
                        new Insets(LoginDialog.passwordCheckGap, 0, 0, 0), 0, 0));

        this.serverCombo = this.createServerCombo(parameters);
        if (this.serverCombo != null) {
            JPanel connectToRow = new JPanel(new GridBagLayout());
            connectToRow.setOpaque(false);

            this.connectToLabel = this.createServerLabel();
            connectToRow.add(this.connectToLabel,
                    new GridBagConstraints(0, 0, 1, 1, 0.25, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                            new Insets(0, 0, 0, 0), 0, 0));
            connectToRow.add(this.serverCombo, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            centerPanel.add(connectToRow,
                    new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.NORTH,
                            GridBagConstraints.HORIZONTAL,
                            new Insets(LoginDialog.comboCheckGap, 0, 0, 0), 0, 0));
        }

        JPanel buttonRow = new JPanel(new GridBagLayout());

        buttonRow.setOpaque(false);

        this.acceptButton = this.createAcceptButton(parameters);
        this.acceptButton.setOpaque(false);
        this.certificateButton = this.createCertificatesButton(parameters);
        this.certificateButton.setOpaque(false);
        this.cancelButton = this.createCancelButton(parameters);
        this.cancelButton.setOpaque(false);
        this.cancelButton.addActionListener(new CancelListener());

        if (!((ClientReferenceLocator) locator).isAllowCertificateLogin()) {
            buttonRow.add(this.acceptButton, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.EAST,
                    GridBagConstraints.NONE, new Insets(2, 0, 2, 10), 30, 0));
            buttonRow.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
                    GridBagConstraints.NONE, new Insets(2, 10, 2, 0), 30, 0));
        } else {
            buttonRow.add(this.acceptButton, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.EAST,
                    GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
            buttonRow.add(this.certificateButton, new GridBagConstraints(1, 0, 1, 1, 0, 1, GridBagConstraints.CENTER,
                    GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
            buttonRow.add(this.cancelButton, new GridBagConstraints(2, 0, 1, 1, 1, 1, GridBagConstraints.WEST,
                    GridBagConstraints.NONE, new Insets(2, 0, 2, 0), 0, 0));
        }

        centerPanel.add(buttonRow,
                new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.SOUTH,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(LoginDialog.buttonComboGap, 0, 0, 0), 0, 0));
        return centerPanel;
    }

    public void installListener() {
        this.acceptButton.addActionListener(new AcceptListener());
        this.certificateButton.addActionListener(new CertificateListener());

        ((JComponent) this.getContentPane()).registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.acceptButton.doClick();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public List<String> getTextsToTranslate() {
    	List<String> v = new ArrayList<String>();
        this.addTextToTranslate(this.password, v);
        this.addTextToTranslate(this.user, v);
        this.addTextToTranslate(this.acceptButton, v);
        this.addTextToTranslate(this.cancelButton, v);
        this.addTextToTranslate(this.rememberLogin, v);
        this.addTextToTranslate(this.rememberPassword, v);
        return v;
    }

    protected void addTextToTranslate(Internationalization component, List<String> texts) {
        if (component != null) {
            List<String> textsToTranslate = component.getTextsToTranslate();
            if ((textsToTranslate != null) && (!textsToTranslate.isEmpty())) {
                texts.addAll(textsToTranslate);
            }
        }
    }

    @Override
    public void setComponentLocale(Locale l) {

    }

    @Override
    public void setResourceBundle(ResourceBundle resourceBundle) {
        super.setResourceBundle(resourceBundle);
    }

    @Override
    protected CheckDataField createRememberLogin(Map<Object, Object> parameters) {
    	Map<Object, Object> p = DefaultXMLParametersManager.getParameters(AbstractLoginDialog.LOGIN_CHECK_DATA_FIELD);
        p.put("attr", "RememberLogin");
        p.put("labelposition", "left");

        CheckDataField check = new CheckDataField(p);
        Font font = Font.decode("Arial-PLAIN-11");
        check.setFont(font);
        try {
            check.setForeground(ColorConstants.parseColor("#333333"));
        } catch (Exception exc) {
            LoginDialog.logger.error(null, exc);
        }

        return check;
    }

    @Override
    protected CheckDataField createRememberPassword(Map<Object, Object> parameters) {
    	Map<Object, Object> p = DefaultXMLParametersManager.getParameters(AbstractLoginDialog.LOGIN_CHECK_DATA_FIELD);
        p.put("attr", "RememberPassword");
        p.put("labelposition", "left");

        CheckDataField check = new CheckDataField(p);
        Font font = Font.decode("Arial-PLAIN-11");
        check.setFont(font);
        try {
            check.setForeground(ColorConstants.parseColor("#333333"));
        } catch (Exception exc) {
            LoginDialog.logger.error(null, exc);
        }
        return check;
    }

    @Override
    protected TextDataField createLogin(Map<Object, Object> parameters) {
    	Map<Object, Object> p = DefaultXMLParametersManager.getParameters(AbstractLoginDialog.LOGIN_TEXT_DATA_FIELD);
        if (!p.containsKey("attr")) {
            p.put("attr", "User_");
        }
        if (!p.containsKey("dim")) {
            p.put("dim", "text");
        }
        if (!p.containsKey("labelsize")) {
            p.put("labelsize", "10");
        }

        TextDataField text = new TextDataField(p) {

            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);
            }

            @Override
            public Dimension getPreferredSize() {
                return super.getPreferredSize();
            }

            @Override
            public Dimension getMinimumSize() {
                return super.getMinimumSize();
            }
        };
        Font font = ParseUtils.getFont((String) p.get("font"), Font.decode("Arial-BOLD-12"));
        text.setFont(font);
        try {
            text.setForeground(ParseUtils.getColor((String) p.get("fontcolor"), ColorConstants.parseColor("#333333")));
            text.setLabelFontColor(
                    ParseUtils.getColor((String) p.get("labelfontcolor"), ColorConstants.parseColor("#333333")));
        } catch (Exception exc) {
            LoginDialog.logger.error(null, exc);
        }
        return text;
    }

    @Override
    protected PasswordDataField createPassword(Map<Object, Object> parameters) {
    	Map<Object, Object> p2 = DefaultXMLParametersManager.getParameters(AbstractLoginDialog.LOGIN_PASSWORD_DATA_FIELD);
        if (!p2.containsKey("attr")) {
            p2.put("attr", "Password");
        }
        if (!p2.containsKey("dim")) {
            p2.put("dim", "text");
        }
        if (!p2.containsKey("labelsize")) {
            p2.put("labelsize", "10");
        }
        if (parameters.containsKey(ILoginDialog.ENCRYPT)) {
            boolean encrypt = ApplicationManager.parseStringValue((String) parameters.get(ILoginDialog.ENCRYPT));
            if (encrypt) {
                p2.put("encrypt", "yes");
            }
        }
        PasswordDataField text = new PasswordDataField(p2);
        Font font = ParseUtils.getFont((String) p2.get("font"), Font.decode("Arial-BOLD-12"));
        text.setFont(font);
        try {
            text.setForeground(ParseUtils.getColor((String) p2.get("fontcolor"), ColorConstants.parseColor("#333333")));
            text.setLabelFontColor(
                    ParseUtils.getColor((String) p2.get("labelfontcolor"), ColorConstants.parseColor("#333333")));
        } catch (Exception exc) {
            LoginDialog.logger.error(null, exc);
        }
        return text;
    }

    @Override
    protected JLabel createStatusLabel(Map<Object, Object> parameters) {
        JLabel label = new JLabel("", SwingConstants.CENTER) {

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                try {
                    FontMetrics fontMetrics = this.getFontMetrics(this.getFont().deriveFont(Font.PLAIN));
                    return new Dimension(d.width, fontMetrics.getHeight());
                } catch (Exception e) {
                    LoginDialog.logger.error(null, e);
                }
                return d;
            }
        };

        Font font = Font.decode("Arial-PLAIN-11");
        label.setFont(font);
        try {
            this.statusBarForeground = ColorConstants.parseColor("#cc3333");
            label.setForeground(this.statusBarForeground);
        } catch (Exception e) {
            LoginDialog.logger.error(null, e);
        }
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        return label;
    }

    /**
     * Set to true if the drag operation is moving the window.
     */
    private boolean isMovingWindow;

    /**
     * Used to determine the corner the resize is occuring from.
     */
    private int dragCursor;

    /**
     * X location the mouse went down on for a drag operation.
     */
    private int dragOffsetX;

    /**
     * Y location the mouse went down on for a drag operation.
     */
    private int dragOffsetY;

    /**
     * Width of the window when the drag started.
     */
    private int dragWidth;

    /**
     * Height of the window when the drag started.
     */
    private int dragHeight;

    @Override
    public void mousePressed(MouseEvent ev) {
        Point dragWindowOffset = ev.getPoint();
        Window w = (Window) ev.getSource();
        if (w != null) {
            w.toFront();
        }

        this.isMovingWindow = true;
        this.dragOffsetX = dragWindowOffset.x;
        this.dragOffsetY = dragWindowOffset.y;
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        if (this.dragCursor != 0) {
            this.validate();
            this.getRootPane().repaint();
        }
        this.isMovingWindow = false;
        this.dragCursor = 0;
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
        Window w = (Window) ev.getSource();
        Point pt = ev.getPoint();

        if (this.isMovingWindow) {
            Point windowPt = new Point();
            try {
                windowPt.x = pt.x - this.dragOffsetX;
                windowPt.y = pt.y - this.dragOffsetY;
                SwingUtilities.convertPointToScreen(windowPt, this);
                w.setLocation(windowPt);
            } catch (Exception e) {
                LoginDialog.logger.trace(null, e);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent ev) {
        Window w = (Window) ev.getSource();
        this.lastCursor = w.getCursor();
    }

    @Override
    public void mouseExited(MouseEvent ev) {
        Window w = (Window) ev.getSource();
        w.setCursor(this.lastCursor);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (!LoginDialog.this.user.isEmpty()) {
                        LoginDialog.this.password.requestFocus();
                    } else {
                        LoginDialog.this.user.requestFocus();
                    }
                }
            });
        }
    }

}
