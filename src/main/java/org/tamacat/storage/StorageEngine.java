/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public interface StorageEngine {
	
	long createFile(StorageData data);
	
	InputStream getInputStream(StorageData data);
	
	boolean deleteFile(StorageData data);
	
	void setConfiguration(String config);
	
	String getPath(StorageData data);
	
	Properties getConfiguration();
	
	Collection<?> list(StorageData data);
}
