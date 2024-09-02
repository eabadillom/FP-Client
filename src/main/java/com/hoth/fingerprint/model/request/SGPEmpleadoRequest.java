/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.request;

import java.util.Objects;

/**
 *
 * @author Alberto
 */

public class SGPEmpleadoRequest 
{
    private String numero = null;
    private String biometrico1 = null;
    private String biometrico2 = null;

    public SGPEmpleadoRequest() {
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBiometrico1() {
        return biometrico1;
    }

    public void setBiometrico1(String biometrico1) {
        this.biometrico1 = biometrico1;
    }

    public String getBiometrico2() {
        return biometrico2;
    }

    public void setBiometrico2(String biometrico2) {
        this.biometrico2 = biometrico2;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SGPEmpleadoRequest other = (SGPEmpleadoRequest) obj;
        return Objects.equals(this.numero, other.numero);
    }

    @Override
    public String toString() {
        return "SincronizacionEmpleadoRequest[" + "Numero Empleado: " + numero + ", B1: " + biometrico1 + ", B2: " + biometrico2 + ']';
    }
    
}
