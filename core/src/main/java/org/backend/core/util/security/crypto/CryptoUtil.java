package org.backend.core.util.security.crypto;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CryptoUtil {
	
	private static Encryptor encryptor;

    public static void initialize(Encryptor encryptorImpl) {
        encryptor = encryptorImpl;
    }

    public static String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public static String decrypt(String cipherText) {
        return encryptor.decrypt(cipherText);
    }

}
