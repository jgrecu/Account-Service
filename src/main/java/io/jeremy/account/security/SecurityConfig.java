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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JpaUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final LoggingService loggingService;

    public SecurityConfig(JpaUserDetailsService userDetailsService,
                          RestAuthenticationEntryPoint restAuthenticationEntryPoint, LoggingService loggingService) {
        this.userDetailsService = userDetailsService;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
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
        http.httpBasic(basic -> basic.authenticationEntryPoint(restAuthenticationEntryPoint))
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .exceptionHandling(exh -> exh.accessDeniedHandler(accessDeniedHandler()))
                .authorizeRequests(auth -> auth
                        .mvcMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .mvcMatchers("/h2-console/**").permitAll()
                        .mvcMatchers("/api/security/events/**").hasAnyAuthority("ROLE_AUDITOR")
                        .mvcMatchers(HttpMethod.POST, "/api/auth/changepass").hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_ADMINISTRATOR")
                        .mvcMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT")
                        .mvcMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                        .mvcMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                        .mvcMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority("ROLE_ADMINISTRATOR")
                        .mvcMatchers(HttpMethod.DELETE, "/api/admin/user").hasAuthority("ROLE_ADMINISTRATOR")
                        .mvcMatchers(HttpMethod.GET, "/api/admin/user/**").hasAuthority("ROLE_ADMINISTRATOR")
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
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(loggingService);
    }
}
