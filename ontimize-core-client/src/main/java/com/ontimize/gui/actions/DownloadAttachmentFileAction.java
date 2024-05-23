package com.ontimize.gui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.ExtendedOperationThread;
import com.ontimize.gui.Form;
import com.ontimize.gui.MainApplication;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.FileManagementEntity;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.util.remote.BytesBlock;

/**
 * <p> Copyright: Copyright (c) 2003 </p>
 *
 * @version 1.0
 */

public class DownloadAttachmentFileAction extends AbstractButtonAction {

	private static final Logger	logger			= LoggerFactory.getLogger(DownloadAttachmentFileAction.class);

	protected int				blockSize		= 64 * 1024;

	protected String			entityName		= null;

	protected JFileChooser		fileChooser		= null;

	protected boolean			openFile		= false;

	protected String			fileFieldName	= null;

	protected boolean			synchronize		= true;

	protected String			uriSound		= null;

	protected boolean			queryOpen		= true;

	protected boolean			tempFile		= false;

	protected static File		lastDirectory	= null;

	protected class DownloadThread extends ExtendedOperationThread {

		protected Form						currentForm		= null;

		protected File						selectedForm	= null;

		protected FileManagementEntity		entF			= null;

		protected Map<?, ?>					keysValues		= null;

		protected EntityReferenceLocator	locator			= null;

		public DownloadThread(Form f, File selectedFile, Map<?, ?> kv, FileManagementEntity entF, EntityReferenceLocator locator) {
			super(ApplicationManager.getTranslation("attachment.download_attached_file", f.getResourceBundle()) + " " + selectedFile.getName());
			this.currentForm = f;
			this.selectedForm = selectedFile;
			this.keysValues = kv;
			this.entF = entF;
			this.locator = locator;
		}

		protected void downloadFinished() {}

		@Override
		public void run() {
			this.setPriority(Thread.MIN_PRIORITY);
			this.hasStarted = true;
			BufferedOutputStream bOut = null;

			try {
				this.status = ApplicationManager.getTranslation("attachment.initiating_transfer", this.currentForm.getResourceBundle());
				String rId = this.entF.prepareToTransfer(this.keysValues, this.locator.getSessionId());
				long totalSize = this.entF.getSize(rId);
				FileOutputStream fOut = new FileOutputStream(this.selectedForm);
				this.status = ApplicationManager.getTranslation("attachment.downloading_file", this.currentForm.getResourceBundle());
				this.progressDivisions = (int) totalSize;
				bOut = new BufferedOutputStream(fOut);
				BytesBlock by = null;
				int totalRead = 0;
				long tIni = System.currentTimeMillis();
				long spendTime = 0;
				while ((by = this.entF.getBytes(rId, totalRead, DownloadAttachmentFileAction.this.blockSize, this.locator.getSessionId())) != null) {
					Thread.yield();
					bOut.write(by.getBytes());
					totalRead = totalRead + by.getBytes().length;
					if (ApplicationManager.DEBUG) {
						DownloadAttachmentFileAction.logger.debug(this.getClass().toString() + " -> Downloaded " + totalRead + " bytes");
					}
					this.currentPosition = totalRead;
					if (this.isCancelled()) {
						this.hasFinished = true;
						this.status = ApplicationManager.getTranslation("cancelled", this.currentForm.getResourceBundle());
						bOut.close();
						this.selectedForm.delete();
						return;
					}
					try {
						Thread.sleep(20);
					} catch (Exception ex) {
						DownloadAttachmentFileAction.logger.trace(null, ex);
					}
					spendTime = System.currentTimeMillis() - tIni;
					this.estimatedTimeLeft = (int) (((totalSize - totalRead) * spendTime) / (float) totalRead);
				}

				this.currentPosition = this.progressDivisions;
				this.status = ApplicationManager.getTranslation("finished", this.currentForm.getResourceBundle());
				this.downloadFinished();
				if (DownloadAttachmentFileAction.this.uriSound != null) {
					ApplicationManager.playSound(DownloadAttachmentFileAction.this.uriSound);
				}
			} catch (Exception e) {
				DownloadAttachmentFileAction.logger.error(null, e);
				this.res = e.getMessage();
				this.status = ApplicationManager.getTranslation("error", this.currentForm.getResourceBundle()) + " : " + e.getMessage();
			} finally {
				this.hasFinished = true;
				try {
					if (bOut != null) {
						bOut.close();
					}
				} catch (Exception e) {
					DownloadAttachmentFileAction.logger.trace(null, e);
				}
			}
		}

	};

