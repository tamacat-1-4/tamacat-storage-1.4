/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.tamacat.dao.exception.InvalidParameterException;
import org.tamacat.io.RuntimeIOException;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.IOUtils;
import org.tamacat.util.StringUtils;

public class LocalDiskStorageEngine extends AbstractStorageEngine {

	static final Log LOG = LogFactory.getLog(LocalDiskStorageEngine.class);

	public String getPath(StorageData data) {
		String root = getConfiguration().getProperty("root");
		if (root.endsWith("/")) {
			root = root.replaceFirst("/$", "");
		}
		String path = data.getPath();
		if (path == null) {
			throw new InvalidParameterException();
		}
		if (path.startsWith("/")) {
			path = path.replaceFirst("^/", "");
		}
		String tid = data.getTenantId();
		if (StringUtils.isEmpty(tid)) {
			tid = "common";
		}
		return root + "/" + tid +"/" + path;
	}

	@Override
	public long createFile(StorageData data) {
		FileOutputStream out = null;
		long result = 0;
		try {
			String path = getPath(data);
			LOG.info("create: "+path);
			File file = new File(path);
			file.getParentFile().mkdirs();
			out = new FileOutputStream(file);
			BufferedInputStream in = new BufferedInputStream(data.getEncryptedInputStream());
			long size = 0;
			int len;
			byte[] buf = new byte[8192];
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
				size += len;
			}
			data.setSize(size);
			result = size;
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		} finally {
			IOUtils.close(out);
		}
		return result;
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		try {
			data.setInputStream(new FileInputStream(getPath(data)));
			return data.getDecryptedInputStream();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
	}

	@Override
	public boolean deleteFile(StorageData data) {
		File file = new File(getPath(data));
		LOG.debug("delete file: " + file);
		return file.delete();
	}
}
