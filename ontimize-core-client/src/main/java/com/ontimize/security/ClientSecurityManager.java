package com.ontimize.security;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.Application;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.SecureElement;
import com.ontimize.gui.TipScroll;
import com.ontimize.jee.common.security.ApplicationPermission;
import com.ontimize.jee.common.security.ClientPermission;
import com.ontimize.jee.common.security.FMPermission;
import com.ontimize.jee.common.security.FormPermission;
import com.ontimize.jee.common.security.GeneralSecurityException;
import com.ontimize.jee.common.security.MenuPermission;
import com.ontimize.jee.common.security.NotInPeriodException;
import com.ontimize.jee.common.security.TableFormPermission;
import com.ontimize.jee.common.security.TreePermission;
import com.ontimize.jee.common.security.XMLClientUtilities;

/**
 * This class manages permissions in client application. Menu, forms, form managers and applications
 * are categories where permissions are divided. Method <code>checkPermission</code> checks the
 * different permissions for application components.
 *
 * <p>
 * Most common tags for XML permission definitions are:<br>
 * <br>
 * <TABLE BORDER=1 CELLPADDING=3 CELLSPACING=1 RULES=ROWS FRAME=BOX>
 *
 * <tr>
 * <td>Component</td>
 * <td>Permission Tag</td>
 * </tr>
 *
 * <tr>
 * <td>Menu</td>
 * <td><i>MENU</td>
 * </tr>
 *
 * <tr>
 * <td>Form</td>
 * <td><i>FORM</td>
 * </tr>
 *
 * <tr>
 * <td>FormManager</td>
 * <td><i>FM</td>
 * </tr>
 *
 * <tr>
 * <td>Tree</td>
 * <td><i>TREE</td>
 * </tr>
 *
 * <tr>
 * <td>Application</td>
 * <td><i>APPLICATION</td>
 * </tr>
 * </TABLE>
 *
 * @author Imatia Innovation
 */
public class ClientSecurityManager {

	public static final Logger logger = LoggerFactory.getLogger(ClientSecurityManager.class);

	public static String MENU_ID = "MENU";

	public static String FORM_ID = "FORM";

	public static String FM_ID = "FM";

	public static String TREE_ID = "TREE";

	public static String APPLICATION_ID = "APPLICATION";

	protected Map<Object, Object>	permissions				= new HashMap<>();

	protected static boolean mouseListenerEnabled = true;

	protected static class ListenerSecuredElements extends MouseAdapter {

		protected javax.swing.Timer timer = null;

		protected TipScroll popup = null;

		protected String text = SecureElement.DESACTIVATE_COMPONENT_BY_PERMISSION_TIP;

		protected Component currentComponent = null;

		protected ResourceBundle resourceBundle = null;

		ActionListener				showTip				= event -> {
			if ((ListenerSecuredElements.this.currentComponent == null)
					|| !ListenerSecuredElements.this.currentComponent.isShowing()) {
				return;
			} else {
				Application ap = ApplicationManager.getApplication();
				if ((ap != null) && (ListenerSecuredElements.this.resourceBundle != ap.getResourceBundle())) {
					try {
						ListenerSecuredElements.this.text = ap.getResourceBundle()
								.getString(SecureElement.DESACTIVATE_COMPONENT_BY_PERMISSION_TIP);

					} catch (Exception ex) {
						ClientSecurityManager.logger.debug(null, ex);
					}
				}
				ListenerSecuredElements.this.popup.show(ListenerSecuredElements.this.currentComponent,
						ListenerSecuredElements.this.currentComponent.getWidth() / 2,
						ListenerSecuredElements.this.currentComponent.getHeight() + 20,
						ListenerSecuredElements.this.text);
			}
		};

