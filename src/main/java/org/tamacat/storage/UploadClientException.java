/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

public class UploadClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UploadClientException() {}

	public UploadClientException(String message) {
		super(message);
	}

	public UploadClientException(Throwable cause) {
		super(cause);
	}

	public UploadClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public UploadClientException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
