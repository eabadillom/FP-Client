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
import java.sql.SQLException;
import java.util.Base64;
import java.util.Date;
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
        boolean exitoso;
        try{
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
            exitoso = Verification.isFinger_M();
            return exitoso;
        }catch(NullPointerException | NegativeArraySizeException ex)
        {
            log.error("Hubo un error en la comparacion de las huellas!!! ", ex);
            return false;
        }
    }

    public boolean registrarEmpleado(Connection conn, String numeroEmpleado, String huella1, String huella2) throws FingerPrintException
    {
        boolean resultado = false;
        log.debug("Entrado al metodo de registrar empleado");
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
                this.empDAO.guardarElemento(conn, emp);
                resultado = true;
            }

        }catch(FingerPrintException | ClassNotFoundException | SQLException ex)
        {
            log.error("Hubo un problema al registrar al empleado ", ex.getMessage());
            throw new FingerPrintException("Problema al realizar la conexión");
        }finally
        {
            close(conn);
        }

        return resultado;
    } 
    
    public Asistencia registrarAsistencia(String numeroEmpleado, String captura) throws FingerPrintException
    {
        log.info("Entrando al método registrar asistencia local");
        Date fecha = fechaUtils.obtenerFechaSistema();
        Asistencia asistenciaEnvio = null;
        Connection conn = null;
        try
        {
            conn = Conexion.dsConexion();
            empDAO.crearTabla(conn);
            asistenciaDAO.crearTabla(conn);

            Empleado empleado = empDAO.obtenerDato(conn, numeroEmpleado);
            if (empleado == null) 
            {
                asistenciaEnvio = registrarNuevoEmpleado(conn, numeroEmpleado, captura, fecha);
                log.info("Guardando un nuevo empleado {} con asistencia", numeroEmpleado);
                return asistenciaEnvio;
            }

            if (empleado.getB1() != null && empleado.getB2() != null) 
            {
                if (!comprobarHuella(empleado.getB1(), empleado.getB2(), captura)) 
                {
                    log.error("Comparación no exitosa del empleado {}", empleado.getNumeroEmpleado());
                    throw new FingerPrintException("Problemas al realizar la validación!!!");
                }
                asistenciaEnvio = actualizarORegistrarAsistencia(conn, empleado, fecha, null);
                return asistenciaEnvio;
            }

            asistenciaEnvio = actualizarORegistrarAsistencia(conn, empleado, fecha, captura);
            return asistenciaEnvio;
        }
        catch (FingerPrintException | ClassNotFoundException | SQLException ex) {
            log.error("Hubo un problema al registrar la asistencia: {}", ex.getMessage());
            throw new FingerPrintException("Problema al guardar la asistencia");
        } finally {
            close(conn);
        }
    }
    
    private Asistencia registrarNuevoEmpleado(Connection conn, String numeroEmpleado, String captura, Date fecha) throws FingerPrintException, ClassNotFoundException, SQLException 
    {
        Empleado nuevoEmpleado = new Empleado();
        nuevoEmpleado.setNumeroEmpleado(numeroEmpleado);
        nuevoEmpleado.setB1(null);
        nuevoEmpleado.setB2(null);
        empDAO.guardarElemento(conn, nuevoEmpleado);

        asistenciaGuardarElemento(conn, nuevoEmpleado, fecha, captura);
        log.debug("Nuevo empleado registrado con asistencia");

        Asistencia asistencia = new Asistencia();
        asistencia.setNumeroEmpleado(numeroEmpleado);
        asistencia.setFechaEntrada(fecha);
        asistencia.setFechaSalida(null);
        asistencia.setB1(null);
        return asistencia;
    }
    
    private Asistencia actualizarORegistrarAsistencia(Connection conn, Empleado empleado, Date fecha, String captura) throws FingerPrintException, ClassNotFoundException, SQLException 
    {
        Date fechaNueva = fechaUtils.LocalDate(fecha);
        Asistencia asistenciaExistente = asistenciaDAO.obtenerDatoPorFecha(conn, empleado.getNumeroEmpleado(), fechaNueva);
        Asistencia asistencia = new Asistencia();
        asistencia.setNumeroEmpleado(empleado.getNumeroEmpleado());
        
        if (asistenciaExistente != null) 
        {
            log.info("Actualizando registro de asistencia del empleado {}", empleado.getNumeroEmpleado());
            asistenciaActualizarElemento(conn, empleado, fecha, captura, asistenciaExistente.getIdAsistencia());
            asistencia.setFechaEntrada(asistenciaExistente.getFechaEntrada());
            asistencia.setFechaSalida(fecha);
        } else 
        {
            log.info("Guardando nuevo registro de asistencia del empleado {}", empleado.getNumeroEmpleado());
            asistenciaGuardarElemento(conn, empleado, fecha, captura);
            asistencia.setFechaEntrada(fecha);
            asistencia.setFechaSalida(null);
        }
        return asistencia;
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
        }catch(FingerPrintException | SQLException ex)
        {
            log.error("Hubo un problema al registrar la asistencia ", ex.getMessage());
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
        }catch(FingerPrintException | SQLException ex)
        {
            log.error("Hubo un problema al actualizar la asistencia ", ex.getMessage());
        }
    }
    
}
