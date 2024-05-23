package com.ontimize.report.listeners;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.jee.common.util.share.IShareRemoteReference;
import com.ontimize.jee.common.util.share.SharedElement;
import com.ontimize.report.DefaultReportDialog;
import com.ontimize.util.share.FormUpdateSharedReference;

public class SharedEditItemListener implements ActionListener {

	protected String					preferenceKey;

	protected DefaultReportDialog		defaultReportDialog;

	protected EntityReferenceLocator	locator;

	private static final Logger			logger	= LoggerFactory.getLogger(SharedEditItemListener.class);

	public SharedEditItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
		this.preferenceKey = preferenceKey;
		this.defaultReportDialog = defaultReportDialog;
		this.locator = ApplicationManager.getApplication().getReferenceLocator();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			int shareId = Integer.parseInt(event.getActionCommand());
			int sessionID = this.locator.getSessionId();
			Point p = ((Component) event.getSource()).getLocationOnScreen();
			String user = ((ClientReferenceLocator) this.locator).getUser();

			IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator).getRemoteReference(IShareRemoteReference.REMOTE_NAME, sessionID);
			SharedElement sharedItem = remoteReference.getSharedItem(shareId, sessionID);

			String filterContent = this.defaultReportDialog.getCurrentConfiguration();

			FormUpdateSharedReference f = new FormUpdateSharedReference(
					SwingUtilities.getWindowAncestor(SwingUtilities.getAncestorOfClass(Window.class, (Component) event.getSource())), true, this.locator, p, sharedItem);
			if (f.getUpdateStatus()) {
				String nameUpdate = f.getName();
				String contentShareUpdate = filterContent;
				String messageUpdate = (String) f.getMessage();

				remoteReference.updateSharedItem(shareId, contentShareUpdate, messageUpdate, nameUpdate, sessionID);
			}

		} catch (Exception ex) {
			SharedEditItemListener.logger.error("{}", ApplicationManager.getTranslation("shareRemote.not_retrive_message"), ex.getMessage(), ex);
			MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(), "shareRemote.not_retrive_message");
		}

	}

}
