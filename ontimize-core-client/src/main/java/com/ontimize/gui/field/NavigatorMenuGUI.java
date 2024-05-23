package com.ontimize.gui.field;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.BorderManager;
import com.ontimize.gui.ColorConstants;
import com.ontimize.gui.DefaultActionMenuListener;
import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.preferences.HasPreferenceComponent;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.jee.common.security.MenuPermission;
import com.ontimize.security.ClientSecurityManager;
import com.ontimize.util.swing.selectablelist.SelectableItem;
import com.ontimize.util.swing.selectablelist.SelectableItemListCellRenderer;
import com.ontimize.util.swing.selectablelist.SelectableItemMouseListener;

public class NavigatorMenuGUI extends NavigationMenu implements IdentifiedElement, AccessForm, FormComponent, HasPreferenceComponent, Freeable {

	private static final Logger			logger				= LoggerFactory.getLogger(NavigatorMenuGUI.class);

	public static final String			SRC					= "src";

	protected Object					attribute			= null;

	protected FormPermission			visiblePermission	= null;

	protected FormPermission			enabledPermision	= null;

	protected Form						parentForm			= null;

	protected boolean					restricted			= false;

	public static final String			NAVIGATOR_MENU		= "navigator_menu";

	protected JPopupMenu				popupmenu;

	protected ResourceBundle			bundle;

	public static final String			RESTORE_DEFAULTS	= "navigatormenu.restore_defaults";

	public static final String			VISIBLE_ITEMS		= "navigatormenu.visible_items";

	protected SetupDialog				dialog				= null;

	protected ApplicationPreferences	aPreferences		= null;

	protected String					userPrefs			= null;

