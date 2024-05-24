package com.ontimize.util.xls;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.windows.office.ExecutionResult;
import com.ontimize.windows.office.ScriptUtilities;

public class ClipboardXLSExporterUtils implements XLSExporter {

	private static final Logger logger = LoggerFactory.getLogger(ClipboardXLSExporterUtils.class);

	public ClipboardXLSExporterUtils() {
		// empty constructor
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, Map<?, ?> hColumnRenderers, List<?> columnSort, boolean writeHeader, boolean xlsx, boolean openFile)
			throws Exception {
		if (!openFile) {
			ClipboardXLSExporterUtils.logger.debug("ClipboardXLSExporterUtils --> openFile=false is not available");
		}
		final StringSelection sselection = new StringSelection(this.getXLSString(rs, writeHeader, columnSort));
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sselection, sselection);
		// Generates the excel
		File fScript = ScriptUtilities.createTemporalFileForScript("com/ontimize/windows/office/scripts/excelpaste.vbs");
		List<Object> vParameters = new ArrayList<>();
		vParameters.add(output.getPath());
		ExecutionResult res = ScriptUtilities.executeScript(fScript.getPath(), vParameters, ScriptUtilities.WSCRIPT);
		if (res.getResult() != 0) {
			throw new Exception("M_XLS_FILE_ERROR");
		}
	}

	public String getXLSString(EntityResult res, boolean writeHeader, List<?> columnSort) {
		// Creates a string with all the object data
		// Export to excel: columns separated with tab and rows with enter.

		StringBuilder header = new StringBuilder();
		Enumeration<?> keysEnum = res.keys();

		List<Object> keyNames = new ArrayList<>();
		while (keysEnum.hasMoreElements()) {
			Object name = keysEnum.nextElement();
			if (writeHeader) {
				header.append(name + "\t");
			}
			keyNames.add(name);
		}

		if (columnSort != null) {
			keyNames = new ArrayList<>(columnSort);
		}

		StringBuilder sbValues = new StringBuilder("");
		for (int j = 0; j < res.calculateRecordNumber(); j++) {
			sbValues.append("\n");
			Map<?, ?> record = res.getRecordValues(j);
			for (int i = 0; i < keyNames.size(); i++) {
				Object oValue = record.get(keyNames.get(i));
				String sText = oValue != null ? oValue.toString() : "";
				sbValues.append("\"");
				sbValues.append(sText);
				sbValues.append("\"");
				sbValues.append("\t");
			}
		}
		sbValues.append("\n");
		return header + sbValues.toString();
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, Map<?, ?> hColumnRenderers, List<?> columnSort, List<?> columnStyles, List<?> columnHeaderStyles,
			Workbook wb, boolean xlsx, boolean writeHeader, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, writeHeader, false, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, Map<?, ?> hColumnRenderers, List<?> columnSort, boolean writeHeader, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, writeHeader, false, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, Map<?, ?> hColumnRenderers, List<?> columnSort, List<?> columnStyles, List<?> columnHeaderStyles,
			Workbook wb, boolean writeHeader, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, hColumnRenderers, columnSort, columnStyles, columnHeaderStyles, wb, false, writeHeader, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, boolean writeHeader, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, writeHeader, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, boolean writeHeader, boolean xlsx, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, writeHeader, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, List<?> columnStyles, List<?> columnHeaderStyles, Workbook wb, boolean writeHeader,
			boolean xlsx, boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, columnStyles, columnHeaderStyles, wb, xlsx, writeHeader, openFile);
	}

	@Override
	public void createXLS(EntityResult rs, File output, String sheetName, List<?> columnSort, List<?> columnStyles, List<?> columnHeaderStyles, Workbook wb, boolean writeHeader,
			boolean openFile) throws Exception {
		this.createXLS(rs, output, sheetName, new HashMap<>(), columnSort, columnStyles, columnHeaderStyles, wb, writeHeader, openFile);
	}

}
