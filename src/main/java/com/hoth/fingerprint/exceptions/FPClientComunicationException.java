/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.exceptions;

/**
 *
 * @author Alberto
 */
public class FPClientComunicationException extends Exception
{
    private static final long serialVersionUID = -829097821834065662L;

    public FPClientComunicationException() 
    {
        super();
    }

    public FPClientComunicationException(String message) 
    {
        super(message);
    }

    public FPClientComunicationException(Throwable cause) 
    {
        super(cause);
    }

    public FPClientComunicationException(String message, Throwable cause) 
    {
        super(message, cause);
    }
    
}
