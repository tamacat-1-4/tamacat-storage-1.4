/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.tamacat.io.RuntimeIOException;
import org.tamacat.util.IOUtils;
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
	
	@Override
	public void download(StorageData data, OutputStream out) {
		InputStream in = getInputStream(data);
		BufferedInputStream bin = new BufferedInputStream(in);
		byte[] buf = new byte[8192];
		int len;
		try {
			while ((len = bin.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
		IOUtils.close(bin);
	}
}