	public static BasicStroke			stroke				= new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 5f, new float[] { 0.5f, 5.0f }, 2.5f);

	/**
	 * Method that configures the component.
	 *
	 * @param parameters
	 *            Map with the parameters.
	 * @throws Exception
	 */
	public NavigatorMenuGUI(Map<Object, Object> parameters) throws Exception {
		this.init(parameters);
		this.installMouseHandler();
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		} else {
			return null;
		}
	}

	/**
	 * This method gets the <code>Map</code> and initializes the component <p>
	 *
	 * @param arguments
	 *            the <code>Map</code> with parameters <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS * FRAME=BOX> <tr> <td><b>attribute</td> <td><b>values</td>
	 *            <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>attr</td> <td></td> <td></td> <td>yes</td> <td>Indicates the component attribute.</td>
	 *            </tr> <tr> <td>src</td> <td></td> <td></td> <td>yes</td> <td>Resource path to locate the configuration XML.</td> </tr> </table>
	 */
	@Override
	public void init(Map<Object, Object> arguments) throws Exception {
		if (arguments.containsKey(DataField.ATTR)) {
			this.attribute = arguments.get("attr");
		} else {
			throw new IllegalArgumentException(DataField.ATTR + " parameter is mandatory");
		}

		if (arguments.containsKey(NavigatorMenuGUI.SRC)) {
			URL urlXML = this.getClass().getClassLoader().getResource(arguments.get(NavigatorMenuGUI.SRC).toString());
			if (urlXML != null) {
				this.parse(urlXML);
			} else {
				throw new IllegalArgumentException(NavigatorMenuGUI.SRC + " parameter is mandatory");
			}
		}
	}

	protected void installMouseHandler() {
		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					NavigatorMenuGUI.this.showPopupMenu(e);
				}
			}
		});
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public void initPermissions() {
		if (ApplicationManager.getClientSecurityManager() != null) {
			Component[] cs = new Component[1];
			cs[0] = this;
			ClientSecurityManager.registerSecuredElement(this, cs);

		}
		boolean pVisible = this.checkVisiblePermission();
		if (!pVisible) {
			this.setVisible(false);
		}

		boolean pEnabled = this.checkEnabledPermission();
		if (!pEnabled) {
			this.setEnabled(false);
		}

	}

	protected boolean checkVisiblePermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.visiblePermission == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.visiblePermission = new FormPermission(this.parentForm.getArchiveName(), "visible", this.attribute.toString(), true);
				}
			}
			try {
				// Check to show
				if (this.visiblePermission != null) {
					manager.checkPermission(this.visiblePermission);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					NavigatorMenuGUI.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					NavigatorMenuGUI.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	protected boolean checkEnabledPermission() {
		ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
		if (manager != null) {
			if (this.enabledPermision == null) {
				if ((this.attribute != null) && (this.parentForm != null)) {
					this.enabledPermision = new FormPermission(this.parentForm.getArchiveName(), "enabled", this.attribute.toString(), true);
				}
			}
			try {
				// Check to show
				if (this.enabledPermision != null) {
					manager.checkPermission(this.enabledPermision);
				}
				this.restricted = false;
				return true;
			} catch (Exception e) {
				this.restricted = true;
				if (e instanceof NullPointerException) {
					NavigatorMenuGUI.logger.error(null, e);
				}
				if (ApplicationManager.DEBUG_SECURITY) {
					NavigatorMenuGUI.logger.debug(this.getClass().toString() + ": " + e.getMessage(), e);
				}
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public boolean isRestricted() {
		return this.restricted;
	}

	@Override
	public void setParentForm(Form form) {
		this.parentForm = form;
	}

	@Override
	public List<String> getTextsToTranslate() {
		return null;
	}

	@Override
	public void setComponentLocale(Locale l) {}

	/**
	 * This method analyzes a "MenuGroup" node of the XML Document configuration to obtain all the parameter to build a new MenuGroupGUI into the NavigationMenu.
	 *
	 * @param node
	 *            The "MenuGroup" node of the XML Document configuration.
	 * @return a <code>MenuGroup</code> object.
	 * @throws Exception
	 */
	@Override
	protected MenuGroup createMenuGroup(Node node) throws Exception {
		Map<Object, Object> parameters = new HashMap<>();
		String[] opts = null;
		ImageIcon[] icons = null;
		Object width = null;

		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node currentNode = attributes.item(i);

			if (currentNode == null) {
				throw new IllegalArgumentException(NavigationMenu.ERROR_MESSAGE);
			} else {
				if (NavigationMenu.OPTIONS.equalsIgnoreCase(currentNode.getNodeName())) {
					String options = currentNode.getNodeValue();
					List<Object> li = new ArrayList<>();
					StringTokenizer tokens = new StringTokenizer(options, ";");
					while (tokens.hasMoreTokens()) {
						li.add(tokens.nextToken());
					}
					opts = li.toArray(new String[li.size()]);
					parameters.put(NavigationMenu.OPTIONS, opts);
				} else if (NavigationMenu.ICONS.equalsIgnoreCase(currentNode.getNodeName())) {
					String sIcons = currentNode.getNodeValue();
					List<Object> li = new ArrayList<>();
					StringTokenizer tokens = new StringTokenizer(sIcons, ";");
					while (tokens.hasMoreTokens()) {
						if (this.imageBasePath != null) {
							String str = this.imageBasePath + tokens.nextToken();
							li.add(str);
						} else {
							li.add(tokens.nextToken());
						}
					}
					icons = new ImageIcon[li.size()];
					for (int m = 0; m < li.size(); m++) {
						try {
							icons[m] = ImageManager.getIcon((String) li.get(m));
						} catch (Exception e) {
							NavigatorMenuGUI.logger.error("Resource not found " + li.get(m), e);
						}
					}
					parameters.put(NavigationMenu.ICONS, icons);
				} else if (NavigationMenu.BGHEADER.equalsIgnoreCase(currentNode.getNodeName())) {
					Color bgHeader = Color.green;
					try {
						bgHeader = NavigationMenu.parseColor(currentNode.getNodeValue());
						parameters.put(NavigationMenu.BGHEADER, bgHeader);
					} catch (Exception e) {
						NavigatorMenuGUI.logger.error(null, e);
					}
				} else if (NavigationMenu.BGBODY.equalsIgnoreCase(currentNode.getNodeName())) {
					Color bgBody = Color.white;
					try {
						bgBody = NavigationMenu.parseColor(currentNode.getNodeValue());
						parameters.put(NavigationMenu.BGBODY, bgBody);
					} catch (Exception e) {
						NavigatorMenuGUI.logger.error(null, e);
					}
				} else if (NavigationMenu.FOREGROUND.equalsIgnoreCase(currentNode.getNodeName())) {
					Color fg = Color.black;
					try {
						fg = NavigationMenu.parseColor(currentNode.getNodeValue());
						parameters.put(NavigationMenu.FOREGROUND, fg);
					} catch (Exception e) {
						NavigatorMenuGUI.logger.error(null, e);
					}
				} else if (NavigationMenu.FOREGROUNDHEADER.equalsIgnoreCase(currentNode.getNodeName())) {
					Color fgHeader = Color.white;
					try {
						fgHeader = NavigationMenu.parseColor(currentNode.getNodeValue());
						parameters.put(NavigationMenu.FOREGROUNDHEADER, fgHeader);
					} catch (Exception e) {
						NavigatorMenuGUI.logger.error(null, e);
					}
				} else if (NavigationMenu.BORDERCOLOR.equalsIgnoreCase(currentNode.getNodeName())) {
					Color border = Color.white;
					try {
						border = NavigationMenu.parseColor(currentNode.getNodeValue());
						parameters.put(NavigationMenu.BORDERCOLOR, border);
					} catch (Exception e) {
						NavigatorMenuGUI.logger.error(null, e);
					}
				} else if (NavigationMenu.WIDTH.equalsIgnoreCase(currentNode.getNodeName())) {
					width = currentNode.getNodeValue();
					parameters.put(NavigationMenu.WIDTH, currentNode.getNodeValue());
				} else {
					parameters.put(currentNode.getNodeName(), currentNode.getNodeValue());
				}

			}

		}
		if (opts == null) {
			throw new IllegalArgumentException(NavigationMenu.OPTIONS + " parameter is mandatory!!!!");
		} else if (width == null) {
			throw new IllegalArgumentException(NavigationMenu.WIDTH + " parameter is mandatory!!!!");
		} else if ((opts != null) && (icons != null) && (opts.length != icons.length)) {
			throw new IllegalArgumentException(NavigationMenu.OPTIONS + " and " + NavigationMenu.ICONS + " parameters must have the same length");
		} else {
			return this.createMenuGroupInstance(parameters);
		}

	}

	/**
	 * This method instantiates a new MenuGroupGUI object with the specified parameters.
	 *
	 * @return a <code>MenuGroupGUI</code> object.
	 */
	protected MenuGroupGUI createMenuGroupInstance(Map<Object, Object> parameters) {

		Class<?> cMenuGroupGUI = null;
		Object ob = null;
		try {
			if (parameters.containsKey(NavigationMenu.MENUGROUPCLASS)) {
				cMenuGroupGUI = Class.forName(parameters.get(NavigationMenu.MENUGROUPCLASS).toString());
			} else {
				cMenuGroupGUI = Class.forName("com.ontimize.gui.field.NavigatorMenuGUI" + "$" + NavigationMenu.MENUGROUP + "GUI");
			}

			Constructor<?> constructors = cMenuGroupGUI.getConstructor(new Class[] { HashMap.class });
			ob = constructors.newInstance(new Object[] { parameters });
		} catch (Exception exc) {
			NavigatorMenuGUI.logger.error(null, exc);
		}
		return (MenuGroupGUI) ob;
	}

	/**
	 * * Constructs a new MenuGroupGUI with the specified parameters.
	 *
	 * @param header
	 *            String with the text to display into the Header of the MenuGroup.
	 * @param opts
	 *            String Array with the identifier of each MenuItem of the MenuGroup.
	 * @param icons
	 *            ImageIcon Array with the icons to each MenuItem of the MenuGroup.
	 * @param x
	 *            The coordinate x of the MenuGroup into the NavigationMenu.
	 * @param y
	 *            The coordinate y of the MenuGroup into the NavigationMenu.
	 * @param width
	 *            The width of the MenuGroup.
	 * @param height
	 *            The absolute height of the MenuGroup. The header height is included into this height.
	 * @param bgHeader
	 *            The color of the MenuGroup Header.
	 * @param bgBody
	 *            The color of the background MenuGroup.
	 * @param fg
	 *            The color of the MenuGroup font.
	 * @param border
	 *            The color of the border of the MenuGroup. Use {@link #createMenuGroupInstance(Map parameters)}
	 * @deprecated
	 */
	@Deprecated
	@Override
	protected MenuGroup createMenuGroupInstance(String header, String[] opts, ImageIcon[] icons, int x, int y, int width, int height, Color bgHeader, Color bgBody,
			Color fg,
			Color border) throws Exception {
		return new MenuGroupGUI(header, opts, icons, x, y, width, height, bgHeader, bgBody, fg, border);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if ((this.menuList != null) && (this.menuList.size() > 0)) {
			int xMax = 0;
			int yMax = 0;
			for (Object element : this.menuList) {
				MenuGroupGUI current = (MenuGroupGUI) element;
				if (xMax < (current.getX() + current.getWidth())) {
					xMax = current.getX() + current.getWidth();
				}
				if (yMax < (current.getY() + current.getHeight())) {
					yMax = current.getY() + current.getHeight();
				}
			}
			if (d.width < xMax) {
				d.width = xMax;
			}

			if (d.height < yMax) {
				d.height = yMax;
			}
		}
		return d;
	}

	@Override
	public void initPreferences(ApplicationPreferences ap, String user) {
		if (ap == null) {
			return;
		}
		this.userPrefs = user;
		this.aPreferences = ap;
		String preferenceValue = this.aPreferences.getPreference(user, this.getNavigationMenuPreferenceKey());
		this.setNavigationMenuPreferenceValue(preferenceValue);
	}

	/**
	 * This method returns a unequivocal key name to assign to this MenuNavigator that will be used into the Preferences.
	 *
	 * @return a <code>String</code> with the name.
	 */
	protected String getNavigationMenuPreferenceKey() {
		Form f = this.parentForm;
		return f != null ? f.getArchiveName() + "_" + NavigatorMenuGUI.NAVIGATOR_MENU : NavigatorMenuGUI.NAVIGATOR_MENU;
	}

	/**
	 * This method save the current preference values.
	 */
	protected void saveNavigationMenuPreference(boolean bSaveRemote) {
		if (this.aPreferences == null) {
			return;
		}
		this.aPreferences.setPreference(this.userPrefs, this.getNavigationMenuPreferenceKey(), this.getNavigationMenuPreferenceValue());
		if (bSaveRemote) {
			this.aPreferences.savePreferences();
		}
	}

	protected void saveNavigationMenuPreference() {
		this.saveNavigationMenuPreference(false);
	}

	/**
	 * This method obtains the current values of MenuGroups and builds a String with the preference values of these MenuGroups.
	 *
	 * <blockquote> The preference values saved are: <p> - position: The coordinates x and y of each MenuGroup. </p> <p> - items: The current state of visibility of each MenuItem
	 * of each MenuGroup. </p> * </blockquote>
	 *
	 * @return a <code>String</code> with the preference values.
	 */
	protected String getNavigationMenuPreferenceValue() {

		if ((this.menuList == null) || (this.menuList.size() == 0)) {
			return null;
		}
		StringBuilder buffer = new StringBuilder();

		// Position
		StringBuilder position = new StringBuilder();
		for (Object element : this.menuList) {
			MenuGroupGUI group = (MenuGroupGUI) element;
			position.append(group.getHeader()).append("=");
			position.append(group.getX()).append(":");
			position.append(group.getY()).append(";");
		}

		buffer.append("~position~");
		buffer.append(position.toString());
		buffer.append("|");

		// Menu Items.
		String str = this.getPreferencesStructure(this.menuList);
		buffer.append(str);
		return buffer.toString();
	}

	/**
	 * This method establishes the position (x,y) of the specified MenuGroup
	 *
	 * @param name
	 *            The name of the MenuGroup.
	 * @param x
	 *            The coordinate x of the MenuGroup.
	 * @param y
	 *            The coordinate y of the MenuGroup.
	 */
	protected void setMenuGroupValue(String name, int x, int y) {
		if ((this.menuList == null) || (this.menuList.size() == 0)) {
			return;
		}
		for (Object element : this.menuList) {
			MenuGroupGUI menu = (MenuGroupGUI) element;
			if (name.equals(menu.getHeader())) {
				menu.setXY(x, y);
				return;
			}
		}
	}

	/**
	 * This method establishes the visibility of the MenuItems of the specified MenuGroup.
	 *
	 * @param name
	 *            The name of the MenuGroup.
	 * @param manager
	 *            The identifier of the MenuItem of the MenuGroup
	 * @param visibility
	 *            Boolean with the visibility value.
	 */
	protected void setMenuGroupValue(String name, String manager, boolean visibility) {
		if ((this.menuList == null) || (this.menuList.size() == 0)) {
			return;
		}
		for (Object element : this.menuList) {
			MenuGroupGUI menu = (MenuGroupGUI) element;
			if (name.equals(menu.getHeader())) {
				menu.setVisibleItems(manager, visibility);
				return;
			}
		}
	}

	/**
	 * This method recovers the preference values from the specified String and sets them to the MenuGroup.
	 *
	 * @param value
	 *            Preference values String.
	 */
	protected void setNavigationMenuPreferenceValue(String value) {
		if ((value == null) || (value.length() == 0)) {
			return;
		}
		StringTokenizer token = new StringTokenizer(value, "|");
		while (token.hasMoreTokens()) {
			String current = token.nextToken();
			String param = current.substring(1, 6);
			if (param.equals("items")) {
				if (current.indexOf("~items~") >= -1) {
					current = current.replaceFirst("~items~", "");
					StringTokenizer tokenI = new StringTokenizer(current, ";");
					while (tokenI.hasMoreElements()) {
						String element = tokenI.nextToken();
						int indexequal = element.indexOf("=");
						if (indexequal >= 0) {
							String header = element.substring(0, indexequal);
							String values = element.substring(indexequal + 1);
							StringTokenizer tokenItem = new StringTokenizer(values, ":");
							while (tokenItem.hasMoreElements()) {
								String elem = tokenItem.nextToken();
								int indexparenthesis = elem.indexOf("(");
								String manager = elem.substring(0, indexparenthesis);
								boolean visibility = true;
								String visibleValue = elem.substring(indexparenthesis + 1, elem.length() - 1);
								if ("false".equals(visibleValue)) {
									visibility = false;
								}
								this.setMenuGroupValue(header, manager, visibility);
							}
							this.resizeMenuGroup(header);
						}
					}
				}
			}
			if (param.equals("posit")) {
				if (current.indexOf("~position~") >= -1) {
					current = current.replaceFirst("~position~", "");
					StringTokenizer tokenI = new StringTokenizer(current, ";");
					while (tokenI.hasMoreElements()) {
						String element = tokenI.nextToken();
						int indexequal = element.indexOf("=");
						if (indexequal >= 0) {
							String header = element.substring(0, indexequal);
							String values = element.substring(indexequal + 1);
							int x = Integer.parseInt(values.substring(0, values.indexOf(":")));
							int y = Integer.parseInt(values.substring(values.indexOf(":") + 1));
							this.setMenuGroupValue(header, x, y);
						}
					}
				}
			}

		}
	}

	/**
	 * This method obtains the current visibility values of the MenuItems of the MenuGroups and builds a String to be used into the preference values.
	 *
	 * @param menulist
	 *            ArrayList with the whole MenuGroups of the NavigationMenu.
	 * @return a <code>String</code>
	 */
	public String getPreferencesStructure(List<Object> menulist) {

		StringBuilder buffer = new StringBuilder("~items~");
		String equals = "=";
		String semicolon = ";";

		int numGroupElem = menulist.size();
		StringBuilder groupElement = new StringBuilder();
		for (int i = 0; i < numGroupElem; i++) {
			MenuGroupGUI menugroupGUI = (MenuGroupGUI) menulist.get(i);
			String name = menugroupGUI.getHeader();
			String items = this.getPreferenceItems(menugroupGUI.getMenuItem());
			groupElement.append(name).append(equals).append(items).append(semicolon);
		}
		buffer.append(groupElement.toString());
		return buffer.toString();
	}

	/**
	 * This is an auxiliary method to check the visibility value of the MenuItem and builds a String with them to be used into the preference values.
	 *
	 * @param items
	 *            An Array of MenuItem objects of a MenuGroup.
	 * @return a <code>String</code>.
	 */
	public String getPreferenceItems(MenuItem[] items) {
		StringBuilder buffer = new StringBuilder();

		boolean moreElem = false;
		for (MenuItem item : items) {
			if (moreElem) {
				buffer.append(":");
			} else {
				moreElem = true;
			}
			String manager = item.getManager();
			String value = null;
			if (item.isVisible()) {
				value = "true";
			} else {
				value = "false";
			}
			buffer.append(manager).append("(").append(value).append(")");
		}
		return buffer.toString();
	}

	/**
	 * This method resizes the specified MenuGroup.
	 *
	 * @param header
	 *            String with the header name of the MenuGroup.
	 */
	protected void resizeMenuGroup(String header) {
		for (Object element : this.menuList) {
			MenuGroupGUI menu = (MenuGroupGUI) element;
			if (header.equals(menu.getHeader())) {
				menu.reBound();
			}
		}
	}

	/**
	 * This method restore the default values of the MenuGroups of the NavigationMenu.
	 */
	protected void setDefaultValues() {
		if ((this.menuList == null) || (this.menuList.size() == 0)) {
			return;
		}
		for (Object element : this.menuList) {
			MenuGroupGUI menu = (MenuGroupGUI) element;
			menu.setDefaultValue();
		}
		this.saveNavigationMenuPreference();
	}

	@Override
	public void setResourceBundle(ResourceBundle bundle) {
		super.setResourceBundle(bundle);
		this.bundle = bundle;
	}

	/**
	 * This method shows a PopupMenu when a right mouse button click is done.
	 *
	 * @param e
	 *            Mouse event.
	 */
	protected void showPopupMenu(MouseEvent e) {
		if (this.popupmenu == null) {
			this.popupmenu = new JPopupMenu();
			JMenuItem defaultValueMenu = new JMenuItem(ApplicationManager.getTranslation(NavigatorMenuGUI.RESTORE_DEFAULTS, this.bundle));
			defaultValueMenu.addActionListener(e1 -> NavigatorMenuGUI.this.setDefaultValues());

			JMenuItem visibleItemsMenu = new JMenuItem(ApplicationManager.getTranslation(NavigatorMenuGUI.VISIBLE_ITEMS, this.bundle));
			visibleItemsMenu.addActionListener(e1 -> NavigatorMenuGUI.this.configureMenuGroupItems());

			this.popupmenu.add(defaultValueMenu);
			this.popupmenu.add(visibleItemsMenu);
		}

		if (this.popupmenu != null) {
			this.popupmenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * This method creates the visibility configuration JDialog to set which MenuItems are going to be displayed in the MenuGroups. If it exists, the method only opens it. In
	 * addition, the method recovers information of the current visibility configuration to repaint all the MenuGroups.
	 */
	private void configureMenuGroupItems() {

		if (this.dialog == null) {
			ApplicationManager.getApplication().getFrame();

			Window w = SwingUtilities.getWindowAncestor(this.parentForm);
			if (w instanceof Frame) {
				this.dialog = new SetupDialog((Frame) w, true);
			} else if (w instanceof Dialog) {
				this.dialog = new SetupDialog((Dialog) w, true);
			}
			this.dialog.getContentPane().setLayout(new GridLayout(1, 0));
			this.dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
			this.dialog.setInputData(this.menuList);
			this.dialog.setResourceBundle(this.bundle);
			this.dialog.jInit();
			Form f = this.parentForm;
			String s = f != null ? f.getArchiveName() + "_" + NavigatorMenuGUI.NAVIGATOR_MENU + "_JDIALOG" : NavigatorMenuGUI.NAVIGATOR_MENU + "JDIALOG";
			((EJDialog) this.dialog).setSizePositionPreference(s);
			this.dialog.pack();
			ApplicationManager.center(this.dialog);
		}

		this.dialog.setResourceBundle(this.bundle);
		this.dialog.setVisible(true);

		if (this.dialog.operation == SetupDialog.ACCEPT) {
			Map<Object, Object> data = this.dialog.getOutputData();
			this.updateVisibleItemsValues(data);
			for (Object element : this.menuList) {
				MenuGroupGUI menu = (MenuGroupGUI) element;
				menu.reBound();
			}
			this.saveNavigationMenuPreference(true);
			this.repaint();
		}
	}

	/**
	 * This method updates the visibility values of each MenuItem of each MenuGroup with the information contained into the Map received. The information is returned by the
	 * visibility configuration JDialog.
	 *
	 * @param data
	 *            Map with the MenuItem visibility information. Pair values like:
	 *
	 *            <Table BORDER=1 CELLPADDING=2 CELLSPACING=1 RULES=ROWS * * FRAME=BOX> <tr> <td><b>key</td> <td><b>values</td> </tr> <tr> <td>"MenuGroupName"</td>
	 *            <td>MenuItemInformation Object</td> </tr> </table>
	 *
	 */
	public void updateVisibleItemsValues(Map<Object, Object> data) {

		Iterator<Object> it = data.keySet().iterator();
		while (it.hasNext()) {
			String nameGroup = (String) it.next();
			for (Object element : this.menuList) {
				String currentHeader = ((MenuGroupGUI) element).getHeader();
				if (nameGroup.equals(currentHeader)) {
					MenuGroup currentMenu = (MenuGroup) element;
					MenuItem[] currentMenuItems = currentMenu.getMenuItem();

					List<?> items = (List<?>) data.get(nameGroup);
					int numItems = items.size();
					for (int k = 0; k < numItems; k++) {
						MenuItemInformation menuItemInfo = (MenuItemInformation) items.get(k);
						String currentItemName = menuItemInfo.getItemName();
						for (MenuItem currentMenuItem : currentMenuItems) {
							String mIname = currentMenuItem.getManager();
							if (currentItemName.equals(mIname)) {
								boolean visibility = true;
								if (!menuItemInfo.isSelected()) {
									visibility = false;
								}
								currentMenuItem.setVisible(visibility);
								break;
							}
						}
					}
					break;
				}
			}

		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	// Public static classes
	// //////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This class integrate Ontimize characteristics into the MenuGroup.
	 *
	 * @author Imatia Innovation
	 */
	public static class MenuGroupGUI extends MenuGroup {

		/**
		 * Default value to the x coordinate of the MenuGroupGUI
		 */
		protected int	defaultX;

		/**
		 * Default value to the y coordinate of the MenuGroupGUI
		 */
		protected int	defaultY;

		/**
		 * Value of displacements into x-axis.
		 */
		int				offsetX		= 0;

		/**
		 * Value of displacements into x-axis.
		 */
		int				offsetY		= 0;

		/**
		 * Boolean that indicate if the MenuGroup are going to be dragged.
		 */
		boolean			drag		= false;

		boolean			executing	= false;

		/**
		 * Constructs a new MenuGroupGUI with the parameters specified into the map.
		 *
		 * @param parameters
		 *            Map with the whole parameters.
		 */
		public MenuGroupGUI(Map<Object, Object> parameters) {
			super(parameters);

			if ((this.borderString != null) && this.hasBorder) {
				this.border = this.getBorder(this.borderString);
				this.setBorder(this.border);
			}

			int x = 0;
			int y = 0;
			if (parameters.containsKey(NavigationMenu.X)) {
				x = Integer.parseInt((String) parameters.get(NavigationMenu.X));
			}
			if (parameters.containsKey(NavigationMenu.Y)) {
				y = Integer.parseInt((String) parameters.get(NavigationMenu.Y));
			}

			// String[] options = null;
			// if(parameters.containsKey(NavigationMenu.OPTIONS)){
			// options = (String[])parameters.get(NavigationMenu.OPTIONS);
			// }

			this.defaultX = x;
			this.defaultY = y;

			ToolTipManager.sharedInstance().registerComponent(this);
		}

		/**
		 * Constructs a new MenuGroupGUI with the specified parameters.
		 *
		 * @param header
		 *            String with the text to display into the Header of the MenuGroup.
		 * @param options
		 *            String Array with the identifier of each MenuItem of the MenuGroup.
		 * @param icons
		 *            ImageIcon Array with the icons to each MenuItem of the MenuGroup.
		 * @param x
		 *            The coordinate x of the MenuGroup into the NavigationMenu.
		 * @param y
		 *            The coordinate y of the MenuGroup into the NavigationMenu.
		 * @param w
		 *            The width of the MenuGroup.
		 * @param h
		 *            The absolute height of the MenuGroup. The header height is included into this height.
		 * @param bgH
		 *            The color of the MenuGroup Header.
		 * @param bgB
		 *            The color of the background MenuGroup.
		 * @param fg
		 *            The color of the MenuGroup font.
		 * @param borderColor
		 *            The color of the border of the MenuGroup.
		 *
		 *            Use {@link #MenuGroupGUI(Map parameters)}
		 * @deprecated
		 */
		@Deprecated
		public MenuGroupGUI(String header, String[] options, ImageIcon[] icons, int x, int y, int w, int h, Color bgH, Color bgB, Color fg, Color borderColor) {
			super(header, options, icons, x, y, w, h, bgH, bgB, fg, borderColor);
			this.defaultX = x;
			this.defaultY = y;

			ToolTipManager.sharedInstance().registerComponent(this);
		}

		/**
		 * @param parameters
		 *            the <code>Map</code> with the whole parameters to configure the MenuItem. <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS * FRAME=BOX> <tr>
		 *            <td><b>attribute</td> <td><b>values</td> <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr> <tr> <td>menuitemclass</td> <td></td>
		 *            <td><i>"com.ontimize.gui.field.NavigatorMenuGUI$MenuItemGUI"</td> <td>no</td> <td>The class of the MenuItem</td> </tr> </table>
		 */
		@Override
		protected void createMenuItems(Map<Object, Object> parameters) {
			if (!parameters.containsKey(NavigationMenu.MENUITEMCLASS)) {
				parameters.put(NavigationMenu.MENUITEMCLASS, "com.ontimize.gui.field.NavigatorMenuGUI$MenuItemGUI");
			}
			String[] options = null;
			if (parameters.containsKey(NavigationMenu.OPTIONS)) {
				options = (String[]) parameters.get(NavigationMenu.OPTIONS);
			}
			super.createMenuItems(parameters);
			for (int i = 0; i < options.length; i++) {
				((MenuItemGUI) this.menuItem[i]).setVisiblePermission(this.checkVisiblePermission(this.menuItem[i]));
				((MenuItemGUI) this.menuItem[i]).setEnabledPermission(this.checkEnabledPermission(this.menuItem[i]));
			}
		}

		/**
		 * This method returns a Border object from a String with the border style.
		 *
		 * @param border
		 *            String with the border style.
		 * @return a <code>Border</code> object.
		 */
		protected Border getBorder(String border) {
			try {
				Border bmBorder = BorderManager.getBorder(border);
				return bmBorder;

			} catch (Exception e) {
				NavigatorMenuGUI.logger.debug(this.getClass().getName() + ". Error getBorder: " + e.getMessage(), e);
				return null;
			}
		}

		public void setDefaultValue() {
			this.setXY(this.defaultX, this.defaultY);
		}

		/**
		 * This method establishes the visibility of the given MenuItem identifier.
		 *
		 * @param manager
		 *            The identifier of the MenuItem.
		 * @param visibility
		 *            Boolean with the visibility value of the MenuItem.
		 */
		public void setVisibleItems(String manager, boolean visibility) {
			for (MenuItem element : this.menuItem) {
				String currentManager = element.getManager();
				if (manager.equals(currentManager)) {
					element.setVisible(visibility);
					return;
				}
			}
		}

		@Override
		public String getToolTipText(MouseEvent event) {
			int index = this.getOptionIndex(event.getX(), event.getY());
			if (index == -2) {
				if (this.dragEnabled) {
					return ApplicationManager.getTranslation("navigatormenu.drag_message");
				}
			}
			return null;
		}

		@Override
		protected void installMouseHandler() {
			this.addMouseMotionListener(new MouseMotionAdapter() {

				@Override
				public void mouseDragged(MouseEvent e) {
					if (MenuGroupGUI.this.executing) {
						return;
					}
					if (MenuGroupGUI.this.drag) {
						int tempX = e.getX() - MenuGroupGUI.this.offsetX;
						int tempY = e.getY() - MenuGroupGUI.this.offsetY;
						MenuGroupGUI.this.move(tempX, tempY);
						MenuGroupGUI groupMove = (MenuGroupGUI) e.getComponent();

						java.awt.Rectangle rect = new java.awt.Rectangle(groupMove.getBounds().width, groupMove.getHeaderHeight());
						// scrollRectToVisible(groupMove.getBounds());
						MenuGroupGUI.this.scrollRectToVisible(rect);
					}
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					if (MenuGroupGUI.this.executing) {
						return;
					}
					if (MenuGroupGUI.this.drag) {
						return;
					}
					int index = MenuGroupGUI.this.getOptionIndex(e.getX(), e.getY());
					if (index == -1) {
						MenuGroupGUI.this.selectedOption = null;
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						return;
					}
					if (index == -2) {
						MenuGroupGUI.this.selectedOption = null;
						return;
					}
					if (MenuGroupGUI.this.menuItem[index].isVisible() && MenuGroupGUI.this.menuItem[index].isEnabled()) {
						MenuGroupGUI.this.selectedOption = MenuGroupGUI.this.menuItem[index].getManager();
						if (MenuGroupGUI.this.selectedOption != null) {
							e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						} else {
							e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					} else {
						MenuGroupGUI.this.selectedOption = null;
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}

					MenuGroupGUI.this.repaint();
				}
			});

			this.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					if (MenuGroupGUI.this.executing) {
						return;
					}
					if (SwingUtilities.isLeftMouseButton(e)) {
						int index = MenuGroupGUI.this.getOptionIndex(e.getX(), e.getY());
						if (((index == -2) && MenuGroupGUI.this.dragEnabled) || (MenuGroupGUI.this.dragEnabled && MenuGroupGUI.this.dragAllMenuEnabled)) {
							e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
							MenuGroupGUI.this.offsetX = e.getX();
							MenuGroupGUI.this.offsetY = e.getY();
							MenuGroupGUI.this.drag = true;
						}
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (MenuGroupGUI.this.executing) {
						return;
					}
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (MenuGroupGUI.this.drag) {
							((NavigatorMenuGUI) e.getComponent().getParent()).saveNavigationMenuPreference();
						}
						MenuGroupGUI.this.drag = false;
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (MenuGroupGUI.this.drag) {
							return;
						}
						int index = MenuGroupGUI.this.getOptionIndex(e.getX(), e.getY());
						if ((index == -1) || (index == -2)) {
							MenuGroupGUI.this.selectedOption = null;
							return;
						}
						if (MenuGroupGUI.this.menuItem[index].isVisible() && MenuGroupGUI.this.menuItem[index].isEnabled()) {
							Cursor oldCursor = e.getComponent().getCursor();
							try {
								MenuGroupGUI.this.executing = true;
								e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								MenuGroupGUI.this.menuItem[index].actionPerformed(new ActionEvent(MenuGroupGUI.this, 0, MenuGroupGUI.this.selectedOption));
							} finally {
								MenuGroupGUI.this.executing = false;
								e.getComponent().setCursor(oldCursor);
							}
						}
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					MenuGroupGUI.this.selectedOption = null;
					MenuGroupGUI.this.repaint();
				}
			});
		}

		@Override
		protected void paintOptions(Graphics g) {
			g.setFont(new Font("Verdana", Font.PLAIN, 10));
			Insets insets = this.getInsets();
			int d = this.getItemHeight();
			int yoffset = this.headerheight + insets.top; // + insets.bottom +
			// optionsOffset;

			boolean first = true;
			for (int i = 0; i < this.menuItem.length; i++) {
				if (this.menuItem[i].isVisible()) {

					boolean selected = false;
					if (this.menuItem[i].getManager().equals(this.selectedOption)) {
						selected = true;
					}

					boolean enabled = true;
					if (!this.menuItem[i].isEnabled()) {
						enabled = false;
					}

					this.menuItemRenderer.setResourceBundle(this.bundle);
					g.translate(insets.left, yoffset);

					Component c = this.menuItemRenderer.getMenuItemRendererComponent(this, this.menuItem[i], selected, enabled);
					if (i == (this.menuItem.length - 1)) {
						d = d + 1;
					}
					c.setSize(this.getWidth() - insets.left - insets.right, d - 1);
					c.paint(g);

					if (this.separator) {
						if (!first) {
							Graphics2D g2d = (Graphics2D) g;
							Stroke old = g2d.getStroke();
							Color oldColor = g2d.getColor();
							try {
								Color newColor = ColorConstants.parseColor("#999999");
								g2d.setColor(newColor);
							} catch (Exception e) {
								NavigatorMenuGUI.logger.error(null, e);
							}
							g2d.setStroke(NavigatorMenuGUI.stroke);
							g2d.drawLine(0, -1, this.getWidth() - insets.left - insets.right, -1);
							g2d.setStroke(old);
							g2d.setColor(oldColor);
						}
					}

					g.translate(-insets.left, -yoffset);
					yoffset = yoffset + d;

					if (first) {
						first = false;
					}

				}

			}

		}

		/**
		 * This method checks the visibility of the specified MenuItem according to Ontimize Permissions. If it returns true it means that the MenuItem could be visible.
		 *
		 * @param menuItem
		 *            The MenuItem to be checked.
		 * @return a <code>boolean</code>.
		 */
		protected boolean checkVisiblePermission(MenuItem menuItem) {
			ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
			if (manager != null) {
				MenuPermission visiblePermission = new MenuPermission("visible", menuItem.getManager(), true);
				try {
					manager.checkPermission(visiblePermission);
					return true;
				} catch (Exception e) {
					NavigatorMenuGUI.logger.trace(null, e);
					return false;
				}
			}
			return true;
		}

		/**
		 * This method checks the availibility of the specified MenuItem according to Ontimize Permissions. If it returns true it means that the MenuItem could be enabled.
		 *
		 * @param menuItem
		 *            The MenuItem to be checked.
		 * @return a <code>boolean</code>.
		 */
		protected boolean checkEnabledPermission(MenuItem menuItem) {
			ClientSecurityManager manager = ApplicationManager.getClientSecurityManager();
			if (manager != null) {
				MenuPermission enabledPermission = new MenuPermission("enabled", menuItem.getManager(), true);
				try {
					manager.checkPermission(enabledPermission);
					return true;
				} catch (Exception e) {
					NavigatorMenuGUI.logger.trace(null, e);
					return false;
				}
			}
			return true;
		}

	}

	/**
	 * This class integrate Ontimize characteristics into the MenuItem.
	 *
	 * @author Imatia Innovation
	 *
	 */
	public static class MenuItemGUI extends MenuItem {

		/**
		 * Boolean that specifies if this MenuItemGUI is visible in accordance with the Ontimize Permissions.
		 */
		protected boolean	visiblePermission	= true;

		/**
		 * Boolean that specifies if this MenuItemGUI is enabled in accordance with the Ontimize Permissions.
		 */
		protected boolean	enabledPermission	= true;

		/**
		 * Constructs a new MenuItemGUI specifying the identifier and the icon of the MenuItmeGUI.
		 *
		 * @param manager
		 *            String with the identifier of the MenuItemGUI. That is, the manager associated to this MenuItem.
		 * @param icon
		 *            ImageIcon with the icon of the MenuItemGUI.
		 */
		public MenuItemGUI(String manager, ImageIcon icon) {
			super(manager, icon);
		}

		/**
		 * This method returns if the MenuItemGUI is visible according to the Ontimize Permissions. If it is true the MenuItemGUI is visible.
		 *
		 * @return a <code>boolean</code>.
		 */
		public boolean isVisiblePermission() {
			return this.visiblePermission;
		}

		/**
		 * This method sets if the MenuItemGUI is visible according to the Ontimize Permissions.
		 *
		 * @param visiblePermission
		 *            boolean.
		 */
		public void setVisiblePermission(boolean visiblePermission) {
			this.visiblePermission = visiblePermission;
		}

		/**
		 * This method returns if the MenuItemGUI is enabled according to the Ontimize Permissions. If it is true the MenuItemGUI is enabled.
		 *
		 * @return a <code>boolean</code>.
		 */
		public boolean isEnabledPermission() {
			return this.enabledPermission;
		}

		/**
		 * This method sets if the MenuItemGUI is enabled according to the Ontimize Permissions.
		 *
		 * @param enabledPermission
		 *            boolean.
		 */
		public void setEnabledPermission(boolean enabledPermission) {
			this.enabledPermission = enabledPermission;
		}

		@Override
		public boolean isVisible() {
			if (!this.visiblePermission) {
				return false;
			}
			return super.isVisible();
		}

		@Override
		public boolean isEnabled() {
			if (!this.enabledPermission) {
				return false;
			}
			return super.isEnabled();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (ApplicationManager.getApplication().getMenuListener() instanceof DefaultActionMenuListener) {
				((DefaultActionMenuListener) ApplicationManager.getApplication().getMenuListener()).actionPerformed(e);
			}
		}

	}

	/**
	 * This class introduces a group of SelectableItem objects into a SelectableItem.
	 *
	 * @author Imatia Innovation.
	 *
	 */
	public static class GroupSelectableItem extends SelectableItem {

		/**
		 * ArrayList of SelectableItems.
		 */
		protected List<Object> itemSelectableItem = null;

		/**
		 * Constructs a new GroupSelectableItem
		 *
		 * @param text
		 *            String with the text of the SelectableItem.
		 */
		public GroupSelectableItem(String text) {
			super(text);
		}

		/**
		 * This method returns the ArrayList of SelectableItems associated to this GroupSelectableItem.
		 *
		 * @return a <code>ArrayList</code> of SelectableItems.
		 */
		public List<Object> getItemSelectableItem() {
			return this.itemSelectableItem;
		}

		/**
		 * This method sets the ArrayList of SelectableItems of this GroupSelectableItem.
		 *
		 * @param itemSelectableItem
		 *            ArrayList of SelectableItems.
		 */
		public void setItemSelectableItem(List<Object> itemSelectableItem) {
			this.itemSelectableItem = itemSelectableItem;
		}

		/**
		 * This method adds a SelectableItem to the ArrayList of SelectablItems.
		 *
		 * @param item
		 *            The SelectbleItem to be added.
		 */
		public void addItemSelectableItem(SelectableItem item) {
			if (this.itemSelectableItem == null) {
				this.itemSelectableItem = new ArrayList();
			}
			this.itemSelectableItem.add(item);
		}

		public boolean isEmpty() {
			if (this.itemSelectableItem == null) {
				return true;
			}
			return false;
		}

	}

	/**
	 * This class configures the JDialog in which the visibility of the MenuItems of the MenuGroups are configured.
	 *
	 * @author Imatia Innovation.
	 *
	 */
	public static class SetupDialog extends EJDialog implements Internationalization {

		/**
		 * Text of the title of the JDialog.
		 */
		public static final String		MENU_TITLE_TEXT_KEY	= "navigatormenu.title_dialog";

		/**
		 * Text of the accept button.
		 */
		public static final String		ACCEPT_BUTTON_TEXT	= "OptionPane.okButtonText";

		/**
		 * Text of the cancel button.
		 */
		public static final String		CANCEL_BUTTON_TEXT	= "OptionPane.cancelButtonText";

		/**
		 * Code of the operation of push the accept button.
		 */
		public static final int			ACCEPT				= 0;

		/**
		 * Code of the operation of push the cancel button.
		 */
		public static final int			CANCEL				= -1;

		/**
		 * ResourceBundle to be applied to the JDialog.
		 */
		protected ResourceBundle		bundle				= null;

		/**
		 * Contains the result of the operation that was done over the JDialog.
		 */
		public int						operation			= -1;

		/**
		 * ArrayList that contains all the MenuGroups of the NavigationMenu.
		 */
		protected List<Object>			menulist			= null;

		/**
		 * Map that contains the whole information about a MenuGroup.
		 */
		protected Map<Object, Object>	menuGroupInfo		= null;

		/**
		 * Cancel JButton.
		 */
		protected JButton				bcancel				= null;

		/**
		 * Accept JButton
		 */
		protected JButton				baccept				= null;

		/**
		 * Constructs a new JDialog with the specified parameters.
		 *
		 * @param owner
		 *            Frame
		 * @param modal
		 *            Boolean to configure modal mode.
		 */
		public SetupDialog(Frame owner, boolean modal) {
			super(owner, SetupDialog.MENU_TITLE_TEXT_KEY, modal);
		}

		/**
		 * Constructs a new JDialog with the specified parameters.
		 *
		 * @param owner
		 *            Dialog
		 * @param modal
		 *            Boolean to configure modal mode.
		 */
		public SetupDialog(Dialog owner, boolean modal) {
			super(owner, SetupDialog.MENU_TITLE_TEXT_KEY, modal);
		}

		boolean allDeselected = false;

		/**
		 * This method initializes the JDialog of visibility configuration.
		 */
		public void jInit() {
			// setUndecorated(true);
			final List<Object> menuGroupGUI = this.menulist;

			JPanel jMyPanel = new JPanel();
			jMyPanel.setLayout(new GridBagLayout());
			jMyPanel.setOpaque(true);
			jMyPanel.setBackground(new Color(215, 221, 223));

			DefaultListModel listModel = new DefaultListModel();

			for (Object element : menuGroupGUI) {
				MenuGroupGUI menuGroupSelected = (MenuGroupGUI) element;
				String groupName = menuGroupSelected.getHeader();
				GroupSelectableItem gsItem = new GroupSelectableItem(groupName);

				boolean allSelected = true;
				MenuItem[] items = menuGroupSelected.getMenuItem();
				if ((items != null) && (items.length > 0)) {
					for (MenuItem item : items) {
						String view = item.getManager();
						SelectableItem selItem = new SelectableItem(view);
						MenuItemGUI mIGUI = (MenuItemGUI) item;
						if (mIGUI.isVisiblePermission()) {
							if (item.isVisible()) {
								selItem.setSelected(true);
							} else {
								allSelected = false;
							}
							gsItem.addItemSelectableItem(selItem);
						}
					}

					if (allSelected) {
						gsItem.setSelected(true);
					}
					if (!gsItem.isEmpty()) {
						listModel.addElement(gsItem);
					}
				}
			}

			final JList jlistGroup = new JList(listModel);
			SelectableItemListCellRenderer selItemRenderer = new SelectableItemListCellRenderer(this.bundle);
			selItemRenderer.setSelectedBackground(new Color(99, 200, 219));
			selItemRenderer.setSelectedForeground(new Color(255, 255, 255));
			selItemRenderer.setNotSelectedBackground(new Color(240, 240, 240));
			selItemRenderer.setNotSelectedForeground(new Color(67, 67, 67));
			selItemRenderer.setFont(new Font("Verdana", Font.PLAIN, 10));
			jlistGroup.setCellRenderer(selItemRenderer);
			jlistGroup.setOpaque(true);
			jlistGroup.setBackground(new Color(240, 240, 240));

			JScrollPane jScrollPaneGroup = new JScrollPane(jlistGroup);
			jScrollPaneGroup.setBorder(null);
			final JList jlistItem = new JList(new DefaultListModel());
			jlistItem.setCellRenderer(selItemRenderer);
			jlistItem.setOpaque(true);
			jlistItem.setBackground(new Color(240, 240, 240));

			final JScrollPane jScrollPaneItem = new JScrollPane(jlistItem);
			jScrollPaneItem.setBorder(null);

			JPanel jPanelCheckSelection = new JPanel();
			jPanelCheckSelection.setLayout(new BorderLayout());
			ButtonGroup checkBoxGroup = new ButtonGroup();
			final JCheckBox jcheckAll = new JCheckBox();
			jcheckAll.setText("Select all");
			jcheckAll.setFont(new Font("Verdana", Font.PLAIN, 9));
			jcheckAll.setForeground(new Color(240, 240, 240));
			jcheckAll.setOpaque(true);
			jcheckAll.setBackground(new Color(153, 153, 153));
			final JCheckBox jcheckNone = new JCheckBox();
			jcheckNone.setText("Deselect all");
			jcheckNone.setFont(new Font("Verdana", Font.PLAIN, 9));
			jcheckNone.setForeground(new Color(240, 240, 240));
			jcheckNone.setOpaque(true);
			jcheckNone.setBackground(new Color(153, 153, 153));
			final JCheckBox jcheck = new JCheckBox();
			jcheck.setVisible(false);
			jcheck.setSelected(true);

			checkBoxGroup.add(jcheckAll);
			checkBoxGroup.add(jcheckNone);
			checkBoxGroup.add(jcheck);
			// jPanelCheckSelection.add(checkBoxGroup,BorderLayout.CENTER);
			jPanelCheckSelection.add(jcheckAll, BorderLayout.NORTH);
			jPanelCheckSelection.add(jcheckNone, BorderLayout.SOUTH);

			InnerLabel jLblMenu = new InnerLabel();
			jLblMenu.setOpaque(true);
			jLblMenu.setText("menus");
			jLblMenu.setFont(new Font("Verdana", Font.BOLD, 14));
			jLblMenu.setForeground(new Color(255, 255, 255));
			jLblMenu.setBackground(new Color(67, 67, 67));
			jLblMenu.setHorizontalAlignment(SwingConstants.CENTER);
			jLblMenu.setPreferredSize(27);

			InnerLabel jLblItem = new InnerLabel();
			jLblItem.setOpaque(true);
			jLblItem.setText("items");
			jLblItem.setFont(new Font("Verdana", Font.BOLD, 14));
			jLblItem.setForeground(new Color(255, 255, 255));
			jLblItem.setBackground(new Color(67, 67, 67));
			jLblItem.setHorizontalAlignment(SwingConstants.CENTER);
			jLblItem.setPreferredSize(27);

			InnerLabel jLblBackGround = new InnerLabel();
			jLblBackGround.setOpaque(true);
			jLblBackGround.setBackground(new Color(222, 222, 222));
			jLblBackGround.setPreferredSize(41);

			JPanel jPanelGroup = new JPanel();
			jPanelGroup.setLayout(new BorderLayout());
			jPanelGroup.add(jLblMenu, BorderLayout.NORTH);

			final JPanel jPanelGroupLists = new JPanel();
			jPanelGroupLists.setLayout(new BorderLayout());
			jPanelGroupLists.add(jPanelCheckSelection, BorderLayout.NORTH);
			jPanelGroupLists.add(jScrollPaneGroup, BorderLayout.CENTER);

			Border border = BorderFactory.createMatteBorder(2, 1, 2, 2, new Color(109, 115, 115));

			jPanelGroupLists.setBorder(border);

			jPanelGroup.add(jPanelGroupLists, BorderLayout.CENTER);

			JPanel jPanelItem = new JPanel();
			jPanelItem.setLayout(new BorderLayout());
			jPanelItem.add(jLblItem, BorderLayout.NORTH);

			JPanel jPanelItemLists = new JPanel();
			jPanelItemLists.setLayout(new BorderLayout());
			jPanelItemLists.add(jLblBackGround, BorderLayout.NORTH);
			jPanelItemLists.add(jScrollPaneItem, BorderLayout.CENTER);
			jPanelItemLists.setBorder(border);

			jPanelItem.add(jPanelItemLists, BorderLayout.CENTER);

			final JSplitPane jsplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jPanelGroup, jPanelItem);
			jsplitPane.setBorder(null);
			jsplitPane.setDividerSize(7);
			jMyPanel.add(jsplitPane, new GridBagConstraints(0, 0, 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 1, 1));

			this.baccept = new JButton(SetupDialog.ACCEPT_BUTTON_TEXT, ImageManager.getIcon(ImageManager.OK)) {

				BasicButtonUI buttonUI = new BasicButtonUI();

				@Override
				public void setUI(ButtonUI ui) {
					super.setUI(new BasicButtonUI());
				}
			};

			this.baccept.setFont(new Font("Verdana", Font.PLAIN, 10));
			this.baccept.setOpaque(true);
			this.baccept.setBackground(new Color(74, 112, 116));
			this.baccept.setForeground(new Color(222, 222, 222));
			// baccept.setForeground(new Color(67,67,67));
			this.baccept.setBorder(null);
			this.baccept.setFocusPainted(false);

			jMyPanel.add(this.baccept, new GridBagConstraints(0, 1, 1, 1, 0.5, 0, GridBagConstraints.EAST, 0, new Insets(2, 0, 2, 3), 20, 10));

			this.bcancel = new JButton(SetupDialog.CANCEL_BUTTON_TEXT, ImageManager.getIcon(ImageManager.CANCEL)) {

				@Override
				public void setUI(ButtonUI ui) {
					super.setUI(new BasicButtonUI());
				}
			};

			this.bcancel.setFont(new Font("Verdana", Font.PLAIN, 10));
			this.bcancel.setOpaque(true);
			this.bcancel.setBackground(new Color(74, 112, 116));
			this.bcancel.setForeground(new Color(222, 222, 222));
			// bcancel.setForeground(new Color(67,67,67));
			this.bcancel.setBorder(null);
			this.bcancel.setFocusPainted(false);
			jMyPanel.add(this.bcancel, new GridBagConstraints(1, 1, 1, 1, 0.5, 0, GridBagConstraints.WEST, 0, new Insets(2, 3, 2, 0), 20, 10));

			// //////////////////////////Listeners///////////////////////////////////////////////////
			this.baccept.addActionListener(event -> {

				Map<Object, Object> data = new HashMap<>();
				data = SetupDialog.this.getInformationJlists(jlistGroup, jlistItem);
				SetupDialog.this.setOutputData(data);
				SetupDialog.this.operation = SetupDialog.ACCEPT;
				SetupDialog.this.setVisible(false);
			});

			this.bcancel.addActionListener(e -> {
				SetupDialog.this.operation = SetupDialog.CANCEL;
				SetupDialog.this.setVisible(false);
			});

			jcheckAll.setFocusPainted(false);
			jcheckAll.addChangeListener(e -> {
				if (jcheckAll.isSelected()) {
					for (int i = 0; i < jlistGroup.getModel().getSize(); i++) {
						GroupSelectableItem group = (GroupSelectableItem) jlistGroup.getModel().getElementAt(i);
						group.setSelected(true);
						((DefaultListModel) jlistGroup.getModel()).setElementAt(group, i);
					}
					jPanelGroupLists.repaint();
				}
			});

			jcheckNone.setFocusPainted(false);
			jcheckNone.addChangeListener(e -> {
				if (jcheckNone.isSelected()) {
					for (int i = 0; i < jlistGroup.getModel().getSize(); i++) {
						GroupSelectableItem group = (GroupSelectableItem) jlistGroup.getModel().getElementAt(i);
						group.setSelected(false);
						((DefaultListModel) jlistGroup.getModel()).setElementAt(group, i);
					}
					jPanelGroupLists.repaint();
				}
				SetupDialog.this.allDeselected = true;
			});

			jlistGroup.getModel().addListDataListener(new ListDataListener() {

				@Override
				public void contentsChanged(ListDataEvent event) {

					int index = event.getIndex0();
					GroupSelectableItem gsItem = (GroupSelectableItem) jlistGroup.getModel().getElementAt(index);
					List<Object> selectableItem = gsItem.getItemSelectableItem();
					if (gsItem.isSelected()) {
						for (Object element : selectableItem) {
							SelectableItem selItem = (SelectableItem) element;
							selItem.setSelected(true);
						}
					} else {
						for (Object element : selectableItem) {
							SelectableItem selItem = (SelectableItem) element;
							selItem.setSelected(false);
						}
					}

					jsplitPane.repaint();
				}

				@Override
				public void intervalAdded(ListDataEvent event) {}

				@Override
				public void intervalRemoved(ListDataEvent event) {}

			});

			jlistGroup.addListSelectionListener(e -> {
				int index = jlistGroup.getSelectedIndex();
				GroupSelectableItem menuSelected = (GroupSelectableItem) jlistGroup.getModel().getElementAt(index);
				String menuGroupNameSelected = menuSelected.getText();
				List<Object> selectableItem = menuSelected.getItemSelectableItem();

				for (Object element : menuGroupGUI) {
					MenuGroupGUI menuGroupSelected = (MenuGroupGUI) element;
					if (menuGroupSelected.getHeader().equals(menuGroupNameSelected)) {

						DefaultListModel dataItemModel = (DefaultListModel) jlistItem.getModel();
						dataItemModel.clear();
						for (Object element2 : selectableItem) {
							SelectableItem selItem = (SelectableItem) element2;
							dataItemModel.addElement(selItem);
						}
						break;
					}
				}

				boolean anySelected = false;

				for (int j = 0; j < jlistGroup.getModel().getSize(); j++) {
					GroupSelectableItem menu = (GroupSelectableItem) jlistGroup.getModel().getElementAt(j);

					if (menu.isSelected()) {
						anySelected = true;
					}
				}

				if (anySelected) {
					jcheck.setSelected(true);
				}
				if (!anySelected && SetupDialog.this.allDeselected) {
					jcheck.setSelected(true);
					SetupDialog.this.allDeselected = false;
				}
			});

			jlistGroup.addMouseListener(new SelectableItemMouseListener());

			jlistItem.addMouseListener(new SelectableItemMouseListener());

			jlistItem.getModel().addListDataListener(new ListDataListener() {

				@Override
				public void contentsChanged(ListDataEvent e) {
					boolean allmarked = true;

					for (int i = 0; i < jlistItem.getModel().getSize(); i++) {
						SelectableItem selItem = (SelectableItem) jlistItem.getModel().getElementAt(i);
						if (!selItem.isSelected()) {
							allmarked = false;
						}
					}
					int index = jlistGroup.getSelectedIndex();
					GroupSelectableItem groupItem = (GroupSelectableItem) jlistGroup.getModel().getElementAt(index);
					if (allmarked && !groupItem.isSelected()) {
						groupItem.setSelected(true);
					}
					if (!allmarked && groupItem.isSelected()) {
						groupItem.setSelected(false);
					}

					jsplitPane.repaint();
				}

				@Override
				public void intervalAdded(ListDataEvent e) {}

				@Override
				public void intervalRemoved(ListDataEvent e) {}

			});

			jlistItem.addListSelectionListener(e -> {
			});

			this.getContentPane().add(jMyPanel);
		}

		/**
		 * This method returns the necessary data to configure the JDialog.
		 *
		 * @return a <code>ArrayList</code> whit the input data necessary.
		 */
		public List<Object> getInputData() {
			return this.menulist;
		}

		/**
		 * This method establishes the input data necessary to configure the JDialog. That is a list with the whole MenuGroups of the NavigationMenu.
		 *
		 * @param menulist
		 *            ArrayList whit the input data necessary.
		 */
		public void setInputData(List<Object> menulist) {
			this.menulist = menulist;
		}

		/**
		 * This method returns a data structure with the visibility configuration of the MenuItems of the MenuGroups.
		 *
		 * @return a <code>Map</code> whit the data structure.
		 */
		public Map<Object, Object> getOutputData() {
			return this.menuGroupInfo;
		}

		/**
		 * This method established the data structure with the visibility configuration of the MenuItems of the MenuGroups.
		 *
		 * @param data
		 *            Hashatble with the data structure.
		 */
		public void setOutputData(Map<Object, Object> data) {
			this.menuGroupInfo = data;
		}

		/**
		 * This method obtains the information of the Jlists with the visibility configuration of the MenuItems and creates a map containing all this information.
		 *
		 * @param jlistGroup
		 *            Jlist that contains all the MenuGroups of the NavigationMenu.
		 * @param jlistItem
		 *            JList that contains all MenuItems of each MenuGroup.
		 * @return a <code>Map</code> with all information.
		 */
		public Map<Object, Object> getInformationJlists(JList jlistGroup, JList jlistItem) {
			Map<Object, Object> data = new HashMap<>();

			int numMenuGroups = jlistGroup.getModel().getSize();
			for (int i = 0; i < numMenuGroups; i++) {
				GroupSelectableItem menuGroup = (GroupSelectableItem) jlistGroup.getModel().getElementAt(i);
				String menuGName = menuGroup.getText();

				List<Object> infoItems = new ArrayList<>();
				List<Object> items = menuGroup.getItemSelectableItem();
				for (Object item : items) {
					SelectableItem selItem = (SelectableItem) item;
					String name = selItem.getText();
					boolean selected = true;
					if (!selItem.isSelected()) {
						selected = false;
					}
					MenuItemInformation menuIInfo = new MenuItemInformation(name, selected);
					infoItems.add(menuIInfo);
				}
				if ((menuGName != null) && !infoItems.isEmpty()) {
					data.put(menuGName, infoItems);
				}
			}

			return data;
		}

		/**
		 * Returns list with all <code>MenuGroup</code> in component.
		 *
		 * @return menugroup list
		 * @since 5.2068EN-0.7
		 */
		public List<Object> getMenulist() {
			return this.menulist;
		}

		/**
		 * Set list with all <code>MenuGroup</code> in component.
		 *
		 * @param menulist
		 *            List with <code>MenuGroup</code>
		 * @since 5.2068EN-0.7
		 */
		public void setMenulist(List<Object> menulist) {
			this.menulist = menulist;
		}

		@Override
		public List<String> getTextsToTranslate() {
			return null;
		}

		@Override
		public void setComponentLocale(Locale arg0) {}

		@Override
		public void setResourceBundle(ResourceBundle bundle) {
			this.bundle = bundle;
			this.setTitle(ApplicationManager.getTranslation(SetupDialog.MENU_TITLE_TEXT_KEY, this.bundle));
			if (this.baccept != null) {
				this.baccept.setText(ApplicationManager.getTranslation(SetupDialog.ACCEPT_BUTTON_TEXT, this.bundle));
			}
			if (this.bcancel != null) {
				this.bcancel.setText(ApplicationManager.getTranslation(SetupDialog.CANCEL_BUTTON_TEXT, this.bundle));
			}

		}

	}

	public static class InnerLabel extends JLabel {

		public void setPreferredSize(int height) {
			Dimension preferredSize = new Dimension();
			preferredSize.width = 50;
			preferredSize.height = height;
			super.setPreferredSize(preferredSize);
		}

	}

	/**
	 * This class is necessary to the visibility configuration of the MenuItems. It contains the current state of selection of each MenuItem into the JList of the configuration
	 * JDialog.
	 *
	 * @author Imatia Innovation.
	 *
	 */
	public static class MenuItemInformation {

		/**
		 * Boolean that indicates if a MenuItem is selected.
		 */
		protected boolean	selected;

		/**
		 * String with the itemName.
		 */
		protected String	itemName;

		/**
		 * Constructs a new MenuItemInformation with the specified parameters.
		 *
		 * @param itemName
		 *            The name of the MenuItem.
		 * @param selected
		 *            Boolean indicating if the MenuItem is selected.
		 */
		public MenuItemInformation(String itemName, boolean selected) {
			this.itemName = itemName;
			this.selected = selected;
		}

		/**
		 * This method returns if the MenuItem is selected.
		 *
		 * @return a <code>boolean</code> with the selection value.
		 */
		public boolean isSelected() {
			return this.selected;
		}

		/**
		 * This method establishes the visibility of the MenuItem.
		 *
		 * @param selected
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		/**
		 * This method returns the MenuItem name.
		 *
		 * @return a <code>String</code> with the MenuItem name.
		 */
		public String getItemName() {
			return this.itemName;
		}

		/**
		 * This method establishes the MenuItem name.
		 *
		 * @param itemName
		 *            The name of the MenuItem.
		 */
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
