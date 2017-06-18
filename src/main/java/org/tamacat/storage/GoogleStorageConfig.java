package org.tamacat.storage;

import java.io.IOException;
import java.util.Properties;

import org.tamacat.util.ClassUtils;
import org.tamacat.util.PropertyUtils;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GoogleStorageConfig {

	static final String CREDENTIALS_KEY = "credentials";
	static final String BUCKET_KEY = "bucket";
	static final String ROOT_KEY = "root";
	
	String credentialsJson;
	String bucket;
	String root;
	
	public GoogleStorageConfig(String config) {
		Properties props = PropertyUtils.getProperties(config);
		credentialsJson = props.getProperty(CREDENTIALS_KEY);
		bucket = props.getProperty(BUCKET_KEY);
		root = props.getProperty(ROOT_KEY);
	}
	
	public Storage getStorage() {
		try {
			return StorageOptions.newBuilder().setCredentials(
				ServiceAccountCredentials.fromStream(ClassUtils.getURL(credentialsJson).openStream()))
				   .build().getService();
		} catch (IOException e) {
			throw new StorageEngineException(e);
		}
	}
	
	public String getBucket() {
		return bucket;
	}
	
	public String getRoot() {
		return root;
	}
}
