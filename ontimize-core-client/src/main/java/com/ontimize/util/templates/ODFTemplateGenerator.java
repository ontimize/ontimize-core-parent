package com.ontimize.util.templates;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.util.FileUtils;
import com.ontimize.windows.office.WindowsUtils;

/**
 * Create ODF (Open Document Format) templates (with or without data). This class uses JooReports library and its dependencies.
 *
 * More information in: http://jooreports.sourceforge.net
 *
 * @author Imatia Innovation
 * @since 21/06/2007 Fill template
 * @since 30/11/2007 Create empty template
 */
public class ODFTemplateGenerator extends AbstractTemplateGenerator {

	private static final Logger	logger					= LoggerFactory.getLogger(ODFTemplateGenerator.class);

	public static boolean		DEBUG					= true;

	private static final String	DEFAULT_JARS_PROPERTIES	= "com/ontimize/util/templates/odf.properties", ODF_EMPTY_TEMPLATE = "com/ontimize/util/templates/template.odt",
			DEFAULT_IMAGE_FORMAT = "png";

	private static boolean		librariesChecked		= false;

	protected boolean			showTableTotals			= false;

	protected boolean			showTemplate			= false;

	protected void log(String log) {
		if (ODFTemplateGenerator.DEBUG) {
			ODFTemplateGenerator.logger.debug(log);
		}
	}

	/**
	 * Check if JOOReports jars are avaliable.
	 */
	public static boolean checkLibraries() {
		if (ODFTemplateGenerator.librariesChecked) {
			return true;
		}
		JarVerifier jv = new JarVerifier(ODFTemplateGenerator.DEFAULT_JARS_PROPERTIES);
		ODFTemplateGenerator.librariesChecked = jv.verify();
		return ODFTemplateGenerator.librariesChecked;
	}

	@Override
	public File createTemplate(Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages) throws Exception {

		valuesTable = ODFParser.translateTableDotFields(valuesTable);
		fieldValues = ODFParser.translateDotFields(fieldValues);
		valuesImages = ODFParser.translateDotFields(valuesImages);

		this.log("Create template from form ...");
		long init = System.currentTimeMillis();

		java.net.URL url = this.getClass().getClassLoader().getResource(ODFTemplateGenerator.ODF_EMPTY_TEMPLATE);
		InputStream input = url.openStream();

		ODFParser op = new ODFParser(input);
		File f = op.create(fieldValues, valuesTable, valuesImages, AbstractTemplateGenerator.createLabelsInTemplate);
		op = null;
		input.close();

		long end = System.currentTimeMillis();
		this.log("\nCreated in " + (end - init) + " ms.\n");
		if (this.showTemplate) {
			WindowsUtils.openFile_Script(f);
		}
		return f;
	}

	@Override
	public File fillDocument(String resource, Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages,
			Map<Object, Object> valuesPivotTable) throws Exception {

		this.log("Fill ODF document ... " + resource);

		valuesTable = ODFParser.translateTableDotFields(valuesTable);
		fieldValues = ODFParser.translateDotFields(fieldValues);
		valuesImages = ODFParser.translateDotFields(valuesImages);

		// Check the library. Verify in the class loader.
		if (!ODFTemplateGenerator.checkLibraries()) {
			return null;
		}

		java.net.URL url = this.getClass().getClassLoader().getResource(resource);
		InputStream input = url.openStream();

		// List templateFields = queryTemplateFields(resource);
		// OpenOfficeTemplateFields ooTF = new
		// OpenOfficeTemplateFields(templateFields);
		//
		// fieldValues = ooTF.checkTemplateFieldValues(fieldValues);
		// valuesTable = ooTF.checkTemplateTableValues(valuesTable);

		File f = this.fillDocument(input, FileUtils.getFileName(resource), fieldValues, valuesTable, valuesImages, valuesPivotTable);
		input.close();
		return f;
	}

