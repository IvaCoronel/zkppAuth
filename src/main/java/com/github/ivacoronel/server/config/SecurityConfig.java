package com.github.ivacoronel.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/zkauth/users/**").permitAll()
                .antMatchers(HttpMethod.POST, "/zkauth/diary/**").permitAll()
                .antMatchers(HttpMethod.GET, "/zkauth/diary/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .httpBasic();
    }
}