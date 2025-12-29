package com.skyapi.weatherforecast.realtime;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.SecurityConfigForControllerTests;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;


@WebMvcTest(RealtimeWeatherApiController.class)
@Import(SecurityConfigForControllerTests.class)
@ActiveProfiles("test")
public class RealtimeWeatherApiControllerTests {

	private static final String END_POINT_PATH = "/v1/realtime";
	private static final String REQUEST_CONTENT_TYPE = "application/json";
	private static final String REPONSE_CONTENT_TYPE = "application/hal+json";
	
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper mapper;
	
	@MockBean RealtimeWeatherService realtimeWeatherService;
	@MockBean GeolocationService locationService;

	
	@Test
	public void testGetShouldReturnStatus400BadRequest() throws Exception {
		
		GeolocationException ex = new GeolocationException();

		
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenThrow(ex);
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isBadRequest())
		.andDo(print());
				
	}
	
	@Test
	public void testGetShouldReturnStatus404NotFound() throws Exception {
		
	
		Location location = new Location();
		
		location.setCountryCode("US");
		location.setCityName("Tampa");
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);

		
		LocationNotFoundException ex = new LocationNotFoundException(location.getCountryCode(), location.getCityName());

		
		Mockito.when(realtimeWeatherService.getByLocation(location)).thenThrow(ex);
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
		.andDo(print());
				
	}
	
	@Test
	public void testGetShouldReturnStatus200OK() throws Exception {
		
		Location location = new Location();
		location.setCode("SFCA_USA");
		location.setCityName("San Franciso");
		location.setRegionName("California");
		location.setCountryName("United States of America");
		location.setCountryCode("US");
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		
		realtimeWeather.setTemperature(22);
		realtimeWeather.setHumidity(40);
		realtimeWeather.setPrecipitation(10);
		realtimeWeather.setStatus("Super Chilly");
		realtimeWeather.setWindSpeed(31);
		realtimeWeather.setLastUpdated(new Date());
		
		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);

		
		Mockito.when(realtimeWeatherService.getByLocation(location)).thenReturn(realtimeWeather);
		
		String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isOk())
		.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
		.andExpect(jsonPath("$.location", is(expectedLocation)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime")))
		.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly")))
		.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
		.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
		.andDo(print());
		
	}
	
	@Test
	public void testByLocationCodeShouldReturnStatus404NotFound() throws Exception {
		
		String locationCode = "ABC_US";
		
		LocationNotFoundException ex = new LocationNotFoundException(locationCode);
		
		Mockito.when(realtimeWeatherService.getByLocationCode(locationCode)).thenThrow(ex);
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		mockMvc.perform(get(requestURI))
			.andExpect(status().isNotFound())		
			.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
			.andDo(print());
		
	}
	
	@Test
	public void testGetByLocationShouldReturnStatus200OK() throws Exception {
		String locationCode ="SFCA_USA";
		
		Location location = new Location();
	
		location.setCode(locationCode);
		location.setCityName("San Franciso");
		location.setRegionName("California");
		location.setCountryName("United States of America");
		location.setCountryCode("US");
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		
		realtimeWeather.setTemperature(22);
		realtimeWeather.setHumidity(40);
		realtimeWeather.setPrecipitation(10);
		realtimeWeather.setStatus("Super Chilly");
		realtimeWeather.setWindSpeed(31);
		realtimeWeather.setLastUpdated(new Date());
		
		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);
		
		Mockito.when(realtimeWeatherService.getByLocationCode(locationCode)).thenReturn(realtimeWeather);
		
		String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
	
		mockMvc.perform(get(requestURI))
		.andExpect(status().isOk())
		.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
		.andExpect(jsonPath("$.location", is(expectedLocation)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
		.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
		.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
		.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
		.andDo(print());
		
	}
	
	@Test
	public void testUpdateShouldReturn400BadRequest2() throws Exception {
		
		String locationCode = "ABC_US";
				
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		
		dto.setTemperature(120);
		dto.setHumidity(400);
		dto.setPrecipitation(106);
		dto.setStatus("Su");
		dto.setWindSpeed(531);
		dto.setLastUpdated(new Date());
		
		String bodyContent = mapper.writeValueAsString(dto);

		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
		.andExpect(status().isBadRequest())
		.andDo(print());
		
		
	}
	
	@Test
	public void testUpdateShouldReturn400BadRequest() throws Exception {
		
		String locationCode = "ABC_US";
				
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		
		dto.setTemperature(120);
		dto.setHumidity(400);
		dto.setPrecipitation(106);
		dto.setStatus("Su");
		dto.setWindSpeed(531);
		dto.setLastUpdated(new Date());
		
		String bodyContent = mapper.writeValueAsString(dto);

		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
		.andExpect(status().isBadRequest())
		.andDo(print());
		
		
	}
	
	@Test
	public void testUpdateShouldReturn404NotFound() throws Exception {
		
		String locationCode = "ABC_US";
				
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		
		dto.setTemperature(10);
		dto.setHumidity(40);
		dto.setPrecipitation(16);
		dto.setStatus("Super Cold");
		dto.setWindSpeed(31);
		dto.setLastUpdated(new Date());
		
		LocationNotFoundException ex = new LocationNotFoundException(locationCode);

		Mockito.when(realtimeWeatherService.update(Mockito.eq(locationCode), Mockito.any())).thenThrow(ex);
		
		String bodyContent = mapper.writeValueAsString(dto);

		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
		.andDo(print());
		
		
	}
	
	@Test
	public void testUpdateShouldReturn200OK() throws Exception {
		
		String locationCode = "SFCA_US";
				
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		
		realtimeWeather.setTemperature(10);
		realtimeWeather.setHumidity(40);
		realtimeWeather.setPrecipitation(16);
		realtimeWeather.setStatus("Super Cold");
		realtimeWeather.setWindSpeed(31);
		realtimeWeather.setLastUpdated(new Date());
		
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		
		dto.setTemperature(10);
		dto.setHumidity(40);
		dto.setPrecipitation(16);
		dto.setStatus("Super Cold");
		dto.setWindSpeed(31);
		dto.setLastUpdated(new Date());
		
		Location location = new Location();
		
		location.setCode(locationCode);
		location.setCityName("San Franciso");
		location.setRegionName("California");
		location.setCountryName("United States of America");
		location.setCountryCode("US");
		
		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);
		
		Mockito.when(realtimeWeatherService.update(locationCode, realtimeWeather)).thenReturn(realtimeWeather);

		
		String bodyContent = mapper.writeValueAsString(dto);

		
		String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();

		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(bodyContent))
		.andExpect(status().isOk())
		.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
		.andExpect(jsonPath("$.location", is(expectedLocation)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/realtime/" + locationCode)))
		.andExpect(jsonPath("$._links.hourly_forecast.href", is("http://localhost/v1/hourly/" + locationCode)))
		.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
		.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
		.andDo(print());
		
		
	}
	
	
	
	
}
