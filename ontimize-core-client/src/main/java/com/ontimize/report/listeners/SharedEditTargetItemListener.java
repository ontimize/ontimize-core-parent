package com.ontimize.report.listeners;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.field.ListDataField;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.jee.common.util.share.IShareRemoteReference;
import com.ontimize.report.DefaultReportDialog;
import com.ontimize.util.share.FormAddUserSharedReference;

public class SharedEditTargetItemListener implements ActionListener {

	protected String					preferenceKey;

	protected EntityReferenceLocator	locator;

	protected DefaultReportDialog		defaultReportDialog;

	private static final Logger			logger	= LoggerFactory.getLogger(SharedEditTargetItemListener.class);

	public SharedEditTargetItemListener(String preferenceKey, DefaultReportDialog defaultReportDialog) {
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
				Point p = ((Component) e.getSource()).getLocationOnScreen();

				IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator).getRemoteReference(IShareRemoteReference.REMOTE_NAME,
						sessionID);
				ListDataField listDataField = this.defaultReportDialog.createAndConfigureTargetUser();
				List<String> oldTargetList = remoteReference.getTargetSharedItemsList(shareId, sessionID);

				listDataField.setValue(new Vector<String>(oldTargetList));
				FormAddUserSharedReference f = new FormAddUserSharedReference(
						SwingUtilities.getWindowAncestor(SwingUtilities.getAncestorOfClass(Window.class, (Component) e.getSource())), true, this.locator, listDataField);
				f.setLocation(p);
				f.setVisible(true);

				if (f.getUpdateStatus()) {
					List<String> targetList = new ArrayList<String>();
					if (listDataField.getValue() != null) {
						for (Object oActual : (Vector) listDataField.getValue()) {
							targetList.add(oActual.toString());
						}
					}
					remoteReference.editTargetSharedElement(shareId, targetList, sessionID);
				}

			}
		} catch (Exception e1) {
			SharedEditTargetItemListener.logger.error("{}", ApplicationManager.getTranslation("shareRemote.error_adding_target_user"), e1.getMessage(), e1);
			MessageDialog.showErrorMessage(this.defaultReportDialog.getContainer(), "shareRemote.error_adding_target_user");
		}

	}

}
