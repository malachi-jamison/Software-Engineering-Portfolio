package com.skyapi.weatherforecast.hourly;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.SecurityConfigForControllerTests;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;

@WebMvcTest(HourlyWeatherApiController.class)
@Import(SecurityConfigForControllerTests.class)
@ActiveProfiles("test")
public class HourlyWeatherApiControllerTests {

	private static final String X_CURRENT_HOUR = "X-Current-Hour";

	private static final String END_POINT_PATH = "/v1/hourly";
	
	private static final String REQUEST_CONTENT_TYPE = "application/json";
	private static final String REPONSE_CONTENT_TYPE = "application/hal+json";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockBean
	private HourlyWeatherService hourlyWeatherService;
	
	@MockBean
	private GeolocationService locationService;
	
	
	@Test 
	public void testGetByIPShouldReturn400BadRequestBecauseNoHeaderXCurrentHour() throws Exception {
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isBadRequest())
		.andDo(print());
		
		
	}
	
	@Test 
	public void testGetByIPShouldReturn400BadRequestBecauseGeolocationException() throws Exception {
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenThrow(GeolocationException.class);
		
		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, "9"))
		.andExpect(status().isBadRequest())
		.andDo(print());
		
		
	}
	
	@Test 
	public void testGetByIPShouldReturn204NoContent() throws Exception {
		
		int currentHour = 9;
		
		Location location = new Location().code("Malachi");
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);
		when(hourlyWeatherService.getByLocation(location, currentHour)).thenReturn(new ArrayList<>());
		
		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, "9"))
		.andExpect(status().isNoContent())
		.andDo(print());
		
		
	}
	
	@Test 
	public void testGetByIPShouldReturn200Ok() throws Exception {
		
		int currentHour = 9;
		
		Location location = new Location();
		
		location.setCode("NYC_USA");
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		location.setEnabled(true);
		
		HourlyWeather forecast1 = new HourlyWeather()
				.location(location)
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
		
		HourlyWeather forecast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(11)
				.temperature(22)
				.precipitation(22)
				.status("Sunny");
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);
		when(hourlyWeatherService.getByLocation(location, currentHour)).thenReturn(List.of(forecast1, forecast2));
		
		String expectedLocation = location.toString();
		
		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, "9"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
		.andExpect(jsonPath("$.location", is(expectedLocation)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly")))
		.andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime")))
		.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily")))
		.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full")))
		.andDo(print());
		
		
	}
	
	@Test
	public void testGetByCodeShouldReturn400BadRequest() throws Exception{
		
		String locationCode = "Malachi";
		String requestURI = END_POINT_PATH +"/" + locationCode;
		
		mockMvc.perform(get(requestURI))
				.andExpect(status().isBadRequest())
				.andDo(print());
		
	}
	
	
	@Test
	public void testGetByCodeShouldReturn404NotFound() throws Exception{
		
		int currentHour = 9;
		String locationCode = "Malachi";
		String requestURI = END_POINT_PATH +"/" + locationCode;
		
		when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenThrow(LocationNotFoundException.class);
		
		mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
				.andExpect(status().isNotFound())
				.andDo(print());
		
	}
	
	
	@Test
	public void testGetByCodeShouldReturn204NoContent() throws Exception{
		
		int currentHour = 9;
		String locationCode = "Malachi";
		String requestURI = END_POINT_PATH +"/" + locationCode;
		
		when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenReturn(Collections.emptyList());
		
		mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
				.andExpect(status().isNoContent())
				.andDo(print());
		
	}
	
	@Test
	public void testGetByCodeShouldReturn200Ok() throws Exception{
		
		int currentHour = 9;
		String locationCode = "NYC_USA";
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		
		Location location = new Location();
		
		location.setCode(locationCode);
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		location.setEnabled(true);
		
		HourlyWeather forecast1 = new HourlyWeather()
				.location(location)
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
		
		HourlyWeather forecast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(11)
				.temperature(22)
				.precipitation(22)
				.status("Sunny");
		
		var hourlyForecast = List.of(forecast1, forecast2);
		
		Mockito.when(hourlyWeatherService.getByLocationCode(locationCode, currentHour)).thenReturn(hourlyForecast);
		
		
		mockMvc.perform(get(requestURI).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
		.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly/" + locationCode )))
		.andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime/" + locationCode)))
		.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
		.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
		.andDo(print());
		
	}
	
	
	@Test
	public void testUpdateShouldReturn400BadRequestBecauseNoData() throws Exception {
		
		String requestURI = END_POINT_PATH + "/NYC_USA";
		
		List<HourlyWeatherDTO> listDTO = Collections.emptyList();
		
		String requestBody =  objectMapper.writeValueAsString(listDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error[0]", is("Hourly forecast data cannot be empty")))
				.andDo(print());		
		
	}
	
	@Test
	public void testUpdateShouldReturn400BadRequestBecauseInvalidData() throws Exception {
		
		String requestURI = END_POINT_PATH + "/NYC_USA";
				
		HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
				.hourOfDay(1)
				.temperature(1)
				.precipitation(122)
				.status("Hot");
		
		HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
				.hourOfDay(2)
				.temperature(2)
				.precipitation(2)
				.status("Sunny");
		
		List<HourlyWeatherDTO> listDTO  = List.of(dto1, dto2);
				
		String requestBody =  objectMapper.writeValueAsString(listDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("error[0]", containsString("Precipitation must be in the range of 0 to 100 percentage")))
				.andDo(print());		
		
	}
	
	
	@Test
	public void testUpdateShouldReturn404NotFound() throws Exception {
		
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
						
		HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
				.hourOfDay(1)
				.temperature(1)
				.precipitation(22)
				.status("Hot");
	
		
		List<HourlyWeatherDTO> listDTO  = List.of(dto1);
				
		String requestBody =  objectMapper.writeValueAsString(listDTO);
		
		when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList())).thenThrow(LocationNotFoundException.class);


		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isNotFound())
				.andDo(print());		
		
	}

	@Test
	public void testUpdateShouldReturn200Ok() throws Exception {
		
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
						
		HourlyWeatherDTO dto1 = new HourlyWeatherDTO()
				.hourOfDay(10)
				.temperature(1)
				.precipitation(22)
				.status("Hot!!");
	
		HourlyWeatherDTO dto2 = new HourlyWeatherDTO()
				.hourOfDay(11)
				.temperature(2)
				.precipitation(2)
				.status("Sunny!!");
		
		Location location = new Location();
		
		location.setCode(locationCode);
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		location.setEnabled(true);
		
		HourlyWeather forecast1 = new HourlyWeather()
				.location(location)
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
		
		HourlyWeather forecast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(11)
				.temperature(22)
				.precipitation(22)
				.status("Sunny");
		
		List<HourlyWeatherDTO> listDTO  = List.of(dto1, dto2);
		
		
		
		var hourlyForecast = List.of(forecast1, forecast2);

				
		String requestBody =  objectMapper.writeValueAsString(listDTO);
		
		when(hourlyWeatherService.updateByLocationCode(Mockito.eq(locationCode), Mockito.anyList())).thenReturn(hourlyForecast);


		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(location.toString())))
				.andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
				.andExpect(content().contentType(REPONSE_CONTENT_TYPE))
				.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/hourly/" + locationCode )))
				.andExpect(jsonPath("$._links.realtime_weather.href", is("http://localhost/v1/realtime/" + locationCode)))
				.andExpect(jsonPath("$._links.daily_forecast.href", is("http://localhost/v1/daily/" + locationCode)))
				.andExpect(jsonPath("$._links.full_forecast.href", is("http://localhost/v1/full/" + locationCode)))
				.andDo(print());		
		
	}
	
	

	
	
}
