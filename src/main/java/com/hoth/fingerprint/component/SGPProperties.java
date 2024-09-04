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
    private String idFpClient = null;
    private String password = null;
    private String appUser = null;
    private String appPassword = null;
    private String sgpTimeout = null;
    

    public SGPProperties() throws IOException {
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/application.properties");
        properties.load(in);
        this.url = properties.getProperty("sgp.url");
        this.idFpClient = properties.getProperty("idFpClient");
        this.password = properties.getProperty("password");
        this.appUser = properties.getProperty("app.user");
        this.appPassword = properties.getProperty("app.password");
        this.sgpTimeout = properties.getProperty("sgp.timeout");
    }
    
    public String getUrl() {
        return url;
    }

    public String getIdFpClient() {
        return idFpClient;
    }

    public String getPassword() {
        return password;
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
    
    /*public String URL(String url) throws IOException
    {
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/application.properties");
        properties.load(in);
        return properties.getProperty("sgp.url") + url;
    }
    
    public String[] usuarioContrasenia() throws IOException
    {
        String[] datos = null;
        Properties properties = new Properties();
        InputStream in = getClass().getResourceAsStream("/application.properties");
        properties.load(in);
        
        datos[0] = properties.getProperty("idFpClientAPI");
        datos[1] = properties.getProperty("password");
        
        return datos;
    }*/
    
}
