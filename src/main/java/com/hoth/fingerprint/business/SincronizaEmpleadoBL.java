package com.hoth.fingerprint.business;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.hoth.fingerprint.dao.EmpleadoDAO;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Empleado;
import com.hoth.fingerprint.model.response.SGPEmpleadoResponse;
import com.hoth.fingerprint.service.EmpleadoService;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Alberto
 */
@RestController
public class SincronizaEmpleadoBL 
{
    private static Logger log = LogManager.getLogger(SincronizaEmpleadoBL.class);
    private EmpleadoDAO empDAO = null;
    
    public SincronizaEmpleadoBL() 
    {
        this.empDAO = new EmpleadoDAO();
    }
    
    public List<Empleado> sincronizaTodos(Connection conn, EmpleadoService empleadoService) throws SQLException, FingerPrintException, ClassNotFoundException, IOException
    {
        log.debug("Entrando a sincronizar a todos los empleados");
        List<SGPEmpleadoResponse> empleadoResponse = null;
        empleadoResponse = empleadoService.obtenerListaEmpleados();
        List<Empleado> empleados = new ArrayList<Empleado>();
        empDAO.crearTabla(conn);
        
        for(SGPEmpleadoResponse auxEmpleadoResponse : empleadoResponse)
        {
            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setNumeroEmpleado(auxEmpleadoResponse.getNumero());
            nuevoEmpleado.setB1(auxEmpleadoResponse.getBiometrico1());
            nuevoEmpleado.setB2(auxEmpleadoResponse.getBiometrico2());
            empleados.add(nuevoEmpleado);
            Empleado auxNuevo = empDAO.obtenerDato(conn, nuevoEmpleado.getNumeroEmpleado());
            if(auxNuevo == null)
            {
                empDAO.guardarElemento(conn, nuevoEmpleado);
            }else{
                empDAO.actualizarElemento(conn, nuevoEmpleado);
            }
        }
        return empleados;
    }
    
    public Empleado sincronizaEmpleado(Connection conn, EmpleadoService empleadoService, String numeroEmpleado) throws SQLException, FingerPrintException, ClassNotFoundException, IOException
    {
        log.debug("Entrando a sincronizar 1 empleado");
        SGPEmpleadoResponse empleadoResponse;
        empleadoResponse = empleadoService.obtenerEmpleadoPorId(numeroEmpleado);
        empDAO.crearTabla(conn);
        
        Empleado auxEmpleado = empDAO.obtenerDato(conn, numeroEmpleado);
        if(auxEmpleado == null)
        {
            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setNumeroEmpleado(empleadoResponse.getNumero());
            nuevoEmpleado.setB1(empleadoResponse.getBiometrico1());
            nuevoEmpleado.setB2(empleadoResponse.getBiometrico2());
            empDAO.guardarElemento(conn, nuevoEmpleado);
            auxEmpleado = nuevoEmpleado;
        }else
        {
            Empleado empleadoExistente = new Empleado();
            empleadoExistente.setNumeroEmpleado(empleadoResponse.getNumero());
            empleadoExistente.setB1(empleadoResponse.getBiometrico1());
            empleadoExistente.setB2(empleadoResponse.getBiometrico2());
            empDAO.actualizarElemento(conn, empleadoExistente);
            auxEmpleado = empleadoExistente;
        }
        
        return auxEmpleado;
    }
    
    public void sincronizaEmpleadoSinHuellas(Connection conn, EmpleadoService empleadoService) throws SQLException, FingerPrintException, ClassNotFoundException, IOException
    {
        log.debug("Entrando a sincronizar empleado sin huellas");
        List<SGPEmpleadoResponse> empleadoResponse = new ArrayList<SGPEmpleadoResponse>();
        empDAO.crearTabla(conn);
        List<Empleado> auxListaEmpleadoSinHuellas = empDAO.obtenerEmpleadoSinHuella(conn);
        
        for(Empleado aux : auxListaEmpleadoSinHuellas)
        {
            empleadoResponse.add(empleadoService.obtenerEmpleadoPorId(aux.getNumeroEmpleado()));
        }
        
        for(SGPEmpleadoResponse auxEmpleadoResponse : empleadoResponse)
        {
            if(auxEmpleadoResponse.getCodigoError() == 0){
                Empleado nuevoEmpleado = new Empleado();
                nuevoEmpleado.setNumeroEmpleado(auxEmpleadoResponse.getNumero());
                nuevoEmpleado.setB1(auxEmpleadoResponse.getBiometrico1());
                nuevoEmpleado.setB2(auxEmpleadoResponse.getBiometrico2());
                empDAO.actualizarElemento(conn, nuevoEmpleado);
            }else
            {
                log.debug("Empleado no encontrado en SGP: {}", auxEmpleadoResponse);
            }
        }
        
    }
    
}
