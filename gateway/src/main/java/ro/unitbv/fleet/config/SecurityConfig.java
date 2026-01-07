package ro.unitbv.fleet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${fleet.security.jwtSecret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("Security Config initialized with JWT Secret loaded: " + (jwtSecret != null ? "YES (Length: " + jwtSecret.length() + ")" : "NO"));
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.requestMatchers(
                "/api/**", 
                "/ws/**", 
                "/swagger-ui/**", 
                "v3/api-docs/**",
                "/actuator/**"
            ).permitAll()
            .anyRequest().authenticated());

        return http.build();
    }
}
