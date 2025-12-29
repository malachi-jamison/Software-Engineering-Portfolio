package com.skyapi.weatherforecast.realtime;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class RealtimeWeatherService {
	
	private RealtimeWeatherRepository realtimeWeatherRepo;
	private LocationRepository locationRepo;
	
	
	
    private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeWeatherService.class);

	
	// From AI
    @PersistenceContext
    private EntityManager entityManager;

	public RealtimeWeatherService(RealtimeWeatherRepository realtimeWeatherRepo, LocationRepository locationRepo) {
		super();
		this.realtimeWeatherRepo = realtimeWeatherRepo;
		this.locationRepo = locationRepo;
	}
	
	
	public RealtimeWeather getByLocation(Location location){
		
	String countryCode= location.getCountryCode();
	
	String cityName = location.getCityName();
	
	RealtimeWeather realtimeWeather = realtimeWeatherRepo.findByCountryCodeAndCity(countryCode, cityName);
	
	if (realtimeWeather == null) {
		throw new LocationNotFoundException(countryCode, cityName);
		
	}
	return realtimeWeather;
		
	}
	
	public RealtimeWeather getByLocationCode(String locationCode){
		
		RealtimeWeather realtimeWeather = realtimeWeatherRepo.findByLocationCode(locationCode);
		
		if (realtimeWeather == null) {
			throw new LocationNotFoundException(locationCode);
			
		}
		
		return realtimeWeather;
		
	}
	
	@Transactional
	public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather){

	    Location location = locationRepo.findByCode(locationCode);

	    if (location == null) {
	        throw new LocationNotFoundException(locationCode);
	    }

	    realtimeWeather.setLocation(location);
	    realtimeWeather.setLastUpdated(new Date());

	    if (location.getRealtimeWeather() == null) {
	        // Create a new RealtimeWeather
	        location.setRealtimeWeather(realtimeWeather);
	        entityManager.persist(location); // Persist the location and the realtime weather object.
	        entityManager.flush();

	        return location.getRealtimeWeather();
	    } else {
	        // Update the existing RealtimeWeather
	        RealtimeWeather existingWeather = location.getRealtimeWeather();
	        existingWeather.setTemperature(realtimeWeather.getTemperature());
	        existingWeather.setHumidity(realtimeWeather.getHumidity());
	        existingWeather.setPrecipitation(realtimeWeather.getPrecipitation());
	        existingWeather.setStatus(realtimeWeather.getStatus());
	        existingWeather.setWindSpeed(realtimeWeather.getWindSpeed());
	        existingWeather.setLastUpdated(realtimeWeather.getLastUpdated());

	        entityManager.merge(existingWeather);
	        entityManager.flush();

	        return existingWeather;
	    }
	}

	
//	@Transactional
//	public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather) throws LocationNotFoundException {
//		
//		Location location = locationRepo.findByCode(locationCode);
//		
//		if ( location == null) {
//			throw new LocationNotFoundException("No location found with the given code:" + locationCode);
//			
//		}
//		
//		realtimeWeather.setLocation(location);
//		realtimeWeather.setLastUpdated(new Date());
//		
//		if (location.getRealtimeWeather() == null) {
//			location.setRealtimeWeather(realtimeWeather);
//			Location updatedLocation = locationRepo.save(location);
//			
//			return updatedLocation.getRealtimeWeather();
//		}
//		
////	    realtimeWeather = entityManager.merge(realtimeWeather);
//
//		
//		// From the video
//		 return realtimeWeatherRepo.save(realtimeWeather);
//	}
		
