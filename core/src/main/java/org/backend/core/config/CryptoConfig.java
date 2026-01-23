//package org.backend.core.config;
//
//import java.util.Base64;
//
//import org.backend.core.util.security.crypto.CryptoUtil;
//import org.backend.core.util.security.crypto.impl.AesGcmEncryptor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//
//import jakarta.annotation.PostConstruct;
//
//@Configuration
//public class CryptoConfig {
//
//	@Value("${crypto.aes.key}")
//    private String secretKey;
//
//    @PostConstruct
//    void init() {
//    	byte[] key = Base64.getDecoder().decode(secretKey);
//        CryptoUtil.initialize(new AesGcmEncryptor(key));
//    }
//
//}
