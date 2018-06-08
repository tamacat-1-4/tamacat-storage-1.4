package org.tamacat.storage;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.junit.Test;

public class EncryptorTest {

	static String secret = "0123456789abcdefgfijklmnopqrstuvwxyz";
	
	@Test
	public void testEncryptAndDecrypt() throws Exception {
		Encryptor encryptor = new Encryptor();
		InputStream in = encryptor.encrypt(secret, new StringInputStream("test"));
		String encrypted = Base64.getEncoder().encodeToString(toByteArray(in));
		System.out.println(encrypted);
		
		InputStream in2 = encryptor.decrypt(secret, new ByteArrayInputStream(
				Base64.getDecoder().decode(encrypted)));
		String decrypted = new String(toByteArray(in2));
		assertEquals("test", decrypted);
	}
	
	@Test
	public void testGetSecretKey() throws Exception {
		assertEquals("AES", new Encryptor().getSecretKey(secret).getAlgorithm());
	}

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[8192];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
}
