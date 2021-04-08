package com.ontimize.report.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.report.DefaultReportDialog;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.swing.Toast;

public class SharedMessageItemListener implements ActionListener {

    protected String preferenceKey;

    protected DefaultReportDialog defaultReportDialog;

    protected EntityReferenceLocator locator;

    private static final Logger logger = LoggerFactory.getLogger(SharedMessageItemListener.class);

    public SharedMessageItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
        this.preferenceKey = preferenceKey;
        this.defaultReportDialog = defaultReportDialog;
        this.locator = ApplicationManager.getApplication().getReferenceLocator();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object o = e.getSource();
            if (o instanceof AbstractButton) {
                int shareId = Integer.parseInt(e.getActionCommand());
                int sessionID = this.locator.getSessionId();

                IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                    .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                            sessionID);
                String message = remoteReference.getSharedElementMessage(shareId, sessionID);
                if (!message.isEmpty()) {

                    Toast.showMessage(this.defaultReportDialog.getContainer(), message, null, 1500);

                } else {
                    JOptionPane.showMessageDialog(this.defaultReportDialog.getContainer(),
                            ApplicationManager.getTranslation("shareRemote.message_empty"),
                            ApplicationManager.getTranslation("shareRemote.message_dialog"),
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e1) {
            SharedMessageItemListener.logger.error("ERROR -> {}", e1.getMessage(), e1);
            MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(), "shareRemote.not_retrive_message");
        }
    }

}
