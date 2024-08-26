/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.dao;

import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Empleado;
import com.hoth.fingerprint.interfaces.DAOInterface;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class EmpleadoDAO extends DAO implements DAOInterface<Empleado>
{
    private static Logger log = LogManager.getLogger(EmpleadoDAO.class);
    private static final String SELECT = "select numero_empleado, b1, b2 from datos_b ";
    private static final String INSERT = "insert into datos_b (numero_empleado, b1, b2) values (?, ?, ?)";
    private static final String UPDATE = "update datos_b set b1 = ?, b2 = ? ";
    private static final String DELETE = "delete from datos_b ";
    
    @Override
    public Empleado getModel(ResultSet rs) throws SQLException
    {
        Empleado model = new Empleado();
        
        model.setNumeroEmpleado(getTrim(rs.getString("numero_empleado")));
        model.setB1(getTrim(rs.getString("b1")));
        model.setB2(getTrim(rs.getString("b2")));
        log.info("Registrando los datos al modelo con exito!!!");
        
        return model;
    }
    
    @Override
    public void crearTabla(Connection con) throws SQLException, ClassNotFoundException 
    {
        boolean estado = false;
        int creacionTabla = 0;
        String createTableSQL = "CREATE TABLE IF NOT EXISTS datos_b("
                + "numero_empleado varchar(10) primary key,"
                + "b1 text null,"
                + "b2 text null"
                + ")";
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(createTableSQL);
            creacionTabla = ps.executeUpdate();
            
            if(creacionTabla != 0)
            {
                log.info("Tabla creada exitosamente!!!");
            }else
            {
                log.info("Tabla ya creada");
            }
        }finally
        {
            close(ps);
        }
    }

    @Override
    public void borrarTabla(Connection con) throws SQLException, FingerPrintException 
    {
        int creacionTabla = 0;
        String borrarTableSQL = "truncate table datos_b";
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(borrarTableSQL);
            creacionTabla = ps.executeUpdate();
            
            if(ps == null)
            {
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            if(creacionTabla != 0)
            {
                log.info("Contenido de la tabla borrada exitosamente!!!");
            }else
            {
                log.info("Tabla ya limpia");
            }
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
        }
    }

    @Override
    public Empleado obtenerDato(Connection con, String id) throws SQLException, FingerPrintException
    {
        Empleado model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarPorId = null;
        
        try
        {
            if(id == null){
                throw new FingerPrintException("El id del cliente no debe de ser un dato vacio.");
            }
            
            if(id.isEmpty()){
                throw new FingerPrintException("El id del cliente no debe de ser un dato vacio.");
            }
            
            buscarPorId = SELECT + "where numero_empleado = ?";
            ps = con.prepareStatement(buscarPorId);
            ps.setString(1, id);
            rs = ps.executeQuery();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            if(rs.next())
            {
                model = getModel(rs);
                log.info("Se obtuvo el empleado satisfactoriamente");
            }
        } finally
        {
            close(ps);
            close(rs);
        }
        return model;
    }

    @Override
    public List<Empleado> obtenerTodosElementos(Connection con) throws SQLException, FingerPrintException {
        List<Empleado> listaModel = null;
        Empleado model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarTodos = null;
        
        try
        {
            buscarTodos = SELECT;
            ps = con.prepareStatement(buscarTodos);
            rs = ps.executeQuery();
            listaModel = new ArrayList<Empleado>();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            while(rs.next())
            {
                model = getModel(rs);
                listaModel.add(model);
            }
            log.info("Se cargo la lista de empleados satisfactoriamente");
            
        }finally
        {
            close(ps);
            close(rs);
        }
        
        return listaModel;
    }
    
    public Empleado obtenerEmpleadoSinHuella(Connection conn) throws SQLException, FingerPrintException
    {
        //Empleado> listaModel = null;
        Empleado model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarEmpleadoSinHuellas = null;
        
        try
        {
            buscarEmpleadoSinHuellas = SELECT + "where b1 is null or b2 is null";
            ps = conn.prepareStatement(buscarEmpleadoSinHuellas);
            rs = ps.executeQuery();
            //listaModel = new ArrayList<Empleado>();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            if(rs.next())
            {
                model = getModel(rs);
                //listaModel.add(model);
            }
            log.info("Se encontro empleadon sin alguna huella");
        }
        finally
        {
            close(ps);
            close(rs);
        }
        
        return model;
    }

    @Override
    public void guardarElemento(Connection conn, Empleado t) throws SQLException, FingerPrintException
    {
        int resultUpdate = 0;
        int indice = 0;
        String guardarConsulta = null;
        
        PreparedStatement ps = null;
        try
        {
            if(t == null){
                throw new FingerPrintException("El cliente no debe de ser un objeto vacio o nulo.");
            }
            
            guardarConsulta = INSERT;
            ps = conn.prepareStatement(guardarConsulta);
            ps.setString(++indice, getTrim(t.getNumeroEmpleado()));
            ps.setString(++indice, getTrim(t.getB1()));
            ps.setString(++indice, getTrim(t.getB2()));
            resultUpdate = ps.executeUpdate();
            
            if(resultUpdate != 0){
                log.info("Se guardo el empleado satisfactoriamente");
            }else{
                log.error("No se guardo el empleado");
                throw new FingerPrintException("Hubo un problema en la base de datos.");
            }
        }finally
        {
            close(ps);
        }
    }

    @Override
    public void actualizarElemento(Connection con, Empleado t) throws SQLException, FingerPrintException 
    {
        int resultUpdate = 0;
        int incremento = 0;
        
        String actualizarConsulta = null;
        PreparedStatement ps = null;
        
        try
        {
            if(t == null){
                throw new FingerPrintException("El cliente no debe de ser un objeto vacio o nulo");
            }
            if(t.getNumeroEmpleado() == null){
                throw new FingerPrintException("El id del empleado no debe de ser incorrecto!!!");
            }
            if(t.getNumeroEmpleado().isEmpty()){
                throw new FingerPrintException("El id del empleado no debe de ser un objeto vacio o nulo");
            }
            
            actualizarConsulta = UPDATE + "where numero_empleado = ?";
            ps = con.prepareStatement(actualizarConsulta);
            ps.setString(++incremento, t.getB1());
            ps.setString(++incremento, t.getB2());
            ps.setString(++incremento, t.getNumeroEmpleado());
            resultUpdate = ps.executeUpdate();
            
            if(resultUpdate != 0)
            {
                log.info("Se actualizo correctamente el empleado!!!");
            }else
            {
                log.error("No se actualizo el empleado");
                throw new FingerPrintException("Hubo un problema en la base de datos.");
            }
        }finally
        {
            close(ps);
        }
        
    }

    @Override
    public void borrarElemento(Connection con, Empleado t) throws SQLException, FingerPrintException
    {
        int resultDelete = 0;
        String borrarConsulta = null;
        PreparedStatement ps = null;
        
        try
        {
            if(t == null){
                throw new FingerPrintException("El cliente no debe de ser un objeto vacio o nulo.");
            }
            
            borrarConsulta = DELETE + "where numero_empleado = ?";
            ps = con.prepareStatement(borrarConsulta);
            ps.setString(1, t.getNumeroEmpleado());
            resultDelete = ps.executeUpdate();
            
            if(resultDelete != 0){
                log.info("Se borro el empleado satisfactoriamente");
            }else{
                log.error("No se borro el empleado");
                throw new FingerPrintException("Hubo un problema en la base de datos.");
            }
        }finally
        {
            close(ps);
        }
    }
    
}
