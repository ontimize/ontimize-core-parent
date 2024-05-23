package com.ontimize.gui.manager;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.FreeableUtils;
import com.ontimize.gui.IDetailForm;
import com.ontimize.gui.TabbedDetailForm;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.preferences.BasicApplicationPreferences;
import com.ontimize.util.ParseUtils;
import com.ontimize.util.swing.EJFrame;

public class TabbedFormManager extends BaseFormManager implements ITabbedFormManager {

	private static final Logger		logger					= LoggerFactory.getLogger(TabbedFormManager.class);

	protected FormTabbedPane		tabbedPane;

	protected Form					mainForm;

	protected static final String	DRAG_TO_FRAME_PARAMETER	= "dragtoframe";

	protected boolean				dragToFrame				= true;

	public static boolean			defaultDragToFrame		= true;

	public TabbedFormManager(Map<Object, Object> parameters) throws Exception {
		super(parameters);
	}

	@Override
	public void init(Map<Object, Object> parameters) throws Exception {
		super.init(parameters);
		this.setDragToFrame(ParseUtils.getBoolean((String) parameters.get(TabbedFormManager.DRAG_TO_FRAME_PARAMETER), TabbedFormManager.defaultDragToFrame));
	}

	/**
	 * Shows the form which file name is passed as entry parameter
	 * 
	 * @param form
	 * @see #showForm
	 * @return true if the form is showed.
	 */
	@Override
	public boolean showFormInEDTh(String form) {
		if (!this.isLoaded()) {
			this.load();
		}
		if (this.checkVisiblePermission(form)) {
			if (!this.loadedList.contains(form)) {
				this.loadFormInEDTh(form);
			}
			this.tabbedPane.setSelectedIndex(0);
			this.currentForm = form;
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected JComponent createCenterPanel() {
		this.tabbedPane = new FormTabbedPane();
		this.tabbedPane.setDragToFrame(this.dragToFrame);
		if (this.getResourceBundle() != null) {
			this.tabbedPane.setResourceBundle(this.getResourceBundle());
		}
		return this.tabbedPane;
	}

	protected void setDragToFrame(boolean drag) {
		this.dragToFrame = drag;
		if (this.tabbedPane != null) {
			this.tabbedPane.setDragToFrame(this.dragToFrame);
		}
	}

	@Override
	public void addFormToContainer(JPanel panelForm, String formName) {
		int index = this.tabbedPane.getTabCount();

		this.tabbedPane.addTab(formName, panelForm);

		this.tabbedPane.setSelectedIndex(index);
		if ((index == 0) && this.formReferenceList.containsKey(formName)) {
			this.setMainForm(this.formReferenceList.get(formName));
		}
	}

	@Override
	public Form getMainForm() {
		return this.mainForm;
	}

	protected void setMainForm(Form form) {
		this.mainForm = form;
		String title = this.mainForm.getFormTitle() != null ? this.mainForm.getFormTitle() : this.mainForm.getEntityName();
		if (title == null) {
			title = this.mainForm.getArchiveName();
		}
		this.setTitleAt(0, ApplicationManager.getTranslation(title, this.getResourceBundle()));
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		super.setResourceBundle(resources);
		if (this.mainForm != null) {
			String title = this.mainForm.getFormTitle() != null ? this.mainForm.getFormTitle() : this.mainForm.getEntityName();
			if (title == null) {
				title = this.mainForm.getArchiveName();
			}
			this.setTitleAt(0, ApplicationManager.getTranslation(title, resources));
		}

		if (this.tabbedPane != null) {
			this.tabbedPane.setResourceBundle(resources);
		}
	}

	@Override
	public void removeTab(int index) {
		this.tabbedPane.remove(index);
	}

	@Override
	public int indexOfComponent(Component component) {
		return this.tabbedPane.indexOfComponent(component);
	}

	@Override
	public int indexOfKeys(Map<?, ?> keyValues) {
		int size = this.tabbedPane.getTabCount();
		// i==0 is use by result table.
		for (int i = 1; i < size; i++) {
			Component component = this.tabbedPane.getComponentAt(i);
			if (component instanceof IDetailForm) {
				IDetailForm detailForm = (IDetailForm) component;
				Form currentForm = detailForm.getForm();
				List<?> keyAttrs = currentForm.getKeys();
				boolean check = true;
				for (Object key : keyAttrs) {
					if (keyValues.containsKey(key)) {
						Object keyValue = keyValues.get(key);
						if ((keyValue instanceof List) && (((List<?>) keyValue).size() > 0)) {
							keyValue = ((List<?>) keyValues.get(key)).get(0);
						}
						if (!keyValue.equals(currentForm.getDataFieldValue(key.toString()))) {
							check = false;
							break;
						}
					} else {
						check = false;
						break;
					}
				}
				if (check) {
					return i;
				}
			}
		}
		return -1;
	}

	@Override
	public void showTab(int index) {
		this.tabbedPane.setSelectedIndex(index);
	}

	@Override
	public void setTitleAt(int index, String text) {
		this.tabbedPane.setTitleAt(index, text);
	}

	protected static class FormTabbedTransferHandler extends TransferHandler {

		protected FormTabbedPane tabbedPane;

		public FormTabbedTransferHandler(FormTabbedPane formTabbedPane) {
			this.tabbedPane = formTabbedPane;
		}

		@Override
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			super.exportAsDrag(comp, e, action);
		}

		@Override
		public boolean canImport(TransferSupport support) {
			if (support.getComponent() instanceof FormTabbedPane) {
				FormTabbedPane formTabbedPane = (FormTabbedPane) support.getComponent();
				int selectedIndex = formTabbedPane.getSelectedIndex();
				if (selectedIndex <= 0) {
					return false;
				}

				// if
				// (!(tabComponent.getBounds().contains(support.getDropLocation().getDropPoint())))
				// {
				// return true;
				// }
			}
			return true;
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				Object o = t.getTransferData(FormTabbedTransferable.df);
				if (o instanceof FormTabbedTransferable) {
					FormTabbedTransferable transferable = (FormTabbedTransferable) o;
					Frame currentFrame = this.tabbedPane.createTabAtFrame(transferable.getIndex());
					currentFrame.setVisible(true);
					return true;
				}
			} catch (Exception ex) {
				TabbedFormManager.logger.error("{}", ex.getMessage(), ex);
			}
			return false;
		}

		// Export Methods
		@Override
		public int getSourceActions(JComponent current) {
			if (current instanceof FormTabbedPane) {
				return TransferHandler.MOVE;
			}
			return super.getSourceActions(current);
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			if (c instanceof FormTabbedPane) {
				FormTabbedPane tabbedPane = (FormTabbedPane) c;
				if (tabbedPane.getSelectedIndex() > 0) {
					return new FormTabbedTransferable(tabbedPane.getSelectedIndex());
				}
			}
			return super.createTransferable(c);
		}

	}

