package com.hoth.fingerprint.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digitalpersona.uareu.*;
import com.google.gson.Gson;
import com.hoth.fingerprint.gui.Capture;
import com.hoth.fingerprint.gui.Enrollment;
import com.hoth.fingerprint.gui.Verification;
import com.hoth.fingerprint.model.request.JsonRespuesta;
import com.hoth.fingerprint.model.request.Peticion;
import com.hoth.fingerprint.model.response.BiometricResponse;

@RestController
@RequestMapping("/finger")
public class FingerprintController {
	private static Logger log = LogManager.getLogger(FingerprintController.class);	
	
	@CrossOrigin("*")
	@PostMapping
	public ResponseEntity<BiometricResponse> validateFingerPrint(@RequestBody Peticion json) {
		ResponseEntity<BiometricResponse> response = null;
		String accion = null;		
		BiometricResponse biometric = null;				

		// ----- Biometricos ----- 
		Reader.CaptureResult captura= null;
		byte[] biometrico = null;
		String b64Biometrico = null;
		Fmd fmd = null;
		
		try {

			accion = json.getTpAccion();
			
			switch (accion) {
				case "Enrollment":

					log.info("Entre a enrolment .........");
					Enrollment.Run();
					fmd = Enrollment.getFmd();
					log.info("fmd enrollment: {}", fmd);

					biometrico = Enrollment.getBiometrico();
					b64Biometrico = new String(Base64.getEncoder().encode(biometrico));
					log.info("Biometrico: {}", b64Biometrico);
					

					biometric = new BiometricResponse();
					biometric.setName("Enrolamiento");
					biometric.setResult(918);
					biometric.setMessage("Error en el proceso");
					biometric.setLastCodeError(8001);
					biometric.setLastMessageError("Error al verificar el dato biométrico");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					break;						

				case "Capture":
				
				log.info("Entre a capture .......");
					Capture.Run();					
					captura = Capture.getCaptura();
					
					Engine engine = UareUGlobal.GetEngine();
					fmd = engine.CreateFmd(captura.image, Fmd.Format.ANSI_378_2004);
					log.info("fmd capture: {}", fmd);			
					
					biometrico = fmd.getData();
					b64Biometrico = new String(Base64.getEncoder().encode(biometrico));
					log.info("Captura de fmd convertida: {}", b64Biometrico);
					log.info("Status captura {}", captura.quality);
					

					biometric = new BiometricResponse();
					biometric.setName("Captura");
					biometric.setResult(918);
					biometric.setMessage("Huella capturada");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					break;
					
				case "Validate":
				
					log.info("Entre a validate .........");

					
					//arreglo de biometricos
					Fmd[] fmd_s = new Fmd[3];
					Fmd capturaFmd = null;
					//Fmd enrolamientoFmd = null;
					JsonRespuesta jsonValidate = null;
					String validateHuella = null;
					String validateHuella2 = null;
					boolean match;
					Fmd huella = null;
					Fmd huella2 = null;
					String numeroEmpleado = json.getNumeroEmpleado();
					
					jsonValidate = connectionChallengeServlet(numeroEmpleado);


					//log.info("Datos de json servlet {}", jsonValidate.getToken() );

					log.info("entre a validar----");
					log.info("json captura: {}", json.getCaptura());
					
					capturaFmd = decodificar(json.getCaptura());
					log.info("capturaFmd: {}",capturaFmd);					
					
					validateHuella = jsonValidate.getHuella();
					log.info("validateHuella {}",validateHuella);
					validateHuella2 = jsonValidate.getHuella2();
					log.info("validateHuella2 {}",validateHuella2);
					huella = decodificar(validateHuella);
					log.info("huella {}",huella);
					huella2 = decodificar(validateHuella2);
					log.info("huella2 {}",huella2);
					
					fmd_s[0] = capturaFmd;
					fmd_s[1] = huella;
					fmd_s[2] = huella2;
					
					Verification.Run(fmd_s);
					match = Verification.isFinger_M();

					log.info("Valor de match .............. . . .: {}",match);

					biometric = new BiometricResponse();
					biometric.setName("Validar");
					biometric.setResult(918);
					biometric.setMessage("Huella validada");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(match);
					
					if(match == true){
						biometric.setToken(jsonValidate.getToken());
					}else{
						biometric.setToken(null);
					}	
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);

					break;

					default:

						log.info("ocurrio un error switch .......");

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
		log.info("entre al metodo decodificador");
		
		//CReamos el fmd en base a los bytes del string
		
		try {

			log.info("descodificare {}", huella);
			byteHuella = Base64.getDecoder().decode(new String(huella).getBytes("UTF-8"));
			log.info("descodificada la huellla");			
			fmd = UareUGlobal.GetImporter().ImportFmd(byteHuella, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);

		} catch (UareUException e) {			
			e.printStackTrace();
			log.info("error al convertir en fmd");
		} catch (UnsupportedEncodingException e) {
			log.info("Error al convertir en base 64");			
			e.printStackTrace();
		}
		

		log.info("converti los bytes de huella a fmd ");
		log.info("Fmd convertido {}", fmd);				
		
		return fmd;
	}

	public JsonRespuesta connectionChallengeServlet(String numeroEmp){

		URL url = null;    
		HttpURLConnection con = null;
		String json = null;
		JsonRespuesta jsonR = null;

		//CARGAR PROPIEDADES DE APPPLICATION.PROPERTIES
		Properties properties = null;
		InputStream in = null;
		String urlSGP = null;
		String idFpClient = null;
		String password = null;

			try {

				properties = new Properties();
				in = getClass().getResourceAsStream("/application.properties");
				properties.load(in);

				urlSGP = properties.getProperty("sgp.url");
				idFpClient = properties.getProperty("idFpClient");
				password = properties.getProperty("password");

				log.info("ESTE ES EL NUMERO DE EMPLEADO {}", numeroEmp);

				url = new URL(urlSGP+"/sgp/challenge?idFpClient="+idFpClient+"&numeroEmpleado="+numeroEmp+"&password="+password);

				//url = new URL("http://192.168.1.70:8080/sgp/challenge?idFpClient=1&numeroEmpleado=0027&password=asfgert222");


				con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");                 
				con.setRequestProperty("Accept", "application/json");
				con.setRequestProperty("dataType","json" );				
				con.setDoInput(true);
				con.setDoOutput(true);
				log.info(con.getURL());


				StringBuilder sb = new StringBuilder();  
				int HttpResult = con.getResponseCode(); 
				System.out.println("Http........." + HttpResult);

				if (HttpResult == HttpURLConnection.HTTP_CREATED) {
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
					String line = null;  
					while ((line = br.readLine()) != null) {  
						sb.append(line + "\n");  
					}
					br.close();
					json = sb.toString();
					jsonR = new Gson().fromJson(json, JsonRespuesta.class);
					//log.info("Numero de empleado accesos ..... {}", jsonR.getNumEmpleado());
					System.out.println("" + sb.toString());  
				} else {
					System.out.println("Respuesta no satisfactoria" + con.getResponseMessage());  
				}  


		}catch(Exception e){
			log.info(e.getMessage());
		}

		return jsonR;
	}
	
}
