package org.tamacat.storage.test;

import org.tamacat.storage.S3CloudStorageEngine;
import org.tamacat.storage.StorageFactory;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MinioStorageAccess_test {

	public static void main(String[] args) {
		
		FileData data = new FileData();
		data.val(FileData.ID, "/");
		data.val(FileData.TID, "tama");
		S3CloudStorageEngine engine = StorageFactory.getInstance("minio", S3CloudStorageEngine.class);
		for (S3ObjectSummary o : engine.list(data)) {
			System.out.println(o.getKey());
		}
	}

}
