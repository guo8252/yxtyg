package com.jscm.yxtyg.config;

import com.jscm.yxtyg.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/static/**", "/js/**", "/css/**", "/fonts/**", "/favicon.ico").permitAll()
                .antMatchers("/api/auth/login").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user/list").hasRole("SYS_ADMIN")
                .antMatchers(HttpMethod.GET, "/api/user/product-managers").authenticated()
                .antMatchers("/api/user/**").hasRole("SYS_ADMIN")
                .antMatchers("/api/urge/**").hasRole("DEV_ADMIN")
                .antMatchers(HttpMethod.POST, "/api/requirement").hasAnyRole("DEV_ADMIN", "SYS_ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/requirement/*").hasAnyRole("DEV_ADMIN", "SYS_ADMIN", "PRODUCT_MANAGER")
                .antMatchers(HttpMethod.DELETE, "/api/requirement/*").hasAnyRole("DEV_ADMIN", "SYS_ADMIN")
                .antMatchers(HttpMethod.POST, "/api/requirement/*/fill").hasRole("PRODUCT_MANAGER")
                .antMatchers(HttpMethod.POST, "/api/requirement/*/approve").hasAnyRole("DEV_ADMIN", "SYS_ADMIN")
                .antMatchers("/api/requirement/**").authenticated()
                .anyRequest().authenticated();

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
