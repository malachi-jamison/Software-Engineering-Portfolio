package com.skyapi.weatherforecast.full;


import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.BadRequestException;
import com.skyapi.weatherforecast.CommonUtility;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/v1/full")
public class FullWeatherApiController {
	
	
	private GeolocationService locationService;
	private FullWeatherService weatherService;
	private ModelMapper modelMapper;
	private FullWeatherModelAssembler modelAssembler;
		
	
	public FullWeatherApiController(GeolocationService locationService, FullWeatherService weatherService,
			ModelMapper modelMapper, FullWeatherModelAssembler modelAssembler) {
		super();
		this.locationService = locationService;
		this.weatherService = weatherService;
		this.modelMapper = modelMapper;
		this.modelAssembler = modelAssembler;
	}

	@GetMapping
	public ResponseEntity<?> getFullWeatherByIPAddress(HttpServletRequest request){
		
		String ipAddress = CommonUtility.getIPAddress(request);
		
		Location locationFromIP = locationService.getLocation(ipAddress);
		
		Location locationInDB = weatherService.getByLocation(locationFromIP);
	
		FullWeatherDTO dto = entity2DTO(locationInDB);
		
		return ResponseEntity.ok(modelAssembler.toModel(dto));
	}
	
	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable String locationCode){
		
		Location locationInDB = weatherService.get(locationCode);
		
		FullWeatherDTO dto = entity2DTO(locationInDB);
		
		return ResponseEntity.ok(modelAssembler.addLinksByLocation(dto, locationCode));
		
	}
	
	
	@PutMapping("/{locationCode}")
	public ResponseEntity<?> updateFullWeather (@PathVariable String locationCode,
			@RequestBody @Valid FullWeatherDTO dto) throws BadRequestException{
		
		if (dto.getListHourlyWeather().isEmpty()) {
			throw new BadRequestException("Hourly Weather data cannot be empty");
		}
		
		if (dto.getListDailyWeather().isEmpty()) {
			throw new BadRequestException("Daily Weather data cannot be empty");
		}
		
		Location locationInRequest = dto2Entity(dto);
		
		Location updatedLocation = weatherService.update(locationCode, locationInRequest);
		
		FullWeatherDTO updatedDto = entity2DTO(updatedLocation);
		
		return ResponseEntity.ok(modelAssembler.addLinksByLocation(updatedDto, locationCode));
	}
		


	
	private FullWeatherDTO entity2DTO(Location entity) {
		FullWeatherDTO dto = modelMapper.map(entity, FullWeatherDTO.class);
		
		// do not show the field location in realtime-weather object	
		dto.getRealtimeWeather().setLocation(null);
		
		return dto;
		
	}
	
	private Location dto2Entity(FullWeatherDTO dto) {
		return modelMapper.map(dto, Location.class);
	}
}
