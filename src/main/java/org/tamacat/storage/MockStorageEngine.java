/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.PropertyUtils;

public class MockStorageEngine implements StorageEngine {

	static final Log LOG = LogFactory.getLog(MockStorageEngine.class);
	
	protected String configuration = "test-storage.properties";
	
	@Override
	public long createFile(StorageData data) {
		LOG.debug("#createFile id="+data.getPath()+", path="+getPath(data));
		return 0;
	}

	@Override
	public boolean deleteFile(StorageData data) {
		LOG.debug("#deleteFile id="+data.getPath()+", path="+getPath(data));
		return true;
	}
	
	@Override
	public void setConfiguration(String config) {
		this.configuration = config;
	}

	@Override
	public String getPath(StorageData data) {
		return data.getPath();
	}

	@Override
	public Properties getConfiguration() {
		return PropertyUtils.getProperties(configuration);
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		//ByteArrayInputStream in = new ByteArrayInputStream(data.getInputStream());
		return data.getInputStream();
	}

	@Override
	public Collection<String> list(StorageData data) {
		List<String> list = new ArrayList<>();
		return list;
	}
}
