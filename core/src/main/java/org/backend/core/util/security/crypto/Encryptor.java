package org.backend.core.util.security.crypto;

public interface Encryptor {
	
	String encrypt(String plainText);
    String decrypt(String cipherText);

}
