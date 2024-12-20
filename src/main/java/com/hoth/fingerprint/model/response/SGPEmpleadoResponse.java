/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.response;

import com.hoth.fingerprint.model.request.SGPEmpleadoRequest;
import java.util.Objects;

/**
 *
 * @author Alberto
 */
public class SGPEmpleadoResponse extends SGPEmpleadoRequest
{
    private String token;
    private Integer codigoError = null;
    private String mensajeError = null;

    public SGPEmpleadoResponse() {
        super();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    public Integer getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(Integer codigoError) {
        this.codigoError = codigoError;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
    
    @Override
    public boolean equals(Object obj)
    {
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
        return Objects.equals(super.getNumero(), other.getNumero());
    }

    @Override
    public String toString() {
        return "SincronizacionEmpleadoResponse[" + "Numero: " + super.getBiometrico1() + ", Biometrico 1: " + super.getBiometrico1() +
                ", Biometrico 2: " + super.getBiometrico2() + ", Token: " + token + ", Codigo Error: " + codigoError + ", Mensaje Error: " + mensajeError + ']';
    }
    
    
    
}
