package com.skyapi.weatherforecast;

import org.springframework.beans.factory.annotation.Autowired;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

public abstract class AbstractLocationService {
	
	@Autowired
	protected LocationRepository repo;
	
	public Location get(String code) {
		Location location = repo.findByCode(code);

		if (location == null) {
			throw new LocationNotFoundException(code);
		}
		return location;

	}

}
