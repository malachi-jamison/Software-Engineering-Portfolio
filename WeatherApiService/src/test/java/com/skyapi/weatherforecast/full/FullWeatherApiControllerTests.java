package com.skyapi.weatherforecast.full;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.SecurityConfigForControllerTests;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.full.FullWeatherApiController;
import com.skyapi.weatherforecast.full.FullWeatherDTO;
import com.skyapi.weatherforecast.full.FullWeatherModelAssembler;
import com.skyapi.weatherforecast.full.FullWeatherService;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;

@WebMvcTest(FullWeatherApiController.class)
@Import(SecurityConfigForControllerTests.class)
@ActiveProfiles("test")
public class FullWeatherApiControllerTests {
	
	private static final String END_POINT_PATH = "/v1/full";
	private static final String REQUEST_CONTENT_TYPE = "application/json";
	private static final String REPONSE_CONTENT_TYPE = "application/hal+json";
		
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired private ObjectMapper objectMapper;
	
	@MockBean
	private FullWeatherService weatherService;	
	@MockBean
	private GeolocationService locationService;
	
	@SpyBean private FullWeatherModelAssembler modelAssembler;
	

	@Test 
	public void testGetByIPShouldReturn400BadRequestBecauseGeolocationException() throws Exception {
		
		GeolocationException ex = new GeolocationException("Geolocation error");
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenThrow(ex);
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
		.andDo(print());
		
		
	}
	
