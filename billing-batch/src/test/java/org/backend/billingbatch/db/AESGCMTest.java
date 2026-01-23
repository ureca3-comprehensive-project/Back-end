package org.backend.billingbatch.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AESGCMTest {
    @Autowired
    private AESGCMDecryptor decryptor;

    @Test
    @DisplayName("환경 변수에 등록된 SecretKey를 사용하여 파이썬 암호문을 복호화한다")
    void decryptTest() throws Exception {
        // Python에서 생성한 결과값 예시
        String encryptedFromPython = "oTMppI6K053XrGYBmv+b1mRnP3IYgJhdshNZrNatlsFD/u7yMpaZmemCtjk=";
        String expectedEmail = "test@example.com";

        System.out.println("암호문(Base64): " + encryptedFromPython);

        // 복호화 실행
        String decrypted = decryptor.decrypt(encryptedFromPython);

        System.out.println("복호화 결과: " + decrypted);

        // 간단한 검증
        System.out.println("🔓 복호화 결과: " + decrypted);
        assertThat(decrypted).isEqualTo(expectedEmail);
    }
}