		public ListenerSecuredElements() {
			this.timer = new javax.swing.Timer(500, this.showTip);
			this.timer.setRepeats(false);
			this.popup = new TipScroll();
			try {
				Application ap = ApplicationManager.getApplication();
				if ((ap != null) && (ap.getResourceBundle() != null)) {
					this.text = ap.getResourceBundle().getString(SecureElement.DESACTIVATE_COMPONENT_BY_PERMISSION_TIP);
				}
			} catch (Exception e) {
				ClientSecurityManager.logger.trace(null, e);
			}
			this.popup.pack();
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (e.getSource() instanceof SecureElement) {
				// It is registered
				if (((SecureElement) e.getSource()).isRestricted()) {
					// popup.show((Component)e.getSource(),0,((Component)e.getSource()
					// ).getHeight()/2,text);
					this.currentComponent = (Component) e.getSource();
					this.timer.start();
				}
			} else if (e.getSource() instanceof Component) {
				Component c = (Component) e.getSource();
				if (ClientSecurityManager.references.containsKey(c)) {
					SecureElement el = (SecureElement) ClientSecurityManager.references.get(c);
					if (el instanceof Component) {
						c = (Component) el;
					}
					if (el.isRestricted()) {
						this.currentComponent = c;
						this.timer.start();
					}
				}
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			this.timer.stop();
			this.popup.setVisible(false);
			this.currentComponent = null;

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.timer.stop();
			this.popup.setVisible(false);
			this.currentComponent = null;
		}

	};

	protected static MouseListener mouseListener = null;

	public static void registerSecuredElement(SecureElement e) {
		if (ClientSecurityManager.mouseListener == null) {
			ClientSecurityManager.mouseListener = new ListenerSecuredElements();
		}
		if (e instanceof Component) {
			((Component) e).removeMouseListener(ClientSecurityManager.mouseListener);
			((Component) e).addMouseListener(ClientSecurityManager.mouseListener);
		}
	}

	public static void registerSecuredElement(SecureElement e, Component[] cs) {
		if (ClientSecurityManager.mouseListener == null) {
			ClientSecurityManager.mouseListener = new ListenerSecuredElements();
		}
		if (e instanceof Component) {
			((Component) e).removeMouseListener(ClientSecurityManager.mouseListener);
			((Component) e).addMouseListener(ClientSecurityManager.mouseListener);
		}
		for (Component element : cs) {
			element.addMouseListener(ClientSecurityManager.mouseListener);
			ClientSecurityManager.references.put(element, e);
		}
	}

	public static void unregisterSecuredElement(Component comp) {
		if (comp != null) {
			comp.removeMouseListener(ClientSecurityManager.mouseListener);
			ClientSecurityManager.references.remove(comp);
		}
	}

	protected static Map<Object, Object> references = new HashMap<>();

	public static void setMouseListenerEnabled(boolean en) {
		ClientSecurityManager.mouseListenerEnabled = en;
	}

