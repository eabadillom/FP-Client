/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.hoth.fingerprint.interfaces;

import com.hoth.fingerprint.exceptions.FingerPrintException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Alberto
 * @param <T>
 */
public interface DAOInterface <T>
{
    
    T getModel(ResultSet rs) throws SQLException;
    
    void crearTabla(Connection con) throws SQLException, ClassNotFoundException;
    
    void borrarTabla(Connection con) throws SQLException, ClassNotFoundException;
    
    T obtenerDato(Connection con, String id) throws SQLException, FingerPrintException;
    
    List<T> obtenerTodosElementos(Connection con) throws SQLException, ClassNotFoundException;
    
    void guardarElemento(Connection con, T t) throws SQLException, FingerPrintException;
    
    void actualizarElemento(Connection con, T t, String parametro) throws SQLException, FingerPrintException;
    
    void borrarElemento(Connection con, T t) throws SQLException, FingerPrintException;
}