//		//from Claude AI 
//		else {
//	        // Existing weather record - update it by fetching first
//	        RealtimeWeather existingWeather = realtimeWeatherRepo.findById(locationCode).orElse(null);
//	        
//	        if (existingWeather != null) {
//	            // Update properties of the existing entity
//	            existingWeather.setTemperature(realtimeWeather.getTemperature());
//	            existingWeather.setHumidity(realtimeWeather.getHumidity());
//	            existingWeather.setPrecipitation(realtimeWeather.getPrecipitation());
//	            existingWeather.setStatus(realtimeWeather.getStatus());
//	            existingWeather.setWindSpeed(realtimeWeather.getWindSpeed());
//	            existingWeather.setLastUpdated(realtimeWeather.getLastUpdated());
//	            
//	            // Save the existing entity
//	            return realtimeWeatherRepo.save(existingWeather);
//	        } else {
//	            // This shouldn't happen, but just in case
//	            location.setRealtimeWeather(realtimeWeather);
//	            Location updatedLocation = locationRepo.save(location);
//	            return updatedLocation.getRealtimeWeather();
//	        }
//	    }
//		
//		
//	}
	
	
//	// From AI
//	public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather) throws LocationNotFoundException {
//	    Location location = locationRepo.findByCode(locationCode);
//
//	    if (location == null) {
//	        throw new LocationNotFoundException("No location found with the given code:" + locationCode);
//	    }
//
//	    RealtimeWeather existingWeather = null;
//
//	    // First check if we can find an existing weather record directly
//	    try {
//	        existingWeather = realtimeWeatherRepo.findById(locationCode).orElse(null);
//	    } catch (Exception e) {
//	        // Handle possible exceptions when finding by ID
//	        LOGGER.warn("Error finding existing weather for location {}: {}", locationCode, e.getMessage());
//	    }
//
//	    if (existingWeather == null) {
//	        // No existing weather found - create a new one
//	        realtimeWeather.setLocation(location);
//	        realtimeWeather.setLastUpdated(new Date());
//	        location.setRealtimeWeather(realtimeWeather);
//	        
//	        // Refresh the location entity to ensure we have the latest state
//	        locationRepo.flush();
//	        entityManager.refresh(location);
//	        
//	        // Save and return
//	        Location updatedLocation = locationRepo.save(location);
//	        return updatedLocation.getRealtimeWeather();
//	    } else {
//	        // Update existing weather entity
//	        existingWeather.setTemperature(realtimeWeather.getTemperature());
//	        existingWeather.setHumidity(realtimeWeather.getHumidity());
//	        existingWeather.setPrecipitation(realtimeWeather.getPrecipitation());
//	        existingWeather.setStatus(realtimeWeather.getStatus());
//	        existingWeather.setWindSpeed(realtimeWeather.getWindSpeed());
//	        existingWeather.setLastUpdated(new Date());
//	        
//	        // Save the existing entity
//	        return realtimeWeatherRepo.save(existingWeather);
//	    }
//	}
	
	
//	public RealtimeWeather update(String locationCode, RealtimeWeather realtimeWeather) throws LocationNotFoundException {
//	    Location location = locationRepo.findByCode(locationCode);
//
//	    if (location == null) {
//	        throw new LocationNotFoundException("No location found with the given code:" + locationCode);
//	    }
//
//	    // Try to find existing weather directly with EntityManager for more control
//	    RealtimeWeather existingWeather = null;
//	    try {
//	        existingWeather = entityManager.find(RealtimeWeather.class, locationCode);
//	    } catch (Exception e) {
//	        LOGGER.warn("Error finding weather with EntityManager: {}", e.getMessage());
//	    }
//
//	    if (existingWeather == null) {
//	        // Create new weather
//	        realtimeWeather.setLocation(location);
//	        realtimeWeather.setLastUpdated(new Date());
//	        location.setRealtimeWeather(realtimeWeather);
//	        
//	        Location savedLocation = locationRepo.saveAndFlush(location);
//	        entityManager.refresh(savedLocation);
//	        return savedLocation.getRealtimeWeather();
//	    } else {
//	        // Detach the incoming entity to avoid conflicts
//	        if (entityManager.contains(realtimeWeather)) {
//	            entityManager.detach(realtimeWeather);
//	        }
//	        
//	        // Update the attached entity
//	        existingWeather.setTemperature(realtimeWeather.getTemperature());
//	        existingWeather.setHumidity(realtimeWeather.getHumidity());
//	        existingWeather.setPrecipitation(realtimeWeather.getPrecipitation());
//	        existingWeather.setStatus(realtimeWeather.getStatus());
//	        existingWeather.setWindSpeed(realtimeWeather.getWindSpeed());
//	        existingWeather.setLastUpdated(new Date());
//	        
//	        entityManager.flush();
//	        return existingWeather;
//	    }
//	}

}
