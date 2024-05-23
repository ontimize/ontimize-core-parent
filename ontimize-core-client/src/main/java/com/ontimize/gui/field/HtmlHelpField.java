package com.ontimize.gui.field;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BrowserControl;
import com.ontimize.util.ParseUtils;

/**
 * <p> HTML component with hyperlinks to invoke some different actions in the form elements. </p> <br> Shown text is the component <code>template</code> parameter value. This value
 * can be one of the following three possibilities: <ul> <li>Template = The HTML text to show.</li> <li>Template = HTML file path with the text to show.</li> <li>Template = Value
 * existing in the application bundle file with the translation. Translation value can be one of the previous options (text or path)</li> </ul> The hyperlinks in the html text are
 * actions to execute in the form elements. <br> This actions can be configured using the element <code>attribute</code> and the method name.<br> For example:<br>
 *
 * <pre>
 * &lt;html&gt;
 *    &lt;body&gt;
 *      &lt;a href=&quot;ETableEntityName.openInsertDetailForm()&quot;&gt;&lt;img src=&quot;com/ontimize/gui/images/detail.png&quot;&gt;Show the insertion form from table ETableEntityName&lt;/a&gt;
 *      &lt;a href=&quot;EPacketsItems.openDetailForm(getSelectedRow())&quot;&gt;Open the detail form with the selected data&lt;/a&gt;
 *      &lt;a href=&quot;query.doClick()&quot;&gt;Do click in the form query button&lt;/a&gt;
 *    &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 *
 * <br> See parameters: {@link HtmlHelpField#init(Map)} <br> <br>
 *
 * @author Imatia Innovation SL
 *
 */
public class HtmlHelpField extends IdentifiedAbstractFormComponent implements HyperlinkListener {

	private static final Logger logger = LoggerFactory.getLogger(HtmlHelpField.class);

	protected CustomEditorPane htmlViewer = new CustomEditorPane();

	protected boolean horizontalScroll;

	protected boolean verticalScroll;

	protected boolean scrollable;

	/**
	 * Variables useful to show the same template with different values in it
	 */
	// Original template
	protected String originalTemplate;

	// Current template (this can be locale dependent)
	protected String currentTemplate;

	// Current values shown in the template
	protected Map<Object, Object>	currentData;

	protected String currentSeparator;

	public HtmlHelpField(Map<Object, Object> parameters) throws Exception {
		super();
		this.init(parameters);
		this.setLayout(new BorderLayout());
		if (this.scrollable) {
			JScrollPane sp = new JScrollPane(this.htmlViewer);
			sp.setBorder(BorderFactory.createEmptyBorder());
			sp.setOpaque(this.isOpaque());
			sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			sp.setOpaque(false);
			sp.getViewport().setOpaque(false);
			this.add(sp);
		} else {
			this.add(this.htmlViewer);
		}
		this.htmlViewer.setBorder(BorderFactory.createEmptyBorder());
		this.htmlViewer.setEditable(false);
		this.htmlViewer.setOpaque(this.isOpaque());
		((HTMLDocument) this.htmlViewer.getDocument()).setBase(this.getClass().getClassLoader().getResource(""));
		this.htmlViewer.addHyperlinkListener(this);
	}

	@Override
	public Dimension getPreferredSize() {
		return super.getPreferredSize();
	}

	/**
	 * Initialize the component with the following parameters:<br>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS * FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>attr</td> <td><i></td> <td></td> <td>yes</td> <td>Attribute for field.</td> </tr> <tr>
	 *            <td>template</td> <td><i></td> <td></td> <td>yes</td> <td>Complete path to html template.</td> </tr> <tr> <td>scroll</td> <td><i></td> <td></td> <td>no</td>
	 *            <td>Specifies whether the component must use an scroll or not.</td> </tr> <tr> <td>scrollh</td> <td><i></td> <td></td> <td>no</td> <td>Adjust the component to use
	 *            horizontal scroll.</td> </tr> <tr> <td>scrollv</td> <td><i></td> <td></td> <td>no</td> <td>Adjust the component to use vertical scroll.</td> </tr> <tr>
	 *            <td>paint</td> <td><i></td> <td></td> <td>no</td> <td>Fixes specified paint for viewer.</td> </tr> <tr> <td>maxwidth</td> <td><i></td> <td></td> <td>no</td>
	 *            <td>Maximum width for component.</td> </tr> <tr> <td>opaque</td> <td><i></td> <td>yes</td> <td>no</td> <td>The opacity condition for component. By default, it is
	 *            opaque.</td> </tr> </table>
	 */
	@Override
	public void init(Map<Object, Object> parameters) {
		this.attribute = parameters.get("attr");
		if (this.attribute == null) {
			throw new IllegalArgumentException(this.getClass().toString() + " -> 'attr' is required");
		}

		this.originalTemplate = ParseUtils.getString((String) parameters.get("template"), "");
		this.scrollable = ParseUtils.getBoolean((String) parameters.get("scroll"), false);
		this.horizontalScroll = ParseUtils.getBoolean((String) parameters.get("scrollh"), false);
		this.verticalScroll = ParseUtils.getBoolean((String) parameters.get("scrollv"), false);

		this.htmlViewer.setPaint(ParseUtils.getPaint((String) parameters.get("paint"), null));

		if (parameters.containsKey("maxwidth")) {
			int width = ParseUtils.getInteger((String) parameters.get("maxwidth"), 10);
			this.setSize(width, this.getSize().height);
			this.setPreferredSize(new Dimension(width, this.getPreferredSize().height));
			this.setMaximumSize(new Dimension(width, this.getMaximumSize().height));
		}
		this.setOpaque(ParseUtils.getBoolean((String) parameters.get("opaque"), true));
	}

