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
 * Sets up the persistence layer with best practices
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "it.mahmoud.advmanagement.repo")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfiguration {

    /**
     * Bean for entity auditing (tracking who created/modified entities)
     * In a real application, this would retrieve the current user from security context
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // This is a simple implementation - in a real app, get the username from security context
        return () -> Optional.of("system");
    }
}
