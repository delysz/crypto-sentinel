package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Para que los formularios funcionen fácil
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/").permitAll() // Público para ver
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll() // Estilos públicos
                        .anyRequest().authenticated() // Privado para modificar (POST/DELETE)
                )
                .formLogin(withDefaults()) // Pantalla de login de Spring
                .logout(logout -> logout.logoutSuccessUrl("/")); // Al salir, vuelve al inicio

        return http.build();
    }
}