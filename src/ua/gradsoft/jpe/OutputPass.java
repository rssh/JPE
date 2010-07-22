/*
 * OutputPass.java
 *
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWareException;

/**
 *Pass used for output.
 * @author rssh
 */
public class OutputPass implements Pass
{
      
    public boolean isReadInput() {
        return false;
    }
        

    public boolean isWalk() {
        return false;
    }
    
    
    public boolean isWriteOutput() {
        return true;
    }
        

    public Term transform(JavaTypeModel typeModel, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException {
        return null;
    }

    public static final OutputPass INSTANCE = new OutputPass();
    
}
