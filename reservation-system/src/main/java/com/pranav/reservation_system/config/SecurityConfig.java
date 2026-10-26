
package com.pranav.reservation_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");

            if (role == null || !List.of("USER", "ADMIN").contains(role)) {
                return List.of();
            }

            return List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/resources",
                                "/api/resources/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/resources",
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/resources/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/api/reservations/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173"
        ));

        String frontendUrl = System.getenv("FRONTEND_URL");

        if (frontendUrl != null && !frontendUrl.isBlank()) {
            configuration.addAllowedOrigin(
                    frontendUrl.replaceAll("/+$", "")
            );
        }

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
