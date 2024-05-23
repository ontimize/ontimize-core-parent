package com.ontimize.gui.container;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Form;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.field.AccessForm;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.jee.common.db.Entity;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/*
 * When a component is clicked then query the associated entity, using the first level container key and the second level if the form parameter was specified in the object building
 */

public class TwoLevelContainer extends JPanel implements OpenDialog, DataComponent, MouseListener, AccessForm, Freeable {

	private static final Logger		logger			= LoggerFactory.getLogger(TwoLevelContainer.class);

	protected String				entity			= null;

	protected String				formName		= null;

	protected Map<Object, Object>	value			= null;

	protected boolean				required		= false;

	protected Object				attribute		= null;

	protected boolean				modificable		= false;

	protected String				labelText		= "";

	protected String				renderClass		= null;

	protected Object[]				parameters		= null;

	protected int					preferredWidth	= 300;

	protected int					hSeparation		= 15;

	protected int					vSeparation		= 15;

	protected JPanel				auxPanel		= new JPanel();

	protected Map<Object, Object>	panels			= new HashMap<>();

	protected Form					form			= null;

	protected Form					parentForm		= null;

	protected Frame					parentFrame		= null;

	protected JDialog				detailWindow	= null;

	protected String				firstLevelKey	= null;

	protected String				secondLevelKey	= null;

	public class FirstLevelPanel extends JPanel {

		Object				id			= null;

		Map<Object, Object>	components	= new HashMap<>();

		EmptyBorder			margins		= new EmptyBorder(0, 0, 0, 0);

		TitledBorder		titleBorder	= null;

		JPanel				innerPanel	= new JPanel(new GridBagLayout()) {

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(TwoLevelContainer.this.preferredWidth, d.height);
			}
		};

