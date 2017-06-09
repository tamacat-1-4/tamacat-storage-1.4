/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import org.tamacat.di.DI;
import org.tamacat.di.DIContainer;

public class StorageFactory {

	static DIContainer di = DI.configure("storage-engine.xml");
	
	public static final StorageEngine getInstance(String name) {
		return di.getBean(name, StorageEngine.class);
	}
}
