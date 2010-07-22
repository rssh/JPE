/*
 * JPEProcessingException.java
 *
 *
 * Copyright (c) 2006 GradSoft  Ukraine
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

/**
 *Handler for exceptions, which are throwed during JPE processing.
 * @author Ruslan Shevchenko
 */
public class JPEProcessingException extends Exception
{
    
    public JPEProcessingException(String message)
    { super(message); }
    
    public JPEProcessingException(String message, Exception ex)
    { super(message,ex); }
    
}
