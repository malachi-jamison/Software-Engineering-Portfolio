package com.skyapi.weatherforecast.security;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTests {
	
	private static final String GET_ACCESS_TOKEN_ENDPOINT = "/oauth2/token";
//	private static final String LOCATION_API_ENDPOINT = "/v1/locations";
//	private static final String REALTIME_WEATHER_API_ENDPOINT = "/v1/realtime";
//	private static final String HOURLY_WEATHER_API_ENDPOINT = "/v1/hourly";
//	private static final String DAILY_WEATHER_API_ENDPOINT = "/v1/daily";
//	private static final String FULL_WEATHER_API_ENDPOINT = "/v1/full";
	
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	
	@Test
	public void testGetAccessTokenFail() throws Exception {
		mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
					.param("client_id", "abc")
					.param("client_secret", "def")
					.param("grant_type", "client_credentials")
				)
			.andDo(print())
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error", is("invalid_client")));
		;
	}

	
	@Test
	public void testGetAccessTokenSuccess() throws Exception {
		mockMvc.perform(post(GET_ACCESS_TOKEN_ENDPOINT)
					.param("client_id", "l2StZNA8bzGKdmo6L48F")
					.param("client_secret", "F2J6NAu3RI2ck33K0IhSLqa0PjEa4XvcZjJGxMok")
					.param("grant_type", "client_credentials")
				)
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.access_token").isString())
			.andExpect(jsonPath("$.expires_in").isNumber())
			.andExpect(jsonPath("$.token_type", is("Bearer")));
		;
	}	
}
