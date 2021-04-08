package com.ontimize.report;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ontimize.db.AdvancedQueryEntity;
import com.ontimize.db.Entity;
import com.ontimize.db.EntityResult;
import com.ontimize.db.EntityResultUtils;
import com.ontimize.db.SQLStatementBuilder.Expression;
import com.ontimize.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.db.query.ContainsSQLConditionValuesProcessorHelper;
import com.ontimize.db.query.ParameterValuesDialog;
import com.ontimize.gui.ApplicationManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.field.DataField;
import com.ontimize.gui.field.ListDataField;
import com.ontimize.gui.i18n.Internationalization;
import com.ontimize.gui.images.ImageManager;
import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.table.Table;
import com.ontimize.locator.ClientReferenceLocator;
import com.ontimize.locator.EntityReferenceLocator;
import com.ontimize.locator.UtilReferenceLocator;
import com.ontimize.report.columns.OrderColumns;
import com.ontimize.report.engine.dynamicjasper.IGroupByDate;
import com.ontimize.report.item.ItemListener;
import com.ontimize.report.item.PredefinedFunctionItem;
import com.ontimize.report.item.SelectableConfigItemsListCellRenderer;
import com.ontimize.report.item.SelectableDateGroupItem;
import com.ontimize.report.item.SelectableDynamicItem;
import com.ontimize.report.item.SelectableFunctionItem;
import com.ontimize.report.item.SelectableItemsListCellRenderer;
import com.ontimize.report.item.SelectableMultipleItem;
import com.ontimize.report.listeners.ColumnSelectionListener;
import com.ontimize.report.listeners.DateGroupListener;
import com.ontimize.report.listeners.DeleteItemListener;
import com.ontimize.report.listeners.DynamicPopupListener;
import com.ontimize.report.listeners.FunctionListener;
import com.ontimize.report.listeners.GroupColumnListener;
import com.ontimize.report.listeners.OrderListener;
import com.ontimize.report.listeners.OrderedPopupListener;
import com.ontimize.report.listeners.PopupGroupDateListener;
import com.ontimize.report.listeners.PopupListener;
import com.ontimize.report.listeners.SharedDeleteItemListener;
import com.ontimize.report.listeners.SharedDeleteTargetItemListener;
import com.ontimize.report.listeners.SharedEditItemListener;
import com.ontimize.report.listeners.SharedEditTargetItemListener;
import com.ontimize.report.listeners.SharedItemListener;
import com.ontimize.report.listeners.SharedLoadItemListener;
import com.ontimize.report.listeners.SharedMessageItemListener;
import com.ontimize.report.listeners.UpdateReportListener;
import com.ontimize.report.store.ReportStore;
import com.ontimize.report.store.ReportStoreDefinition;
import com.ontimize.util.share.IShareRemoteReference;
import com.ontimize.util.share.SharedElement;
import com.ontimize.util.swing.ButtonSelection;
import com.ontimize.util.swing.EJFrame;
import com.ontimize.util.swing.MenuButton;
import com.ontimize.util.swing.RolloverButton;
import com.ontimize.util.swing.list.I18nListCellRenderer;

public class DefaultReportDialog {

    private static final Logger logger = LoggerFactory.getLogger(DefaultReportDialog.class);

    /** An instance of report engine */
    protected ReportEngine reportEngine;

    // Keys for elements

    public static String TITLE_KEY = "report.custom_reports";

    public static String PRINTING_COLUMN_KEY = "table.columns_to_print";

    public static String GROUPS_KEY = "group";

    public static String FUNCTION_KEY = "report.functions";

    public static String TEMPLATE_KEY = "report.template";

    public static String SQL_QUERY_KEY = "ReportDesigner.Consulta";

    public static String REPORT_STORE_KEY = "ReportDesigner.AlmacenInformes";

    public static String GRID_KEY = "report.draw_grid";

    public static String INCLUDE_ROW_NUMBER_KEY = "report.include_row_numbers";

    public static String HIDE_GROUP_DETAIL_KEY = "report.hide_group_details";

    public static String GROUP_START_IN_NEW_PAGE = "report.group_start_in_new_page";

    public static String FIRST_GROUP_START_IN_NEW_PAGE = "report.first_group_start_in_new_page";

    public static String INCLUDE_COLUMN_NAMES_KEY = "report.include_column_names";

    public static String UNDERLINE_LINES_KEY = "report.remark_line";

    public static String FIT_HEADER_KEY = "ReportDesigner.AjustarCabeceras";

    public static String DYNAMIC_HEADER_KEY = "ReportDesigner.CabeceraDinamicas";

    public static String AVERAGE_OP_KEY = "average";

    public static String SUM_OP_KEY = "sum";

    public static String MAXIMUM_OP_KEY = "maximum";

    public static String MINIMUM_OP_KEY = "minimum";

    public static String UP_BUTTON_KEY = "report.move_up";

    public static String DOWN_BUTTON_KEY = "report.move_down";

    public static String ALL_UP_BUTTON_KEY = "report.move_first";

    public static String ALL_DOWN_BUTTON_KEY = "report.move_last";

    public static String UP_GROUP_BUTTON_KEY = "report.group_move_up";

    public static String GROUP_DOWN_BUTTON_KEY = "report.group_move_down";

    public static String ALL_UP_GROUP_BUTTON_KEY = "report.group_move_first";

    public static String ALL_DOWN_GROUP_BUTTON_KEY = "report.group_move_last";

    public static String OPTIONS_BUTTON_KEY = "ReportDesigner.Opciones";

    public static String ORDER_BUTTON_KEY = "ReportDesigner.Ordenar";

    public static String CONFIRM_MESSAGE_KEY = "ReportDesigner.Descargar";

    public static String RELOAD_REPORT_KEY = "ReportDesigner.RecargarInforme";

    public static String ENTITIES_COMBO_KEY = "ReportDesigner.ComboEntidades";

    public static String GROUP_KEY = "group";

    public static String EMPTY_ENTITY_KEY = "ReportDesigner.EntidadVacia";

    public static String SIMPLE_LINE_KEY = "report.one_line";

    public static String MULTILINE_KEY = "report.multiline";

    public static String GROUP_BY_DATE_TIME_KEY = "report.group_by_date_time";

    public static String GROUP_BY_DATE_KEY = "report.group_by_date";

    public static String GROUP_BY_MONTH_KEY = "report.group_by_month";

    public static String GROUP_BY_MONTH_AND_YEAR_KEY = "report.group_by_month_and_year";

    public static String GROUP_BY_QUARTER_KEY = "report.group_by_quarter";

    public static String GROUP_BY_QUARTER_AND_YEAR_KEY = "report.group_by_quarter_and_year";

    public static String GROUP_BY_YEAR_KEY = "report.group_by_year";

    public static String DYNAMIC_SELECTION_TIP_KEY = "report.multiline_setup_tip";

    public static String GROUP_TIP_KEY = "report.group_tip";

    public static String ORDER_TIP_KEY = "report.order_tip";

    public static String DYNAMIC_UPDATE_CHECK_KEY = "report.update_dinamically";

    public static String LOAD_TEMPLATE_KEY = "ReportDesigner.CargarPlantilla";

    public static String SAVE_TEMPLATE_KEY = "ReportDesigner.GuardarPlantilla";

    public static String DELETE_TEMPLATE_KEY = "ReportDesigner.EliminarPlantilla";

    // Variables of reportDialog

    /** An instance of report engine */

    protected EntityReferenceLocator locator = null;

    // Containers in report dialog

    protected Window container = null;

    protected JScrollPane scroll = new JScrollPane();

    protected JList printingColumnList = new JList();

    protected JPanel configurationPanel = new JPanel(new GridBagLayout()) {

        @Override
        public String getName() {
            return Form.FORMBODYPANEL;
        }
    };

    protected JPanel entitiesPanel = new JPanel(new BorderLayout());

    protected JList groupList = new JList();

    protected Object[] multigroups;

    protected JPanel groupListPanel;

    protected JList functionList = new JList();

    protected JPanel functionListPanel = new JPanel();

    protected JPanel printingColumnsPanel = new JPanel();

    protected JPanel templatesPanel = new JPanel();

    protected JScrollPane sqlQueryPanel = new JScrollPane();

    protected TableModel model;

    protected com.ontimize.db.query.QueryExpression query;

    protected ResourceBundle bundle;

    protected boolean isTable = true;

    protected ReportStore[] rs;

    // Components in dialog

    protected OrderWindow order;

    protected JTextArea sqlQueryText;

    protected JComboBox templateCombo = new JComboBox();

    protected String reportDescription;

    protected String title;

    protected String description;

    protected List orderCols;

    protected List<String> configuredColumns = new ArrayList<String>();

    protected Map<String, Integer> columnFixedWidth = new HashMap<String, Integer>();

    protected Map<String, Integer> columnAlignment = new HashMap<String, Integer>();

    protected Form.FormButton optionsButton = new Form.FormButton(ImageManager.getIcon(ImageManager.OPTIONS));

    protected JPopupMenu optionsMenu;

    protected JCheckBoxMenuItem grid1CheckMenu = new JCheckBoxMenuItem(DefaultReportDialog.GRID_KEY);

    protected JCheckBoxMenuItem rowNumber2CheckMenu = new JCheckBoxMenuItem(DefaultReportDialog.INCLUDE_ROW_NUMBER_KEY);

    protected JCheckBoxMenuItem includeColumnName3CheckMenu = new JCheckBoxMenuItem(
            DefaultReportDialog.INCLUDE_COLUMN_NAMES_KEY);

    protected JCheckBoxMenuItem underlineLine4CheckMenu = new JCheckBoxMenuItem(
            DefaultReportDialog.UNDERLINE_LINES_KEY);

    protected JCheckBoxMenuItem fitHead5CheckMenu = new JCheckBoxMenuItem(DefaultReportDialog.FIT_HEADER_KEY);

    protected JCheckBoxMenuItem dynamicHead6CheckMenu = new JCheckBoxMenuItem(DefaultReportDialog.DYNAMIC_HEADER_KEY);

    protected JCheckBoxMenuItem hideGroupDetail7CheckMenu = new JCheckBoxMenuItem(
            DefaultReportDialog.HIDE_GROUP_DETAIL_KEY);

    protected JCheckBoxMenuItem groupStartInNewPageCheckMenu = new JCheckBoxMenuItem(
            DefaultReportDialog.GROUP_START_IN_NEW_PAGE);

    protected JCheckBoxMenuItem firstGroupStartInNewPageCheckMenu = new JCheckBoxMenuItem(
            DefaultReportDialog.FIRST_GROUP_START_IN_NEW_PAGE);

    protected RolloverButton bStore = new RolloverButton(ImageManager.getIcon(ImageManager.STORE));

    protected RolloverButton bSQL = new RolloverButton(ImageManager.getIcon(ImageManager.SQL));

    protected RolloverButton bRefreshReport = new RolloverButton(ImageManager.getIcon(ImageManager.REFRESH));

    protected JComboBox entitiesCombo = new JComboBox();

    protected JButton orderButton = new JButton(ImageManager.getIcon(ImageManager.SORT));

    protected JPopupMenu operationTypePopup = new JPopupMenu();

    protected ButtonGroup opTypeButtonGroup = new ButtonGroup();

    protected JRadioButtonMenuItem sumOpMenu = new JRadioButtonMenuItem(DefaultReportDialog.SUM_OP_KEY);

    protected JRadioButtonMenuItem minimumOpMenu = new JRadioButtonMenuItem(DefaultReportDialog.MINIMUM_OP_KEY);

    protected JRadioButtonMenuItem averageOpMenu = new JRadioButtonMenuItem(DefaultReportDialog.AVERAGE_OP_KEY);

    protected JRadioButtonMenuItem maximumOpMenu = new JRadioButtonMenuItem(DefaultReportDialog.MAXIMUM_OP_KEY);

    protected SelectableFunctionItem currentItem = null;

    protected SelectableDateGroupItem currentDateGroupItem = null;

    protected JPopupMenu groupPopup = new JPopupMenu();

    protected JMenuItem groupMenu = new JMenuItem(DefaultReportDialog.GROUP_KEY);

    protected JPopupMenu ascendingDescendingPopup = new JPopupMenu();

    protected JRadioButtonMenuItem ascendingOpMenu = new JRadioButtonMenuItem("Ascendente");

    protected JRadioButtonMenuItem descendingOpMenu = new JRadioButtonMenuItem("Descendente");

    protected JPopupMenu multilinePopup = new JPopupMenu();

    protected ButtonGroup multilineGroupButton = new ButtonGroup();

    protected JRadioButtonMenuItem simpleLineMenu = new JRadioButtonMenuItem(DefaultReportDialog.SIMPLE_LINE_KEY);

    protected JRadioButtonMenuItem multilineMenu = new JRadioButtonMenuItem(DefaultReportDialog.MULTILINE_KEY);

    protected SelectableDynamicItem currentDynamicItem = null;

    protected JPopupMenu confMenu;

    protected JPopupMenu groupByDatePopup = new JPopupMenu();

    protected ButtonGroup groupByDateGroupButton = new ButtonGroup();

