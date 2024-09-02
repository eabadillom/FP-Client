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
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Alberto
 */
@Service
public class EmpleadoService 
{
    private static Logger log = LogManager.getLogger(EmpleadoService.class);
    private RestTemplate restTemplate;
    private String url = "http://192.168.1.15:8080/sgp-api/fp-client";
    RestTemplate configuracion = new RestTemplate();
    private final String usuario = "PLANTA1";
    private final String contrasenia = "abc123@";

    public EmpleadoService() 
    {
        this.restTemplate = new RestTemplate();
    }
    
    public SGPEmpleadoResponse obtenerEmpleadoPorId(String numeroEmpleado)
    {
        String urlCompleta = this.url + "/empleado/" + numeroEmpleado;
        HttpEntity entity = null;
        SGPEmpleadoResponse empleadoResponse = null;
        ResponseEntity<SGPEmpleadoResponse> response = null;
        try{
            entity = new HttpEntity<String>(createHeaders(this.usuario, this.contrasenia));
            response = restTemplate.exchange(urlCompleta, HttpMethod.GET, entity, SGPEmpleadoResponse.class);
            empleadoResponse = response.getBody();
        }catch(HttpClientErrorException.NotFound ex)
        {
            String error = ex.getResponseBodyAsString();
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("Empleado no encontrado: {}", error);
            try
            {
                SGPEmpleadoResponse errorResponse = objectMapper.readValue(error, SGPEmpleadoResponse.class);
                return errorResponse;
            }catch(JsonProcessingException jsonException)
            {
                log.info("Empleado no encontrado: {}", jsonException);
            }
        }
        return empleadoResponse;
    }
    
    public List<SGPEmpleadoResponse> obtenerListaEmpleados()
    {
        String urlCompleta = this.url + "/empleado";
        //ResponseEntity<SincronizacionEmpleadoResponse[]> response = restTemplate.exchange(urlCompleta, HttpMethod.GET, new HttpEntity<String>(createHeaders(this.usuario, this.contrasenia)), SGPEmpleadoResponse[].class);
        HttpEntity entity = new HttpEntity<String>(createHeaders(this.usuario, this.contrasenia));
        ParameterizedTypeReference typeReference = new ParameterizedTypeReference<List<SGPEmpleadoResponse>>() {};
        ResponseEntity<List<SGPEmpleadoResponse>> response = restTemplate.exchange(urlCompleta, HttpMethod.GET, entity, typeReference);
        List<SGPEmpleadoResponse> listaEmpleadoResponse;
        
        //listaEmpleadoResponse = Arrays.asList(response.getBody());
        listaEmpleadoResponse = response.getBody();
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
