package com.skyapi.weatherforecast.hourly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class HourlyWeatherRepositoryTest {

	
	@Autowired 
	private HourlyWeatherRepository repo;
	
	@Test
	public void testAdd() {
		
		String locationCode = "DAL_USA";
		int hourOfDay = 10;
		
		Location location = new Location().code(locationCode);
		
		HourlyWeather forcast = new HourlyWeather()
				.location(location)
				.hourOfDay(hourOfDay)
				.temperature(11)
				.precipitation(11)
				.status("Cloudy");
	
	
		HourlyWeather updatedForcast = repo.save(forcast);
		
		assertThat(updatedForcast.getId().getLocation().getCode()).isEqualTo(locationCode);	
		assertThat(updatedForcast.getId().getHourOfDay()).isEqualTo(hourOfDay);	

	}
	
	@Test
	public void testDelete() {
		
		Location location = new Location().code("DAL_USA");
		
		HourlyWeatherId id = new HourlyWeatherId(10, location);
		
		repo.deleteById(id);
		
		Optional<HourlyWeather> result = repo.findById(id);
		
		assertThat(result).isNotPresent();
		
	}
	
	@Test
	public void testFindByLocationCodeFound() {
		
		String locationCode = "DAL_USA";
		int currentHour = 7;
		
		List<HourlyWeather> hourlyForcast =  repo.findByLocationCode(locationCode, currentHour);
		
		assertThat(hourlyForcast).isNotEmpty();
	}
	
	@Test
	public void testFindByLocationCodeNotFound() {
		
		// No forecast past 12:00
		String locationCode = "DAL_USA";
		int currentHour = 12;
		
		List<HourlyWeather> hourlyForcast =  repo.findByLocationCode(locationCode, currentHour);
		
		assertThat(hourlyForcast).isEmpty();
	}
	
	
	@Test
	public void testFindByLocationCodeNotFound2() {
		
		// Location is trashed
		String locationCode = "Malachi";
		int currentHour = 12;
		
		List<HourlyWeather> hourlyForcast =  repo.findByLocationCode(locationCode, currentHour);
		
		assertThat(hourlyForcast).isEmpty();
	}
}
