package com.hoth.fingerprint.controller;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

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

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Reader.CaptureQuality;
import com.digitalpersona.uareu.dpfj.FmdImpl;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.hoth.fingerprint.gui.Capture;
import com.hoth.fingerprint.gui.Enrollment;
import com.hoth.fingerprint.gui.Verification;
import com.hoth.fingerprint.gui.Enrollment.EnrollmentThread.EnrollmentEvent;
import com.hoth.fingerprint.model.request.Peticion;
import com.hoth.fingerprint.model.response.BiometricResponse;

@RestController
@RequestMapping("/finger")
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
	public ResponseEntity<BiometricResponse> validateFingerPrint(@RequestBody Peticion json) {
		ResponseEntity<BiometricResponse> response = null;
		String accion = null;		
		BiometricResponse biometric = null;				

		// ----- Biometricos ----- 
		Reader.CaptureResult captura ;
		byte[] biometrico = null;
		String b64Biometrico = null;
		String bioEnrolamiento = null;
		String bioCaptura = null;
		Fmd fmd = null;
		
		try {

			log.info("json finger: {}", json.toString());

			log.info("JSON recibido: {}", json.getTpAccion());
			
			accion = json.getTpAccion();

			log.info("Accion de lector: {} ", accion);
			
			

			switch (accion) {
				case "Enrollment":
					//window = new Enrollment();
					Enrollment.Run();
					fmd = Enrollment.getFmd();
					log.info("fmd enrollment: {}", fmd);
					biometrico = Enrollment.getBiometrico();
					b64Biometrico = new String(Base64.getEncoder().encode(biometrico));
					log.info("Biometrico: {}", b64Biometrico);

					

					biometric = new BiometricResponse();
					biometric.setName("Enrollamiento");
					biometric.setResult(918);
					biometric.setMessage("Error en el proceso");
					biometric.setLastCodeError(8001);
					biometric.setLastMessageError("Error al verificar el dato biométrico");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					break;						

				case "Capture":
				

					Capture.Run();
					
					captura = Capture.getCaptura();
					
					Engine engine = UareUGlobal.GetEngine();
					fmd = engine.CreateFmd(captura.image, Fmd.Format.ANSI_378_2004);
					log.info("fmd capture: {}", fmd);
					//CaptureQuality quality = captura.quality;
					//Fid fid = captura.image;					
					
					biometrico = fmd.getData();
					b64Biometrico = new String(Base64.getEncoder().encode(biometrico));
					log.info("Captura de fmd convertida: {}", b64Biometrico);
					log.info("Status captura {}", captura.quality);
					byte[] bytes = b64Biometrico.getBytes();

					

					biometric = new BiometricResponse();
					biometric.setName("Captura");
					biometric.setResult(918);
					biometric.setMessage("Huella capturada");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					break;
					
				case "Validate":
					
					//arreglo de biometricos
					Fmd[] fmd_s = new Fmd[2];
					Fmd capturaFmd = null;
					Fmd enrolamientoFmd = null;

					log.info("entre a validar----");
					log.info("json captura: {}", json.getCaptura());
					capturaFmd = decodificar(json.getCaptura());
					log.info("capturaFmd: {}",capturaFmd);


					enrolamientoFmd = decodificar(json.getEnrolamiento());
					log.info("enrolamientoFmd: {}",enrolamientoFmd);

					fmd_s[0] = capturaFmd;
					fmd_s[1] = enrolamientoFmd;

					Verification.Run(fmd_s);

					biometric = new BiometricResponse();
					biometric.setName("Validando");
					biometric.setResult(918);
					biometric.setMessage("Huella validada");
					//biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					break;

					default:
					break;
			}
			
		} catch(Exception ex) {
			log.error("Problema para obtener el biometrico...", ex);
		} 

		return response;
	}
	
	public Fmd decodificar(String huella){
		
		Fmd fmd = null;
		byte[] byteHuella = null;

		try{
			log.info("entre al metodo decodificador");
			byteHuella = huella.getBytes();
			//CReamos el fmd en base a los bytes del string 
			fmd = UareUGlobal.GetEngine().CreateFmd(byteHuella, 320, 350, 500, 1, 3407615, Fmd.Format.ANSI_378_2004);
			log.info("Fmd convertido {}", fmd);

			//obtenemos bytes del nuevo fmd
			byte [] byteH = fmd.getData();	
			//Convertimos a base64
			String b64Biometrico2 = new String(Base64.getEncoder().encode(byteH));
			log.info("b64Biometrico2 del decodificador: {}", b64Biometrico2);
		}catch(UareUException e){
			log.info("Error al decodificar huella", e);
		}
		
		
		return fmd;
	}

	/*public Fmd obtenerHuellaFmd(String huella) throws UareUException, UnsupportedEncodingException {
		return UareUGlobal.GetImporter().ImportFmd(huella.getBytes("ISO-8859-1"), Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
	}*/
	
}