	public DownloadAttachmentFileAction(String entity, boolean openFile) {
		this(entity, openFile, null);
	}

	public DownloadAttachmentFileAction(String entity, boolean openFile, String sugestFieldName) {
		this(entity, openFile, sugestFieldName, true);
	}

	public DownloadAttachmentFileAction(String entity, boolean openFile, String sugestFieldName, boolean wait) {
		this(entity, openFile, sugestFieldName, wait, null);
	}

	public DownloadAttachmentFileAction(String entity, boolean openFile, String sugestFieldName, boolean wait, String uriSound) {
		this(entity, openFile, sugestFieldName, wait, uriSound, true);
	}

	public DownloadAttachmentFileAction(String entity, boolean openFile, String sugestFieldName, boolean wait, String uriSound, boolean askToOpen) {
		this(entity, openFile, sugestFieldName, wait, uriSound, askToOpen, false);
	}

	public DownloadAttachmentFileAction(String entity, boolean openFile, String sugestFieldName, boolean wait, String uriSound, boolean askToOpen, boolean temporalFile) {
		this.entityName = entity;
		this.openFile = openFile;
		this.fileFieldName = sugestFieldName;
		this.synchronize = wait;
		this.uriSound = uriSound;
		this.queryOpen = askToOpen;
		this.tempFile = temporalFile;
	}

	protected Map<Object, Object> getAttachmentValuesKeys(Form f) throws Exception {
		final Map<Object, Object> kv = new HashMap<>();
		List<?> vKeys = f.getKeys();
		if (vKeys.isEmpty()) {
			throw new Exception("The 'keys' parameter ir required in parent form");
		}
		for (int i = 0; i < vKeys.size(); i++) {
			Object oKeyValue = f.getDataFieldValueFromFormCache(vKeys.get(i).toString());
			if (oKeyValue == null) {
				throw new Exception("Value for key " + vKeys.get(i) + " not found in parent form");
			}
			kv.put(vKeys.get(i), oKeyValue);
		}
		return kv;
	}

