package org.backend.billingbatch; // 패키지명 확인

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@SpringBootApplication
public class BillingBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingBatchApplication.class, args);
    }

}