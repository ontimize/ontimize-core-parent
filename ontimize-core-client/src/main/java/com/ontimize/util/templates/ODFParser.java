package com.ontimize.util.templates;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.util.FileUtils;

/**
 * Generates a new ODF template file from a base empty ODF file. This file contains the input fields, tables and images to fill with the values of the form.
 *
 * @author Imatia Innovation
 * @since Ontimize 5.1085
 */
public class ODFParser {

	private static final Logger	logger			= LoggerFactory.getLogger(ODFParser.class);

	public static boolean		DEBUG			= true, VERBOSE = true;

	public static final String	FILE_TO_PARSE	= "content.xml", FILE_DIR_ODF_IMAGES = "Pictures", FILE_EMPTY_IMAGE = ImageManager.EMPTY_IMAGE,

			XML_TAG_STYLE_AUT = "office:automatic-styles", XML_TAG_STYLE = "style:style", XML_TAG_STYLE_NAME = "style:name", XML_TAG_STYLE_NAME_4_BOLD_VALUE = "Text.Bold",
			XML_TAG_STYLE_NAME_4_TABLE_VALUE = "Table.Cell.Line.Border", XML_TAG_STYLE_FAMILY = "style:family", XML_TAG_STYLE_FAMILY_4_BOLD_VALUE = "paragraph",
			XML_TAG_STYLE_FAMILY_4_TABLE_VALUE = "table-cell", XML_TAG_STYLE_TEXT = "style:text-properties", XML_TAG_STYLE_WEIGHT = "fo:font-weight",
			XML_TAG_STYLE_WEIGHT_VALUE = "bold", XML_TAG_STYLE_CELL = "style:table-cell-properties", XML_TAG_STYLE_BORDER = "fo:border",
			XML_TAG_STYLE_BORDER_VALUE = "0.002cm solid #000000",

			XML_TAG_OFFICE_TEXT = "office:text",

			XML_TAG_TEXT = "text:p", XML_TAG_TEXT_STYLE = "text:style-name",

			XML_TAG_INPUT = "text:text-input", XML_TAG_INPUT_DESCRIP = "text:description",

			XML_TAG_TABLE = "table:table", XML_TAG_TABLE_NAME = "table:name", XML_TAG_TABLE_ROW = "table:table-row", XML_TAG_TABLE_COLUMN = "table:table-column",
			XML_TAG_TABLE_COLUMN_NUMBER = "table:number-columns-repeated", XML_TAG_TABLE_CELL = "table:table-cell", XML_TAG_TABLE_CELL_STYLE = "table:style-name",
			XML_TAG_TABLE_CELL_TYPE = "office:value-type", XML_TAG_TABLE_CELL_TYPE_VALUE = "string",

			XML_TAG_DRAW_FRAME = "draw:frame", XML_TAG_DRAW_FRAME_NAME = "draw:name", XML_TAG_DRAW_FRAME_ANCHOR = "text:anchor-type", XML_TAG_DRAW_FRAME_ANCHOR_VALUE = "as-char",
			XML_TAG_DRAW_FRAME_WIDTH = "svg:width", XML_TAG_DRAW_FRAME_WIDTH_VALUE = "5cm", XML_TAG_DRAW_FRAME_HEIGHT = "svg:height", XML_TAG_DRAW_FRAME_HEIGHT_VALUE = "5cm",
			XML_TAG_DRAW_IMAGE = "draw:image", XML_TAG_DRAW_IMAGE_HREF = "xlink:href";

	protected static void log(String log) {
		if (ODFParser.DEBUG) {
			ODFParser.logger.debug(log);
		}
	}

	protected static void verbose(String verbose) {
		if (ODFParser.VERBOSE) {
			ODFParser.logger.debug(verbose);
		}
	}

