/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.hoth.fingerprint.interfaces;

import com.hoth.fingerprint.model.response.SGPEmpleadoResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Alberto
 */
@FeignClient(name = "fingerprint-client", url = "http://192.168.1.15:8080/sgp-api/fp-client")
public interface IAPIEmpleado 
{
    /*@RequestMapping(method = RequestMethod.GET, value = "/Empleado")
    List<Empleado> getPosts();*/
    
    @RequestMapping(method = RequestMethod.POST, value = "/empleado/{numeroEmpleado}")
    //@GetMapping("/empleado/{numeroEmpleado}");
    SGPEmpleadoResponse obtenerEmpleadoPorNumero(@PathVariable("numeroEmpleado") String numeroEmpleado);
    
}
