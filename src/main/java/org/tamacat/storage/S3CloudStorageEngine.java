/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.DateUtils;
import org.tamacat.util.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

public class S3CloudStorageEngine extends AbstractStorageEngine {

	static final Log LOG = LogFactory.getLog(S3CloudStorageEngine.class);

	protected String configuration = "s3cloud-storage.properties";
	protected S3Config config;
	protected String bucket;
	protected AmazonS3 s3;

	@Override
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	@Override
	public Properties getConfiguration() {
		init();
		return config.getConfiguration();
	}

	protected void init() {
		if (s3 == null) {
			config = new S3Config(configuration);
			bucket = config.getBucket();
			s3 = config.getS3Client();
		}
	}
	
	@Override
	public String getPath(StorageData data) {
		init();
		String root = getConfiguration().getProperty("root");
		if (root.endsWith("/")) {
			root = root.replaceFirst("/$","");
		}
		String path = data.getPath();
		if (path.startsWith("/")) {
			path = path.replaceFirst("^/","");
		}
		String tid = data.getTenantId();
		if (StringUtils.isEmpty(tid)) {
			tid = "common";
		}
		return root + "/" + tid + "/" + path;
	}
	
	static final long PART_SIZE = 5 * 1024L * 1024L; //5MB

	@Override
	public long createFile(StorageData data) {
		String path = getPath(data);
		LOG.debug("path="+path);

		ObjectMetadata meta = new ObjectMetadata();
		meta.setContentType(data.getContentType());
		String lastUpdated = data.getLastUpdated();
		if (StringUtils.isNotEmpty(lastUpdated)) {
			meta.setLastModified(DateUtils.parse(lastUpdated, "yyyy-MM-dd HH:mm:ss"));
		} else {
			meta.setLastModified(new Date());
		}
		meta.setContentLength(data.getEncryptedFileSize());
		
		InputStream stream = data.getEncryptedInputStream();
		
		TransferManagerBuilder builder = TransferManagerBuilder.standard()
			.withS3Client(s3).withMinimumUploadPartSize(PART_SIZE);
		TransferManager tx = builder.build();
		final Upload upload = tx.upload(bucket, path, stream, meta);
		upload.addProgressListener(new ProgressListener() {
			// This method is called periodically as your transfer progresses
			public void progressChanged(ProgressEvent progressEvent) {
				if (progressEvent.getEventType() == ProgressEventType.HTTP_RESPONSE_COMPLETED_EVENT) {
					LOG.debug("Upload complete!!!");
				}
			}
		});
		LOG.debug("createFile name="+data.getFileName() + ", type="+data.getContentType()+", path="+ path +", length="+data.getSize());

		UploadResult result = null;
		try {
			long last = 0;
			TransferProgress progress = upload.getProgress();
			while (!upload.isDone()) {
				long p = Math.round(upload.getProgress().getPercentTransferred());
				if (last > progress.getPercentTransferred()) {
					last = p;
				}
				LOG.debug("path="+path+" " + p+"%");
				Thread.sleep(1000);
			}
			upload.waitForCompletion();
			result = upload.waitForUploadResult();
		} catch (AmazonClientException | InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			tx.shutdownNow(false);
		}

		meta = s3.getObjectMetadata(bucket, path);
		long len = meta.getContentLength();
		String hash = meta.getContentMD5();
		LOG.info(path +", length="+len+ ", md5="+hash + ", ETag="+result.getETag());
		data.setHash(hash);
		data.setSize(len);
		//LOG.debug("name="+data.getFileName() + ", path="+ path +", length="+len+ ", md5="+hash + ", ETag="+result.getETag());
		return len;
	}

	@Override
	public InputStream getInputStream(StorageData data) {
		String path = getPath(data);
		LOG.debug("path="+path);

		S3Object object = s3.getObject(bucket, path);
		data.setInputStream(object.getObjectContent());
		return data.getDecryptedInputStream();
	}

	@Override
	public boolean deleteFile(StorageData data) {
		String path = getPath(data);
		ObjectListing list = s3.listObjects(bucket, path);
		for (S3ObjectSummary sum : list.getObjectSummaries()) {
			s3.deleteObject(bucket, sum.getKey());
			LOG.debug("deleted key="+ sum.getKey());
		}
		return true;
	}
	
	public Collection<S3ObjectSummary> list(StorageData data) {
		String path = getPath(data);
		ObjectListing list = s3.listObjects(bucket, path);
		return list.getObjectSummaries();
	}
}
