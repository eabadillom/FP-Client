/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class DAO 
{
    private static Logger log = LogManager.getLogger(DAO.class);

    /**
     * Cierra un objeto de conexion a Base de Datos.
     *
     * @param conn Objeto de Conexion a Base de Datos.
     */
    public static void close(Connection conn) {
        try {
            if (conn != null) {
                if (!conn.isClosed()) {
                    conn.close();
                }
            }
            conn = null;
            log.debug("El objeto Connection se cerro satisfactoriamente.");
        } catch (SQLException ex) {
            log.error("Error al cerrar la conexion a BD.", ex);
        }
    }

    /**
     * Cierra un objeto de sentencia SQL PreparedStatement.
     *
     * @param ps Objeto PreparedStatement.
     */
    public static void close(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
            ps = null;
            log.debug("El objeto PreparedStatement se cerro satisfactoriamente.");
        } catch (SQLException ex) {
            log.error("Error al cerrar un PreparedStatement: ", ex);
        }
    }

    /**
     * Cierra un objeto de sentencia SQL CallableStatement
     *
     * @param cs Objeto CallableStatement
     */
    public static void close(CallableStatement cs) {
        try {
            if (cs != null) {
                cs.close();
            }
            cs = null;
            log.debug("El objeto CallableStatement se cerro satisfactoriamente.");
        } catch (SQLException ex) {
            log.error("Error al cerrar un CallableStatement: ", ex);
        }
    }

    /**
     * Cierra un objeto de datos ResultSet.
     *
     * @param rs Objeto ResultSet.
     */
    public static void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            rs = null;
            log.debug("El objeto ResultSet se cerro satisfactoriamente");
        } catch (SQLException ex) {
            log.error("Error al cerrar un ResultSet: ", ex);
        }
    }
    
    public static void close(Statement st)
    {
        try {
            if (st != null) {
                st.close();
            }
            st = null;
            log.debug("El objeto ResultSet se cerro satisfactoriamente");
        } catch (SQLException ex) {
            log.error("Error al cerrar un ResultSet: ", ex);
        }
    }

    public static synchronized String getTrim(String value) {
        String tmp = null;
        if (value != null) {
            tmp = value.trim();
        }
        return tmp;
    }

    public static synchronized Date getDate(java.sql.Date value) {
        Date tmp = null;
        if (value != null) {
            tmp = new Date(value.getTime());
        }
        return tmp;
    }
    
    public static synchronized Date getDate(java.sql.Timestamp value) {
        Date tmp = null;
        if (value != null) {
            tmp = new Date(value.getTime());
        }
        return tmp;
    }

    public static synchronized java.sql.Date getSqlDate(Date value) {
        java.sql.Date tmp = null;
        if (value != null) {
            tmp = new java.sql.Date(value.getTime());
        }

        return tmp;
    }

    public static synchronized Instant getInstant(Timestamp value) {
        Instant tmp = null;
        if (value != null) {
            tmp = value.toInstant();
        }
        return tmp;
    }

    public static synchronized Timestamp getTimestamp(Instant value) {
        Timestamp tmp = null;
        if (value != null) {
            tmp = Timestamp.from(value);
        }

        return tmp;
    }
    
    public static synchronized Timestamp getTimestamp(Date value) {
        Timestamp tmp = null;
        if (value != null) {
            tmp = new Timestamp(value.getTime());
        }

        return tmp;
    }
    
    /*public static synchronized Timestamp getTimestamp(LocalDate value) {
        Timestamp tmp = null;
        if (value != null) {
            tmp = new Timestamp(value.);
        }

        return tmp;
    }*/

    public static synchronized Integer getInteger(ResultSet rs, String column) throws SQLException {
        Integer result = null;
        Integer iTmp = 0;
        iTmp = rs.getInt(column);
        if (!rs.wasNull()) {
            result = new Integer(iTmp);
        }
        return result;
    }

    public static synchronized void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
    
}
