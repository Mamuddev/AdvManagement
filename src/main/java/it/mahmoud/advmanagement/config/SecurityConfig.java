package it.mahmoud.advmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disabilita CSRF per API REST (puoi rimuovere se non necessario)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()  // Consenti accesso alla console H2
                        .requestMatchers("/api/public/**").permitAll()  // Esempio: endpoint pubblici
                        .anyRequest().authenticated()                   // Richiedi autenticazione per altri endpoint
                )
                .headers(headers -> headers.frameOptions().disable())  // Necessario per H2-Console
                .httpBasic(httpBasic -> {});  // Usa autenticazione HTTP Basic

        return http.build();
    }
}
