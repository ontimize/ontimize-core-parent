package com.ontimize.gui.field;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Freeable;
import com.ontimize.gui.OpenDialog;
import com.ontimize.gui.ReferenceComponent;
import com.ontimize.gui.ValueEvent;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.ConnectionManager;
import com.ontimize.jee.common.locator.EntityReferenceLocator;

/**
 * @deprecated
 * @see #ReferenceExtDataField
 */
@Deprecated
public class ReferenceDataField extends TextFieldDataField implements DataComponent, ReferenceComponent, OpenDialog, Internationalization, Freeable {

	private static final Logger			logger					= LoggerFactory.getLogger(ReferenceDataField.class);

	protected String					entityName				= null;

	protected EntityReferenceLocator	locator					= null;

	protected JTextField				codeField				= new JTextField(4);

	protected JButton					queryBt					= new JButton();

	protected JDialog					dialog					= null;

	protected Frame						parentFrame				= null;

	protected String					code					= null;

	protected String					description				= null;

	protected List<Object>				attributesVector		= null;

	protected List<Object>				localeAttributesVector	= null;

	protected List<String>				descriptionField		= null;

	protected JTable					table					= new JTable();

	protected DefaultTableModel			model					= new DefaultTableModel();

	protected JScrollPane				scrollPane				= null;

	protected boolean					descriptionValue		= false;

	protected JLabel					labelInfo				= new JLabel("Click two times to select");

	protected String					parentKey				= null;

	protected boolean					codeFieldVisible		= true;

	protected boolean					integerValue			= false;

	QueryThread							queryThread				= null;

	class QueryThread extends Thread {

		public QueryThread() {
			this.setPriority(Thread.MIN_PRIORITY);
		}

		List<Object> attributeVector = null;

