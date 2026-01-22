package org.backend.billing;

import org.backend.billing.message.service.InMemoryStores;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.backend")
public class BillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingApplication.class, args);
    }

    @Bean
    public InMemoryStores inMemoryStores() {
        return new InMemoryStores();
    }
}