		public FirstLevelPanel(Object panelId) {
			this.id = panelId;
			this.titleBorder = new TitledBorder(this.id.toString());
			this.setBorder(new CompoundBorder(this.titleBorder, new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(15, 15, 15, 15))));
			this.innerPanel.setBorder(this.margins);
			this.setLayout(new BorderLayout());
			this.add(this.innerPanel);
			this.innerPanel.setLayout(new GridLayout(0, 3, TwoLevelContainer.this.hSeparation, TwoLevelContainer.this.vSeparation));
			this.addComponentListener(new ComponentAdapter() {

				@Override
				public void componentResized(ComponentEvent e) {
					FirstLevelPanel.this.recalculateLayoutParameters();
				}
			});
		}

		public void recalculateLayoutParameters() {
			// Calculate. Preferred horizontal size in pixels
			if (this.components.isEmpty()) {
				return;
			}
			Enumeration<?> enumKeys = Collections.enumeration(this.components.keySet());
			Object oKey = enumKeys.nextElement();
			int componentWidth = ((Component) this.components.get(oKey)).getPreferredSize().width;
			// Number of components that can fit in
			int componentNumber = Math.max(this.innerPanel.getWidth(), TwoLevelContainer.this.preferredWidth) / (componentWidth + TwoLevelContainer.this.hSeparation);
			if (componentNumber > 0) {
				componentNumber = Math.min(this.components.size(), componentNumber);
				((GridLayout) this.innerPanel.getLayout()).setColumns(componentNumber);
				int totalSeparation = TwoLevelContainer.this.hSeparation * (componentNumber - 1);
				// The extra space without the separation
				int extraPixeles = Math.max(this.innerPanel.getWidth(), TwoLevelContainer.this.preferredWidth) - (componentNumber * componentWidth);
				int finalExtraPixeles = extraPixeles - totalSeparation;
				// Put the space as insets
				this.margins = new EmptyBorder(0, 0, 0, finalExtraPixeles);
				this.innerPanel.setBorder(this.margins);
			} else {
				((GridLayout) this.getLayout()).setColumns(1);
			}
		}

		public Object getId() {
			return this.id;
		}

		public boolean isId(Object id) {
			if (id.equals(this.id)) {
				return true;
			} else {
				return false;
			}
		}

		public void removeDataComponent(Object id) {
			for (int i = 0; i < this.innerPanel.getComponentCount(); i++) {
				Component c = this.innerPanel.getComponent(i);
				c.removeMouseListener(TwoLevelContainer.this);
				if (c instanceof DataComponent) {
					if (((DataComponent) c).getAttribute().equals(id)) {
						this.innerPanel.remove(i);
						this.components.remove(id);
						return;
					}
				}
			}
		}

		public DataComponent addDataComponent(final Object id) {
			try {
				Class<?> renderComponentClass = Class.forName(TwoLevelContainer.this.renderClass);
				Class<?>[] c = { Hashtable.class };
				Constructor<?> constructor = renderComponentClass.getConstructor(c);
				((Map<Object, Object>) TwoLevelContainer.this.parameters[0]).put("attr", id);
				Object comp = constructor.newInstance(TwoLevelContainer.this.parameters);
				this.components.put(id, comp);
				this.innerPanel.add((Component) comp);
				((Component) comp).addMouseListener(TwoLevelContainer.this);
				return (DataComponent) comp;
			} catch (Exception e) {
				TwoLevelContainer.logger.error(null, e);
				this.components.remove(id);
				this.removeDataComponent(id);
				return null;
			}
		}

		public DataComponent getDataComponent(Object id) {
			return (DataComponent) this.components.get(id);
		}

		public boolean setDataComponentValue(Object id, Object value) {
			// If it does not exist then create it
			DataComponent comp = this.getDataComponent(id);
			if (comp == null) {
				comp = this.addDataComponent(id);
				comp.setValue(value);
				return true;
			} else {
				comp.setValue(value);
				return false;
			}
		}

		public boolean setNewValue(Map<?, ?> data) {
			// For each current component, if it is no in the new ones then
			// remove it
			Enumeration<?> currentKeys = Collections.enumeration(this.components.keySet());
			while (currentKeys.hasMoreElements()) {
				Object oKey = currentKeys.nextElement();
				if (!data.containsKey(oKey)) {
					this.removeDataComponent(oKey);
				}
			}
			boolean newExist = false;
			// Now set the data
			Enumeration<?> newKeys = Collections.enumeration(data.keySet());
			while (newKeys.hasMoreElements()) {
				Object oKey = newKeys.nextElement();
				Object oValue = data.get(oKey);
				boolean isNew = this.setDataComponentValue(oKey, oValue);
				if (isNew) {
					newExist = true;
				}
			}
			return newExist;
		}

	}

	public TwoLevelContainer(Map<Object, Object> parameters) {
		this.setLayout(new GridBagLayout());
		this.init(parameters);
		this.parameters = new Object[1];
		this.parameters[0] = parameters;
		this.setBorder(new TitledBorder("NO_DATA_FOUND"));
	}

	@Override
	public void init(Map<Object, Object> parameters) {
		// Parameters reading
		Object entity = parameters.get("entity");
		if (entity == null) {
			TwoLevelContainer.logger.debug(this.getClass().toString() + ": Parameter 'entity' required");
		} else {
			this.entity = entity.toString();
		}

		Object containerwidth = parameters.get("containerwidth");
		if (containerwidth == null) {
		} else {
			try {
				this.preferredWidth = Integer.parseInt(containerwidth.toString());
			} catch (Exception e) {
				TwoLevelContainer.logger.trace(null, e);
				this.preferredWidth = 400;
			}
		}

		Object attr = parameters.get("attr");
		if (attr == null) {
			TwoLevelContainer.logger.debug(this.getClass().toString() + ": Parameter 'attr' required");
		} else {
			this.attribute = attr;
		}
		parameters.remove("attr");

		Object required = parameters.get("required");
		if (required != null) {
			if (required.equals("yes")) {
				this.required = true;
			} else {
				this.required = false;
			}
		}

		Object render = parameters.get("render");
		if (render != null) {
			this.renderClass = render.toString();
		} else {
			TwoLevelContainer.logger.debug("Render component class must be specified. A constructor with a hasthtable parameter must exist");
		}

		Object form = parameters.get("form");
		if (form == null) {
		} else {
			this.formName = form.toString();
		}

		Object parentkey = parameters.get("parentkey");
		if (parentkey == null) {
		} else {
			this.firstLevelKey = parentkey.toString();
		}

		Object key = parameters.get("key");
		if (key == null) {
		} else {
			this.secondLevelKey = key.toString();
		}
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		if (parentLayout instanceof GridBagLayout) {
			return new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);
		} else {
			return null;
		}
	}

	@Override
	public void setValue(Object value) {
		if (this.renderClass == null) {
			return;
		}
		boolean someNew = false;
		if (value instanceof Map) {
			this.value = (Map<Object, Object>) value;
			if (this.value.isEmpty()) {
				this.setBorder(new TitledBorder("NO_HAY_DATOS"));
				// Remove all panels
				Enumeration<?> currentPanelKeys = Collections.enumeration(this.panels.keySet());
				while (currentPanelKeys.hasMoreElements()) {
					Object oPanelKey = currentPanelKeys.nextElement();
					this.removePanel(oPanelKey);
				}
				return;
			} else {
				this.setBorder(null);
			}
			// Now we have to create the components to show the results.
			// For each key in the main hashtable, we check if the component
			// exists.
			// Check if the new data hashtable has the keys of the first level
			// panels
			Enumeration<?> currentPanelKeys = Collections.enumeration(this.panels.keySet());
			while (currentPanelKeys.hasMoreElements()) {
				Object oPanelKey = currentPanelKeys.nextElement();
				if (!this.value.containsKey(oPanelKey)) {
					this.removePanel(oPanelKey);
				}
			}

			Enumeration<?> enumKeys = Collections.enumeration(this.value.keySet());
			while (enumKeys.hasMoreElements()) {
				Object oKey = enumKeys.nextElement();
				FirstLevelPanel panel = this.getPanel(oKey);
				// If it exists, then look the components
				if (panel != null) {
					Map<?, ?> dataComponents = (Map<?, ?>) this.value.get(oKey);
					boolean newExist = panel.setNewValue(dataComponents);
					if (newExist) {
						someNew = true;
					}
				} else {
					FirstLevelPanel p = this.addPanel(oKey);
					p.setNewValue((Map<?, ?>) this.value.get(oKey));
					someNew = true;
				}
			}
		} else {
			this.deleteData();
			someNew = true;
		}

		if (someNew) {
			this.setVisible(false);
			this.doLayout();
			SwingUtilities.invokeLater(() -> {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					TwoLevelContainer.logger.trace(null, e);
				}
				TwoLevelContainer.this.setVisible(true);
			});
		} else {
			this.repaint();
		}
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public String getLabelComponentText() {
		return this.labelText;
	}

	@Override
	public void deleteData() {
		this.removeAll();
		this.value = null;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isModifiable() {
		return this.modificable;
	}

	@Override
	public void setModifiable(boolean modif) {
		this.modificable = modif;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public int getSQLDataType() {
		return java.sql.Types.SMALLINT;
	}

	@Override
	public Object getAttribute() {
		return this.attribute;
	}

	@Override
	public boolean isRequired() {
		return this.required;
	}

	@Override
	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setResourceBundle(ResourceBundle res) {}

	@Override
	public Vector getTextsToTranslate() {
		Vector v = new Vector();
		return v;
	}

	@Override
	public void setComponentLocale(Locale l) {}

	protected void removePanel(Object key) {
		for (int i = 0; i < this.getComponentCount(); i++) {
			Component c = this.getComponent(i);
			if (c instanceof FirstLevelPanel) {
				if (((FirstLevelPanel) c).isId(key)) {
					this.remove(i);
					this.panels.remove(key);
					return;
				}
			}
		}
	}

	protected FirstLevelPanel addPanel(Object key) {
		FirstLevelPanel panel = new FirstLevelPanel(key);
		this.add(panel,
				new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		this.remove(this.auxPanel);
		this.add(this.auxPanel,
				new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		this.panels.put(key, panel);
		return panel;
	}

	protected FirstLevelPanel getPanel(Object id) {
		for (int i = 0; i < this.getComponentCount(); i++) {
			Component c = this.getComponent(i);
			if (c instanceof FirstLevelPanel) {
				if (((FirstLevelPanel) c).isId(id)) {
					return (FirstLevelPanel) c;
				}
			}
		}
		return null;
	}

	public DataComponent getTwoLevelComponent(Object idLevel1, Object idLevel2) {
		FirstLevelPanel p = (FirstLevelPanel) this.panels.get(idLevel1);
		if (p == null) {
			return null;
		} else {
			return p.getDataComponent(idLevel2);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if ((this.formName != null) && (this.entity != null)) {
			Object oSource = e.getSource();
			if (oSource instanceof DataComponent) {
				Object attr2 = ((DataComponent) oSource).getAttribute();
				Container containerParent = ((Component) oSource).getParent().getParent();
				if (containerParent instanceof FirstLevelPanel) {
					Object attr1 = ((FirstLevelPanel) containerParent).id;
					// Now make a query
					if (this.detailWindow == null) {
						this.form = this.parentForm.getFormManager().getFormCopy(this.formName);
						this.detailWindow = new JDialog(this.parentFrame, true);
						this.detailWindow.getContentPane().add(this.form);
					}
					try {
						// Make a query and set the form values
						EntityReferenceLocator locator = this.form.getFormManager().getReferenceLocator();
						Entity ent = locator.getEntityReference(this.entity);
						if (ent != null) {
							Map<Object, Object> keysValues = new HashMap<>();
							keysValues.put(this.firstLevelKey, attr1);
							keysValues.put(this.secondLevelKey, attr2);
							EntityResult res = ent.query(keysValues, this.form.getDataFieldAttributeList(), locator.getSessionId());
							this.form.updateDataFields(res);
							this.form.setModifiable("RemoteCode", false);
							if (this.form.getInteractionManager() != null) {
								this.form.getInteractionManager().setUpdateMode();
							}
							this.detailWindow.pack();
							Dimension d = this.getToolkit().getScreenSize();
							this.detailWindow.setLocation((d.width - this.detailWindow.getSize().width) / 2, (d.height - this.detailWindow.getSize().height) / 2);
							this.detailWindow.setVisible(true);
						}
					} catch (Exception ex) {
						this.parentForm.message("M_ERROR_QUERYING_ENTITY", Form.ERROR_MESSAGE, ex);
						TwoLevelContainer.logger.error(this.getClass().toString() + " : " + ex.getMessage(), e);
						if (com.ontimize.gui.ApplicationManager.DEBUG) {
							TwoLevelContainer.logger.error(null, ex);
						} else {
							TwoLevelContainer.logger.trace(null, e);
						}
					} finally {
					}
				}
			}
		} else {
			TwoLevelContainer.logger.debug(TwoLevelContainer.class.getName() + "mouseClicked() -->: Form or entity null");
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void setParentForm(Form parentForm) {
		this.parentForm = parentForm;
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
	}

	@Override
	public void initPermissions() {}

	@Override
	public boolean isRestricted() {
		return false;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub

	}

}
