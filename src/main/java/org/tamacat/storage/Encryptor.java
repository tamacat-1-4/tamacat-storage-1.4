/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.util.PropertyUtils;
import org.tamacat.util.StringUtils;

public class Encryptor {
	
	static final Log LOG = LogFactory.getLog(Encryptor.class);
	
	protected String propertiesFile = "encryptor.properties";
	
	protected String algorithm = "AES/CBC/PKCS5Padding";
	protected String iv;
	
	protected String salt = "DA0a5!3!-9*F7E%&Eb41e#d*6A+aD5ec";

	protected int iterationCount = 32;
	protected int keyLength = 256; //128,192,256;
	protected Charset encoding = StandardCharsets.UTF_8; //"UTF-8";
	
	public Encryptor() {
		loadProperties();
	}

	public Encryptor(String algorithm) {
		this.algorithm = algorithm;
		loadProperties();
	}
	
	public Encryptor(String algorithm, int keyLength, String salt, int iterationCount) {
		this.algorithm = algorithm;
		this.keyLength = keyLength;
		this.salt = salt;
		this.iterationCount = iterationCount;
	}
	
	void loadProperties() {
		try {
			Properties props = PropertyUtils.getProperties(propertiesFile);
			String defaultSalt = props.getProperty("salt");
			if (StringUtils.isEmpty(defaultSalt)) {
				salt = defaultSalt;
			}
			iterationCount = StringUtils.parse(props.getProperty("iteration_count"), 32);
			keyLength = StringUtils.parse(props.getProperty("key_length"), 128);
		} catch (Exception e) {
			LOG.warn("Loading error in "+propertiesFile+". : "+e.getMessage());
		}
	}

	public String getSalt() {
		if (StringUtils.isNotEmpty(salt)) {
			return salt;
		}
		throw new EncryptException("salt is null.");
	}
	
	public void setIterationCount(int iterationCount) {
		this.iterationCount = iterationCount;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	public void setIv(String iv) {
		this.iv = iv;
	}
	
	protected IvParameterSpec getIvParameterSpec(String secretKey) {
		IvParameterSpec ips = null;
		if (algorithm.indexOf("CBC")>=0) {
			if (StringUtils.isNotEmpty(iv)) {
				if (iv.length() >= 16) {
					iv = iv.substring(0, 16);
				}
				ips = new IvParameterSpec(iv.getBytes(encoding));
			} else if (secretKey.length() >= 16) {
				ips = new IvParameterSpec(secretKey.substring(0, 16).getBytes(encoding));
			}
		}
		return ips;
	}
	
	public InputStream encrypt(String secretKey, InputStream in) {
		if (secretKey == null) {
			throw new EncryptException("secret key is null.");
		}
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(secretKey), getIvParameterSpec(secretKey));
			CipherInputStream cin = new CipherInputStream(in, cipher);
			return cin;
		} catch (Exception e) {
			e.printStackTrace();
			throw new EncryptException(e);
		}
	}

	public InputStream decrypt(String secretKey, InputStream in) {
		try {
			Cipher cipher = Cipher.getInstance(algorithm);
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(keyLength);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(secretKey), getIvParameterSpec(secretKey));
			CipherInputStream cin = new CipherInputStream(in, cipher);
			return cin;
		} catch (Exception e) {
			throw new EncryptException(e);
		}
	}

	public static long getEncryptedLength(long len) {
		return len + (16 - (len % 16));
	}

	protected static String printBase64Binary(byte[] value) {
		return DatatypeConverter.printBase64Binary(value);
	}

	protected static String printHexBinary(byte[] value) {
		return DatatypeConverter.printHexBinary(value);
	}

	protected SecretKey getSecretKey(String secretKey) throws Exception {
		//PKCS #5 v2.0 Password based encryption. 
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), getSalt().getBytes(), iterationCount, keyLength);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		return secret;
	}
}
