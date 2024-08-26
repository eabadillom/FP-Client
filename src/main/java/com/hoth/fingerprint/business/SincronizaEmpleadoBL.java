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
    
    public List<Empleado> sincronizaTodos(Connection conn, EmpleadoService empleadoService) throws SQLException, FingerPrintException, ClassNotFoundException
    {
        List<SGPEmpleadoResponse> empleadoResponse = null;
        empleadoResponse = empleadoService.obtenerListaEmpleados();
        List<Empleado> empleados = new ArrayList<Empleado>();
        empDAO.crearTabla(conn);
        empDAO.borrarTabla(conn);
        
        for(SGPEmpleadoResponse auxEmpleadoResponse : empleadoResponse)
        {
            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setNumeroEmpleado(auxEmpleadoResponse.getNumero());
            nuevoEmpleado.setB1(auxEmpleadoResponse.getBiometrico1());
            nuevoEmpleado.setB2(auxEmpleadoResponse.getBiometrico2());
            empleados.add(nuevoEmpleado);
            empDAO.guardarElemento(conn, nuevoEmpleado);
        }
        return empleados;
    }
    
    public Empleado sincronizaEmpleado(Connection conn, EmpleadoService empleadoService, String numeroEmpleado) throws SQLException, FingerPrintException, ClassNotFoundException
    {
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
    
}
