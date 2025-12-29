package com.skyapi.weatherforecast;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;

public class IP2LocationTests {
	
	private String DBPath = "ip2locdb/IP2LOCATION-LITE-DB3.BIN";
	
	@Test
	public void testInvalidIP() throws IOException {
		
		IP2Location ipLocator = new IP2Location();
		ipLocator.Open(DBPath);
		
		String ipAddress = "abc";
		IPResult ipResult = ipLocator.IPQuery(ipAddress);
		
		assertThat(ipResult.getStatus()).isEqualTo("INVALID_IP_ADDRESS");
		
		System.out.println(ipResult);
		
	}
	
	@Test
	public void testValidIP1() throws IOException {
		
		IP2Location ipLocator = new IP2Location();
		ipLocator.Open(DBPath);
		
		String ipAddress = "68.5.138.58"; // Cali
		IPResult ipResult = ipLocator.IPQuery(ipAddress);
		
		assertThat(ipResult.getStatus()).isEqualTo("OK");
		
		System.out.println(ipResult);
		
	}
	
	@Test
	public void testValidIP2() throws IOException {
		
		IP2Location ipLocator = new IP2Location();
		ipLocator.Open(DBPath);
		
		String ipAddress = "85.110.75.46"; // Turkey
		IPResult ipResult = ipLocator.IPQuery(ipAddress);
		
		assertThat(ipResult.getStatus()).isEqualTo("OK");
		assertThat(ipResult.getCity()).isEqualTo("Ankara");
		
		System.out.println(ipResult);
		
	}
	
	
	@Test
	public void testValidIP3() throws IOException {
		
		IP2Location ipLocator = new IP2Location();
		ipLocator.Open(DBPath);
		
		String ipAddress = "63.116.61.253"; // New York
		IPResult ipResult = ipLocator.IPQuery(ipAddress);
		
		assertThat(ipResult.getStatus()).isEqualTo("OK");
		
		System.out.println(ipResult);
		
	}
	
	

	@Test
	public void testValidIP4() throws IOException {
		
		IP2Location ipLocator = new IP2Location();
		ipLocator.Open(DBPath);
		
		String ipAddress = "156.146.38.131"; // Dallas
		IPResult ipResult = ipLocator.IPQuery(ipAddress);
		
		assertThat(ipResult.getStatus()).isEqualTo("OK");
		
		System.out.println(ipResult);
		
	}

}
