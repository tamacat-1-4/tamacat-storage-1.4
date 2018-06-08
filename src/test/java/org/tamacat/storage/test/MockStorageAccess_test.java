package org.tamacat.storage.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.tamacat.storage.MockStorageEngine;
import org.tamacat.storage.StorageFactory;

public class MockStorageAccess_test {

	public static void main(String[] args) {
		
		FileData data = new FileData();
		data.val(FileData.ID, "/");
		data.val(FileData.TID, "test");
		MockStorageEngine engine = StorageFactory.getInstance("test", MockStorageEngine.class);
		Collection<String> list = engine.list(data);
		assertEquals(list.size(), 0);
		for (String name : list) {
			System.out.println(name);
		}
	}
}
