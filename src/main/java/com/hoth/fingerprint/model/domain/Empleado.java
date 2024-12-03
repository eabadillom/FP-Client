/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.domain;

/**
 *
 * @author Alberto
 */
public class Empleado 
{
    private String numeroEmpleado;
    private String b1;
    private String b2;

    public String getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public void setNumeroEmpleado(String numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }

    public String getB1() {
        return b1;
    }

    public void setB1(String b1) {
        this.b1 = b1;
    }

    public String getB2() {
        return b2;
    }

    public void setB2(String b2) {
        this.b2 = b2;
    }

    @Override
    public String toString() {
        return "DatosB [" + "numeroEmpleado: " + numeroEmpleado + ", b1: " + b1 + ", b2: " + b2 + ']';
    }
    
}
