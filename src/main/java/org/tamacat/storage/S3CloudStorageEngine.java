/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.tamacat.io.RuntimeIOException;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.DateUtils;
import org.tamacat.util.IOUtils;
import org.tamacat.util.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;

/**
 * Storage Engine for Amazon S3 compatible storage.
 * default configuration file is "s3cloud-storage.properties"
 */
public class S3CloudStorageEngine extends AbstractStorageEngine {

	static final Log LOG = LogFactory.getLog(S3CloudStorageEngine.class);
	
	protected long maxSupportFileEncryptedSize = (2 * 1024*1024*1024)-(16+1); //2GB-(16+1)byte

	protected S3Config config;
	protected String bucket;
	protected AmazonS3 s3;
	
	public S3CloudStorageEngine() {
		configuration = "s3cloud-storage.properties";
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
		TransferManager tx = TransferManagerBuilder.standard()
			.withS3Client(s3).withMinimumUploadPartSize(PART_SIZE).build();
		//data.getEncryptedInputStream()
		final Upload upload = tx.upload(bucket, path, stream, meta);

		upload.addProgressListener(new ProgressListener() {
			// This method is called periodically as your transfer progresses
			public void progressChanged(ProgressEvent progressEvent) {
				//LOG.debug(upload.getProgress().getBytesTransferred() + "byte");
				//LOG.debug(Math.round(upload.getProgress().getPercentTransferred()) + "%");
				if (progressEvent.getEventType() == ProgressEventType.HTTP_RESPONSE_COMPLETED_EVENT) {
					LOG.trace("Upload complete!!!");
				}
			}
		});
		
		//PutObjectResult result = s3.putObject(s3req);
		LOG.debug("createFile name="+data.getFileName() + ", type="+data.getContentType()+", path="+ path +", length="+data.getSize());

		UploadResult result;
		try {
			long last = 0;
			TransferProgress progress = upload.getProgress();
			while (!upload.isDone()) {
				long p = Math.round(upload.getProgress().getPercentTransferred());
				if (last > progress.getPercentTransferred()) {
					last = p;
				}
				LOG.debug("uploading... path="+path+" " + p+"%");
				Thread.sleep(3000);
			}
			upload.waitForCompletion();
			result = upload.waitForUploadResult();
		} catch (AmazonClientException | InterruptedException e) {
			//e.printStackTrace();
			throw new UploadClientException(e);
		} finally {
			tx.shutdownNow(false);
		}

		meta = s3.getObjectMetadata(bucket, path);
		long len = meta.getContentLength();
		//String hash = meta.getContentMD5();
		
		String hash = data.getMessageDigest();
		
		LOG.info(path +", length="+len+ ", hash="+hash + ", ETag="+result.getETag());
		data.setHash(hash);
		data.setSize(len);
		//data.setNote("name="+data.getFileName() + ", path="+ path +", length="+len+ ", hash="+hash + ", ETag="+result.getETag() + "\r\n");
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
	
	public InputStream getInputStream(StorageData data, long start, long end) {
		init();
		String path = getPath(data);
		LOG.debug("path="+path+", start="+start+", end="+end);
		GetObjectRequest req = new GetObjectRequest(bucket, path);
		if (start>=0 && end>0) {
			req.setRange(start, end);
		}
		/*
			@Override
			public void progressChanged(ProgressEvent event) {
				if (event.getEventType() != ProgressEventType.RESPONSE_BYTE_TRANSFER_EVENT) {
					LOG.debug(event.getEventType().name());
				} else {
					LOG.debug(event.getBytesTransferred() + "byte");
				}
			}
		});
		*/
		S3Object object = s3.getObject(req);
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
	
	@Override
	public void download(StorageData data, OutputStream out) {
		long size = data.getSize();
		if (size > 0 && size > maxSupportFileEncryptedSize) {
			long start = 0;
			long max = size / maxSupportFileEncryptedSize + 1;
			LOG.debug("loop="+max+" max="+maxSupportFileEncryptedSize);
			long count = 0;
			for (int i=0; i<max; i++) {
				count++;
				long end = maxSupportFileEncryptedSize * count;
				if (end > size) end = size;
				if (start>=end) break;
				LOG.debug(count+". start="+start+", end="+end+", size="+size);
				InputStream in = getInputStream(data, start, end);
				try {
					int len = -1;
					byte[] buf = new byte[8192];
					while ((len = in.read(buf)) != -1) {
						out.write(buf, 0, len);
					}
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				} finally {
					IOUtils.close(in);
				}
				start = end+1;
			}
		} else {
			super.download(data, out);
		}	
	}
}
