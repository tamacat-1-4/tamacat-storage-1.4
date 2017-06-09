/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage.test;

import java.util.Collection;

import org.tamacat.dao.Condition;
import org.tamacat.dao.DaoAdapter;
import org.tamacat.dao.Query;
import org.tamacat.dao.Search;
import org.tamacat.dao.Sort;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.storage.StorageEngine;
import org.tamacat.storage.StorageFactory;

public class FileDataDao  extends DaoAdapter<FileData> {
	static final Log LOG = LogFactory.getLog(FileDataDao.class);
	
	StorageEngine engine = StorageFactory.getInstance("file-share");

	public FileDataDao() {
		setDatabase("service");
	}
	
	public FileData search(FileData data) {
		if (data.isEmpty(FileData.ID)) {
			throw new IllegalArgumentException();
		}
		Search search = createSearch();
		search.and(FileData.ID, Condition.EQUAL, data.val(FileData.ID));
		search.and(FileData.CID, Condition.EQUAL, data.val(FileData.CID));
		Query<FileData> query = createQuery()
			.select(FileData.TABLE.getColumns())
			.where(search, createSort());
		return super.search(query);
	}

	public Collection<FileData> searchList(Search search, Sort sort) {
		Query<FileData> query = createQuery()
			.select(FileData.TABLE.getColumns())
			.and(search, sort);
		return super.searchList(query, search.getStart(), search.getMax());
	}
	
	@Override
	protected String getInsertSQL(FileData data) {
		Query<FileData> query = createQuery().addUpdateColumns(FileData.TABLE.getColumns());
		return query.getInsertSQL(data);
	}

	@Override
	protected String getUpdateSQL(FileData data) {
		Query<FileData> query = createQuery().addUpdateColumns(FileData.TABLE.getColumns())
			.where(param(FileData.ID, Condition.EQUAL, data.getValue(FileData.ID)))
			.where(param(FileData.CID, Condition.EQUAL, data.getValue(FileData.CID)));
		return query.getUpdateSQL(data);
	}

	@Override
	protected String getDeleteSQL(FileData data) {
		Query<FileData> query = createQuery().addUpdateColumn(FileData.ID)
			.where(param(FileData.ID, Condition.EQUAL, data.getValue(FileData.ID)))
			.where(param(FileData.CID, Condition.EQUAL, data.val(FileData.CID)));

		if (data.isNotEmpty(FileData.FOLDER_ID)) {
			query.where(param(FileData.FOLDER_ID, Condition.EQUAL, data.getValue(FileData.FOLDER_ID)));
		}
		return query.getDeleteSQL(data);
	}
	
	protected String getUpdateTrashSQL(FileData data) {
		Query<FileData> query = createQuery()
			.addUpdateColumns(FileData.TRASH, FileData.TRASH_DATE, FileData.ORIGIN_CATEGORY)
			.where(param(FileData.ID, Condition.EQUAL, data.getValue(FileData.ID)))
			.where(param(FileData.CID, Condition.EQUAL, data.val(FileData.CID)));
		return query.getUpdateSQL(data);
	}
	
	public int createWithFile(FileData data) {
		int result = 0;
		startTransaction();
		try {
			long size = engine.createFile(data);
			data.val(FileData.SIZE, size);
			result = super.create(data);
			commit();
		} catch (Exception e) {
			rollback();
			handleException(e);
		} finally {
			endTransaction();
		}
		return result;
	}
	
	/**
	 * ゴミ箱に入れる処理
	 * @param data
	 * @return
	 */
	public int trash(FileData data) {
		data.val(FileData.TRASH, 1);
		data.val(FileData.TRASH_DATE, getTimestampString());
		return executeUpdate(getUpdateTrashSQL(data));
	}
	
	/**
	 * ゴミ箱から戻す処理
	 * @param data
	 * @return
	 */
	public int revert(FileData data) {
		data.val(FileData.TRASH, 0);
		data.val(FileData.TRASH_DATE, "");
		return executeUpdate(getUpdateTrashSQL(data));
	}

	public int deleteWithFile(FileData data) {
		int result = 0;
		boolean isTran = isTransactionStarted();
		if (!isTran) startTransaction();
		try {
			engine.deleteFile(data);
			result = super.delete(data);
			if (!isTran) commit();
		} catch (Exception e) {
			if (!isTran) rollback();
			handleException(e);
		} finally {
			if (!isTran) endTransaction();
		}
		return result;
	}
}
