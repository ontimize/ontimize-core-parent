package com.ontimize.gui.attachment;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.attachment.AttachmentComponent.IActionId;
import com.ontimize.gui.button.AttachmentButtonSelection;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.FileManagementEntity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.ClientReferenceLocator;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.locator.UtilReferenceLocator;

public class MouseHandler extends MouseAdapter implements MouseMotionListener {

	private static final Logger logger = LoggerFactory.getLogger(MouseHandler.class);

	/**
	 * GUI text
	 */
	protected static final String M_ERROR_CHANGE_PRIVATE_ATTACHMENT = "form.only_user_that_attachment_file_can_check";

	protected Form form;

	protected AttachmentListPopup attachmentListPopup;

	protected String attachmentEntity;

	protected JFileChooser fileChooser;

	protected JDescriptionPanelExtended descriptionPopupEditor;

	protected JDialog dDescriptionDialog = new JDialog();

	protected EJDialog edDescription;

	protected boolean bNeedUpdate = false;

	public MouseHandler(Form form, AttachmentListPopup alp) {
		this.form = form;
		this.attachmentListPopup = alp;
		this.descriptionPopupEditor = new JDescriptionPanelExtended(ApplicationManager.getApplicationBundle());
		this.dDescriptionDialog.setModal(true);
		this.dDescriptionDialog.add(this.descriptionPopupEditor);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int sel = this.attachmentListPopup.locationToIndex(e.getPoint());

		int action = this.attachmentListPopup.getCurrentAction();

		switch (action) {
			case AttachmentListCellRenderer.CHECK:
				// First the private check
				this.configurePrivateAction(sel);
				this.attachmentListPopup.setPopupVisible(false);
				break;
			case AttachmentListCellRenderer.SAVE:

				// Save file button
				try {
					Map<?, ?> hRecord = this.attachmentListPopup.getRecord(sel);
					if (this.fileChooser == null) {
						this.fileChooser = new JFileChooser();
						this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					}
					File selectedFile = null;
					Object oOriginalName = hRecord.get(Form.ORIGINAL_FILE_NAME);
					if (oOriginalName != null) {
						this.fileChooser
						.setSelectedFile(new File(this.fileChooser.getCurrentDirectory(), (String) oOriginalName));
					}
					int option = this.fileChooser.showSaveDialog((Component) e.getSource());
					if (option == JFileChooser.CANCEL_OPTION) {
						return;
					}
					selectedFile = this.fileChooser.getSelectedFile();

					if (selectedFile.exists() && !this.form.question("attachment.file_exists_proceed_anyway")) {
						return;
					}
					Map<Object, Object> kv = new HashMap<>();
					kv.put(Form.ATTACHMENT_ID, hRecord.get(Form.ATTACHMENT_ID));

					com.ontimize.gui.actions.DownloadThread eop = new com.ontimize.gui.actions.DownloadThread(
							this.form.getResourceBundle(), selectedFile, kv,
							(FileManagementEntity) this.getAttachmentEntity(),
							this.form.getFormManager().getReferenceLocator());

					Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());
					if (w instanceof Dialog) {
						ApplicationManager.proccessOperation((Dialog) w, eop, 0);
					} else {
						ApplicationManager.proccessOperation((Frame) w, eop, 0);
					}
					if (eop.getResult() != null) {
						Object rs = eop.getResult();
						if (rs instanceof EntityResult) {
							this.form.message(((EntityResult) eop.getResult()).getMessage(), Form.ERROR_MESSAGE);
						} else {
							this.form.message(rs.toString(), Form.ERROR_MESSAGE);
						}
					} else {
						this.form.message("attachment.file_downloaded_successfully", Form.INFORMATION_MESSAGE);
					}

				} catch (Exception ex) {
					MouseHandler.logger.error(null, ex);
				}
				this.attachmentListPopup.setPopupVisible(false);

				break;
			case AttachmentListCellRenderer.DELETE:

				// Delete the file
				try {
					boolean bDeleteFile = this.form.question("form.file_will_be_deleted");
					if (!bDeleteFile) {
						return;
					}
					Map<Object, Object> kv = new HashMap<>();
					kv.put(Form.ATTACHMENT_ID, this.attachmentListPopup.getRecord(sel).get(Form.ATTACHMENT_ID));
					EntityResult res = this.getAttachmentEntity()
							.delete(kv, this.form.getFormManager().getReferenceLocator().getSessionId());
					if (res.getCode() == EntityResult.OPERATION_SUCCESSFUL) {
						int index = ((AttachmentListModel) this.attachmentListPopup.getModel()).getRecordIndex(kv);
						((AttachmentListModel) this.attachmentListPopup.getModel()).deleteRecord(index);
						if (((AttachmentListModel) this.attachmentListPopup.getModel()).isEmpty()) {
							AttachmentButtonSelection attachmentButton = this.form.getAttachmentButton();
							this.form.getAttachmentButton().setIcon(attachmentButton.getNoAttachmentIcon());
						}
						this.form.message("form.file_deleted_correctly", Form.INFORMATION_MESSAGE);
					} else {
						this.form.message("ERROR: " + res.getMessage(), Form.ERROR_MESSAGE);
					}
					this.attachmentListPopup.setPopupVisible(false);
				} catch (Exception ex) {
					this.form.message("ERROR: " + ex.getMessage(), Form.ERROR_MESSAGE);
					MouseHandler.logger.error(null, ex);
				}
				this.attachmentListPopup.setPopupVisible(false);

				break;
			case AttachmentListCellRenderer.EDIT_DESCRIPTION:

				// Updates the description the file
				this.configureDescriptionDialog(sel);
				this.dDescriptionDialog.setVisible(true);

				if (this.bNeedUpdate) {

					this.bNeedUpdate = false;
					Map<?, ?> hRecord = this.attachmentListPopup.getRecord(sel);
					EntityReferenceLocator locator = this.form.getFormManager().getReferenceLocator();

					Map<Object, Object> kv = new HashMap<>();
					Map<Object, Object> av = new HashMap<>();

					if (hRecord.containsKey(Form.DESCRIPTION_FILE)) {
						Object o = hRecord.get(Form.DESCRIPTION_FILE);
						if (o instanceof String) {
							if (!((String) o).equals(this.descriptionPopupEditor.getDescriptionField().getText())) {
								kv.put(Form.ATTACHMENT_ID, hRecord.get(Form.ATTACHMENT_ID));
								av.put(Form.DESCRIPTION_FILE,
										this.descriptionPopupEditor.getDescriptionField().getText());
								try {
									Entity attachmentEntity = this.getAttachmentEntity();
									attachmentEntity.update(av, kv, locator.getSessionId());
									int index = ((AttachmentListModel) this.attachmentListPopup.getModel())
											.getRecordIndex(kv);
									((AttachmentListModel) this.attachmentListPopup.getModel()).updateRecord(index, av);
								} catch (Exception ex) {
									MouseHandler.logger.error(null, ex);
								}
							}
						}
					}
				}
				// attachmentListPopup.setPopupVisible(false);

				break;
			case AttachmentListCellRenderer.OPEN:

				// Open the file
				try {
					Map<?, ?> hRecord = this.attachmentListPopup.getRecord(sel);
					File fSelec = null;
					Object suggestedName = hRecord.get(Form.ORIGINAL_FILE_NAME);
					if (suggestedName != null) {
						fSelec = new File(System.getProperty("java.io.tmpdir", "."), (String) suggestedName);
					} else {
						fSelec = File.createTempFile("download", null);
					}

					fSelec.deleteOnExit();
					Map<Object, Object> kv = new HashMap<>();
					kv.put(Form.ATTACHMENT_ID, this.attachmentListPopup.getRecord(sel).get(Form.ATTACHMENT_ID));
					com.ontimize.gui.actions.DownloadThread eop = new com.ontimize.gui.actions.DownloadThread(
							this.form.getResourceBundle(), fSelec, kv,
							(FileManagementEntity) this.getAttachmentEntity(),
							this.form.getFormManager().getReferenceLocator());

					Window w = SwingUtilities.getWindowAncestor(this.form);
					if (w instanceof Dialog) {
						ApplicationManager.proccessOperation((Dialog) w, eop, 0);
					} else if (w instanceof Frame) {
						ApplicationManager.proccessOperation((Frame) w, eop, 0);
					} else {
						ApplicationManager.proccessOperation(this.form.getParentFrame(), eop, 0);
					}
					if (eop.getResult() != null) {
						this.form.message(eop.getResult().toString(), Form.ERROR_MESSAGE);
					} else {
						com.ontimize.windows.office.WindowsUtils.openFile_Script(fSelec);
					}

				} catch (Exception ex) {
					MouseHandler.logger.error(null, ex);
				} finally {
					this.form.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					this.attachmentListPopup.setPopupVisible(false);
				}

				break;
		}
	}

	/**
	 * Configures the dialog to show description and private fields.
	 * @param sel Selected attachment
	 */
	public void configureDescriptionDialog(int sel) {
		this.dDescriptionDialog.setSize(new Dimension(200, 200));
		this.dDescriptionDialog.setTitle((String) this.attachmentListPopup.getRecord(sel).get(Form.ORIGINAL_FILE_NAME));
		this.descriptionPopupEditor.getDescriptionField()
		.setText((String) this.attachmentListPopup.getRecord(sel).get(Form.DESCRIPTION_FILE));
		this.descriptionPopupEditor.setSelectedRecord(sel);
		this.descriptionPopupEditor.gePrivateField()
		.setSelected(Integer
				.parseInt(this.attachmentListPopup.getRecord(sel).get(Form.PRIVATE_ATTACHMENT).toString()) == 1 ? true
						: false);
		this.descriptionPopupEditor.setToolTips();
		this.descriptionPopupEditor.setResourceBundle(ApplicationManager.getApplicationBundle());
		this.dDescriptionDialog.setLocation(this.form.attachmentButton.getLocationOnScreen());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.attachmentListPopup.setCurrentSelectionIndex(this.attachmentListPopup.locationToIndex(e.getPoint()));
		int x = e.getX();

		Object renderer = this.attachmentListPopup.getCellRenderer();
		if (renderer instanceof AttachmentListCellRenderer) {
			AttachmentComponent component = ((AttachmentListCellRenderer) this.attachmentListPopup
					.getCellRenderer()).component;
			Component[] comps = component.getComponents();
			this.attachmentListPopup.setCurrentAction(-1);
			for (Component c_ : comps) {
				Rectangle b = c_.getBounds();
				// Just check 'x' due to 'y' value of component c_ bounds not
				// corresponds with 'y' value of the mouse event.
				boolean bool = (x >= b.x) && (x < (b.x + b.width));
				if (bool) {
					if (c_ instanceof IActionId) {
						this.attachmentListPopup.setCurrentAction(((IActionId) c_).getActionId());
						break;
					}
				}
			}

		}
		this.attachmentListPopup.repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.attachmentListPopup.setCurrentSelectionIndex(-1);
		this.attachmentListPopup.setCurrentAction(-1);
		this.attachmentListPopup.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	protected Entity getAttachmentEntity() {
		EntityReferenceLocator referenceLocator = this.form.getFormManager().getReferenceLocator();
		if (referenceLocator instanceof UtilReferenceLocator) {
			try {
				return ((UtilReferenceLocator) referenceLocator).getAttachmentEntity(referenceLocator.getSessionId());
			} catch (Exception e) {
				MouseHandler.logger.error(null, e);
			}
		}
		return null;
	}

	public void configurePrivateAction(int selectedRecord) {
		boolean changeAttachmentOwner = false;
		Map<?, ?> hRecord = this.attachmentListPopup.getRecord(selectedRecord);
		EntityReferenceLocator locator = this.form.getFormManager().getReferenceLocator();
		if (hRecord.containsKey(Form.USER)) {
			Object o = hRecord.get(Form.USER);
			String user = ((ClientReferenceLocator) locator).getUser();
			// attachment has not user column, cannot make private
			if (o == null) {
				this.attachmentListPopup.setPopupVisible(false);
				this.form.message(MouseHandler.M_ERROR_CHANGE_PRIVATE_ATTACHMENT, Form.QUESTION_MESSAGE);
				return;
			}
			if ((o != null) && !user.equals(o)) {
				int value = this.form.message("mousehandler.changeattachmentowner", Form.QUESTION_MESSAGE);
				if (value == Form.YES) {
					changeAttachmentOwner = true;
				}
			}

		}
		Map<Object, Object> kv = new HashMap<>();
		Map<Object, Object> av = new HashMap<>();

		if (hRecord.containsKey(Form.PRIVATE_ATTACHMENT)) {
			Object o = hRecord.get(Form.PRIVATE_ATTACHMENT);
			if (o instanceof Number) {
				boolean bPrivate = ((Number) o).intValue() > 0;

				if (bPrivate) {
					av.put(Form.PRIVATE_ATTACHMENT, new Integer(0));
				} else {
					av.put(Form.PRIVATE_ATTACHMENT, new Integer(1));
					if (changeAttachmentOwner) {
						av.put(Form.USER, ((ClientReferenceLocator) locator).getUser());
					}
				}
			}
		} else {
			av.put(Form.PRIVATE_ATTACHMENT, new Integer(1));
		}
		kv.put(Form.ATTACHMENT_ID, hRecord.get(Form.ATTACHMENT_ID));

		try {
			Entity attachmentEntity = this.getAttachmentEntity();
			attachmentEntity.update(av, kv, locator.getSessionId());
			int index = ((AttachmentListModel) this.attachmentListPopup.getModel()).getRecordIndex(kv);
			((AttachmentListModel) this.attachmentListPopup.getModel()).updateRecord(index, av);
		} catch (Exception ex) {
			MouseHandler.logger.error(null, ex);
		}
	}

	protected class JDescriptionPanelExtended extends JDescriptionPanel {

		public static final String SAVE_DESCRIPTION_TOOLTIP_KEY = "mousehandler.jdescriptionpanelextended.savedescription";

		public static final String CANCEL_DESCRIPTION_TOOLTIP_KEY = "mousehandler.jdescriptionpanelextended.canceldescription";

		protected JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));

		protected int selectedRecord = -1;

		protected JButton buSaveDescription = new JButton(ImageManager.getIcon(ImageManager.SAVE));

		protected JButton buCancelSaveDescription = new JButton(ImageManager.getIcon(ImageManager.DELETE));

		public JDescriptionPanelExtended(ResourceBundle bundle) {
			super(bundle);
			this.lDescription.setText("");
			this.panelButtons.add(this.buSaveDescription);
			this.panelButtons.add(this.buCancelSaveDescription);
			this.add(this.panelButtons, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			this.configureEditDescriptionActions();
			this.setToolTips();
		}

		public void setToolTips() {
			this.buSaveDescription
			.setToolTipText(
					ApplicationManager.getTranslation(JDescriptionPanelExtended.SAVE_DESCRIPTION_TOOLTIP_KEY,
							ApplicationManager.getApplicationBundle()));
			this.buCancelSaveDescription
			.setToolTipText(
					ApplicationManager.getTranslation(JDescriptionPanelExtended.CANCEL_DESCRIPTION_TOOLTIP_KEY,
							ApplicationManager.getApplicationBundle()));
		}

		public JTextArea getDescriptionField() {
			return this.tDescription;
		}

		public void configureEditDescriptionActions() {
			this.buCancelSaveDescription.addActionListener(event -> MouseHandler.this.dDescriptionDialog.setVisible(false));

			this.buSaveDescription.addActionListener(event -> {
				MouseHandler.this.dDescriptionDialog.setVisible(false);
				MouseHandler.this.bNeedUpdate = true;
			});

			this.tPrivate.addActionListener(event -> MouseHandler.this.configurePrivateAction(JDescriptionPanelExtended.this.selectedRecord));
		}

		public int getSelectedRecord() {
			return this.selectedRecord;
		}

		public void setSelectedRecord(int iselectedRecord) {
			this.selectedRecord = iselectedRecord;
		}

		@Override
		public void setResourceBundle(ResourceBundle resources) {
			super.setResourceBundle(resources);
			this.lDescription.setText("");

		}

	}

}