	@Override
	public void setResourceBundle(ResourceBundle resourceBundle) {
		super.setResourceBundle(resourceBundle);
		if (this.originalTemplate != null) {
			this.currentTemplate = this.getTranslateTemplate(resourceBundle);
			if (this.currentData != null) {
				this.setData(this.currentData, this.currentSeparator);
			} else {
				this.setText(this.currentTemplate);
			}
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		if (this.originalTemplate != null) {
			return new ArrayList<>(Arrays.asList(new String[] { this.originalTemplate }));
		} else {
			return null;
		}
	}

	protected String getTranslateTemplate(ResourceBundle resourceBundle) {
		if (this.originalTemplate != null) {
			String current = null;
			try {
				current = resourceBundle.getString(this.originalTemplate);
				return current;
			} catch (Exception e) {
				HtmlHelpField.logger.trace(null, e);
				// Try to create a template using the locale prefix
				if (this.originalTemplate.endsWith(".html") || this.originalTemplate.endsWith(".htm")) {
					Locale locale = resourceBundle.getLocale();
					String variant = (locale.getVariant() != null) && !"".equals(locale.getVariant())
							? "_" + locale.getVariant() : "";
							String testPath = this.originalTemplate.substring(0, this.originalTemplate.lastIndexOf(".")) + "_"
									+ locale.getLanguage() + "_" + locale
									.getCountry()
									+ variant + this.originalTemplate.substring(this.originalTemplate.lastIndexOf("."));
							java.net.URL url = this.getClass().getClassLoader().getResource(testPath);
							if (url != null) {
								return testPath;
							}
				}

			}
			return this.originalTemplate;
		}
		return null;
	}

	public void setText(String text_path) {
		String templateText = this.getTemplateText(text_path);
		if (templateText == null) {
			templateText = "";
		}
		this.htmlViewer.setText(templateText);
		this.htmlViewer.setCaretPosition(0);
	}

	public String getCurrentTemplate() {
		return this.currentTemplate == null ? this.originalTemplate : this.currentTemplate;
	}

	protected String getTemplateText(String templatePath) {
		String templateText = null;
		if ("".equals(templatePath)) {
			templateText = "<html><body></body></html>";
		} else {
			java.net.URL url = this.getClass().getClassLoader().getResource(templatePath);
			if (url == null) {
				templateText = templatePath;
			} else {
				try {
					InputStream in = url.openStream();
					InputStreamReader reader = new InputStreamReader(in);
					BufferedReader br = new BufferedReader(reader);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					templateText = sb.toString();
				} catch (Exception e) {
					HtmlHelpField.logger.error(null, e);
				}
			}
		}
		return templateText;
	}

	public List<Object> getDataFieldNames(String template, String separator) {
		String templateText = this.getTemplateText(template);
		if ((templateText != null) && (templateText.length() > 0)) {
			List<Object> result = new ArrayList<>();
			int lastIndex = -1;
			boolean first = true;
			while ((lastIndex >= 0) || first) {
				first = false;
				int firstIndex = templateText.indexOf(separator, lastIndex);
				if (firstIndex >= 0) {
					int secondIndex = templateText.indexOf(separator, firstIndex + 1);
					if (secondIndex > firstIndex) {
						result.add(templateText.substring(firstIndex + separator.length(), secondIndex));
						lastIndex = secondIndex + 1;
					} else {
						lastIndex = -1;
					}
				} else {
					lastIndex = -1;
				}
			}
			return result;
		}

		return null;
	}

	public void showHtml(String templatePath, Map<Object, Object> dictionary) {
		String html = this.getTemplateText(templatePath);
		// Replace texts
		if (dictionary != null) {
			for (Entry<Object, Object> entry : dictionary.entrySet()) {
				String sChange = (String) entry.getKey();
				html = ApplicationManager.replaceText(html, sChange, dictionary.get(sChange).toString());
			}
		}
		this.htmlViewer.setText(html);
		this.htmlViewer.setCaretPosition(0);
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(-1, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0);
		} else {
			return null;
		}
	}

	public void setData(Map<Object, Object> data, String separator) {
		this.currentData = data;
		this.currentSeparator = separator;
		String sTemplateCurrent = this.getCurrentTemplate();
		if (sTemplateCurrent != null) {
			Map<Object, Object> dictionary = new HashMap<>();
			if (data != null) {
				for (Entry<Object, Object> entry : data.entrySet()) {
					dictionary.put(separator + entry.getKey() + separator, entry.getValue());
				}
			}
			this.showHtml(sTemplateCurrent, dictionary);
		}
	}

