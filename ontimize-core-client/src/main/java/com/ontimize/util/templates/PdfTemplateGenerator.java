package com.ontimize.util.templates;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.ontimize.jee.common.tools.StringTools;
import com.ontimize.util.FileUtils;
import com.ontimize.util.pdf.PdfFiller;

public class PdfTemplateGenerator extends AbstractTemplateGenerator implements TemplateGenerator {

	protected boolean showTemplate = true;

	/**
	 * Template creation in PDF format is not supported
	 */
	@Override
	public File createTemplate(Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages) {
		throw new RuntimeException("It isn't supported");
	}

	@Override
	public File fillDocument(InputStream input, String nameFile, Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages,
			Map<Object, Object> valuesPivotTable) throws Exception {
		File directory = FileUtils.createTempDirectory();
		File template = new File(directory.getAbsolutePath(), FileUtils.getFileName(nameFile));
		List<Object> imageField = new ArrayList<>();

		if ((valuesImages != null) && !valuesImages.isEmpty()) {
			for (Entry<Object, Object> entry : valuesImages.entrySet()) {
				Object key = entry.getKey();
				fieldValues.put(key, entry.getValue());
				imageField.add(key);
			}
		}

		PdfFiller.fillTextImageFields(input, new FileOutputStream(template), fieldValues, imageField, true);
		if (this.showTemplate) {
			com.ontimize.windows.office.WindowsUtils.openFile_Script(template);
		}
		return template;
	}

	@Override
	public void setShowTemplate(boolean show) {
		this.showTemplate = show;
	}

	@Override
	public List<String> queryTemplateFields(String template) throws Exception {
		File templateFile = new File(template);
		if (templateFile.exists()) {
			return this.queryTemplateFields(templateFile);
		} else {
			throw new Exception("File " + template + " not found.");
		}

	}

	@Override
	public List<String> queryTemplateFields(File template) throws Exception {
		FileInputStream pdfInputStream = new FileInputStream(template);

		try (ByteArrayOutputStream baOut = new ByteArrayOutputStream()) {
			try (BufferedInputStream bInput = new BufferedInputStream(pdfInputStream)) {
				for (int a = 0; (a = bInput.read()) != -1;) {
					baOut.write(a);
				}
				byte buffer[] = baOut.toByteArray();
				PdfReader reader = new PdfReader(buffer);
				AcroFields form = reader.getAcroFields();
				HashMap<?, ?> fields = form.getFields();
				Iterator<?> names = fields.keySet().iterator();
				List<String> result = new ArrayList<>();
				while (names.hasNext()) {
					result.add(StringTools.toString(names.next()));
				}
				return result;
			}
		}
	}

}
