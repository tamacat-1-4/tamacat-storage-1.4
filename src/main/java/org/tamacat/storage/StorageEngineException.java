/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

public class StorageEngineException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StorageEngineException() {}

	public StorageEngineException(String message) {
		super(message);
	}

	public StorageEngineException(Throwable cause) {
		super(cause);
	}

	public StorageEngineException(String message, Throwable cause) {
		super(message, cause);
	}
}
