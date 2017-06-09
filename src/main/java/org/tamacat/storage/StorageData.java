/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;

public interface StorageData {

	boolean isDelete();
	
	void setDelete(boolean isDelete);
	
	boolean isNew();
	
	void setNew(boolean isNew);
	
	String getTenantId();
	
	String getPath();
	
	String getFileName();
	
	String getHash();
	
	void setHash(String hash);
	
	String getContentType();
	
	String getLastUpdated();
	
	void setEncrypted(boolean isEncrypted);
	
	boolean isEncrypted();
	
	void setSecretKey(String secretKey);
	
	InputStream getInputStream();
	
	void setInputStream(InputStream stream);
	
	InputStream getEncryptedInputStream();

	InputStream getDecryptedInputStream();
	
	void setSize(long size);
	
	long getSize();
	
	long getEncryptedFileSize();
	
	String getFileSize();
}
