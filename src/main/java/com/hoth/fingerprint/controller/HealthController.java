package com.hoth.fingerprint.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

	private static Logger log = LogManager.getLogger(HealthController.class);
	
	@CrossOrigin("*")
	@GetMapping("/is-alive")
	public ResponseEntity<String> validate() {
		ResponseEntity<String> response = null;
		log.info("Verificando respuesta health / is-alive");
		response = new ResponseEntity<String>("", HttpStatus.OK);
		return response;
	}
}
