package com.ontimize.gui.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.db.EntityResultUtils;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.db.PrintDataEntity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.report.ReportUtils;

public class GenerateReportAction extends AbstractButtonAction {

	public static final String M_INSERT_VALUE_FIELD = "value_must_be_entered_message";

	protected String entity = null;

	protected String uriXMLReportDefinition = null;

	protected List<String>		keys					= null;

	protected List<String>		requiredKeys			= null;

	protected boolean preview = true;

	protected boolean printDialog = true;

	public GenerateReportAction(String entity, String uriXMLReportDefinition, boolean preview, List<String> keys, List<String> requiredKeys, boolean printDialog) {
		this.entity = entity;
		this.uriXMLReportDefinition = uriXMLReportDefinition;
		this.keys = keys;
		this.requiredKeys = requiredKeys;
		this.preview = preview;
		this.printDialog = printDialog;
	}

	protected Map<Object, Object> getReportValuesKeys(Form f) throws Exception {
		if ((this.keys == null) || this.keys.isEmpty()) {
			if (f == null) {
				throw new Exception("parent form is null");
			}
			final Map<Object, Object> kv = new HashMap<>();
			List<String> vKeys = f.getKeys();
			if (vKeys.isEmpty()) {
				throw new Exception("The 'keys' parameter is necessary  in the parent form");
			}
			for (String vKey : vKeys) {
				Object oKeyValue = f.getDataFieldValueFromFormCache(vKey.toString());
				if (oKeyValue == null) {
					throw new Exception("Value for the key " + vKey + " not found in parent form");
				}
				kv.put(vKey, oKeyValue);
			}
			return kv;
		} else {
			List<String> vKeys = this.keys;
			Map<Object, Object> kv = new HashMap<>();
			for (Object vKey : vKeys) {
				Object oKeyValue = f.getDataFieldValue(vKey.toString());
				if (oKeyValue == null) {
					if ((this.requiredKeys != null) && this.requiredKeys.contains(vKey)) {
						throw new RuntimeException("");
					}
				} else {
					kv.put(vKey, oKeyValue);
				}
			}
			return kv;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Form f = this.getForm(e);
		try {
			Entity ent = f.getFormManager().getReferenceLocator().getEntityReference(this.entity);
			if (ent instanceof PrintDataEntity) {
				EntityResult res = ((PrintDataEntity) ent).getPrintingData(this.getReportValuesKeys(f),
						f.getFormManager().getReferenceLocator().getSessionId());
				if (res.getCode() == EntityResult.OPERATION_WRONG) {
					f.message(res.getMessage(), Form.ERROR_MESSAGE);
				} else if (res.isEmpty()) {
					f.message("M_NOT_RESULTS_FOUND", Form.WARNING_MESSAGE);
				} else {
					java.net.URL url = this.getClass().getClassLoader().getResource(this.uriXMLReportDefinition);
					com.ontimize.report.utils.PreviewDialog d = ReportUtils.getPreviewDialog(f,
							ApplicationManager.getTranslation("TituloImpresionAlbaran", f.getResourceBundle()),
							EntityResultUtils.createTableModel(res), url, null);
					if (!this.preview) {
						d.print(this.printDialog);
					} else {
						d.setVisible(true);
					}
				}
			} else {
				f.message("M_INVALID_REPORT_ENTITY_NO_REPORT_DATA_FOUND", Form.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			if (!(ex instanceof RuntimeException)) {
				f.message(ex.getMessage(), Form.ERROR_MESSAGE, ex);
			}
		}

	}

}
