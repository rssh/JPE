/*
 * Pass.java
 *
 * Created on October 5, 2007, 1:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ua.gradsoft.jpe;

import ua.gradsoft.javachecker.EntityNotFoundException;
import ua.gradsoft.javachecker.models.JavaTypeModel;
import ua.gradsoft.termware.Term;
import ua.gradsoft.termware.TermWareException;

/**
 *
 * @author rssh
 */
public interface Pass {
    
    
    public boolean isReadInput();
    
    public boolean isWriteOutput();
    
    public boolean isWalk();
    
    /**
     * return transformed type model.
     */
    public Term  transform(JavaTypeModel typeModel, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException;
    
    
}
