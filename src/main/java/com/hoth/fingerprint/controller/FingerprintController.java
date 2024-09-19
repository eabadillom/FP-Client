package com.hoth.fingerprint.controller;

import java.util.Base64;

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
import com.hoth.fingerprint.business.RegistroLocalBL;
import com.hoth.fingerprint.component.SGPProperties;
import com.hoth.fingerprint.gui.Capture;
import com.hoth.fingerprint.gui.Enrollment;
import com.hoth.fingerprint.model.request.Peticion;
import com.hoth.fingerprint.model.response.BiometricResponse;
import com.hoth.fingerprint.exceptions.FPClientOperationException;
import com.hoth.fingerprint.exceptions.FPClientComunicationException;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.model.response.SGPEmpleadoResponse;
import com.hoth.fingerprint.service.EmpleadoService;
import com.hoth.fingerprint.tools.DateUtils;
import java.io.IOException;
import java.sql.Connection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.client.ResourceAccessException;

@RestController
@RequestMapping("/finger")
public class FingerprintController {

    private static Logger log = LogManager.getLogger(FingerprintController.class);
    private RegistroLocalBL registros;
    private DateUtils fechas = new DateUtils();

    @CrossOrigin("*")
    @PostMapping
    public ResponseEntity<BiometricResponse> validateFingerPrint(@RequestBody Peticion json) throws Exception {
        this.registros = new RegistroLocalBL();
        SGPProperties propiedadesSGP = new SGPProperties();
        ResponseEntity<BiometricResponse> response = null;
        String accion = null;
        BiometricResponse biometric = null;
        SGPEmpleadoResponse challengeResponse = null;
        
        // ----- Biometricos ----- 
        Reader.CaptureResult captura = null;
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
                    try{
                        log.info("Capturando biometrico...");
                        if(json.getCaptureTimeout() > 0)
                        {
                            Capture.Run(json.getCaptureTimeout());
                        }else
                        {
                            Capture.Run(propiedadesSGP.getCaptureTimeout()*1000);
                        }
                    
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
                    }catch(NullPointerException | UareUException ex)
                    {
                        throw new NullPointerException("Biometrico no capturado...");
                    }
                    break;
                    
                case "Validate":

                    String numeroEmpleado = json.getNumeroEmpleado();
                    
                    if (numeroEmpleado == null || numeroEmpleado.equals("")) {
                        throw new FPClientOperationException("Debe indicar un numero de empleado");
                    }
                    
                    if(numeroEmpleado.length() == propiedadesSGP.getNumeroEmpleado())
                    {
                        challengeResponse = this.connectionChallenge(numeroEmpleado);
                        
                        log.debug("Respuesta del challenge servlet: {}", challengeResponse);
                        String validateHuella = challengeResponse.getBiometrico1();
                        log.trace("validateHuella {}", validateHuella);
                        String validateHuella2 = challengeResponse.getBiometrico2();
                        log.trace("validateHuella2 {}", validateHuella2);
                        String huellaCapturada = json.getCaptura();
                        log.trace("huellaCapturada {}", huellaCapturada);

                        boolean match = registros.comprobarHuella(validateHuella, validateHuella2, huellaCapturada);
                        log.trace("Valor de match .............. . . .: {}", match);
                        if(false == match)
                        {
                            throw new FingerPrintException("Biometricos no coinciden");
                        }
                        
                        Connection conn = null;
                        boolean registroEmpleadoCompletado = registros.registrarEmpleado(conn, numeroEmpleado, challengeResponse.getBiometrico1(), challengeResponse.getBiometrico2());
                        if (registroEmpleadoCompletado == true) {
                            log.debug("Empleado registrado exitosamente!!!");
                        } else {
                            log.debug("Empleado ya registrado!!!");
                        }

                        biometric = new BiometricResponse();
                        biometric.setName("Validar");
                        biometric.setResult(918);
                        biometric.setMessage("Huella validada");
                        biometric.setBiometricData1(b64Biometrico);
                        biometric.setVerifyBiometricData(match);

                        if (match == true) {
                            biometric.setToken(challengeResponse.getToken());
                        } else {
                            biometric.setToken(null);
                        }
                        response = new ResponseEntity<BiometricResponse>(biometric, HttpStatus.OK);

                        break;
                    }
                    else
                    {
                        throw new FingerPrintException("Deben ser 4 digitos");
                    }
                default:
                    log.info("ocurrio un error switch .......");
                    break;
            }

        } catch (FPClientComunicationException | FPClientOperationException ex) {
            log.error("Error en la conexión...", ex.getMessage());
            log.info("Guardando empleado en base local...");

            try {
                String numeroEmpleado = json.getNumeroEmpleado();
                String huellaCaptura = json.getCaptura();
                if(numeroEmpleado.length() == propiedadesSGP.getNumeroEmpleado())
                {
                    Asistencia asistenciaMensaje = registros.registrarAsistencia(numeroEmpleado, huellaCaptura);
                    biometric = new BiometricResponse();
                    biometric.setLastCodeError(1);
                    biometric.setLastMessageError(ex.getMessage());
                    biometric.setNumero(asistenciaMensaje.getNumeroEmpleado());
                    biometric.setHoraEntrada(fechas.DateToOffsetDateTime(asistenciaMensaje.getFechaEntrada()));
                    if (asistenciaMensaje.getFechaSalida() != null) {
                        biometric.setHoraSalida(fechas.DateToOffsetDateTime(asistenciaMensaje.getFechaSalida()));
                    }
                    response = new ResponseEntity<>(biometric, HttpStatus.FORBIDDEN);                       
                }else
                {
                    throw new FingerPrintException("Deben ser 4 digitos");
                }
            } catch (FingerPrintException ex1) {
                log.error("Problema encontrado en la asistencia!!!", ex1.getMessage());
                throw new Exception("Hubo algun problema con la base de datos");
            }
        } catch (NullPointerException | NegativeArraySizeException  ex) {
            log.error("Problema al comparar biometricos... ", ex);
            biometric = new BiometricResponse();
            biometric.setLastCodeError(1);
            biometric.setLastMessageError("Ocurrio un problema con la comparacion de huella, por favor avisa a tu "
                    + "administrador de sistemas");

            response = new ResponseEntity<>(biometric, HttpStatus.FORBIDDEN);
        } catch (FingerPrintException ex) {
            log.error("Problema para obtener el biometrico...", ex);
            biometric = new BiometricResponse();
            biometric.setLastCodeError(1);
            biometric.setLastMessageError("Ocurrio un problema con el lector de huella, por favor avisa a tu "
                    + "administrador de sistemas");

            response = new ResponseEntity<>(biometric, HttpStatus.FORBIDDEN);
        }

        return response;
    }

    public SGPEmpleadoResponse connectionChallenge(String numeroEmpleado) throws FPClientComunicationException
    {
        EmpleadoService empleadoService = new EmpleadoService();
        SGPEmpleadoResponse empleadoResponse = null;
        log.info("Iniciando sincronizacion de un empleado con numero de empleado a SGP...");
        try{
            empleadoResponse = empleadoService.obtenerEmpleadoPorId(numeroEmpleado);
        }catch (ResourceAccessException ex) {
            log.error("Problema al realizar la conexión...", ex.getMessage());
            throw new FPClientComunicationException(", se tomara registro de tu asistencia"
                    + " y despues se registrara en el sistema...");
        }catch(IOException ex)
        {
            log.error("Error al traer la informacion en la sincronizacion ",ex);
        }
        return empleadoResponse;
    }
    
}