	@Override
	public File fillDocument(InputStream input, String nameFile, Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages,
			Map<Object, Object> valuesPivotTable) throws Exception {

		valuesTable = ODFParser.translateTableDotFields(valuesTable);
		fieldValues = ODFParser.translateDotFields(fieldValues);
		valuesImages = ODFParser.translateDotFields(valuesImages);

		this.log("Fill ODF document ... " + nameFile);
		long init = System.currentTimeMillis();

		// Check the library. Verify in the class loader.
		if (!ODFTemplateGenerator.checkLibraries()) {
			return null;
		}

		// Copy.
		String tmp = System.getProperty("java.io.tmpdir");
		File directory = (tmp != null) && (tmp.length() != 0) ? new File(tmp) : FileUtils.createTempDirectory(); // Create
		// it.
		directory.deleteOnExit();
		File template = new File(directory, FileUtils.getFileName(nameFile));

		ODFFreeMarkerParser ofmp = new ODFFreeMarkerParser(input);

		List<String> templateFields = ofmp.queryTemplateFields();
		OpenOfficeTemplateFields ooTF = new OpenOfficeTemplateFields(templateFields);

		fieldValues = ooTF.checkTemplateFieldValues(fieldValues);

		File templateOut = ofmp.parse(fieldValues, valuesTable, valuesImages, valuesPivotTable);
		templateOut.deleteOnExit();

		Map<Object, Object> allTables = new HashMap<>();
		if (valuesTable != null) {
			allTables.putAll(valuesTable);
		}
		if (valuesPivotTable != null) {
			allTables.putAll(valuesPivotTable);
		}
		allTables = ooTF.checkTemplateTableValues(allTables);

		if (templateOut == null) {
			throw new Exception("TEMPLATE_NOT_FOUND" + ofmp);
		}
		// Fields.
		Map<Object, Object> h = this.createDateHashtableParsed(fieldValues);

		// Tables.
		if ((allTables != null) && !allTables.isEmpty()) {

			// For each table
			Enumeration e = allTables.keys();
			Collection v = allTables.values();
			Iterator i = v.iterator();

			while (e.hasMoreElements()) {

				Object o = e.nextElement();
				if ((o == null) || !(o instanceof String)) {
					i.next();
					continue;
				}

				String table = (String) o;
				o = i.next();
				if ((o == null) || !(o instanceof Hashtable)) {
					continue;
				}

				Hashtable t = (Hashtable) o;
				Hashtable p = this.createDateHashtableParsed(t); // p is a Copy
				if (this.showTableTotals) {
					this.generateTotalsHashtableRow(p);
				}
				List l = this.createTableList(p);
				if (l != null) {
					h.put(table, l);
				}
			}
		}

		// Images.
		net.sf.jooreports.templates.images.ByteArrayImageProvider imageProvider = new net.sf.jooreports.templates.images.ByteArrayImageProvider();
		if ((valuesImages != null) && !valuesImages.isEmpty()) {

			Enumeration e = valuesImages.keys();
			Collection v = valuesImages.values();
			Iterator i = v.iterator();

			while (e.hasMoreElements()) {

				Object o = e.nextElement();
				if ((o == null) || !(o instanceof String)) {
					i.next();
					continue;
				}
				String image = (String) o;
				o = i.next();
				if (o == null) {
					continue;
				}

				// ByteArrayOutputStream, File, Image, Ontimize BytesBlock
				byte[] data = this.createImageBytes(o);
				if (data != null) {
					imageProvider.setImage(image, data);
				}
			}
		}

		this.log("Sending data to JOOReports using template " + templateOut.getAbsolutePath());

		net.sf.jooreports.templates.DocumentTemplate dt = null;
		if (templateOut.isDirectory()) {
			dt = new net.sf.jooreports.templates.UnzippedDocumentTemplate(templateOut);
		} else {
			dt = new net.sf.jooreports.templates.ZippedDocumentTemplate(templateOut);
		}

		dt.createDocument(h, new FileOutputStream(template), imageProvider);

		if (this.showTemplate) {
			// Open file
			WindowsUtils.openFile_Script(template);
		}

		long end = System.currentTimeMillis();
		this.log("Filled in " + (end - init) + " ms.");
		return template;
	}

	/**
	 * If true, the table shows a new row with the column totals. If the column isn't a numeric value, shows the row count. The default is false.
	 */
	public void setShowTableTotals(boolean showTableTotals) {
		this.showTableTotals = showTableTotals;
	}

	@Override
	public void setShowTemplate(boolean show) {
		this.showTemplate = show;
	}