	protected String getProposedFileName(ActionEvent e) {
		final Form f = this.getForm(e);
		if ((this.fileFieldName != null) && (f.getDataFieldValue(this.fileFieldName) != null)) {
			return (String) f.getDataFieldValue(this.fileFieldName);
		} else {
			return null;
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		try {
			if (this.fileChooser == null) {
				this.fileChooser = new com.ontimize.util.filechooser.CustomFileChooser();
				this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
			if (DownloadAttachmentFileAction.lastDirectory != null) {
				if (DownloadAttachmentFileAction.lastDirectory.isDirectory()) {
					this.fileChooser.setCurrentDirectory(DownloadAttachmentFileAction.lastDirectory);
				} else {
					this.fileChooser.setCurrentDirectory(DownloadAttachmentFileAction.lastDirectory.getParentFile());
				}
			}
			final Form f = this.getForm(event);

			final EntityReferenceLocator loc = f.getFormManager().getReferenceLocator();
			Entity ent = loc.getEntityReference(this.entityName);
			if (!(ent instanceof FileManagementEntity)) {
				throw new Exception("Entity " + this.entityName + " does not implement FileManagementEntity");
			}
			if (f == null) {
				throw new Exception("parent form is null");
			}

			final Map<Object, Object> kv = this.getAttachmentValuesKeys(f);
			final ActionEvent eAux = event;
			final FileManagementEntity entF = (FileManagementEntity) ent;
			File fSelec = null;
			boolean temporal = this.synchronize && this.tempFile && this.openFile;
			if (!temporal) {
				String sugName = this.getProposedFileName(event);
				if (sugName != null) {
					this.fileChooser.setSelectedFile(new File(this.fileChooser.getCurrentDirectory(), sugName));
					this.fileChooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, event1 -> {
						if (event1.getPropertyName().equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
							DownloadAttachmentFileAction.this.fileChooser.setSelectedFile(
									new File(DownloadAttachmentFileAction.this.fileChooser.getCurrentDirectory(), DownloadAttachmentFileAction.this.getProposedFileName(eAux)));
						}
					});
				}
				int option = this.fileChooser.showSaveDialog(f);
				DownloadAttachmentFileAction.lastDirectory = this.fileChooser.getCurrentDirectory();
				if (option != JFileChooser.APPROVE_OPTION) {
					return;
				}
				fSelec = this.fileChooser.getSelectedFile();
			} else {
				String n = this.getProposedFileName(eAux);

				if (n != null) {
					int indexOfDot = n.lastIndexOf('.');
					String name = n.substring(0, indexOfDot);
					String ext = n.substring(indexOfDot, n.length());
					StringBuilder builder = new StringBuilder();
					builder.append(name.replace(' ', '_'));
					builder.append("~");
					builder.append(System.currentTimeMillis());
					builder.append(ext);
					fSelec = new File(System.getProperty("java.io.tmpdir", "."), builder.toString());
				} else {
					StringBuilder builder = new StringBuilder();
					builder.append("download");
					builder.append("~");
					builder.append(System.currentTimeMillis());
					fSelec = File.createTempFile(builder.toString(), null);
				}

				fSelec.deleteOnExit();
			}

			final File fSelected = fSelec;
			if (!temporal && fSelected.exists()) {
				if (!f.question("attachment.file_exists_proceed_anyway")) {
					return;
				}
			}
			if (ApplicationManager.DEBUG) {
				DownloadAttachmentFileAction.logger.debug("DownloadAttachmentFile " + fSelected.getCanonicalPath());
			}
			ExtendedOperationThread eop = this.createDownloadThread(f, fSelected, kv, entF, loc);
			if (this.synchronize) {
				Window w = SwingUtilities.getWindowAncestor((Component) event.getSource());
				if (w instanceof Dialog) {
					ApplicationManager.proccessOperation((Dialog) w, eop, 0);
				} else {
					ApplicationManager.proccessOperation((Frame) w, eop, 0);
				}
				if (eop.getResult() != null) {
					f.message(eop.getResult().toString(), Form.ERROR_MESSAGE);
				} else {
					if (!temporal && !this.openFile) {
						f.message("attachment.file_downloaded_successfully", Form.INFORMATION_MESSAGE);
					}
					if (this.openFile) {
						if (temporal || !this.queryOpen || f.question("attachment.would_you_like_to_open_file_now")) {
							com.ontimize.windows.office.WindowsUtils.openFile_Script(fSelected);
						}
					}
				}
			} else {
				ApplicationManager.ExtOpThreadsMonitor m = ApplicationManager.getExtOpThreadsMonitor((Component) event.getSource());
				m.addExtOpThread(eop);
				if ((f.getFormManager() != null) && (f.getFormManager().getApplication() instanceof MainApplication)) {
					((MainApplication) f.getFormManager().getApplication()).registerExtOpThreadsMonitor(m);
				}
				ApplicationManager.getExtOpThreadsMonitor((Component) event.getSource()).setVisible(true);
			}
		} catch (Exception ex) {
			DownloadAttachmentFileAction.logger.error(null, ex);
			Form f = this.getForm(event);
			if (f != null) {
				f.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
			}
		}
	}

	protected ExtendedOperationThread createDownloadThread(Form f, File selectedFile, Map<?, ?> kv, FileManagementEntity entF, EntityReferenceLocator loc) {
		return new DownloadThread(f, selectedFile, kv, entF, loc);
	}

}
