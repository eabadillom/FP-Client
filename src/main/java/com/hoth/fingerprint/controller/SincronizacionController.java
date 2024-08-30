/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoth.fingerprint.business.SincronizaAsistenciaBL;
import com.hoth.fingerprint.business.SincronizaEmpleadoBL;
import com.hoth.fingerprint.dao.DAO;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.model.domain.Empleado;
import com.hoth.fingerprint.service.AsistenciaService;
import com.hoth.fingerprint.service.EmpleadoService;
import com.hoth.fingerprint.tools.Conexion;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Alberto
 */
@RestController
@RequestMapping("/sincronizar")
public class SincronizacionController extends DAO
{
    private static Logger log = LogManager.getLogger(SincronizacionController.class);
    SincronizaEmpleadoBL sinEmpleado = new SincronizaEmpleadoBL();
    SincronizaAsistenciaBL sinAsistencia = new SincronizaAsistenciaBL();
    private final EmpleadoService empleadoService;
    private final AsistenciaService asistenciaService;

    public SincronizacionController() 
    {
        this.empleadoService = new EmpleadoService();
        this.asistenciaService = new AsistenciaService();
    }
    
    @GetMapping("/empleado/{numeroEmpleado}")
    public ResponseEntity<Empleado> sincronizarEmpleado(@PathVariable String numeroEmpleado) 
    {
        ResponseEntity<Empleado> response = null;
        Empleado empBuscado = null;
        Connection conn = null;
        try{
            conn = Conexion.dsConexion();
            empBuscado = sinEmpleado.sincronizaEmpleado(conn, empleadoService, numeroEmpleado);
            
            response = new ResponseEntity<Empleado>(empBuscado, HttpStatus.OK);
        }catch(SQLException | ClassNotFoundException ex)
        {
            log.error("Error al guardar la informacion a la base de datos ",ex);
        }
        catch(FingerPrintException ex)
        {
            response = new ResponseEntity<>(empBuscado, HttpStatus.FORBIDDEN);
            log.error("Error al traer la informacion en la sincronizacion ",ex);
        }finally
        {
            close(conn);
        }
        return response;
    }
    
    @GetMapping("/empleado")
    public ResponseEntity<List<Empleado>> sincronizarTodosEmpleados() 
    {
        ResponseEntity<List<Empleado>> response = null;
        List<Empleado> empBuscado = null;
        Connection conn = null;
        try{
            conn = Conexion.dsConexion();
            empBuscado = sinEmpleado.sincronizaTodos(conn, empleadoService);
            
            response = new ResponseEntity<List<Empleado>>(empBuscado, HttpStatus.OK);
        }catch(SQLException | ClassNotFoundException ex)
        {
            log.error("Error al guardar la informacion a la base de datos ",ex);
        }
        catch(FingerPrintException ex)
        {
            response = new ResponseEntity<>(empBuscado, HttpStatus.FORBIDDEN);
            log.error("Error al traer la informacion en la sincronizacion ",ex);
        }finally
        {
            close(conn);
        }
        return response;
    }
    
    @GetMapping("/asistenciaCompleta")
    public ResponseEntity<List<Asistencia>> sincronizarAsistenciaCompleta(@RequestParam String fecha) 
    {
        ResponseEntity<List<Asistencia>> response = null;
        List<Asistencia> asistenciaBuscado = null;
        Connection conn = null;
        try{
            conn = Conexion.dsConexion();
            asistenciaBuscado = sinAsistencia.asistenciaEmpleados(conn, asistenciaService, fecha);
            response = new ResponseEntity<List<Asistencia>>(asistenciaBuscado, HttpStatus.OK);
        }catch(SQLException | ClassNotFoundException | JsonProcessingException ex)
        {
            log.error("Error al guardar la informacion a la base de datos {}",ex);
        }
        catch(FingerPrintException ex)
        {
            response = new ResponseEntity<>(asistenciaBuscado, HttpStatus.FORBIDDEN);
            log.error("Error al traer la informacion en la sincronizacion {}", ex);
        }catch(Exception ex)
        {
            log.error("Hubo algún error {}", ex);
        }finally
        {
            close(conn);
        }
        return response;
    }
    
    @GetMapping("/asistenciaIncompleta")
    public ResponseEntity<List<Asistencia>> sincronizaEmpleados(@RequestParam String fecha) {
        ResponseEntity<List<Asistencia>> response = null;
        List<Asistencia> asistenciaBuscado = null;
        Connection conn = null;
        
        try{
            conn = Conexion.dsConexion();
            sinEmpleado.sincronizaEmpleadoSinHuellas(conn, empleadoService);
            asistenciaBuscado = sinAsistencia.actualizarAsistenciaEmpleados(conn, asistenciaService, fecha);
            response = new ResponseEntity<List<Asistencia>>(asistenciaBuscado, HttpStatus.OK);
        }catch(SQLException | ClassNotFoundException | JsonProcessingException ex)
        {
            log.error("Error al guardar la informacion a la base de datos ",ex);
        }catch(FingerPrintException ex)
        {
            response = new ResponseEntity<>(asistenciaBuscado, HttpStatus.FORBIDDEN);
            log.error("Error al traer la informacion en la sincronizacion ",ex);
        }catch(Exception ex)
        {
            log.error("Hubo algún error {}", ex);
        }finally
        {
            close(conn);
        }
        return response;
    }
    
    /*@InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }*/
    
}