	/**
	 * Create a copy of the parameter hashtable to parse the containing dates with the current date format parser.
	 * 
	 * @param h
	 *            Hashtable to parse.
	 * @return A copy of the hastable to parse.
	 */
	private EntityResult createDateHashtableParsed(final EntityResult h) {
		if (h == null) {
			return new EntityResultMapImpl();
		}

		EntityResult p = new EntityResultMapImpl(Collections.list(h.keys()));

		Set<?> s = h.entrySet();
		Iterator<?> i = s.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if ((o == null) || !(o instanceof Map.Entry)) {
				continue;
			}
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			Object k = e.getKey();
			Object v = e.getValue();

			if ((v instanceof java.util.Date) || (v instanceof java.sql.Timestamp)) {
				v = this.dateFormat.format(v);
			} else if (v instanceof List) { // EntityResult
				List<Object> l = new ArrayList<>((List<?>) v);

				final int size = l.size();
				for (int j = 0; j < size; j++) {
					Object tmp = l.get(j);
					if ((tmp instanceof java.util.Date) || (tmp instanceof java.sql.Timestamp)) {
						l.set(j, this.dateFormat.format((Date) tmp));
					}
				}
			}
			p.put(k, v);
		}
		return p;
	}

	private void generateTotalsHashtableRow(EntityResult h) {
		if (h == null) {
			return;
		}

		Set<?> s = h.entrySet();
		Iterator<?> i = s.iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if ((o == null) || !(o instanceof Map.Entry)) {
				continue;
			}
			// Map.Entry e = (Map.Entry) o;
			// Object k = e.getKey();
			// Object v = e.getValue();

			// TODO v must be a vector, and values must be integer objects, to
			// get
			// the sum of them, if there is other data type then only count the
			// attemps

		}
	}

	private List<Object> createTableList(Map<Object, Object> h) {
		if (h == null) {
			return null;
		}

		List<Object> l = new ArrayList<>();

		Set keys = h.keySet();
		if (keys.isEmpty()) {
			return l;
		}

		// Class to represent the bean. Use to avoid memory problems in the
		// BeanCreator
		Class c = dynclass.BeanCreator.createClassForProperties(keys);

		Object[] keysA = keys.toArray();
		Object o = h.get(keysA[0]);

		// o must be a vector
		if ((o == null) || !(o instanceof List)) {
			return l;
		}

		List<?> v1 = (List<?>) o;
		int size = v1.size();

		for (int i = 0; i < size; i++) { // Row.
			Map<Object, Object> row = new HashMap<>(keysA.length);

			for (int j = 0; j < keysA.length; j++) { // Column.
				Object cObj = keysA[j];
				Object rObj = h.get(cObj);

				if ((rObj == null) || !(rObj instanceof List) || (((List<?>) rObj).size() == 0)) {
					continue;
				}
				List<?> v = (List<?>) rObj;

				o = v.get(i);
				row.put(cObj, o == null ? new String() : o);
			}

			try {
				Object bean = dynclass.BeanCreator.createBean(c, row);
				if (bean != null) {
					l.add(bean);
				}
			} catch (Exception exc) {
				ODFTemplateGenerator.logger.error(null, exc);
			}
		}
		return l;
	}

	private byte[] createImageBytes(Object o) throws Exception {
		byte[] data = null;

		if (o == null) {
			return data;
		}

		if (o instanceof ByteArrayOutputStream) {
			data = ((ByteArrayOutputStream) o).toByteArray();
		} else if (o instanceof File) {
			File f = (File) o;
			if (f.exists() && f.isFile()) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(ImageIO.read(f), ODFTemplateGenerator.DEFAULT_IMAGE_FORMAT, baos);
				baos.flush();

				data = baos.toByteArray();
				baos.close();
			}
		} else if (o instanceof Image) {
			Image img = (Image) o;

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (img instanceof RenderedImage) {
				ImageIO.write((RenderedImage) img, ODFTemplateGenerator.DEFAULT_IMAGE_FORMAT, baos);
			} else {
				BufferedImage bim = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
				Graphics g = bim.getGraphics();
				g.drawImage(img, 0, 0, null);
				bim.flush();
				ImageIO.write(bim, ODFTemplateGenerator.DEFAULT_IMAGE_FORMAT, baos);
			}
			baos.flush();

			data = baos.toByteArray();
			baos.close();
		} else if (o instanceof BytesBlock) {
			data = ((BytesBlock) o).getBytes();
		}
		return data;
	}

	@Override
	public List<String> queryTemplateFields(String template) throws Exception {
		return this.queryTemplateFields(new File(template));
	}

	@Override
	public List<String> queryTemplateFields(File template) throws Exception {
		ODFParser op = new ODFParser(template);
		return op.queryTemplateFields();
	}

}
