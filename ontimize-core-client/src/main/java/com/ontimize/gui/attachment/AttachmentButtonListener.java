package com.ontimize.gui.attachment;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ExtendedOperationThread;
import com.ontimize.gui.Form;
import com.ontimize.gui.MainApplication;
import com.ontimize.gui.actions.SendThread;
import com.ontimize.gui.button.AttachmentButtonSelection;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.FileManagementEntity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;
import com.ontimize.util.swing.ButtonSelection;

/**
 * Action listener used to upload a new file attached to a form record
 */
public class AttachmentButtonListener implements ActionListener {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(AttachmentButtonListener.class);

	/**
	 * Variable that stores the {@link JFileChooser}
	 */
	protected EJFile fileChooser = null;

	/**
	 * Variable that stores the associated form
	 */
	protected Form form;

	/**
	 * Constructor of the class
	 * @param form the {@link Form} associated to the listener
	 */
	public AttachmentButtonListener(Form form) {
		this.form = form;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		EntityReferenceLocator locator = this.form.getFormManager().getReferenceLocator();
		final Component eventSource = (Component) event.getSource();
		try {
			final Entity attachmentEntity = ((UtilReferenceLocator) locator)
					.getAttachmentEntity(locator.getSessionId());
			if (this.fileChooser == null) {
				this.fileChooser = new EJFile();
				this.fileChooser.setMultiSelectionEnabled(true);
				this.fileChooser.setPanel(new JDescriptionPanel(this.form.getFormManager().getResourceBundle()));
				this.fileChooser.setAccessory(this.fileChooser.getPanel());
			}
			this.fileChooser.getPanel().clearValues();
			// clear previous file selected
			this.fileChooser.setSelectedFile(new File(""));
			int option = this.fileChooser.showOpenDialog(eventSource);
			if (option != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File selectedFile = this.fileChooser.getSelectedFile();
			File[] files = this.fileChooser.getSelectedFiles();

			final Map<Object, Object> kv = new HashMap<>();
			if (selectedFile != null) {
				List<Object> v = this.form.getKeys();
				Collections.sort(v);
				for (Object element : v) {
					Object o = this.form.getDataFieldValue((String) element);
					if (o != null) {
						kv.put(element, o);
					}
				}

				if (this.fileChooser.getPanel().getPrivate()) {
					kv.put(Form.PRIVATE_ATTACHMENT, new Integer(1));
				} else {
					kv.put(Form.PRIVATE_ATTACHMENT, new Integer(0));
				}
				kv.put(Form.ATTACHMENT_ENTITY_NAME, this.form.getEntityName());
				for (File file : files) {
					ExtendedOperationThread eop = new SendThread(this.form.getResourceBundle(), file, kv,
							(FileManagementEntity) attachmentEntity,
							this.fileChooser.getPanel().getDescription(),
							this.form.getFormManager().getReferenceLocator()) {
						@Override
						protected void uploadFinished() {
							try {

								ButtonSelection button = (ButtonSelection) SwingUtilities
										.getAncestorOfClass(ButtonSelection.class, eventSource);
								kv.remove(Form.PRIVATE_ATTACHMENT);
								EntityResult res = attachmentEntity.query(kv, new Vector(),
										this.locator.getSessionId());
								if (button != null) {

									JList jList = button.getMenuList();
									AttachmentListPopup attachmentList = null;
									if (jList instanceof AttachmentListPopup) {
										attachmentList = (AttachmentListPopup) jList;
									}
									if (attachmentList == null) {
										attachmentList = new AttachmentListPopup(AttachmentButtonListener.this.form);
										button.setMenuList(attachmentList);
									}
									attachmentList.setAttachments(res);
									AttachmentButtonSelection attachmentButton = AttachmentButtonListener.this.form
											.getAttachmentButton();
									attachmentButton.setIcon(attachmentButton.getAttachmentIcon());

								}

							} catch (Exception e) {
								AttachmentButtonListener.logger.error("M_ERROR_RETRIEVING_ATTACHMENT", e.getMessage(),
										e);
							}
						}
					};

					ApplicationManager.ExtOpThreadsMonitor m = ApplicationManager
							.getExtOpThreadsMonitor((Component) event.getSource());
					m.addExtOpThread(eop);
				}
				ApplicationManager.ExtOpThreadsMonitor m = ApplicationManager
						.getExtOpThreadsMonitor((Component) event.getSource());
				if ((this.form.getFormManager() != null)
						&& (this.form.getFormManager().getApplication() instanceof MainApplication)) {
					((MainApplication) this.form.getFormManager().getApplication()).registerExtOpThreadsMonitor(m);
				}
				ApplicationManager.getExtOpThreadsMonitor((Component) event.getSource()).setVisible(true);

			}
		} catch (Exception e) {
			AttachmentButtonListener.logger.error(null, e);
		}
	}

}
