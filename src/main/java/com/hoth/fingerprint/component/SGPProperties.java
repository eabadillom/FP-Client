/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Alberto
 */
public class SGPProperties 
{
    private String url = null;
    private String appUser = null;
    private String appPassword = null;
    private String sgpTimeout = null;
    private final int numeroEmpleado;
    private final int captureTimeout;

    public SGPProperties() throws IOException 
    {
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/application.properties");
        properties.load(in);
        this.url = properties.getProperty("sgp.url");
        this.appUser = properties.getProperty("sgp.app.user");
        this.appPassword = properties.getProperty("sgp.app.password");
        this.sgpTimeout = properties.getProperty("sgp.read.timeout");
        this.numeroEmpleado = Integer.parseInt(properties.getProperty("sgp.empleado.numeroEmpleado"));
        this.captureTimeout = Integer.parseInt(properties.getProperty("sgp.capture.timeout"));
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getAppUser() {
        return appUser;
    }

    public String getAppPassword() {
        return appPassword;
    }

    public String getSgpTimeout() {
        return sgpTimeout;
    }
    
    public int getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public int getCaptureTimeout() {
        return captureTimeout;
    }
    
}
