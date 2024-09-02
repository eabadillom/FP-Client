/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hoth.fingerprint.dao.AsistenciaDAO;
import com.hoth.fingerprint.dao.EmpleadoDAO;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.model.domain.Empleado;
import com.hoth.fingerprint.model.request.SGPAsistenciaRequest;
import com.hoth.fingerprint.model.response.SGPAsistenciaResponse;
import com.hoth.fingerprint.service.AsistenciaService;
import com.hoth.fingerprint.tools.DateUtils;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class SincronizaAsistenciaBL 
{
    private static Logger log = LogManager.getLogger(SincronizaAsistenciaBL.class);
    private AsistenciaDAO asistDAO = null;
    private EmpleadoDAO empDAO = null;
    private RegistroLocalBL registros = null;
    DateUtils fechas = new DateUtils();

    public SincronizaAsistenciaBL() 
    {
        this.empDAO = new EmpleadoDAO();
        this.asistDAO = new AsistenciaDAO();
        this.registros = new RegistroLocalBL();
    }
    
    public List<Asistencia> asistenciaEmpleados(Connection conn, AsistenciaService asistenciaService, String fecha)  throws Exception
    {
        List<SGPAsistenciaRequest> asistenciaRequest = new ArrayList<SGPAsistenciaRequest>();
        List<Asistencia> listarAsistencia = new ArrayList<Asistencia>();
        asistDAO.crearTabla(conn);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = fechas.LocalDate(formatter.parse(fecha));
        listarAsistencia = asistDAO.obtenerElementosRegistrados(conn, date);
        for(Asistencia aux : listarAsistencia)
        {
            SGPAsistenciaRequest auxRequest = new SGPAsistenciaRequest();
            auxRequest.setUUId(aux.getIdAsistencia());
            auxRequest.setNumero(aux.getNumeroEmpleado());
            auxRequest.setHoraEntrada(fechas.DateToOffsetDateTime(aux.getFechaEntrada()));
            if(aux.getFechaSalida() != null)
            {
                auxRequest.setHoraSalida(fechas.DateToOffsetDateTime(aux.getFechaSalida()));
            }
            asistenciaRequest.add(auxRequest);
        }
        List<SGPAsistenciaResponse> asistenciaResponse = null;
        asistenciaResponse = asistenciaService.enviarListaAsistencia(asistenciaRequest);
        List<Asistencia> asistenciaFinal = new ArrayList<Asistencia>();
        
        for(SGPAsistenciaResponse auxAsistenciaResponse : asistenciaResponse)
        {
            Asistencia nuevaAsistencia = new Asistencia();
            nuevaAsistencia.setIdAsistencia(auxAsistenciaResponse.getUUId());
            nuevaAsistencia.setNumeroEmpleado(auxAsistenciaResponse.getNumero());
            nuevaAsistencia.setFechaEntrada(fechas.OffsetDateTimeToDate(auxAsistenciaResponse.getHoraEntrada()));
            if(auxAsistenciaResponse.getHoraSalida() != null){
                nuevaAsistencia.setFechaSalida(fechas.OffsetDateTimeToDate(auxAsistenciaResponse.getHoraSalida()));
            }
            asistenciaFinal.add(nuevaAsistencia);
            if(auxAsistenciaResponse.getCodigoError() == 0)
            {
                asistDAO.borrarElemento(conn, nuevaAsistencia);
            }
        }
        
        return asistenciaFinal;
    }
    
    public List<Asistencia> actualizarAsistenciaEmpleados(Connection conn, AsistenciaService asistenciaService, String fecha)  throws Exception
    {
        List<Asistencia> listarAsistencia = new ArrayList<Asistencia>();
        //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //Date date = fechas.LocalDate(formatter.parse(fecha));
        
        listarAsistencia = asistDAO.obtenerElementosNoRegistrados(conn);
        for(Asistencia auxAsistencia : listarAsistencia)
        {
            Empleado auxEmpleado = empDAO.obtenerDato(conn, auxAsistencia.getNumeroEmpleado());
            boolean exitoso = registros.comprobarHuella(auxEmpleado.getB1(), auxEmpleado.getB2(), auxAsistencia.getB1());
            if(exitoso == true)
            {
                Asistencia asistencia = new Asistencia();
                asistencia.setIdAsistencia(auxAsistencia.getIdAsistencia());
                asistencia.setNumeroEmpleado(auxAsistencia.getNumeroEmpleado());
                asistencia.setFechaEntrada(auxAsistencia.getFechaEntrada());
                if(auxAsistencia.getFechaSalida() != null)
                {
                    asistencia.setFechaSalida(auxAsistencia.getFechaSalida());
                }
                asistencia.setB1(null);
                this.asistDAO.actualizarElemento(conn, asistencia);
                log.info("Se actualizo el registro de asistencia de {}", asistencia);
            }else
            {
                log.info("No se actualizo el empleado {}", auxEmpleado.getNumeroEmpleado());
            }
        }
        
        listarAsistencia = asistenciaEmpleados(conn, asistenciaService, fecha);
        
        return listarAsistencia;
    }
    
}