		@Override
		public void run() {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				ReferenceDataField.logger.trace(null, e);
			}
			try {
				if ((ReferenceDataField.this.code == null) || (ReferenceDataField.this.description == null)) {
					return;
				}
				// Execute the query against the database
				this.attributeVector = new ArrayList<>();
				this.attributeVector.add(ReferenceDataField.this.code);
				for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
					this.attributeVector.add(ReferenceDataField.this.descriptionField.get(i));
				}
				// Vector datosAux = new Vector();
				Map<Object, Object> hKeysValues = new HashMap<>();
				hKeysValues.put(ReferenceDataField.this.code, ReferenceDataField.this.codeField.getText());
				if (ReferenceDataField.this.parentKey != null) {
					hKeysValues.put(ReferenceDataField.this.parentKey, ReferenceDataField.this.parentForm.getDataFieldValue(ReferenceDataField.this.parentKey));
				}
				EntityResult hResult = ReferenceDataField.this.locator.getEntityReference(ReferenceDataField.this.entityName).query(hKeysValues, this.attributeVector,
						ReferenceDataField.this.locator.getSessionId());
				// Code is primary key and values of the hashtable are vectors
				if (hResult.isEmpty()) {
					ReferenceDataField.this.deleteData();
				} else {
					// Put the values in the data field
					String sDataFieldText = "";
					for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
						Object oValue2 = hResult.get(ReferenceDataField.this.descriptionField.get(i));
						if (oValue2 != null) {
							if (oValue2 instanceof Vector) {
								Object oAuxValue = ((Vector) oValue2).get(0);
								if (oAuxValue != null) {
									sDataFieldText += " " + oAuxValue.toString();
								}
							} else {
								sDataFieldText += " " + oValue2.toString();
							}
						}
					}
					((JTextField) ReferenceDataField.this.dataField).setText(sDataFieldText);
					ReferenceDataField.this.valueSave = ReferenceDataField.this.getValue();
				}
			} catch (Exception e) {
				ReferenceDataField.logger.error("Error querying code", e);
			}
		}

	};

	public static final String propUserSelection = "UserSelection";

	class InnerKeyListener extends KeyAdapter {

		public int		MAX_CHAR	= 10;

		StringBuilder	stringKey	= new StringBuilder("");

		public InnerKeyListener() {}

		@Override
		public void keyPressed(KeyEvent keyEvent) {
			if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
				if (this.stringKey.length() > 0) {
					this.stringKey.delete(this.stringKey.length() - 1, this.stringKey.length());
					ReferenceDataField.this.labelInfo.setForeground(Color.blue);
					ReferenceDataField.this.labelInfo.setText(this.stringKey.toString());
					ReferenceDataField.this.labelInfo.paintImmediately(0, 0, ReferenceDataField.this.labelInfo.getWidth(), ReferenceDataField.this.labelInfo.getHeight());
				}
				if (this.stringKey.length() == 0) {
					ReferenceDataField.this.labelInfo.setForeground(Color.black);
					ReferenceDataField.this.labelInfo.setText("Double click to select");
					ReferenceDataField.this.labelInfo.paintImmediately(0, 0, ReferenceDataField.this.labelInfo.getWidth(), ReferenceDataField.this.labelInfo.getHeight());
				}
				if (ApplicationManager.DEBUG) {
					ReferenceDataField.logger.debug(ReferenceDataField.this.labelInfo.getText());
				}

				return;
			}
			// Get the character
			char character = keyEvent.getKeyChar();
			if ((character != KeyEvent.CHAR_UNDEFINED) && (Character.isLetterOrDigit(character) || Character.isSpaceChar(character)) && (this.stringKey.length() < this.MAX_CHAR)) {
				this.stringKey.append(character);
				ReferenceDataField.this.labelInfo.setForeground(Color.blue);
				ReferenceDataField.this.labelInfo.setText(this.stringKey.toString());
				ReferenceDataField.this.labelInfo.paintImmediately(0, 0, ReferenceDataField.this.labelInfo.getWidth(), ReferenceDataField.this.labelInfo.getHeight());
				if (ApplicationManager.DEBUG) {
					ReferenceDataField.logger.debug(ReferenceDataField.this.labelInfo.getText());
				}

				// Search in the description field in all rows.
				for (int i = 0; i < ReferenceDataField.this.table.getRowCount(); i++) {
					Object oValue = ReferenceDataField.this.table.getValueAt(i, 1);
					if (oValue != null) {
						String oStringValue = oValue.toString();
						if (this.stringKey.length() <= oStringValue.length()) {
							if (oStringValue.substring(0, this.stringKey.length()).equalsIgnoreCase(this.stringKey.toString())) {
								ReferenceDataField.this.table.setRowSelectionInterval(i, i);
								ReferenceDataField.this.table.scrollRectToVisible(ReferenceDataField.this.table.getCellRect(i, 1, false));
								return;
							}
						}
					}
				}
			}
		}

	}

	public ReferenceDataField(Map<Object, Object> parameters) {
		this.init(parameters);
	}

	@Override
	public void init(Map<Object, Object> parameters) {
		super.init(parameters);
		((JTextField) this.dataField).setEnabled(false);
		((JTextField) this.dataField).setBackground(DataComponent.VERY_LIGHT_GRAY);
		((JTextField) this.dataField).setForeground(Color.black);

		Object entity = parameters.get("entity");
		if (entity == null) {
			ReferenceDataField.logger.debug(this.getClass().getName() + ": Parameter 'entity' not found. Check parameters.");
		} else {
			this.entityName = entity.toString();
		}
		// Parameter cod : Code colomn
		Object cod = parameters.get("cod");
		if (cod == null) {
			ReferenceDataField.logger.debug(this.getClass().getName() + ": Parameter 'cod' not found. Check parameters.");
		} else {
			this.code = cod.toString();
		}
		// Parameter descr = Description column name
		Object descr = parameters.get("descr");
		if (descr == null) {
			ReferenceDataField.logger.debug(this.getClass().getName() + ": Parameter 'descr' not found. Check parameters.");
		} else {
			this.description = descr.toString();
			this.descriptionField = ApplicationManager.getTokensAt(this.description, ";");
		}

		Object csize = parameters.get("csize");
		if (csize != null) {
			try {
				this.codeField.setColumns(new Integer(csize.toString()).intValue());
			} catch (Exception e) {
				ReferenceDataField.logger.error(" Error in parameter 'csize': ", e);
			}
		}

		Object oValue = parameters.get("value");
		if (oValue != null) {
			if (oValue.toString().equalsIgnoreCase("descr")) {
				this.descriptionValue = true;
			} else {
				this.descriptionValue = false;
			}
		} else {
			this.descriptionValue = false;
		}

		Object parentkey = parameters.get("parentkey");
		if (parentkey != null) {
			this.parentKey = parentkey.toString();
		} else {
			this.parentKey = null;
		}

		Object codVisible = parameters.get("codvisible");
		if (codVisible != null) {
			try {
				this.codeFieldVisible = new Boolean(codVisible.toString()).booleanValue();
				this.codeField.setVisible(this.codeFieldVisible);
			} catch (Exception e) {
				ReferenceDataField.logger.error("Error in parameter 'codvisible': ", e);
			}
		} else {
			this.codeFieldVisible = true;
		}

		Object codInteger = parameters.get("codinteger");
		if (codInteger != null) {
			try {
				if (codInteger.equals("yes")) {
					this.integerValue = true;
				} else {
					this.integerValue = false;
				}
			} catch (Exception e) {
				ReferenceDataField.logger.error("Error in parameter 'codInteger': ", e);
			}
		} else {
			this.integerValue = false;
		}
		// Adds the button to show the results and the code field
		super.panel.add(this.codeField, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		ImageIcon buttonIcon = ImageManager.getIcon(ImageManager.MAGNIFYING_GLASS);
		if (buttonIcon == null) {
			if (ApplicationManager.DEBUG) {
				ReferenceDataField.logger.debug("magnifyingglass.png icon not found");
			}
		} else {
			this.queryBt.setIcon(buttonIcon);
			this.queryBt.setPreferredSize(new Dimension(buttonIcon.getIconWidth() + 4, buttonIcon.getIconHeight() + 4));
		}
		super.add(this.queryBt,
				new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		// Process the button click
		this.queryBt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evento) {
				if ((ReferenceDataField.this.code == null) || (ReferenceDataField.this.description == null)) {
					return;
				}
				// Execute the query
				ReferenceDataField.this.attributesVector = new Vector();
				ReferenceDataField.this.attributesVector.add(ReferenceDataField.this.code);
				for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
					ReferenceDataField.this.attributesVector.add(ReferenceDataField.this.descriptionField.get(i));
				}
				List<Object> vAuxData = new ArrayList<>();
				ReferenceDataField.this.table = new JTable((Object[][])vAuxData.toArray(), ReferenceDataField.this.attributesVector.toArray());
				// Key Listener
				ReferenceDataField.this.table.addKeyListener(new InnerKeyListener());
				ReferenceDataField.this.table.addMouseListener(new MouseAdapter() {

					@Override
					public void mouseClicked(MouseEvent event) {
						ReferenceDataField.this.labelInfo.setForeground(Color.black);
						ReferenceDataField.this.labelInfo.setText("Doble click to select");
						if (event.getClickCount() == 2) {
							int row = ReferenceDataField.this.table.getSelectedRow();
							Object cod = ((JTable) event.getSource()).getValueAt(row, 0);
							if (cod != null) {
								ReferenceDataField.this.codeField.setText(cod.toString());
								// Put the other values in the data field
								String sDataFieldText = "";
								for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
									Object oValue = ((JTable) event.getSource()).getValueAt(row, i + 1);
									if (oValue != null) {
										if (i == 0) {
											sDataFieldText += oValue.toString();
										} else {
											sDataFieldText += " " + oValue.toString();
										}
									}
								}
								((JTextField) ReferenceDataField.this.dataField).setText(sDataFieldText);
								ReferenceDataField.this.firePropertyChange(ReferenceDataField.propUserSelection, null, cod);
							}
							ReferenceDataField.this.dialog.dispose();
						}
					}
				});
				ReferenceDataField.this.scrollPane = new JScrollPane(ReferenceDataField.this.table);
				Cursor cursor = ReferenceDataField.this.getCursor();
				JWindow jWaitWindow = null;
				try {
					// Show a message window
					jWaitWindow = new JWindow(ReferenceDataField.this.parentFrame);
					JPanel panel = new JPanel();
					panel.setBorder(new EtchedBorder());
					jWaitWindow.getContentPane().add(panel);
					JLabel queryLabel = new JLabel("Querying");
					panel.add(queryLabel);
					jWaitWindow.pack();
					ReferenceDataField.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					jWaitWindow.setLocation(ReferenceDataField.this.queryBt.getLocationOnScreen().x - ReferenceDataField.this.queryBt.getWidth() - jWaitWindow.getWidth(),
							ReferenceDataField.this.queryBt.getLocationOnScreen().y);
					jWaitWindow.setVisible(true);
					jWaitWindow.toFront();
					panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
					Map<Object, Object> hKeysValues = new HashMap<>();
					if (ReferenceDataField.this.parentKey != null) {
						hKeysValues.put(ReferenceDataField.this.parentKey, ReferenceDataField.this.parentForm.getDataFieldValue(ReferenceDataField.this.parentKey));
					}
					EntityResult hResult = ReferenceDataField.this.locator.getEntityReference(ReferenceDataField.this.entityName).query(hKeysValues,
							ReferenceDataField.this.attributesVector, ReferenceDataField.this.locator.getSessionId());
					if (hResult != null) {
						ConnectionManager.checkEntityResult(hResult, ReferenceDataField.this.locator);
					}
					// Create a vector with vectors
					List<Object> vRows = new ArrayList<>();
					Object oData = hResult.get(ReferenceDataField.this.attributesVector.get(0));
					if (oData instanceof Vector) {
						// More than one row
						for (int i = 0; i < ((List<?>) oData).size(); i++) {
							List<Object> vRowData = new ArrayList<>();
							for (int j = 0; j < ReferenceDataField.this.attributesVector.size(); j++) {
								vRowData.add(((List<?>) hResult.get(ReferenceDataField.this.attributesVector.get(j))).get(i));
							}
							vRows.add(vRowData.toArray());
						}
					} else {
						// Only one row
						List<Object> vRowData = new ArrayList<>();
						for (int j = 0; j < ReferenceDataField.this.attributesVector.size(); j++) {
							vRowData.add(hResult.get(ReferenceDataField.this.attributesVector.get(j)));
						}
						vRows.add(vRowData);
					}
					// Create the JTable component using the appropriate
					// language
					if (ReferenceDataField.this.localeAttributesVector == null) {
						ReferenceDataField.this.localeAttributesVector = new ArrayList<>();
						for (int i = 0; i < ReferenceDataField.this.attributesVector.size(); i++) {
							ReferenceDataField.this.localeAttributesVector.add(ReferenceDataField.this.attributesVector.get(i));
						}
					}
					ReferenceDataField.this.model.setDataVector((Object[][]) vRows.toArray(), ReferenceDataField.this.localeAttributesVector.toArray());
					ReferenceDataField.this.table.setModel(ReferenceDataField.this.model);
					// No editable
					for (int i = 0; i < ReferenceDataField.this.table.getColumnCount(); i++) {
						ReferenceDataField.this.table.setDefaultEditor(ReferenceDataField.this.table.getColumnClass(i), null);
					}
					// Show a dialog to select the description
					ReferenceDataField.this.dialog = new JDialog(ReferenceDataField.this.parentFrame, ReferenceDataField.this.entityName, true);
					ReferenceDataField.this.dialog.getContentPane().setLayout(new GridBagLayout());

					ReferenceDataField.this.dialog.getContentPane().add(ReferenceDataField.this.labelInfo,
							new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
					ReferenceDataField.this.dialog.getContentPane().add(ReferenceDataField.this.scrollPane,
							new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
					// 10 lines
					FontMetrics fontMetrics = ReferenceDataField.this.table.getFontMetrics(ReferenceDataField.this.table.getFont());
					ReferenceDataField.this.scrollPane.getViewport()
							.setPreferredSize(new Dimension((int) ReferenceDataField.this.table.getPreferredSize().getWidth(), 10 * fontMetrics.getHeight()));
					ReferenceDataField.this.table.setBackground(ReferenceDataField.this.dialog.getBackground());
					ReferenceDataField.this.dialog.pack();
					// Center the window
					Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
					ReferenceDataField.this.dialog.setLocation((d.width - ReferenceDataField.this.dialog.getSize().width) / 2,
							(d.height - ReferenceDataField.this.dialog.getSize().height) / 2);
					jWaitWindow.hide();
					jWaitWindow.dispose();
					ReferenceDataField.this.dialog.setVisible(true);
				} catch (Exception e) {
					ReferenceDataField.logger.error("Error in query. Results can not be shown", e);
				} finally {
					if (jWaitWindow != null) {
						jWaitWindow.setVisible(false);
						jWaitWindow.dispose();
					}
					ReferenceDataField.this.setCursor(cursor);
				}
				ReferenceDataField.this.dialog = null;

			}
		});

		// Adds a listener to the code field. When code lost focus then fill the
		// description
		this.codeField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent evento) {
				// When code lost focus search the value for this code
				if (ReferenceDataField.this.codeField.getText().equals("")) {
				} else {
					// Query:
					try {
						if ((ReferenceDataField.this.code == null) || (ReferenceDataField.this.description == null)) {
							return;
						}
						// Execute the query
						ReferenceDataField.this.attributesVector = new ArrayList<>();
						ReferenceDataField.this.attributesVector.add(ReferenceDataField.this.code);
						for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
							ReferenceDataField.this.attributesVector.add(ReferenceDataField.this.descriptionField.get(i));
						}
						Map<Object, Object> hKeysValues = new HashMap<>();
						hKeysValues.put(ReferenceDataField.this.code, ReferenceDataField.this.codeField.getText());
						if (ReferenceDataField.this.parentKey != null) {
							hKeysValues.put(ReferenceDataField.this.parentKey, ReferenceDataField.this.parentForm.getDataFieldValue(ReferenceDataField.this.parentKey));
						}
						EntityResult hResult = ReferenceDataField.this.locator.getEntityReference(ReferenceDataField.this.entityName).query(hKeysValues,
								ReferenceDataField.this.attributesVector, ReferenceDataField.this.locator.getSessionId());
						if (hResult.isEmpty()) {
							ReferenceDataField.this.deleteData();
						} else {
							String sDataFieldText = "";
							for (int i = 0; i < ReferenceDataField.this.descriptionField.size(); i++) {
								Object oValue = hResult.get(ReferenceDataField.this.descriptionField.get(i));
								if (oValue != null) {
									if (oValue instanceof Vector) {
										Object oAuxValue = ((List<?>) oValue).get(0);
										if (oAuxValue != null) {
											if (i == 0) {
												sDataFieldText += oAuxValue.toString();
											} else {
												sDataFieldText += " " + oAuxValue.toString();
											}
										}
									} else {
										if (i == 0) {
											sDataFieldText += oValue.toString();
										} else {
											sDataFieldText += " " + oValue.toString();
										}
									}
								}
							}
							((JTextField) ReferenceDataField.this.dataField).setText(sDataFieldText);
							ReferenceDataField.this.firePropertyChange(ReferenceDataField.propUserSelection, null, ReferenceDataField.this.codeField.getText());
						}
					} catch (Exception e) {
						ReferenceDataField.logger.error("Error querying code", e);
					}
				}
			}

			@Override
			public void focusGained(FocusEvent evento) {}
		});
	}

	@Override
	public void setReferenceLocator(EntityReferenceLocator referenceLocator) {
		if (referenceLocator == null) {
			ReferenceDataField.logger.debug("Error: " + this.getClass().getName() + " null value for ReferenceLocator.");
		} else {
			this.locator = referenceLocator;
		}
	}

	@Override
	public void setValue(Object value) {
		this.setInnerListenerEnabled(false);
		Object oPreviousValue = this.getValue();
		if (value != null) {
			if (value instanceof Vector) {
				if (((Vector) value).get(0) instanceof String) {
					if (this.descriptionValue) {
						((JTextField) this.dataField).setText((String) ((Vector) value).get(0));
						this.valueSave = this.getValue();
						this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
					} else {
						this.codeField.setText((String) ((Vector) value).get(0));
						// Excute the query
						if (this.codeField.getText().equals("")) {
							this.valueSave = this.getValue();
							this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
						} else {
							// Query
							if ((this.queryThread != null) && this.queryThread.isAlive()) {
								this.queryThread.interrupt();
								this.queryThread = null;
							}
							this.queryThread = new QueryThread();
							this.queryThread.start();
						}
					}
				} else {
					if (this.descriptionValue) {
						((JTextField) this.dataField).setText(((Vector) value).get(0).toString());
						this.valueSave = this.getValue();
						this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
					} else {
						this.codeField.setText(((Vector) value).get(0).toString());
						// Execute the query
						if (this.codeField.getText().equals("")) {
							this.valueSave = this.getValue();
							this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
						} else {
							// Query
							if ((this.queryThread != null) && this.queryThread.isAlive()) {
								this.queryThread.interrupt();
								this.queryThread = null;
							}
							this.queryThread = new QueryThread();
							this.queryThread.start();
						}
					}
				}
			} else {
				if (value instanceof String) {
					if (this.descriptionValue) {
						((JTextField) this.dataField).setText(value.toString());
						this.valueSave = this.getValue();
						this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
					} else {
						this.codeField.setText(value.toString());
						// Execute the query
						if (this.codeField.getText().equals("")) {
							this.valueSave = this.getValue();
							this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
						} else {
							// Query
							if ((this.queryThread != null) && this.queryThread.isAlive()) {
								this.queryThread.interrupt();
								this.queryThread = null;
							}
							this.queryThread = new QueryThread();
							this.queryThread.start();
						}
					}
				} else {
					if (this.descriptionValue) {
						((JTextField) this.dataField).setText(value.toString());
						this.valueSave = this.getValue();
						this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
					} else {
						this.codeField.setText(value.toString());
						if (this.codeField.getText().equals("")) {
							this.valueSave = this.getValue();
							this.fireValueChanged(this.valueSave, oPreviousValue, ValueEvent.PROGRAMMATIC_CHANGE);
						} else {
							if ((this.queryThread != null) && this.queryThread.isAlive()) {
								this.queryThread.interrupt();
								this.queryThread = null;
							}
							this.queryThread = new QueryThread();
							this.queryThread.start();
						}
					}
				}
			}
		} else {
			this.deleteData();
			this.valueSave = this.getValue();
		}
		this.setInnerListenerEnabled(true);
	}

	/**
	 * Return the description value as a String
	 */
	@Override
	public Object getValue() {
		if (this.isEmpty()) {
			return null;
		}
		if (this.descriptionValue) {
			return ((JTextField) this.dataField).getText();
		} else {
			String sValue = this.codeField.getText();
			if (this.integerValue) {
				try {
					return new Integer(sValue);
				} catch (Exception e) {
					ReferenceDataField.logger.trace(null, e);
					return null;
				}
			} else {
				return sValue;
			}
		}
	}

	@Override
	public void setParentFrame(Frame parentFrame) {
		this.parentFrame = parentFrame;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			boolean permission = this.checkEnabledPermission();
			if (!permission) {
				return;
			}
		}
		this.codeField.setEnabled(enabled);
		this.queryBt.setVisible(enabled);
		this.enabled = enabled;
		if (!enabled) {
			this.codeField.setBackground(DataComponent.VERY_LIGHT_GRAY);
			this.dataField.setBackground(DataComponent.VERY_LIGHT_GRAY);
			this.codeField.setForeground(Color.black);
		} else {
			if (this.required) {
				this.codeField.setBackground(DataComponent.VERY_LIGHT_SKYBLUE);
				if (!this.codeFieldVisible) {
					this.dataField.setBackground(DataComponent.LIGHT_GREYISH_BLUE);
				}
			} else {
				if (!this.codeFieldVisible) {
					this.dataField.setBackground(DataComponent.VERY_LIGHT_GRAY);
				}
				this.codeField.setBackground(this.backgroundColor);
			}
			this.codeField.setForeground(Color.black);
		}
	}

	@Override
	public void deleteData() {
		this.setInnerListenerEnabled(false);
		Object previosValue = this.getValue();
		((JTextField) this.dataField).setText("");
		this.codeField.setText("");
		this.valueSave = this.getValue();
		this.fireValueChanged(this.valueSave, previosValue, ValueEvent.PROGRAMMATIC_CHANGE);
		this.setInnerListenerEnabled(true);
	}

	@Override
	public boolean isModified() {
		if ((this.queryThread != null) && this.queryThread.isAlive()) {
			return false;
		} else {
			return super.isModified();
		}
	}

	@Override
	public void setResourceBundle(ResourceBundle resources) {
		super.setResourceBundle(resources);
		this.localeAttributesVector = new Vector();
		// Column titles
		Object oCodeByLanguage = null;
		try {
			oCodeByLanguage = resources.getObject(this.code);
		} catch (Exception e) {
			if (ApplicationManager.DEBUG) {
				ReferenceDataField.logger.debug(null, e);
			} else {
				ReferenceDataField.logger.trace(null, e);
			}
		}
		if (oCodeByLanguage == null) {
			this.localeAttributesVector.add(this.code);
		} else {
			this.localeAttributesVector.add(oCodeByLanguage);
		}
		for (int i = 0; i < this.descriptionField.size(); i++) {
			Object oTitleColTranslated = null;
			try {
				oTitleColTranslated = resources.getObject(this.descriptionField.get(i).toString());
			} catch (Exception e) {
				if (ApplicationManager.DEBUG) {
					ReferenceDataField.logger.debug(null, e);
				} else {
					ReferenceDataField.logger.trace(null, e);
				}
			}
			if (oTitleColTranslated == null) {
				this.localeAttributesVector.add(this.descriptionField.get(i));
			} else {
				this.localeAttributesVector.add(oTitleColTranslated);
			}
		}
	}

	@Override
	public List<String> getTextsToTranslate() {
		List<String> v = super.getTextsToTranslate();
		v.add(this.code);
		for (int i = 0; i < this.descriptionField.size(); i++) {
			v.add(this.descriptionField.get(i).toString());
		}
		return v;
	}

	@Override
	public void free() {
		super.free();
		this.parentFrame = null;
		this.model = null;
		this.locator = null;
		this.parentForm = null;
		if (ApplicationManager.DEBUG) {
			ReferenceDataField.logger.debug(this.getClass().toString() + " Free.");
		}
	}

	@Override
	public int getSQLDataType() {
		return java.sql.Types.VARCHAR;
	}

	public String getCodeFieldName() {
		return this.code;
	}

	@Override
	protected void installFocusListener() {
		if (this.codeField != null) {
			this.codeField.addFocusListener(this.fieldlistenerFocus);
		}
	}

}
