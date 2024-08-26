/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.tools;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author Alberto
 */
public class DateUtils 
{
    private Date fechaD = null;
    private Timestamp fechaT = null;

    public Date getFechaD() {
        return fechaD;
    }

    public void setFechaD(Date fechaD) {
        this.fechaD = fechaD;
    }

    public Timestamp getFechaT() {
        return fechaT;
    }

    public void setFechaT(Timestamp fechaT) {
        this.fechaT = fechaT;
    }
    
    public Timestamp DateToTimestamp(Date fecha)
    {
        this.fechaD = fecha;  
        Timestamp ts = new Timestamp(this.fechaD.getTime());
        
        return ts;
    }
    
    public Date TimestampToDate(Timestamp fecha)
    {
        this.fechaT = fecha;
        Date date = new Date(this.fechaT.getTime());
        
        return date;
    }
    
    public Date LocalDate(Date fecha)
    {
        LocalDate localDate = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        
        int anio = localDate.getYear();
        int mes = localDate.getMonthValue();
        int dia = localDate.getDayOfMonth();
        
        LocalDate localNewDate = LocalDate.of(anio, mes, dia);
        Date date = java.sql.Date.valueOf(localNewDate);
        return date;
    }
    
    public Date OffsetDateTimeToDate(OffsetDateTime offsetDateTime)
    {
        Instant instant = offsetDateTime.toInstant();
        Date date = Date.from(instant);
        return date;
    }
    
    public OffsetDateTime DateToOffsetDateTime(Date date)
    {
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(instant, zoneId);
        return offsetDateTime;
    }
    
}
