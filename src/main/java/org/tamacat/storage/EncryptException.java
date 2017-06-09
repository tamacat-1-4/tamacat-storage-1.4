/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

public class EncryptException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EncryptException() {}

	public EncryptException(String message) {
		super(message);
	}

	public EncryptException(Throwable cause) {
		super(cause);
	}

	public EncryptException(String message, Throwable cause) {
		super(message, cause);
	}
}
