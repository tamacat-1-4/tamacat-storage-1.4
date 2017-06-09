/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;

import org.tamacat.dao.Condition;
import org.tamacat.dao.DaoAdapter;
import org.tamacat.dao.DaoFactory;
import org.tamacat.dao.Query;
import org.tamacat.dao.meta.Column;
import org.tamacat.dao.meta.Columns;
import org.tamacat.dao.meta.DataType;
import org.tamacat.dao.meta.Table;
import org.tamacat.dao.meta.Tables;
import org.tamacat.dao.orm.MapBasedORMappingBean;

public class BlobStorageEngine extends AbstractStorageEngine {

	protected String id;
	protected String blob;
	protected String table;
	
	public BlobStorageEngine() {
	}

	@Override
	public long createFile(StorageData data) {
		BlobTableDao dao = DaoFactory.create(BlobTableDao.class);
		try {
			BlobTable blob = new BlobTable(data);
			dao.create(blob);
			return data.getSize();
		} finally {
			dao.release();
		}
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		return data.getInputStream();
	}

	@Override
	public boolean deleteFile(StorageData data) {
		BlobTableDao dao = DaoFactory.create(BlobTableDao.class);
		try {
			BlobTable blob = new BlobTable(data);
			int result = dao.delete(blob);
			return result == 1;
		} finally {
			dao.release();
		}
	}

	@Override
	public String getPath(StorageData data) {
		return null;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setBlob(String blob) {
		this.blob = blob;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	class BlobTable extends MapBasedORMappingBean {
		private static final long serialVersionUID = 1L;
		Column ID = Columns.create(id).type(DataType.STRING).autoGenerateId(true).primaryKey(true);
		Column BLOB = Columns.create(blob).type(DataType.OBJECT);
		Table TABLE = Tables.create(table).registerColumn(ID, BLOB);
		InputStream in;
		
		public BlobTable() {}
		
		public BlobTable(StorageData data) {
			val(ID, data.getPath());
			in = data.getInputStream();
		}
		
		public InputStream getInputStream() {
			return in;
		}
	}
	
	class BlobTableDao extends DaoAdapter<BlobTable> {
		public String getInsertSQL(BlobTable data) {
			Query<BlobTable> query = createQuery()
				.addUpdateColumns(data.ID, data.BLOB)
				.addTable(data.TABLE);
			return query.getInsertSQL(data);
		}
		
		public String getDeleteSQL(BlobTable data) {
			Query<BlobTable> query = createQuery()
				.addUpdateColumn(data.ID)
				.addTable(data.TABLE)
				.where(param(data.ID, Condition.EQUAL, data.val(data.ID)));
			return query.getDeleteSQL(data);
		}
		
		public int create(BlobTable data) {
			return executeUpdate(getInsertSQL(data), 1, data.getInputStream());
		}
	}
}
