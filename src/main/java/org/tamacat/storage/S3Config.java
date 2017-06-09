/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.util.Properties;

import org.tamacat.util.PropertyUtils;
import org.tamacat.util.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;

public class S3Config {

	protected String endpoint;
	protected String region;
	protected String bucket;
	protected boolean isPathStyleAccess;
	protected boolean isChunkedEncodingDisabled;
	
	protected AWSCredentialsProvider provider;
	protected ClientConfiguration config = new ClientConfiguration();
	protected Properties props;
	protected String signer = "S3SignerType"; //AWS3SignerType, AWS4SignerType
	
	public S3Config(String name) {
		props = PropertyUtils.getProperties(name);
		bucket = props.getProperty("bucket");
		endpoint = props.getProperty("endpoint");
		signer = props.getProperty("signer", signer);
		if (StringUtils.isNotEmpty(signer)) {
			config.setSignerOverride(signer);
		}

		if ("true".equalsIgnoreCase(props.getProperty("pathStyleAccess"))) {
			isPathStyleAccess = true;
		}
		if ("true".equalsIgnoreCase(props.getProperty("chunkedEncodingDisabled"))) {
			isChunkedEncodingDisabled = true;
		}
		
		region = props.getProperty("region");
		
		provider = new ClasspathPropertiesFileCredentialsProvider(name);

		String proxyHost = props.getProperty("proxyHost");
		if (StringUtils.isNotEmpty(proxyHost)) {
			config.setProxyHost(proxyHost);
		}
		int proxyPort = StringUtils.parse(props.getProperty("proxyPort"), -1);
		if (proxyPort > 0) {
			config.setProxyPort(proxyPort);
		}
		String proxyUser = props.getProperty("proxyUser");
		if (StringUtils.isNotEmpty(proxyUser)) {
			config.setProxyUsername(proxyUser);
		}
		String proxyPassword = props.getProperty("proxyPassword");
		if (StringUtils.isNotEmpty(proxyPassword)) {
			config.setProxyPassword(proxyPassword);
		}
	}

	public String getBucket() {
		return bucket;
	}

	public Properties getConfiguration() {
		return props;
	}

	public AmazonS3 getS3Client() {
		AmazonS3 s3 = new AmazonS3Client(provider, config);
		if (StringUtils.isNotEmpty(endpoint)) {
			s3.setEndpoint(endpoint);
		}
		if (StringUtils.isNotEmpty(region)) {
			s3.setRegion(Region.getRegion(Regions.valueOf(region)));
		}
		S3ClientOptions options = new S3ClientOptions();
		if (isPathStyleAccess) {
			options.setPathStyleAccess(true);
		}
		if (isChunkedEncodingDisabled) {
			options.setChunkedEncodingDisabled(true);
		}
		s3.setS3ClientOptions(options);
		return s3;
	}

	public AWSCredentialsProvider getAWSCredentialsProvider() {
		return provider;
	}
}
