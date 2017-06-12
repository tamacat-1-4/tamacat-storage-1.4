package org.tamacat.storage;

import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.StringUtils;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GoogleStorageEngine extends AbstractStorageEngine {

	static final Log LOG = LogFactory.getLog(S3CloudStorageEngine.class);

	protected String configuration = "google-storage.properties";
	protected GoogleStorageConfig config;
	protected String bucket;
	protected String root;

	protected Storage storage;

	public GoogleStorageEngine() {
		storage = StorageOptions.getDefaultInstance().getService();
	}

	@Override
	public long createFile(StorageData data) {
		BlobInfo blob = storage.create(BlobInfo.newBuilder(bucket, data.getFileName()).build(), data.getInputStream());
		return blob.getSize();
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		return Channels.newInputStream(storage.reader(bucket, data.getFileName()));
	}

	@Override
	public boolean deleteFile(StorageData data) {
		return storage.delete(bucket, data.getFileName());
	}

	@Override
	public String getPath(StorageData data) {
		String root = getConfiguration().getProperty("root");
		if (root.endsWith("/")) {
			root = root.replaceFirst("/$", "");
		}
		String path = data.getPath();
		if (path.startsWith("/")) {
			path = path.replaceFirst("^/", "");
		}
		String tid = data.getTenantId();
		if (StringUtils.isEmpty(tid)) {
			tid = "common";
		}
		return root + "/" + tid + "/" + path;
	}
	
	public Collection<Blob> list(StorageData data) {
		Page<Blob> page = storage.list(getPath(data));
		Iterable<Blob> ite = page.getValues();
		List<Blob> list = new ArrayList<>();
		ite.forEach(list::add);
		return list;
	}
}
