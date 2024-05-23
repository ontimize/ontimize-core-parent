package com.ontimize.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Node;

import com.ontimize.jee.common.builder.CustomNode;

public class OModule {

	protected String id;

	protected String resources;

	protected String clientBaseClasspath = "";

	protected List<CustomNode> formManagers = new ArrayList<>();

	protected CustomNode toolbar;

	protected CustomNode menu;

	protected String toolbarListener;

	protected String menuListener;

	protected String localEntityPackage;

	protected List<String> localEntities;

	// Server
	protected String serverPackage;

	protected Properties entityProperties;

	protected String entityClass;

	protected Node remoteReferences;

	protected Node references;

	public OModule(String id) {
		this.id = id;
	}

	public String getClientBaseClasspath() {
		return this.clientBaseClasspath;
	}

	public void setClientBaseClasspath(String clientPackage) {
		this.clientBaseClasspath = clientPackage;
		if ((this.clientBaseClasspath == null) || (this.clientBaseClasspath.length() == 0)) {
			this.clientBaseClasspath = "";
		} else {
			this.clientBaseClasspath = this.clientBaseClasspath.replace('.', '/').concat("/");
		}
	}

	public List<CustomNode> getFormManagers() {
		return this.formManagers;
	}

	public void setFormManagers(List<CustomNode> formManagers) {
		this.formManagers = formManagers;
	}

	public CustomNode getToolbar() {
		return this.toolbar;
	}

	public void setToolbar(CustomNode toolbar) {
		this.toolbar = toolbar;
	}

	public String getToolbarListener() {
		return this.toolbarListener;
	}

	public void setToolbarListener(String toolbarListener) {
		this.toolbarListener = toolbarListener;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResources() {
		return this.resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

	public CustomNode getMenu() {
		return this.menu;
	}

	public void setMenu(CustomNode menu) {
		this.menu = menu;
	}

	public String getMenuListener() {
		return this.menuListener;
	}

	public void setMenuListener(String menuListener) {
		this.menuListener = menuListener;
	}

	public String getLocalEntityPackage() {
		return this.localEntityPackage;
	}

	public void setLocalEntityPackage(String localEntityPackage) {
		this.localEntityPackage = localEntityPackage;
	}

	public List<String> getLocalEntities() {
		return this.localEntities;
	}

	public void setLocalEntities(List<String> localEntities) {
		this.localEntities = localEntities;
	}

	public String getServerPackage() {
		return this.serverPackage;
	}

	public void setServerPackage(String serverPackage) {
		this.serverPackage = serverPackage;
	}

	public Properties getEntityProperties() {
		return this.entityProperties;
	}

	public void setEntityProperties(Properties entityProperties) {
		this.entityProperties = entityProperties;
	}

	public String getEntityClass() {
		return this.entityClass == null ? "com.ontimize.db.DefaultTableEntity" : this.entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public Node getRemoteReferences() {
		return this.remoteReferences;
	}

	public void setRemoteReferences(Node remoteReferences) {
		this.remoteReferences = remoteReferences;
	}

	public Node getReferences() {
		return this.references;
	}

	public void setReferences(Node references) {
		this.references = references;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OModule) {
			return this.id.equals(((OModule) obj).getId());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

}
