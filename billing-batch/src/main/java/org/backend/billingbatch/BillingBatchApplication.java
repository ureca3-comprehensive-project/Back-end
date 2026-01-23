package org.backend.billingbatch; // 패키지명 확인

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "org.backend.domain")
@EnableJpaRepositories(basePackages = "org.backend.domain")
public class BillingBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingBatchApplication.class, args);
    }

}