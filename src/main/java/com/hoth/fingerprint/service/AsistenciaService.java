/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.service;

import com.hoth.fingerprint.model.request.SGPAsistenciaRequest;
import com.hoth.fingerprint.model.request.SGPEmpleadoRequest;
import com.hoth.fingerprint.model.response.SGPAsistenciaResponse;
import com.hoth.fingerprint.model.response.SGPEmpleadoResponse;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Alberto
 */
@Service
public class AsistenciaService 
{
    private static Logger log = LogManager.getLogger(EmpleadoService.class);
    private RestTemplate restTemplate;
    private String url = "http://192.168.1.15:8080/sgp-api/fp-client";
    RestTemplate configuracion = new RestTemplate();
    private final String usuario = "PLANTA1";
    private final String contrasenia = "abc123@";

    public AsistenciaService() 
    {
        this.restTemplate = new RestTemplate();
    }
    
    public List<SGPAsistenciaResponse> enviarListaAsistencia(List<SGPAsistenciaRequest> asistenciaRequest)
    {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime.withZoneSameLocal(ZoneId.of("UTC-6"));
        String urlCompleta = this.url + "/empleado";
        List<SGPAsistenciaRequest> auxRequest = asistenciaRequest;
        log.info("Aux Request: " + auxRequest.toString());
        HttpHeaders headers = createHeaders(this.usuario, this.contrasenia);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setDate(zonedDateTime);
        //headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<List<SGPAsistenciaRequest>> entity = new HttpEntity<>(auxRequest, headers);
        log.info("Aux Request Converted: " + entity.toString());
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<List<SGPAsistenciaResponse>>() {};
        ResponseEntity<List<SGPAsistenciaResponse>> response = restTemplate.exchange(urlCompleta, HttpMethod.POST, entity, typeReference);
        List<SGPAsistenciaResponse> listaAsistenciaResponse = response.getBody();
        log.info("Aux Request: " + listaAsistenciaResponse.toString());
        return listaAsistenciaResponse;
    }
    
    HttpHeaders createHeaders(String username, String password){
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64( auth.getBytes(Charset.forName("UTF-8")) );
            String authHeader = "Basic " + new String( encodedAuth );
            set( "Authorization", authHeader );
        }};
    }
    
}
