/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.exceptions;

/**
 *
 * @author Alberto
 */
public class FPClientDataBaseException extends Exception
{
    private static final long serialVersionUID = -829097821834065662L;

    public FPClientDataBaseException() 
    {
        super();
    }

    public FPClientDataBaseException(String message) 
    {
        super(message);
    }

    public FPClientDataBaseException(Throwable cause) 
    {
        super(cause);
    }

    public FPClientDataBaseException(String message, Throwable cause) 
    {
        super(message, cause);
    }
    
}
