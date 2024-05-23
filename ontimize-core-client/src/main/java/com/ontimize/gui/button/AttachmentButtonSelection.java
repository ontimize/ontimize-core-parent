package com.ontimize.gui.button;

import java.awt.LayoutManager;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.ontimize.gui.Form;
import com.ontimize.gui.attachment.AttachmentListPopup;
import com.ontimize.gui.field.DataComponent;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.gui.attachment.AttachmentAttribute;
import com.ontimize.util.swing.ButtonSelection;

/**
 * Button extension for attachments
 */
public class AttachmentButtonSelection extends ButtonSelectionInternationalization implements DataComponent {

	protected AttachmentAttribute attachmentAttribute;

	protected String entityName;

	protected Form form;

	protected ImageIcon attachmentIcon;

	protected ImageIcon noAttachmentIcon;

	/**
	 * Constructor
	 * @param attachmentIcon {@link Icon} for the button
	 * @param attachmentEmptyIcon
	 * @param b {@link Boolean} for highlight the button
	 * @param entityName Name of the attachment entity
	 * @param form {@link Form} associated to the button
	 */
	public AttachmentButtonSelection(Icon attachmentIcon, Icon attachmentEmptyIcon, boolean b, String entityName,
			Form form) {
		super(attachmentIcon, b);
		this.attachmentIcon = (ImageIcon) attachmentIcon;
		this.noAttachmentIcon = (ImageIcon) attachmentEmptyIcon;
		this.entityName = entityName;
		this.form = form;
		this.attachmentAttribute = new AttachmentAttribute(entityName, form.getKeys());
	}

	@Override
	public void init(Map<Object, Object> parameters) throws Exception {
	}

	@Override
	public Object getConstraints(LayoutManager parentLayout) {
		return null;
	}

	@Override
	public Object getAttribute() {
		return this.attachmentAttribute;
	}

	@Override
	public void initPermissions() {
	}

	@Override
	public boolean isRestricted() {
		return false;
	}

	@Override
	public String getLabelComponentText() {
		return null;
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {

		if (value == null) {
			this.deleteData();
		} else {

			ButtonSelection button = this;
			if (button != null) {

				JList jList = button.getMenuList();
				AttachmentListPopup attachmentList = null;
				if (jList instanceof AttachmentListPopup) {
					attachmentList = (AttachmentListPopup) jList;
				}

				if (attachmentList == null) {
					attachmentList = new AttachmentListPopup(this.form);
					button.setMenuList(attachmentList);
				}

				if (value instanceof EntityResult) {
					attachmentList.setAttachments((EntityResult) value);
					this.setIcon(this.getAttachmentIcon());
				}
			}
		}
	}

	public ImageIcon getAttachmentIcon() {
		return this.attachmentIcon;
	}

	public void setAttachmentIcon(ImageIcon attachmentIcon) {
		this.attachmentIcon = attachmentIcon;
	}

	public ImageIcon getNoAttachmentIcon() {
		return this.noAttachmentIcon;
	}

	public void setNoAttachmentIcon(ImageIcon noAttachmentIcon) {
		this.noAttachmentIcon = noAttachmentIcon;
	}

	@Override
	public void deleteData() {
		JList jList = this.getMenuList();
		if (jList instanceof AttachmentListPopup) {
			((AttachmentListPopup) jList).removeAttachment();
		}
		this.setIcon(this.getNoAttachmentIcon());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean isModifiable() {
		return false;
	}

	@Override
	public void setModifiable(boolean modifiable) {
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public int getSQLDataType() {
		return 0;
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public void setRequired(boolean required) {
	}

}
