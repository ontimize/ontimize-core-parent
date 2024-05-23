package com.ontimize.report.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.jee.common.util.share.IShareRemoteReference;
import com.ontimize.report.DefaultReportDialog;

public class SharedDeleteTargetItemListener implements ActionListener {

    protected String deleteKey = "REPORT_DELETE_SHARE_KEY";

    protected String preferenceKey;

    protected EntityReferenceLocator locator;

    protected DefaultReportDialog defaultReportDialog;

    private static final Logger logger = LoggerFactory.getLogger(SharedDeleteTargetItemListener.class);

    public SharedDeleteTargetItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
        this.preferenceKey = preferenceKey;
        this.locator = ApplicationManager.getApplication().getReferenceLocator();
        this.defaultReportDialog = defaultReportDialog;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            this.defaultReportDialog.getConfMenu().setVisible(false);
            if (MessageDialog.showQuestionMessage(this.defaultReportDialog.getContainer(),
                    ApplicationManager.getTranslation(this.deleteKey))) {
                int shareId = Integer.parseInt(event.getActionCommand());
                int sessionID = this.locator.getSessionId();
                IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                    .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                            sessionID);
                EntityResult erToret = remoteReference.deleteTargetSharedItem(shareId, sessionID);
                if (erToret.getCode() == EntityResult.OPERATION_WRONG) {
                    SharedDeleteTargetItemListener.logger.error("{}",
                            ApplicationManager.getTranslation("shareRemote.not_delete_target"), erToret.getMessage());
                    MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(),
                            "shareRemote.not_delete_target");
                }
            }
        } catch (Exception ex) {
            SharedDeleteTargetItemListener.logger.error("{}",
                    ApplicationManager.getTranslation("shareRemote.not_delete_target"), ex.getMessage(), ex);
            MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(), "shareRemote.not_delete_target");
        } finally {
            this.defaultReportDialog.getConfMenu().setVisible(false);
        }

    }

}