	public void clean() {
		this.htmlViewer.setText("");
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent evt) {
		String href = evt.getDescription();
		int dotIndex = href.indexOf(".");
		if (dotIndex == -1) {
			HtmlHelpField.logger.debug("HREF format unknow");
			return;
		}

		if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
		} else if (evt
				.getEventType() == HyperlinkEvent.EventType.EXITED) {
		} else if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			String attr = href.substring(0, dotIndex);
			String method = href.substring(dotIndex + 1);
			Object ob = this.parentForm.getButton(attr);
			if (ob == null) {
				ob = this.parentForm.getDataFieldReference(attr);
			}
			if (ob == null) {
				ob = this.parentForm.getElementReference(attr);
			}
			if (ob == null) {
				this.showNewPage(href);
			} else {
				try {
					this.doAction(ob, method);
				} catch (Exception e) {
					HtmlHelpField.logger.error(null, e);
				}
			}
		}
	}

	private void showNewPage(String href) {
		if (href.indexOf("mailto:") != -1) {
			BrowserControl.displayURL(href);
		} else if (href.startsWith("http:") || href.startsWith("www.")) {
			BrowserControl.displayURL(href);
		} else {
			String text = ApplicationManager.getTranslation(href, this.parentForm.getResourceBundle());
			this.setText(text == null ? href : text);
		}
	}

	protected void doAction(Object ob, String action) throws Exception {
		this.getParameterInfo(ob, action);
	}

	public void setPaint(Paint paint) {
		this.htmlViewer.setOpaque(true);
		this.htmlViewer.setPaint(paint);
	}

	public JEditorPane getViewer() {
		return this.htmlViewer;
	}

	protected ParameterInfo getParameterInfo(Object ob, String param) throws Exception {
		if (param.indexOf("(") != -1) {
			int first = param.indexOf("(");
			int last = param.lastIndexOf(")");
			String methodName = param.substring(0, first);
			String params[] = this.getParameters(param.substring(first + 1, last));
			ParameterInfo[] parameterInfo = new ParameterInfo[params.length];
			for (int i = 0; i < params.length; i++) {
				parameterInfo[i] = this.getParameterInfo(ob, params[i]);
			}
			Method method = this.getMethod(ob, methodName, parameterInfo);
			method.setAccessible(true);
			Object[] paramsValues = new Object[parameterInfo.length];
			for (int i = 0; i < parameterInfo.length; i++) {
				paramsValues[i] = parameterInfo[i].getTheValue();
			}
			return new ParameterInfo(method.getReturnType(), method.invoke(ob, paramsValues));
		} else {
			if ("true".equalsIgnoreCase(param)) {
				return new ParameterInfo(Boolean.TYPE, Boolean.TRUE);
			}
			if ("false".equalsIgnoreCase(param)) {
				return new ParameterInfo(Boolean.TYPE, Boolean.FALSE);
			}
			try {
				return new ParameterInfo(Integer.TYPE, new Integer(Integer.parseInt(param)));
			} catch (Exception e) {
				HtmlHelpField.logger.trace(null, e);
			}
			try {
				return new ParameterInfo(Double.TYPE, new Double(Double.parseDouble(param)));
			} catch (Exception e) {
				HtmlHelpField.logger.trace(null, e);
			}
			return new ParameterInfo(String.class, param);
		}
	}

	protected Method getMethod(Object ob, String methodName, ParameterInfo[] parameters) throws Exception {
		Class<?>[] paramsClass = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			paramsClass[i] = parameters[i].getTheClass();
		}
		try {
			return ob.getClass().getMethod(methodName, paramsClass);
		} catch (Exception e) {
			HtmlHelpField.logger.trace(null, e);
			// search no public methods
			return this.getNonPublicMethod(ob.getClass(), methodName, paramsClass);
		}
	}

	protected Method getNonPublicMethod(Class<?> c, String methodName, Class<?>[] paramsClasss) throws Exception {
		try {
			return c.getDeclaredMethod(methodName, paramsClasss);
		} catch (Exception exc) {
			HtmlHelpField.logger.trace(null, exc);
			if (c.getSuperclass() != null) {
				return this.getNonPublicMethod(c.getSuperclass(), methodName, paramsClasss);
			} else {
				throw exc;
			}
		}
	}

	protected String[] getParameters(String paramString) {
		StringTokenizer st = new StringTokenizer(paramString, ",");
		int tokens = st.countTokens();
		String[] response = new String[tokens];
		for (int i = 0; i < tokens; i++) {
			response[i] = st.nextToken();
		}
		return response;
	}

	protected static class ParameterInfo {

		protected Class<?>	theClass;

		protected Object theValue;

		public ParameterInfo(Class<?> c, Object o) {
			this.theClass = c;
			this.theValue = o;
		}

		public Class<?> getTheClass() {
			return this.theClass;
		}

		public Object getTheValue() {
			return this.theValue;
		}

	}

}
