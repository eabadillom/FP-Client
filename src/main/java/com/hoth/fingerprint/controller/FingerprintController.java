package com.hoth.fingerprint.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import com.hoth.fingerprint.business.RegistroLocalBL;
import static com.hoth.fingerprint.dao.DAO.close;
import com.hoth.fingerprint.gui.Capture;
import com.hoth.fingerprint.gui.Enrollment;
import com.hoth.fingerprint.model.response.ChallengeResponse;
import com.hoth.fingerprint.model.request.Peticion;
import com.hoth.fingerprint.model.response.BiometricResponse;
import com.hoth.fingerprint.exceptions.FPClientOperationException;
import com.hoth.fingerprint.exceptions.FPClientComunicationException;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/finger")
public class FingerprintController {
	private static Logger log = LogManager.getLogger(FingerprintController.class);
        private RegistroLocalBL registros;
	
	@CrossOrigin("*")
	@PostMapping
	public ResponseEntity<BiometricResponse> validateFingerPrint(@RequestBody Peticion json) throws Exception {
                this.registros = new RegistroLocalBL();
                ResponseEntity<BiometricResponse> response = null;
		String accion = null;		
		BiometricResponse biometric = null;
		ChallengeResponse jsonChallengeResponse = null;

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
				
					log.info("Capturando biometrico...");
					Capture.Run();					
					captura = Capture.getCaptura();
					
					Engine engine = UareUGlobal.GetEngine();
					fmd = engine.CreateFmd(captura.image, Fmd.Format.ANSI_378_2004);
					log.trace("fmd capture: {}", fmd);			
					
					biometrico = fmd.getData();
					b64Biometrico = new String(Base64.getEncoder().encode(biometrico));
					log.trace("Captura de fmd convertida: {}", b64Biometrico);
					log.trace("Status captura {}", captura.quality);
					

					biometric = new BiometricResponse();
					biometric.setName("Captura");
					biometric.setResult(918);
					biometric.setMessage("Huella capturada");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(true);
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
					log.debug("Terminando captura de biometrico.");
					break;
					
				case "Validate":
                                        
                                        String numeroEmpleado = json.getNumeroEmpleado();
                                        
                                        if(numeroEmpleado == null || numeroEmpleado.equals(""))
                                        {
                                            throw new FPClientOperationException("Debe indicar un numero de empleado");
                                        }
                                        
                                        jsonChallengeResponse = connectionChallengeServlet(numeroEmpleado);
                                        
                                        String validateHuella = jsonChallengeResponse.getHuella();
                                        log.trace("validateHuella {}", validateHuella);
					String validateHuella2 = jsonChallengeResponse.getHuella2();
                                        log.trace("validateHuella2 {}", validateHuella2);
                                        String huellaCapturada = json.getCaptura();
                                        log.trace("huellaCapturada {}", huellaCapturada);
                                        
                                        boolean match;
                                        
                                        log.debug("Respuesta del challenge servlet: {}", jsonChallengeResponse);
                                        Integer codigoError = jsonChallengeResponse.getCodigoError();
                                    	
                                        //match = registros.comprobarHuella(jsonChallengeResponse.getHuella(), jsonChallengeResponse.getHuella2(), json.getCaptura()); 
					match = registros.comprobarHuella(validateHuella, validateHuella2, huellaCapturada);
                                        
                                        Connection conn = null;
                                        boolean registroEmpleadoCompletado = registros.registrarEmpleado(conn, numeroEmpleado, jsonChallengeResponse.getHuella(), jsonChallengeResponse.getHuella2());
                                        if(registroEmpleadoCompletado != true)
                                        {
                                            log.info("Empleado registrado exitosamente!!!");
                                        }else
                                        {
                                            log.info("El empleado no se registro!!!");
                                        }
                                        close(conn);
                                        
                                        log.trace("Valor de match .............. . . .: {}",match);

					biometric = new BiometricResponse();
					biometric.setName("Validar");
					biometric.setResult(918);
					biometric.setMessage("Huella validada");
					biometric.setBiometricData1(b64Biometrico);					
					biometric.setVerifyBiometricData(match);
					
					if(match == true){
						biometric.setToken(jsonChallengeResponse.getToken());
                                        }else{
						biometric.setToken(null);
					}	
					response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);
                                        
                                        
					break;
					default:
						log.info("ocurrio un error switch .......");
					break;
			}
			
		}catch(FPClientComunicationException | FPClientOperationException ex){
                    log.error("Error en la conexión...", ex.getMessage());
                    log.error("Guardando empleado en base local...");
                    
                    try 
                    {
                        registros.registrarAsistencia(json.getNumeroEmpleado(), json.getCaptura());
                        
                        biometric = new BiometricResponse();
                        biometric.setLastCodeError(1);
                        biometric.setLastMessageError(ex.getMessage());

                        response = new ResponseEntity<>(biometric, HttpStatus.FORBIDDEN);
                    } catch (FingerPrintException ex1) 
                    {
                        log.info("Problema encontrado en la asistencia!!!", ex1.getMessage());
                        throw new Exception("Hubo algun problema con la base de datos");
                    }
                }catch(Exception ex) {
			log.error("Problema para obtener el biometrico...", ex);
			biometric = new BiometricResponse();
			biometric.setLastCodeError(1);
			biometric.setLastMessageError("Ocurrio un problema con el lector de huella, por favor avisa a tu "
                                + "administrador de sistemas");
			
			response = new ResponseEntity<>(biometric, HttpStatus.FORBIDDEN);
		} 

		return response;
	}
        
        @CrossOrigin("*")
        @DeleteMapping
        public ResponseEntity<BiometricResponse> eliminarEmpleado(@RequestBody Peticion json) {
            ResponseEntity<BiometricResponse> respuesta = null;
            
            log.info("Entrando al metodo DELETE del Controller... {}", json.getNumeroEmpleado());
            
            return respuesta;
        }
        
        
        @CrossOrigin("*")
        @GetMapping(path = "/buscar")
        public ResponseEntity<BiometricResponse> buscarPorNumeroEmpleado(@RequestBody Peticion json) {
            ResponseEntity<BiometricResponse> respuesta = null;
            
            log.info("Entrando al metodo GET....{}", json.getNumeroEmpleado());
            
            return respuesta;
        }
        
	public ChallengeResponse connectionChallengeServlet(String numeroEmp) throws FPClientComunicationException{

		URL url = null;    
		HttpURLConnection con = null;
		String json = null;
		ChallengeResponse jsonR = null;

		//CARGAR PROPIEDADES DE APPPLICATION.PROPERTIES
		Properties properties = null;
		InputStream in = null;
		String urlSGP = null;
		String idFpClient = null;
		String password = null;
                Integer timeout = null;

			try {

				properties = new Properties();
				in = getClass().getResourceAsStream("/application.properties");
				properties.load(in);

				urlSGP = properties.getProperty("sgp.url");
				idFpClient = properties.getProperty("idFpClient");
				password = properties.getProperty("password");
                                timeout = Integer.parseInt(properties.getProperty("sgp.timeout"));

				log.debug("ESTE ES EL NUMERO DE EMPLEADO {}", numeroEmp);

				url = new URL(urlSGP+"/sgp/challenge?idFpClient="+idFpClient+"&numeroEmpleado="+numeroEmp+"&password="+password);
				log.trace("Conectando a {} ...", url);

				con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");                 
				con.setRequestProperty("Accept", "application/json");
				con.setRequestProperty("dataType","json" );				
				con.setReadTimeout(timeout * 1000);
                                con.setDoInput(true);
				con.setDoOutput(true);
				
				StringBuilder sb = new StringBuilder();  
				int HttpResult = con.getResponseCode(); 
				log.trace("HTTP Code de Challenge: {}", HttpResult);
				log.debug("Objeto conexion URL: {}", con);

				if (HttpResult == HttpURLConnection.HTTP_CREATED) {
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
					String line = null;  
					while ((line = br.readLine()) != null) {  
						sb.append(line + "\n");  
					}
					br.close();
					json = sb.toString();
					jsonR = new Gson().fromJson(json, ChallengeResponse.class);
					log.debug("JSON Respuesta: {}", sb.toString());  
				} else {
					log.warn("Respuesta no satisfactoria: {}", con.getResponseMessage());

					BufferedReader br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "utf-8"));
					String line = null;  
					while ((line = br.readLine()) != null) {  
						sb.append(line + "\n");  
					}
					br.close();
					json = sb.toString();
					jsonR = new Gson().fromJson(json, ChallengeResponse.class);
					log.info("JSON Respuesta: {}", sb.toString());
				}  


		}catch(SocketTimeoutException | SocketException ex){
                    log.error("Problema al realizar la conexión...", ex.getMessage());
                    throw new FPClientComunicationException("Hay un problema en la comunicación, se tomara registro de tu asistencia"
                            + " y despues se registrara en el sistema...");
                }catch(Exception e){
                    log.error("Problema al realizar la validación del usuario...", e);
		}

		return jsonR;
	}
        
}