	/**
	 * Change dot character "." on keys with "ç" character
	 * 
	 * @param values
	 * @return
	 */
	public static Map<Object, Object> translateDotFields(Map<Object, Object> values) {

		try {
			Map<Object, Object> translations = new HashMap<>();
			if (values == null) {
				return translations;
			}
			Iterator<?> valuesit = values.keySet().iterator();
			while (valuesit.hasNext()) {
				Object o = valuesit.next();
				if ((o != null) && (o instanceof String)) {
					String str = o.toString();
					if (str.indexOf(".") > -1) {
						String stralt = str.replaceAll("\\.", "ç");
						translations.put(str, stralt);
					}
				}
			}
			Iterator<?> transit = translations.keySet().iterator();
			while (transit.hasNext()) {
				String val = transit.next().toString();
				values.put(translations.get(val), values.remove(val));
			}
			// since 5.3.8
			// order columns should be the same as entity result because
			// are used in method getKeysOrder()
			if (values instanceof EntityResult) {
				((EntityResult) values).setColumnOrder(new ArrayList<>(translations.values()));
			}
		} catch (Exception e) {
			ODFParser.logger.error(null, e);
		}
		return values;
	}

	/**
	 * ODF file.
	 */
	protected InputStream	input;

	/**
	 * Uncompressed ODF file (Ref. to temporary directory).
	 */
	protected File			temp;

	public ODFParser(File file) throws IOException {
		this(new FileInputStream(file));
	}

	public ODFParser(InputStream input) throws IOException {
		this.input = input;

		this.init();
	}

	protected void init() throws IOException {
		this.temp = FileUtils.createTempDirectory();
		this.unzip(this.input, this.temp);
	}

	public File getTemporalDiretory() {
		return this.temp;
	}

	public File create(Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages, boolean createLabels) throws Exception {

		ODFParser.log("ODFParser -> Creating empty template for given data");

		valuesTable = ODFParser.translateTableDotFields(valuesTable);
		fieldValues = ODFParser.translateDotFields(fieldValues);
		valuesImages = ODFParser.translateDotFields(valuesImages);

		if (this.input == null) {
			return null;
		}

		File fp = this.get(ODFParser.FILE_TO_PARSE);
		if ((fp == null) || !fp.exists()) {
			ODFParser.log("ODFParser -> File to create not found. " + fp.getCanonicalPath());
			return null;
		}

		Document d = ODFParser.getDocument(fp);
		Element e = d.getDocumentElement();

		// Gets the styles of the document.
		NodeList nloas = e.getElementsByTagName(ODFParser.XML_TAG_STYLE_AUT);
		if (nloas.getLength() != 1) {
			ODFParser.log("ODFParser -> " + ODFParser.XML_TAG_STYLE_AUT + " wrong.");
			return null;
		}
		Element eoas = (Element) nloas.item(0);

		Element[] s = this.createStyleElements(d); // Defaults styles.
		for (int i = 0, size = s.length; i < size; i++) {
			eoas.appendChild(s[i]);
		}

		// Gets the body of the document.
		NodeList nlot = e.getElementsByTagName(ODFParser.XML_TAG_OFFICE_TEXT);
		if (nlot.getLength() != 1) {
			ODFParser.log("ODFParser -> " + ODFParser.XML_TAG_OFFICE_TEXT + " wrong.");
			return null;
		}
		Element eot = (Element) nlot.item(0);

		Element[] f = this.createFieldElements(d, fieldValues, createLabels); // Fields.
		for (int i = 0, size = f.length; i < size; i++) {
			eot.appendChild(f[i]);
		}
		eot.appendChild(this.createSeparatorElement(d));

		Element[] t = this.createTableElements(d, valuesTable); // Tables.
		for (int i = 0, size = t.length; i < size; i++) {
			eot.appendChild(t[i]);
			eot.appendChild(this.createSeparatorElement(d));
		}

		Element[] i = this.createImageElements(d, valuesImages); // Images.
		for (int j = 0, size = i.length; j < size; j++) {
			eot.appendChild(i[j]);
			eot.appendChild(this.createSeparatorElement(d));
		}

		// Save the current FILE_TO_PARSE XML.
		ODFParser.log("ODFParser -> Save modified XML file to " + fp.getCanonicalPath());

		// Save XML and zip the temporal directory.
		ODFParser.setDocument(d, fp);
		File file = new File(this.temp.getParent(), this.temp.getName() + ".odt");
		this.zip(this.temp, file);

		this.input.close();
		return file;

	}

