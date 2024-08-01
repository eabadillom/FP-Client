/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.tools;

/**
 *
 * @author Alberto
 */
public class FPComunicationException extends Exception
{
    private static final long serialVersionUID = -829097821834065662L;

    public FPComunicationException() 
    {
        super();
    }

    public FPComunicationException(String message) 
    {
        super(message);
    }

    public FPComunicationException(Throwable cause) 
    {
        super(cause);
    }

    public FPComunicationException(String message, Throwable cause) 
    {
        super(message, cause);
    }
    
}
