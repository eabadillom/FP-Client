/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.tools;

import java.sql.Timestamp;
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
    
}
