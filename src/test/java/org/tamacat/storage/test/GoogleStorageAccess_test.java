package org.tamacat.storage.test;

import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
import org.tamacat.storage.GoogleStorageEngine;
import org.tamacat.storage.StorageFactory;

import com.google.cloud.storage.Blob;

public class GoogleStorageAccess_test {

	public static void main(String[] args) {
		
		FileData data = new FileData();
		data.val(FileData.ID, "/");
		data.val(FileData.CID, "test");
		GoogleStorageEngine engine = StorageFactory.getInstance("google", GoogleStorageEngine.class);
		for (Blob o : engine.list(data)) {
			System.out.println("name="+o.getName()+", size="+o.getSize()+", type="+o.getContentType());
			
			FileData f = new FileData();
			f.val(FileData.ID, o.getName());
			f.val(FileData.CID);
			try {
				System.out.println(Base64.getEncoder().encodeToString(IOUtils.toString(engine.getInputStream(f)).getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			//engine.deleteFile(f);
		}
	}

}
