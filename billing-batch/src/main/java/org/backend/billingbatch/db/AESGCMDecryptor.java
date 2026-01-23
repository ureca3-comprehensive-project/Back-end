package org.backend.billingbatch.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AESGCMDecryptor {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int NONCE_LENGTH_BYTE = 12;

    @Value("${encrypt.aes.secret-key}")
    private String secretKey;

    public String decrypt(String encryptedBase64) throws Exception {
        // Base64 디코딩
        byte[] decode = Base64.getDecoder().decode(encryptedBase64);
        ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

        // nonce 추출 (앞 12바이트)
        byte[] nonce = new byte[NONCE_LENGTH_BYTE];
        byteBuffer.get(nonce);

        // 나머지 (Ciphertext+Tag) 추출
        byte[] encryptedPayload = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedPayload);

        // 복호화 설정
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        // 복호화 실행
        byte[] decryptedBytes = cipher.doFinal(encryptedPayload);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
