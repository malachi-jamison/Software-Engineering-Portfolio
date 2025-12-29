package com.skyapi.weatherforecast;


import com.ip2location.IPResult;

public class GeolocationException extends RuntimeException {

	public GeolocationException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeolocationException() {
		super("The Geolocation is incorrect");
	}
	
	public GeolocationException(IPResult result) {
		super("Geolocation failed with status: " + result.getStatus());
	}
	
	public GeolocationException(String message) {
        super(message);
    }
	
	

}
