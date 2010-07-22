/*
 * JPEConfigurationException.java
 *
 *
 * Copyright (c) 2006 - 2007 GradSoft  Ukraine
 * All Rights Reserved
 */


package ua.gradsoft.jpe;

/**
 *Exception which is throwing during incorrect configuration.
 * @author Ruslan Shevchenko
 */
public class JPEConfigurationException extends Exception
{
    JPEConfigurationException(String message)
    { super(message); }

    JPEConfigurationException(String message, Exception ex)
    { super(message,ex); }
    
}