	public File create(Map<Object, Object> fieldValues, Map<Object, Object> valuesTable, Map<Object, Object> valuesImages) throws Exception {
		return this.create(fieldValues, valuesTable, valuesImages, true);
	}

	private Element[] createStyleElements(Document d) {
		Element[] l = new Element[2];

		// Creates a style for the bold text.
		Element bold = d.createElement(ODFParser.XML_TAG_STYLE);
		bold.setAttribute(ODFParser.XML_TAG_STYLE_NAME, ODFParser.XML_TAG_STYLE_NAME_4_BOLD_VALUE);
		bold.setAttribute(ODFParser.XML_TAG_STYLE_FAMILY, ODFParser.XML_TAG_STYLE_FAMILY_4_BOLD_VALUE);

		Element boldProp = d.createElement(ODFParser.XML_TAG_STYLE_TEXT);
		boldProp.setAttribute(ODFParser.XML_TAG_STYLE_WEIGHT, ODFParser.XML_TAG_STYLE_WEIGHT_VALUE);
		bold.appendChild(boldProp);

		l[0] = bold;

		// Creates a style for the cell tables (border line)
		Element cell = d.createElement(ODFParser.XML_TAG_STYLE);
		cell.setAttribute(ODFParser.XML_TAG_STYLE_NAME, ODFParser.XML_TAG_STYLE_NAME_4_TABLE_VALUE);
		cell.setAttribute(ODFParser.XML_TAG_STYLE_FAMILY, ODFParser.XML_TAG_STYLE_FAMILY_4_TABLE_VALUE);

		Element cellProp = d.createElement(ODFParser.XML_TAG_STYLE_CELL);
		cellProp.setAttribute(ODFParser.XML_TAG_STYLE_BORDER, ODFParser.XML_TAG_STYLE_BORDER_VALUE);
		cell.appendChild(cellProp);

		l[1] = cell;
		return l;
	}

	private Element createSeparatorElement(Document d) {
		return d.createElement(ODFParser.XML_TAG_TEXT);
	}

	private Element[] createFieldElements(Document document, Map<?, ?> fields, boolean createLabel) {
		if ((fields == null) || fields.isEmpty()) {
			return new Element[0];
		}

		List<Object> elements = new ArrayList<>();
		// The keys contains the field names.
		Object[] keysOrder = AbstractTemplateGenerator.getKeysOrder(fields);
		for (int k = 0; k < keysOrder.length; k++) {
			Object oKey = keysOrder[k];
			if ((oKey == null) || !(oKey instanceof String)) {
				continue;
			}

			Object oValue = fields.get(oKey);
			String key = (String) oKey;

			if (oValue instanceof Map) {
				// Create a group with a title in the template
				Element eTitle = document.createElement(ODFParser.XML_TAG_TEXT); // Title
				eTitle.appendChild(document.createTextNode(key));
				elements.add(eTitle);
				Object[] keysOrderGroup = AbstractTemplateGenerator.getKeysOrder((Map<Object, Object>) oValue);
				for (int j = 0; j < keysOrderGroup.length; j++) {
					if (keysOrderGroup[j] instanceof String) {
						String groupElementKey = (String) keysOrderGroup[j];
						Object groupElementValue = ((Map<?, ?>) oValue).get(groupElementKey);
						Element eText = document.createElement(ODFParser.XML_TAG_TEXT); // Text.
						if (createLabel) {
							eText.appendChild(document.createTextNode(groupElementValue + ": "));
						}
						Element eInput = document.createElement(ODFParser.XML_TAG_INPUT); // Input
						// text.
						eInput.setAttribute(ODFParser.XML_TAG_INPUT_DESCRIP, groupElementKey);
						eInput.appendChild(document.createTextNode(groupElementKey));

						eText.appendChild(eInput);
						elements.add(eText);
					}
				}
				if (k < (keysOrder.length - 1)) {
					Element element = document.createElement(ODFParser.XML_TAG_TEXT); // Blank
					element.appendChild(document.createTextNode(""));
					elements.add(element);
				}
			} else {
				Element eText = document.createElement(ODFParser.XML_TAG_TEXT); // Text.
				if (createLabel) {
					eText.appendChild(document.createTextNode(oValue + ": "));
				}

				Element eInput = document.createElement(ODFParser.XML_TAG_INPUT); // Input
				// text.
				eInput.setAttribute(ODFParser.XML_TAG_INPUT_DESCRIP, key);
				eInput.appendChild(document.createTextNode(key));

				eText.appendChild(eInput);
				elements.add(eText);
			}
		}
		Element[] res = new Element[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			res[i] = (Element) elements.get(i);
		}
		return res;
	}

