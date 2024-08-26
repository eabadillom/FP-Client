/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.response;

import com.hoth.fingerprint.model.request.SGPAsistenciaRequest;
import java.util.Objects;

/**
 *
 * @author Alberto
 */
public class SGPAsistenciaResponse extends SGPAsistenciaRequest
{
    private Integer codigoError = null;
    private String mensajeError = null;

    public SGPAsistenciaResponse() 
    {
        super();
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
        int hash = 7;
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
        final SGPAsistenciaRequest other = (SGPAsistenciaRequest) obj;
        return Objects.equals(super.getNumero(), other.getNumero());
    }

    @Override
    public String toString() 
    {
        return "SincronizacionAsistenciaResponse[" + "Id Asistencia: " + super.getUUId() + ", Numero Empleado: " 
                + super.getNumero() + ", Fecha Entrada: " + super.getHoraEntrada() + ", Fecha Salida: " 
                + super.getHoraSalida() + ", Codigo Error: " + codigoError 
                + ", Mensaje Error: " + mensajeError + ']';
    }
    
}
