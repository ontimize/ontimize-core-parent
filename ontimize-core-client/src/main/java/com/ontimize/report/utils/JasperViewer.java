/*
 * Class JasperViewer.
 */
package com.ontimize.report.utils;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

public class JasperViewer extends JFrame {

    protected String title;

    protected javax.swing.JPanel pnlMain;

    /** Creates new form JasperViewer */
    /**
     * @param jasperPrint report to display
     * @param frameTitle Title to be displayed
     * @throws JRException
     */
    protected JasperViewer(JasperPrint jasperPrint, String title) throws JRException {
        this.title = title;
        this.initComponents();
        JRViewer viewer = new JRViewer(jasperPrint);
        this.pnlMain.add(viewer, BorderLayout.CENTER);
    }

    protected void initComponents() {
        this.pnlMain = new javax.swing.JPanel();

        this.setTitle(this.title);
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                JasperViewer.this.exitForm();
            }
        });
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    JasperViewer.this.exitForm();
                }
            }

            @Override
            public void keyTyped(KeyEvent event) {
                super.keyTyped(event);
            }
        });

        this.pnlMain.setLayout(new java.awt.BorderLayout());

        this.getContentPane().add(this.pnlMain, java.awt.BorderLayout.CENTER);

        this.pack();
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(new java.awt.Dimension(750, 550));
        this.setLocation((screenSize.width - 750) / 2, (screenSize.height - 550) / 2);
    }

    protected void exitForm() {
        this.hide();
        this.dispose();
    }

    public static void viewReport(JasperPrint jasperPrint) throws JRException {
        JasperViewer jasperViewer = new JasperViewer(jasperPrint, "JasperReport");
        jasperViewer.show();
    }

    public static void viewReport(JasperPrint jasperPrint, String frameTitle) throws JRException {
        JasperViewer jasperViewer = new JasperViewer(jasperPrint, frameTitle);
        jasperViewer.show();
    }

    public JPanel getReportPanel() {
        return this.pnlMain;
    }

    public void setReportPanel(JPanel pnlMain) {
        this.pnlMain = pnlMain;
    }

}
