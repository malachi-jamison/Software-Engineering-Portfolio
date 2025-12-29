package com.skyapi.weatherforecast.realtime;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.RealtimeWeather;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class RealtimeWeatherRepositoryTest {
	
	@Autowired
	private RealtimeWeatherRepository repo;
	
	@Test
	public void testUpdate() {
		String locationCode = "Aylin";
		
		RealtimeWeather realtimeWeather = repo.findById(locationCode).get();
		
		realtimeWeather.setTemperature(102);
		realtimeWeather.setHumidity(80);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Super Hotty");
		realtimeWeather.setWindSpeed(8);
		realtimeWeather.setLastUpdated(new Date());
		
		RealtimeWeather updatedRealtimeWeather = repo.save(realtimeWeather);
		
		assertThat(updatedRealtimeWeather.getTemperature()).isEqualTo(102);
	}
	
	@Test
	public void testUpdate2() {
		String locationCode = "NYC_USA";
		
		RealtimeWeather realtimeWeather = repo.findById(locationCode).get();
		
		realtimeWeather.setTemperature(102);
		realtimeWeather.setHumidity(80);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Super Hot");
		realtimeWeather.setWindSpeed(8);
		realtimeWeather.setLastUpdated(new Date());
		
		RealtimeWeather updatedRealtimeWeather = repo.save(realtimeWeather);
		
		assertThat(updatedRealtimeWeather.getTemperature()).isEqualTo(102);
	}
	
	
	@Test
	public void testFindByCountryCodeAndCityNotFound() {
		String countryCode = "JP";
		String cityName = "Tokyo";
		
		RealtimeWeather realtimeWeather = repo.findByCountryCodeAndCity(countryCode, cityName);
		
		assertThat(realtimeWeather).isNull();
	}
	
	@Test
	public void testFindByCountryCodeAndCityFound() {
		String countryCode = "UM";
		String cityName = "Banu City";
		
		RealtimeWeather realtimeWeather = repo.findByCountryCodeAndCity(countryCode, cityName);
		
		assertThat(realtimeWeather).isNotNull();
		assertThat(realtimeWeather.getLocation().getCityName()).isEqualTo(cityName);
		assertThat(realtimeWeather.getLocation().getCountryName()).isEqualTo("United States of Malachi");

	}
	
	@Test
	public void testFindByLocationNotFound() {
		
		String locationCode = "ABCXYZ";
		
		RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);
		
		assertThat(realtimeWeather).isNull();
		
		
	}
	
	@Test
	public void testFindByTrashedLocationNotFound() {
		
		String locationCode = "Malachi";
		
		RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);
		
		assertThat(realtimeWeather).isNull();
		
		
	}
	
	@Test
	public void testFindByLocationFound() {
		
		String locationCode = "NYC_USA";
		
		RealtimeWeather realtimeWeather = repo.findByLocationCode(locationCode);
		
		
		assertThat(realtimeWeather).isNotNull();
		assertThat(realtimeWeather.getLocationCode()).isEqualTo(locationCode);

;
		
		
		
	}
	

}
