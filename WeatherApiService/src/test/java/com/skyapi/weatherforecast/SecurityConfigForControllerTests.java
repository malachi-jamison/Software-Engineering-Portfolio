package com.skyapi.weatherforecast;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test")
public class SecurityConfigForControllerTests {

	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		
		http.csrf(crsf -> crsf.disable());
		
		return http.build();
		
	}
}