	@Test
	public void testGetByIPShouldReturn404NotFound() throws Exception {
		
		Location location = new Location().code("VA_US");
		
		when(locationService.getLocation(Mockito.anyString())).thenReturn(location);
		
		LocationNotFoundException ex = new LocationNotFoundException(location.getCode());
		
		when(weatherService.getByLocation(location)).thenThrow(ex);
		
		mockMvc.perform(get(END_POINT_PATH))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
		.andDo(print());
		
	}
	
	
	@Test 
	public void testGetByIPShouldReturn200Ok() throws Exception {
		Location location = new Location();
		
		location.setCode("NYC_USA");
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		
		realtimeWeather.setTemperature(10);
		realtimeWeather.setHumidity(40);
		realtimeWeather.setPrecipitation(16);
		realtimeWeather.setStatus("Super Cold");
		realtimeWeather.setWindSpeed(31);
		realtimeWeather.setLastUpdated(new Date());
		
		location.setRealtimeWeather(realtimeWeather);
		realtimeWeather.setLocation(location);
		
		DailyWeather dailyForecast1 = new DailyWeather()
				.location(location)
				.dayOfMonth(16)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		DailyWeather dailyForecast2 = new DailyWeather()
				.location(location)
				.dayOfMonth(17)
				.month(7)
				.minTemp(33)
				.maxTemp(44)
				.precipitation(12)
				.status("Sunny");
		
		location.setListDailyWeather(List.of(dailyForecast1, dailyForecast2));
	
		HourlyWeather hourlyForecast1 = new HourlyWeather()
				.location(location)
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
		
		HourlyWeather hourlyForecast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(11)
				.temperature(22)
				.precipitation(22)
				.status("Sunny");
		
		location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));
		
		Mockito.when(locationService.getLocation(Mockito.anyString())).thenReturn(location);
		when(weatherService.getByLocation(location)).thenReturn(location);
		
		
		String expectedLocation = location.toString();
		
		mockMvc.perform(get(END_POINT_PATH))
        .andExpect(status().isOk())
        .andExpect(content().contentType(REPONSE_CONTENT_TYPE))
        .andExpect(jsonPath("$.location", is(expectedLocation))) 
        .andExpect(jsonPath("$.realtime_weather.temperature", is(10)))
		.andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
		.andExpect(jsonPath("$.daily_forecast[0].precipitation", is(40)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full")))
        .andDo(print());
	}
	
	@Test
	public void testGetLocationCodeShouldReturn404NotFound() throws Exception {
		
		String locationCode = "VA_US";
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
			
		LocationNotFoundException ex = new LocationNotFoundException(locationCode);
		
		when(weatherService.get(locationCode)).thenThrow(ex);
		
		
		mockMvc.perform(get(requestURI))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.error[0]", is(ex.getMessage())))
		.andDo(print());
		
	}
	
	@Test 
	public void testGetByLocationCodeReturn200Ok() throws Exception {
		
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" + locationCode;
		
		Location location = new Location();
		
		location.setCode(locationCode);
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		
		realtimeWeather.setTemperature(10);
		realtimeWeather.setHumidity(40);
		realtimeWeather.setPrecipitation(16);
		realtimeWeather.setStatus("Super Cold");
		realtimeWeather.setWindSpeed(31);
		realtimeWeather.setLastUpdated(new Date());
		
		location.setRealtimeWeather(realtimeWeather);
		realtimeWeather.setLocation(location);
		
		DailyWeather dailyForecast1 = new DailyWeather()
				.location(location)
				.dayOfMonth(16)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		DailyWeather dailyForecast2 = new DailyWeather()
				.location(location)
				.dayOfMonth(17)
				.month(7)
				.minTemp(33)
				.maxTemp(44)
				.precipitation(12)
				.status("Sunny");
		
		location.setListDailyWeather(List.of(dailyForecast1, dailyForecast2));
	
		HourlyWeather hourlyForecast1 = new HourlyWeather()
				.location(location)
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
		
		HourlyWeather hourlyForecast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(11)
				.temperature(22)
				.precipitation(22)
				.status("Sunny");
		
		location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));
		
		Mockito.when(weatherService.get(locationCode)).thenReturn(location);
	
		
		String expectedLocation = location.toString();
		
		mockMvc.perform(get(requestURI))
        .andExpect(status().isOk())
        .andExpect(content().contentType(REPONSE_CONTENT_TYPE))
        .andExpect(jsonPath("$.location", is(expectedLocation))) 
        .andExpect(jsonPath("$.realtime_weather.temperature", is(10)))
		.andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
		.andExpect(jsonPath("$.daily_forecast[0].precipitation", is(40)))
		.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full/" + locationCode)))
        .andDo(print());
		
		
	}
	
	@Test 
	public void testUpdateShouldReturn400BadRequestNoHourlyWeather() throws Exception {
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" +  locationCode;
		
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", is("Hourly Weather data cannot be empty")))
		.andDo(print());
		
		
	}
	
	@Test 
	public void testUpdateShouldReturn400BadRequestNoDailyWeather() throws Exception {
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" +  locationCode;
		
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
		HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
		
		fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1); 
		
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", is("Daily Weather data cannot be empty")))
		.andDo(print());
		
		
	}
	
	@Test 
	public void testUpdateShouldReturn400BecauseInvalidRealtimeWeather() throws Exception {
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" +  locationCode;
		
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
		DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
				.dayOfMonth(16)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		fullWeatherDTO.getListDailyWeather().add(dailyForecast1);
		
		HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
		
		fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1); 
		
			
		
		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		
		realtimeWeatherDTO.setTemperature(100000);
		realtimeWeatherDTO.setHumidity(40);
		realtimeWeatherDTO.setPrecipitation(16);
		realtimeWeatherDTO.setStatus("Super Cold");
		realtimeWeatherDTO.setWindSpeed(31);
		realtimeWeatherDTO.setLastUpdated(new Date());

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
			
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", containsString("Temperature must be in the range of -50 to 50 Celsius degree")))
		.andDo(print());
		
		
	}
	
	@Test 
	public void testUpdateShouldReturn400BecauseInvalidHourlyWeatherData() throws Exception {
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" +  locationCode;
		
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
		DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
				.dayOfMonth(16)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		fullWeatherDTO.getListDailyWeather().add(dailyForecast1);
		
		HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
				.hourOfDay(100)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
		
		fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1); 
		
			
		
		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		
		realtimeWeatherDTO.setTemperature(10);
		realtimeWeatherDTO.setHumidity(40);
		realtimeWeatherDTO.setPrecipitation(16);
		realtimeWeatherDTO.setStatus("Super Cold");
		realtimeWeatherDTO.setWindSpeed(31);
		realtimeWeatherDTO.setLastUpdated(new Date());

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
			
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", containsString("Hour of day must be in the range of 0 to 23")))
		.andDo(print());
		
		
	}
	
	@Test 
	public void testUpdateShouldReturn400BecauseInvalidDailyWeatherData() throws Exception {
		String locationCode = "NYC_USA";
		
		String requestURI = END_POINT_PATH + "/" +  locationCode;
				
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
		DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
				.dayOfMonth(44)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		fullWeatherDTO.getListDailyWeather().add(dailyForecast1);
		
		HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
		
		fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1); 
		
			
		
		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		
		realtimeWeatherDTO.setTemperature(10);
		realtimeWeatherDTO.setHumidity(40);
		realtimeWeatherDTO.setPrecipitation(16);
		realtimeWeatherDTO.setStatus("Super Cold");
		realtimeWeatherDTO.setWindSpeed(31);
		realtimeWeatherDTO.setLastUpdated(new Date());

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
			
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.error[0]", containsString("Day of month must be between 1-31")))
		.andDo(print());
		
		
	}
	
	@Test
	public void testUpdateShouldReturn404NotFound() throws Exception {
		String locationCode = "NYC_USA";
		String requestURI = END_POINT_PATH + "/" + locationCode; 
		
		Location locationInRequest = new Location();
		locationInRequest.setCode(locationCode);
		
		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		fullWeatherDTO.setLocation(locationCode);
		
		DailyWeatherDTO dailyForecast1 = new DailyWeatherDTO()
				.dayOfMonth(1)
				.month(7)
				.minTemp(22)
				.maxTemp(33)
				.precipitation(40)
				.status("Cloudy");
		
		fullWeatherDTO.getListDailyWeather().add(dailyForecast1);
		
		HourlyWeatherDTO hourlyForecast1 = new HourlyWeatherDTO()
				.hourOfDay(10)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
		
		fullWeatherDTO.getListHourlyWeather().add(hourlyForecast1); 
		
		
		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		
		realtimeWeatherDTO.setTemperature(10);
		realtimeWeatherDTO.setHumidity(40);
		realtimeWeatherDTO.setPrecipitation(16);
		realtimeWeatherDTO.setStatus("Super Cold");
		realtimeWeatherDTO.setWindSpeed(31);
		realtimeWeatherDTO.setLastUpdated(new Date());

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		

		
		String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
		
		LocationNotFoundException ex = new LocationNotFoundException(locationCode);
		when(weatherService.update(Mockito.eq(locationCode), Mockito.any())).thenThrow(ex);
		
		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
			.andExpect(status().isNotFound())
			.andDo(print());				
		
	}	
	

	@Test 
	public void testUpdateShouldReturn200Ok() throws Exception {
	String locationCode = "NYC_USA";
	
	String requestURI = END_POINT_PATH + "/" +  locationCode;
	
	
	// Data in Request
	Location location = new Location();
	
	location.setCode(locationCode);
	location.setCityName("New York City");
	location.setRegionName("New York");
	location.setCountryCode("US");
	location.setCountryName("United States of America");
	location.setEnabled(true);
	
	
	RealtimeWeather realtimeWeather = new RealtimeWeather();
	
	realtimeWeather.setTemperature(10);
	realtimeWeather.setHumidity(40);
	realtimeWeather.setPrecipitation(16);
	realtimeWeather.setStatus("Super Cold");
	realtimeWeather.setWindSpeed(31);
	realtimeWeather.setLastUpdated(new Date());
	
	location.setRealtimeWeather(realtimeWeather);
	realtimeWeather.setLocation(location);
	
	DailyWeather dailyForecast1 = new DailyWeather()
			.location(location)
			.dayOfMonth(16)
			.month(7)
			.minTemp(22)
			.maxTemp(33)
			.precipitation(33)
			.status("Cloudy");
	
	DailyWeather dailyForecast2 = new DailyWeather()
			.location(location)
			.dayOfMonth(17)
			.month(7)
			.minTemp(33)
			.maxTemp(44)
			.precipitation(12)
			.status("Sunny");
	
	location.setListDailyWeather(List.of(dailyForecast1, dailyForecast2));

	HourlyWeather hourlyForecast1 = new HourlyWeather()
			.location(location)
			.hourOfDay(10)
			.temperature(11)
			.precipitation(11)
			.status("Cloudy");
	
	HourlyWeather hourlyForecast2 = new HourlyWeather()
			.location(location)
			.hourOfDay(11)
			.temperature(22)
			.precipitation(22)
			.status("Sunny");
	
	location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));
	
	
	
	// Data simulated in the database
	FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();
		
	DailyWeatherDTO dailyForecastDTO = new DailyWeatherDTO()
			.dayOfMonth(2)
			.month(7)
			.minTemp(22)
			.maxTemp(33)
			.precipitation(40)
			.status("Cloudy");
	
	fullWeatherDTO.getListDailyWeather().add(dailyForecastDTO);
	
	HourlyWeatherDTO hourlyForecastDTO = new HourlyWeatherDTO()
			.hourOfDay(10)
			.temperature(11)
			.precipitation(11)
			.status("Cloudy");

	
	fullWeatherDTO.getListHourlyWeather().add(hourlyForecastDTO); 
	
		
	
	RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
	
	realtimeWeatherDTO.setTemperature(10);
	realtimeWeatherDTO.setHumidity(40);
	realtimeWeatherDTO.setPrecipitation(16);
	realtimeWeatherDTO.setStatus("Super Cold");
	realtimeWeatherDTO.setWindSpeed(31);
	realtimeWeatherDTO.setLastUpdated(new Date());

	fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
	
	
	
		
	String requestBody = objectMapper.writeValueAsString(fullWeatherDTO);
	
	when(weatherService.update(Mockito.eq(locationCode), Mockito.any())).thenReturn(location);
	String expectedLocation = location.toString();

	
	mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
	.andExpect(status().isOk())
    .andExpect(jsonPath("$.location", is(expectedLocation))) 
    .andExpect(content().contentType(REPONSE_CONTENT_TYPE))
    .andExpect(jsonPath("$.realtime_weather.temperature", is(10)))
	.andExpect(jsonPath("$.hourly_forecast[0].hour_of_day", is(10)))
	.andExpect(jsonPath("$.daily_forecast[0].precipitation", is(33)))
	.andExpect(jsonPath("$._links.self.href", is("http://localhost/v1/full/" + locationCode)))
	.andDo(print());
	
	
}

}