    protected JRadioButtonMenuItem groupByDateTime = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_DATE_TIME_KEY));

    protected JRadioButtonMenuItem groupByDate = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_DATE_KEY));

    protected JRadioButtonMenuItem groupByMonth = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_MONTH_KEY));

    protected JRadioButtonMenuItem groupByMonthAndYear = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_MONTH_AND_YEAR_KEY));

    protected JRadioButtonMenuItem groupByQuarter = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_QUARTER_KEY));

    protected JRadioButtonMenuItem groupByQuarterAndYear = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_QUARTER_AND_YEAR_KEY));

    protected JRadioButtonMenuItem groupByYear = new JRadioButtonMenuItem(
            ApplicationManager.getTranslation(DefaultReportDialog.GROUP_BY_YEAR_KEY));

    protected ItemListener listener;

    protected DeleteItemListener deleteListener;

    /**
     * Listener to share report with other users
     */
    protected SharedItemListener shareListener;

    /**
     * Edit target user list
     */
    protected SharedEditTargetItemListener shareEditTargetListener;

    /**
     * Load shared report
     */
    protected SharedLoadItemListener shareLoadListener;

    /**
     * Obtain message from shared report
     */
    protected SharedMessageItemListener sharedMessageItemListener;

    /**
     * Edit shared report
     */
    protected SharedEditItemListener shareEditListener;

    /**
     * Delete report listener report
     */
    protected SharedDeleteItemListener shareDeleteListener;

    /**
     * Delete target listener report
     */
    protected SharedDeleteTargetItemListener shareDeleteTargetListener;

    /**
     * Check if exists Shared Remote Reference to share preferences
     */
    protected boolean shareRemoteReferenceReports = false;

    private final OrderListener orderTypeListener = new OrderListener(this);

    private final ActionListener opTypeListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == DefaultReportDialog.this.sumOpMenu) {
                DefaultReportDialog.this.currentItem.setOperation(ReportUtils.SUM);
                DefaultReportDialog.this.getFunctionList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.maximumOpMenu) {
                DefaultReportDialog.this.currentItem.setOperation(ReportUtils.MAX);
                DefaultReportDialog.this.getFunctionList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.minimumOpMenu) {
                DefaultReportDialog.this.currentItem.setOperation(ReportUtils.MIN);
                DefaultReportDialog.this.getFunctionList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.averageOpMenu) {
                DefaultReportDialog.this.currentItem.setOperation(ReportUtils.AVG);
                DefaultReportDialog.this.getFunctionList().repaint();
                DefaultReportDialog.this.updateReport();
            } else {
            }
        }

    };

    private final ActionListener groupByDateListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == DefaultReportDialog.this.groupByDateTime) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_DATE_TIME);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByDate) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_DATE);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByMonth) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_MONTH);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByMonthAndYear) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_MONTH_AND_YEAR);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByQuarter) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_QUARTER);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByQuarterAndYear) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_QUARTER_AND_YEAR);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else if (e.getSource() == DefaultReportDialog.this.groupByYear) {
                DefaultReportDialog.this.currentDateGroupItem.setOperation(ReportUtils.GROUP_BY_YEAR);
                DefaultReportDialog.this.getGroupList().repaint();
                DefaultReportDialog.this.updateReport();
            } else {
            }
        }

    };

    private final ActionListener groupListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultReportDialog.this.groupColumns();
        }
    };

    protected PopupListener operationSelectionListener = new PopupListener(this);

    protected PopupGroupDateListener dateGroupSelectionListener = new PopupGroupDateListener(this);

    protected OrderedPopupListener orderListener = new OrderedPopupListener();

    protected DynamicPopupListener dynamicSelectionListener = new DynamicPopupListener(this);

    protected UpdateReportListener updateReportListener = new UpdateReportListener(this);

    protected JPanel chartPanel = new JPanel(new BorderLayout()) {

        @Override
        public String getName() {
            return Form.FORMBODYPANEL;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max(650, d.width);
            d.height = Math.max(550, d.height);
            return d;
        }
    };

    protected ButtonSelection loadButton = new ButtonSelection(true) {

    };

    protected Form.FormButton saveButton = new Form.FormButton();

    protected String reportName = "";

    protected JToggleButton updateCheck;

    protected java.util.List templates;

    protected String dscr;

    protected String user;

    protected String preferenceKey;

    protected ApplicationPreferences prefs;

    protected ReportSetupDialog reportConfigurationDialog;

    protected ReportDeleteDialog deleteReportDialog;

    protected JButton upButton = new JButton();

    protected JButton downButton = new JButton();

    protected JButton allUpButton = new JButton();

    protected JButton allDownButton = new JButton();

    protected JButton upGroupButton = new JButton();

    protected JButton downGroupButton = new JButton();

    protected JButton allUpGroupButton = new JButton();

    protected JButton allDownGroupButton = new JButton();

    protected ColumnSelectionListener columnSelectionListener = new ColumnSelectionListener(this);

    protected GroupColumnListener groupColumnListener = new GroupColumnListener(this);

    protected FunctionListener functionListener = new FunctionListener(this);

    protected DateGroupListener dateGroupListener = new DateGroupListener(this);

    protected Table table;

    public DefaultReportDialog(Frame f, TableModel m, ResourceBundle res, java.util.List templateList, String tit,
            String dscr, String user, String preferenceKey,
            ApplicationPreferences prefs) {
        this(f, m, res, templateList, tit, dscr, user, preferenceKey, prefs, null);

    }

    public DefaultReportDialog(Frame f, TableModel m, ResourceBundle res, java.util.List templateList, String tit,
            String dscr, String user, String preferenceKey,
            ApplicationPreferences prefs, Table table) {

        this.locator = ApplicationManager.getApplication().getReferenceLocator();
        try {
            this.shareRemoteReferenceReports = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                        this.locator.getSessionId()) != null ? true : false;
        } catch (Exception e) {
            DefaultReportDialog.logger.trace(null, e);
            this.shareRemoteReferenceReports = false;
        }

        if (this.reportEngine == null) {
            try {
                this.reportEngine = ReportManager.getReportEngine();
                this.reportEngine.setDefaultReportDialog(this);
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
                // No reports enabled
            }
        }
        this.model = m;
        
        
        this.container = new EJFrame(DefaultReportDialog.TITLE_KEY);
        if (this.container != null) {
            ((Frame) this.container).setIconImage(ApplicationManager.getApplication().getFrame().getIconImage());
        }
        
        
        this.bundle = res;
        if ((templateList == null) || (templateList.size() == 0)) {
            this.initTemplateList();
        } else {
            this.templates = templateList;
        }
        this.title = tit;
        this.dscr = dscr;
        this.user = user;
        this.preferenceKey = preferenceKey;
        this.prefs = prefs;
        this.isTable = true;
        if (table != null) {
            this.table = table;
        }
        this.init();
    }

    public DefaultReportDialog(ReportStore[] rs, EntityReferenceLocator referenceLocator, ResourceBundle res,
            java.util.List templateList, String tit) {

        if (this.reportEngine == null) {
            try {
                this.reportEngine = ReportManager.getReportEngine();
                this.reportEngine.setDefaultReportDialog(this);
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
                // No reports enabled
            }
        }
        this.container = new EJFrame(ApplicationManager.getTranslation(DefaultReportDialog.TITLE_KEY, this.bundle));
        if (this.container != null) {
            ((Frame) this.container).setIconImage(ApplicationManager.getApplication().getFrame().getIconImage());
        }

        this.bundle = res;
        this.rs = rs;
        this.title = tit;
        this.locator = referenceLocator;

        try {
            this.shareRemoteReferenceReports = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                        this.locator.getSessionId()) != null ? true : false;
        } catch (Exception e) {
            DefaultReportDialog.logger.trace(null, e);
            this.shareRemoteReferenceReports = false;
        }

        this.isTable = false;
        if (templateList == null) {
            this.initTemplateList();
        } else {
            this.templates = templateList;
        }
        try {
            this.reportEngine = ReportManager.getReportEngine();
        } catch (Exception e) {
            DefaultReportDialog.logger.error(null, e);
            // No reports enabled
        }
        this.init();
    }

    public DefaultReportDialog(Frame f, ReportStore[] rs, EntityReferenceLocator referenceLocator, ResourceBundle res,
            java.util.List templateList, String tit, String dscr) {

        if (this.reportEngine == null) {
            try {
                this.reportEngine = ReportManager.getReportEngine();
                this.reportEngine.setDefaultReportDialog(this);
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
                // No reports enabled
            }
        }
        this.container = new JFrame(ApplicationManager.getTranslation(DefaultReportDialog.TITLE_KEY, this.bundle));
        if (f != null) {
            ((Frame) this.container).setIconImage(f.getIconImage());
        }

        this.bundle = res;
        this.title = tit;
        this.rs = rs;
        this.dscr = dscr;
        this.locator = referenceLocator;

        try {
            this.shareRemoteReferenceReports = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                        this.locator.getSessionId()) != null ? true : false;
        } catch (Exception e) {
            DefaultReportDialog.logger.trace(null, e);
            this.shareRemoteReferenceReports = false;
        }

        this.isTable = false;
        if (templateList == null) {
            // this.templates = ReportUtils.getCustomReportTemplates();
            this.initTemplateList();
        } else {
            this.templates = templateList;
        }
        try {
            this.reportEngine = ReportManager.getReportEngine();
        } catch (Exception e) {
            DefaultReportDialog.logger.error(null, e);
            // No reports enabled
        }
        this.init();
    }

    public DefaultReportDialog(Dialog d, TableModel m, ResourceBundle res, java.util.List templateList, String tit,
            String dscr, String user, String preferenceKey,
            ApplicationPreferences prefs) {
        this(d, m, res, templateList, tit, dscr, user, preferenceKey, prefs, null);

    }

    public DefaultReportDialog(Dialog d, TableModel m, ResourceBundle res, java.util.List templateList, String tit,
            String dscr, String user, String preferenceKey,
            ApplicationPreferences prefs, Table table) {

        this.locator = ApplicationManager.getApplication().getReferenceLocator();
        try {
            this.shareRemoteReferenceReports = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                        this.locator.getSessionId()) != null ? true : false;
        } catch (Exception e) {
            DefaultReportDialog.logger.trace(null, e);
            this.shareRemoteReferenceReports = false;
        }

        if (table != null) {
            this.table = table;
        }

        if (this.reportEngine == null) {
            try {
                this.reportEngine = ReportManager.getReportEngine();
                this.reportEngine.setDefaultReportDialog(this);
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
                // No reports enabled
            }
        }
        this.container = new EJFrame(ApplicationManager.getTranslation(DefaultReportDialog.TITLE_KEY, this.bundle));
        if (this.container != null) {
            ((Frame) this.container).setIconImage(ApplicationManager.getApplication().getFrame().getIconImage());
        }
        this.model = m;
        this.title = tit;
        this.bundle = res;
        if (templateList == null) {
            this.initTemplateList();
        } else {
            this.templates = templateList;
        }
        this.user = user;
        this.dscr = dscr;
        this.prefs = prefs;
        this.preferenceKey = preferenceKey;
        this.isTable = true;
        if (this.reportEngine == null) {
            try {
                this.reportEngine = ReportManager.getReportEngine();
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
                // No reports enabled
            }
        }
        this.init();
    }

    public void initTemplateList() {
        this.templates = this.reportEngine.getDefaultTemplates();
    }

    public void setSize(int width, int height) {
        this.container.setSize(width, height);
    }

    public void setVisible(boolean b) {
        this.container.setVisible(b);
    }

    public void setLocation(int x, int y) {
        this.container.setLocation(x, y);
    }

    public void init() {
        if (!this.isTable) {
            if (this.locator != null) {
                try {
                    java.util.List entityList = ((com.ontimize.report.RemoteReportReferencer) this.locator)
                        .getReportEntityNames(this.locator.getSessionId());
                    DefaultComboBoxModel model = null;
                    if (entityList instanceof Vector) {
                        entityList.add(0, DefaultReportDialog.EMPTY_ENTITY_KEY);
                        model = new DefaultComboBoxModel((Vector) entityList);
                    } else {
                        Vector aux = new Vector();
                        aux.add(DefaultReportDialog.EMPTY_ENTITY_KEY);
                        for (int i = 0; i < entityList.size(); i++) {
                            aux.add(entityList.get(i));
                        }
                        model = new DefaultComboBoxModel(aux);
                    }
                    this.entitiesCombo.setModel(model);
                } catch (Exception ex1) {
                    DefaultReportDialog.logger.error(null, ex1);
                }
            }

            this.entitiesCombo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Object o = DefaultReportDialog.this.entitiesCombo.getSelectedItem();
                    if ((o != null) && !o.equals(DefaultReportDialog.EMPTY_ENTITY_KEY)) {
                        try {
                            DefaultReportDialog.this.model = EntityResultUtils
                                .createTableModel(DefaultReportDialog.this.locator.getEntityReference((String) o)
                                    .query(new Hashtable(), new Vector(),
                                            DefaultReportDialog.this.locator.getSessionId()));
                        } catch (Exception e1) {
                            DefaultReportDialog.logger.error(null, e1);
                        }
                        DefaultReportDialog.this.setReportEntity((String) o);
                        DefaultReportDialog.this.clearReport();

                        DefaultReportDialog.this.query = null;
                        DefaultReportDialog.this.sqlQueryText.setText(" ");
                        DefaultReportDialog.this.bStore.setEnabled(false);
                        DefaultReportDialog.this.bSQL.setEnabled(true);
                        DefaultReportDialog.this.bRefreshReport.setEnabled(true);
                    } else {
                        DefaultReportDialog.this.bSQL.setEnabled(false);
                        DefaultReportDialog.this.bRefreshReport.setEnabled(false);
                        DefaultListModel m = (DefaultListModel) DefaultReportDialog.this.printingColumnList.getModel();
                        m.clear();
                        m = (DefaultListModel) DefaultReportDialog.this.groupList.getModel();
                        m.clear();
                        m = (DefaultListModel) DefaultReportDialog.this.functionList.getModel();
                        m.clear();
                    }
                }
            });
            this.updateCheck = new JToggleButton(DefaultReportDialog.DYNAMIC_UPDATE_CHECK_KEY, false);
        } else {
            this.updateCheck = new JToggleButton(DefaultReportDialog.DYNAMIC_UPDATE_CHECK_KEY, true);

        }

        class EAction extends AbstractAction {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    DefaultReportDialog.logger.debug("Evento {}", e);
                    String s = DefaultReportDialog.this.getXMLTemplate();
                    JOptionPane.showMessageDialog(DefaultReportDialog.this.container, s);
                    ApplicationManager.copyToClipboard(s);
                } catch (Exception ex) {
                    DefaultReportDialog.logger.error(null, ex);
                }
            }

        }

        this.setAction(KeyEvent.VK_1, InputEvent.CTRL_MASK, new EAction(), "Ver XML");

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Vector vColumns = this.getColumns();
        Vector items = this.createPrintingColumn(vColumns);
        this.setPrintingColumns(items);

        this.printingColumnList.setCellRenderer(new SelectableConfigItemsListCellRenderer());

        Vector items2 = this.createGroupColumn(vColumns);
        this.setGroupColumns(items2);

        this.groupList.setCellRenderer(new SelectableItemsListCellRenderer());

        Vector vYAxisColumns = this.createFunctionColumns(vColumns);
        this.setFunctionColumns(vYAxisColumns);

        this.functionList.setCellRenderer(new SelectableItemsListCellRenderer());

        this.includeColumnName3CheckMenu.setSelected(true);
        this.underlineLine4CheckMenu.setSelected(true);
        this.optionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (DefaultReportDialog.this.optionsMenu == null) {
                    DefaultReportDialog.this.reportEngine.buildOptions();
                }
                Dimension d = DefaultReportDialog.this.optionsButton.getSize();
                DefaultReportDialog.this.optionsMenu.show((Component) e.getSource(), 0, d.height);
            }
        });

        this.bStore.setEnabled(false);
        this.loadButton
            .setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.LOAD_TEMPLATE_KEY, this.bundle));
        this.loadButton.setFocusable(false);
        this.saveButton
            .setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.SAVE_TEMPLATE_KEY, this.bundle));
        this.optionsButton.setToolTipText(ApplicationManager.getTranslation("options", this.bundle));
        this.bSQL.setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.SQL_QUERY_KEY, this.bundle));
        this.bStore
            .setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.REPORT_STORE_KEY, this.bundle));

        this.ascendingDescendingPopup.add(this.ascendingOpMenu);
        this.ascendingDescendingPopup.addSeparator();
        this.ascendingDescendingPopup.add(this.descendingOpMenu);
        this.ascendingOpMenu.setSelected(true);

        this.operationTypePopup.add(this.sumOpMenu);
        this.operationTypePopup.add(this.averageOpMenu);
        this.operationTypePopup.add(this.maximumOpMenu);
        this.operationTypePopup.add(this.minimumOpMenu);

        this.groupPopup.add(this.groupMenu);

        this.opTypeButtonGroup.add(this.sumOpMenu);
        this.opTypeButtonGroup.add(this.averageOpMenu);
        this.opTypeButtonGroup.add(this.maximumOpMenu);
        this.opTypeButtonGroup.add(this.minimumOpMenu);
        this.sumOpMenu.setSelected(true);

        this.groupMenu.addActionListener(this.groupListener);

        this.maximumOpMenu.addActionListener(this.opTypeListener);
        this.averageOpMenu.addActionListener(this.opTypeListener);
        this.sumOpMenu.addActionListener(this.opTypeListener);
        this.minimumOpMenu.addActionListener(this.opTypeListener);

        this.groupByDateTime.addActionListener(this.groupByDateListener);
        this.groupByDate.addActionListener(this.groupByDateListener);
        this.groupByMonth.addActionListener(this.groupByDateListener);
        this.groupByMonthAndYear.addActionListener(this.groupByDateListener);
        this.groupByQuarter.addActionListener(this.groupByDateListener);
        this.groupByQuarterAndYear.addActionListener(this.groupByDateListener);
        this.groupByYear.addActionListener(this.groupByDateListener);

        this.ascendingOpMenu.addActionListener(this.orderTypeListener);
        this.descendingOpMenu.addActionListener(this.orderTypeListener);

        this.multilineGroupButton.add(this.multilineMenu);
        this.multilineGroupButton.add(this.simpleLineMenu);
        this.multilinePopup.add(this.simpleLineMenu);
        this.multilinePopup.add(this.multilineMenu);
        this.simpleLineMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Vector vColumns = DefaultReportDialog.this.getSelectedPrintingColumns();
                if (vColumns.contains(DefaultReportDialog.this.currentDynamicItem.getText())) {
                    DefaultReportDialog.this.currentDynamicItem.setDynamic(false);
                    DefaultReportDialog.this.printingColumnList.repaint();
                    DefaultReportDialog.this.updateReport();
                } else {
                }
            }
        });
        this.multilineMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Vector vColumns = DefaultReportDialog.this.getSelectedPrintingColumns();
                if (vColumns.contains(DefaultReportDialog.this.currentDynamicItem.getText())) {
                    DefaultReportDialog.this.currentDynamicItem.setDynamic(true);
                    DefaultReportDialog.this.printingColumnList.repaint();
                    DefaultReportDialog.this.updateReport();
                } else {
                }
            }
        });

        // Now the listener
        this.printingColumnList.addMouseListener(this.columnSelectionListener);
        this.printingColumnList.addMouseListener(this.dynamicSelectionListener);
        this.printingColumnList.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.printingColumnList,
                        DefaultReportDialog.this.allUpButton, DefaultReportDialog.this.upButton,
                        DefaultReportDialog.this.downButton, DefaultReportDialog.this.allDownButton);
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });

        this.printingColumnList.setToolTipText(
                ApplicationManager.getTranslation(DefaultReportDialog.DYNAMIC_SELECTION_TIP_KEY, this.bundle));
        this.groupList.addMouseListener(this.groupColumnListener);
        this.groupList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
            }
        });

        this.groupList.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });

        this.groupList
            .setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.GROUP_TIP_KEY, this.bundle));
        this.groupList.addMouseListener(this.dateGroupListener);
        this.groupList.addMouseListener(this.dateGroupSelectionListener);

        this.functionList.addMouseListener(this.functionListener);
        this.functionList.addMouseListener(this.operationSelectionListener);

        this.printingColumnsPanel = new JPanel(new BorderLayout());
        this.printingColumnsPanel.setBorder(new TitledBorder(DefaultReportDialog.PRINTING_COLUMN_KEY));
        this.printingColumnList.setVisibleRowCount(6);
        JScrollPane scrollPane = new JScrollPane(this.printingColumnList);
        this.printingColumnsPanel.add(scrollPane);

        this.groupListPanel = new JPanel(new BorderLayout());
        this.groupListPanel.setBorder(new TitledBorder(DefaultReportDialog.GROUPS_KEY));
        this.groupList.setVisibleRowCount(6);
        this.groupListPanel.add(new JScrollPane(this.groupList));

        this.functionListPanel = new JPanel(new BorderLayout());
        this.functionListPanel.setBorder(new TitledBorder(DefaultReportDialog.FUNCTION_KEY));
        this.functionList.setVisibleRowCount(3);
        this.functionListPanel.add(new JScrollPane(this.functionList));

        JPanel jpButtonsPanel = new JPanel(new GridLayout(0, 1));
        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.PAGE_AXIS));
        toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel jpGroupsButtonsPanel = new JPanel(new GridLayout(0, 1));

        JPanel toolbarGroups = new JPanel();
        toolbarGroups.setLayout(new BoxLayout(toolbarGroups, BoxLayout.PAGE_AXIS));
        toolbarGroups.setBorder(new EmptyBorder(0, 0, 0, 0));
        JPanel panel = new JPanel(new GridBagLayout());
        toolbar.add(this.allUpButton);
        toolbar.add(this.upButton);
        toolbar.add(this.downButton);
        toolbar.add(this.allDownButton);
        panel.add(toolbar,
                new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1, 1, GridBagConstraints.NORTH,
                        GridBagConstraints.VERTICAL, new Insets(2, 2, 2, 2), 0, 0));
        panel.add(this.orderButton,
                new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 0, 0, GridBagConstraints.SOUTH,
                        GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        RolloverHandler.getInstance().add(this.upButton);
        RolloverHandler.getInstance().add(this.downButton);
        RolloverHandler.getInstance().add(this.allUpButton);
        RolloverHandler.getInstance().add(this.allDownButton);
        RolloverHandler.getInstance().add(this.orderButton);

        jpButtonsPanel.add(panel);
        JPanel panelAux = new JPanel();
        panelAux.add(jpButtonsPanel);
        this.printingColumnsPanel.add(panel, BorderLayout.EAST);
        this.printingColumnsPanel.add(new JScrollPane(this.printingColumnList));

        RolloverHandler.getInstance().add(this.upGroupButton);
        RolloverHandler.getInstance().add(this.downGroupButton);
        RolloverHandler.getInstance().add(this.allUpGroupButton);
        RolloverHandler.getInstance().add(this.allDownGroupButton);

        toolbarGroups.add(this.allUpGroupButton);
        toolbarGroups.add(this.upGroupButton);
        toolbarGroups.add(this.downGroupButton);
        toolbarGroups.add(this.allDownGroupButton);
        jpGroupsButtonsPanel.add(toolbarGroups);
        JPanel panelAuxGrupos = new JPanel();
        panelAuxGrupos.add(jpGroupsButtonsPanel);
        this.groupListPanel.add(panelAuxGrupos, BorderLayout.EAST);
        this.groupListPanel.add(new JScrollPane(this.groupList));

        ImageIcon startIcon = ImageManager.getIcon(ImageManager.START_2_VERTICAL);
        if (startIcon != null) {
            this.allUpButton.setIcon(startIcon);
            this.allUpGroupButton.setIcon(startIcon);
        } else {
            this.allUpButton.setText("Up+");
            this.allUpGroupButton.setText("Up+");
        }

        ImageIcon prevIcon = ImageManager.getIcon(ImageManager.PREVIOUS_2_VERTICAL);
        if (prevIcon != null) {
            this.upButton.setIcon(prevIcon);
            this.upGroupButton.setIcon(prevIcon);
        } else {
            this.upButton.setText("Up");
            this.upGroupButton.setText("Up");
        }

        ImageIcon nextIcon = ImageManager.getIcon(ImageManager.NEXT_2_VERTICAL);
        if (nextIcon != null) {
            this.downButton.setIcon(nextIcon);
            this.downGroupButton.setIcon(nextIcon);
        } else {
            this.downGroupButton.setText("Down");
        }

        ImageIcon endIcon = ImageManager.getIcon(ImageManager.END_2_VERTICAL);
        if (endIcon != null) {
            this.allDownButton.setIcon(endIcon);
            this.allDownGroupButton.setIcon(endIcon);
        } else {
            this.allDownButton.setText("Down+");
            this.allDownGroupButton.setText("Down+");
        }

        this.upButton.setMargin(new Insets(0, 0, 0, 0));
        this.downButton.setMargin(new Insets(0, 0, 0, 0));
        this.allUpButton.setMargin(new Insets(0, 0, 0, 0));
        this.allDownButton.setMargin(new Insets(0, 0, 0, 0));
        try {
            this.upButton.setToolTipText(this.bundle.getString(DefaultReportDialog.UP_BUTTON_KEY));
            this.downButton.setToolTipText(this.bundle.getString(DefaultReportDialog.DOWN_BUTTON_KEY));
            this.allUpButton.setToolTipText(this.bundle.getString(DefaultReportDialog.ALL_UP_BUTTON_KEY));
            this.allDownButton.setToolTipText(this.bundle.getString(DefaultReportDialog.ALL_DOWN_BUTTON_KEY));
            this.upGroupButton.setToolTipText(this.bundle.getString(DefaultReportDialog.UP_GROUP_BUTTON_KEY));
            this.downGroupButton.setToolTipText(this.bundle.getString(DefaultReportDialog.GROUP_DOWN_BUTTON_KEY));
            this.allUpGroupButton.setToolTipText(this.bundle.getString(DefaultReportDialog.ALL_UP_GROUP_BUTTON_KEY));
            this.allDownGroupButton
                .setToolTipText(this.bundle.getString(DefaultReportDialog.ALL_DOWN_GROUP_BUTTON_KEY));
        } catch (Exception ex) {
            DefaultReportDialog.logger.trace(null, ex);
            this.upButton.setToolTipText(DefaultReportDialog.UP_BUTTON_KEY);
            this.downButton.setToolTipText(DefaultReportDialog.DOWN_BUTTON_KEY);
            this.allUpButton.setToolTipText(DefaultReportDialog.ALL_UP_BUTTON_KEY);
            this.allDownButton.setToolTipText(DefaultReportDialog.ALL_DOWN_BUTTON_KEY);
            this.upGroupButton.setToolTipText(DefaultReportDialog.UP_GROUP_BUTTON_KEY);
            this.downGroupButton.setToolTipText(DefaultReportDialog.GROUP_DOWN_BUTTON_KEY);
            this.allUpGroupButton.setToolTipText(DefaultReportDialog.ALL_UP_GROUP_BUTTON_KEY);
            this.allDownGroupButton.setToolTipText(DefaultReportDialog.ALL_DOWN_GROUP_BUTTON_KEY);
        }

        this.upGroupButton.setMargin(new Insets(0, 0, 0, 0));
        this.downGroupButton.setMargin(new Insets(0, 0, 0, 0));
        this.allUpGroupButton.setMargin(new Insets(0, 0, 0, 0));
        this.allDownGroupButton.setMargin(new Insets(0, 0, 0, 0));

        // Initialize the state of the buttons
        DefaultReportDialog.checkListStatusButtons(this.printingColumnList, this.allUpButton, this.upButton,
                this.downButton, this.allDownButton);
        DefaultReportDialog.checkListStatusButtons(this.groupList, this.allUpGroupButton, this.upGroupButton,
                this.downGroupButton, this.allDownGroupButton);

        this.allDownButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.printingColumnList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.printingColumnList.getModel();
                if ((index >= 0) && (index < (model.getSize() - 1))) {
                    Object act = model.get(index);
                    Object sig = model.get((index + model.getSize()) - 1 - index);
                    model.setElementAt(sig, index);
                    model.setElementAt(act, (index + model.getSize()) - 1 - index);
                    DefaultReportDialog.this.printingColumnList.setSelectedIndex((index + model.getSize()) - 1 - index);
                    DefaultReportDialog.this.printingColumnList
                        .ensureIndexIsVisible((index + model.getSize()) - 1 - index);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.printingColumnList,
                        DefaultReportDialog.this.allUpButton, DefaultReportDialog.this.upButton,
                        DefaultReportDialog.this.downButton, DefaultReportDialog.this.allDownButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.downButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.printingColumnList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.printingColumnList.getModel();
                if ((index >= 0) && (index < (model.getSize() - 1))) {
                    Object act = model.get(index);
                    Object sig = model.get(index + 1);
                    model.setElementAt(sig, index);
                    model.setElementAt(act, index + 1);
                    DefaultReportDialog.this.printingColumnList.setSelectedIndex(index + 1);
                    DefaultReportDialog.this.printingColumnList.ensureIndexIsVisible(index + 1);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.printingColumnList,
                        DefaultReportDialog.this.allUpButton, DefaultReportDialog.this.upButton,
                        DefaultReportDialog.this.downButton, DefaultReportDialog.this.allDownButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.upButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int iIndex = DefaultReportDialog.this.printingColumnList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.printingColumnList.getModel();
                if (iIndex > 0) {
                    Object act = model.get(iIndex);
                    model.remove(iIndex);
                    model.insertElementAt(act, iIndex - 1);
                    DefaultReportDialog.this.printingColumnList.setSelectedIndex(iIndex - 1);
                    DefaultReportDialog.this.printingColumnList.ensureIndexIsVisible(iIndex - 1);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.printingColumnList,
                        DefaultReportDialog.this.allUpButton, DefaultReportDialog.this.upButton,
                        DefaultReportDialog.this.downButton, DefaultReportDialog.this.allDownButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.allUpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.printingColumnList.getSelectedIndex();
                DefaultListModel dlModel = (DefaultListModel) DefaultReportDialog.this.printingColumnList.getModel();
                if (index > 0) {
                    Object act = dlModel.get(index);
                    dlModel.remove(index);
                    dlModel.insertElementAt(act, 0);
                    DefaultReportDialog.this.printingColumnList.setSelectedIndex(0);
                    DefaultReportDialog.this.printingColumnList.ensureIndexIsVisible(0);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.printingColumnList,
                        DefaultReportDialog.this.allUpButton, DefaultReportDialog.this.upButton,
                        DefaultReportDialog.this.downButton, DefaultReportDialog.this.allDownButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.allDownGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.groupList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.groupList.getModel();
                if ((index >= 0) && (index < (model.getSize() - 1))) {
                    Object act = model.get(index);
                    Object sig = model.get((index + model.getSize()) - 1 - index);
                    model.setElementAt(sig, index);
                    model.setElementAt(act, (index + model.getSize()) - 1 - index);
                    DefaultReportDialog.this.groupList.setSelectedIndex((index + model.getSize()) - 1 - index);
                    DefaultReportDialog.this.groupList.ensureIndexIsVisible((index + model.getSize()) - 1 - index);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.downGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.groupList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.groupList.getModel();
                if ((index >= 0) && (index < (model.getSize() - 1))) {
                    Object act = model.get(index);
                    Object sig = model.get(index + 1);
                    model.setElementAt(sig, index);
                    model.setElementAt(act, index + 1);
                    DefaultReportDialog.this.groupList.setSelectedIndex(index + 1);
                    DefaultReportDialog.this.groupList.ensureIndexIsVisible(index + 1);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.upGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.groupList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.groupList.getModel();
                if (index > 0) {
                    Object act = model.get(index);
                    model.remove(index);
                    model.insertElementAt(act, index - 1);
                    DefaultReportDialog.this.groupList.setSelectedIndex(index - 1);
                    DefaultReportDialog.this.groupList.ensureIndexIsVisible(index - 1);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.allUpGroupButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = DefaultReportDialog.this.groupList.getSelectedIndex();
                DefaultListModel model = (DefaultListModel) DefaultReportDialog.this.groupList.getModel();
                if (index > 0) {
                    Object act = model.get(index);
                    model.remove(index);
                    model.insertElementAt(act, 0);
                    DefaultReportDialog.this.groupList.setSelectedIndex(0);
                    DefaultReportDialog.this.groupList.ensureIndexIsVisible(0);
                }
                DefaultReportDialog.checkListStatusButtons(DefaultReportDialog.this.groupList,
                        DefaultReportDialog.this.allUpGroupButton, DefaultReportDialog.this.upGroupButton,
                        DefaultReportDialog.this.downGroupButton, DefaultReportDialog.this.allDownGroupButton);
                DefaultReportDialog.this.updateReport();
            }
        });

        this.orderButton.setMargin(new Insets(0, 0, 0, 0));
        this.orderButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Window w = SwingUtilities.getWindowAncestor((Component) e.getSource());

                if (w instanceof Frame) {
                    DefaultReportDialog.this.order = new OrderWindow((Frame) w, "Order",
                            DefaultReportDialog.this.bundle);
                } else if (w instanceof Dialog) {
                    DefaultReportDialog.this.order = new OrderWindow((Dialog) w, "Order",
                            DefaultReportDialog.this.bundle);
                }
                if (DefaultReportDialog.this.order != null) {
                    Vector v = DefaultReportDialog.this.getSelectedPrintingColumns();
                    Vector vGroupList = DefaultReportDialog.this.getSimpleSelectedGroupColumns();
                    java.util.List li = null;
                    li = DefaultReportDialog.this.order.showOrderWindow(v, vGroupList,
                            DefaultReportDialog.this.orderCols);

                    if (li != null) {
                        DefaultReportDialog.this.orderCols = li;
                        DefaultReportDialog.this.updateReport();
                    }
                }
            }
        });

        this.sqlQueryText = new JTextArea();
        this.sqlQueryText.setEditable(false);
        this.sqlQueryPanel = new JScrollPane(this.sqlQueryText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        if (this.isTable) {
            this.loadButton.setVisible(true);
            this.saveButton.setVisible(true);
            this.sqlQueryPanel.setVisible(false);
            this.bSQL.setVisible(false);
            this.bStore.setVisible(false);
            this.getUpdateCheck().setVisible(true);
            this.bRefreshReport.setVisible(false);
        } else {
            this.loadButton.setVisible(false);
            this.saveButton.setVisible(false);
            this.sqlQueryPanel.setVisible(true);
            this.bSQL.setVisible(true);
            this.bSQL.setEnabled(false);
            this.bStore.setVisible(true);
            this.bStore.setEnabled(false);
            this.getUpdateCheck().setVisible(false);
            this.bRefreshReport.setVisible(true);
            this.bRefreshReport.setEnabled(false);
        }

        this.sqlQueryPanel.setBorder(new TitledBorder(DefaultReportDialog.SQL_QUERY_KEY));
        this.templatesPanel = new JPanel(new GridBagLayout());
        this.templatesPanel.setBorder(new TitledBorder(DefaultReportDialog.TEMPLATE_KEY));
        this.templatesPanel.add(this.templateCombo, new GridBagConstraints(0, 0, 1, 1, 3, 1, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        this.templateCombo.addActionListener(this.updateReportListener);
        this.templateCombo.setRenderer(new I18nListCellRenderer(this.bundle));
        try {
            this.loadDefaultTemplates();
        } catch (Exception ex) {
            DefaultReportDialog.logger.error(null, ex);
            JOptionPane.showMessageDialog(this.container,
                    ReportUtils.getTranslation("M_ERROR_LOADING_TEMPLATE_LIST", this.bundle, null),
                    ReportUtils.getTranslation("Error", this.bundle, null), JOptionPane.ERROR_MESSAGE);
        }

        this.entitiesPanel.setBorder(new TitledBorder(ApplicationManager.getTranslation("Entidades", this.bundle)));
        this.entitiesCombo.setRenderer(new I18nListCellRenderer(this.bundle));
        this.entitiesPanel.add(this.entitiesCombo);

        if (this.isTable) {
            this.entitiesPanel.setVisible(false);
        } else {
            this.entitiesPanel.setVisible(true);
        }

        this.bStore.setToolTipText(ApplicationManager.getTranslation("ReportDesigner.Almacen", this.bundle));

        this.bStore.setMargin(new Insets(0, 0, 0, 0));
        this.bStore.setToolTipText(DefaultReportDialog.REPORT_STORE_KEY);
        this.bSQL.setMargin(new Insets(0, 0, 0, 0));
        this.bSQL.setToolTipText(DefaultReportDialog.SQL_QUERY_KEY);
        this.optionsButton.setMargin(new Insets(0, 0, 0, 0));
        this.configurationPanel.add(this.sqlQueryPanel,
                new GridBagConstraints(0, 1, GridBagConstraints.REMAINDER, 1, 1, 0, GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        this.configurationPanel.add(this.templatesPanel,
                new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.entitiesCombo
            .setToolTipText(ApplicationManager.getTranslation(DefaultReportDialog.ENTITIES_COMBO_KEY, this.bundle));
        this.configurationPanel.add(this.entitiesPanel,
                new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.configurationPanel.add(this.printingColumnsPanel,
                new GridBagConstraints(0, 4, 2, 1, 0, 0.4, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.configurationPanel.add(this.groupListPanel,
                new GridBagConstraints(0, 5, 2, 1, 0, 0.4, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        this.configurationPanel.add(this.functionListPanel,
                new GridBagConstraints(0, 6, 2, 1, 0, 0.2, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        JPanel panelControl = new JPanel(new GridBagLayout());

        panelControl.add(this.loadButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

        panelControl.add(this.saveButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));

        panelControl.add(this.optionsButton, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
        panelControl.add(this.bSQL, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));
        panelControl.add(this.bStore, new GridBagConstraints(5, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));
        panelControl.add(this.bRefreshReport,
                new GridBagConstraints(6, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));
        panelControl.add(this.getUpdateCheck(),
                new GridBagConstraints(6, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 0,
                        GridBagConstraints.EAST,
                        GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));

        JPanel aux = new JPanel(new GridBagLayout());
        aux.add(panelControl,
                new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 1, 1,
                        GridBagConstraints.CENTER,
                        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        this.configurationPanel.add(aux,
                new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1, 0, GridBagConstraints.WEST,
                        GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));

        JScrollPane scrollConf = new JScrollPane(this.configurationPanel) {

            @Override
            public Dimension getMinimumSize() {
                return super.getPreferredSize();
            }
        };
        scrollConf.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(6);
        splitPane.setDividerLocation(0.25);
        this.chartPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));

        splitPane.add(scrollConf, JSplitPane.LEFT);
        // left controsl in a scroll pane
        splitPane.add(this.chartPanel, JSplitPane.RIGHT);
        this.getContentPane().add(splitPane);

        this.loadButton.setMargin(new Insets(1, 1, 1, 1));
        this.loadButton.setIcon(ImageManager.getIcon(ImageManager.PAGE));

        this.loadButton.setMargin(new Insets(0, 0, 0, 0));
        this.loadButton.getButton().setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));

        this.saveButton.setIcon(ImageManager.getIcon(ImageManager.SAVE_FILE));
        this.saveButton.setMargin(new Insets(0, 0, 0, 0));

        this.bRefreshReport
            .setText(ApplicationManager.getTranslation(DefaultReportDialog.RELOAD_REPORT_KEY, this.bundle));

        this.bRefreshReport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object oEntity = DefaultReportDialog.this.entitiesCombo.getSelectedItem();
                if (oEntity.equals("")) {
                    return;
                }

                if ((oEntity != null) && (oEntity instanceof String)) {
                    Vector v = DefaultReportDialog.this.getSelectedColumns();
                    Hashtable hKeysValues = new Hashtable();
                    if ((DefaultReportDialog.this.query == null)
                            || (DefaultReportDialog.this.query.getExpression() == null)) {
                        int o = JOptionPane.showConfirmDialog((Component) e.getSource(),
                                ApplicationManager.getTranslation(DefaultReportDialog.CONFIRM_MESSAGE_KEY,
                                        DefaultReportDialog.this.bundle));
                        if (o != JOptionPane.OK_OPTION) {
                            return;
                        }
                        try {
                            Entity entity = DefaultReportDialog.this.locator.getEntityReference((String) oEntity);
                            EntityResult res = entity.query(hKeysValues, v,
                                    DefaultReportDialog.this.locator.getSessionId());
                            if (res.isEmpty()) {
                                JOptionPane.showMessageDialog((Component) e.getSource(),
                                        ApplicationManager.getTranslation("NO_DATA_QUERY",
                                                DefaultReportDialog.this.bundle),
                                        "Error", Form.ERROR_MESSAGE);
                            }
                            for (int i = 0; i < v.size(); i++) {
                                String sName = (String) v.get(i);
                                if (!res.containsKey(sName)) {
                                    res.put(sName, new Vector());
                                }
                            }
                            DefaultReportDialog.this.model = EntityResultUtils.createTableModel(res);

                        } catch (Exception ex) {
                            DefaultReportDialog.logger.error(null, ex);
                        }
                    } else {
                        Expression expP = DefaultReportDialog.this.query.getExpression();
                        if (com.ontimize.db.query.QueryBuilder.hasExpressionParameters(expP)) {
                            expP = ParameterValuesDialog.showParameterValuesTable(
                                    DefaultReportDialog.this.bRefreshReport,
                                    ApplicationManager.getApplication().getResourceBundle(),
                                    DefaultReportDialog.this.query.getExpression(),
                                    DefaultReportDialog.this.query.getEntity());
                        }
                        Expression expression = com.ontimize.db.ContainsExtendedSQLConditionValuesProcessor
                            .queryToStandard(expP, DefaultReportDialog.this.query.getEntity(),
                                    DefaultReportDialog.this.query.getCols(), DefaultReportDialog.this.locator);

                        hKeysValues.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, expression);
                        try {
                            Entity entity = DefaultReportDialog.this.locator.getEntityReference((String) oEntity);
                            EntityResult res = entity.query(hKeysValues, v,
                                    DefaultReportDialog.this.locator.getSessionId());
                            if (res.isEmpty()) {
                                JOptionPane.showMessageDialog((Component) e.getSource(),
                                        ApplicationManager.getTranslation("NO_DATA_QUERY",
                                                DefaultReportDialog.this.bundle),
                                        "Error", Form.ERROR_MESSAGE);
                            }
                            for (int i = 0; i < v.size(); i++) {
                                String sName = (String) v.get(i);
                                if (!res.containsKey(sName)) {
                                    res.put(sName, new Vector());
                                }
                            }
                            DefaultReportDialog.this.model = EntityResultUtils.createTableModel(res);
                        } catch (Exception ex) {
                            DefaultReportDialog.logger.error(null, ex);
                        }
                    }
                    DefaultReportDialog.this.reportEngine.updateReport(true);
                }
            }
        });

        this.bSQL.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = DefaultReportDialog.this.entitiesCombo.getSelectedItem();
                if ((o != null) && (o instanceof String)) {
                    com.ontimize.db.query.QueryExpression auxQuery = com.ontimize.db.query.QueryBuilder
                        .showQueryBuilder((Component) e.getSource(), (String) o,
                                DefaultReportDialog.this.bundle, DefaultReportDialog.this.locator,
                                DefaultReportDialog.this.query, true, false, true);
                    if (auxQuery != null) {
                        // If the expression has changed and we have to change
                        // the model
                        DefaultReportDialog.this.query = auxQuery;
                        Hashtable hKeysValues = new Hashtable();
                        Vector vColumns = new Vector();
                        if ((DefaultReportDialog.this.query != null)
                                && (DefaultReportDialog.this.query.getExpression() != null)) {
                            Expression expression = com.ontimize.db.ContainsExtendedSQLConditionValuesProcessor
                                .queryToStandard(DefaultReportDialog.this.query.getExpression(),
                                        DefaultReportDialog.this.query.getEntity(),
                                        DefaultReportDialog.this.query.getCols(), DefaultReportDialog.this.locator);
                            vColumns.addAll(DefaultReportDialog.this.query.getCols());
                            hKeysValues.put(ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY, expression);
                            String sText = ContainsSQLConditionValuesProcessorHelper
                                .renderQueryConditionsExpressBundle(DefaultReportDialog.this.query.getExpression(),
                                        DefaultReportDialog.this.bundle);
                            FontMetrics metrics = DefaultReportDialog.this.sqlQueryText
                                .getFontMetrics(DefaultReportDialog.this.sqlQueryText.getFont());
                            int iTextWidth = SwingUtilities.computeStringWidth(metrics, sText);
                            int iComponentWidth = DefaultReportDialog.this.sqlQueryText.getWidth();
                            if (iTextWidth > iComponentWidth) {
                                StringTokenizer token = new StringTokenizer(sText, " ");
                                StringBuilder sbOutput = new StringBuilder();
                                StringBuilder temp = new StringBuilder();
                                while (token.hasMoreTokens()) {
                                    String t = token.nextToken();
                                    if (SwingUtilities.computeStringWidth(metrics,
                                            temp.toString() + " " + t) < iComponentWidth) {
                                        temp.append(" ");
                                        temp.append(t);
                                    } else {
                                        sbOutput.append("\n");
                                        sbOutput.append(temp.toString());
                                        temp.delete(0, temp.length());
                                        temp.append(t);
                                    }
                                }
                                sbOutput.append("\n");
                                sbOutput.append(temp.toString());
                                DefaultReportDialog.this.sqlQueryText.setText(sbOutput.toString());
                            } else {
                                DefaultReportDialog.this.sqlQueryText.setText(sText);
                            }
                        } else {
                            if (DefaultReportDialog.this.query.getExpression() == null) {
                                DefaultReportDialog.this.sqlQueryText.setText("");
                            }
                        }
                    }
                }
            }
        });

        this.bStore.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object o = DefaultReportDialog.this.entitiesCombo.getSelectedItem();
                DefaultReportDialog.this.orderCols = null;
                if (o == null) {
                    return;
                }
                String entity = (String) o;
                ReportStoreDefinition rsd = DefaultReportDialog.this.reportEngine.generaReportStoreDefinition("");
                if (rsd != null) {
                    BasicReportSelection.showSelection((Component) e.getSource(), DefaultReportDialog.this.locator,
                            DefaultReportDialog.this.bundle, DefaultReportDialog.this.rs,
                            entity, rsd);
                }
            }
        });

        this.loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultReportDialog.this.showReportConfigurationDialog();
            }
        });

        this.loadButton.addActionMenuListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultReportDialog.this.createReportConfMenu();
                DefaultReportDialog.this.confMenu.show(DefaultReportDialog.this.loadButton, 0,
                        DefaultReportDialog.this.loadButton.getHeight());
            }
        });

        this.getUpdateCheck().setFont(this.getUpdateCheck().getFont().deriveFont(Font.BOLD));
        this.getUpdateCheck().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (DefaultReportDialog.this.getUpdateCheck().isSelected()) {
                    DefaultReportDialog.this.updateReport();
                }
            }
        });

        this.saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Object s = MessageDialog.showInputMessage(
                            SwingUtilities.getWindowAncestor((Component) e.getSource()),
                            "table.enter_configuration_name",
                            DefaultReportDialog.this.bundle, DefaultReportDialog.this.reportName);
                    if (s != null) {
                        String str = s.toString();
                        str = str.replace(':', '_');
                        int o = DefaultReportDialog.this.saveReportConfiguration(str);
                        if (o != JOptionPane.OK_OPTION) {
                            return;
                        }
                        DefaultReportDialog.this.reportName = str;
                        Window window = SwingUtilities.getWindowAncestor((Component) e.getSource());
                        if (window instanceof Frame) {
                            Frame f = (Frame) window;
                            MessageDialog.showMessage(f, "M_SAVE_OK", JOptionPane.INFORMATION_MESSAGE,
                                    DefaultReportDialog.this.bundle);
                        } else if (window instanceof Dialog) {
                            Dialog d = (Dialog) window;
                            MessageDialog.showMessage(d, "M_SAVE_OK", JOptionPane.INFORMATION_MESSAGE,
                                    DefaultReportDialog.this.bundle);
                        }
                    }
                } catch (Exception ex) {
                    DefaultReportDialog.logger.trace(null, ex);
                    MessageDialog.showErrorMessage(SwingUtilities.getWindowAncestor((Component) e.getSource()),
                            ex.getMessage());
                }
            }
        });
        // Report engines that implement this interface it is possible to make
        // date groupings, selecting on the right mouse button above date
        // columns.
        if (this.reportEngine instanceof IGroupByDate) {
            this.groupByDateGroupButton.add(this.groupByDateTime);
            this.groupByDateGroupButton.add(this.groupByDate);
            this.groupByDateGroupButton.add(this.groupByMonth);
            this.groupByDateGroupButton.add(this.groupByMonthAndYear);
            this.groupByDateGroupButton.add(this.groupByQuarter);
            this.groupByDateGroupButton.add(this.groupByQuarterAndYear);
            this.groupByDateGroupButton.add(this.groupByYear);
            this.groupByDatePopup.add(this.groupByDateTime);
            this.groupByDatePopup.addSeparator();
            this.groupByDatePopup.add(this.groupByDate);
            this.groupByDatePopup.add(this.groupByMonth);
            this.groupByDatePopup.add(this.groupByMonthAndYear);
            this.groupByDatePopup.add(this.groupByQuarter);
            this.groupByDatePopup.add(this.groupByQuarterAndYear);
            this.groupByDatePopup.add(this.groupByYear);

            this.groupByDateTime.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
            this.groupByDate.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });

            this.groupByMonth.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
            this.groupByMonthAndYear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
            this.groupByQuarter.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
            this.groupByQuarterAndYear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
            this.groupByYear.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    DefaultReportDialog.this.updateReport();
                }

            });
        }

        if (this.isTable) {
            this.updateReport();
        }
        this.setResourceBundle(this.bundle);
    }

    public void setDefaultCloseOperation(int operation) {
        if (this.container instanceof JFrame) {
            ((JFrame) this.container).setDefaultCloseOperation(operation);
        }
    }

    protected void groupColumns() {
        int index = this.groupList.getSelectedIndex();
        this.multigroups = this.groupList.getSelectedValues();
        if (this.multigroups.length > 0) {
            Vector vItemList = new Vector();
            for (int i = 0; i < this.multigroups.length; i++) {
                Object actual = this.multigroups[i];
                if (actual instanceof com.ontimize.report.item.SelectableItem) {
                    vItemList.add(actual);
                } else {
                    vItemList.addAll(((SelectableMultipleItem) actual).getItemList());
                }
                DefaultListModel model = (DefaultListModel) this.groupList.getModel();
                model.removeElement(actual);
            }
            SelectableMultipleItem multiple = new SelectableMultipleItem(vItemList, this.bundle);
            DefaultListModel model = (DefaultListModel) this.groupList.getModel();
            model.add(index, multiple);
        }
        this.updateReport();
    }

    protected void clearReport() {
        if (this.reportEngine.getBaseTemplate() != null) {
            this.reportEngine.dispose();
        }
    }

    public RolloverButton getStoreButton() {
        return this.bStore;
    }

    protected void showReportConfigurationDialog() {
        Window w = SwingUtilities.getWindowAncestor(this.container);
        if (w instanceof Frame) {
            this.reportConfigurationDialog = new ReportSetupDialog((Frame) w);
        } else {
            this.reportConfigurationDialog = new ReportSetupDialog((Dialog) w);
        }

        Map<String, Object> confMap = new HashMap<String, Object>();
        List<String> confList = this.getConfigurations();
        for (String name : confList) {
            confMap.put(name, null);
        }

        if (this.shareRemoteReferenceReports != false) {
            try {
                int sessionID = this.locator.getSessionId();
                IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                    .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                            sessionID);
                List<SharedElement> sourceShared = remoteReference.getSourceSharedItemsList(this.user,
                        this.preferenceKey, sessionID);
                List<SharedElement> targetShared = remoteReference.getSharedItemsWithUserAndKey(this.user,
                        this.preferenceKey, sessionID);

                int i = 1;
                for (SharedElement shared : sourceShared) {
                    StringBuilder builderName = new StringBuilder();
                    builderName.append(shared.getName());
                    if (confMap.containsKey(builderName.toString())) {
                        builderName.append(" (");
                        builderName.append(i);
                        builderName.append(")");
                    }
                    confMap.put(builderName.toString(), shared);
                    confList.add(builderName.toString());
                }

                i = 1;
                for (SharedElement shared : targetShared) {
                    StringBuilder builderName = new StringBuilder();
                    builderName.append(shared.getName());
                    builderName.append(" - ");
                    builderName.append(shared.getUserSource());
                    if (confMap.containsKey(builderName.toString())) {
                        builderName.append(" (");
                        builderName.append(i);
                        builderName.append(" )");
                    }
                    confMap.put(builderName.toString(), shared);
                    confList.add(builderName.toString());
                }

            } catch (Exception e) {
                DefaultReportDialog.logger.error("Error retrieving shared report", e);
            }
        }

        String conf = this.reportConfigurationDialog.showSetupDialog(confList, this.bundle);
        if (conf != null) {
            Object loadId = confMap.get(conf);
            if (loadId != null) {
                this.loadSharedConfiguration((SharedElement) loadId);
            } else {
                this.loadConfiguration(conf);
            }
        }
    }

    public JCheckBoxMenuItem getUnderlineLine4CheckMenu() {
        return this.underlineLine4CheckMenu;
    }

    public JCheckBoxMenuItem getFitHead5CheckMenu() {
        return this.fitHead5CheckMenu;
    }

    public JCheckBoxMenuItem getDynamicHead6CheckMenu() {
        return this.dynamicHead6CheckMenu;
    }

    public JCheckBoxMenuItem getHideGroupDetail7CheckMenu() {
        return this.hideGroupDetail7CheckMenu;
    }

    public JCheckBoxMenuItem getGrid1CheckMenu() {
        return this.grid1CheckMenu;
    }

    public JCheckBoxMenuItem getRowNumber2CheckMenu() {
        return this.rowNumber2CheckMenu;
    }

    public JCheckBoxMenuItem getIncludeColumnName3CheckMenu() {
        return this.includeColumnName3CheckMenu;
    }

    public JCheckBoxMenuItem getGroupStartInNewPageCheckMenu() {
        return this.groupStartInNewPageCheckMenu;
    }

    public JCheckBoxMenuItem getFirstGroupStartInNewPageCheckMenu() {
        return this.firstGroupStartInNewPageCheckMenu;
    }


    public void dispose() {
        this.container.dispose();
    }

    public void pack() {
        this.container.pack();
    }

    public void setJMenuBar(JMenuBar jMenuBar) {
        if (this.container instanceof JFrame) {
            ((JFrame) this.container).setJMenuBar(jMenuBar);
        }
    }

    private Hashtable getColumnWidth() {
        return this.reportEngine.getColumnWidth();
    }

    public ButtonSelection getLoadButton() {
        return this.loadButton;
    }

    public JButton getSaveButton() {
        return this.saveButton;
    }

    public boolean loadConfiguration(String conf) {
        if ((this.preferenceKey != null) && (this.prefs != null)) {
            String p = this.prefs.getPreference(this.user, this.preferenceKey);
            if (p != null) {
                StringTokenizer st = new StringTokenizer(p, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int index = token.indexOf(":");
                    if (index > 0) {
                        String sName = token.substring(0, index);
                        if (sName.equalsIgnoreCase(conf)) {
                            String datosConf = token.substring(index + 1);
                            // Configure
                            this.reportName = conf;
                            this.configureReport(datosConf);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void loadSharedConfiguration(SharedElement sharedReport) {
        this.reportName = sharedReport.getName();
        this.configureReport(sharedReport.getContentShare());
    }

    public static Object getConfigurationValue(String preferenceKey, String user, String preferenceName) {
        String p = ApplicationManager.getApplication().getPreferences().getPreference(user, preferenceKey);
        if (p != null) {
            StringTokenizer st = new StringTokenizer(p, ";");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int index = token.indexOf(":");
                if (index > 0) {
                    String sName = token.substring(0, index);
                    if (sName.equalsIgnoreCase(preferenceName)) {
                        return token.substring(index + 1);
                    }
                }
            }
        }
        return null;
    }

    protected void configureNewReport(String confData) {
        boolean bGrid = false;
        boolean bRowNumber = false;
        boolean bColumnName = false;
        boolean bRemarkLine = false;
        boolean bAdjustHeader = false;
        boolean bHideDetail = false;
        boolean bGroupNewPage = false;
        boolean bFirstGroupNewPage = false;
        String sTitle = null;

        DefaultReportDialog.logger.debug("New preferences : ", confData);

        boolean sel = this.getUpdateCheck().isSelected();

        if (sel) {
            this.getUpdateCheck().setSelected(false);
        } else {
            this.getUpdateCheck().setSelected(true);
        }

        StringTokenizer token = new StringTokenizer(confData, "|");
        while (token.hasMoreElements()) {
            String sCurrentToken = token.nextToken();

            if (sCurrentToken.indexOf("~plantilla~") != -1) {
                int pos = sCurrentToken.indexOf("~plantilla~") + "~plantilla~".length();
                String templateName = sCurrentToken.substring(pos);
                this.templateCombo.setSelectedItem(templateName);
            }

            if (sCurrentToken.indexOf("~rejilla~") != -1) {
                bGrid = true;
            }

            if (sCurrentToken.indexOf("~numerofila~") != -1) {
                bRowNumber = true;
            }

            if (sCurrentToken.indexOf("~nombrecolumna~") != -1) {
                bColumnName = true;
            }

            if (sCurrentToken.indexOf("~remarcarlinea~") != -1) {
                bRemarkLine = true;
            }

            if (sCurrentToken.indexOf("~ajustarcabecera~") != -1) {
                bAdjustHeader = true;
            }

            if (sCurrentToken.indexOf("~ocultardetalle~") != -1) {
                bHideDetail = true;
            }

            if (sCurrentToken.indexOf("~groupnewpage~") != -1) {
                bGroupNewPage = true;
            }

            if (sCurrentToken.indexOf("~firstgroupnewpage~") != -1) {
                bFirstGroupNewPage = true;
            }

            if (sCurrentToken.indexOf("~columnaimprimir~") != -1) {
                int pos = sCurrentToken.indexOf("~columnaimprimir~") + "~columnaimprimir~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setPrintingColumnPreferences(analyze);
            }

            if (sCurrentToken.indexOf("~multilinea~") != -1) {
                int pos = sCurrentToken.indexOf("~multilinea~") + "~multilinea~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setDynamicColumnPreferences(analyze);
            }

            if (sCurrentToken.indexOf("~columnaagrupar~") != -1) {
                int pos = sCurrentToken.indexOf("~columnaagrupar~") + "~columnaagrupar~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setGroupColumnPreference(analyze);
            }

            if (sCurrentToken.indexOf("~funcion~") != -1) {
                int pos = sCurrentToken.indexOf("~funcion~") + "~funcion~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setFunctionColumnPreferences(analyze);
            }

            if (sCurrentToken.indexOf("~ordenar~") != -1) {
                int pos = sCurrentToken.indexOf("~ordenar~") + "~ordenar~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setOrderColumnPreferences(analyze);
            }

            if (sCurrentToken.indexOf("~titulo~") != -1) {
                int pos = sCurrentToken.indexOf("~titulo~") + "~titulo~".length();
                sTitle = sCurrentToken.substring(pos);
            }

            if (sCurrentToken.indexOf("~configurecolumns~") != -1) {
                int pos = sCurrentToken.indexOf("~configurecolumns~") + "~configurecolumns~".length();
                String analyze = sCurrentToken.substring(pos);
                this.setConfigureColumnsPreferences(analyze);

            }
        }

        this.grid1CheckMenu.setSelected(bGrid);
        this.rowNumber2CheckMenu.setSelected(bRowNumber);
        this.includeColumnName3CheckMenu.setSelected(bColumnName);
        this.underlineLine4CheckMenu.setSelected(bRemarkLine);
        this.fitHead5CheckMenu.setSelected(bAdjustHeader);
        this.hideGroupDetail7CheckMenu.setSelected(bHideDetail);
        this.groupStartInNewPageCheckMenu.setSelected(bGroupNewPage);
        this.firstGroupStartInNewPageCheckMenu.setSelected(bFirstGroupNewPage);


        if (sel) {
            this.getUpdateCheck().setSelected(true);
        }
        this.reportEngine.updateReport(true);
        if (sTitle != null) {
            this.reportEngine.setTitleReport(sTitle);
        } else {
            this.reportEngine.setTitleReport("");
        }
        this.reportEngine.updateReport(true);
    }

    private void configureReport(String confData) {
        if (confData.indexOf("~plantilla~") != -1) {
            this.configureNewReport(confData);
            return;
        }

        int index = -1;
        while ((index = confData.indexOf("||")) != -1) {
            String t = confData.substring(0, index + 1);
            String t2 = confData.substring(index + 1);
            confData = t + " " + t2;
        }

        StringTokenizer st = new StringTokenizer(confData, "|");
        Vector v = new Vector();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s != null) {
                v.add(s);
            } else {
                v.add(" ");
            }
        }
        if (v.size() < 7) {
            DefaultReportDialog.logger.warn("{} -> Incorrect configuration : {}", this.getClass().toString(), confData);
            return;
        }
        boolean sel = this.getUpdateCheck().isSelected();

        if (sel) {
            this.getUpdateCheck().setSelected(false);
        } else {
            this.getUpdateCheck().setSelected(true);
        }

        String sTemplate = (String) v.get(0);
        this.templateCombo.setSelectedItem(sTemplate);
        if ("true".equals(v.get(1))) {
            this.grid1CheckMenu.setSelected(true);
        } else {
            this.grid1CheckMenu.setSelected(false);
        }
        if ("true".equals(v.get(2))) {
            this.rowNumber2CheckMenu.setSelected(true);
        } else {
            this.rowNumber2CheckMenu.setSelected(false);
        }
        if ("true".equals(v.get(3))) {
            this.includeColumnName3CheckMenu.setSelected(true);
        } else {
            this.includeColumnName3CheckMenu.setSelected(false);
        }
        if ("true".equals(v.get(4))) {
            this.underlineLine4CheckMenu.setSelected(true);
        } else {
            this.underlineLine4CheckMenu.setSelected(false);
        }

        String sColsToPrint = (String) v.get(5);
        String sColsGroup = (String) v.get(6);
        String sColsFunctions = null;
        String sColsSort = null;
        if (v.size() > 7) {
            sColsFunctions = (String) v.get(7);
        }
        if (v.size() > 8) {
            sColsSort = (String) v.get(8);
        }

        StringTokenizer st2 = new StringTokenizer(sColsToPrint, ":");
        Vector colsImpr = new Vector();
        while (st2.hasMoreTokens()) {
            String col = st2.nextToken();
            if (!" ".equalsIgnoreCase(col)) {
                colsImpr.add(col);
            }
        }
        this.setSelectedPrintingColumns(colsImpr);

        StringTokenizer st3 = new StringTokenizer(sColsGroup, ":");
        Vector colsAgrup = new Vector();
        while (st3.hasMoreTokens()) {
            String col = st3.nextToken();
            if (!" ".equalsIgnoreCase(col)) {
                if (col.indexOf("#") != -1) {
                    String c = col.substring(0, col.indexOf("#"));
                    String t = col.substring(col.indexOf("#") + 1);
                    this.getSelectedDateGroupingColumns().put(c, t);
                }
                colsAgrup.add(col);
            }
        }
        this.setSelectedColumnsToGroup(colsAgrup);

        Hashtable colsFunc = new Hashtable();
        if (sColsFunctions != null) {
            StringTokenizer st4 = new StringTokenizer(sColsFunctions, ":");
            while (st4.hasMoreTokens()) {
                String col = st4.nextToken();
                if (!" ".equalsIgnoreCase(col)) {
                    String c = col.substring(0, col.indexOf("#"));
                    String t = col.substring(col.indexOf("#") + 1);
                    colsFunc.put(c, t);
                }
            }
        }
        this.setSelectedFunctionColumns(colsFunc);

        OrderColumns colsOrde = new OrderColumns();
        if (sColsSort != null) {
            StringTokenizer st5 = new StringTokenizer(sColsSort, ":");
            while (st5.hasMoreTokens()) {
                String col = st5.nextToken();
                if (!" ".equalsIgnoreCase(col)) {
                    String c = col.substring(0, col.indexOf("#"));
                    String t = col.substring(col.indexOf("#") + 1);
                    colsOrde.add(c, t);
                }
            }
        }
        this.setOrderColumns(colsOrde);

        if (sel) {
            this.getUpdateCheck().setSelected(true);
        }
        this.reportEngine.updateReport(true);

    }

    public void setAction(int keyCode, int modifiers, Action action, String key) {
        if (this.container instanceof JFrame) {

            final int keyC = keyCode;
            final int modif = modifiers;
            final Action actio = action;
            final String k = key;
            this.container.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    ((Frame) e.getSource()).requestFocus();
                }

                /**
                 * mouseExited
                 * @param e MouseEvent
                 */
                @Override
                public void mouseExited(MouseEvent e) {
                }

                /**
                 * mousePressed
                 * @param e MouseEvent
                 */
                @Override
                public void mousePressed(MouseEvent e) {
                }

                /**
                 * mouseReleased
                 * @param e MouseEvent
                 */
                @Override
                public void mouseReleased(MouseEvent e) {
                }

            });
            this.container.addKeyListener(new KeyListener() {

                /**
                 * keyPressed
                 * @param e KeyEvent
                 */
                @Override
                public void keyPressed(KeyEvent e) {
                    if ((e.getKeyCode() == keyC) && (e.getModifiers() == modif)) {
                        actio.actionPerformed(new ActionEvent(e.getSource(), 0, k));
                    }
                }

                /**
                 * keyReleased
                 * @param e KeyEvent
                 */
                @Override
                public void keyReleased(KeyEvent e) {
                }

                /**
                 * keyTyped
                 * @param e KeyEvent
                 */
                @Override
                public void keyTyped(KeyEvent e) {
                }
            });
        }
    }

    public java.util.List getResources(String template) {
        ArrayList list = new ArrayList();
        URL templateURL = this.getClass().getClassLoader().getResource(template);

        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = df.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(templateURL.openStream());
            if (!doc.hasChildNodes()) {
                return list;
            }

            NodeList l = doc.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) {
                Node section = l.item(i);
                NodeList componentList = section.getChildNodes();
                for (int j = 0; j < componentList.getLength(); j++) {
                    Node comp = componentList.item(j);
                    NodeList listC = comp.getChildNodes();
                    for (int k = 0; k < listC.getLength(); k++) {
                        Node ele = listC.item(k);
                        if ("imageref".equalsIgnoreCase(ele.getNodeName())) {
                            Hashtable h = DefaultReportDialog.getParameters(ele);
                            String src = (String) h.get("src");
                            if (src != null) {
                                int index = template.lastIndexOf("/");
                                URL url = this.getClass()
                                    .getClassLoader()
                                    .getResource(template.substring(0, index + 1) + src);
                                try {
                                    FileResource rr = new FileResource(src, url.openStream());
                                    list.add(rr);
                                } catch (Exception ex) {
                                    DefaultReportDialog.logger.error("Error reading: {}", url.getPath(), ex);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex1) {
            DefaultReportDialog.logger.error(null, ex1);
        }
        return list;
    }

    public Map<String, Integer> getColumnFixedWidth() {
        return this.columnFixedWidth;
    }

    public Map<String, Integer> getColumnAlignment() {
        return this.columnAlignment;
    }

    public List<String> getConfiguredColumns() {
        return this.configuredColumns;
    }

    protected void loadDefaultTemplates() throws java.io.IOException {
        // Load the template combo
        this.templates = this.reportEngine.getDefaultTemplates();
        this.templateCombo.removeAllItems();
        for (int i = 0; i < this.templates.size(); i++) {
            URL url = this.getClass().getClassLoader().getResource((String) this.templates.get(i));
            if (url == null) {
                DefaultReportDialog.logger.warn("Template not found : '{}'", this.templates.get(i));
            } else {
                this.templateCombo.addItem(this.templates.get(i));
            }
        }
    }

    protected int saveReportConfiguration(String conf) {
        StringBuilder sbConfiguration = new StringBuilder();
        boolean bFound = false;
        if ((this.preferenceKey != null) && (this.prefs != null)) {
            String p = this.prefs.getPreference(this.user, this.preferenceKey);
            if (p != null) {
                StringTokenizer st = new StringTokenizer(p, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int index = token.indexOf(":");
                    if (index > 0) {
                        String sName = token.substring(0, index);
                        if (sName.equalsIgnoreCase(conf)) {
                            String sNew = this.getCurrentConfiguration();
                            int o = JOptionPane.showConfirmDialog(ApplicationManager.getApplication().getFrame(),
                                    ApplicationManager.getTranslation("M_SOBREESCRIBIR_INFORME?", this.bundle), "",
                                    JOptionPane.YES_NO_OPTION);
                            bFound = true;
                            if (o != JOptionPane.OK_OPTION) {
                                return o;
                            }
                            sbConfiguration.append(sName + ":" + sNew);
                        } else {
                            sbConfiguration.append(token);
                        }
                        sbConfiguration.append(";");
                    }
                }
            }
            if (!bFound) {
                sbConfiguration.append(";" + conf + ":" + this.getCurrentConfiguration());
            }
            this.prefs.setPreference(this.user, this.preferenceKey, sbConfiguration.toString());
            this.prefs.savePreferences();
            return JOptionPane.OK_OPTION;
        }
        return JOptionPane.NO_OPTION;
    }

    public void saveReportConfiguration(SharedElement elementShared) {
        StringBuilder sbConfiguration = new StringBuilder();
        boolean bFound = false;
        if ((this.preferenceKey != null) && (this.prefs != null)) {
            String p = this.prefs.getPreference(this.user, this.preferenceKey);
            if (p != null) {
                StringTokenizer st = new StringTokenizer(p, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int index = token.indexOf(":");
                    if (index > 0) {
                        String sName = token.substring(0, index);
                        if (sName.equalsIgnoreCase(elementShared.getName())) {
                            String sNew = elementShared.getContentShare();
                            bFound = true;
                            sbConfiguration.append(sName + ":" + sNew);
                        } else {
                            sbConfiguration.append(token);
                        }
                        sbConfiguration.append(";");
                    }
                }
            }
            if (!bFound) {
                sbConfiguration.append(";" + elementShared.getName() + ":" + elementShared.getContentShare());
            }
            this.prefs.setPreference(this.user, this.preferenceKey, sbConfiguration.toString());
            this.prefs.savePreferences();
        }
    }

    public String getCurrentConfiguration() {
        // Put the keys separated by ~ before each preference

        // Template
        // Options
        // Order
        // Print
        // Group
        // Functions

        StringBuilder sb = new StringBuilder();
        String p = this.getSourceTemplateName();
        this.add(sb, "~plantilla~" + p);
        // Options
        if (this.grid1CheckMenu.isSelected()) {
            this.add(sb, "~rejilla~");
        }
        if (this.rowNumber2CheckMenu.isSelected()) {
            this.add(sb, "~numerofila~");
        }
        if (this.includeColumnName3CheckMenu.isSelected()) {
            this.add(sb, "~nombrecolumna~");
        }
        if (this.underlineLine4CheckMenu.isSelected()) {
            this.add(sb, "~remarcarlinea~");
        }
        if (this.fitHead5CheckMenu.isSelected()) {
            this.add(sb, "~ajustarcabecera~");
        }

        if (this.hideGroupDetail7CheckMenu.isSelected()) {
            this.add(sb, "~ocultardetalle~");
        }

        if (this.groupStartInNewPageCheckMenu.isSelected()) {
            this.add(sb, "~groupnewpage~");
        }

        if (this.firstGroupStartInNewPageCheckMenu.isSelected()) {
            this.add(sb, "~firstgroupnewpage~");
        }

        Vector v = this.getSelectedPrintingColumns();
        StringBuilder colsToPrint = new StringBuilder();
        if (v.size() > 0) {
            colsToPrint.append("~columnaimprimir~");

            for (int i = 0; i < v.size(); i++) {
                colsToPrint.append(v.get(i));
                if (i < (v.size() - 1)) {
                    colsToPrint.append(":");
                }
            }
            this.add(sb, colsToPrint.toString());
        }

        v = this.getSelectedDynamicColumnName();
        StringBuilder sbDynamicCols = new StringBuilder();
        if (v.size() > 0) {
            sbDynamicCols.append("~multilinea~");
            for (int i = 0; i < v.size(); i++) {
                sbDynamicCols.append(v.get(i));
                if (i < (v.size() - 1)) {
                    sbDynamicCols.append(":");
                }
            }
            this.add(sb, sbDynamicCols.toString());
        }

        v = this.getSelectedGroupColumns();
        StringBuilder colsGrp = new StringBuilder();
        if (v.size() > 0) {
            colsGrp.append("~columnaagrupar~");
            for (int i = 0; i < v.size(); i++) {
                colsGrp.append(v.get(i));
                if (java.util.Date.class.isAssignableFrom(this.getColumnClassForColumn(v.get(i).toString()))) {
                    Integer operation = this.getSelectedDateGroupingColumns().get(v.get(i).toString()) != null
                            ? (Integer) this.getSelectedDateGroupingColumns()
                                .get(v.get(i).toString())
                            : new Integer(0);
                    colsGrp.append("#");
                    colsGrp.append(operation.toString());
                }
                if (i < (v.size() - 1)) {
                    colsGrp.append(":");
                }
            }
            this.add(sb, colsGrp.toString());
        }

        Hashtable h = this.getSelectedFunctionColumns();
        StringBuilder sbFunctionCols = new StringBuilder();
        Enumeration enumKeys = h.keys();
        int i = 0;
        while (enumKeys.hasMoreElements()) {
            Object oKey = enumKeys.nextElement();
            Object tipo = h.get(oKey);
            String t = oKey + "#" + tipo;
            sbFunctionCols.append(t);
            if (i < (h.size() - 1)) {
                sbFunctionCols.append(":");
            }
            i++;
        }
        if (sbFunctionCols.toString().length() > 0) {
            sbFunctionCols.insert(0, "~funcion~");
            this.add(sb, sbFunctionCols.toString());
        }

        OrderColumns orderColumn = this.getOrderColumns();
        StringBuilder sbSortColumns = new StringBuilder();
        List names = orderColumn.getColumnNameList();
        for (int j = 0; j < names.size(); j++) {
            String sKey = (String) names.get(j);
            String sType = orderColumn.getOrder(sKey);
            String t = sKey + "#" + sType;
            sbSortColumns.append(t);
            if (i < (orderColumn.size() - 1)) {
                sbSortColumns.append(":");
            }

        }

        if (sbSortColumns.toString().length() > 0) {
            sbSortColumns.insert(0, "~ordenar~");
            this.add(sb, sbSortColumns.toString());
        }

        String sTitle = this.reportEngine.getTitle();
        if (sTitle != null) {
            this.add(sb, "~titulo~" + sTitle);
        }

        List<String> configuredColumns = this.getConfiguredColumns();
        if (configuredColumns.size() > 0) {
            getConfiguredColumnsCurrentConfiguration(sb, configuredColumns);
        }

        return sb.toString();
    }

    protected void getConfiguredColumnsCurrentConfiguration(StringBuilder sb, List<String> configuredColumns) {
        StringBuilder configuration = new StringBuilder();
        Map<String, Integer> columnFixedWidth = this.getColumnFixedWidth();
        Map<String, Integer> columnAlignment = this.getColumnAlignment();
        for (String col : configuredColumns) {
            configuration.append(col).append("#");
            if (columnFixedWidth.containsKey(col)) {
                configuration.append(columnFixedWidth.get(col));
            } else {
                configuration.append(" ");
            }
            configuration.append(":");
            if (columnAlignment.containsKey(col)) {
                configuration.append(columnAlignment.get(col));
            } else {
                configuration.append(" ");
            }
            configuration.append("#");
        }
        this.add(sb, "~configurecolumns~" + configuration.toString());
    }

    public Class getColumnClassForColumn(String columnName) {
        for (int j = 0; j < this.model.getColumnCount(); j++) {
            if (this.model.getColumnName(j).equals(columnName)) {
                return this.model.getColumnClass(j);
            }
        }
        return null;
    }

    protected OrderColumns getOrderColumns() {
        OrderColumns columns = new OrderColumns();

        if (this.orderCols == null) {
            return columns;
        }
        Vector v = this.getSelectedPrintingColumns();
        for (int i = 0; i < this.orderCols.size(); i++) {
            Object o = this.orderCols.get(i);
            if (o instanceof SelectableItemOrder) {
                SelectableItemOrder item = (SelectableItemOrder) o;
                if (v.contains(item.getText())) {
                    columns.add(item.getText(), item.isAscending() ? "ASC" : "DES");
                }
            }
        }
        return columns;
    }

    public static class CustomMenuButtonBorder extends AbstractBorder {

    }

    protected void createReportConfMenu() {
        /*
         * BORRAR L�NEA INFERIOR
         */
        // this.confMenu = null;
        /*
         * BORRAR L�NEA SUPERIOR
         */
        if (this.confMenu == null) {
            this.confMenu = new JPopupMenu();
            this.listener = new ItemListener(this);
            this.deleteListener = new DeleteItemListener(this.bundle, this);
            if (this.shareRemoteReferenceReports) {
                this.shareListener = new SharedItemListener(this.preferenceKey, this); /*
                                                                                        * Share report with other users
                                                                                        */
                this.shareEditTargetListener = new SharedEditTargetItemListener(this.preferenceKey, this); /*
                                                                                                            * Edit list
                                                                                                            * of user
                                                                                                            * target
                                                                                                            */
                this.shareLoadListener = new SharedLoadItemListener(this.preferenceKey, this); /* Load shared report */
                this.sharedMessageItemListener = new SharedMessageItemListener(this.preferenceKey, this); /*
                                                                                                           * Obtain
                                                                                                           * message
                                                                                                           * form shared
                                                                                                           * report
                                                                                                           */
                this.shareEditListener = new SharedEditItemListener(this.preferenceKey, this); /* Edit shared report */
                this.shareDeleteListener = new SharedDeleteItemListener(this.preferenceKey, this); /*
                                                                                                    * Delete shared
                                                                                                    * report
                                                                                                    */
                this.shareDeleteTargetListener = new SharedDeleteTargetItemListener(this.preferenceKey, this); /*
                                                                                                                * Delete
                                                                                                                * target
                                                                                                                * shared
                                                                                                                * report
                                                                                                                */

            }
        }
        java.util.List list = this.getConfigurations();
        int originalSize = list.size();

        for (int i = this.confMenu.getComponentCount() - 1; i >= 0; i--) {
            Object o = this.confMenu.getComponent(i);
            if (o instanceof JPanel) {
                JPanel actual = (JPanel) o;
                JButton item = (JButton) actual.getComponent(0);
                String sKey = item.getActionCommand();
                if (!list.contains(sKey)) {
                    this.confMenu.remove(i);
                } else {
                    list.remove(sKey);
                }
            } else {
                this.confMenu.remove(i);
            }
        }

        if (originalSize != 0) {
            for (int i = 0; i < list.size(); i++) {
                if (!this.shareRemoteReferenceReports) {
                    JPanel panel = this.createNonSharedPanelReport(list, i);
                    this.confMenu.add(panel);
                } else {
                    JPanel panel = this.createSharedPanelReport(list, i);
                    this.confMenu.add(panel);
                }
            }
        } else {
            JLabel label = new JLabel(
                    ApplicationManager.getTranslation("table.no_stored_report_templates", this.bundle));
            this.confMenu.add(label);
        }

        if (this.shareRemoteReferenceReports) {
            try {
                String username = ((ClientReferenceLocator) this.locator).getUser();
                int sessionID = this.locator.getSessionId();
                IShareRemoteReference remoteReference = (IShareRemoteReference) ((UtilReferenceLocator) this.locator)
                    .getRemoteReference(IShareRemoteReference.REMOTE_NAME,
                            sessionID);
                List<HashMap<String, Object>> result = remoteReference.getSourceSharedElementMenuList(username,
                        this.preferenceKey, sessionID);

                if (!result.isEmpty()) {
                    this.confMenu.addSeparator();
                }

                for (Map<String, Object> actualPreference : result) {
                    this.createSharedReportPanelEntry(actualPreference);
                }

                List<HashMap<String, Object>> resultTarget = remoteReference.getTargetSharedElementMenuList(username,
                        this.preferenceKey, sessionID);

                if (!resultTarget.isEmpty()) {
                    this.confMenu.addSeparator();
                }

                for (Map<String, Object> actualPreference : resultTarget) {
                    this.createSharedTargetReportPanelEntry(actualPreference);
                }

            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
            }
        }
    }

    public ListDataField createAndConfigureTargetUser() throws Exception {
        Hashtable h = new Hashtable();
        h.put(DataField.ATTR, IShareRemoteReference.SHARE_USER_TARGET_STRING);
        h.put(DataField.TEXT_STR, ApplicationManager.getTranslation(IShareRemoteReference.SHARE_USER_TARGET_STRING));
        h.put(DataField.LABELPOSITION, "top");
        h.put(DataField.DIM, "text");
        h.put(DataField.EXPAND, "yes");
        h.put("rows", "5");
        ListDataField listDataField = new ListDataField(h);
        return listDataField;
    }

    public static class CustomMenuButton extends MenuButton {

        public CustomMenuButton(Icon icon) {
            super(icon);
            this.setBorder(null);
            // TODO Auto-generated constructor stub
        }

        public CustomMenuButton(String string) {
            super(string);
            this.setBorder(null);
        }

    }

    protected JPanel createNonSharedPanelReport(List elementList, int i) {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton item = new CustomMenuButton((String) elementList.get(i));
        item.addActionListener(this.listener);
        item.setOpaque(false);
        panel.add(item, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        item.setMargin(new Insets(0, 0, 0, 0));
        MenuButton delete = new CustomMenuButton(ImageManager.getIcon(ImageManager.RECYCLER));
        delete.setActionCommand((String) elementList.get(i));
        delete.addActionListener(this.deleteListener);
        delete.setMargin(new Insets(0, 0, 0, 0));
        delete.setOpaque(false);
        panel.add(delete, new GridBagConstraints(1, 0, GridBagConstraints.REMAINDER, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.setOpaque(false);
        return panel;
    }

    protected JPanel createSharedPanelReport(List elementList, int i) {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton item = new CustomMenuButton((String) elementList.get(i));
        item.addActionListener(this.listener);
        panel.add(item, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setOpaque(false);
        MenuButton share = new CustomMenuButton(ImageManager.getIcon(ImageManager.DATA_SHARE_ACTION));
        share.setActionCommand((String) elementList.get(i));
        share.addActionListener(this.shareListener);
        share.setMargin(new Insets(0, 0, 0, 0));
        share.setOpaque(false);
        panel.add(share, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        MenuButton delete = new CustomMenuButton(ImageManager.getIcon(ImageManager.RECYCLER));
        delete.setActionCommand((String) elementList.get(i));
        delete.addActionListener(this.deleteListener);
        delete.setMargin(new Insets(0, 0, 0, 0));
        delete.setOpaque(false);
        panel.add(delete, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        panel.setOpaque(false);
        return panel;
    }

    protected void createSharedReportPanelEntry(Map<String, Object> actualPreference) {
        int shareKeyId = (Integer) actualPreference.get(IShareRemoteReference.SHARE_KEY_STRING);
        String filterName = (String) actualPreference.get(IShareRemoteReference.SHARE_NAME_STRING);

        /* Edit user target */
        JPanel panel = new JPanel(new GridBagLayout());
        MenuButton editUserTarget = new CustomMenuButton(ImageManager.getIcon(ImageManager.USERS_EDIT));
        editUserTarget.setActionCommand(Integer.toString(shareKeyId));
        editUserTarget.addActionListener(this.shareEditTargetListener);
        editUserTarget.setMargin(new Insets(0, 0, 0, 0));
        editUserTarget.setOpaque(false);
        panel.add(editUserTarget, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /* Load share report */

        MenuButton item = new CustomMenuButton(filterName);
        item.setActionCommand(Integer.toString(shareKeyId));
        item.addActionListener(this.shareLoadListener);
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setOpaque(false);
        panel.add(item, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 5, 0));

        /* Shared message report */
        MenuButton messageReport = new CustomMenuButton(ImageManager.getIcon(ImageManager.INFO_16));
        messageReport.setActionCommand(Integer.toString(shareKeyId));
        messageReport.addActionListener(this.sharedMessageItemListener);
        messageReport.setMargin(new Insets(0, 0, 0, 0));
        messageReport.setOpaque(false);
        panel.add(messageReport, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /* Edit report */
        MenuButton editReport = new CustomMenuButton(ImageManager.getIcon(ImageManager.EDIT));
        editReport.setActionCommand(Integer.toString(shareKeyId));
        editReport.addActionListener(this.shareEditListener);
        editReport.setMargin(new Insets(0, 0, 0, 0));
        editReport.setOpaque(false);
        panel.add(editReport, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));

        /* Delete shared report */
        MenuButton deleteShared = new CustomMenuButton(ImageManager.getIcon(ImageManager.DATA_SHARED_DELETE));
        deleteShared.setActionCommand(Integer.toString(shareKeyId));
        deleteShared.addActionListener(this.shareDeleteListener);
        deleteShared.setMargin(new Insets(0, 0, 0, 0));
        deleteShared.setOpaque(false);
        panel.add(deleteShared, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.REMAINDER, new Insets(0, 0, 0, 0), 0, 0));

        panel.setOpaque(false);
        this.confMenu.add(panel);
    }

    protected void createSharedTargetReportPanelEntry(Map<String, Object> actualPreference) {
        int shareKeyId = (Integer) actualPreference.get(IShareRemoteReference.SHARE_KEY_STRING);
        int shareTargetKeyId = (Integer) actualPreference.get(IShareRemoteReference.SHARE_TARGET_KEY_STRING);
        String filterName = (String) actualPreference.get(IShareRemoteReference.SHARE_NAME_STRING);

        /* Load share report */
        JPanel panel = new JPanel(new GridBagLayout());
        MenuButton item = new CustomMenuButton(filterName);
        item.setActionCommand(Integer.toString(shareKeyId));
        item.addActionListener(this.shareLoadListener);
        item.setMargin(new Insets(0, 0, 0, 0));
        item.setOpaque(false);
        panel.add(item, new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 5, 0));

        /* Shared message report */
        MenuButton messageReport = new CustomMenuButton(ImageManager.getIcon(ImageManager.INFO_16));
        messageReport.setActionCommand(Integer.toString(shareKeyId));
        messageReport.addActionListener(this.sharedMessageItemListener);
        messageReport.setMargin(new Insets(0, 0, 0, 0));
        messageReport.setOpaque(false);
        panel.add(messageReport, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        /* Delete shared report */
        MenuButton deleteShared = new CustomMenuButton(ImageManager.getIcon(ImageManager.DATA_SHARED_DELETE));
        deleteShared.setActionCommand(Integer.toString(shareTargetKeyId));
        deleteShared.addActionListener(this.shareDeleteTargetListener);
        deleteShared.setMargin(new Insets(0, 0, 0, 0));
        deleteShared.setOpaque(false);
        panel.add(deleteShared, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        panel.setOpaque(false);

        this.confMenu.add(panel);
    }

    //
    // protected JPanel createSharedWithUserReportPanelEntry() {
    // return null;
    // }

    public void updateReport(boolean force) {
        this.reportEngine.updateReport(force);
    }

    public void updateReport() {
        this.reportEngine.updateReport(false);
    }

    public static Hashtable getParameters(Node node) {
        NamedNodeMap nnm = node.getAttributes();
        if (nnm == null) {
            return null;
        } else {
            Hashtable h = new Hashtable();
            for (int i = 0; i < nnm.getLength(); i++) {
                Node n = nnm.item(i);
                h.put(n.getNodeName(), n.getNodeValue());
            }
            return h;
        }
    }

    public static void checkListStatusButtons(JList list, JButton upAll, JButton up, JButton down, JButton downAll) {
        try {
            if ((list.getSelectedIndices().length != 1) || (((DefaultListModel) list.getModel()).size() == 1)) {
                upAll.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
                downAll.setEnabled(false);
                return;
            }
            int index = list.getSelectedIndex();

            if ((index > 0) && (index < (((DefaultListModel) list.getModel()).size() - 1))) {
                upAll.setEnabled(true);
                up.setEnabled(true);
                downAll.setEnabled(true);
                down.setEnabled(true);
            } else if (index == 0) {
                upAll.setEnabled(false);
                up.setEnabled(false);
                downAll.setEnabled(true);
                down.setEnabled(true);
            } else if (index == (((DefaultListModel) list.getModel()).size() - 1)) {
                upAll.setEnabled(true);
                up.setEnabled(true);
                downAll.setEnabled(false);
                down.setEnabled(false);
            } else {
                upAll.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
                downAll.setEnabled(false);
            }
        } catch (Exception ex) {
            DefaultReportDialog.logger.error(null, ex);
        }
    }

    protected String getXMLTemplate() throws IOException {
        return this.reportEngine.getXMLTemplate();
    }

    protected Vector getSelectedDynamicColumnName() {
        Vector v = new Vector();
        Vector cols = this.getSelectedPrintingColumns();
        for (int i = 0; i < this.printingColumnList.getModel().getSize(); i++) {
            SelectableDynamicItem item = (SelectableDynamicItem) this.printingColumnList.getModel().getElementAt(i);
            int iIndex = cols.indexOf(item.getText());
            if (iIndex >= 0) {
                if (item.isDynamic()) {
                    v.add(item.getText());
                }
            }
        }
        return v;

    }

    public Vector getPrintingColumns() {
        Vector v = new Vector();
        for (int i = 0; i < this.printingColumnList.getModel().getSize(); i++) {
            com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.printingColumnList
                .getModel()
                .getElementAt(i);
            v.add(item.getText());
        }
        return v;
    }

    public Vector getSelectedPrintingColumns() {
        Vector v = new Vector();
        for (int i = 0; i < this.printingColumnList.getModel().getSize(); i++) {
            com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.printingColumnList
                .getModel()
                .getElementAt(i);
            if (item.isSelected()) {
                v.add(item.getText());
            }
        }
        return v;
    }

    /**
     * This method combines printing columns with grouped columns returning a <code>Vector</code> with
     * non repeated columns.
     * @return the resultant <code>Vector</code>
     */
    public Vector getSelectedPrintingAndGroupedColumns() {
        Vector vSelectedPrintingColumns = this.getSelectedPrintingColumns();
        for (int i = 0; i < this.getSelectedGroupColumns().size(); i++) {
            if (!vSelectedPrintingColumns.contains(this.getSelectedGroupColumns().get(i))) {
                vSelectedPrintingColumns.add(this.getSelectedGroupColumns().get(i));
            }
        }
        return vSelectedPrintingColumns;
    }

    public Container getContentPane() {
        if (this.container instanceof JFrame) {
            return ((JFrame) this.container).getContentPane();
        }
        return null;
    }

    public Vector getSelectedDynamicColumns() {
        Vector v = new Vector();
        Vector cols = this.getSelectedPrintingColumns();
        v.setSize(cols.size());
        for (int i = 0; i < this.printingColumnList.getModel().getSize(); i++) {
            SelectableDynamicItem item = (SelectableDynamicItem) this.printingColumnList.getModel().getElementAt(i);

            int iIndex = cols.indexOf(item.getText());
            if (iIndex >= 0) {
                if (item.isDynamic()) {
                    v.setElementAt(Boolean.TRUE, iIndex);
                } else {
                    v.setElementAt(Boolean.FALSE, iIndex);
                }
            }
        }
        return v;
    }

    public Vector getSelectedGroupItems() {
        Vector v = new Vector();
        for (int i = 0; i < this.groupList.getModel().getSize(); i++) {
            Object o = this.groupList.getModel().getElementAt(i);
            if (o instanceof com.ontimize.report.item.SelectableItem) {
                com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.groupList
                    .getModel()
                    .getElementAt(i);
                if (item.isSelected()) {
                    v.add(item);
                }
            } else if (o instanceof SelectableMultipleItem) {
                v.add(o);
            }
        }

        return v;
    }

    public Vector getSelectedGroupColumns() {
        Vector v = new Vector();
        for (int i = 0; i < this.groupList.getModel().getSize(); i++) {
            Object o = this.groupList.getModel().getElementAt(i);
            if (o instanceof com.ontimize.report.item.SelectableItem) {
                com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.groupList
                    .getModel()
                    .getElementAt(i);
                if (item.isSelected()) {
                    v.add(item.getText());
                }
            } else if (o instanceof SelectableMultipleItem) {
                Vector vItems = ((SelectableMultipleItem) o).getItemList();
                for (int j = 0; j < vItems.size(); j++) {
                    com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) vItems
                        .get(j);
                    if (item.isSelected()) {
                        v.add(item.getText());
                    }
                }
            }
        }
        return v;
    }

    /**
     * Gets the selected group columns that are also marked in selected printing column list.
     * @return list with columns for grouping.
     */
    public Vector getPrintedGroupsFromSelectedGroups() {
        Vector vSelectedGroupColumns = this.getSelectedGroupColumns();
        Vector vSelectedPrintingColumns = this.getSelectedPrintingColumns();
        Vector vPrintedGroups = new Vector();
        for (int i = 0; i < vSelectedGroupColumns.size(); i++) {
            if (vSelectedPrintingColumns.contains(vSelectedGroupColumns.get(i))) {
                vPrintedGroups.add(vSelectedGroupColumns.get(i));
            }
        }
        return vPrintedGroups;
    }

    protected Vector getSelectedColumns() {
        Vector columns = new Vector();
        Vector v = this.getSelectedPrintingColumns();
        if (v != null) {
            columns.addAll(v);
        }
        v = this.getSimpleSelectedGroupColumns();
        if (v != null) {
            columns.addAll(v);
        }
        return columns;
    }

    public Vector getSimpleSelectedGroupColumns() {

        Vector v = new Vector();
        for (int i = 0; i < this.groupList.getModel().getSize(); i++) {
            Object o = this.groupList.getModel().getElementAt(i);
            if (o instanceof com.ontimize.report.item.SelectableItem) {
                com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.groupList
                    .getModel()
                    .getElementAt(i);
                if (item.isSelected()) {
                    v.add(item.getText());
                }
            } else if (o instanceof SelectableMultipleItem) {
                Vector temp = ((SelectableMultipleItem) o).getItemList();
                for (int j = 0; j < temp.size(); j++) {
                    com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) temp
                        .get(j);
                    v.add(item.getText());
                }
            }
        }
        return v;
    }

    public Hashtable getSelectedFunctionColumns() {
        Hashtable res = new Hashtable();
        for (int i = 0; i < this.functionList.getModel().getSize(); i++) {
            SelectableFunctionItem item = (SelectableFunctionItem) this.functionList.getModel().getElementAt(i);
            if (item.isSelected()) {
                res.put(item.getText(), new Integer(item.getOperation()));
            }
        }
        return res;
    }

    public void setSelectedDateGroupingOperations(String itemText, int operation) {
        for (int i = 0; i < this.groupList.getModel().getSize(); i++) {
            if (this.groupList.getModel().getElementAt(i) instanceof SelectableDateGroupItem) {
                if (((SelectableDateGroupItem) this.groupList.getModel().getElementAt(i)).getText().equals(itemText)) {
                    ((SelectableDateGroupItem) this.groupList.getModel().getElementAt(i)).setOperation(operation);
                }
            }
        }
    }

    public Hashtable getSelectedDateGroupingColumns() {
        Hashtable res = new Hashtable();
        for (int i = 0; i < this.groupList.getModel().getSize(); i++) {
            if (this.groupList.getModel().getElementAt(i) instanceof SelectableDateGroupItem) {
                SelectableDateGroupItem item = (SelectableDateGroupItem) this.groupList.getModel().getElementAt(i);
                if (item.isSelected()) {
                    res.put(item.getText(), new Integer(item.getOperation()));
                }
            } else {
                if (this.groupList.getModel().getElementAt(i) instanceof SelectableMultipleItem) {
                    SelectableMultipleItem multipleitem = (SelectableMultipleItem) this.groupList.getModel()
                        .getElementAt(i);
                    for (int j = 0; j < multipleitem.getItemList().size(); j++) {
                        if (multipleitem.getItemList().get(j) instanceof SelectableDateGroupItem) {
                            SelectableDateGroupItem item = (SelectableDateGroupItem) multipleitem.getItemList().get(j);
                            if (item.isSelected()) {
                                res.put(item.getText(), new Integer(item.getOperation()));
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    protected Vector getTextToTranslateColumnsAndSelected() {
        Vector v = new Vector();
        for (int i = 0; i < this.printingColumnList.getModel().getSize(); i++) {
            com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) this.printingColumnList
                .getModel()
                .getElementAt(i);
            if (item.isSelected()) {
                v.add(item.toString());
            }
        }
        return v;
    }

    public TableModel getDataModel(boolean ascending) {
        Vector colsGrupos = this.getSelectedGroupColumns();
        if (colsGrupos != null) {
            if (!colsGrupos.isEmpty()) {
                TableSorter sorter = new TableSorter(this.model);
                for (int i = 0; i < colsGrupos.size(); i++) {
                    Object oColumn = colsGrupos.get(i);
                    StringTokenizer token = new StringTokenizer(oColumn.toString(), ",");
                    while (token.hasMoreTokens()) {
                        String c = token.nextToken();
                        int ind = this.getColumnIndex(c, this.model);
                        sorter.sortByColumn(ind, ascending);
                    }
                }
                return sorter;
            }
        }
        return this.model;
    }

    public void setDataModel(TableModel model) {
        this.model = model;
    }

    /**
     * This method is used to order a table model. The table model is ordered when exists groups in
     * reports or when user presses ascending-descending order in report dialog (a-z pop-up menu).
     * @param ascending condition to ascending-descending order.
     * @return the <code>com.ontimize.report.TableSorter</code> with order
     */
    public TableSorter getOrderedDataModel(boolean ascending) {
        Vector colsGrupos = this.getSelectedGroupColumns();
        TableSorter sorter = new TableSorter(this.getModel());
        if (colsGrupos != null) {
            if (!colsGrupos.isEmpty()) {
                for (int i = 0; i < colsGrupos.size(); i++) {
                    Object oColumn = colsGrupos.get(i);
                    StringTokenizer token = new StringTokenizer(oColumn.toString(), ",");
                    while (token.hasMoreTokens()) {
                        String c = token.nextToken();
                        int ind = this.getColumnIndex(c, this.model);
                        sorter.sortByColumn(ind, ascending);
                    }
                }
            }
        }
        if ((this.orderCols != null) && !this.orderCols.isEmpty()) {
            for (int j = 0; j < this.orderCols.size(); j++) {
                Object o = this.orderCols.get(j);
                if (o instanceof String) {
                    int index = this.getColumnIndex((String) this.orderCols.get(j), sorter);
                    sorter.sortByColumn(index, true);
                } else if (o instanceof SelectableItemOrder) {
                    SelectableItemOrder item = (SelectableItemOrder) o;
                    int iIndex = this.getColumnIndex(item.getText(), sorter);
                    sorter.sortByColumn(iIndex, item.getOrder());
                }
            }
        }
        return sorter;
    }

    /**
     * Returns the index of column in data model according to the column name.
     * @param columnName the name of column
     * @param table the table model
     * @return index of column
     */
    public int getColumnIndex(String columnName, TableModel table) {
        if (table == null) {
            return -1;
        }
        int iColumnCount = table.getColumnCount();
        for (int i = 0; i < iColumnCount; i++) {
            String sName = table.getColumnName(i);
            if (sName.equals(columnName)) {
                return i;
            }
        }
        return -1; // Column not found
    }

    public JPopupMenu getConfMenu() {
        return this.confMenu;
    }

    public JRadioButtonMenuItem getSumOpMenu() {
        return this.sumOpMenu;
    }

    public JRadioButtonMenuItem getAverageOpMenu() {
        return this.averageOpMenu;
    }

    public JRadioButtonMenuItem getMaximumOpMenu() {
        return this.maximumOpMenu;
    }

    public JRadioButtonMenuItem getMinimumOpMenu() {
        return this.minimumOpMenu;
    }

    public String getSourceTemplateName() {
        if (this.templateCombo.getSelectedItem() == null) {
            return null;
        }
        return "" + this.templateCombo.getSelectedItem();
    }

    public void updateFunctionList() {
        Vector vColumns = this.getSelectedPrintingColumns();
        // we configure the list for functions
        Vector vYAxisColumns = new Vector();
        for (int i = 0; i < vColumns.size(); i++) {
            int index = this.getColumnIndex(vColumns.elementAt(i).toString(), this.model);
            Class columnClass = this.model.getColumnClass(index);
            if (columnClass.getSuperclass() == Number.class) {
                vYAxisColumns.add(new SelectableFunctionItem(vColumns.get(i).toString(), this.bundle));
            }
        }
        this.setFunctionColumns(vYAxisColumns);
    }

    public void setFunctionColumns(Vector v) {
        DefaultListModel m = new DefaultListModel();
        for (int i = 0; i < v.size(); i++) {
            m.add(i, v.get(i));
        }
        this.functionList.setModel(m);
    }

    public void updateColumnClasses() {
        Vector vColumns = new Vector();

        for (int i = 0; i < this.model.getColumnCount(); i++) {
            vColumns.add(this.model.getColumnName(i));
        }

        // Configures the list
        Vector items = new Vector();
        for (int i = 0; i < vColumns.size(); i++) {
            items.add(new SelectableDynamicItem(vColumns.get(i).toString(), this.bundle));
        }
        this.setPrintingColumns(items);
    }

    protected void setPrintingColumns(Vector v) {
        DefaultListModel m = new DefaultListModel();
        this.updateReportListener.setProcessEvents(false);
        try {
            for (int i = 0; i < v.size(); i++) {
                if (v.get(i) instanceof com.ontimize.report.item.SelectableItem) {
                    m.add(i, v.get(i));
                } else {
                    SelectableDynamicItem item = new SelectableDynamicItem((String) v.get(i), this.bundle);
                    m.add(i, item);
                }
            }
        } catch (Exception ex) {
            DefaultReportDialog.logger.error(null, ex);
        } finally {
            this.updateReportListener.setProcessEvents(true);
        }
        this.printingColumnList.setModel(m);
    }

    public JComboBox getEntitiesCombo() {
        return this.entitiesCombo;
    }

    protected Vector getColumns() {
        Vector vColumns = new Vector();
        if (this.isTable) {
            for (int i = 0; i < this.model.getColumnCount(); i++) {
                String sColName = this.model.getColumnName(i);
                vColumns.add(sColName);
            }
        } else {
            // It is not a table. We must columns of entity selected in combo.
        }
        return vColumns;
    }

    public JScrollPane getScroll() {
        return this.scroll;
    }

    protected Vector createPrintingColumn(Vector columns) {
        // Configures the list
        Vector items = new Vector();
        for (int i = 0; i < columns.size(); i++) {
            items.add(new SelectableDynamicItem(columns.get(i).toString(), this.bundle));
        }

        if (this.table != null) {
            if (this.table.isSortReportColumn()) {
                Collections.sort(items, new com.ontimize.report.item.SelectableItem(" ", this.bundle));
            }
        } else {
            Collections.sort(items, new com.ontimize.report.item.SelectableItem(" ", this.bundle));
        }
        return items;
    }

    protected Vector createGroupColumn(Vector columns) {
        // Configures the list for grouping
        Vector items2 = new Vector();
        for (int i = 0; i < columns.size(); i++) {
            int index = this.getColumnIndex(columns.elementAt(i).toString(), this.model);
            Class columnClass = this.model.getColumnClass(index);
            if (columnClass.getSuperclass() == java.util.Date.class) {
                items2
                    .add(new com.ontimize.report.item.SelectableDateGroupItem(columns.get(i).toString(), this.bundle));
            } else {
                items2.add(new com.ontimize.report.item.SelectableItem(columns.get(i).toString(), this.bundle));
            }
        }

        if (this.table != null) {
            if (this.table.isSortReportColumn()) {
                Collections.sort(items2, new com.ontimize.report.item.SelectableItem(" ", this.bundle));
            }
        } else {
            Collections.sort(items2, new com.ontimize.report.item.SelectableItem(" ", this.bundle));
        }
        return items2;
    }

    protected Vector createFunctionColumns(Vector columns, Vector types) {
        Vector yAxisColumns = new Vector();
        for (int i = 0; i < columns.size(); i++) {
            Object t = types.get(i);
            if ((t == null) || (((String) t).length() == 0)) {
                continue;
            }
            if ("Integer".equalsIgnoreCase((String) t) || "Double".equalsIgnoreCase((String) t)) {
                yAxisColumns.add(new SelectableFunctionItem(columns.get(i).toString(), this.bundle));
            }
        }
        Collections.sort(yAxisColumns, new SelectableFunctionItem(" ", this.bundle));
        if (columns.size() > 0) {
            yAxisColumns.add(new PredefinedFunctionItem("ReportDesigner.NumeroOcurrencias", this.bundle));
        }
        return yAxisColumns;
    }

    protected void add(StringBuilder sb, String s) {
        if (s != null) {
            sb.append(s);
            sb.append("|");
        }
    }

    public JList getFunctionList() {
        return this.functionList;
    }

    public JPanel getFunctionListPanel() {
        return this.functionListPanel;
    }

    protected void setOrderColumns(OrderColumns h) {
        if (this.orderCols != null) {
            this.orderCols.clear();
        } else {
            this.orderCols = new Vector();
        }

        List enu = h.getColumnNameList();
        for (int j = 0; j < enu.size(); j++) {
            String o = (String) enu.get(j);
            String sValue = h.getOrder(o);
            SelectableItemOrder orden = new SelectableItemOrder(o);
            if (sValue.equalsIgnoreCase("ASC")) {
                orden.setOrder(true);
            } else {
                orden.setOrder(false);
            }
            this.orderCols.add(orden);
        }
    }

    protected void initGroup(DefaultListModel l) {
        for (int i = l.getSize() - 1; i >= 0; i--) {
            Object o = l.elementAt(i);
            if (o instanceof SelectableMultipleItem) {
                l.remove(i);
                SelectableMultipleItem itemM = (SelectableMultipleItem) o;
                Vector v = itemM.getItemList();
                for (int j = 0; j < v.size(); j++) {
                    l.addElement(v.get(j));
                }
            }
        }
    }

    protected void setSelectedColumnsToGroup(Vector v) {
        DefaultListModel l = (DefaultListModel) this.groupList.getModel();

        this.initGroup(l);

        Vector vIndex = (Vector) v.clone();

        Object[] array = new Object[vIndex.size()];

        for (int i = l.getSize() - 1; i >= 0; i--) {
            com.ontimize.report.item.SelectableItem item;
            if (l.getElementAt(i) instanceof com.ontimize.report.item.SelectableDateGroupItem) {
                item = (com.ontimize.report.item.SelectableDateGroupItem) l.getElementAt(i);
            } else {
                item = (com.ontimize.report.item.SelectableItem) l.getElementAt(i);
            }

            if (v.remove(item.getText())) {
                int j = vIndex.indexOf(item.getText());
                item.setSelected(true);
                array[j] = item;
                l.remove(i);
            } else {
                item.setSelected(false);
            }
        }
        if (v.size() > 0) {
            for (int i = 0; i < v.size(); i++) {
                int index = -1;
                String sOriginalText = (String) v.get(i);
                StringTokenizer token = new StringTokenizer(sOriginalText, ",");
                Vector vItemsList = new Vector();
                while (token.hasMoreTokens()) {
                    String col = token.nextToken();
                    com.ontimize.report.item.SelectableItem item = null;
                    for (int j = 0; j < l.getSize(); j++) {
                        item = (com.ontimize.report.item.SelectableItem) l.getElementAt(j);
                        if (col.equalsIgnoreCase(item.getText())) {
                            l.removeElement(item);
                            if (index == -1) {
                                index = j;
                            }
                            break;
                        }
                    }
                    vItemsList.add(item);
                }
                SelectableMultipleItem multiple = new SelectableMultipleItem(vItemsList, this.bundle);
                int j = vIndex.indexOf(sOriginalText);
                array[j] = multiple;
            }
        }

        for (int i = 0; i < array.length; i++) {
            l.add(i, array[i]);
        }

        this.groupList.repaint();
    }

    protected void setSelectedFunctionColumns(Hashtable h) {
        for (int i = 0; i < this.functionList.getModel().getSize(); i++) {
            SelectableFunctionItem item = (SelectableFunctionItem) this.functionList.getModel().getElementAt(i);
            if (h.containsKey(item.getText())) {
                Object op = h.get(item.getText());
                item.setSelected(true);
                item.setOperation(Integer.parseInt((String) op));
            } else {
                item.setSelected(false);
            }
        }
        this.functionList.repaint();
    }

    protected void setSelectedPrintingColumns(Vector v) {
        DefaultListModel l = (DefaultListModel) this.printingColumnList.getModel();

        Object[] array = new Object[v.size()];

        for (int i = l.getSize() - 1; i >= 0; i--) {
            com.ontimize.report.item.SelectableItem item = (com.ontimize.report.item.SelectableItem) l.getElementAt(i);
            if (v.contains(item.getText())) {
                item.setSelected(true);
                int j = v.indexOf(item.getText());
                array[j] = item;
                l.remove(i);
            } else {
                item.setSelected(false);
            }
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                l.add(i, array[i]);
            }
        }

        this.printingColumnList.repaint();
    }

    public Window getContainer() {
        return this.container;
    }

    protected void setDynamicColumnsToSet(Vector v) {
        DefaultListModel l = (DefaultListModel) this.printingColumnList.getModel();
        for (int i = l.getSize() - 1; i >= 0; i--) {
            SelectableDynamicItem item = (SelectableDynamicItem) l.getElementAt(i);
            if (v.contains(item.getText())) {
                item.setDynamic(true);
            } else {
                item.setDynamic(false);
            }
        }
    }

    /**
     * addComponentListener
     * @param componentListener ComponentListener
     */
    public void addComponentListener(ComponentListener componentListener) {
        this.container.addComponentListener(componentListener);
    }

    /**
     * removeComponentListener
     * @param componentListener ComponentListener
     */
    public void removeComponentListener(ComponentListener componentListener) {
        this.container.removeComponentListener(componentListener);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ResourceBundle getBundle() {
        return this.bundle;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    protected void setPrintingColumnPreferences(String stringToParse) {
        StringTokenizer st2 = new StringTokenizer(stringToParse, ":");
        Vector colsToPrint = new Vector();
        while (st2.hasMoreTokens()) {
            String col = st2.nextToken();
            if (!" ".equalsIgnoreCase(col)) {
                colsToPrint.add(col);
            }
        }
        this.setSelectedPrintingColumns(colsToPrint);
    }

    public void setFunctionColumnPreferences(String analyze) {
        Hashtable colsFunc = new Hashtable();
        if (analyze != null) {
            StringTokenizer st4 = new StringTokenizer(analyze, ":");
            while (st4.hasMoreTokens()) {
                String col = st4.nextToken();
                if (!" ".equalsIgnoreCase(col)) {
                    String c = col.substring(0, col.indexOf("#"));
                    String t = col.substring(col.indexOf("#") + 1);
                    colsFunc.put(c, t);
                }
            }
        }
        this.setSelectedFunctionColumns(colsFunc);
    }

    protected void setDynamicColumnPreferences(String analizar) {
        StringTokenizer st2 = new StringTokenizer(analizar, ":");
        Vector vColsToPrint = new Vector();
        while (st2.hasMoreTokens()) {
            String col = st2.nextToken();
            if (!" ".equalsIgnoreCase(col)) {
                vColsToPrint.add(col);
            }
        }
        this.setDynamicColumnsToSet(vColsToPrint);
    }

    protected void setGroupColumnPreference(String analizar) {
        StringTokenizer st3 = new StringTokenizer(analizar, ":");
        Vector colsGroup = new Vector();
        while (st3.hasMoreTokens()) {
            String col = st3.nextToken();
            if (!" ".equalsIgnoreCase(col)) {
                if (col.indexOf("#") != -1) {
                    this.setSelectedDateGroupingOperations(col.substring(0, col.indexOf("#")),
                            Integer.parseInt(col.substring(col.indexOf("#") + 1)));
                    colsGroup.add(col.substring(0, col.indexOf("#")));
                } else {
                    colsGroup.add(col);
                }

            }
        }
        this.setSelectedColumnsToGroup(colsGroup);
    }

    protected void setOrderColumnPreferences(String analizar) {
        OrderColumns colsOrde = new OrderColumns();
        if (analizar != null) {
            StringTokenizer st5 = new StringTokenizer(analizar, ":");
            while (st5.hasMoreTokens()) {
                String col = st5.nextToken();
                if (!" ".equalsIgnoreCase(col)) {
                    String c = col.substring(0, col.indexOf("#"));
                    String t = col.substring(col.indexOf("#") + 1);
                    colsOrde.add(c, t);
                }
            }
        }
        this.setOrderColumns(colsOrde);
    }

    protected void setConfigureColumnsPreferences(String analyze) {
        List<String> configuredColumns = this.getConfiguredColumns();
        configuredColumns.clear();
        Map<String, Integer> columnFixedWidth = this.getColumnFixedWidth();
        columnFixedWidth.clear();
        Map<String, Integer> columnAlignment = this.getColumnAlignment();
        columnAlignment.clear();
        if (analyze != null) {
            StringTokenizer st5 = new StringTokenizer(analyze, "#");
            while (st5.hasMoreTokens()) {
                String col = st5.nextToken();
                if ((col != null) && (col.length() > 0)) {
                    configuredColumns.add(col);
                    String value = st5.nextToken();
                    StringTokenizer vToken = new StringTokenizer(value, ":");
                    while (vToken.hasMoreTokens()) {
                        String v = vToken.nextToken();
                        if ((v != null) && (v.length() > 0)) {
                            try {
                                if (!" ".equalsIgnoreCase(v)) {
                                    columnFixedWidth.put(col, Integer.valueOf(v));
                                }
                            } catch (Exception ex) {
                                DefaultReportDialog.logger.error("Error retrieving {} column configuration : {}", col,
                                        v, ex);
                            }
                        }
                        v = vToken.nextToken();
                        if ((v != null) && (v.length() > 0)) {
                            try {
                                if (!" ".equalsIgnoreCase(v)) {
                                    columnAlignment.put(col, Integer.valueOf(v));
                                }
                            } catch (Exception ex) {
                                DefaultReportDialog.logger.error("Error retrieving {} column configuration : {}", col,
                                        v, ex);
                            }
                        }

                    }
                }
            }
        }
    }

    public void setTemplateList(List templates) {
        this.templates = templates;
    }

    public List getTemplateList() {
        return this.templates;
    }

    public JComboBox getTemplateCombo() {
        return this.templateCombo;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPreferenceKey(String preferenceKey) {
        this.preferenceKey = preferenceKey;
    }

    public void setPreferences(ApplicationPreferences prefs) {
        this.prefs = prefs;
    }

    public void setModel(TableModel model) {
        this.model = model;
    }

    public TableModel getModel() {
        return this.model;
    }

    protected Vector createFunctionColumns(Vector columns) {
        // Configures the list for functions
        Vector yAxisColumns = new Vector();
        for (int i = 0; i < columns.size(); i++) {
            Class columnClass = this.model.getColumnClass(i);
            if (columnClass.getSuperclass() == Number.class) {
                yAxisColumns.add(new SelectableFunctionItem(columns.get(i).toString(), this.bundle));
            }
        }
        if (columns.size() > 0) {
            yAxisColumns.add(new PredefinedFunctionItem("ReportDesigner.NumeroOcurrencias", this.bundle));
        }
        return yAxisColumns;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    protected void setReportEntity(String entity) {
        Vector columns = new Vector();
        Vector types = new Vector();
        Entity entityObject = null;
        try {
            if (entity.length() > 0) {
                entityObject = this.locator.getEntityReference(entity);
                if (entityObject instanceof AdvancedQueryEntity) {
                    AdvancedQueryEntity eAv = (AdvancedQueryEntity) entityObject;
                    Map m = eAv.getColumnListForAvancedQuery(this.locator.getSessionId());
                    DefaultReportDialog.logger.info("datos: {}", m);
                    Set keySet = m.keySet();
                    Iterator it = keySet.iterator();
                    while (it.hasNext()) {
                        Object c = it.next();
                        Object t = m.get(c);
                        columns.add(c);
                        types.add(t);
                    }
                }
            }

            Vector items = this.createPrintingColumn(columns);
            this.setPrintingColumns(items);
            Vector items2 = this.createGroupColumn(columns);
            this.setGroupColumns(items2);
            Vector vYAxisColumns = this.createFunctionColumns(columns, types);
            this.setFunctionColumns(vYAxisColumns);
            if (entityObject != null) {
                EntityResult res = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.NODATA_RESULT);
                this.model = EntityResultUtils.createTableModel(res);
            }

        } catch (Exception ex) {
            DefaultReportDialog.logger.error(null, ex);
        }
    }

    protected void setGroupColumns(Vector v) {
        DefaultListModel m = new DefaultListModel();
        for (int i = 0; i < v.size(); i++) {
            m.add(i, v.get(i));
        }
        this.groupList.setModel(m);
    }

    protected java.util.List getConfigurations() {
        ArrayList list = new ArrayList();
        if ((this.preferenceKey != null) && (this.prefs != null)) {
            String p = this.prefs.getPreference(this.user, this.preferenceKey);
            if (p != null) {
                StringTokenizer st = new StringTokenizer(p, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int index = token.indexOf(":");
                    if (index > 0) {
                        String sName = token.substring(0, index);
                        list.add(sName);
                    }
                }
            }
        }
        return list;
    }

    public UpdateReportListener getUpdateReportListener() {
        return this.updateReportListener;
    }

    public void deleteConfiguration(String conf) {
        if ((this.preferenceKey != null) && (this.prefs != null)) {
            String p = this.prefs.getPreference(this.user, this.preferenceKey);
            String pout = "";
            if (p != null) {
                StringTokenizer st = new StringTokenizer(p, ";");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int iIndex = token.indexOf(":");
                    if (iIndex > 0) {
                        String sName = token.substring(0, iIndex);
                        if (!sName.equalsIgnoreCase(conf)) {
                            pout += pout.length() == 0 ? token : ";" + token;
                        }
                    }
                }
                this.prefs.setPreference(this.user, this.preferenceKey, pout);
                this.prefs.savePreferences();
            }
        }
    }

    protected void showDeleteReportDialog() {
        Window w = SwingUtilities.getWindowAncestor(this.container);
        if (w instanceof Frame) {
            this.deleteReportDialog = new ReportDeleteDialog((Frame) w);
        } else {
            this.deleteReportDialog = new ReportDeleteDialog((Dialog) w);
        }

        String conf = this.deleteReportDialog.showDeleteDialog(this.getConfigurations(), this.bundle);
        if (conf != null) {
            this.deleteConfiguration(conf);
        }

    }

    public void setContainer(Object container) {
        this.container = (Window) container;
    }

    public void center() {
        ApplicationManager.center(this.container);
    }

    public JButton getUpButton() {
        return this.upButton;
    }

    public JButton getDownButton() {
        return this.downButton;
    }

    public JButton getAllUpButton() {
        return this.allUpButton;
    }

    public JButton getUpGroupButton() {
        return this.upGroupButton;
    }

    public JButton getAllDownButton() {
        return this.allDownButton;
    }

    public JButton getDownGroupButton() {
        return this.downGroupButton;
    }

    public JButton getAllUpGroupButton() {
        return this.allUpGroupButton;
    }

    public JButton getAllDownGroupButton() {
        return this.allDownGroupButton;
    }

    public JPopupMenu getOperationTypePopup() {
        return this.operationTypePopup;
    }

    public boolean isTable() {
        return this.isTable;
    }

    public SelectableDynamicItem getCurrentDynamicItem() {
        return this.currentDynamicItem;
    }

    public void setCurrentDynamicItem(SelectableDynamicItem currentDynamicItem) {
        this.currentDynamicItem = currentDynamicItem;
    }

    public JList getPrintingColumnList() {
        return this.printingColumnList;
    }

    public JList getGroupList() {
        return this.groupList;
    }

    public java.util.List getOrderCols() {
        return this.orderCols;
    }

    public JPopupMenu getGroupPopup() {
        return this.groupPopup;
    }

    public JRadioButtonMenuItem getSimpleLineMenu() {
        return this.simpleLineMenu;
    }

    public JRadioButtonMenuItem getMultilineMenu() {
        return this.multilineMenu;
    }

    public JPopupMenu getMultilinePopup() {
        return this.multilinePopup;
    }

    public com.ontimize.db.query.QueryExpression getQuery() {
        return this.query;
    }

    public JRadioButtonMenuItem getAscendingOpMenu() {
        return this.ascendingOpMenu;
    }

    public JRadioButtonMenuItem getDescendingOpMenu() {
        return this.descendingOpMenu;
    }

    public void setCurrentItem(SelectableFunctionItem currentItem) {
        this.currentItem = currentItem;
    }

    public SelectableFunctionItem getCurrentItem() {
        return this.currentItem;
    }

    public void setCurrentDateGroupItem(SelectableDateGroupItem currentDateGroupItem) {
        this.currentDateGroupItem = currentDateGroupItem;
    }

    public SelectableDateGroupItem getCurrentDateGroupItem() {
        return this.currentDateGroupItem;
    }

    public JPanel getChartPanel() {
        return this.chartPanel;
    }

    public JToggleButton getUpdateCheck() {
        return this.updateCheck;
    }

    public Object[] getMultigroups() {
        return this.multigroups;
    }

    public Vector getTextsToTranslate() {
        return null;
    }

    public void setComponentLocale(Locale l) {
    }

    public void addTemplates(List templates) {
        this.templates.addAll(templates);
    }

    public JPopupMenu getOptionMenu() {
        return this.optionsMenu;
    }

    public void setOptionMenu(JPopupMenu optionMenu) {
        this.optionsMenu = optionMenu;
    }

    public void setResourceBundle(ResourceBundle res) {
        this.bundle = res;
        if (res != null) {
            try {
                if (ReportManager.getReportEngine() instanceof Internationalization) {
                    ((Internationalization) ReportManager.getReportEngine()).setResourceBundle(res);
                }
            } catch (Exception e) {
                DefaultReportDialog.logger.error(null, e);
            }
            Object separator = (this.dscr != null) && !this.dscr.equals("") ? separator = ":" : "";
            if (this.dscr != null) {
                this.setTitle(ReportUtils.getTranslation(DefaultReportDialog.TITLE_KEY, this.bundle,
                        new Object[] { ReportUtils.getTranslation(this.dscr, this.bundle, null), separator }));

            }
            if ((this.printingColumnsPanel.getBorder() != null)
                    && (this.printingColumnsPanel.getBorder() instanceof TitledBorder)) {
                ((TitledBorder) this.printingColumnsPanel.getBorder())
                    .setTitle(ReportUtils.getTranslation(DefaultReportDialog.PRINTING_COLUMN_KEY, res, null));
            }

            if ((this.groupListPanel.getBorder() != null)
                    && (this.groupListPanel.getBorder() instanceof TitledBorder)) {
                ((TitledBorder) this.groupListPanel.getBorder())
                    .setTitle(ReportUtils.getTranslation(DefaultReportDialog.GROUPS_KEY, res, null));
            }

            if ((this.functionListPanel.getBorder() != null)
                    && (this.functionListPanel.getBorder() instanceof TitledBorder)) {
                ((TitledBorder) this.functionListPanel.getBorder())
                    .setTitle(ReportUtils.getTranslation(DefaultReportDialog.FUNCTION_KEY, res, null));
            }

            if ((this.templatesPanel.getBorder() != null)
                    && (this.templatesPanel.getBorder() instanceof TitledBorder)) {
                ((TitledBorder) this.templatesPanel.getBorder())
                    .setTitle(ReportUtils.getTranslation(DefaultReportDialog.TEMPLATE_KEY, res, null));
            }

            if ((this.sqlQueryPanel.getBorder() != null) && (this.sqlQueryPanel.getBorder() instanceof TitledBorder)) {
                ((TitledBorder) this.sqlQueryPanel.getBorder())
                    .setTitle(ReportUtils.getTranslation(DefaultReportDialog.SQL_QUERY_KEY, res, null));

            }

            this.bSQL.setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.SQL_QUERY_KEY, res, null));
            this.bStore.setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.REPORT_STORE_KEY, res, null));
            this.optionsButton
                .setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.OPTIONS_BUTTON_KEY, res, null));
            this.loadButton
                .setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.LOAD_TEMPLATE_KEY, res, null));
            this.saveButton
                .setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.SAVE_TEMPLATE_KEY, res, null));
            this.bRefreshReport
                .setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.RELOAD_REPORT_KEY, res, null));
            this.orderButton
                .setToolTipText(ReportUtils.getTranslation(DefaultReportDialog.ORDER_BUTTON_KEY, res, null));

            this.grid1CheckMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.GRID_KEY, res));
            this.rowNumber2CheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.INCLUDE_ROW_NUMBER_KEY, res));
            this.includeColumnName3CheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.INCLUDE_COLUMN_NAMES_KEY, res));
            this.underlineLine4CheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.UNDERLINE_LINES_KEY, res));
            this.fitHead5CheckMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.FIT_HEADER_KEY, res));
            this.dynamicHead6CheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.DYNAMIC_HEADER_KEY, res));
            this.hideGroupDetail7CheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.HIDE_GROUP_DETAIL_KEY, res));
            this.groupStartInNewPageCheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.GROUP_START_IN_NEW_PAGE, res));
            this.firstGroupStartInNewPageCheckMenu
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.FIRST_GROUP_START_IN_NEW_PAGE, res));

            this.maximumOpMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.MAXIMUM_OP_KEY, res));
            this.minimumOpMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.MINIMUM_OP_KEY, res));
            this.sumOpMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.SUM_OP_KEY, res));
            this.averageOpMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.AVERAGE_OP_KEY, res));
            this.groupMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.GROUP_KEY, res));
            this.multilineMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.MULTILINE_KEY, res));
            this.simpleLineMenu.setText(ApplicationManager.getTranslation(DefaultReportDialog.SIMPLE_LINE_KEY, res));

            this.printingColumnList.setToolTipText(
                    ApplicationManager.getTranslation(DefaultReportDialog.DYNAMIC_SELECTION_TIP_KEY, this.bundle));

            this.updateCheck
                .setText(ApplicationManager.getTranslation(DefaultReportDialog.DYNAMIC_UPDATE_CHECK_KEY, this.bundle));
            if ((this.container != null) && (this.container instanceof JFrame)) {
                ((JFrame) this.container).setTitle(this.title);
            }

        }
    }

    public ReportEngine getReportEngine() {
        return this.reportEngine;
    }

    public JPopupMenu getGroupByDatePopup() {
        return this.groupByDatePopup;
    }

    public JRadioButtonMenuItem getGroupByDateMenuItem() {
        return this.groupByDate;
    }

    public void setGroupByDateMenuItem(JRadioButtonMenuItem groupByDate) {
        this.groupByDate = groupByDate;
    }

    public JRadioButtonMenuItem getGroupByDateTimeMenuItem() {
        return this.groupByDateTime;
    }

    public void setGroupByDateTimeMenuItem(JRadioButtonMenuItem groupByDateTime) {
        this.groupByDateTime = groupByDateTime;
    }

    public JRadioButtonMenuItem getGroupByMonthMenuItem() {
        return this.groupByMonth;
    }

    public void setGroupByMonthMenuItem(JRadioButtonMenuItem groupByMonth) {
        this.groupByMonth = groupByMonth;
    }

    public JRadioButtonMenuItem getGroupByMonthAndYearMenuItem() {
        return this.groupByMonthAndYear;
    }

    public void setGroupByMonthAndYearMenuItem(JRadioButtonMenuItem groupByMonthAndYear) {
        this.groupByMonthAndYear = groupByMonthAndYear;
    }

    public JRadioButtonMenuItem getGroupByQuarterMenuItem() {
        return this.groupByQuarter;
    }

    public void setGroupByQuarterMenuItem(JRadioButtonMenuItem groupByQuarter) {
        this.groupByQuarter = groupByQuarter;
    }

    public JRadioButtonMenuItem getGroupByQuarterAndYearMenuItem() {
        return this.groupByQuarterAndYear;
    }

    public void setGroupByQuarterAndYearMenuItem(JRadioButtonMenuItem groupByQuarterAndYear) {
        this.groupByQuarterAndYear = groupByQuarterAndYear;
    }

    public JRadioButtonMenuItem getGroupByYearMenuItem() {
        return this.groupByYear;
    }

    public void setGroupByYearMenuItem(JRadioButtonMenuItem groupByYear) {
        this.groupByYear = groupByYear;
    }

    public Table getTable() {
        return this.table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

}
