/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.util.Properties;

import org.tamacat.util.PropertyUtils;

public abstract class AbstractStorageEngine implements StorageEngine {

	protected String configuration = "storage.properties";

	@Override
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	@Override
	public Properties getConfiguration() {
		return PropertyUtils.getProperties(configuration);
	}
}