	private Element[] createTableElements(Document d, Map<?, ?> t) {
		if ((t == null) || t.isEmpty()) {
			return new Element[0];
		}

		Element[] l = new Element[t.size()];

		Iterator<?> k = t.keySet().iterator();
		Collection<?> c = t.values();
		Iterator<?> v = c.iterator();
		int i = 0;

		while (k.hasNext()) {
			Object o = k.next();
			if ((o == null) || !(o instanceof String)) {
				v.next();
				continue;
			}
			String key = (String) o; // Table name.

			o = v.next();
			if ((o == null) || !(o instanceof Map)) {
				continue;
			}
			Map<?, ?> value = (Map<?, ?>) o; // Table.
			int cols = value.size();

			Element table = d.createElement(ODFParser.XML_TAG_TABLE);
			table.setAttribute(ODFParser.XML_TAG_TABLE_NAME, key);

			Element columns = d.createElement(ODFParser.XML_TAG_TABLE_COLUMN);
			columns.setAttribute(ODFParser.XML_TAG_TABLE_COLUMN_NUMBER, Integer.toString(cols));
			table.appendChild(columns);

			// Create the title row.
			Element titles = d.createElement(ODFParser.XML_TAG_TABLE_ROW);
			table.appendChild(titles);

			// Create the data row.
			Element data = d.createElement(ODFParser.XML_TAG_TABLE_ROW);
			table.appendChild(data);

			Iterator<?> e = value.keySet().iterator();
			while (e.hasNext()) {
				o = e.next();
				if ((o == null) || !(o instanceof String)) {
					continue;
				}
				String columnName = (String) o;
				String translatedColumnName = (String) value.get(o);
				String cellName = key + "." + columnName;

				// Title cell.
				Element cell = d.createElement(ODFParser.XML_TAG_TABLE_CELL);
				cell.setAttribute(ODFParser.XML_TAG_TABLE_CELL_STYLE, ODFParser.XML_TAG_STYLE_NAME_4_TABLE_VALUE);
				cell.setAttribute(ODFParser.XML_TAG_TABLE_CELL_TYPE, ODFParser.XML_TAG_TABLE_CELL_TYPE_VALUE);

				Element cellText = d.createElement(ODFParser.XML_TAG_TEXT);
				cellText.setAttribute(ODFParser.XML_TAG_TEXT_STYLE, ODFParser.XML_TAG_STYLE_NAME_4_BOLD_VALUE);
				cellText.appendChild(d.createTextNode(translatedColumnName));
				cell.appendChild(cellText);

				titles.appendChild(cell);

				// Data cell.
				cell = d.createElement(ODFParser.XML_TAG_TABLE_CELL);
				cell.setAttribute(ODFParser.XML_TAG_TABLE_CELL_STYLE, ODFParser.XML_TAG_STYLE_NAME_4_TABLE_VALUE);
				cell.setAttribute(ODFParser.XML_TAG_TABLE_CELL_TYPE, ODFParser.XML_TAG_TABLE_CELL_TYPE_VALUE);

				cellText = d.createElement(ODFParser.XML_TAG_TEXT);
				cell.appendChild(cellText);

				Element cellInput = d.createElement(ODFParser.XML_TAG_INPUT);
				cellText.appendChild(cellInput);
				cellInput.setAttribute(ODFParser.XML_TAG_INPUT_DESCRIP, cellName);
				cellInput.appendChild(d.createTextNode(cellName));

				data.appendChild(cell);
			}
			l[i++] = table;
		}
		return l;
	}

