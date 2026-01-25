package org.backend.billing;

import org.backend.billing.message.service.InMemoryStores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// 테스트를 위해 EntityScan, EnableJpaRepositories 추가
@EntityScan(basePackages = "org.backend.domain")
@EnableJpaRepositories(basePackages = "org.backend.domain")
@ComponentScan(
        basePackages = {
                "org.backend.billing",       
                "org.backend.domain",
                "org.backend.core",
                "org.backend.billingbatch"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "org\\.backend\\.billingbatch\\.BillingBatchApplication"
        )
)
public class BillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }

    @Bean
    public InMemoryStores inMemoryStores() {
        return new InMemoryStores();
    }
}

