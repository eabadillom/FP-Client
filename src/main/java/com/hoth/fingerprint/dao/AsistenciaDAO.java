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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
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
    private static final String SELECT = "select id_asistencia, numero_empleado, fecha_entrada, fecha_salida, b from asistencia ";
    private static final String SELECTTODOSREGISTROS = "select id_asistencia, numero_empleado, fecha_entrada, fecha_salida from asistencia ";
    private static final String INSERT = "insert into asistencia (numero_empleado, fecha_entrada, fecha_salida, b) values (?, ?, ?, ?)";
    private static final String UPDATE = "update asistencia set fecha_salida = ? ";
    private static final String DELETE = "delete from asistencia ";
    
    
    @Override
    public Asistencia getModel(ResultSet rs) throws SQLException
    {
        Asistencia model = new Asistencia();
        
        UUID uuid = (java.util.UUID) rs.getObject("id_asistencia");
        model.setIdAsistencia(uuid);
        model.setNumeroEmpleado(getTrim(rs.getString("numero_empleado")));
        model.setFechaEntrada(dateWithTimeZone(getDate(rs.getTimestamp("fecha_entrada"))));
        model.setFechaSalida(dateWithTimeZone(getDate(rs.getTimestamp("fecha_salida"))));
        model.setB1(getTrim(rs.getString("b")));
        
        return model;
    }
    
    public Asistencia getModel2(ResultSet rs) throws SQLException
    {
        Asistencia model = new Asistencia();
        
        UUID uuid = (java.util.UUID) rs.getObject("id_asistencia");
        model.setIdAsistencia(uuid);
        model.setNumeroEmpleado(getTrim(rs.getString("numero_empleado")));
        model.setFechaEntrada(dateWithTimeZone(getDate(rs.getTimestamp("fecha_entrada"))));
        model.setFechaSalida(dateWithTimeZone(getDate(rs.getTimestamp("fecha_salida"))));
        
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
                log.info("Tabla creada exitosamente!!!");
            }else
            {
                log.info("Tabla ya creada");
            }
        }finally
        {
            log.info("Cerrando Conexion");
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
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
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
    
    public Asistencia obtenerDatoActual(Connection con, String id, Date fechaActual) throws SQLException, FingerPrintException 
    {
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarPorFecha = null;
        
        try
        {
            if(fechaActual == null){
                throw new FingerPrintException("El id de asistencia no debe de ser un dato vacio.");
            }
            
            buscarPorFecha = "select id_asistencia, numero_empleado, cast(fecha_entrada as date) as event_date from asistencia where numero_empleado = ? and cast(fecha_entrada as date) = ?";
            ps = con.prepareStatement(buscarPorFecha);
            ps.setString(1, getTrim(id));
            //Date date = java.sql.Date.valueOf(fechaActual);
            log.info("Fecha ya formateada: " + fechaActual.toString());
            //LocalDateTime localtime = fechaActual.atStartOfDay();
            //Instant instant = localtime.toInstant(ZoneOffset.UTC);
            //Timestamp timestamp = Timestamp.from(instant);
            //SimpleDateFormat formated = new SimpleDateFormat("yyyy-MM-dd");
            //String formattedDate = formated.format(timestamp);
            //log.info("Fecha ya formateada: " + instant.toString());
            
            ps.setDate(2, getSqlDate(fechaActual));
            rs = ps.executeQuery();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
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
            log.info("Se cargo la lista de asistencia satisfactoriamente");
            
        }finally
        {
            log.info("Cerrando Conexion");
            close(ps);
            close(rs);
        }
        
        return listaModel;
    }
    
    public List<Asistencia> obtenerElementosRegistrados(Connection con) throws SQLException, FingerPrintException
    {
        List<Asistencia> listaModel = null;
        Asistencia model = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String buscarTodos = null;
        
        try
        {
            buscarTodos = SELECTTODOSREGISTROS + "where fecha_entrada is not null and fecha_salida is not null";
            ps = con.prepareStatement(buscarTodos);
            rs = ps.executeQuery();
            listaModel = new ArrayList<>();
            
            if(rs == null){
                throw new FingerPrintException("Ocurrio algo con la base de datos");
            }
            
            while(rs.next())
            {
                model = getModel2(rs);
                listaModel.add(model);
            }
            log.info("Se cargo la lista de asistencia satisfactoriamente");
            
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
            ps.setString(++indice, t.getNumeroEmpleado());
            ps.setTimestamp(++indice, getTimestamp(t.getFechaEntrada()));
            ps.setTimestamp(++indice, getTimestamp(t.getFechaSalida()));
            ps.setString(++indice, t.getB1());
            resultUpdate = ps.executeUpdate();
            
            if(resultUpdate != 0)
            {
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
    public void actualizarElemento(Connection con, Asistencia t) throws SQLException, FingerPrintException {
        int resultUpdate = 0;
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
            
            modificarConsulta = UPDATE + "where id_asistencia = ?";
            ps = con.prepareStatement(modificarConsulta);
            ps.setTimestamp(1, getTimestamp(t.getFechaSalida()));
            ps.setObject(2, t.getIdAsistencia());
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
            
            if(resultDelete != 0)
            {
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
    
    public Date dateWithTimeZone(Date fecha)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/GMT-6"));
        Date utcDate = calendar.getTime();
        return utcDate;
    }
    
}