	private Element[] createImageElements(Document d, Map<Object, Object> i) throws Exception {
		if ((i == null) || i.isEmpty()) {
			return new Element[0];
		}

		Element[] l = new Element[i.size()];

		File dImages = this.get(ODFParser.FILE_DIR_ODF_IMAGES);
		if (!dImages.exists()) {
			dImages.mkdirs();
		}

		Iterator<?> k = i.keySet().iterator();
		int c = 0;

		int imageIndex = 0;

		while (k.hasNext()) {

			InputStream empty = this.getEmptyImageInputStream(imageIndex);
			imageIndex = imageIndex + 1;

			Object o = k.next();
			if ((o == null) || !(o instanceof String)) {
				continue;
			}
			String key = (String) o;

			Element eText = d.createElement(ODFParser.XML_TAG_TEXT);
			Element eFrame = d.createElement(ODFParser.XML_TAG_DRAW_FRAME);
			eText.appendChild(eFrame);

			eFrame.setAttribute(ODFParser.XML_TAG_DRAW_FRAME_NAME, key);
			eFrame.setAttribute(ODFParser.XML_TAG_DRAW_FRAME_ANCHOR, ODFParser.XML_TAG_DRAW_FRAME_ANCHOR_VALUE);
			eFrame.setAttribute(ODFParser.XML_TAG_DRAW_FRAME_WIDTH, ODFParser.XML_TAG_DRAW_FRAME_WIDTH_VALUE);
			eFrame.setAttribute(ODFParser.XML_TAG_DRAW_FRAME_HEIGHT, ODFParser.XML_TAG_DRAW_FRAME_HEIGHT_VALUE);

			Element eImage = d.createElement(ODFParser.XML_TAG_DRAW_IMAGE);
			eFrame.appendChild(eImage);
			eImage.setAttribute(ODFParser.XML_TAG_DRAW_IMAGE_HREF, ODFParser.FILE_DIR_ODF_IMAGES + "/" + key + ".png");

			FileUtils.copyFile(empty, new File(dImages, key + ".png"));
			l[c++] = eText;

		}
		return l;
	}

