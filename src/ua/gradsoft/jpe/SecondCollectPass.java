/*
 * SecondCollectPass.java
 *
 * Created on October 5, 2007, 3:35 PM
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
public class SecondCollectPass implements Pass
{

    
    public boolean isReadInput() {
        return true;
    }
    
    

    public boolean isWriteOutput() {
        return false;
    }
        

    public boolean isWalk() {
        return false;
    }
    
    

    public Term transform(JavaTypeModel typeModel, JPEFacts facts, Configuration configuration) throws TermWareException, EntityNotFoundException {
        return null;
    }
    
    public static final SecondCollectPass INSTANCE = new SecondCollectPass();
    
}
