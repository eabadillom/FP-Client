/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Alberto
 */
public class SGPAsistenciaRequest 
{
    private UUID uuid;
    private String numero = null;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC-6")
    private OffsetDateTime horaEntrada = null;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC-6")
    private OffsetDateTime horaSalida = null;

    public UUID getUUId() {
        return uuid;
    }

    public void setUUId(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public OffsetDateTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(OffsetDateTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public OffsetDateTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(OffsetDateTime horaSalida) {
        this.horaSalida = horaSalida;
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
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public String toString() {
        return "SincronizacionAsistenciaRequest[" + "Id Asistencia: " + this.uuid + ",Numero Empleado: " 
                + this.numero + ", Fecha Entrada: " + this.horaEntrada + ", Fecha Salida: " + this.horaSalida + ']';
    }
    
}
