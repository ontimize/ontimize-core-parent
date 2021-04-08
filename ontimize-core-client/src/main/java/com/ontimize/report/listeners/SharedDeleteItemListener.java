package com.ontimize.report.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.locator.ClientReferenceLocator;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.report.DefaultReportDialog;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.share.SharedElement;

public class SharedDeleteItemListener implements ActionListener {

    protected String deleteKey = "REPORT_DELETE_SHARE_KEY";

    protected String preferenceKey;

    protected DefaultReportDialog defaultReportDialog;

    protected EntityReferenceLocator locator;

    private static final Logger logger = LoggerFactory.getLogger(SharedDeleteItemListener.class);

    public SharedDeleteItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
        this.preferenceKey = preferenceKey;
        this.defaultReportDialog = defaultReportDialog;
        this.locator = ApplicationManager.getApplication().getReferenceLocator();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            this.defaultReportDialog.getConfMenu().setVisible(false);
            if (MessageDialog.showQuestionMessage(this.defaultReportDialog.getContainer(),
                    ApplicationManager.getTranslation(this.deleteKey))) {
                int shareId = Integer.parseInt(e.getActionCommand());
                int sessionID = this.locator.getSessionId();
                String user = ((ClientReferenceLocator) this.locator).getUser();
                IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                    .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                            sessionID);
                SharedElement sharedItem = remoteReference.getSharedItem(shareId, sessionID);

                this.defaultReportDialog.saveReportConfiguration(sharedItem);

                remoteReference.deleteSharedItem(shareId, sessionID);
            }
        } catch (Exception ex) {
            SharedDeleteItemListener.logger.error("{}",
                    ApplicationManager.getTranslation("shareRemote.not_retrive_message"), ex.getMessage(), ex);
            MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(),
                    "shareRemote.error_deleting_shared_element");
        }
    }

}
