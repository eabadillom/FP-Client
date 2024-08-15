/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Alberto
 */
public class Conexion 
{
    private static Logger log = LogManager.getLogger(Conexion.class);
	
    public static synchronized Connection dsConexion() throws SQLException, ClassNotFoundException {
        log.info("Entrando a la conexion");
        Connection conn = null;
        String JDBC_DRIVER = "org.h2.Driver";
        String DB_URL = "jdbc:h2:~/asistencia";
        log.debug("Iniciando conexion a base de datos...");
        
        String u = "admAsistencia";
        String p = "Xc2RPIpQLgqco6EGymfL";
        Class.forName(JDBC_DRIVER);
        
        conn = DriverManager.getConnection(DB_URL, u, p);
        
        return conn;
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Connection con = null;
        con = dsConexion();
        return con;
    }

    public static void close(ResultSet resultset) {
        try {
            if (resultset != null)
                    resultset.close();
            resultset = null;
            log.debug("El objeto ResultSet se cerro satisfactoriamente");
        } catch (SQLException ex) {
            log.error("Error al cerrar un ResultSet: ", ex);
        }
    }

    public static void close(Statement statement) {
        try {
            if (statement != null)
                    statement.close();
            statement = null;
            log.debug("El objeto Statement se cerro satisfactoriamente.");
        } catch (SQLException ex) {
            log.error("Error al cerrar un Statement: ", ex);
        }
    }

    public static void close(PreparedStatement statement) {
        try {
            if (statement != null)
                    statement.close();
            statement = null;
            log.debug("El objeto PreparedStatement se cerro satisfactoriamente.");
        } catch (SQLException ex) {
            log.error("Error al cerrar un PreparedStatement: ", ex);
        }
    }

    public static synchronized void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                    connection.close();
            }
        } catch (SQLException ex) {
            log.error("Problema al cerrar el objeto Connection.", ex);
        } catch (Exception ex) {
            log.error("Problema general al cerrar el objeto Connection.", ex);
        } finally {
            connection = null;
        }
    }

    public static void rollback(Connection conn) {
        try {
            if (conn != null)
                conn.rollback();
        } catch (SQLException ex) {
            log.error("Problema al realizar rollback de la conexion.", ex);
        }
    }
    
}
