package com.skyapi.weatherforecast.full;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.AbstractLocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class FullWeatherService extends AbstractLocationService {
	
    @PersistenceContext
	private EntityManager entityManager;
	
	public FullWeatherService(LocationRepository repo) {
		super();
		this.repo = repo;
	}
	
	public Location getByLocation(Location locationFromIP) {
		String cityName = locationFromIP.getCityName();
		String countryCode = locationFromIP.getCountryCode();
		
		Location locationInDB = repo.findByCountryCodeAndCityName(countryCode, cityName);
		
		if(locationInDB == null) {
			throw new LocationNotFoundException(cityName, countryCode);
		}
		
		return locationInDB;
	}
	
	@Transactional
	public Location update(String locationCode, Location locationInRequest) {
		Location locationInDB = repo.findByCode(locationCode);
		
		if(locationInDB == null) {
			throw new LocationNotFoundException(locationCode);
		}
		
		
		setLocationForWeatherData(locationInRequest, locationInDB);
		
		saveRealtimeWeatherIfNotExistBefore(locationInRequest, locationInDB);

		
		locationInRequest.copyAllFieldsFrom(locationInDB);

		return repo.save(locationInRequest);
	}

	@Transactional
	private void saveRealtimeWeatherIfNotExistBefore(Location locationInRequest, Location locationInDB) {
		if(locationInDB.getRealtimeWeather() == null) {
			locationInDB.setRealtimeWeather(locationInRequest.getRealtimeWeather());
			locationInRequest.getRealtimeWeather().setLocation(locationInDB);

			entityManager.persist(locationInDB); // Persist the locationInDB in the database
	        entityManager.flush();

		}
	}

	private void setLocationForWeatherData(Location locationInRequest, Location locationInDB) {
		RealtimeWeather realtimeWeather = locationInRequest.getRealtimeWeather();
		realtimeWeather.setLocation(locationInDB);
		realtimeWeather.setLastUpdated(new Date());
	
		
		List<DailyWeather> listDailyWeather = locationInRequest.getListDailyWeather();
		listDailyWeather.forEach(dw -> dw.getId().setLocation(locationInDB));
		
		List<HourlyWeather> listHourlyWeather = locationInRequest.getListHourlyWeather();
		listHourlyWeather.forEach(hw -> hw.getId().setLocation(locationInDB));
	}


}

