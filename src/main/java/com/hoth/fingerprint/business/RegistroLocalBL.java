/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.business;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.hoth.fingerprint.dao.AsistenciaDAO;
import static com.hoth.fingerprint.dao.DAO.close;
import com.hoth.fingerprint.dao.EmpleadoDAO;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.gui.Verification;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.model.domain.Empleado;
import com.hoth.fingerprint.tools.Conexion;
import com.hoth.fingerprint.tools.DateUtils;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author Alberto
 */
public class RegistroLocalBL 
{
    private static Logger log = LogManager.getLogger(RegistroLocalBL.class);
    DateUtils fechaUtils = new DateUtils();
    
    private EmpleadoDAO empDAO;
    private AsistenciaDAO asistenciaDAO;

    public RegistroLocalBL() 
    {
        this.empDAO = new EmpleadoDAO();
        this.asistenciaDAO = new AsistenciaDAO();
    }
    
    public Fmd decodificar(String huella)
    {
		
            Fmd fmd = null;
            byte[] byteHuella = null;
            log.debug("entre al metodo decodificador");

            //Creamos el fmd en base a los bytes del string
            try 
            {
                log.debug("descodificare {}", huella);
                byteHuella = Base64.getDecoder().decode(new String(huella).getBytes("UTF-8"));
                log.debug("descodificada la huellla");
                log.debug("byeHuella: {}", byteHuella);
                fmd = UareUGlobal.GetImporter().ImportFmd(byteHuella, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
            } catch (UareUException e) {			
                    log.error("error al convertir en fmd", e);
            } catch (UnsupportedEncodingException e) {
                    log.error("Error al convertir en base 64", e);
            }

            log.debug("converti los bytes de huella a fmd ");
            log.debug("Fmd convertido {}", fmd);				

            return fmd;
	}
    
    public boolean comprobarHuella(String b1, String b2, String captura)
    {
        log.debug("Iniciando validacion...");
        //arreglo de biometricos
        Fmd[] fmd_s = new Fmd[3];
        Fmd capturaFmd = null;
        //Fmd enrolamientoFmd = null;

        Fmd huella = null;
        Fmd huella2 = null;

        log.info("Validando biometricos...");
        capturaFmd = decodificar(captura);
        log.debug("capturaFmd: {}",capturaFmd);					

        huella = decodificar(b1);
        log.trace("huella {}",huella);
        huella2 = decodificar(b2);
        log.trace("huella2 {}",huella2);

        fmd_s[0] = capturaFmd;
        fmd_s[1] = huella;
        fmd_s[2] = huella2;

        Verification.Run(fmd_s);

        return Verification.isFinger_M();
    }

    public boolean registrarEmpleado(Connection conn, String numeroEmpleado, String huella1, String huella2) throws FingerPrintException
    {
        boolean resultado = false;
        log.info("Entrado al metodo de registrar empleado");
        try
        {
            conn = Conexion.dsConexion();
            this.empDAO.crearTabla(conn);
            
            Empleado emp = empDAO.obtenerDato(conn, numeroEmpleado);

            if(emp == null)
            {
                emp = new Empleado();
                emp.setNumeroEmpleado(numeroEmpleado);
                emp.setB1(huella1);
                emp.setB2(huella2);
                resultado = true;
            }

        }catch(Exception ex)
        {
            log.error("Hubo un problema al registrar al empleado ", ex.getMessage());
            throw new FingerPrintException("Problema al realizar la conexión");
        }

        return resultado;
    } 

    public void registrarAsistencia(String numeroEmpleado, String captura) throws FingerPrintException
    {
        Connection conn = null;
        log.info("Entrado al metodo de registrar asistencia");
        UUID uuidAux;
        Date fecha = null;
        Calendar cal = null;
        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-6:00"), new Locale("es", "MX"));
        fecha = cal.getTime();
        //log.info("Fecha: " + fecha.toString());
        Date fechaNueva = fechaUtils.LocalDate(fecha);
        //log.info("Fecha convertida: "+fechaNueva.toString());
        try
        {
            conn = Conexion.dsConexion();
            this.asistenciaDAO.crearTabla(conn);

            Empleado emp = this.empDAO.obtenerDato(conn, numeroEmpleado);

            if(emp != null)
            {
                if(emp.getB1() != null && emp.getB2() != null)
                {
                    boolean exitoso = comprobarHuella(emp.getB1(), emp.getB2(), captura);
                    if(exitoso == true)
                    {
                        Asistencia asistenciaVer = asistenciaDAO.obtenerDatoActual(conn, emp.getNumeroEmpleado(), fechaNueva);
                        if(asistenciaVer != null)
                        {
                            uuidAux = asistenciaVer.getIdAsistencia();
                            asistenciaActualizarElemento(conn, emp, fecha, null, uuidAux);
                            
                        }else
                        {
                            asistenciaGuardarElemento(conn, emp, fecha, null);
                        }
                        /*if(asistenciaVer != null)
                        {
                            uuidAux = asistenciaVer.getIdAsistencia();
                            asistenciaActualizarElemento(conn, emp, fecha, null, uuidAux);
                        }else
                        {
                            asistenciaGuardarElemento(conn, emp, fecha, null);
                        }*/
                    }else
                    {
                        log.error("Comparacion no exitosa");
                        throw new FingerPrintException("Problemas al realizar la validacion...");
                    }
                }else
                {
                    Asistencia asistenciaVer = asistenciaDAO.obtenerDato(conn, emp.getNumeroEmpleado());
                    if(asistenciaVer != null)
                    {
                        uuidAux = asistenciaVer.getIdAsistencia();
                        asistenciaActualizarElemento(conn, emp, fecha, captura, uuidAux);
                    }else
                    {
                        asistenciaGuardarElemento(conn, emp, fecha, captura);
                    }
                }
            }else
            {
                Empleado empNuevo = new Empleado();
                empNuevo.setNumeroEmpleado(numeroEmpleado);
                empNuevo.setB1(null);
                empNuevo.setB2(null);
                this.empDAO.guardarElemento(conn, empNuevo);
                asistenciaGuardarElemento(conn, empNuevo, fecha, captura);
            }

        }catch(Exception ex)
        {
            log.error("Hubo un problema al registrar la asistencia ", ex.getMessage());
            throw new FingerPrintException("Problema al guardar la asistencia");
        }finally
        {
            close(conn);
        }

    }
    
    public void asistenciaGuardarElemento(Connection conn, Empleado emp, Date fecha, String captura) throws FingerPrintException
    {
        try
        {
            Asistencia asistencia = new Asistencia();
            asistencia.setNumeroEmpleado(emp.getNumeroEmpleado());
            asistencia.setFechaEntrada(fecha);
            asistencia.setFechaSalida(null);
            asistencia.setB1(captura);
            this.asistenciaDAO.guardarElemento(conn, asistencia);
        }catch(Exception ex)
        {
            log.error("Hubo un problema al registrar la asistencia ", ex.getMessage());
            throw new FingerPrintException("Problema al guardar la asistencia");
        }
    }
    
    public void asistenciaActualizarElemento(Connection conn, Empleado emp, Date fecha, String captura, UUID uuid) throws FingerPrintException
    {
        try
        {
            Asistencia asistencia = new Asistencia();
            asistencia.setIdAsistencia(uuid);
            asistencia.setNumeroEmpleado(emp.getNumeroEmpleado());
            asistencia.setFechaEntrada(null);
            asistencia.setFechaSalida(fecha);
            asistencia.setB1(captura);
            this.asistenciaDAO.actualizarElemento(conn, asistencia);
        }catch(Exception ex)
        {
            log.error("Hubo un problema al registrar la asistencia ", ex.getMessage());
            throw new FingerPrintException("Problema al guardar la asistencia");
        }
    }
    
}
