/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.business;

import com.hoth.fingerprint.dao.AsistenciaDAO;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.model.request.SGPAsistenciaRequest;
import com.hoth.fingerprint.model.response.SGPAsistenciaResponse;
import com.hoth.fingerprint.service.AsistenciaService;
import com.hoth.fingerprint.tools.DateUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    DateUtils fechas = new DateUtils();

    public SincronizaAsistenciaBL() 
    {
        this.asistDAO = new AsistenciaDAO();
    }
    
    public List<Asistencia> asistenciaEmpleados(Connection conn, AsistenciaService asistenciaService)  throws SQLException, FingerPrintException, ClassNotFoundException
    {
        List<SGPAsistenciaRequest> asistenciaRequest = new ArrayList<SGPAsistenciaRequest>();
        List<Asistencia> listarAsistencia = new ArrayList<Asistencia>();
        asistDAO.crearTabla(conn);
        listarAsistencia = asistDAO.obtenerElementosRegistrados(conn);
        
        for(Asistencia aux : listarAsistencia)
        {
            SGPAsistenciaRequest auxRequest = new SGPAsistenciaRequest();
            auxRequest.setUUId(aux.getIdAsistencia());
            auxRequest.setNumero(aux.getNumeroEmpleado());
            auxRequest.setHoraEntrada(fechas.DateToOffsetDateTime(aux.getFechaEntrada()));
            auxRequest.setHoraSalida(fechas.DateToOffsetDateTime(aux.getFechaSalida()));
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
            nuevaAsistencia.setFechaSalida(fechas.OffsetDateTimeToDate(auxAsistenciaResponse.getHoraSalida()));
            //nuevaAsistencia.setB1(null);
            asistenciaFinal.add(nuevaAsistencia);
        }
        
        return asistenciaFinal;
    }
    
    
    
}
