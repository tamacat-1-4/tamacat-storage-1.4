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
import com.google.cloud.storage.Storage.BlobListOption;

/**
 * StorageEngine for GoogleStorage.
 * @see https://github.com/GoogleCloudPlatform/google-cloud-java
 */
public class GoogleStorageEngine extends AbstractStorageEngine {

	static final Log LOG = LogFactory.getLog(S3CloudStorageEngine.class);

	protected GoogleStorageConfig config;
	protected Storage storage;

	public GoogleStorageEngine() {}
	
	@Override
	public void setConfiguration(String configuration) {
		config = new GoogleStorageConfig(configuration);
	}

	public Storage getStorage() {
		if (storage == null) {
			storage = config.getStorage();
		}
		return storage;
	}

	@Override
	public long createFile(StorageData data) {
		BlobInfo blob = getStorage().create(
			BlobInfo.newBuilder(config.getBucket(), data.getFileName()).build(), data.getInputStream()
		);
		return blob.getSize();
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		return Channels.newInputStream(getStorage().reader(config.getBucket(), data.getPath()));
	}

	@Override
	public boolean deleteFile(StorageData data) {
		return getStorage().delete(config.getBucket(), data.getPath());
	}

	@Override
	public String getPath(StorageData data) {
		String root = config.getRoot();
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
		String path = getPath(data);
		Page<Blob> page = getStorage().list(config.getBucket(), BlobListOption.prefix(path));
		Iterable<Blob> ite = page.getValues();
		List<Blob> list = new ArrayList<>();
		ite.forEach(list::add);
		return list;
	}
}
