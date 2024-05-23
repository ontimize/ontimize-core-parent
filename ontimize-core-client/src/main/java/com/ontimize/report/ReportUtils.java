package com.ontimize.report;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.print.PageFormat;
import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.preferences.ApplicationPreferences;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.locator.EntityReferenceLocator;
import com.ontimize.jee.common.report.store.ReportStore;
import com.ontimize.report.utils.PreviewDialog;
import com.ontimize.report.utils.ReportProcessor;

public class ReportUtils {

	private static final Logger			logger						= LoggerFactory.getLogger(ReportUtils.class);

	public static final String			REPORT_NAME					= "ReportDesigner.ReportName";

	public static final String			REPORT_STORE				= "ReportDesigner.ReportStore";

	public static final String			REPORT_SAVE					= "ReportDesigner.ReportSaveDialog";

	public static final String			STORED_REPORT_LIST			= "ReportDesigner.StoredReportList";

	private static List<Object>			templateList				= new ArrayList<Object>();

	public static final int				MIN							= 0;

	public static final int				MAX							= 1;

	public static final int				SUM							= 2;

	public static final int				AVG							= 3;

	public static final int				GROUP_BY_DATE_TIME			= 0;

	public static final int				GROUP_BY_DATE				= 1;

	public static final int				GROUP_BY_MONTH				= 2;

	public static final int				GROUP_BY_MONTH_AND_YEAR		= 3;

	public static final int				GROUP_BY_QUARTER			= 4;

	public static final int				GROUP_BY_QUARTER_AND_YEAR	= 5;

	public static final int				GROUP_BY_YEAR				= 6;

	public static final int				LIST_MOUSE_X_MAX			= 24;

	private static Object				emptyReport					= null;

	protected TableModel				m							= null;

	protected ResourceBundle			res							= null;

	protected List<Object>				templates					= null;

	protected String					pageTitle					= null;

	protected String					dscr						= "";

	protected String					user						= null;

	protected String					preferenceKey				= null;

	protected ApplicationPreferences	preferences					= null;

	public ReportUtils(TableModel m, String titPag, ResourceBundle res, List<Object> templateList, String descr) {
		this(m, titPag, res, templateList, descr, null, null, null);
	}

	public ReportUtils(TableModel m, String pageTitle, ResourceBundle res, List<Object> templateList, String descr, String user, String preferenceKey,
			ApplicationPreferences prefs) {

		this.m = m;
		this.pageTitle = pageTitle;
		this.res = res;
		if (templateList == null) {
			this.templates = templateList;
		} else {
			this.templates = templateList;
		}

		if (descr != null) {
			this.dscr = descr;
		}
		this.user = user;
		this.preferenceKey = preferenceKey;
		this.preferences = prefs;
	}

	public void setModel(TableModel m) {
		this.m = m;
	}

	public void setResourceBundle(ResourceBundle res) {
		this.res = res;
	}

	public DefaultReportDialog createDefaultDialog(Component c, String reportDescription) {
		ReportUtils.logger.debug("{} default dialog asked", this.getClass().getName());
		DefaultReportDialog defaultDialog = null;
		if (defaultDialog == null) {
			ReportUtils.logger.debug("{} creating default dialog", this.getClass().getName());

			Window w = SwingUtilities.getWindowAncestor(c);
			if (c instanceof Table) {
				if (w instanceof Frame) {
					defaultDialog = new DefaultReportDialog((Frame) w, this.m, this.res, this.templates, this.pageTitle, this.dscr, this.user, this.preferenceKey, this.preferences,
							(Table) c);
				} else {
					defaultDialog = new DefaultReportDialog((Dialog) w, this.m, this.res, this.templates, this.pageTitle, this.dscr, this.user, this.preferenceKey,
							this.preferences, (Table) c);
				}
			} else {
				if (w instanceof Frame) {
					defaultDialog = new DefaultReportDialog((Frame) w, this.m, this.res, this.templates, this.pageTitle, this.dscr, this.user, this.preferenceKey,
							this.preferences);
				} else {
					defaultDialog = new DefaultReportDialog((Dialog) w, this.m, this.res, this.templates, this.pageTitle, this.dscr, this.user, this.preferenceKey,
							this.preferences);
				}
			}

			defaultDialog.setReportDescription(reportDescription);
			defaultDialog.pack();
			defaultDialog.center();
		}
		return defaultDialog;

	}

