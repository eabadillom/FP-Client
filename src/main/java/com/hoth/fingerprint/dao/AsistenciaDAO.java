/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.dao;

import static com.hoth.fingerprint.dao.DAO.close;
import com.hoth.fingerprint.exceptions.FingerPrintException;
import com.hoth.fingerprint.model.domain.Asistencia;
import com.hoth.fingerprint.interfaces.DAOInterface;
import com.hoth.fingerprint.tools.DateUtils;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class AsistenciaDAO extends DAO implements DAOInterface<Asistencia>
{
    private static Logger log = LogManager.getLogger(AsistenciaDAO.class);
    DateUtils fechaUtils = new DateUtils();
    private static final String SELECT = "select id_asistencia, numero_empleado, fecha_entrada, fecha_salida, b from asistencia ";
    private static final String SELECTNOREGISTROS = "select id_asistencia, asistencia.numero_empleado, fecha_entrada, fecha_salida, b from asistencia , datos_b ";
    private static final String INSERT = "insert into asistencia (numero_empleado, fecha_entrada, fecha_salida, b) values (?, ?, ?, ?)";
    private static final String UPDATE = "update asistencia ";
    private static final String DELETE = "delete from asistencia ";
    
    
    @Override
    public Asistencia getModel(ResultSet rs) throws SQLException
    {
        Asistencia model = new Asistencia();
        
        UUID uuid = (java.util.UUID) rs.getObject("id_asistencia");
        model.setIdAsistencia(uuid);
        model.setNumeroEmpleado(getTrim(rs.getString("numero_empleado")));
        model.setFechaEntrada(getDate(rs.getTimestamp("fecha_entrada")));
        model.setFechaSalida(getDate(rs.getTimestamp("fecha_salida")));
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
            
            if(creacionTabla != 0)
            {
                log.debug("Tabla creada exitosamente!!!");
            }else
            {
                log.debug("Tabla ya creada");
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
        String borrarTableSQL = "truncate table asistencia";
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
                log.debug("Contenido de la tabla borrada exitosamente!!!");
            }else
            {
                log.debug("Tabla ya limpia");
            }
        }finally
        {
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
        int indice = 0;
        
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
            ps.setString(++indice, getTrim(id));
            rs = ps.executeQuery();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            if(rs.next())
            {
                model = getModel(rs);
                log.debug("Se obtuvo la asistencia {} satisfactoriamente", model.getIdAsistencia());
            }
            
        } finally
        {
            close(ps);
            close(rs);
        }
        return model;
    }
    
    public Asistencia obtenerDatoPorFecha(Connection con, String id, Date fechaActual) throws SQLException, FingerPrintException 
    {
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarPorFecha = null;
        int indice = 0;
        
        try
        {
            if(id == null){
                throw new FingerPrintException("El id de asistencia no debe de ser un dato vacio.");
            }
            
            if(fechaActual == null){
                throw new FingerPrintException("La fecha de asistencia no debe de ser un dato vacio.");
            }
            
            buscarPorFecha = SELECT + "where numero_empleado = ? and cast(fecha_entrada as date) = ?";
            ps = con.prepareStatement(buscarPorFecha);
            ps.setString(++indice, getTrim(id));
            ps.setDate(++indice, getSqlDate(fechaActual));
            rs = ps.executeQuery();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            if(rs.next())
            {
                model = getModel(rs);
                log.debug("Se obtuvo la asistencia {} satisfactoriamente", model.getIdAsistencia());
            }
            
        } finally
        {
            close(ps);
            close(rs);
        }
        return model;
    }

    @Override
    public List<Asistencia> obtenerTodosElementos(Connection con) throws SQLException, FingerPrintException 
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
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            while(rs.next())
            {
                model = getModel(rs);
                listaModel.add(model);
            }
            log.debug("Se cargo la lista de asistencia satisfactoriamente");
            
        }finally
        {
            close(ps);
            close(rs);
        }
        
        return listaModel;
    }
    
    public List<Asistencia> obtenerElementosRegistrados(Connection con, Date fecha) throws SQLException, FingerPrintException
    {
        List<Asistencia> listaModel = null;
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarTodos = null;
        int indice = 0;
        
        try
        {
            buscarTodos = SELECT + "where fecha_entrada is not null and b is null and cast(fecha_entrada as date) = ?";
            ps = con.prepareStatement(buscarTodos);
            ps.setDate(++indice, getSqlDate(fecha));
            
            rs = ps.executeQuery();
            listaModel = new ArrayList<>();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            while(rs.next())
            {
                model = getModel(rs);
                listaModel.add(model);
            }
            log.debug("Se cargo la lista de asistencia satisfactoriamente");
            
        }finally
        {
            close(ps);
            close(rs);
        }
        
        return listaModel;
    }
    
    
    public List<Asistencia> obtenerElementosNoRegistrados(Connection con) throws SQLException, FingerPrintException
    {
        List<Asistencia> listaModel = null;
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarTodos = null;
        int indice = 0;
        
        try
        {
            buscarTodos = SELECTNOREGISTROS + "where fecha_entrada is not null and b is not null and asistencia.numero_empleado = datos_b.numero_empleado and (b1 is not null or b2 is not null)";
            ps = con.prepareStatement(buscarTodos);
            
            rs = ps.executeQuery();
            listaModel = new ArrayList<>();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            while(rs.next())
            {
                model = getModel(rs);
                listaModel.add(model);
            }
            log.debug("Se cargo la lista de asistencia satisfactoriamente");
            
        }finally
        {
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
            ps.setString(++indice, getTrim(t.getNumeroEmpleado()));
            ps.setTimestamp(++indice, getTimestamp(t.getFechaEntrada()));
            ps.setTimestamp(++indice, getTimestamp(t.getFechaSalida()));
            ps.setString(++indice, getTrim(t.getB1()));
            resultUpdate = ps.executeUpdate();
            
            if(resultUpdate != 0)
            {
                log.debug("Se guardo el empleado satisfactoriamente {}", t.getIdAsistencia());
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
    public void actualizarElemento(Connection con, Asistencia t) throws SQLException, FingerPrintException {
        int resultUpdate = 0;
        int indice = 0;
        String modificarConsulta = null;
        PreparedStatement ps = null;
        
        try
        {
            if(t == null)
            {
                throw new FingerPrintException("La asistencia no debe de ser un objeto vacio o nulo.");
            }
            
            if(t.getIdAsistencia() == null)
            {
                throw new FingerPrintException("El numero de asistencia no debe ser una dato vacio");
            }
            
            modificarConsulta = UPDATE + "set fecha_salida = ?, b = ? where id_asistencia = ?";
            ps = con.prepareStatement(modificarConsulta);
            ps.setTimestamp(++indice, getTimestamp(t.getFechaSalida()));
            if(t.getB1() == null)
            {
                ps.setString(++indice, null);
            }else
            {
                ps.setString(++indice, getTrim(t.getB1()));
            }
            ps.setObject(++indice, t.getIdAsistencia());
            resultUpdate = ps.executeUpdate();
            if(resultUpdate != 0)
            {
                log.debug("Se actualizo correctamente la asistencia {}!!!", t.getIdAsistencia());
            }else
            {
                log.error("No se actualizo la asistencia");
                throw new FingerPrintException("Hubo un problema con la base de datos.");
            }
        }finally
        {
            close(ps);
        }
    }

    @Override
    public void borrarElemento(Connection con, Asistencia t) throws SQLException, FingerPrintException
    {
        int resultDelete = 0;
        int indice = 0;
        String borrarConsulta = null;
        PreparedStatement ps = null;
        
        try
        {
            if(t == null){
                throw new FingerPrintException("El cliente no debe de ser un objeto vacio o nulo.");
            }
            
            borrarConsulta = DELETE + "where id_asistencia = ?";
            ps = con.prepareStatement(borrarConsulta);
            ps.setObject(++indice, t.getIdAsistencia());
            resultDelete = ps.executeUpdate();
            
            if(resultDelete != 0)
            {
                log.info("Se borro la asistencia {} satisfactoriamente!!!", t.getIdAsistencia() + " - " + t.getNumeroEmpleado());
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
