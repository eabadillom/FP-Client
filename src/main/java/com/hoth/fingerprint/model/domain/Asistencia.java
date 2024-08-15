/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.domain;

import java.sql.Timestamp;
import java.util.UUID;

/**
 *
 * @author Alberto
 */
public class Asistencia 
{
    private UUID idAsistencia;
    private String numeroEmpleado;
    private Empleado datosEmpleado = new Empleado();
    private Timestamp fechaEntrada;
    private Timestamp fechaSalida;
    private String b1;

    public UUID getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(UUID idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public String getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public void setNumeroEmpleado(String numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }
    
    public Empleado getDatosEmpleado() {
        return datosEmpleado;
    }

    public void setDatosEmpleado(Empleado datosEmpleado) {
        this.datosEmpleado = datosEmpleado;
    }

    public Timestamp getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Timestamp fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Timestamp getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Timestamp fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getB1() {
        return b1;
    }

    public void setB1(String b1) {
        this.b1 = b1;
    }

    @Override
    public String toString() {
        return "Asistencia [" + "idAsistencia: " + idAsistencia + ", numeroEmpleado: " + numeroEmpleado + ", fechaEntrada: " + fechaEntrada + ", fechaSalida: " + fechaSalida + ", b1: " + b1 + ']';
    }
    
}
