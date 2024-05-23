package com.ontimize.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Window;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.builder.xml.XMLApplicationBuilder;
import com.ontimize.gui.container.EJDialog;
import com.ontimize.gui.field.FormComponent;
import com.ontimize.gui.field.IdentifiedElement;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.preferences.BasicApplicationPreferences;
import com.ontimize.gui.preferences.HasPreferenceComponent;
import com.ontimize.gui.toolbar.ApToolComponentTransferHandler;
import com.ontimize.help.HelpUtilities;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.xml.XMLClientProvider;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.selectablelist.SelectableItem;
import com.ontimize.util.swing.selectablelist.SelectableItemListCellRenderer;
import com.ontimize.util.swing.selectablelist.SelectableItemMouseListener;
import com.ontimize.xml.DefaultXMLParametersManager;

/**
 * This class implements a general toolbar for application.
 * <p>
 *
 * @author Imatia Innovation
 */
public class ApplicationToolBar extends JToolBar
implements FormComponent, Freeable, DropTargetListener, HasPreferenceComponent {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationToolBar.class);

	/**
	 * The toolbar button size. By default, 24.
	 */
	public static int DEFAULT_BUTTON_SIZE = 24;

	public static int DEFAULT_TOOLBAR_HEIGHT = -1;

	protected int height = -1;

	/**
	 * The reference to resource bundle file. By default, null.
	 */
	protected ResourceBundle resources = null;

	protected boolean dynamicloaded = false;

	protected TexturePaint texturePaint;


	protected JPopupMenu optionMenu;

	protected JMenuItem restoreMenuItem;

	protected JMenuItem removeMenuItem;

	protected JMenuItem addSeparatorItem;

	protected JMenuItem addFillerItem;

	protected static final String RESTORE_MENU_ITEM_TEXT = "applicationtoolbar.restore";

	protected static final String REMOVE_MENU_ITEM_TEXT = "applicationtoolbar.remove";

	protected static final String ADD_SEPARATOR_ITEM_TEXT = "applicationtoolbar.addseparator";

	protected static final String ADD_FILLER_ITEM_TEXT = "applicationtoolbar.addfiller";

	protected static final String REMOVE_COMPONENT_WINDOW_TITLE = "applicationtoolbar.remove_component_title";

	protected List<Component> originalComponentList = new ArrayList<>();

	protected String userPrefs;

	protected JComponent locationComponent = new JPanel() {
		{
			this.setBorder(new LineBorder(Color.GRAY, 2, false));
			this.setMaximumSize(new Dimension(5, ApplicationToolBar.DEFAULT_BUTTON_SIZE));
		}

		@Override
		public java.awt.Dimension getPreferredSize() {
			return new Dimension(5, ApplicationToolBar.DEFAULT_BUTTON_SIZE);
		};

		@Override
		public void setBounds(Rectangle r) {
			super.setBounds(r);
		}
	};

	protected ApplicationPreferences applicationPreferences;

	protected MouseListener appToolBarMouseListener;

	/**
	 * The class constructor. Calls to <code>super()</code> and initializes parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters
	 */
	public ApplicationToolBar(Map<Object, Object> parameters) {
		super();
		this.init(parameters);
		this.setTransferHandler(new ApToolComponentTransferHandler());

		DropTarget dt = this.getDropTarget();
		try {
			dt.addDropTargetListener(this);
		} catch (TooManyListenersException e) {
			ApplicationToolBar.logger.error(null, e);
		}

		this.appToolBarMouseListener = this.createMouseListener();
		this.addMouseListener(this.appToolBarMouseListener);
	}

	protected MouseListener createMouseListener() {
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					ApplicationToolBar.this.getOptionMenu().show((JComponent) e.getSource(), e.getX(), e.getY());
				}
			}
		};

		return mouseListener;
	}

	/**
	 * Gets the help identifier.
	 * <p>
	 * @return the help identifier
	 */
	public String getHelpIdString() {
		String sClassName = this.getClass().getName();
		sClassName = sClassName.substring(sClassName.lastIndexOf(".") + 1);
		return sClassName + "HelpId";
	}

	@Override
	public void setFloatable(boolean floatable) {
		super.setFloatable(floatable);
		if (floatable) {
			this.setToolTipText(
					ApplicationManager.getTranslation("click_drag_to_change_toolbar_location", this.resources));
		} else {
			this.setToolTipText(null);
		}
	}

	/**
	 * Install the help identifier.
	 */
	public void installHelpId() {
		try {
			String helpId = this.getHelpIdString();
			HelpUtilities.setHelpIdString(this, helpId);
		} catch (Exception e) {
			ApplicationToolBar.logger.error(e.getMessage(), e);
			return;
		}
	}

	/**
	 * Inits parameters. <p>
	 *
	 * @param parameters
	 *            the <code>Map</code> with parameters. Adds the next parameters: <p> <Table BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX> <tr> <td><b>attribute</td>
	 *            <td><b>values</td> <td><b>default</td> <td><b>required</td> <td><b>meaning</td> </tr>
	 *
	 *            <tr> <td>floatable</td> <td><i>yes/no</td> <td>no</td> <td>no</td> <td>Supports the change of the toolbar location.</td> </tr>
	 *
	 *            <tr> <td>buttonsize</td> <td></td> <td>24</td> <td>no</td> <td>Button size for toolbar.</td> </tr>
	 *
	 *            <tr> <td>opaque</td> <td>yes/no</td> <td>yes</td> <td>no</td> <td>The opacity for toolbar container.</td> </tr>
	 *
	 *            <tr> <td>border</td> <td><i>default/none/raised/lowered or a color defined in {@link ColorConstants}. Moreover, it is also allowed a border defined in
	 *            #BorderManager</td> <td></td> <td>no</td> <td>The border for toolbar</td> </tr>
	 *
	 *            <tr> <td>bgcolor</td> <td></td> <td></td> <td>no</td> <td>The background color.</td> </tr>
	 *
	 *            <tr> <td>toolbarheight</td> <td></td> <td></td> <td>no</td> <td>The toolbar height. If this parameter doesn't established the toolbar height is calculated using
	 *            toolbarbutton heights.</td> </tr>
	 *
	 *            <tr> <td>textureimage</td> <td></td> <td></td> <td>no</td> <td>Path to source icon to extract a image and create a <code>TexturePaint</code></td> </tr>
	 *
	 *            </TABLE>
	 *
	 *
	 */

	@Override
	public void init(Map<Object, Object> parameters) {
		ApplicationToolBar.DEFAULT_BUTTON_SIZE = ParseUtils.getInteger((String) parameters.get("buttonsize"),
				ApplicationToolBar.DEFAULT_BUTTON_SIZE);
		Object floatable = parameters.get("floatable");
		if (floatable != null) {
			if (floatable.equals("yes")) {
				this.setFloatable(true);
			} else {
				this.setFloatable(false);
			}
		} else {
			this.setFloatable(false);
		}

		this.installHelpId();

		this.height = ParseUtils.getInteger((String) parameters.get("toolbarheight"),
				ApplicationToolBar.DEFAULT_TOOLBAR_HEIGHT);

		this.setBorder(ParseUtils.getBorder((String) parameters.get("border"), this.getBorder()));
		this.setBackground(ParseUtils.getColor((String) parameters.get("bgcolor"), this.getBackground()));
		this.setOpaque(ParseUtils.getBoolean((String) parameters.get("opaque"), true));

		Image im = ParseUtils.getImage((String) parameters.get("textureimage"), null);
		if (im != null) {
			BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			bi.getGraphics().drawImage(im, 0, 0, null);
			this.texturePaint = new TexturePaint(bi, new Rectangle(bi.getWidth(null), bi.getHeight(null)));
		} else {
			this.texturePaint = null;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (this.texturePaint != null) {
			Graphics2D g2 = (Graphics2D) g;
			Rectangle2D r = new Rectangle2D.Float(0, 0, this.getSize().width, this.getSize().height);
			// Now fill the round rectangle.
			g2.setPaint(this.texturePaint);
			g2.fill(r);
		} else {
			super.paintComponent(g);
		}
	}

	public boolean isToolbarHeightFix() {
		return this.height == -1 ? false : true;
	}

	public int getToolbarHeight() {
		return this.height;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension dimension = super.getPreferredSize();
		// Dimension dimension = new Dimension(0,0);
		if (this.getOrientation() == SwingConstants.HORIZONTAL) {
			if (this.height >= 0) {
				dimension.height = this.height;
			}
		} else {
			if (this.height >= 0) {
				dimension.width = this.height;
			}
		}
		return dimension;
	}

	@Override
	public Object getConstraints(LayoutManager layout) {
		return null;
	};

	/**
	 * Returns the reference to button specified.
	 * <p>
	 * @param attr the attribute
	 * @return the reference to button
	 */
	public AbstractButton getButton(String attr) {
		for (int i = 0; i < this.getComponentCount(); i++) {
			Component c = this.getComponentAtIndex(i);
			if ((c instanceof IdentifiedElement) && (c instanceof AbstractButton)) {
				if ((((IdentifiedElement) c).getAttribute() != null)
						&& ((IdentifiedElement) c).getAttribute().equals(attr)) {
					return (AbstractButton) c;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the reference to component specified.
	 * <p>
	 * @param attr the attribute
	 * @return the reference to component
	 */
	public JComponent getComponent(String attr) {
		for (int i = 0; i < this.getComponentCount(); i++) {
			Component c = this.getComponentAtIndex(i);
			if ((c instanceof IdentifiedElement) && (c instanceof JComponent)) {
				if ((((IdentifiedElement) c).getAttribute() != null)
						&& ((IdentifiedElement) c).getAttribute().equals(attr)) {
					return (JComponent) c;
				}
				if (c instanceof ApToolBarPopupButton) {
					for (int j = 0; j < ((ApToolBarPopupButton) c).getPopupComponentsCount(); j++) {
						Component c2 = ((ApToolBarPopupButton) c).getPopupComponentAt(j);
						if ((((IdentifiedElement) c2).getAttribute() != null)
								&& ((IdentifiedElement) c2).getAttribute().equals(attr)) {
							return (JComponent) c2;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void add(Component c, Object constraints) {
		super.add(c, constraints);
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		this.resources = resources;
		for (int i = 0; i < this.getComponentCount(); i++) {
			if (this.getComponentAtIndex(i) instanceof Internationalization) {
				((Internationalization) this.getComponentAtIndex(i)).setResourceBundle(resources);
			}
		}
		if (this.isFloatable()) {
			this.setToolTipText(ApplicationManager.getTranslation("click_drag_to_change_toolbar_location", resources));
		} else {
			this.setToolTipText(null);
		}

		if (this.optionMenu != null) {
			this.restoreMenuItem.setText(ApplicationManager.getTranslation(ApplicationToolBar.RESTORE_MENU_ITEM_TEXT, resources));
			this.removeMenuItem.setText(ApplicationManager.getTranslation(ApplicationToolBar.REMOVE_MENU_ITEM_TEXT, resources));
			this.addSeparatorItem.setText(ApplicationManager.getTranslation(ApplicationToolBar.ADD_SEPARATOR_ITEM_TEXT, resources));
			this.addFillerItem.setText(ApplicationManager.getTranslation(ApplicationToolBar.ADD_FILLER_ITEM_TEXT, resources));
		}
	}

	@Override
	public void setComponentLocale(Locale l) {
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = new ArrayList<>(0);
		return v;
	}

	public void loadDynamicItems() {
		if (!this.dynamicloaded) {
			try {
				EntityReferenceLocator locator = ApplicationManager.getApplication().getReferenceLocator();
				if (locator instanceof XMLClientProvider) {
					XMLClientProvider clientProvider = (XMLClientProvider) locator;
					String xmlToolbar = clientProvider.getXMLToolbar(locator.getSessionId());
					if (xmlToolbar != null) {
						XMLApplicationBuilder.getXMLApplicationBuilder()
						.getToolbarBuilder()
						.appendButtonBar(this, xmlToolbar);

						for (int i = 0; i < this.getComponentCount(); i++) {
							JComponent currenComponent = (JComponent) this.getComponent(i);
							if ((currenComponent instanceof IDynamicItem)
									&& ((IDynamicItem) currenComponent).isDynamic()) {

								if (currenComponent instanceof AbstractButton) {
									((AbstractButton) currenComponent).addActionListener(
											(ActionListener) ApplicationManager.getApplication().getToolBarListener());
								}
								if (currenComponent instanceof ApToolBarPopupButton) {
									ApToolBarPopupButton b = (ApToolBarPopupButton) currenComponent;
									for (int j = 0; j < b.getPopupComponentsCount(); j++) {
										if (b.getPopupComponentAt(j) instanceof AbstractButton) {
											((AbstractButton) b.getPopupComponentAt(j))
											.addActionListener((ActionListener) ApplicationManager.getApplication()
													.getToolBarListener());
										}
									}
								}
							}
						}
					}
				}
				this.dynamicloaded = true;
			} catch (Exception e) {
				ApplicationToolBar.logger.error(null, e);
			}
			this.revalidate();
			this.repaint();
		}
	}

	public void removeDynamicItems() {
		if (this.dynamicloaded) {
			try {
				for (int i = 0; i < this.getComponentCount(); i++) {
					JComponent currenComponent = (JComponent) this.getComponent(i);
					if ((currenComponent instanceof IDynamicItem) && ((IDynamicItem) currenComponent).isDynamic()) {
						Container parent = currenComponent.getParent();
						parent.remove(currenComponent);

						if (currenComponent instanceof AbstractButton) {
							((AbstractButton) currenComponent).removeActionListener(
									(ActionListener) ApplicationManager.getApplication().getToolBarListener());
						}
						if (currenComponent instanceof ApToolBarPopupButton) {
							ApToolBarPopupButton b = (ApToolBarPopupButton) currenComponent;
							for (int j = 0; j < b.getPopupComponentsCount(); j++) {
								if (b.getPopupComponentAt(j) instanceof AbstractButton) {
									((AbstractButton) b.getPopupComponentAt(j)).removeActionListener(
											(ActionListener) ApplicationManager.getApplication().getToolBarListener());
								}
							}
						}
					}
				}
				this.revalidate();
				this.repaint();
				this.dynamicloaded = false;
			} catch (Exception e) {
				ApplicationToolBar.logger.error(null, e);
			}
		}
	}

	@Override
	public void free() {
		if (this.originalComponentList != null) {
			this.originalComponentList.clear();
			this.originalComponentList = null;
		}
	}


	public void setDropLocation(TransferSupport support) {
		this.remove(this.locationComponent);
		int locationIndex = this.getLocationIndex(support);
		if (locationIndex >= 0) {
			this.add(this.locationComponent, this.getLocationIndex(support));
		}
		this.doLayout();
	}

	protected Transferable getTransferable(TransferSupport support) {
		Transferable transferable = support.getTransferable();
		try {
			return (Transferable) transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
		} catch (Exception e) {
			ApplicationToolBar.logger.error(null, e);
		}
		return null;
	}

	protected int getLocationIndex(TransferSupport support) {
		Point dropPoint = support.getDropLocation().getDropPoint();
		Transferable t = this.getTransferable(support);

		if (t == null) {
			return -1;
		}

		int member = this.getComponentCount();
		Component beforeComponent, afterComponent;
		if (member == 0) {
			return 0;
		}

		Rectangle bound = this.getComponent(0).getBounds();
		if (dropPoint.x < (bound.x + (bound.width / 2))) {
			if (t.equals(this.getComponent(0))) {
				return -1;
			}
			return 0;
		}

		bound = this.getComponent(member - 1).getBounds();
		if (dropPoint.x > (bound.x + (bound.width / 2))) {
			if (t.equals(this.getComponent(member - 1))) {
				return -1;
			}
			return member;
		}

		for (int i = 1; i < member; i++) {
			beforeComponent = this.getComponent(i - 1);
			afterComponent = this.getComponent(i);
			if (t.equals(beforeComponent) || t.equals(afterComponent)) {
				continue;
			}
			Rectangle b = beforeComponent.getBounds();
			Rectangle a = afterComponent.getBounds();
			int x1 = b.x + (b.width / 2);
			int x2 = a.x + (a.width / 2);
			Rectangle r = new Rectangle(x1, 0, x2 - x1, this.getHeight());
			if (r.contains(dropPoint)) {
				return i;
			}
		}
		return -1;
	}


	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		this.remove(this.locationComponent);
		this.validate();
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		this.remove(this.locationComponent);
		this.validate();
		this.saveConfiguration();
	}

	protected JPopupMenu createOptionMenu() {
		JPopupMenu rootMenu = new JPopupMenu();
		this.restoreMenuItem = new JMenuItem(ApplicationManager.getTranslation(ApplicationToolBar.RESTORE_MENU_ITEM_TEXT, this.resources));
		this.restoreMenuItem.addActionListener(e -> ApplicationToolBar.this.restoreOriginalLayout());

		rootMenu.add(this.restoreMenuItem);

		this.removeMenuItem = new JMenuItem(ApplicationManager.getTranslation(ApplicationToolBar.REMOVE_MENU_ITEM_TEXT, this.resources));
		this.removeMenuItem.addActionListener(e -> ApplicationToolBar.this.showRemoveDialog());

		rootMenu.add(this.removeMenuItem);

		this.addSeparatorItem = new JMenuItem(ApplicationManager.getTranslation(ApplicationToolBar.ADD_SEPARATOR_ITEM_TEXT, this.resources));
		this.addSeparatorItem.addActionListener(e -> {
			Map<Object, Object> parameters = DefaultXMLParametersManager.getParameters(ApToolBarSeparator.class.getName());
			ApplicationToolBar.this.add(new ApToolBarSeparator(parameters));
			ApplicationToolBar.this.validate();
		});

		rootMenu.add(this.addSeparatorItem);


		this.addFillerItem = new JMenuItem(ApplicationManager.getTranslation(ApplicationToolBar.ADD_FILLER_ITEM_TEXT, this.resources));
		this.addFillerItem.addActionListener(e -> {
			Map<Object, Object> parameters = DefaultXMLParametersManager.getParameters(ApToolBarFiller.class.getName());
			ApplicationToolBar.this.add(new ApToolBarFiller(parameters));
			ApplicationToolBar.this.validate();
		});

		rootMenu.add(this.addFillerItem);

		return rootMenu;
	}

	public JPopupMenu getOptionMenu() {
		if (this.optionMenu == null) {
			this.optionMenu = this.createOptionMenu();
		}

		return this.optionMenu;
	}

	public void restoreOriginalLayout() {
		this.removeAll();
		for (Component current : this.originalComponentList) {
			this.add(current);
		}

		this.validate();
		this.applicationPreferences.setPreference(this.getUser(), BasicApplicationPreferences.APP_TOOL_BAR_CONFIG, null);
		this.applicationPreferences.savePreferences();
	}


	protected void showRemoveDialog() {
		EJDialog dialog = new EJDialog(ApplicationManager.getApplication().getFrame(),
				ApplicationManager.getTranslation(ApplicationToolBar.REMOVE_COMPONENT_WINDOW_TITLE, this.resources), true);
		JPanel panel = this.createRemovePanel();
		dialog.setContentPane(panel);
		dialog.pack();
		ApplicationManager.center(dialog);
		dialog.setVisible(true);
	}

	protected JPanel createRemovePanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		final DefaultListModel model = new DefaultListModel();
		List<String> attrList = this.retrieveIdentifiedElements();
		for (String current : attrList) {
			model.addElement(new SelectableItem(current));
		}
		JList elementList = new JList(model);
		elementList.setCellRenderer(new SelectableItemListCellRenderer(this.resources));
		elementList.addMouseListener(new SelectableItemMouseListener());
		elementList.setVisibleRowCount(20);
		mainPanel.add(new JScrollPane(elementList));

		JPanel panelButton = new JPanel(new GridBagLayout());
		JButton acceptButton = new JButton(ApplicationManager.getTranslation("application.accept", this.resources));
		acceptButton.addActionListener(e -> {
			int elements = model.getSize();
			for (int i = elements - 1; i >= 0; i--) {
				SelectableItem item = (SelectableItem) model.getElementAt(i);
				if (item.isSelected()) {
					ApplicationToolBar.this.remove(i);
				}
			}
			ApplicationToolBar.this.validate();
			ApplicationToolBar.this.saveConfiguration();
			Window w = SwingUtilities.getWindowAncestor((JComponent) e.getSource());
			w.setVisible(false);

		});

		JButton cancelButton = new JButton(ApplicationManager.getTranslation("application.cancel", this.resources));
		cancelButton.addActionListener(e -> {
			Window w = SwingUtilities.getWindowAncestor((JComponent) e.getSource());
			w.setVisible(false);
		});

		panelButton.add(acceptButton, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.EAST,
				GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0));
		panelButton.add(cancelButton, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.WEST,
				GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0));

		mainPanel.add(panelButton, BorderLayout.SOUTH);
		return mainPanel;
	}

	protected List<String> retrieveIdentifiedElements() {
		List<String> list = new ArrayList<>();
		int total = this.getComponentCount();

		for (int i = 0; i < total; i++) {
			Component component = this.getComponent(i);
			if (component instanceof IdentifiedElement) {
				list.add(((IdentifiedElement) component).getAttribute().toString());
			}
		}
		return list;
	}

	@Override
	public void initPreferences(ApplicationPreferences ap, String user) {
		this.applicationPreferences = ap;
		this.userPrefs = user;

		int total = this.getComponentCount();
		for (int i = 0; i < total; i++) {
			Component component = this.getComponent(i);
			this.originalComponentList.add(component);
		}

		String config = this.applicationPreferences.getPreference(user,
				BasicApplicationPreferences.APP_TOOL_BAR_CONFIG);
		if (config != null) {
			this.loadConfiguration(config);
		}
	}

	/**
	 * Returns the user name.
	 * @return the user name
	 */
	protected String getUser() {
		// if (this.locator instanceof ClientReferenceLocator) {
		// return ((ClientReferenceLocator) this.locator).getUser();
		// } else {
		return this.userPrefs;
		// }
	}

	protected void loadConfiguration(String configuration) {
		StringTokenizer tokens = new StringTokenizer(configuration, ";");
		List<Component> finalPosition = new ArrayList<>();

		this.removeAll();

		while (tokens.hasMoreTokens()) {
			String attr = tokens.nextToken();
			boolean foundComponent = false;
			for (Component current : this.originalComponentList) {

				if (current instanceof IdentifiedElement) {
					IdentifiedElement element = (IdentifiedElement) current;
					if (attr.equals(element.getAttribute()) && !finalPosition.contains(current)) {
						finalPosition.add(current);
						this.add(current);
						foundComponent = true;
						break;
					}
				}
			}
			if (!foundComponent) {
				JComponent newComponent = this.createComponent(attr);
				if (newComponent != null) {
					finalPosition.add(newComponent);
					this.add(newComponent);
				}
			}
		}

		this.validate();
	}

	protected JComponent createComponent(String attr) {
		if ("aptoolbarseparator".equals(attr)) {
			Map<Object, Object> parameters = DefaultXMLParametersManager.getParameters(ApToolBarSeparator.class.getName());
			return new ApToolBarSeparator(parameters);
		} else if ("aptoolbarfiller".equals(attr)) {
			Map<Object, Object> parameters = DefaultXMLParametersManager.getParameters(ApToolBarFiller.class.getName());
			return new ApToolBarFiller(parameters);
		} else {
			// Find component in menus
			ApplicationMenuBar menuBar = (ApplicationMenuBar) ApplicationManager.getApplication().getMenu();
			JMenuItem menuItem = menuBar.getMenuItem(attr);
			if (menuItem instanceof MenuItem) {
				MenuItem current = (MenuItem) menuItem;
				return current.createAppToolBarButton();
			}
		}

		return null;
	}

	protected void saveConfiguration() {
		int total = this.getComponentCount();
		StringBuilder configuration = new StringBuilder();
		for (int i = 0; i < total; i++) {
			Component component = this.getComponent(i);
			if (component instanceof IdentifiedElement) {
				if (configuration.length() > 0) {
					configuration.append(";");
				}
				configuration.append(((IdentifiedElement) component).getAttribute());
			}
		}

		this.applicationPreferences.setPreference(this.getUser(), BasicApplicationPreferences.APP_TOOL_BAR_CONFIG,
				configuration.toString());
		this.applicationPreferences.savePreferences();
	}

}
