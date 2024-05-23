package com.ontimize.xml;

import java.awt.Color;
import java.awt.Paint;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BorderManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.util.ParseUtils;

public abstract class DefaultXMLParametersManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultXMLParametersManager.class);

	public static class ParameterValue {

		protected String	parameter	= null;

		protected String	value		= null;

		public ParameterValue(String parameter, String value) {
			this.parameter = parameter;
			this.value = value;
		}

		public String getParameter() {
			return this.parameter;
		}

		public String getValue() {
			return this.value;
		}

	}

	protected static Map<Object, Object> parameters = new HashMap<>();

	public static void add(String className, String parameter, String value) {
		if (DefaultXMLParametersManager.parameters.containsKey(className)) {
			List<Object> list = (List<Object>) DefaultXMLParametersManager.parameters.get(className);
			list.add(new ParameterValue(parameter, value));
		} else {
			List<Object> list = new ArrayList<>();
			list.add(new ParameterValue(parameter, value));
			DefaultXMLParametersManager.parameters.put(className, list);
		}
	}

	public static void setXMLDefaultParameterFile(String filePath) {
		List<String> fileNames = ApplicationManager.getTokensAt(filePath, ";");

		for (String fileName : fileNames) {

			try {
				URL url = DefaultXMLParametersManager.class.getClassLoader().getResource(fileName);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder;
				dBuilder = factory.newDocumentBuilder();
				Document doc = dBuilder.parse(url.openStream());

				NodeList nodeList = null;

				// Parse the SystemProperties node to set the system properties
				// This must be the first node to read because the properties
				// must
				// be set before create any data field
				nodeList = doc.getElementsByTagName("SystemProperties");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseSystemProperties(rootNode);
				}

				// Parse the Colors node to register the color in the
				// ColorConstants
				// using the method ColorConstants.addUserColor(colorName,
				// color)
				nodeList = doc.getElementsByTagName("Colors");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseColors(rootNode);
				}

				// Parse the Paints node to register the paints in the
				// ColorConstants using the method
				// ColorConstants.addUserPaint(paintName, (Paint)
				// paintObject);
				nodeList = doc.getElementsByTagName("Paints");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parsePaints(rootNode);
				}

				// Parse the Borders node to register the borders in the
				// BorderManager
				// using BorderManager.putBorder(borderName, (Border)
				// borderObject);
				nodeList = doc.getElementsByTagName("Borders");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseBorders(rootNode);
				}

				// Parse the StaticValues node to set the static values and call
				// the
				// static set methods with the specified values
				nodeList = doc.getElementsByTagName("StaticValues");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseStaticValues(rootNode);
				}

				// Parse the UIManager parameters
				nodeList = doc.getElementsByTagName("UIManagerParameters");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseUIManagerParameters(rootNode);
				}

				// Parse the XMLConfigurationParametes node to set the default
				// parameters to use in the components construction
				nodeList = doc.getElementsByTagName("XMLConfigurationParameters");
				if ((nodeList != null) && (nodeList.getLength() == 1)) {
					Node rootNode = nodeList.item(0);
					DefaultXMLParametersManager.parseXMLParameters(rootNode);
				}

			} catch (Exception exc) {
				DefaultXMLParametersManager.logger.error("{}", exc.getMessage(), exc);
			}
		}
	}

	protected static void parseColors(Node rootNode) {
		// <Colors>
		// <Color name="HeaderColor" value="0;51;153" />
		// </Colors>
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("Color".equalsIgnoreCase(item.getNodeName())) {
					String colorName = item.getAttributes().getNamedItem("name").getNodeValue();
					String colorValue = item.getAttributes().getNamedItem("value").getNodeValue();
					Color c = ParseUtils.getColor(colorValue, null);
					if ((c != null) && (colorName != null)) {
						ColorConstants.addUserColor(colorName, c);
					}
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static void parsePaints(Node rootNode) {
		// <Paints>
		// <Paint name="linearGradientPaint"
		// class="java.awt.LinearGradientPaint">
		// <Parameter class="java.awt.geom.Point2D">
		// <Value class="java.awt.Point">
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// </Value>
		// </Parameter>
		// <Parameter class="java.awt.geom.Point2D">
		// <Value class="java.awt.Point">
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// <Parameter class="int">
		// <Value>26</Value>
		// </Parameter>
		// </Value>
		// </Parameter>
		// <Parameter class="[F">
		// <Value>0.0f;0.4999f;0.5f;1f</Value>
		// </Parameter>
		// <Parameter class="[Ljava.awt.Color;">
		// <Value>#f2f2f2;#ebebeb;#dddddd;#cfcfcf</Value>
		// </Parameter>
		// </Paint>
		// </Paints>
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("Paint".equalsIgnoreCase(item.getNodeName())) {
					String paintName = item.getAttributes().getNamedItem("name").getNodeValue();
					try {
						Object paintObject = DefaultXMLParametersManager.createObject(item);
						ColorConstants.addUserPaint(paintName, (Paint) paintObject);
					} catch (Exception ex) {
						DefaultXMLParametersManager.logger.error("Error creating {} paint: {}", paintName, ex.getMessage(), ex);
					}
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static void parseUIManagerParameters(Node rootNode) {
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("UIManagerParameter".equalsIgnoreCase(item.getNodeName())) {
					String keyName = item.getAttributes().getNamedItem("name").getNodeValue();
					Object object = DefaultXMLParametersManager.createObject(item);
					UIManager.put(keyName, object);
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static void parseBorders(Node rootNode) {
		// <Borders>
		// <Border name="empty" class="javax.swing.border.EmptyBorder">
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// <Parameter class="int">
		// <Value>0</Value>
		// </Parameter>
		// </Border>
		// </Borders>
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("Border".equalsIgnoreCase(item.getNodeName())) {
					String borderName = item.getAttributes().getNamedItem("name").getNodeValue();
					Object borderObject = DefaultXMLParametersManager.createObject(item);
					BorderManager.putBorder(borderName, (Border) borderObject);
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static Object createObject(Node rootNode) throws Exception {
		String objectClassName = rootNode.getAttributes().getNamedItem("class").getNodeValue();
		NodeList childParameters = rootNode.getChildNodes();
		List<Object> parameterTypes = new ArrayList<>();
		List<Object> parameterValues = new ArrayList<>();

		// Read all the parameters to know the classes and the values
		// The classes are needed to know the constructor to use
		for (int k = 0; k < childParameters.getLength(); k++) {
			if ("Parameter".equalsIgnoreCase(childParameters.item(k).getNodeName())) {
				String parameterClassName = childParameters.item(k).getAttributes().getNamedItem("class").getNodeValue();
				Class<?> parameterClass = DefaultXMLParametersManager.getClassFromString(parameterClassName);
				Object parameterValue = null;
				NodeList childNodes = childParameters.item(k).getChildNodes();
				for (int m = 0; m < childNodes.getLength(); m++) {
					if ("Value".equalsIgnoreCase(childNodes.item(m).getNodeName())) {
						NodeList valueChildNodes = childNodes.item(m).getChildNodes();
						if ((valueChildNodes.getLength() == 1) && (valueChildNodes.item(0).getNodeType() == Node.TEXT_NODE)) {
							parameterValue = DefaultXMLParametersManager.getObjectFromClass(valueChildNodes.item(0).getNodeValue(), parameterClass);
						} else {
							// When the parameter is a new class to create a new
							// instance with its own parameters
							parameterValue = DefaultXMLParametersManager.createObject(childNodes.item(m));
						}
					}
				}
				parameterTypes.add(parameterClass);
				parameterValues.add(parameterValue);
			}
		}

		Class<?> objectClass = DefaultXMLParametersManager.getClassFromString(objectClassName);
		Class<?>[] types = new Class[parameterTypes.size()];
		Object[] values = new Object[parameterValues.size()];
		for (int i = 0; i < parameterTypes.size(); i++) {
			types[i] = (Class<?>) parameterTypes.get(i);
			values[i] = parameterValues.get(i);
		}

		Constructor<?> constructor = objectClass.getConstructor(types);
		Object newInstance = constructor.newInstance(values);
		return newInstance;
	}

	/**
	 * Get the Class for the specified String parameter. Is not exactly the same as Class.forName(String) because this method compare first the className with the primitive types
	 *
	 * @param className
	 *            Name of the class, for example: <br> (int, float, double ..., java.awt.Color, [I, [F, [Ljava.awt.Color;
	 * @return
	 * @throws Exception
	 */
	protected static Class<?> getClassFromString(String className) throws Exception {
		if (className.equalsIgnoreCase("int")) {
			return int.class;
		} else if (className.equalsIgnoreCase("float")) {
			return float.class;
		} else if (className.equalsIgnoreCase("double")) {
			return double.class;
		} else if (className.equalsIgnoreCase("boolean")) {
			return boolean.class;
		} else if (className.equalsIgnoreCase("byte")) {
			return byte.class;
		} else if (className.equalsIgnoreCase("long")) {
			return long.class;
		} else if (className.equalsIgnoreCase("short")) {
			return short.class;
		} else if (className.equalsIgnoreCase("char")) {
			return char.class;
		}

		if (className.equalsIgnoreCase("color")) {
			return Color.class;
		} else if (className.equalsIgnoreCase("icon")) {
			return ImageIcon.class;
		}
		// [F is float[]
		// [I is int[]
		// [Ljava.awt.Color; is Color[]
		return Class.forName(className);
	}

	protected static void parseStaticValues(Node rootNode) {
		// <StaticValues>
		// <Class
		// name="com.ontimize.gui.preferences.BasicApplicationPreferences">
		// <StaticValue variable="remoteUserPreferences" value="true" />
		// </Class>
		// <Class name="com.ontimize.gui.Form">
		// <StaticSetMethod method="setDefaultTitlePaint"
		// value="linearGradientPaint"
		// datatype="java.awt.Paint" />
		// </Class>
		// </StaticValues>
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("Class".equalsIgnoreCase(item.getNodeName())) {
					String className = item.getAttributes().getNamedItem("name").getNodeValue();
					NodeList classParamtersList = item.getChildNodes();
					for (int k = 0; k < classParamtersList.getLength(); k++) {
						Node parameterItem = classParamtersList.item(k);
						if ("StaticValue".equalsIgnoreCase(parameterItem.getNodeName())) {
							String variableName = parameterItem.getAttributes().getNamedItem("variable").getNodeValue();
							String sValue = parameterItem.getAttributes().getNamedItem("value").getNodeValue();
							DefaultXMLParametersManager.setStaticValue(className, variableName, sValue);
						} else if ("StaticSetMethod".equalsIgnoreCase(parameterItem.getNodeName())) {
							String methodName = parameterItem.getAttributes().getNamedItem("method").getNodeValue();
							String sValue = parameterItem.getAttributes().getNamedItem("value").getNodeValue();
							String dataType = null;
							if (parameterItem.getAttributes().getNamedItem("datatype") != null) {
								dataType = parameterItem.getAttributes().getNamedItem("datatype").getNodeValue();
							}
							DefaultXMLParametersManager.executeStaticSetMethod(className, methodName, sValue, dataType);
						}
					}
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static void parseSystemProperties(Node rootNode) {
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("SystemProperty".equalsIgnoreCase(item.getNodeName())) {
					String propertyName = item.getAttributes().getNamedItem("name").getNodeValue();
					String propertyValue = item.getAttributes().getNamedItem("value").getNodeValue();
					System.setProperty(propertyName, propertyValue);
				}
			} catch (Exception e) {
				DefaultXMLParametersManager.logger.error("{}", e.getMessage(), e);
			}
		}
	}

	protected static void executeStaticSetMethod(String className, String methodName, String value, String dataType) throws Exception {
		Class<?> classObject = DefaultXMLParametersManager.getClassFromString(className);

		List<String> listValueName = ApplicationManager.getTokensAt(value, ";");
		List<String> listDataType = ApplicationManager.getTokensAt(dataType, ";");
		Class<?>[] arrayReflectionClasses = null;
		Object[] arrayReflectionValues = null;
		if ((listDataType != null) && (listValueName != null) && (listDataType.size() == listValueName.size())) {
			arrayReflectionClasses = new Class[listValueName.size()];
			arrayReflectionValues = new Object[listValueName.size()];
			for (int i = 0; i < listDataType.size(); i++) {
				Class<?> classI = DefaultXMLParametersManager.getClassFromString(listDataType.get(i));
				Object objectI = DefaultXMLParametersManager.getObjectFromClass(listValueName.get(i), classI);
				arrayReflectionValues[i] = objectI;
				arrayReflectionClasses[i] = classI;
			}
			Method method = classObject.getMethod(methodName, arrayReflectionClasses);
			if (method != null) {
				method.invoke(classObject, arrayReflectionValues);
			}
		}
	}

	protected static void setStaticValue(String className, String variableName, String value) throws Exception {
		Class<?> classObject = DefaultXMLParametersManager.getClassFromString(className);
		Field field = classObject.getField(variableName);
		if (field != null) {
			Object oValue = DefaultXMLParametersManager.getObjectFromClass(value, field.getType());
			field.set(classObject, oValue);
		}
	}

	protected static Object getObjectFromClass(String value, Class<?> classObject) {
		String dataType = classObject.getName();
		if (value != null) {
			if (classObject.isArray()) {
				List<String> v = ApplicationManager.getTokensAt(value, ";");
				Object newInstance = Array.newInstance(classObject.getComponentType(), v.size());
				for (int i = 0; i < v.size(); i++) {
					Array.set(newInstance, i, DefaultXMLParametersManager.getObjectFromClass(v.get(i), classObject.getComponentType()));
				}
				return newInstance;
			} else {
				return DefaultXMLParametersManager.getObjectFromString(value, dataType);
			}
		}

		return value;
	}

	/**
	 * Get an object of types: float, double, boolean, int, byte, long, short or char. For example Float.valueOf(value)<br> If dataType is color, paint or image then use
	 * {@link ParseUtils} class to get the object
	 *
	 * @param value
	 * @param dataType
	 * @return
	 */
	protected static Object getObjectFromString(String value, String dataType) {
		if (value != null) {
			dataType = dataType.substring(dataType.lastIndexOf(".") + 1);
			if ((value != null) && (dataType != null)) {
				if (dataType.toLowerCase().indexOf("float") >= 0) {
					return Float.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("double") >= 0) {
					return Double.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("boolean") >= 0) {
					return Boolean.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("color") >= 0) {
					return ParseUtils.getColor(value, null);
				} else if (dataType.toLowerCase().indexOf("paint") >= 0) {
					return ParseUtils.getPaint(value, null);
				} else if (dataType.toLowerCase().indexOf("image") >= 0) {
					return ParseUtils.getImage(value, null);
				} else if (dataType.toLowerCase().indexOf("font") >= 0) {
					return ParseUtils.getFont(value, null);
				} else if (dataType.toLowerCase().indexOf("byte") >= 0) {
					return Byte.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("long") >= 0) {
					return Long.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("short") >= 0) {
					return Short.valueOf(value);
				} else if (dataType.toLowerCase().indexOf("char") >= 0) {
					return value;
				} else if (dataType.toLowerCase().indexOf("int") >= 0) {
					return Integer.valueOf(value);
				}
			}
		}
		return value;
	}

	protected static void parseXMLParameters(Node rootNode) {
		NodeList childNodes = rootNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			try {
				Node item = childNodes.item(i);
				if ("Class".equalsIgnoreCase(item.getNodeName())) {
					String className = item.getAttributes().getNamedItem("name").getNodeValue();
					List<String> classNames = ApplicationManager.getTokensAt(className, ";");

					NodeList classParamtersList = item.getChildNodes();
					for (int k = 0; k < classParamtersList.getLength(); k++) {
						Node parameterItem = classParamtersList.item(k);
						if ("Parameter".equalsIgnoreCase(parameterItem.getNodeName())) {
							String parameterName = parameterItem.getAttributes().getNamedItem("key").getNodeValue();
							String parameterValue = parameterItem.getAttributes().getNamedItem("value").getNodeValue();
							for (String className2 : classNames) {
								DefaultXMLParametersManager.add(className2, parameterName, parameterValue);
							}
						}
					}
				}
			} catch (Exception exc) {
				DefaultXMLParametersManager.logger.error("{}", exc.getMessage(), exc);
			}
		}
	}

	public static void clearXMLDefaultParameters() {
		DefaultXMLParametersManager.parameters.clear();
	}

	/**
	 * Get a list with all the default xml configuration parameters for the class specified
	 *
	 * @param className
	 * @return
	 */
	public static ParameterValue[] get(String className) {
		if (DefaultXMLParametersManager.parameters.containsKey(className)) {
			return ((List<?>) DefaultXMLParametersManager.parameters.get(className)).toArray(new ParameterValue[] {});
		} else {
			return null;
		}
	}

	public static Map<Object, Object> getParameters(String className) {
		Map<Object, Object> hParams = new HashMap<>();
		ParameterValue[] parameterValues = DefaultXMLParametersManager.get(className);
		if (parameterValues != null) {
			for (ParameterValue parameterValue : parameterValues) {
				hParams.put(parameterValue.getParameter(), parameterValue.getValue());
			}
		}
		return hParams;
	}

	public static ParameterValue[] getStartsWith(String tag) {
		// keys
		List<Object> list = new ArrayList<>();
		for (Entry<Object, Object> entry : DefaultXMLParametersManager.parameters.entrySet()) {
			String key = entry.getKey().toString();
			if (tag.startsWith(key) && !tag.equals(key)) {
				list.addAll(Arrays.asList(DefaultXMLParametersManager.get(key)));
			}
		}
		ParameterValue[] params = DefaultXMLParametersManager.get(tag);
		if (params != null) {
			list.addAll(Arrays.asList(DefaultXMLParametersManager.get(tag)));
		}
		return list.toArray(new ParameterValue[] {});
	}

}
