/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.dao;

import static com.hoth.fingerprint.dao.DAO.close;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.interfaces.DAOInterface;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class AsistenciaDAO extends DAO implements DAOInterface<Asistencia>
{
    private static Logger log = LogManager.getLogger(AsistenciaDAO.class);
    private static final String SELECT = "select numero_empleado, fecha_entrada, fecha_salida, b from asistencia ";
    private static final String INSERT = "insert into asistencia (numero_empleado, fecha_entrada, fecha_salida, b) values (?, ?, ?, ?)";
    private static final String UPDATE = "update asistencia set fecha_salida = ? ";
    private static final String DELETE = "delete from asistencia ";
    
    @Override
    public Asistencia getModel(ResultSet rs) throws SQLException
    {
        Asistencia model = new Asistencia();
        
        model.setNumeroEmpleado(getTrim(rs.getString("numero_empleado")));
        model.setFechaEntrada(rs.getTimestamp("fecha_entrada"));
        model.setFechaSalida(rs.getTimestamp("fecha_salida"));
        model.setB1(getTrim(rs.getString("b")));
        
        return model;
    }
    
    @Override
    public void crearTabla(Connection con) throws SQLException, ClassNotFoundException 
    {
        int creacionTabla = 0;
        String createTableSQL = "CREATE TABLE IF NOT EXISTS asistencia("
                + "id_asistencia uuid default random_uuid() primary key,"
                + "numero_empleado varchar(10),"
                + "fecha_entrada timestamp null,"
                + "fecha_salida timestamp null,"
                + "b text null,"
                + "constraint fk_empleado foreign key (numero_empleado) references datos_b(numero_empleado)"
                + ")";
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(createTableSQL);
            creacionTabla = ps.executeUpdate();
            
            if(creacionTabla != 0){
                log.info("Tabla creada exitosamente!!!");
            }else{
                log.info("Tabla ya creada");
            }
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
        }
    }

    @Override
    public void borrarTabla(Connection con) throws SQLException, ClassNotFoundException
    {
        int creacionTabla = 0;
        String borrarTableSQL = "truncate table asistencia";
        PreparedStatement ps = null;
        try
        {
            ps = con.prepareStatement(borrarTableSQL);
            creacionTabla = ps.executeUpdate();
            
            if(creacionTabla != 0){
                log.info("Contenido de la tabla borrada exitosamente!!!");
            }else{
                log.info("Tabla ya limpia");
            }
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
        }
    }

    @Override
    public Asistencia obtenerDato(Connection con, String id) throws SQLException, FingerPrintException 
    {
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarPorId = null;
        
        try
        {
            if(id == null){
                throw new FingerPrintException("El id de asistencia no debe de ser un dato vacio.");
            }
            
            if(id.isEmpty()){
                throw new FingerPrintException("El id de asistencia no debe de ser un dato vacio.");
            }
            
            buscarPorId = SELECT + "where numero_empleado = ?";
            ps = con.prepareStatement(buscarPorId);
            ps.setString(1, id);
            rs = ps.executeQuery();
            
            if(rs == null)
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            
            if(rs.next())
            {
                model = getModel(rs);
                log.info("Se obtuvo la asistencia satisfactoriamente");
            }
            
        } finally
        {
            log.info("Cerrando Conexion");
            close(ps);
            close(rs);
        }
        return model;
    }

    @Override
    public List<Asistencia> obtenerTodosElementos(Connection con) throws SQLException, ClassNotFoundException 
    {
        List<Asistencia> listaModel = null;
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarTodos = null;
        
        try
        {
            buscarTodos = SELECT;
            ps = con.prepareStatement(buscarTodos);
            rs = ps.executeQuery();
            listaModel = new ArrayList<Asistencia>();
            
            while(rs.next())
            {
                model = getModel(rs);
                listaModel.add(model);
            }
            log.info("Se cargo la lista de asistencia satisfactoriamente");
            
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
            close(rs);
        }
        
        return listaModel;
    }
    
    @Override
    public void guardarElemento(Connection con, Asistencia t) throws SQLException, FingerPrintException 
    {
        int resultUpdate = 0;
        int indice = 0;
        String guardarConsulta = null;
        
        PreparedStatement ps = null;
        try
        {
            if(t == null){
                throw new FingerPrintException("La asistencia no debe de ser un objeto vacio o nulo.");
            }
            
            guardarConsulta = INSERT;
            ps = con.prepareStatement(guardarConsulta);
            ps.setString(++indice, t.getNumeroEmpleado());
            ps.setTimestamp(++indice, t.getFechaEntrada());
            ps.setTimestamp(++indice, t.getFechaSalida());
            ps.setString(++indice, t.getB1());
            resultUpdate = ps.executeUpdate();
            
            if(resultUpdate != 0){
                log.info("Se guardo el empleado satisfactoriamente");
            }else{
                log.error("No se guardo el empleado");
                throw new FingerPrintException("Hubo un problema en la base de datos.");
            }
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
        }
    }

    @Override
    public void actualizarElemento(Connection con, Asistencia t, String parametro) throws SQLException, FingerPrintException {
        int resultUpdate = 0;
        String modificarConsulta = null;
        PreparedStatement ps = null;
        
        try
        {
            if(t == null){
                throw new FingerPrintException("La asistencia no debe de ser un objeto vacio o nulo.");
            }
            
            if(parametro == null)
                throw new FingerPrintException("El numero del empleado no debe ser una dato vacio");
            
            if(parametro.isEmpty())
                throw new FingerPrintException("El numero del empleado no debe ser una dato vacio");
            
            modificarConsulta = UPDATE + "where numero_empleado = ?";
            ps = con.prepareStatement(modificarConsulta);
            ps.setTimestamp(1, t.getFechaSalida());
            ps.setString(2, parametro);
            resultUpdate = ps.executeUpdate();
            if(resultUpdate != 0)
            {
                log.info("Se actualizo correctamente la asistencia!!!");
            }else
            {
                log.error("No se actualizo la asistencia");
                throw new FingerPrintException("Hubo un problema con la base de datos.");
            }
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
        }
    }

    @Override
    public void borrarElemento(Connection con, Asistencia t) throws SQLException, FingerPrintException
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
                log.info("Se borro la asistencia satisfactoriamente!!!");
            }else{
                log.error("No se borro la asistencia");
                throw new FingerPrintException("Hubo un problema en la base de datos.");
            }
        }finally
        {
            close(ps);
        }
    }
    
}
