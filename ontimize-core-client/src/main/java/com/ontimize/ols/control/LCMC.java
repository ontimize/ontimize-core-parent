package com.ontimize.ols.control;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.rmi.Naming;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.ols.RemoteLControlAdministration;
import com.ontimize.jee.common.security.GeneralSecurityException;
import com.ontimize.jee.common.security.License;
import com.ontimize.jee.common.security.ModeErrorSecurityException;
import com.ontimize.ols.LControl;
import com.ontimize.ols.SerialNumber;
import com.ontimize.ols.WindowLError;

public class LCMC extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(LCMC.class);

    public static boolean DEBUG = false;

    private static String serverIP = null;

    private static String serverPort = null;

    private static String serviceName = null;

    private static String userAdministration = null;

    private static String passwordAdministration = null;

    private boolean connected = false;

    private boolean ok = false;

    private boolean haspermission = false;

    private SerialNumber sn = null;

    private LPanel lp = null;

    private final JLabel pc = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private boolean pcok = false;

    private final JLabel dt = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private final JLabel ips = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private final JLabel macs = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private final JLabel bSConnect = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private final JTextField bShared = new JTextField(4);

    private JButton connect = null;

    private JButton refresh = null;

    private JButton update = null;

    private JButton help = null;

    private JButton close = null;

    private final JTextArea licenseFileAsString = new JTextArea();

    private Table tbCodes = null;

    private String lchmText = "LCMC.CLICK_CONNECT";

    private ResourceBundle bundle = null;

    private static LControl lc = null;

    private static RemoteLControlAdministration rlc = null;

    private static final String path = "prop/LCMC.properties";

    private static final String IPServerName = "IPServer";

    private static String IPCServer = null;

    static {
        Properties prop = new Properties();
        InputStream is = LCMC.class.getResourceAsStream(LCMC.path);
        try {
            if (is != null) {
                prop.load(is);
            }
            if (prop.getProperty(LCMC.IPServerName) != null) {
                LCMC.IPCServer = prop.getProperty(LCMC.IPServerName);
            }
        } catch (Exception e) {
            LCMC.logger.error(null, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LCMC.logger.error(null, e);
                }
            }
        }

    }

    public static String getIPConnectionServer() {
        return LCMC.IPCServer;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("connect")) {
            this.connect(true);
        }
        if (e.getActionCommand().equals("refresh")) {
            this.refresh();
        }
        if (e.getActionCommand().equals("update")) {
            this.update();// (Dialog)SwingUtilities.getWindowAncestor((Component)e.getSource())
        }
        if (e.getActionCommand().equals("help")) {
            Object o = SwingUtilities.getWindowAncestor((Component) e.getSource());
            if (o instanceof Frame) {
                JOptionPane.showMessageDialog((Frame) o, ApplicationManager.getTranslation(this.lchmText, this.bundle),
                        ApplicationManager.getTranslation("LCMC.HELP", this.bundle),
                        JOptionPane.QUESTION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog((Dialog) o, ApplicationManager.getTranslation(this.lchmText, this.bundle),
                        ApplicationManager.getTranslation("LCMC.HELP", this.bundle), JOptionPane.QUESTION_MESSAGE);
            }
        }
        if (e.getActionCommand().equals("close")) {
            if (SwingUtilities.getWindowAncestor((Component) e.getSource()) != null) {
                SwingUtilities.getWindowAncestor((Component) e.getSource()).setVisible(false);
            }
        }
    }

    private void initHowToConnect() throws Exception {
        if (ApplicationManager.getApplication() != null) {
            LCMC.lc = (LControl) ApplicationManager.getApplication().getReferenceLocator();
            LCMC.rlc = null;
        } else {
            Object o = Naming.lookup("//" + LCMC.serverIP + ":" + LCMC.serverPort + "/" + LCMC.serviceName);
            LCMC.rlc = (RemoteLControlAdministration) o;
            LCMC.lc = null;
        }
    }

    private Map<Object, Object> getParametersInterface() throws Exception {
        if (LCMC.lc != null) {
            return LCMC.lc.getParameters();
        }
        if (LCMC.rlc != null) {
            return LCMC.rlc.getParameters(LCMC.userAdministration, LCMC.passwordAdministration);
        }
        return null;
    }

    private Map<Object, Object> updateInterface(Map<Object, Object> h) throws Exception {
        if (LCMC.lc != null) {
            return LCMC.lc.updateL(h);
        }
        if (LCMC.rlc != null) {
            return LCMC.rlc.updateL(h, LCMC.userAdministration, LCMC.passwordAdministration);
        }
        return null;
    }

    private boolean hasConnectionInterface() {
        return (LCMC.lc != null) || (LCMC.rlc != null);
    }

    private License getLF() {
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setSize(400, 300);
        if (jfc.showDialog(null, "Licencia") == JFileChooser.APPROVE_OPTION) {
            License lic = null;
            try {
                FileInputStream fis = new FileInputStream(jfc.getSelectedFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
                lic = (License) ois.readObject();
            } catch (Exception e) {
                LCMC.logger.trace(null, e);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.NOT_A_LICENSE_FILE", this.bundle),
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return lic;
        }
        return null;
    }

    /**
     * Solicita al servidor de aplicaciones que realize una conexion contra el SL para solicitar una
     * nueva licencia
     */
    public void update() {

        if (!this.pcok) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.NO_PRODUCT_CODE_CONTACT_WITH_ADMINISTRATOR", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<Object, Object> h = new HashMap<>();
        String responsable = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this),
                ApplicationManager.getTranslation("LCMC.RESPONSABLE", this.bundle),
                ApplicationManager.getTranslation("LCMC.QUESTION", this.bundle), JOptionPane.QUESTION_MESSAGE);
        if ((responsable == null) || responsable.equals("")) {
            return;
        }
        h.put("Responsable", responsable);

        String string1 = ApplicationManager.getTranslation("LCMC.ASK_SL", this.bundle);
        String string2 = ApplicationManager.getTranslation("LCMC.GIVE_LOCAL_LICENSE_FILE", this.bundle);
        Object[] options = { string1, string2 };
        int n = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(this),
                ApplicationManager.getTranslation("LCMC.ASK_SL_OR_GIVE_FILE", this.bundle),
                ApplicationManager.getTranslation("LCMC.QUESTION", this.bundle), JOptionPane.YES_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, string1);

        if ((n != JOptionPane.YES_OPTION) && (n != JOptionPane.NO_OPTION)) {
            return;
        }

        if (n == JOptionPane.YES_OPTION) {
            if (this.sn.getSerialNumber() == null) {
                String s = SerialNumber.showSerialNumber(SwingUtilities.getWindowAncestor(this), this.bundle, true,
                        false, true);
                if (s == null) {
                    return;
                }
                h.put("Serial", s);
            }

            String ips = null;
            if (SwingUtilities.getWindowAncestor(this) instanceof JDialog) {
                // ips =
                // JOptionPane.showInputDialog((JDialog)SwingUtilities.getWindowAncestor(this),
                // ApplicationManager.getTraduccion("LCMC.HELP_TEXT",this.bundle),
                // IPCServer);

                ips = (String) JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.HELP_TEXT", this.bundle),
                        ApplicationManager.getTranslation("LCMC.HELP_TEXT_TITLE", this.bundle),
                        JOptionPane.QUESTION_MESSAGE, null, null, LCMC.IPCServer);
            } else {
                ips = (String) JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.HELP_TEXT", this.bundle),
                        ApplicationManager.getTranslation("LCMC.HELP_TEXT_TITLE", this.bundle),
                        JOptionPane.QUESTION_MESSAGE, null, null, LCMC.IPCServer);
            }

            if ((ips == null) || ips.equals("")) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.NO_IPSERVER", this.bundle),
                        ApplicationManager.getTranslation("LCMC.NO_IPSERVER_TITLE", this.bundle),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            h.put("ServerWeb", ips);

        }
        if (n == JOptionPane.NO_OPTION) {
            License lic = this.getLF();
            if (lic == null) {
                return;
            }
            h.put("License", lic);
        }

        Map<Object, Object> hr = null;
        try {
            hr = this.updateInterface(h);
        } catch (GeneralSecurityException exc) {
            LCMC.logger.trace(null, exc);
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.NO_PERMISSION", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception exc) {
            LCMC.logger.error(null, exc);
            this.connected = false;
            this.activateButtons();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.CONNECTION_ERROR", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (hr == null) {
            this.connected = false;
            this.activateButtons();
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.CONNECTION_ERROR", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!((Boolean) hr.get("Result")).booleanValue()) {
            // Hubo error => lo comprobamos
            String s = (String) hr.get("Message");
            String out = null;

            boolean weberror = false;

            if (s.indexOf("LVM.RESULT_CODE") != -1) {
                out = ApplicationManager.getTranslation("LVM.RESULT_CODE", this.bundle);
                out += s.substring(s.indexOf("LVM.RESULT_CODE") + "LVM.RESULT_CODE".length()) + "</body></html>";
            } else {

                if (s.indexOf("NOT_VALID_LICENSE") != -1) {
                    out = ApplicationManager.getTranslation("LVM.NOT_VALID_LICENSE", this.bundle);
                    out += s.substring(s.lastIndexOf(".") + 1)
                            + ApplicationManager.getTranslation("LVM.NOT_VALID_LICENSE_2", this.bundle);
                } else {
                    if (s.indexOf("<html") == -1) {
                        out = ApplicationManager.getTranslation(s, this.bundle);
                    } else {
                        weberror = true;
                    }
                }
            }
            if (!weberror) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), out,
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            } else {
                URLError.showURLError(SwingUtilities.getWindowAncestor(this), this.bundle, s);
            }

            if (hr.get("URL_TO_CONNECT") != null) {
                URL url = (URL) hr.get("URL_TO_CONNECT");
                DURLToConnect.showDURLToConnect(SwingUtilities.getWindowAncestor(this), this.bundle, url.toString());
            }

        } else {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.LICENSE_UPDATED", this.bundle),
                    ApplicationManager.getTranslation("LCMC.MESSAGE", this.bundle), JOptionPane.INFORMATION_MESSAGE);
        }
        this.refresh();
    }

    /**
     * Clear the values in the window
     */
    public void clearValues() {
        this.sn.clearSerialNumber();
        this.lp.clear();
        this.licenseFileAsString.setText("");
        this.tbCodes.setValue(null);
        this.haspermission = false;
    }

    private void activateButtons() {
        this.activateButtons(false);
    }

    private void activateButtons(boolean frik) {
        this.connect.setEnabled(true);
        this.refresh.setEnabled(this.connected);

        if (!frik) {
            this.update.setEnabled(this.haspermission && this.connected);
            if (this.connected) {
                if (this.bShared.getText().equals("Yes")) {
                    this.update.setEnabled(false);
                }
            }
        } else {
            this.update.setEnabled(false);
        }

        if (this.connected) {
            this.dt.setIcon(ImageManager.getIcon(ImageManager.OK));
        } else {
            this.dt.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
            this.pc.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
            this.ips.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
            this.macs.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
        }
    }

    private void onlyClose() {
        this.connect.setEnabled(false);
        this.refresh.setEnabled(false);
        this.update.setEnabled(false);
        this.help.setEnabled(false);
        this.close.setEnabled(true);
    }

    /**
     * Comprueba la conexion con el servicio de control de licencias. Si se logra conectar realiza una
     * peticion de los datos.
     */
    public void connect(boolean alert) {
        this.clearValues();
        try {

            this.initHowToConnect();
            if (this.hasConnectionInterface()) {
                if (alert) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                            ApplicationManager.getTranslation("LCMC.CONNECTION_OK", this.bundle),
                            ApplicationManager.getTranslation("LCMC.MESSAGE", this.bundle),
                            JOptionPane.INFORMATION_MESSAGE);
                }
                this.connected = true;
                this.refresh();
                return;
            } else {
                if (alert) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                            ApplicationManager.getTranslation("LCMC.CONNECTION_ERROR", this.bundle),
                            ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
                }
                this.connected = false;
                this.lchmText = "LCMC.HELP_NOT_CONNECTED";
            }
        } catch (Exception e) {
            LCMC.logger.error(null, e);
            if (alert) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.CONNECTION_ERROR", this.bundle),
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            }
            this.connected = false;
            this.lchmText = ApplicationManager.getTranslation("LCMC.HELP_NOT_CONNECTED", this.bundle);
        }
        this.activateButtons();
    }

    private void setValues(Map<Object, Object> h) {

        if (LCMC.DEBUG) {
            LCMC.logger.debug(h.toString());
        }
        if (h != null) {
            if (h.get("LicensePermission") != null) {
                this.haspermission = (h.get("LicensePermission") instanceof Boolean)
                        && ((Boolean) h.get("LicensePermission")).booleanValue();
            }

            if (h.get("ProductCode") != null) {
                this.pc.setIcon(ImageManager.getIcon(ImageManager.OK));
                this.pcok = true;
            } else {
                this.pcok = false;
            }
            if (h.get("Serial") != null) {
                this.sn.setSerialNumber((String) h.get("Serial"));
            }
            if (h.get("LicenseFileOK") != null) {
                this.ok = ((Boolean) h.get("LicenseFileOK")).booleanValue();
            } else {
                this.ok = false;
            }

            if (h.get("IPServer") != null) {
                this.ips.setIcon(ImageManager.getIcon(ImageManager.OK));
            }
            if (h.get("MACServer") != null) {
                this.macs.setIcon(ImageManager.getIcon(ImageManager.OK));
            }

            WindowLError.setVWLE(!this.ok);

            if (h.get("LicenseTODO") != null) {
                this.lchmText = (String) h.get("LicenseTODO");
                if (this.lchmText.equals("LCMS.NOTHING_OK")) {
                    this.frik = true;
                }
            } else {
                this.lchmText = ApplicationManager.getTranslation("LCMC.HELP_NOT_HAVE_HELP", this.bundle);
            }

            if (h.get("Shared") != null) {
                if (((Boolean) h.get("Shared")).booleanValue()) {
                    this.bShared.setText("Yes");
                    if (h.get("SharedConnection") != null) {
                        this.bSConnect.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
                    } else {
                        this.bSConnect.setIcon(ImageManager.getIcon(ImageManager.OK));
                    }
                }

                else {
                    this.bShared.setText("No");
                    this.bSConnect.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
                }
            }

            if (h.get("LicenseFileAsString") != null) {
                this.licenseFileAsString.setText(h.get("LicenseFileAsString").toString());
            }

            if (h.get("ListValidLicenseCodes") != null) {
                this.tbCodes.setValue(null);
                Map<Object, Object> hcode = new HashMap<>();
                hcode.put("Enabled", new Boolean(true));
                List<?> v = (List<?>) h.get("ListValidLicenseCodes");
                for (int i = 0, a = v.size(); i < a; i++) {
                    hcode.put("Functionality", v.get(i));
                    this.tbCodes.addRow(hcode);
                }
            }

            this.lp.setValues(h);
        } else {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.REQUEST_ERROR", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            this.connected = false;
            this.lchmText = ApplicationManager.getTranslation("LCMC.HELP_NOT_CONNECTED", this.bundle);
        }
    }

    private boolean frik = false;

    public void refresh() {
        this.clearValues();
        this.frik = false;
        if (!this.connected) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    ApplicationManager.getTranslation("LCMC.NOT_CONNECTED", this.bundle),
                    ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
            this.lchmText = ApplicationManager.getTranslation("LCMC.HELP_NOT_CONNECTED", this.bundle);
        } else {
            try {
            	Map<Object, Object> h = this.getParametersInterface();
                this.setValues(h);

            } catch (ModeErrorSecurityException ex) {
                LCMC.logger.trace(null, ex);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.MODE_ERROR", this.bundle),
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
                this.connected = false;

            } catch (GeneralSecurityException ex) {
                LCMC.logger.trace(null, ex);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.NO_PERMISSION", this.bundle),
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception ex) {
                LCMC.logger.error(null, ex);
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                        ApplicationManager.getTranslation("LCMC.REQUEST_MANAGING_ERROR", this.bundle),
                        ApplicationManager.getTranslation("LCMC.ERROR", this.bundle), JOptionPane.ERROR_MESSAGE);
                this.connected = false;
            }
        }
        this.activateButtons(this.frik);
    }

    public LCMC() {
        this.construct(null, false);
    }

    public LCMC(ResourceBundle bundle) {
        this(bundle, false);
    }

    public LCMC(ResourceBundle bundle, boolean init) {
        this.construct(bundle, init);
    }

    // private void construct(ResourceBundle bundle){
    // construct(bundle,false);
    // }
    private void construct(ResourceBundle bundle, boolean notInit) {
        this.bundle = bundle;
        this.lp = new LPanel(null, this.bundle);
        this.sn = new SerialNumber(this.bundle, false, true);
        this.bShared.setText("No");
        this.bShared.setEditable(false);

        this.licenseFileAsString.setEnabled(false);
        this.licenseFileAsString.setEditable(false);
        this.licenseFileAsString.setRows(5);
        this.licenseFileAsString.setLineWrap(true);
        this.licenseFileAsString.setWrapStyleWord(true);

        Map<Object, Object> ht = new HashMap<>();
        ht.put("cols", "Functionality;Enabled");
        ht.put("visiblecols", "Functionality;Enabled");
        ht.put("buttons", "no");
        ht.put("controls", "no");
        ht.put("keys", "no");
        ht.put("entity", "");
        ht.put("numrowscolumn", "no");

        try {
            this.tbCodes = new Table(ht);
            this.tbCodes.setEditable(false);
            this.tbCodes.setEnabled(false);
        } catch (Exception ex) {
            LCMC.logger.error(null, ex);
        }

        this.connect = new JButton(ApplicationManager.getTranslation("LCMC.BUTTON_CONNECT", bundle));
        this.refresh = new JButton(ApplicationManager.getTranslation("LCMC.BUTTON_REFRESH", bundle));
        this.update = new JButton(ApplicationManager.getTranslation("LCMC.BUTTON_UPDATE", bundle));
        this.help = new JButton(ApplicationManager.getTranslation("LCMC.BUTTON_HELP", bundle));
        this.close = new JButton(ApplicationManager.getTranslation("LCMC.BUTTON_CLOSE", bundle));

        this.dt.setBorder(new EmptyBorder(2, 2, 2, 2));

        this.connect.setActionCommand("connect");
        this.connect.setToolTipText(ApplicationManager.getTranslation("LCMC.BUTTON_TIP_CONNECT", bundle));
        this.connect.addActionListener(this);

        this.refresh.setActionCommand("refresh");
        this.refresh.setToolTipText(ApplicationManager.getTranslation("LCMC.BUTTON_TIP_REFRESH", bundle));
        this.refresh.addActionListener(this);

        this.update.setActionCommand("update");
        this.update.setToolTipText(ApplicationManager.getTranslation("LCMC.BUTTON_TIP_UPDATE", bundle));
        this.update.addActionListener(this);

        this.help.setActionCommand("help");
        this.help.setToolTipText(ApplicationManager.getTranslation("LCMC.BUTTON_TIP_HELP", bundle));
        this.help.addActionListener(this);
        this.help.setEnabled(true);

        this.close.setActionCommand("close");
        this.close.setToolTipText(ApplicationManager.getTranslation("LCMC.BUTTON_TIP_CLOSE", bundle));
        this.close.addActionListener(this);
        this.close.setEnabled(true);

        this.bShared.setEnabled(false);

        JPanel jpButtonsPanel = new JPanel();
        jpButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        jpButtonsPanel.add(this.connect);
        jpButtonsPanel.add(this.refresh);
        jpButtonsPanel.add(this.update);
        jpButtonsPanel.add(this.help);
        jpButtonsPanel.add(this.close);

        JPanel bs = new JPanel();
        FlowLayout f2 = new FlowLayout(FlowLayout.LEFT);
        f2.setHgap(7);
        f2.setVgap(7);
        bs.setLayout(f2);
        bs.setBorder(new TitledBorder(ApplicationManager.getTranslation("LPanel.SHARED", bundle)));
        bs.add(this.bShared);
        bs.add(this.bSConnect);

        TitledBorder ts = null;// BorderFactory.createTitledBorder(ApplicationManager.getTraduccion("LCMC.SERIAL",bundle));
        JPanel spanel = new JPanel(new GridBagLayout());

        this.sn.setBorder(new TitledBorder(ApplicationManager.getTranslation("LCMC.SERIAL", bundle)));
        spanel.add(this.sn, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        spanel.add(bs, new GridBagConstraints(1, 0, 1, 1, 0.3, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        // spanel.setBorder(ts);

        ts = BorderFactory.createTitledBorder(ApplicationManager.getTranslation("LCMC.LICENSE", bundle));
        JPanel lpanel = new JPanel(new GridBagLayout());
        lpanel.add(this.lp, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
        lpanel.setBorder(ts);

        JPanel pd = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pd.add(new JLabel(ApplicationManager.getTranslation("LCMC.DETECTED", bundle)));
        pd.add(this.dt);

        JPanel ppc = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ppc.add(new JLabel(ApplicationManager.getTranslation("LCMC.PRODUCT_CODE", bundle)));
        ppc.add(this.pc);

        JPanel ipsp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ipsp.add(new JLabel(ApplicationManager.getTranslation("LCMC.IP_SERVER", bundle)));
        ipsp.add(this.ips);

        JPanel macsp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        macsp.add(new JLabel(ApplicationManager.getTranslation("LCMC.MAC_SERVER", bundle)));
        macsp.add(this.macs);

        ts = BorderFactory.createTitledBorder(ApplicationManager.getTranslation("LCMC.SERVER_INFO", bundle));
        JPanel si = new JPanel(new GridBagLayout());
        si.add(pd, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        si.add(ipsp, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        si.add(ppc, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        si.add(macsp, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        si.setBorder(ts);

        ts = BorderFactory.createTitledBorder(ApplicationManager.getTranslation("LCMC.APPLICATION_INFO", bundle));
        JPanel ai = new JPanel(new GridBagLayout());
        ai.add(spanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        ai.add(lpanel, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));
        ai.setBorder(ts);

        JTabbedPane jtb = new JTabbedPane(SwingConstants.BOTTOM);
        jtb.addTab(ApplicationManager.getTranslation("LCMC.TAB_INFO", bundle), ai);
        jtb.addTab(ApplicationManager.getTranslation("LCMC.TAB_TEXT", bundle), this.licenseFileAsString);
        jtb.addTab(ApplicationManager.getTranslation("LCMC.TAB_COMPONENTS", bundle), this.tbCodes);

        this.setLayout(new GridBagLayout());

        this.add(si, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        this.add(jtb, new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        this.add(jpButtonsPanel, new GridBagConstraints(0, 2, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        if (!notInit) {
            this.activateButtons();
        }
    }

    public static void init(String IP, int port) {
        LCMC.init(IP, port, null, null, null);
    }

    public static void init(String IP, int port, String sN, String user, String pass) {

        LCMC.serverIP = IP;
        LCMC.serverPort = new Integer(port).toString();
        if (sN != null) {
            LCMC.serviceName = sN;
        }
        if (user != null) {
            LCMC.userAdministration = user;
        }
        if (pass != null) {
            LCMC.passwordAdministration = pass;
        }
    }

    public static void showLCMC(Frame f) {
        LCMC.showLCMC(f, null);
    }

    private static EJDialog jd = null;

    private static LCMC lcmc;

    public static void showLCMC(Frame f, ResourceBundle b) {
        LCMC.showLCMC(f, b, null);
    }

    public static void showLCMC(Frame f, ResourceBundle b, Map<Object, Object> h) {
        if (LCMC.jd == null) {
            LCMC.jd = new EJDialog(f, ApplicationManager.getTranslation("LCMC.LICENSE_CONTROLLER_MANAGER_CLIENT", b));
            LCMC.lcmc = new LCMC(b, h != null);
            LCMC.jd.getContentPane().setLayout(new BorderLayout());
            LCMC.jd.getContentPane().add(LCMC.lcmc, BorderLayout.CENTER);
        }
        if (LCMC.jd != null) {
            LCMC.jd.pack();
        }
        if (h == null) {
            if (LCMC.lcmc != null) {
                LCMC.lcmc.connect(true);
            }
        } else {
            LCMC.lcmc.setValues(h);
            LCMC.lcmc.onlyClose();
        }
        LCMC.jd.pack();
        LCMC.center(LCMC.jd);
        LCMC.jd.setVisible(true);
    }

    public static void center(Component c) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (d.width / 2) - (c.getWidth() / 2);
        int y = (d.height / 2) - (c.getHeight() / 2);
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > d.width) {
            x = 0;
        }
        if (y > d.height) {
            y = 0;
        }
        c.setLocation(x, y);
    }

    public static void setIcon(Frame f, String icon) {

        if (icon != null) {
            URL url = LCMC.class.getClassLoader().getResource(icon);
            if (url != null) {
                f.setIconImage(new ImageIcon(url).getImage());
            }
        }
    }

    public static void main(String args[]) {
        if (args.length != 5) {
            LCMC.logger.debug("Parameter Error...");
            LCMC.logger.debug("java LicenseVerifierManagerClient ServerIP port nombreServicio user password");
            return;
        }

        try {
            Integer.parseInt(args[1]);
        } catch (Exception e) {
            LCMC.logger.error("Parameter Error...");
            LCMC.logger.error("java LicenseVerifierManagerClient ServerIP port");
            LCMC.logger.error("Port is not a number", e);
        }

        LCMC.init(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4]);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LCMC.logger.error(null, e);
        }

        JFrame f = new JFrame("LCMC");
        f.setIconImage(ImageManager.getIcon(ImageManager.ICON_IMATIA).getImage());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LCMC lc = new LCMC(ResourceBundle.getBundle("com/ontimize/ols/resource/bundle"));
        lc.connect(true);
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(lc);
        f.pack();
        LCMC.center(f);
        f.show();

    }

}
