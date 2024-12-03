/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoth.fingerprint.model.response.SGPEmpleadoResponse;
//import com.hoth.fingerprint.tools.RestTemplateConfig;
import java.nio.charset.Charset;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoth.fingerprint.component.SGPProperties;
import com.hoth.fingerprint.exceptions.FPClientComunicationException;
import java.io.IOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Alberto
 */
@Service
@Component
public class EmpleadoService 
{
    private static Logger log = LogManager.getLogger(EmpleadoService.class);
    private RestTemplate restTemplate;

    public EmpleadoService()
    {
        this.restTemplate = new RestTemplate();
    }
    
    public SGPEmpleadoResponse obtenerEmpleadoPorId(String numeroEmpleado) throws FPClientComunicationException, IOException
    {
        SGPProperties propiedadesSGP = new SGPProperties();
        log.debug("Entrando a la petición de obtener empleado por id a SGP");
        String urlCompleta = propiedadesSGP.getUrl() + "/sgp-api/fp-client/empleado/" + numeroEmpleado;
        HttpEntity entity = null;
        SGPEmpleadoResponse empleadoResponse = null;
        ResponseEntity<SGPEmpleadoResponse> response = null;
        try{
            entity = new HttpEntity<String>(createHeaders(propiedadesSGP.getAppUser(), propiedadesSGP.getAppPassword()));
            response = restTemplate.exchange(urlCompleta, HttpMethod.GET, entity, SGPEmpleadoResponse.class);
            empleadoResponse = response.getBody();
            log.info("Recibiendo información de 1 empleado de SGP");
        }catch(HttpClientErrorException.NotFound ex)
        {
            String error = ex.getResponseBodyAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            try
            {
                SGPEmpleadoResponse errorResponse = objectMapper.readValue(error, SGPEmpleadoResponse.class);
                log.error("Empleado no encontrado: {}", errorResponse.getNumero());
                return errorResponse;
            }catch(JsonProcessingException jsonException)
            {
                log.error("Empleado no encontrado: {}", jsonException);
            }
        }catch(ResourceAccessException ex)
        {
            throw new FPClientComunicationException("Hay un problema con la comunicación");
        }
        return empleadoResponse;
    }
    
    public List<SGPEmpleadoResponse> obtenerListaEmpleados() throws IOException
    {
        SGPProperties propiedadesSGP = new SGPProperties();
        log.debug("Entrando a la petición de obtener empleado por id a SGP");
        String urlCompleta = propiedadesSGP.getUrl() + "/sgp-api/fp-client/empleado";
        log.info("Construyendo la peticion de empleado a SGP...");
        HttpEntity entity = new HttpEntity<String>(createHeaders(propiedadesSGP.getAppUser(), propiedadesSGP.getAppPassword()));
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<List<SGPEmpleadoResponse>>() {};
        log.debug("Haciendo la peticion de datos de empleados a SGP");
        ResponseEntity<List<SGPEmpleadoResponse>> response = restTemplate.exchange(urlCompleta, HttpMethod.GET, entity, typeReference);
        List<SGPEmpleadoResponse> listaEmpleadoResponse;
        
        //listaEmpleadoResponse = Arrays.asList(response.getBody());
        listaEmpleadoResponse = response.getBody();
        log.info("Recibiendo información de la lista de empleado de SGP");
        return listaEmpleadoResponse;
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
