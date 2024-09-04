/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.tools;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author Alberto
 */
public class DateUtils 
{
    private Date fechaD = null;
    private Timestamp fechaT = null;
    private OffsetDateTime offsetDateTime = null;

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

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
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
    
    public Date dateWithTimeZone(Date fecha)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fecha);
        calendar.setTimeZone(TimeZone.getTimeZone("Etc/GMT-6"));
        Date utcDate = calendar.getTime();
        return utcDate;
    }
    
    public Date OffsetDateTimeToDate(OffsetDateTime offsetDateTime)
    {
        Date date = Date.from(offsetDateTime.toInstant());
        return date;
    }
    
    public OffsetDateTime DateToOffsetDateTime(Date date)
    {
        ZoneOffset zoneOffset = ZoneOffset.of("-06:00");
        OffsetDateTime offsetDateTime = date.toInstant().atOffset(zoneOffset);
        return offsetDateTime;
    }
    
    public Date obtenerFechaSistema()
    {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-6:00"), new Locale("es", "MX"));
        return cal.getTime();
    }
    
}
