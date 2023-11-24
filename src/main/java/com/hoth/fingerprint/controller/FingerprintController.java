package com.hoth.fingerprint.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hoth.fingerprint.gui.Enrollment;
import com.hoth.fingerprint.model.response.BiometricResponse;

@RestController
@RequestMapping("/")
public class FingerprintController {
	private static Logger log = LogManager.getLogger(FingerprintController.class);
	
	@GetMapping
	public ResponseEntity<String> readFingerPrint(@RequestParam String accion) {
		ResponseEntity<String> response = null;
		
		Enrollment window = null;
		if(accion == null) {
		}
		
		try {
			window = new Enrollment();
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
		    response = new ResponseEntity<String>("biometric data...", HttpStatus.OK);
		}
		
		return response;
	}
	
	@PostMapping
	public ResponseEntity<BiometricResponse> validateFingerPrint(@RequestBody String json) {
		ResponseEntity<BiometricResponse> response = null;
		String accion = null;
		Enrollment window = null;
		BiometricResponse biometric = null;
		
		try {
			log.info("JSON recibido: {}", json);
			
			if(accion == null) {
			}
			
//			window = new Enrollment();
			
		} catch(Exception ex) {
			log.error("Problema para obtener el biometrico...", ex);
		} finally {
			biometric = new BiometricResponse();
			biometric.setName("verirficarHuella");
			biometric.setResult(918);
			biometric.setMessage("Error en el proceso");
		    biometric.setLastCodeError(8001);
		    biometric.setLastMessageError("Error al verificar el dato biométrico");
		    biometric.setBiometricData1(null);
		    biometric.setBiometricData2(null);
		    biometric.setVerifyBiometricData(true);
			response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
		}
		
		return response;
	}
	
	
}
