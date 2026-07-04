package com.prova.gerador_provas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Sem login implementado ainda: libera tudo por enquanto.
            // Quando você adicionar autenticação de verdade, troque
            // .anyRequest().permitAll() por regras específicas
            // (ex: .requestMatchers("/api/**").authenticated()).
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // Desliga CSRF: seu frontend é um SPA-ish consumindo /api/**
            // via fetch/JSON, sem token CSRF configurado. Se no futuro
            // você tiver formulários HTML tradicionais fazendo POST,
            // considere reabilitar CSRF só para essas rotas específicas.
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
