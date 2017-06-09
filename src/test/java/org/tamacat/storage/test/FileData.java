/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage.test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;

import org.tamacat.dao.meta.Column;
import org.tamacat.dao.meta.Columns;
import org.tamacat.dao.meta.DataType;
import org.tamacat.dao.meta.Table;
import org.tamacat.dao.meta.Tables;
import org.tamacat.dao.orm.MapBasedORMappingBean;
import org.tamacat.storage.Encryptor;
import org.tamacat.storage.StorageData;
import org.tamacat.util.StringUtils;

public class FileData extends MapBasedORMappingBean implements StorageData {

	private static final long serialVersionUID = 1L;

	public static final Column ID = Columns.create("id").autoGenerateId(true);
	public static final Column NAME = Columns.create("name");
	public static final Column DESCRIPTION = Columns.create("description");
	public static final Column CONTENT_TYPE = Columns.create("content_type");
	public static final Column SIZE = Columns.create("size").type(DataType.NUMERIC);
	public static final Column PATH = Columns.create("path");
	public static final Column ICON = Columns.create("icon");
	public static final Column HASH = Columns.create("hash");
	public static final Column UPDATE_DATE = Columns.create("update_date").type(DataType.TIME).autoTimestamp(true);
	public static final Column FOLDER_ID = Columns.create("folder_id");
	public static final Column CID = Columns.create("cid");
	
	public static final Column ENCRYPTED = Columns.create("encrypted").type(DataType.NUMERIC);
	public static final Column SECRET_KEY = Columns.create("secret_key");
	
	public static final Column TRASH = Columns.create("trash").type(DataType.NUMERIC);
	public static final Column TRASH_DATE = Columns.create("trash_date").type(DataType.TIME);
	public static final Column ORIGIN_CATEGORY = Columns.create("origin_category");
	public static final Column ORIGIN_FOLDER = Columns.create("origin_folder");
	
	public static final Table TABLE = Tables.create("file_data")
		.registerColumn(ID,NAME,DESCRIPTION,CONTENT_TYPE,SIZE,PATH,ICON,
			HASH,UPDATE_DATE,FOLDER_ID,CID,ENCRYPTED,SECRET_KEY,
			TRASH,TRASH_DATE,ORIGIN_CATEGORY,ORIGIN_FOLDER);

	protected boolean isDelete;
	protected boolean isNew;
	protected InputStream stream;
	protected Path filePath;
	protected Encryptor encryptor = new Encryptor();
	
	public FileData() {
		defaultTableName = TABLE.getTableNameWithSchema();
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}
	
	public Path getFilePath() {
		return filePath;
	}
	
	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	public InputStream getInputStream() {
		return stream;
	}
	
	public void setInputStream(InputStream stream) {
		this.stream = stream;
	}
	
	public String getFileName() {
		return val(NAME);
	}
	
	public String getTenantId() {
		return val(CID);
	}
	
	/**
	 * Size with unit.
	 */
	public String getFileSize() {
		return getSize(val(SIZE));
	}
	
	/**
	 * Original size.
	 */
	public long getSize() {
		return StringUtils.parse(val(SIZE), 0L);
	}

	@Override
	public String getPath() {
		return val(ID);
	}

	@Override
	public void setSize(long size) {
		val(SIZE, size);
	}
	
	public String getContentType() {
		return val(CONTENT_TYPE);
	}
	
	public String getHash() {
		return val(HASH);
	}
	
	public void setHash(String hash) {
		val(HASH, hash);
	}
	
	public String getLastUpdated() {
		return val(UPDATE_DATE);
	}
	
	@Override
	public String getSize(String value) {
		long length = StringUtils.parse(value, 0L);
		if (length <1024*1024) {
			return BigDecimal.valueOf((double)length/1024).setScale(1, BigDecimal.ROUND_UP) +" KB";
		} else if (length <1024*1024*1024) {
			return BigDecimal.valueOf((double)length/1024/1024).setScale(1, BigDecimal.ROUND_UP) +" MB";
		} else {
			return BigDecimal.valueOf((double)length/1024/1024/1024).setScale(1, BigDecimal.ROUND_UP) +" GB";
		}
	}
	
	@Override
	public void setEncrypted(boolean isEncrypted) {
		val(ENCRYPTED, isEncrypted? "1" : "0");
	}

	@Override
	public boolean isEncrypted() {
		return "1".equals(val(ENCRYPTED)) && isNotEmpty(SECRET_KEY);
	}
	
	@Override
	public void setSecretKey(String secretKey) {
		val(SECRET_KEY, secretKey);
		encryptor.setSalt(secretKey);
	}

	@Override
	public InputStream getEncryptedInputStream() {
		if (isEncrypted()) {
			return encryptor.encrypt(val(SECRET_KEY), stream);
		} else {
			return stream;
		}
	}

	@Override
	public InputStream getDecryptedInputStream() {
		if (isEncrypted()) {
			return encryptor.decrypt(val(SECRET_KEY), stream);
		} else {
			return stream;
		}
	}

	/**
	 * Encrypted file size.
	 */
	@Override
	public long getEncryptedFileSize() {
		if (isEncrypted()) {
			return Encryptor.getEncryptedLength(getSize());
		} else {
			return getSize();
		}
	}
}
