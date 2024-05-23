package com.ontimize.gui.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.FileManagementEntity;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

public class DeleteAttachmentFileAction extends AbstractButtonAction {

	private static final Logger	logger		= LoggerFactory.getLogger(DeleteAttachmentFileAction.class);

	protected String			entityName	= null;

	protected boolean			refresh		= false;

	public DeleteAttachmentFileAction(String entity, boolean refresh) {
		this.entityName = entity;
		this.refresh = refresh;
	}

	public void setEntity(String entity) {
		this.entityName = entity;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final Form f = this.getForm(event);
		EntityReferenceLocator referenceLocator = f.getFormManager().getReferenceLocator();
		try {
			Entity entity = referenceLocator.getEntityReference(this.entityName);
			if (entity instanceof FileManagementEntity) {
				FileManagementEntity eGA = (FileManagementEntity) entity;
				Map<Object, Object> kv = this.getAttachmentValuesKeys(f);
				boolean delete = eGA.deleteAttachmentFile(kv, referenceLocator.getSessionId());
				if (this.refresh) {
					f.refreshCurrentDataRecord();
				}

				if (delete) {
					f.message("The attach file has been deleted successfully", JOptionPane.INFORMATION_MESSAGE);
				} else {
					f.message("Error when the attach file was being deleted", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception ex) {
			DeleteAttachmentFileAction.logger.error(null, ex);
			f.message("Error when the attach file was being deleted: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE, ex);
		}
	}

	protected Map<Object, Object> getAttachmentValuesKeys(Form f) throws Exception {
		final Map<Object, Object> kv = new HashMap<>();
		List<?> vKeys = f.getKeys();
		if (vKeys.isEmpty()) {
			throw new Exception("The 'keys' parameter is necessary  in the parent form");
		}
		for (int i = 0; i < vKeys.size(); i++) {
			Object oKeyValue = f.getDataFieldValueFromFormCache(vKeys.get(i).toString());
			if (oKeyValue == null) {
				throw new Exception("Value of the key " + vKeys.get(i) + " not found in the parent form");
			}
			kv.put(vKeys.get(i), oKeyValue);
		}
		return kv;
	}

}
