package com.metr.challenge.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

// project-wide default timeout so any @Transactional method without its own `timeout` is still bounded
@Configuration
public class TransactionConfig {

    private static final int DEFAULT_TRANSACTION_TIMEOUT_SECONDS = 10;

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(entityManagerFactory);
        transactionManager.setDefaultTimeout(DEFAULT_TRANSACTION_TIMEOUT_SECONDS);
        return transactionManager;
    }
}