	protected InputStream getEmptyImageInputStream(int imageIndex) throws Exception {
		// Get a reference to the empty image.
		int width = 150;
		int height = 60;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
		graphics.setBackground(Color.white);
		graphics.fillRect(0, 0, width, height);
		graphics.setColor(Color.red);
		graphics.drawString("Image", 50, 10);
		graphics.drawString("not available", 30, 30);
		graphics.drawString("" + imageIndex, 60, 50);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "jpg", out);
		ByteArrayInputStream result = new ByteArrayInputStream(out.toByteArray());
		return result;
	}

	/**
	 * Returns the file inside of the ODF document. Note: The ODF document is a ZIP file.
	 */
	public File get(String path) {
		if (this.temp == null) {
			return null;
		}
		if ((path == null) || (path.length() == 0)) {
			return this.temp;
		}
		return new File(this.temp, path);
	}

	public static Document getDocument(File f) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(f);
		return document;
	}

	public static void setDocument(Document d, File f) throws Exception {
		Source source = new DOMSource(d);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result resFile = new StreamResult(f);
		transformer.transform(source, resFile);
	}

	public void zip(File input, File output) throws IOException {
		ODFParser.verbose("Zip to " + output.getCanonicalPath() + "\n\n");

		// List all files in directory in recursive mode.
		ArrayList<Object> list = new ArrayList<>();
		this.listFiles(input, list);

		String baseStr = input.getCanonicalPath();
		int baseLength = baseStr.length() + 1;
		byte[] buffer = new byte[4096];
		FileOutputStream fos = new FileOutputStream(output);
		ZipOutputStream out = new ZipOutputStream(fos);

		for (int i = 0, size = list.size(); i < size; i++) {

			// Current file.
			Object o = list.get(i);
			if ((o == null) || !(o instanceof File)) {
				continue;
			}
			File current = (File) o;
			if (current.isDirectory()) {
				continue;
			}

			String currentStr = current.getCanonicalPath();
			if (baseLength < currentStr.length()) {
				currentStr = currentStr.substring(baseLength);
			}
			currentStr = currentStr.replace('\\', '/');

			// Insert in ZIP file.
			ZipEntry ze = new ZipEntry(currentStr);
			out.putNextEntry(ze);
			ODFParser.verbose("\rZip file " + currentStr + ". ");

			if (!ze.isDirectory()) {
				try (FileInputStream fis = new FileInputStream(current)) {
					int bytesTotal = 0, bytesRead = 0;
					while ((bytesRead = fis.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
						bytesTotal += bytesRead;
					}
					ODFParser.verbose("Written bytes " + bytesTotal);
				}
			}

			out.closeEntry();
		}

		out.flush();
		fos.flush();
		out.close();
		fos.close();
	}

	protected void listFiles(File file, List<Object> list) {

		if ((file == null) || !file.exists()) {
			return;
		}

		list.add(file);

		if (!file.isDirectory()) {
			return;
		}

		File[] f = file.listFiles();
		for (int i = 0, size = f.length; i < size; i++) {
			this.listFiles(f[i], list);
		}
	}

	/**
	 * Unzip the current InputStream to a temporal directory.
	 */
	public void unzip(InputStream input, File output) throws IOException {
		ODFParser.verbose("Unzip to " + output + "\n\n");

		byte[] buffer = new byte[4096];

		ZipInputStream in = new ZipInputStream(input);
		ZipEntry entry = null;
		FileOutputStream out = null;

		while ((entry = in.getNextEntry()) != null) {
			File fout = new File(output, entry.getName());
			String s = fout.getCanonicalPath();

			if (entry.isDirectory()) {
				if (fout.mkdirs()) {
					ODFParser.verbose("\rUnzip directory " + s + ". Created sucessfully.");
				} else {
					ODFParser.verbose("\rUnzip directory " + s + ". Directory not created.");
				}
			} else {
				ODFParser.verbose("\rUnzip file " + s + ". ");

				// Create parent directories if not exists.
				File foutp = fout.getParentFile();
				if (foutp != null) {
					foutp.mkdirs();
				}

				// Check if file exists.
				if (fout.exists()) {
					fout.delete();
				}

				out = new FileOutputStream(fout);

				int bytesTotal = 0, bytesRead = 0;
				while ((bytesRead = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);

					bytesTotal += bytesRead;
				}

				ODFParser.verbose("Written bytes " + bytesTotal);
				out.flush();
				out.close();
			}
		}
		in.close();
		out.close();
	}

	public List<String> queryTemplateFields() throws Exception {
		File fp = this.get(ODFParser.FILE_TO_PARSE);
		if ((fp == null) || !fp.exists()) {
			ODFParser.log("ODFParser -> File to query not found. " + fp.getCanonicalPath());
			return null;
		}

		Document d = ODFParser.getDocument(fp);
		Element e = d.getDocumentElement();

		// Gets the input-fields of the document.
		NodeList nInput = e.getElementsByTagName(ODFParser.XML_TAG_INPUT);
		if ((nInput != null) && (nInput.getLength() > 0)) {
			List<String> fieldNames = new ArrayList<>();
			for (int i = 0; i < nInput.getLength(); i++) {
				Node currentField = nInput.item(i);
				if ((currentField.getAttributes() != null) && (currentField.getAttributes().getNamedItem(ODFParser.XML_TAG_INPUT_DESCRIP) != null)) {
					fieldNames.add(currentField.getAttributes().getNamedItem(ODFParser.XML_TAG_INPUT_DESCRIP).getNodeValue());
				}
			}
			return fieldNames;
		}
		return null;
	}

	public static Map<Object, Object> translateTableDotFields(Map<Object, Object> valuesTable) {
		valuesTable = ODFParser.translateDotFields(valuesTable);
		Iterator<?> it = valuesTable.keySet().iterator();
		List<Object> vlist = new ArrayList<>();
		while (it.hasNext()) {
			vlist.add(it.next());
		}

		for (int i = 0; i < vlist.size(); i++) {
			Object o = valuesTable.get(vlist.get(i));
			if ((o != null) && (o instanceof Map)) {
				Map<Object, Object> newo = ODFParser.translateTableDotFields((Map<Object, Object>) o);
				if ((newo != null) && !newo.equals(o)) {
					valuesTable.put(vlist.get(i), newo);
				}
			}
		}
		return valuesTable;
	}

}
