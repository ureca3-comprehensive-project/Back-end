package org.backend.billingbatch;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "org.backend.billingbatch.repository")
@EntityScan(basePackages = "org.backend.billingbatch.entity")
public class TestBatchConfig {
}
