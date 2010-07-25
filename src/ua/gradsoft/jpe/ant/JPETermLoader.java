/*
 * JPETermLoader.java
 *
 */

package ua.gradsoft.jpe.ant;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import ua.gradsoft.termware.InputStreamSource;
import ua.gradsoft.termware.TermLoader;
import ua.gradsoft.termware.exceptions.AssertException;
import ua.gradsoft.termware.exceptions.ExternalException;
import ua.gradsoft.termware.exceptions.ResourceNotFoundException;
import ua.gradsoft.termware.termloaders.FileOrClassTermLoader;
import ua.gradsoft.termware.termloaders.GenericInputStreamSource;

/**
 *TermLoader, which set in TermWare instance to load system definitions
 *from jar, where JPETermLoader is defined, not from systems. 
 * @author rssh
 */
public class JPETermLoader extends TermLoader
{
 
    public void addSearchPath(String path)
    {
        // do nothing.
    }
    
    public List<String> getSearchPathes()
    { return Collections.emptyList(); }
   
    public InputStreamSource getSource(String name) throws ResourceNotFoundException, ExternalException
    {
      try {
          return fsTermLoader.getSource(name);
      } catch (ResourceNotFoundException ex) {
          /* do nothing */
      }
      ClassLoader cl=JPETermLoader.class.getClassLoader();
      if (cl==null) {
          throw new ExternalException(new AssertException("Can't get classloader for JPETermLoader"));
      }
      InputStream inputStream=cl.getResourceAsStream(name);
      if (inputStream==null) {
          throw new ResourceNotFoundException(name);
      }
      return new GenericInputStreamSource(inputStream,name);
    }
    
    FileOrClassTermLoader fsTermLoader = new FileOrClassTermLoader();
    
}