	/**
	 *
	 */
	public static void showDefaultReportDesigner(Frame f, ReportStore[] rs, EntityReferenceLocator referenceLocator, ResourceBundle res, List<Object> template, String tit,
			String descr) {
		DefaultReportDialog defaultReport = new DefaultReportDialog(f, rs, referenceLocator, res, template, tit, descr);
		defaultReport.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		defaultReport.setSize(screenSize.width - 80, screenSize.height - 100);
		defaultReport.center();
		defaultReport.setVisible(true);
	}

	/**
	 * Shows a dialog to configure the chart of reports for a table. It is possible to choose the series, the x axis column, the y axis column and the type of chart
	 * 
	 * @param c
	 * @param reportDescription
	 */
	public void showDefaultReportDialog(Component c, String reportDescription) {
		this.showDefaultReportDialog(c, reportDescription, null);
	}

	public void showDefaultReportDialog(DefaultReportDialog reportDialog, String configuration) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		reportDialog.setSize(screenSize.width - 80, screenSize.height - 100);

		reportDialog.setResourceBundle(this.res);
		reportDialog.getReportEngine().setTitleReport("");
		reportDialog.getReportEngine().setReportDescription("");
		reportDialog.updateReport();

		if (configuration != null) {
			reportDialog.loadConfiguration(configuration);
		}
		reportDialog.center();
		reportDialog.setVisible(true);
	}

	public void showDefaultReportDialog(Component c, String reportDescription, String configuration) {
		DefaultReportDialog reportDialog = this.createDefaultDialog(c, reportDescription);
		this.showDefaultReportDialog(reportDialog, configuration);
	}

	public void showDefaultReportDialog(Component c) {
		this.showDefaultReportDialog(c, null);
	}

	public static URL getDefaultBase() {
		try {
			String tmpDir = System.getProperty("java.io.tmpdir", ".");
			String out = tmpDir.endsWith(System.getProperty("file.separator", "/")) ? tmpDir + "out.xml" : tmpDir + System.getProperty("file.separator", "/") + "out.xml";
			return new File(out).toURL();
		} catch (Exception e) {
			ReportUtils.logger.trace(null, e);
			return null;
		}
	}

	/**
	 * Sets the report template list for the custom reports.
	 * 
	 * @param list
	 *            a <code>List</code> with the list of template paths.
	 */
	public static void setCustomReportTemplates(List<Object> list) {
		ReportUtils.templateList = list;
	}

	public static String getTranslation(String sText, ResourceBundle res, Object[] args) {
		if (res == null) {
			return new String(sText);
		} else {
			try {
				String trad = res.getString(sText);

				// Args
				if (args != null) {
					String tradArgs = MessageFormat.format(trad, args);
					return tradArgs;
				} else {
					return trad;
				}
			} catch (Exception e) {
				ReportUtils.logger.trace(null, e);
				StringBuilder sb = new StringBuilder();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						sb.append(args[i] + " ");
					}
				}
				ReportUtils.logger.debug("{} --> argumentos: {}", e.getMessage(), sb);
				return new String(sText);
			}
		}
	}

	public static List<Object> getCustomReportTemplates() {
		return ReportUtils.templateList;
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, String xMLTemplate, URL base, ReportProcessor rp) throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, xMLTemplate, base, rp);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, String template, URL base) throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, template, base);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, URL template, URL base, ReportProcessor rp) throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, template, base, rp);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, URL template, URL base) throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, template, base);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc) throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base, order, asc);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc, ReportProcessor rp)
			throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, template, base, order, asc, rp);
	}

	public static PreviewDialog showPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc, ReportProcessor rp, PageFormat pf)
			throws Exception {
		return ReportManager.getReportEngine().showPreviewDialog(c, title, m, template, base, order, asc, rp, pf);
	}

	public static PreviewDialog getPreviewDialog(Component c, String title, TableModel m, URL template, URL base, ReportProcessor r) throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base, r);
	}

	public static PreviewDialog getPreviewDialog(Component c, String title, TableModel m, URL template, URL base) throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base);
	}

	public static PreviewDialog getPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc) throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base, order, asc);
	}

	public static PreviewDialog getPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc, ReportProcessor r)
			throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base, order, asc, r);
	}

	public static PreviewDialog getPreviewDialog(Component c, String title, TableModel m, URL template, URL base, String[] order, boolean[] asc, ReportProcessor r, PageFormat pf)
			throws Exception {
		return ReportManager.getReportEngine().getPreviewDialog(c, title, m, template, base, order, asc, r, pf);
	}

}
