/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hoth.fingerprint.exceptions;

/**
 *
 * @author Alberto
 */
public class FPClientOperationException extends Exception
{
    private static final long serialVersionUID = -829097821834065662L;

    public FPClientOperationException() 
    {
        super();
    }

    public FPClientOperationException(String message) 
    {
        super(message);
    }

    public FPClientOperationException(Throwable cause) 
    {
        super(cause);
    }

    public FPClientOperationException(String message, Throwable cause) 
    {
        super(message, cause);
    }
    
}
