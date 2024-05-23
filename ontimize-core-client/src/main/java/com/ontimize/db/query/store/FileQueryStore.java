package com.ontimize.db.query.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.db.query.QueryExpression;
import com.ontimize.jee.common.db.query.store.QueryStore;

public class FileQueryStore implements QueryStore {

	private static final Logger	logger				= LoggerFactory.getLogger(FileQueryStore.class);

	private static final String	DEFAULT_DIRECTORY	= System.getProperty("user.home");

	private static String		SUBDIR_NAME			= ".query";

	private static final String	EXTENSION			= ".qry";

	public static final String	NULL_ENTITY			= "NULL_ENTITY";

	private String				storePath			= null;

	public FileQueryStore() {
		this.storePath = FileQueryStore.DEFAULT_DIRECTORY + File.separator + FileQueryStore.SUBDIR_NAME + (com.ontimize.gui.ApplicationManager.getApplication()
				.getName() != null ? File.separator + com.ontimize.gui.ApplicationManager.getApplication().getName() : "");

	}

	/**
	 * addQuery
	 *
	 * @param description
	 *            String
	 * @param query
	 *            QueryExpression
	 */
	@Override
	public void addQuery(String description, QueryExpression query) {
		try {
			this.save(description, query);
		} catch (Exception ex) {
			FileQueryStore.logger.error(null, ex);
		}
	}

	protected String generateFileName(String description, String entity) {
		if (description == null) {
			throw new IllegalArgumentException("Id can´t be null");
		}

		String nullEntity = FileQueryStore.NULL_ENTITY;
		if (entity != null) {
			nullEntity = "" + entity.hashCode();
		}
		return description + "-" + nullEntity + FileQueryStore.EXTENSION;
	}

	private void save(String description, QueryExpression query) throws IOException {
		String name = this.generateFileName(description, query.getEntity());
		File fAux = new File(this.storePath);
		fAux.mkdirs();
		File f = new File(this.storePath, name);
		FileOutputStream fOut = new FileOutputStream(f);
		try {
			ObjectOutputStream out = new ObjectOutputStream(fOut);
			out.writeObject(query);
			out.flush();
			out.close();
		} catch (IOException exc) {
			FileQueryStore.logger.error(null, exc);
			throw exc;
		} finally {
			if (fOut != null) {
				fOut.close();
			}
		}
	}

	private QueryExpression load(String description, String entity) throws IOException {
		String name = this.generateFileName(description, entity);
		File f = new File(this.storePath, name);
		if (!f.exists()) {
			return null;
		}
		FileInputStream fIn = new FileInputStream(f);

		try {
			ObjectInputStream in = new ObjectInputStream(fIn);
			Object o = in.readObject();
			if (o instanceof QueryExpression) {
				return (QueryExpression) o;
			} else {
				return null;
			}
		} catch (IOException exc) {
			FileQueryStore.logger.error(null, exc);
			throw exc;
		} catch (ClassNotFoundException ex) {
			FileQueryStore.logger.error(null, ex);
			return null;
		} finally {
			if (fIn != null) {
				fIn.close();
			}
		}
	}

	/**
	 * removeQuery
	 *
	 * @param description
	 *            String
	 */
	@Override
	public void removeQuery(String description, String entity) {
		try {
			this.delete(description, entity);
		} catch (Exception ex) {
			FileQueryStore.logger.error(null, ex);
		}
	}

	private void delete(String description, String entity) throws IOException {
		String name = this.generateFileName(description, entity);
		File f = new File(this.storePath, name);
		if (!f.exists()) {
			return;
		}
		f.delete();
	}

	/**
	 * list
	 *
	 * @param entity
	 *            String
	 * @return QueryExpression[]
	 */
	@Override
	public String[] list(String entity) {
		String filter = new String();
		if (entity == null) {
			filter = "-" + FileQueryStore.NULL_ENTITY + FileQueryStore.EXTENSION;
		} else {
			filter = "-" + String.valueOf(entity.hashCode()) + FileQueryStore.EXTENSION;
		}

		File f = new File(this.storePath);
		if (!f.exists()) {
			return new String[0];
		}
		String[] list = f.list();
		String[] filteredList = new String[0];

		for (String element : list) {
			String sCurrent = element;
			if (sCurrent.indexOf(filter) != -1) {
				sCurrent = sCurrent.replaceFirst(filter, "");
				String[] aux = new String[filteredList.length + 1];
				System.arraycopy(filteredList, 0, aux, 0, filteredList.length);
				aux[filteredList.length] = sCurrent;
				filteredList = aux;
			}
		}
		return filteredList;
	}

	/**
	 * get
	 *
	 * @param description
	 *            String
	 * @param entity
	 *            String
	 * @return QueryExpression
	 */
	@Override
	public QueryExpression get(String description, String entity) {
		try {
			return this.load(description, entity);
		} catch (Exception ex) {
			FileQueryStore.logger.error(null, ex);
			return null;
		}
	}

}
