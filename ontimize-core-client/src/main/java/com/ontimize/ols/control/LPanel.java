package com.ontimize.ols.control;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.images.ImageManager;

public class LPanel extends JPanel {

    private final JTextField path = new JTextField();

    private final JTextField message = new JTextField();

    private final JTextField specific = new JTextField();

    private final JTextField daysToExpire = new JTextField(5);

    private final JTextField userMax = new JTextField(3);

    private final JTextField creationDate = new JTextField(8);

    private final JLabel bExist = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private final JLabel bOk = new JLabel(ImageManager.getIcon(ImageManager.CANCEL));

    private ResourceBundle bundle = null;

    public LPanel(Map<Object, Object> h, ResourceBundle bundle) {
        this.constructor(h, bundle);
    }

    public LPanel(Map<Object, Object> h) {
        this.constructor(h, null);
    }

    private void constructor(Map<Object, Object> h, ResourceBundle bundle) {
        this.bundle = bundle;
        this.init();
        this.setValues(h);
    }

    private void init() {
        this.setLayout(new GridBagLayout());

        this.path.setEditable(false);
        this.message.setEditable(false);
        this.specific.setEditable(false);

        this.daysToExpire.setEditable(false);
        this.userMax.setEditable(false);
        this.creationDate.setEditable(false);

        this.bOk.setBorder(new EmptyBorder(2, 10, 2, 2));
        this.bExist.setBorder(new EmptyBorder(2, 10, 2, 2));

        this.path.setPreferredSize(new Dimension(100, 20));
        this.message.setPreferredSize(new Dimension(100, 20));
        this.specific.setPreferredSize(new Dimension(100, 20));

        JPanel bi = new JPanel();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        fl.setHgap(0);
        bi.setLayout(fl);
        bi.add(new JLabel(ApplicationManager.getTranslation("LPanel.LICENSE_FILE_EXIST", this.bundle)));
        bi.add(this.bExist);

        JPanel bd = new JPanel();
        bd.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bd.add(new JLabel(ApplicationManager.getTranslation("LPanel.OK", this.bundle)));
        bd.add(this.bOk);

        JPanel datep = new JPanel();
        fl = new FlowLayout(FlowLayout.CENTER);
        fl.setHgap(10);
        datep.setLayout(fl);
        datep.add(new JLabel(ApplicationManager.getTranslation("LPanel.CREATION_DATE", this.bundle)));
        datep.add(this.creationDate);

        JPanel bt = new JPanel();
        bt.setLayout(new GridBagLayout());
        bt.add(bi, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        /*
         * bt.add(bs,new GridBagConstraints(1,0,1,1,1,1, GridBagConstraints.WEST,
         * GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
         */

        bt.add(bd, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 0), 0, 0));

        JPanel exp = new JPanel();
        exp.setLayout(new GridBagLayout());
        exp.add(new JLabel(ApplicationManager.getTranslation("LPanel.DAYS_TO_EXPIRE", this.bundle)),
                new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        exp.add(this.daysToExpire, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        exp.add(datep, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));

        exp.add(new JLabel(ApplicationManager.getTranslation("LPanel.USER_MAX", this.bundle)),
                new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        exp.add(this.userMax, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));

        this.add(bt, new GridBagConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

        this.add(new JLabel(ApplicationManager.getTranslation("LPanel.PATH", this.bundle)),
                new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 4, 2, 2), 0, 0));

        this.add(this.path, new GridBagConstraints(1, 1, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(4, 6, 2, 3), 0, 0));

        this.add(new JLabel(ApplicationManager.getTranslation("LPanel.MESSAGE", this.bundle)),
                new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(4, 4, 2, 2), 0, 0));

        this.add(this.message, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(2, 6, 2, 3), 1, 0));

        this.add(new JLabel(ApplicationManager.getTranslation("LPanel.SPECIFIC_DATA", this.bundle)),
                new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(4, 4, 2, 2), 0, 0));

        this.add(this.specific, new GridBagConstraints(1, 3, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(2, 6, 2, 3), 1, 0));

        this.add(exp, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                new Insets(2, 2, 2, 2), 0, 0));

    }

    public void setValues(Map<Object, Object> h) {
        if (h == null) {
            return;
        }
        this.clear();

        if (h.get("LicenseFilePath") != null) {
            this.path.setText((String) h.get("LicenseFilePath"));
        }
        if ((h.get("LicenseFileExist") != null) && ((Boolean) h.get("LicenseFileExist")).booleanValue()) {
            this.bExist.setIcon(ImageManager.getIcon(ImageManager.OK));
        } else {
            this.bExist.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
        }
        if ((h.get("LicenseFileOK") != null) && ((Boolean) h.get("LicenseFileOK")).booleanValue()) {
            this.bOk.setIcon(ImageManager.getIcon(ImageManager.OK));
        } else {
            this.bOk.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
        }
        if (h.get("LicenseMessage") != null) {
            this.message.setText((String) h.get("LicenseMessage"));
        }
        if (h.get("DaysToExpire") != null) {
            this.daysToExpire.setText(((Long) h.get("DaysToExpire")).toString());
        }
        if (h.get("UserMax") != null) {
            this.userMax.setText(((Integer) h.get("UserMax")).toString());
        }
        if (h.get("CreationDate") != null) {
            this.creationDate.setText((String) h.get("CreationDate"));
        }
        if (h.get("Specific") != null) {
            if (h.get("Specific") instanceof String) {
                this.specific.setText((String) h.get("Specific"));
            } else {
                if (h.get("Specific") instanceof String[]) {
                    String s[] = (String[]) h.get("Specific");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0, a = s.length; i < a; i++) {
                        sb.append(s[i]);
                        if (i != (a - 1)) {
                            sb.append("<<-->>");
                        }
                    }
                    this.specific.setText(sb.toString());
                }
            }

        }
    }

    public void clear() {
        this.path.setText("");
        this.creationDate.setText("");
        this.message.setText("");
        this.daysToExpire.setText("");
        this.userMax.setText("");
        this.specific.setText("");
        this.bExist.setIcon(ImageManager.getIcon(ImageManager.CANCEL));
        this.bOk.setIcon(ImageManager.getIcon(ImageManager.OK));
    }

}
