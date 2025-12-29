package com.skyapi.weatherforecast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.clientapp.ClientAppRepository;
import com.skyapi.weatherforecast.common.ClientApp;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class ClientAppRepositoryTests {
	
	@Autowired ClientAppRepository repo;
	
	@Test
	public void testFindByClientIdNotFound() {
		String clientId = "abcde";
		
		
		Optional<ClientApp> result = repo.findByClientId(clientId);
	
		assertThat(result).isNotPresent();
	
	}
	
	@Test
	public void testFindByClientIdFound() {
		String clientId ="6MDD1xlbtWK0vNwMOxSo";
		
		Optional<ClientApp> result = repo.findByClientId(clientId);
		
		assertThat(result).isPresent();
		
	}
	
	

}
