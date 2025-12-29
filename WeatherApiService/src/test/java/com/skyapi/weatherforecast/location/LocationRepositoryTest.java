package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class LocationRepositoryTest {

	@Autowired
	private LocationRepository repository;
	
	@Autowired
    private TestEntityManager entityManager; 
	
	@Test
	public void testAddSuccess() {
		Location location = new Location();
		
		location.setCode("DAL_USA");
		location.setCityName("Dallas");
		location.setRegionName("Texas");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		location.setEnabled(true);
		
		Location savedLocation = repository.save(location);
		
		assertThat(savedLocation).isNotNull();
		assertThat(savedLocation.getCode()).isEqualTo("DAL_USA");
		
	}
	
	@Test
	@Disabled
	public void testListSuccess() {
		List<Location> locations = repository.findUntrashed();
		
		assertThat(locations).isNotEmpty();
		locations.forEach(System.out::println);
	}
	
	@Test
	public void testListFirstPage() {
		int pageSize = 5;
		int pageNum = 0;
		
		Pageable pageable = PageRequest.of(pageNum, pageSize);
		Page<Location> page = repository.findUntrashed(pageable);
		
		assertThat(page).size().isEqualTo(pageSize);
		
		page.forEach(System.out::println);
		
	}
	
	@Test
	public void testListPageNoContent() {
		int pageSize = 5;
		int pageNum = 10;
		
		Pageable pageable = PageRequest.of(pageNum, pageSize);
		Page<Location> page = repository.findUntrashed(pageable);
		
		assertThat(page).isEmpty();
		
		page.forEach(System.out::println);
		
	}
	
	@Test
	public void testList2ndPageWithSort() {
		int pageSize = 5;
		int pageNum = 0;
		
		Sort sort = Sort.by("code").descending();
		
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
		Page<Location> page = repository.findUntrashed(pageable);
		
		assertThat(page).size().isEqualTo(pageSize);
		
		page.forEach(System.out::println);
		
	}

	@Test
	public void testGetNotFound() {
		
		String code = "abc";
		Location location = repository.findByCode(code);
		
//		assertThat(location).isNotNull();

		assertThat(location).isNull();

		
	}
	
	@Test
	public void testGetFound() {
		
		String code = "Malachi";
		Location location = repository.findByCode(code);
	
		assertThat(location).isNotNull();

		
		assertThat(location.getCode()).isEqualTo(code);

		
	}
	
	@Test
	public void testTrashSuccess() {
		
		String code = "Malachi";
		
		repository.trashedByCode(code);
		
		Location location = repository.findByCode(code);
		
		assertThat(location).isNull();
		
		
	}
	
	
	/* Test will not work due to an  unexpected roll back exception being thrown in the transaction. Due to the realtimeWeather being null 
	Changed the version of spring-boot-starter-parent to 3.3.5 just like your project, 
	and i also have to change the spring-boot-starter-validation to 3.3.5 ,
	Test still didnt work, skipped Lesson 75 */
	@Test
	@Transactional
	public void testAddRealtimeWeatherData() {
		
		String code = "JR_USA";
		
		Location location = repository.findByCode(code);
		
		RealtimeWeather realtimeWeather = location.getRealtimeWeather();
		
		if(realtimeWeather == null) {
		realtimeWeather = new RealtimeWeather();
		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);	
		}
		
		realtimeWeather.setTemperature(101);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Super Hot");
		realtimeWeather.setWindSpeed(8);
		realtimeWeather.setLastUpdated(new Date());
		
		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo(code);
		
	}
	
	//WORKED
	@Test
	@Transactional
	public void testAddRealtimeWeatherDatav3() {
		
		String code = "JR_USA";
		
		Location location = repository.findByCode(code);
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();

		if(location.getRealtimeWeather() == null) {
			realtimeWeather.setLocation(location);
			location.setRealtimeWeather(realtimeWeather);	
			}
		
		entityManager.persist(location); // Save the location
	    entityManager.flush(); // Ensure location is saved
		

		realtimeWeather.setLocation(location);
		

		
		realtimeWeather.setTemperature(-1);
		realtimeWeather.setHumidity(20);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Snowy");
		realtimeWeather.setWindSpeed(6);
		realtimeWeather.setLastUpdated(new Date());
		
		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo(code);
		
	}
	
	@Test
	@Transactional
	public void testAddRealtimeWeatherDatav4() {
		
		String code = "NYC_USA";
		
		Location location = repository.findByCode(code);
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();

		if(location.getRealtimeWeather() == null) {
			realtimeWeather.setLocation(location);
			location.setRealtimeWeather(realtimeWeather);	
			}
		
		entityManager.persist(location); // Save the location
	    entityManager.flush(); // Ensure location is saved
		

		realtimeWeather.setLocation(location);
		

		
		realtimeWeather.setTemperature(-15);
		realtimeWeather.setHumidity(10);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Blizzard");
		realtimeWeather.setWindSpeed(22);
		realtimeWeather.setLastUpdated(new Date());
		
		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo(code);
		
	}
	
	@Test
	@Transactional
	public void addNewLocationAndRealtimeWeather() {
		Location location = new Location();
		
		location.setCode("JR_USA");
		location.setCityName("Cars City");
		location.setRegionName("Baby Land");
		location.setCountryCode("US");
		location.setCountryName("United States of America");
		location.setEnabled(true);
		
//		Location savedLocation = repository.save(location);

		entityManager.persist(location); // Save the location
	    entityManager.flush(); // Ensure location is saved



		
		
		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);
		
		
		realtimeWeather.setTemperature(85);
		realtimeWeather.setHumidity(50);
		realtimeWeather.setPrecipitation(70);
		realtimeWeather.setStatus("Sunny");
		realtimeWeather.setWindSpeed(6);
		realtimeWeather.setLastUpdated(new Date());
		
		
		try {
			Location updatedLocation = repository.save(location);



        assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo("JR_USA");

		  } catch (OptimisticLockException e) {
		        fail("Optimistic locking failed: " + e.getMessage());
		    }
		
		
	}
	
	@Test
	public void testAddHourlyWeatherData() {
		
		Location location = repository.findById("DAL_USA").get();
		
		List<HourlyWeather> listHourlyWeather =  location.getListHourlyWeather();
		
		HourlyWeather forcast1 = new HourlyWeather().id(location, 8)
											.temperature(75)
											.precipitation(33)
											.status("Cloudy");
		
		HourlyWeather forcast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(9)
				.temperature(22)
				.precipitation(22)
				.status("Rainy");
		
		listHourlyWeather.add(forcast1);
		listHourlyWeather.add(forcast2);

		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getListHourlyWeather()).isNotEmpty();
		
		
	}
	
	@Test
	public void testAddHourlyWeatherData2() {
		
		Location location = repository.findById("Baltimore").get();
		
		List<HourlyWeather> listHourlyWeather =  location.getListHourlyWeather();
		
		HourlyWeather forcast1 = new HourlyWeather().id(location, 11)
											.temperature(12)
											.precipitation(13)
											.status("Sunny");
		
		HourlyWeather forcast2 = new HourlyWeather()
				.location(location)
				.hourOfDay(12)
				.temperature(22)
				.precipitation(33)
				.status("Cloudy");
		
		listHourlyWeather.add(forcast1);
		listHourlyWeather.add(forcast2);

		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getListHourlyWeather()).isNotEmpty();
		
		
	}
	
	@Test
	public void testFindByCountryCodeAndCityNotFound() {
		
		String countryCode = "UM";
		
		String cityName = "New Malachi";
		
		Location location = repository.findByCountryCodeAndCityName(countryCode, cityName);
		
		assertThat(location).isNull();
		
	}
	
	
	
	@Test
	public void testFindByCountryCodeAndCityFound() {
		
		String countryCode = "UM";
		
		String cityName = "Banu City";
		
		Location location = repository.findByCountryCodeAndCityName(countryCode, cityName);
		
		assertThat(location).isNotNull();
		assertThat(location.getCountryCode()).isEqualTo(countryCode);
		assertThat(location.getCityName()).isEqualTo(cityName);

	}
	
	@Test
	public void testAddDailyWeatherData() {
		
		Location location = repository.findById("Baltimore").get();
		
		List<DailyWeather> listDailyWeather = location.getListDailyWeather();
		
		DailyWeather forecast1 = new DailyWeather()
				.location(location)
				.dayOfMonth(4)
				.month(6)
				.minTemp(66)
				.maxTemp(75)
				.precipitation(55)
				.status("Cloudy");
		
		DailyWeather forecast2 = new DailyWeather()
				.location(location)
				.dayOfMonth(5)
				.month(6)
				.minTemp(73)
				.maxTemp(83)
				.precipitation(55)
				.status("Sunny");
		
		listDailyWeather.add(forecast1);
		listDailyWeather.add(forecast2);

		Location updatedLocation = repository.save(location);
		
		assertThat(updatedLocation.getListDailyWeather()).isNotEmpty();
		
	}

	
	
}