	// This class is necessary because the PropertyResourceBundle isn't
	// serializable.
	protected static class FormTabbedTransferable implements Transferable {

		public static DataFlavor df = null;
		static {
			try {
				FormTabbedTransferable.df = new DataFlavor(Class.forName("com.ontimize.gui.manager.TabbedFormManager$FormTabbedTransferable"), "FormTabbedTransferable");
			} catch (ClassNotFoundException e) {
				TabbedFormManager.logger.error(null, e);
			}
		}

		protected int index;

		public FormTabbedTransferable(int selectedIndex) {
			this.index = selectedIndex;
		}

		public int getIndex() {
			return this.index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { FormTabbedTransferable.df };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return FormTabbedTransferable.df.equals(flavor);
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (this.isDataFlavorSupported(flavor)) {
				return this;
			}
			return null;
		}

	}

	@Override
	public List<JFrame> getFrameList() {
		return this.tabbedPane.frameList;
	}

	protected static class FormTabbedPane extends JTabbedPane implements Internationalization, MouseMotionListener {

		/**
		 * The name of class. Used by L&F to put UI properties.
		 *
		 */
		private static String		NAME						= "FormTabbedPane";

		private static final String	UICLASSID					= "FormTabbedPaneUI";

		protected JPopupMenu		tabOptionMenu;

		protected JMenuItem			menuClose;

		protected JMenuItem			menuCloseOthers;

		protected JMenuItem			menuCloseAll;

		protected String			menuCloseBundleKey			= "tabbedformmanager.close";

		protected String			menuCloseOthersBundleKey	= "tabbedformmanager.closeothers";

		protected String			menuCloseAllBundleKey		= "tabbedformmanager.closeall";

		protected int				selectedMenuIndex			= -1;

