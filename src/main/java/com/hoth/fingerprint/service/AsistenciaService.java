/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.service;

import com.hoth.fingerprint.component.SGPProperties;
import com.hoth.fingerprint.model.request.SGPAsistenciaRequest;
import com.hoth.fingerprint.model.response.SGPAsistenciaResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static Logger log = LogManager.getLogger(AsistenciaService.class);
    private RestTemplate restTemplate;

    public AsistenciaService() 
    {
        this.restTemplate = new RestTemplate();
    }
    
    public List<SGPAsistenciaResponse> enviarListaAsistencia(List<SGPAsistenciaRequest> asistenciaRequest) throws IOException
    {
        SGPProperties propiedadesSGP = new SGPProperties();
        log.debug("Entrando a la petición de enviar la asistencia a SGP");
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime.withZoneSameLocal(ZoneId.of("UTC-6"));
        String urlCompleta = propiedadesSGP.getUrl() + "/sgp-api/fp-client/empleado";
        HttpHeaders headers = createHeaders(propiedadesSGP.getAppUser(), propiedadesSGP.getAppPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setDate(zonedDateTime);
        log.info("Construyendo la peticion de asistencia a SGP...");
        HttpEntity<List<SGPAsistenciaRequest>> entity = new HttpEntity<>(asistenciaRequest, headers);
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<List<SGPAsistenciaResponse>>() {};
        log.debug("Haciendo la petición de envío de asistencias a SGP");
        ResponseEntity<List<SGPAsistenciaResponse>> response = restTemplate.exchange(urlCompleta, HttpMethod.POST, entity, typeReference);
        List<SGPAsistenciaResponse> listaAsistenciaResponse = response.getBody();
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
