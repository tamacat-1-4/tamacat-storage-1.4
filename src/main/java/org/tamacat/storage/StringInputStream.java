package org.tamacat.storage;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class StringInputStream extends ByteArrayInputStream {
	
	final String string;

	public StringInputStream(String s) {
		super(s.getBytes(StandardCharsets.UTF_8));
		this.string = s;
	}

	public String getString() {
		return string;
	}
}
