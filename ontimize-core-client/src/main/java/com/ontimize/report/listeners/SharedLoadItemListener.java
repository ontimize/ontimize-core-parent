package com.ontimize.report.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.report.DefaultReportDialog;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.share.SharedElement;

public class SharedLoadItemListener implements ActionListener {

    protected String preferenceKey;

    protected DefaultReportDialog defaultReportDialog;

    protected EntityReferenceLocator locator;

    private static final Logger logger = LoggerFactory.getLogger(SharedLoadItemListener.class);

    public SharedLoadItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
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
                SharedElement sharedItem = remoteReference.getSharedItem(shareId, sessionID);
                this.defaultReportDialog.loadSharedConfiguration(sharedItem);
            }
        } catch (Exception e1) {
            SharedLoadItemListener.logger.error("{}",
                    ApplicationManager.getTranslation("shareRemote.error_loading_shared_content"), e1.getMessage(), e1);
            MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(),
                    "shareRemote.error_loading_shared_content");
        } finally {
            this.defaultReportDialog.getConfMenu().setVisible(false);
        }
    }

}