		protected ResourceBundle	bundle;

		protected List<JFrame>		frameList;

		protected boolean			dragToFrame					= true;

		public FormTabbedPane() {
			super();
			KeyStroke altF = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.CTRL_DOWN_MASK, false);
			Action control_tab_action = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int index = FormTabbedPane.this.getSelectedIndex();
					int total = FormTabbedPane.this.getTabCount();
					index++;
					if (index >= total) {
						index = 0;
					}
					FormTabbedPane.this.setSelectedIndex(index);
				}
			};
			this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altF, "CONTROL_TAB");
			this.getActionMap().put("CONTROL_TAB", control_tab_action);
			this.setTransferHandler(new FormTabbedTransferHandler(this));
			this.addMouseMotionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);

			this.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent event) {
					if (SwingUtilities.isRightMouseButton(event)) {
						TabbedPaneUI ui = FormTabbedPane.this.getUI();
						int tab = ui.tabForCoordinate(FormTabbedPane.this, event.getX(), event.getY());
						if (tab > 0) {
							FormTabbedPane.this.selectedMenuIndex = tab;
							FormTabbedPane.this.showTabOptionMenu(event);
						}

					}
				}
			});
		}

		@Override
		public String getName() {
			return FormTabbedPane.NAME;
		}

		@Override
		public String getUIClassID() {
			if (ApplicationManager.useOntimizePlaf) {
				return FormTabbedPane.UICLASSID;
			} else {
				return super.getUIClassID();
			}
		}

		@Override
		public void addTab(String title, Component component) {
			int index = this.getTabCount();
			super.addTab(title, component);
			if (index > 0) {
				this.setTabComponentAt(index, new ButtonTabComponent(this));
			} else {
				this.setTabComponentAt(index, new ButtonTabComponent(this, false));
			}
		}

		@Override
		public void setTabComponentAt(int index, Component component) {
			super.setTabComponentAt(index, component);
		}

		/**
		 * Returns the tooltip text for the component determined by the mouse event location.
		 * 
		 * @param event
		 *            the <code>MouseEvent</code> that tells where the cursor is lingering
		 * @return the <code>String</code> containing the tooltip text
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			if (event.isControlDown() && event.isAltDown() && event.isShiftDown() && (this.ui != null)) {
				return this.createDeveloperTooltip(event);
			}
			return super.getToolTipText(event);
		}

		/**
		 * Creates the developer tooltip with information about the formmanager, interaction manager...
		 * 
		 * @param event
		 *            the event
		 * @return the string
		 */
		protected String createDeveloperTooltip(MouseEvent event) {
			int index = ((TabbedPaneUI) this.ui).tabForCoordinate(this, event.getX(), event.getY());
			JComponent component = (JComponent) FormTabbedPane.this.getComponentAt(index);
			if ((component != null) && (component.getComponents()[0] != null) && (component.getComponents()[0] instanceof Form)) {
				Form form = (Form) component.getComponents()[0];
				StringBuilder buffer = new StringBuilder();
				buffer.append("<html>");
				try {
					URL url = form.getFormManager().getURL(form.getArchiveName());
					buffer.append("<B>form:  </B>").append(url != null ? url.toString() : form.getArchiveName());
				} catch (Exception ex) {
					TabbedFormManager.logger.trace(null, ex);
				}
				try {
					buffer.append("<br>");
					buffer.append("<B>im:  </B>").append(form.getInteractionManager().getClass().getName());
				} catch (Exception ex1) {
					TabbedFormManager.logger.trace(null, ex1);
				}
				buffer.append("<br>");
				buffer.append("<B>entity:  </B>").append(form.getEntityName());

				if (form.getFormManager() != null) {
					buffer.append("<br>");
					buffer.append("<B>fm:  </B>").append(form.getFormManager().getId());
					buffer.append("<br>");
				}
				buffer.append("</html>");
				return buffer.toString();
			}
			return null;
		}

		@Override
		public void setComponentLocale(Locale l) {

		}

		@Override
		public void setResourceBundle(ResourceBundle resourceBundle) {
			this.bundle = resourceBundle;
			this.applyResourceBundle();
		}

		@Override
		public List<String> getTextsToTranslate() {
			return null;
		}

		public boolean isDragToFrame() {
			return this.dragToFrame;
		}

		public void setDragToFrame(boolean dragToFrame) {
			this.dragToFrame = dragToFrame;
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if ((this.getTransferHandler() != null) && this.isDragToFrame()) {
				TransferHandler th = this.getTransferHandler();
				th.exportAsDrag(this, event, TransferHandler.MOVE);
			}
		}

		@Override
		public void mouseMoved(MouseEvent event) {}

		protected void applyResourceBundle() {
			if (this.tabOptionMenu != null) {
				this.menuClose.setText(ApplicationManager.getTranslation(this.menuCloseBundleKey, this.bundle));
				this.menuCloseAll.setText(ApplicationManager.getTranslation(this.menuCloseAllBundleKey, this.bundle));
				this.menuCloseOthers.setText(ApplicationManager.getTranslation(this.menuCloseOthersBundleKey, this.bundle));
			}

			int tabCount = this.getTabCount();
			for (int i = 0; i < tabCount; i++) {
				Component component = this.getComponentAt(i);
				if (component instanceof IDetailForm) {
					Form currentForm = ((IDetailForm) component).getForm();
					currentForm.setResourceBundle(this.bundle);
				}
			}

			if (this.frameList != null) {
				for (JFrame current : this.frameList) {
					Component[] components = current.getContentPane().getComponents();
					for (Component component : components) {
						if (component instanceof Internationalization) {
							((Internationalization) component).setResourceBundle(this.bundle);
						}
					}
				}
			}
		}

		protected void createTabOptionMenu() {
			this.tabOptionMenu = new JPopupMenu();
			this.menuClose = new JMenuItem(this.menuCloseBundleKey);
			this.menuCloseAll = new JMenuItem(this.menuCloseAllBundleKey);
			this.menuCloseOthers = new JMenuItem(this.menuCloseOthersBundleKey);

			this.tabOptionMenu.add(this.menuClose);
			this.tabOptionMenu.add(this.menuCloseOthers);
			this.tabOptionMenu.add(this.menuCloseAll);

			this.applyResourceBundle();

			this.menuCloseAll.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int tabCount = FormTabbedPane.this.getTabCount();
					if (tabCount > 1) {
						for (int i = tabCount - 1; i > 0; i--) {
							FormTabbedPane.this.remove(i);
						}
					}
				}
			});

			this.menuClose.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (FormTabbedPane.this.selectedMenuIndex > 0) {
						FormTabbedPane.this.remove(FormTabbedPane.this.selectedMenuIndex);
					}
				}
			});

			this.menuCloseOthers.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int tabCount = FormTabbedPane.this.getTabCount();
					if ((tabCount > 2) && (FormTabbedPane.this.selectedMenuIndex > 0)) {
						Component selectedComponent = FormTabbedPane.this.getTabComponentAt(FormTabbedPane.this.selectedMenuIndex);
						for (int i = tabCount - 1; i > 0; i--) {
							Component currentComponent = FormTabbedPane.this.getTabComponentAt(i);
							if (selectedComponent.equals(currentComponent)) {
								continue;
							} else {
								FormTabbedPane.this.remove(i);
							}
						}
					}
				}
			});

		}

		public void showTabOptionMenu(MouseEvent e) {
			if (e.getSource() instanceof FormTabbedPane) {
				if (this.tabOptionMenu == null) {
					this.createTabOptionMenu();
				}
				this.tabOptionMenu.show((Component) e.getSource(), e.getX(), e.getY());
			}
		}

		protected EJFrame createFrame(String title) {
			EJFrame frame = new EJFrame(title);
			return frame;
		}

		protected Frame createTabAtFrame(int index) {
			final TabbedDetailForm detailForm = (TabbedDetailForm) this.getComponentAt(index);
			EJFrame frame = this.createFrame(detailForm.getTitle());
			frame.setSizePositionPreference(this.getDetailFormSizePreferenceKey(detailForm));
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			// Retrieve Images
			Window w = SwingUtilities.getWindowAncestor(this);
			if (w != null) {
				frame.setIconImages(w.getIconImages());
			}
			((ITabbedFormManager) detailForm.getForm().getFormManager()).removeTab(index);
			frame.getContentPane().add(detailForm);
			frame.pack();

			if (this.frameList == null) {
				this.frameList = new ArrayList<JFrame>();
			}
			this.frameList.add(frame);
			frame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					if (FormTabbedPane.this.frameList.contains(e.getSource())) {
						FormTabbedPane.this.frameList.remove(e.getSource());
					}
					FreeableUtils.freeComponent(detailForm);
				}

				@Override
				public void windowClosed(WindowEvent e) {}
			});
			return frame;
		}

		public String getDetailFormSizePreferenceKey(TabbedDetailForm detailForm) {

			StringBuilder builder = new StringBuilder();
			builder.append(BasicApplicationPreferences.DETAIL_DIALOG_SIZE_POSITION);
			builder.append("_");
			builder.append(detailForm.getTable().getFormName());
			builder.append("_");
			builder.append(detailForm.getTable().getEntityName());
			return builder.toString();
		}

	}

	public static class ButtonTabComponent extends JPanel {

		protected final transient JTabbedPane pane;

		public ButtonTabComponent(final JTabbedPane pane) {
			this(pane, true);
		}

		public ButtonTabComponent(final JTabbedPane pane, boolean close) {
			// unset default FlowLayout' gaps
			super(new FlowLayout(FlowLayout.LEFT, 0, 0));
			if (pane == null) {
				throw new NullPointerException("TabbedPane is null");
			}
			this.pane = pane;
			this.setOpaque(false);

			// make JLabel read titles from JTabbedPane
			JLabel label = new JLabel() {

				@Override
				public String getText() {
					int i = pane.indexOfTabComponent(ButtonTabComponent.this);
					if (i != -1) {
						return pane.getTitleAt(i);
					}
					return null;
				}

				@Override
				public String getName() {
					return "FormTabbedPaneTab.Label";
				}
			};

			this.add(label);
			// add more space between the label and the button
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
			if (close) {
				// tab button
				JButton button = new TabButton();
				this.add(button, BorderLayout.EAST);
			}
			// add more space to the top of the component
			this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			// this.addMouseListener(new MouseAdapter() {
			// @Override
			// public void mouseClicked(MouseEvent e) {
			// if (SwingUtilities.isRightMouseButton(e)) {
			// ((FormTabbedPane) pane).showTabOptionMenu(e);
			// } else {
			// super.mouseClicked(e);
			// }
			// }
			// });
		}

		private class TabButton extends JButton implements ActionListener {

			public TabButton() {
				int size = 17;
				this.setPreferredSize(new Dimension(size, size));
				// this.setToolTipText("close this tab");
				// Make the button looks the same for all Laf's
				this.setUI(new BasicButtonUI());
				// Make it transparent
				this.setContentAreaFilled(false);
				// No need to be focusable
				this.setFocusable(false);
				this.setBorder(BorderFactory.createEtchedBorder());
				this.setBorderPainted(false);
				// Making nice rollover effect
				// we use the same listener for all buttons
				this.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseEntered(MouseEvent e) {
						Component component = e.getComponent();
						if (component instanceof AbstractButton) {
							AbstractButton button = (AbstractButton) component;
							button.setBorderPainted(true);
						}
					}

					@Override
					public void mouseExited(MouseEvent e) {
						Component component = e.getComponent();
						if (component instanceof AbstractButton) {
							AbstractButton button = (AbstractButton) component;
							button.setBorderPainted(false);
						}
					}
				});

				this.setRolloverEnabled(true);
				// Close the proper tab by clicking the button
				this.addActionListener(this);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = ButtonTabComponent.this.pane.indexOfTabComponent(ButtonTabComponent.this);
				if (i != -1) {
					// Component tabComponentAt = ButtonTabComponent.this.pane.getTabComponentAt(i);
					TabbedDetailForm tabbedDetailForm = (TabbedDetailForm) ButtonTabComponent.this.pane.getComponentAt(i);
					tabbedDetailForm.hideDetailForm();
				}
			}

			// // we don't want to update UI for this button
			// @Override
			// public void updateUI() {}

			// paint the cross
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				// shift the image for pressed buttons
				if (this.getModel().isPressed()) {
					g2.translate(1, 1);
				}
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				// if (this.getModel().isRollover()) {
				// g2.setColor(Color.MAGENTA);
				// }
				int delta = 6;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawLine(delta, delta, this.getWidth() - delta - 1, this.getHeight() - delta - 1);
				g2.drawLine(this.getWidth() - delta - 1, delta, delta, this.getHeight() - delta - 1);
				g2.dispose();
			}

		}

	}

}