	public ClientSecurityManager(String xmlPermissionDescriptionFile) throws Exception {
		URL url = this.getClass().getClassLoader().getResource(xmlPermissionDescriptionFile);
		if (url == null) {
			throw new Exception("File " + xmlPermissionDescriptionFile + " not Found");
		}
		// Reads
		InputStreamReader in = null;
		try {
			in = new InputStreamReader(url.openStream());
			StringBuffer sb = new StringBuffer();
			int car = -1;
			while ((car = in.read()) != -1) {
				sb.append((char) car);
			}
			in.close();
			this.permissions = XMLClientUtilities.buildClientPermissions(sb);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public ClientSecurityManager(StringBuffer xmlPermissionDescription) throws Exception {
		this.permissions = XMLClientUtilities.buildClientPermissions(xmlPermissionDescription);
	}

	public ClientSecurityManager(Map<Object, Object> permissions) throws Exception {
		this.permissions = permissions;
	}

	public void setPermissions(Map<Object, Object> permissions) {
		this.permissions = permissions;
	}

	public void checkPermission(ClientPermission clientpermission)
			throws NotInPeriodException, GeneralSecurityException {
		ClientSecurityManager.logger.debug("Checking permission: {}", clientpermission);
		if (clientpermission instanceof MenuPermission) {
			this.checkMenuPermission((MenuPermission) clientpermission);
		} else if (clientpermission instanceof ApplicationPermission) {
			this.checkApplicationPermission(clientpermission);
		} else if (clientpermission instanceof TableFormPermission) {
			this.checkTableFormPermission((TableFormPermission) clientpermission);
		} else if (clientpermission instanceof FormPermission) {
			this.checkFormPermission((FormPermission) clientpermission);
		} else if (clientpermission instanceof FMPermission) {
			this.checkFormManagerPermission((FMPermission) clientpermission);
		} else if (clientpermission instanceof TreePermission) {
			this.checkTreePermission((TreePermission) clientpermission);
		}
	}

	protected void checkTreePermission(TreePermission treePermission) throws NotInPeriodException,
	GeneralSecurityException {
		Map<?, ?> treepermissions = (Map<?, ?>) this.permissions.get(ClientSecurityManager.TREE_ID);
		if (treepermissions != null) {
			String treeName = treePermission.getTree();
			if (treeName == null) {
				ClientSecurityManager.logger.warn("Tree has not an id.");
				return;
			}
			List<?> nodepermissions = (List<?>) treepermissions.get(treeName);
			if (nodepermissions != null) {
				for (Object nodepermission : nodepermissions) {
					TreePermission fp = (TreePermission) nodepermission;
					if (fp.getAttribute().equals((treePermission).getAttribute())) {
						if (fp.getPermissionName().equalsIgnoreCase((treePermission).getPermissionName())) {
							if (!fp.hasPermission()) {
								if (fp.isPeriodRestricted()) {
									throw new NotInPeriodException("Permission " + treePermission.toString()
									+ " denied. Not in allowed period");
								}
								throw new GeneralSecurityException(
										"Permission " + treePermission.toString() + " denied");
							}
						}
					}
				}
			}
		}
	}

	protected void checkFormManagerPermission(FMPermission formManagerpermission) throws NotInPeriodException,
	GeneralSecurityException {
		Map<?, ?> formmanagerpermissions = (Map<?, ?>) this.permissions.get(ClientSecurityManager.FM_ID);
		if (formmanagerpermissions != null) {
			String gfName = formManagerpermission.getFMId();
			if (gfName == null) {
				ClientSecurityManager.logger.warn("FormManager has not an id. ");
				return;
			}
			List<?> formpermissions = (List<?>) formmanagerpermissions.get(gfName);
			if (formpermissions != null) {
				for (Object formpermission : formpermissions) {
					FMPermission fp = (FMPermission) formpermission;
					if (fp.getPermissionName().equalsIgnoreCase((formManagerpermission).getPermissionName())) {
						if (fp.getAttribute().equals((formManagerpermission).getAttribute())) {
							if (!fp.hasPermission()) {
								if (fp.isPeriodRestricted()) {
									throw new NotInPeriodException("Permission " + formManagerpermission.toString()
									+ " denied. Not in allowed period");
								}
								throw new GeneralSecurityException(
										"Permission " + formManagerpermission.toString() + " denied");
							}
						}
					}
				}
			}
		}
	}

	protected void checkFormPermission(FormPermission formPermission) throws NotInPeriodException,
	GeneralSecurityException {
		Map<?, ?> formpermissions = (Map<?, ?>) this.permissions.get(ClientSecurityManager.FORM_ID);
		if (formpermissions != null) {
			String archiveName = formPermission.getArchiveName();
			if (archiveName == null) {
				ClientSecurityManager.logger.warn("Form doesn't have an archive name.");
				return;
			}
			List<?> componentpermissions = (List<?>) formpermissions.get(archiveName);
			if (componentpermissions != null) {
				for (Object componentpermission : componentpermissions) {
					FormPermission fp = (FormPermission) componentpermission;
					if (fp.getAttribute().equals((formPermission).getAttribute())) {
						if (fp.getPermissionName().equalsIgnoreCase((formPermission).getPermissionName())) {
							if (!fp.hasPermission()) {
								if (fp.isPeriodRestricted()) {
									throw new NotInPeriodException("Permission " + formPermission.toString()
									+ " denied. Not in allowed period");
								}
								throw new GeneralSecurityException(
										"Permission " + formPermission.toString() + " denied");
							}
						}
					}
				}
			}
		}
	}

	protected void checkTableFormPermission(TableFormPermission tableFormPermission) throws NotInPeriodException,
	GeneralSecurityException {
		Map<?, ?> formPermissions = (Map<?, ?>) this.permissions.get(ClientSecurityManager.FORM_ID);
		if (formPermissions != null) {
			String archiveName = tableFormPermission.getArchiveName();
			if (archiveName == null) {
				ClientSecurityManager.logger.warn("Form doesn't have an archive name");
				return;
			}
			List<?> componentpermissions = (List<?>) formPermissions.get(archiveName);
			if (componentpermissions != null) {
				for (Object componentpermission : componentpermissions) {
					Object o = componentpermission;
					if (!(o instanceof TableFormPermission)) {
						continue;
					}
					TableFormPermission fp = (TableFormPermission) componentpermission;
					if (fp.getAttribute().equals(tableFormPermission.getAttribute())) {
						if (fp.getPermissionName().equalsIgnoreCase(tableFormPermission.getPermissionName())) {
							if ((fp.getColumnName() != null)
									&& fp.getColumnName()
									.equalsIgnoreCase((tableFormPermission)
											.getColumnName())
									&& (fp.getType() != null)
									&& fp.getType().equalsIgnoreCase((tableFormPermission).getType())) {
								if (!fp.hasPermission()) {
									if (fp.isPeriodRestricted()) {
										throw new NotInPeriodException("Permission " + tableFormPermission.toString()
										+ " denied. Not in allowed period");
									}
									throw new GeneralSecurityException("Permission " + fp.toString() + " denied");
								}
							} else if ((fp.getType() != null) && fp.getType()
									.equalsIgnoreCase((tableFormPermission).getType()) && (fp.getColumnName() == null)) {
								if (!fp.hasPermission()) {
									if (fp.isPeriodRestricted()) {
										throw new NotInPeriodException("Permission " + tableFormPermission.toString()
										+ " denied. Not in allowed period");
									}
									throw new GeneralSecurityException(
											"Permission " + tableFormPermission.toString() + " denied");
								}
							}
						}
					}
				}
			}
		}
	}

	protected void checkApplicationPermission(ClientPermission clientpermission) throws GeneralSecurityException {
		// Finds in application permissions
		List<?> applicationpermissions = (List<?>) this.permissions.get(ClientSecurityManager.APPLICATION_ID);
		if ((applicationpermissions == null) || applicationpermissions.isEmpty()) {
			return;
		}
		// Ahora, si hay permisos miramos si existe el que nos piden
		for (Object applicationpermission : applicationpermissions) {
			ApplicationPermission mP = (ApplicationPermission) applicationpermission;
			if (mP.getPermissionName().equalsIgnoreCase(clientpermission.getPermissionName())) {
				if (!mP.hasPermission()) {
					throw new GeneralSecurityException("Permission " + clientpermission.toString() + " denied");
				}
			}
		}
	}

	protected void checkMenuPermission(MenuPermission menupermission) throws NotInPeriodException,
	GeneralSecurityException {
		// Finds in menu permissions
		List<?> menupermissions = (List<?>) this.permissions.get(ClientSecurityManager.MENU_ID);
		if ((menupermissions == null) || menupermissions.isEmpty()) {
			return;
		}
		// Finds in menu permissions
		for (Object menupermission2 : menupermissions) {
			MenuPermission mP = (MenuPermission) menupermission2;
			if (mP.getAttribute().equals(menupermission.getAttribute())) {
				// Checks restricted condition
				if (mP.getPermissionName().equalsIgnoreCase(menupermission.getPermissionName())) {
					if (!mP.hasPermission()) {
						if (mP.isPeriodRestricted()) {
							throw new NotInPeriodException(
									"Permission " + menupermission.toString() + " denied. Not in allowed period");
						}
						throw new GeneralSecurityException("Permission " + menupermission.toString() + " denied");
					}
				}
			}
		}
	}

}
