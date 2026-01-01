package com.dmc.lplates;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@SpringBootApplication
public class LplatesApplication {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public static void main(String[] args) {
		SpringApplication.run(LplatesApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void loadUsers() throws JsonParseException, JsonMappingException, IOException {
		ClassPathResource resource = new ClassPathResource("users.json");
//		if (this.userRepository.count() == 0 && resource.exists()) {
//			log.debug("Loading users from json file ...");
//			userService.loadUsersFromFile(resource.getInputStream());
//		}
	}
}
