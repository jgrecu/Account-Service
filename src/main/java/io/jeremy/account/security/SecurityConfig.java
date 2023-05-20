package io.jeremy.account.security;

import io.jeremy.account.service.JpaUserDetailsService;
import io.jeremy.account.service.LoggingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JpaUserDetailsService userDetailsService;
    private final LoggingService loggingService;

    public SecurityConfig(JpaUserDetailsService userDetailsService, LoggingService loggingService) {
        this.userDetailsService = userDetailsService;
        this.loggingService = loggingService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic(basic -> basic.authenticationEntryPoint(restAuthenticationEntryPoint()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions().disable())
                .exceptionHandling(exh -> exh.accessDeniedHandler(accessDeniedHandler()))
                .authorizeRequests(authz -> authz
                        .mvcMatchers("/h2-console/**").permitAll()
                        .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .mvcMatchers(HttpMethod.POST, "/api/auth/changepass").authenticated()
                        .mvcMatchers("/api/security/events").hasRole("AUDITOR")
                        .mvcMatchers(HttpMethod.GET, "/api/empl/payment")
                        .hasAnyRole("USER", "ACCOUNTANT")
                        .mvcMatchers(HttpMethod.POST, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .mvcMatchers(HttpMethod.PUT, "/api/acct/payments").hasRole("ACCOUNTANT")
                        .mvcMatchers(HttpMethod.PUT, "/api/admin/user/role").hasRole("ADMINISTRATOR")
                        .mvcMatchers(HttpMethod.DELETE, "/api/admin/user").hasRole("ADMINISTRATOR")
                        .mvcMatchers(HttpMethod.GET, "/api/admin/user/**").hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(loggingService);
    }
}
