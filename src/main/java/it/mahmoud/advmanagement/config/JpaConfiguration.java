package it.mahmoud.advmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * JPA and Hibernate configuration
 * Sets up the persistence layer
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "it.mahmoud.advmanagement.repo")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfiguration {

    /**
     * Bean for entity auditing (tracking who created/modified entities
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }
}
