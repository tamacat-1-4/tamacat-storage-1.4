/*
 * Copyright (c) 2018 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.StringUtils;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Storage Engine for Amazon S3
 * default configuration file is "aws-s3.properties"
 */
public class AmazonS3StorageEngine extends S3CloudStorageEngine {

	static final Log LOG = LogFactory.getLog(AmazonS3StorageEngine.class);
	
	public AmazonS3StorageEngine() {
		configuration = "aws-s3.properties";
	}
	
	protected void init() {
		if (s3 == null) {
			config = new S3Config(configuration);
			bucket = config.getBucket();
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
			
			String region = config.getRegion();
			if (StringUtils.isNotEmpty(region)) {
				builder.withRegion(region);
			}
			s3 = builder.build();
		}
	}
}
